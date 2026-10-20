import { useAuthStore } from './stores/auth'

/**
 * 站内通知 WebSocket 客户端（优化11）：
 * - 断线自动重连 + 30s 心跳保活
 * - 消息分发到订阅者（UserLayout 未读角标 / 通知页实时刷新）
 * - 未登录不连接；退出登录断开
 */
let socket = null
let reconnectTimer = null
let heartbeatTimer = null
const listeners = new Set()

export function connectWs() {
  const auth = useAuthStore()
  if (!auth.token || socket) return
  const proto = location.protocol === 'https:' ? 'wss' : 'ws'
  const url = `${proto}://${location.host}/ws/notify?token=${encodeURIComponent(auth.token)}`
  socket = new WebSocket(url)

  socket.onopen = () => {
    heartbeatTimer = setInterval(() => {
      if (socket && socket.readyState === WebSocket.OPEN) socket.send('ping')
    }, 30000)
  }

  socket.onmessage = (e) => {
    try {
      const msg = JSON.parse(e.data)
      listeners.forEach((fn) => fn(msg))
    } catch { /* 非 JSON 消息忽略 */ }
  }

  socket.onclose = () => {
    cleanup()
    scheduleReconnect()
  }

  socket.onerror = () => {
    if (socket) socket.close()
  }
}

export function disconnectWs() {
  cleanup()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

export function onWsMessage(fn) {
  listeners.add(fn)
  return () => listeners.delete(fn)
}

function cleanup() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
  if (socket) {
    try { socket.close() } catch { /* ignore */ }
    socket = null
  }
}

function scheduleReconnect() {
  reconnectTimer = setTimeout(connectWs, 5000)
}
