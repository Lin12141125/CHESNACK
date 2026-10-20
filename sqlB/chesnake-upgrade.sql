-- =============================================================
-- CHESNAKE 增量升级脚本（在 chesnake-merged.sql 基础上执行）
-- 新增：MQ 本地消息表 / 消费幂等表 / 死信补偿表
-- 执行方式: mysql -uroot -p chesnake < chesnake-upgrade.sql
-- =============================================================
USE chesnake;

-- 本地消息表：生产者发送前落库，Confirm 回调更新状态，补偿任务扫描重投
CREATE TABLE IF NOT EXISTS `mq_outbox` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `msg_id` VARCHAR(64) NOT NULL COMMENT '消息唯一ID(UUID)',
  `exchange` VARCHAR(100) NOT NULL,
  `routing_key` VARCHAR(100) NOT NULL,
  `payload` TEXT NOT NULL COMMENT '消息体JSON',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待确认 1-已确认 2-确认失败',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbox_msg` (`msg_id`),
  KEY `idx_outbox_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ本地消息表(生产者可靠性)';

-- 消费幂等表：消费者处理前先抢占记录，唯一索引兜底重复消费
CREATE TABLE IF NOT EXISTS `mq_processed` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `msg_id` VARCHAR(64) NOT NULL COMMENT '消息唯一ID',
  `consumer` VARCHAR(64) NOT NULL COMMENT '消费者标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_processed` (`msg_id`, `consumer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ消费幂等表';

-- 死信补偿表：重试耗尽仍失败的消息落表，供人工/定时补偿
CREATE TABLE IF NOT EXISTS `mq_dead_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `msg_id` VARCHAR(64) DEFAULT NULL,
  `queue` VARCHAR(100) DEFAULT NULL COMMENT '原业务队列',
  `body` TEXT COMMENT '消息体',
  `error` VARCHAR(500) DEFAULT NULL COMMENT '最后一次失败原因',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0-待处理 1-已处理',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dead_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ死信补偿记录';

-- follow 表补充「关注者 email」列（社区通知按 email 落库/推送），幂等可重复执行
SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = 'chesnake' AND TABLE_NAME = 'follow' AND COLUMN_NAME = 'followee_email');
SET @sql := IF(@col = 0,
    'ALTER TABLE follow ADD COLUMN followee_email VARCHAR(50) DEFAULT NULL COMMENT ''关注者email'' AFTER followee',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 历史数据回填：按用户名反查用户表补齐 followee_email
UPDATE follow f
JOIN `user` u ON u.username = f.followee
SET f.followee_email = u.email
WHERE f.followee_email IS NULL;

-- =============================================================
-- orders 唯一索引从 (order_no) 改为 (order_no, good_id)：支持一单多商品
-- （旧索引下多商品订单第二条 insert 必撞唯一键，导致库存已扣但订单/购物车清理丢失）
-- 幂等可重复执行
SET @uk := (SELECT COUNT(*) FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = 'chesnake' AND TABLE_NAME = 'orders'
              AND INDEX_NAME = 'uk_order_no');
SET @sql := IF(@uk > 0,
    'ALTER TABLE orders DROP INDEX uk_order_no, ADD UNIQUE KEY uk_order_good (order_no, good_id)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
