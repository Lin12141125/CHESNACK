# CHESNAKE （Vue3 + Spring Boot）

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue3 + Vite + Element Plus + Pinia + Vue Router + Axios + 原生 WebSocket |
| 后端 | Spring Boot 4.1.1 + Spring Security(JWT) + MyBatis-Plus 3.5.17 + Sentinel 1.8 + springdoc-openapi 3 |
| 搜索 | Elasticsearch 8.19（多字段加权检索，不可用自动降级 MySQL LIKE） |
| 中间件 | MySQL 8（单库 chesnake）、Redis 7、RabbitMQ 3、Elasticsearch 8（Docker） |

## 目录结构

- `backend/` Spring Boot 后端（Controller-Service-Mapper 三层）
- `frontend/` Vue3 前端
- `sqlB/chesnake-merged.sql` 合并后单库建表脚本（含 40 商品种子数据 + MQ 可靠性三表）
- `sqlB/chesnake-upgrade.sql` 增量升级脚本（本地老库执行一次即可）
- `docker-compose.yml` 六服务一键部署（MySQL/Redis/RabbitMQ/ES/backend/frontend）
- 原 JSP 版（`src/`、`WebContent/`）保留作参考

## 启动步骤

1. 中间件：
   - MySQL：本地 3306，库 `chesnake`（执行 `sqlB/chesnake-merged.sql`；老库追加执行 `sqlB/chesnake-upgrade.sql`）
   - Redis：`docker run -d --name chesnake-redis -p 6379:6379 redis:7-alpine --requirepass chesnake123`
   - RabbitMQ：`docker run -d --name chesnake-rabbitmq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=chesnake -e RABBITMQ_DEFAULT_PASS=chesnake123 rabbitmq:3-management`
   - Elasticsearch（可选）：`docker run -d --name chesnake-es -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.19.0`
2. 后端：`backend/` 下 `mvn spring-boot:run`（8081）
3. 前端：`frontend/` 下 `npm install && npm run dev`（5173，代理 /api、/uploads、/ws 到 8081）
4. 访问 http://localhost:5173
5. 一键部署：根目录 `docker compose up -d --build`（复制 `.env.example` 为 `.env`）

## 核心设计

### 秒杀链路（订单 N抢1 全链路防线）
- 信号量限流：同时在途下单请求上限（200）
- DB 库存预检快速失败 → Redis Lua 原子「多商品检查+预扣」（任一不足整体回滚）
- 库存预热：应用启动全量预热（SETNX）；管理员变更商品覆盖同步
- 消费者 DB 原子条件扣减 `WHERE stock >= n`（最终防线防超卖）+ orderNo 唯一索引兜底
- Sentinel：下单接口 QPS 20 限流 + 慢调用比例熔断（1s/50%/熔断 10s），商品详情 QPS 100

### MQ 可靠性闭环
- 生产者：本地消息表 `mq_outbox` 落库 → 发送（消息头带 msgId）→ Publisher Confirm 回调更新状态 → 补偿任务 30s 扫描超时未确认重投
- 消费者：手动 Ack（事务提交成功才确认）；消费幂等表 `mq_processed`（与业务同事务，回滚不残留）
- 失败链路：basicNack(requeue=false) → DLX → 死信消费者按 `x-chesnake-retry` 计数重投原队列（上限 3 次）→ 耗尽落 `mq_dead_record` 补偿表；订单死信回补 Redis 预扣库存

### 缓存三场景防护 + 双删一致性（GoodsService）
- **缓存穿透**：商品 ID 参数校验 + 空值缓存（`__NULL__`，TTL 5 分钟）
- **缓存击穿**：Redis `SETNX` 互斥锁（锁值 UUID，Lua 脚本原子比对释放防误删）+ 双重校验 + 未抢到锁等待重试（锁 TTL 10s）
- **缓存雪崩**：过期时间 = 基础 TTL + 随机偏移（详情 30min+0~10min；列表 5min+0~3min）
- **一致性**：写库前删缓存 → 写库 → 写库后删缓存 → 500ms 延迟双删（异步兜底并发窗口）；列表缓存用 SCAN 游标清理避免 KEYS 阻塞
- 下单（消费者）失效详情+列表缓存；管理员增删改商品走双删；列表缓存只存轻量 `PageResult`

### Elasticsearch 全文搜索
- 多字段加权（name×3 > brand×2 > type/origin）+ 销量排序
- 应用启动异步全量建索引；管理员增删改增量同步
- ES 不可用自动降级 MySQL LIKE，保证搜索可用性

### WebSocket 实时通知
- `/ws/notify` 端点，握手 JWT 认证（token 走 query 参数）+ 黑名单校验
- 站内通知 / 订单完成实时推送；前端断线自动重连 + 30s 心跳 + 60s 轮询兜底

### RabbitMQ 异步解耦（chesnake.exchange）
| 队列 | 场景 |
|---|---|
| chesnake.order.pay.queue | 下单支付：生成订单 + 扣库存 + 清购物车 + 热度+3（消费幂等表 + 唯一索引 DuplicateKey 兜底；事务模板整体回滚） |
| chesnake.notify.queue | 注册欢迎通知（落库后 WS 实时推送） |
| chesnake.community.event.queue | 发帖→通知粉丝；评论→通知帖子作者（落库后 WS 推送） |

### API 文档与测试
- springdoc-openapi 3：`/swagger-ui.html`（开发环境）
- 单元测试：秒杀库存 Lua 预扣、消费幂等、搜索降级、JWT 黑名单
| chesnake.audit.log.queue | 管理员操作审计日志 |
| chesnake.dead.queue（死信） | 重试耗尽的消息人工补偿入口 |

### 订单并发「N抢1」防线
1. pay 接口提交前库存预检（快速失败）
2. 消费者 DB 原子条件扣减 `UPDATE ... SET stock=stock-n WHERE stock>=n`（防超卖最终防线）
3. orderNo 唯一索引 + DuplicateKeyException 幂等兜底

### 安全加固
- 注册一律 `role=2`（防注册提权）；评价只能评价自己的订单；退出登录 token 进 Redis 黑名单实现 JWT 主动吊销

### API 约定
- 统一响应 `R{code,msg,data}`，code=200 成功 / 401 未登录
- 认证：`Authorization: Bearer <JWT>`；`/api/auth/**` 公开，`/api/admin/**` 需 ADMIN
- 测试账号：newuser@qq.com/123456（用户）、admin2@qq.com/admin123（管理员）、userb@qq.com/123456
