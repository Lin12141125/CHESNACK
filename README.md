# CHESNACK 零食商城

> 休闲零食主题电商平台。前后端分离架构,集高并发秒杀、消息可靠性保障、三级缓存防护、全文搜索与实时通知于一体。

## 项目简介

CHESNACK 是一个休闲零食主题的 B2C 电商平台,涵盖辣条、面包、饼干、巧克力、坚果等休闲零食的商品浏览与搜索、购物车、下单支付、订单管理、评价、社区(发帖/评论/关注/上传)、站内实时通知与管理员后台等完整业务闭环。

项目采用 **Vue3 + Spring Boot** 前后端分离架构,配套 MySQL、Redis、RabbitMQ、Elasticsearch 四大中间件,支持 Docker Compose 一键部署。旧版 JSP + Servlet 实现保留在 `CHESNACK/src` 与 `CHESNACK/WebContent`,作为演进参考。

### 主要功能

- 🛒 **商品与交易**:商品分页/排序、购物车、下单支付、订单管理、售后评价
- 🔍 **智能搜索**:Elasticsearch 多字段加权全文检索,故障自动降级 MySQL LIKE
- ⚡ **秒杀高并发**:订单「N 抢 1」场景下的全链路防超卖防线
- 💬 **社区**:发帖、评论、关注、素材上传、粉丝/作者动态通知
- 🔔 **实时通知**:WebSocket 站内信与订单完成推送,断线重连 + 轮询兜底
- 🛡️ **安全**:Spring Security + JWT、角色隔离、防注册提权、登出令牌吊销
- 📊 **管理后台**:商品管理、库存维护、销售报表、操作审计日志

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios + 原生 WebSocket |
| 后端 | Spring Boot 4.1.1 + Spring Security(JWT) + MyBatis-Plus 3.5.17 + Sentinel + springdoc-openapi 3(Java 17) |
| 数据库 | MySQL 8(单库 `chesnake`) |
| 缓存 | Redis 7 |
| 消息队列 | RabbitMQ 3(可靠性消息 + 死信重试) |
| 搜索 | Elasticsearch 8.19 |
| 部署 | Docker + Docker Compose + Nginx |

## 技术亮点

### 1. 秒杀链路「N 抢 1」全链路防线

- **信号量限流**:同时在途下单请求上限(200)
- **库存预检 + 原子预扣**:DB 库存预检快速失败 → Redis Lua 原子「多商品检查 + 预扣」(任一不足整体回滚)
- **库存预热**:应用启动全量预热(SETNX);管理员变更商品覆盖同步
- **防超卖最终防线**:消费者 DB 原子条件扣减 `WHERE stock >= n` + orderNo 唯一索引兜底
- **Sentinel 流控**:下单接口 QPS 20 限流 + 慢调用比例熔断(1s/50%/熔断 10s),商品详情 QPS 100

### 2. RabbitMQ 消息可靠性闭环

- **生产者**:本地消息表 `mq_outbox` 落库 → 发送(消息头带 msgId)→ Publisher Confirm 回调更新状态 → 补偿任务 30s 扫描超时未确认重投
- **消费者**:手动 Ack(事务提交成功才确认);消费幂等表 `mq_processed`(与业务同事务,回滚不残留)
- **失败链路**:basicNack(requeue=false)→ DLX → 死信消费者按 `x-chesnake-retry` 计数重投原队列(上限 3 次)→ 耗尽落 `mq_dead_record` 补偿表;订单死信回补 Redis 预扣库存

| 队列 | 场景 |
|---|---|
| chesnake.order.pay.queue | 下单支付:生成订单 + 扣库存 + 清购物车 + 热度+3(幂等表 + 唯一索引兜底;事务整体回滚) |
| chesnake.notify.queue | 注册欢迎通知(落库后 WS 实时推送) |
| chesnake.community.event.queue | 发帖→通知粉丝;评论→通知帖子作者(落库后 WS 推送) |
| chesnake.audit.log.queue | 管理员操作审计日志 |
| chesnake.dead.queue(死信) | 重试耗尽的消息人工补偿入口 |

### 3. 缓存三场景防护 + 双删一致性

- **缓存穿透**:商品 ID 参数校验 + 空值缓存(`__NULL__`,TTL 5 分钟)
- **缓存击穿**:Redis `SETNX` 互斥锁(锁值 UUID,Lua 原子比对释放防误删)+ 双重校验 + 未抢到锁等待重试(锁 TTL 10s)
- **缓存雪崩**:过期时间 = 基础 TTL + 随机偏移(详情 30min+0~10min;列表 5min+0~3min)
- **一致性**:写库前删缓存 → 写库 → 写库后删缓存 → 500ms 延迟双删(异步兜底并发窗口);列表缓存用 SCAN 游标清理避免 KEYS 阻塞

### 4. Elasticsearch 全文搜索与降级

- 多字段加权(name×3 > brand×2 > type/origin)+ 销量排序
- 应用启动异步全量建索引;管理员增删改增量同步
- ES 不可用自动降级 MySQL LIKE,保证搜索可用性

### 5. WebSocket 实时通知

- `/ws/notify` 端点,握手 JWT 认证(token 走 query 参数)+ 黑名单校验
- 站内通知 / 订单完成实时推送;前端断线自动重连 + 30s 心跳 + 60s 轮询兜底

### 6. 安全加固

- 注册一律 `role=2`(防注册提权);评价只能评价自己的订单;退出登录 token 进 Redis 黑名单实现 JWT 主动吊销

## 目录结构

```
CHESNACK(仓库根)
├── CHESNACK/                  # 重构版(前后端分离)
│   ├── backend/               # Spring Boot 后端(Controller-Service-Mapper 三层)
│   ├── frontend/              # Vue3 前端
│   ├── docker-compose.yml     # 六服务一键编排
│   ├── .env.example           # 环境变量模板
│   ├── src/                   # 旧版 JSP 后端(Servlet + DAO)
│   └── WebContent/            # 旧版 JSP 页面
└── sqlB/                      # 数据库脚本
    ├── chesnake-merged.sql    # 建库建表 + 40 商品种子数据 + MQ 可靠性三表
    └── chesnake-upgrade.sql   # 旧库增量升级脚本
```

## 部署方式

### 方式一:Docker Compose 一键部署(推荐)

1. `cd CHESNACK`
2. 复制 `.env.example` 为 `.env`,按需修改数据库/Redis 等密码
3. `docker compose up -d --build`
4. 访问 `http://localhost`(前端 Nginx);后端 API 位于 `http://localhost:8081`;MySQL 映射到宿主 3307

首次启动 MySQL 会自动执行 `sqlB/chesnake-merged.sql`(建库建表 + 种子数据)。各中间件配置健康检查,后端会等待 MySQL/Redis/RabbitMQ 就绪后再启动。

### 方式二:本地开发部署

1. **MySQL**:本地 3306,创建库 `chesnake`,执行 `sqlB/chesnake-merged.sql`(旧库追加执行 `sqlB/chesnake-upgrade.sql`)
2. **Redis**:`docker run -d --name chesnake-redis -p 6379:6379 redis:7-alpine --requirepass chesnake123`
3. **RabbitMQ**:`docker run -d --name chesnake-rabbitmq -p 5672:5672 -p 15672:15672 -e RABBITMQ_DEFAULT_USER=chesnake -e RABBITMQ_DEFAULT_PASS=chesnake123 rabbitmq:3-management`
4. **Elasticsearch(可选)**:`docker run -d --name chesnake-es -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.19.0`
5. **后端**:`cd CHESNACK/backend && mvn spring-boot:run`(8081)
6. **前端**:`cd CHESNACK/frontend && npm install && npm run dev`(5173,已代理 /api、/uploads、/ws 到 8081)
7. 访问 `http://localhost:5173`

## API 约定

- 统一响应结构 `R{code,msg,data}`:`code=200` 成功、`401` 未登录
- 认证方式:`Authorization: Bearer <JWT>`
- 权限:`/api/auth/**` 公开;`/api/admin/**` 需 ADMIN 角色
- 在线文档:Swagger UI `http://localhost:8081/swagger-ui.html`(开发环境)

## 测试账号

| 角色 | 邮箱 | 密码 |
|---|---|---|
| 用户 | newuser@qq.com | 123456 |
| 用户 | userb@qq.com | 123456 |
| 管理员 | admin2@qq.com | admin123 |

## 单元测试

覆盖秒杀库存 Lua 预扣、消费幂等、搜索降级、JWT 黑名单等核心场景(`CHESNACK/backend/src/test`)。
