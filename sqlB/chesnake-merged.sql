-- =============================================================
-- CHESNAKE 重构版数据库（合并单库）
-- 执行方式: mysql -uroot -p < chesnake-merged.sql
-- 表结构由原 chesnake.sql + user.sql 合并并规范化而来
-- =============================================================
CREATE DATABASE IF NOT EXISTS chesnake DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE chesnake;

SET FOREIGN_KEY_CHECKS=0;

-- 用户（原 login 表）
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(50) NOT NULL COMMENT '账号',
  `username` VARCHAR(50) NOT NULL COMMENT '昵称',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt密文',
  `role` TINYINT NOT NULL DEFAULT 2 COMMENT '1-管理员 2-用户',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 商品（原 goods 表，id 改自增，新增 stock 库存列）
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(20) NOT NULL,
  `name` VARCHAR(200) NOT NULL,
  `origin` VARCHAR(20) NOT NULL,
  `brand` VARCHAR(50) NOT NULL,
  `net_content` INT NOT NULL COMMENT '净含量',
  `price` DECIMAL(18,2) NOT NULL,
  `sale_quantity` INT NOT NULL DEFAULT 0 COMMENT '销量',
  `stock` INT NOT NULL DEFAULT 100 COMMENT '库存(下单扣减)',
  `img1` VARCHAR(500) DEFAULT NULL,
  `img2` VARCHAR(500) DEFAULT NULL,
  `score` INT NOT NULL DEFAULT 0 COMMENT '热度分',
  PRIMARY KEY (`id`),
  KEY `idx_goods_type` (`type`),
  KEY `idx_goods_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

-- 购物车（原 cart 表，email+good_id 唯一）
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(50) NOT NULL,
  `good_id` BIGINT NOT NULL,
  `amount` INT NOT NULL DEFAULT 1,
  `price` DECIMAL(18,2) NOT NULL COMMENT '小计(单价*数量)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart` (`email`,`good_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- 订单（原 order 表，改名为 orders 避开保留字，新增订单号/状态）
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `email` VARCHAR(50) NOT NULL,
  `good_id` BIGINT NOT NULL,
  `amount` INT NOT NULL,
  `price` DECIMAL(18,2) NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1-已支付 0-待支付',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_good` (`order_no`, `good_id`),
  KEY `idx_orders_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- 商品评价（原 comment 表）
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `goods_id` BIGINT NOT NULL,
  `order_id` BIGINT NOT NULL,
  `pid` BIGINT DEFAULT NULL COMMENT '父评论ID，非空则为二级评论',
  `level` INT DEFAULT NULL COMMENT '评论星级(1-5)',
  `reported` INT NOT NULL DEFAULT 0 COMMENT '被举报次数标记',
  PRIMARY KEY (`id`),
  KEY `idx_comment_goods` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价';

-- 社区帖子（原 community 表）
DROP TABLE IF EXISTS `community`;
CREATE TABLE `community` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) DEFAULT NULL,
  `content` VARCHAR(1000) DEFAULT NULL,
  `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `username` VARCHAR(50) DEFAULT NULL,
  `account` VARCHAR(50) DEFAULT NULL COMMENT '作者email',
  `img1` VARCHAR(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子';

-- 关注（原 follow 表）
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `account` VARCHAR(50) DEFAULT NULL COMMENT '被关注者email',
  `followee` VARCHAR(50) DEFAULT NULL COMMENT '关注者用户名',
  `followee_email` VARCHAR(50) DEFAULT NULL COMMENT '关注者email',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`account`,`followee`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系';

-- 社区评论（原 community_comment 表）
DROP TABLE IF EXISTS `community_comment`;
CREATE TABLE `community_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `community_id` BIGINT NOT NULL,
  `pid` BIGINT DEFAULT NULL COMMENT '父评论ID',
  PRIMARY KEY (`id`),
  KEY `idx_cc_community` (`community_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区评论';

-- 站内通知（RabbitMQ 注册成功等异步消息落库）
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(50) NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` VARCHAR(500) DEFAULT NULL,
  `is_read` TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notify_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知';

-- 管理员操作审计（RabbitMQ 异步记录）
DROP TABLE IF EXISTS `admin_log`;
CREATE TABLE `admin_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) DEFAULT NULL,
  `action` VARCHAR(100) DEFAULT NULL,
  `detail` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志';

-- MQ本地消息表（生产者可靠性：Confirm 回调 + 补偿重投）
DROP TABLE IF EXISTS `mq_outbox`;
CREATE TABLE `mq_outbox` (
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

-- MQ消费幂等表（消费者处理前抢占，唯一索引兜底重复消费）
DROP TABLE IF EXISTS `mq_processed`;
CREATE TABLE `mq_processed` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `msg_id` VARCHAR(64) NOT NULL COMMENT '消息唯一ID',
  `consumer` VARCHAR(64) NOT NULL COMMENT '消费者标识',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_processed` (`msg_id`, `consumer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ消费幂等表';

-- MQ死信补偿表（重试耗尽仍失败的消息落表）
DROP TABLE IF EXISTS `mq_dead_record`;
CREATE TABLE `mq_dead_record` (
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

-- ==================== 种子数据 ====================

INSERT INTO `goods` (`type`,`name`,`origin`,`brand`,`net_content`,`price`,`sale_quantity`,`stock`,`img1`,`img2`,`score`) VALUES
('dried meat','Natural slices of pork breast made with herb monosodium glutamate','Sichuan province','BE&CHEERY',100,8.00,10000,100,'1.jpg','2.jpg',0),
('puffed food','Want Want small crisp delicious nostalgic snack gift package to satisfy cravings net red puffed casual snack food small package','Henan province','Tao Food Style',200,15.00,10000,100,'3.jpg','4.png',0),
('chili','Yanjin shop spicy bar snack casual snack food network red burst spicy bar gift package dormitory to satisfy hunger','Hunan province','yanjin shop',72,5.00,10000,100,'5.jpg_.webp','6.webp',0),
('puffed food','Bibizan palm dry noodles eat mixed Turkey crispy noodles full box bag instant noodles','Fujian Province','BGRIMM',420,9.00,1000,100,'7.jpg_.webp','8.jpg_.webp',0),
('candy','Vietnam import Ben Ben Ben Ben Ben Ben Ben cocoa special concentrated coconut sugar old packaging Hainan specialty childhood nostalgia snack','Hunan province','Ben Tre',400,13.00,2000,100,'9.jpg_.webp','10.jpg_.webp',0),
('confectionery','Huaishan Coix barley pastry heart full low-fat wheat coarse grain bread breakfast whole box of non-saccharin snack zero food','Hunan province','Dieva yunnanensis',25,5.00,400,100,'11.jpg_.webp','12.jpg_.webp',0),
('bread','Free delivery Fortune More hand-torn bread 400g*1 box full box meal replacement cake casual snacks hand-torn snacks','Fujian Province','Have many blessings',40,8.00,400,100,'13.jpg_.webp','14.jpg_.webp',0),
('nut','Orchid bean broad bean snacks casual snack food beef taste to satisfy the greedy small package of wine and vegetables fried goods whole box wholesale','Anhui province','Fancy delicacy',500,9.00,1000,100,'15.jpg_.webp','16.jpg_.webp',0),
('bread','Thousands of silk crispy brown sugar twist net red to satisfy the craving biscuit pouch breakfast bread snack snack casual explosive','Fujian province','Small twist 6',500,8.00,1000,100,'17.jpg_400x400.webp','18.jpg_400x400.webp',0),
('chili','North and South special torn vegetarian meat bean dry spicy strip vegetarian beef steak products Internet celebrity snacks snack snack snack food Year L','Hunan province','NBT/ North and South Te',50,5.00,300,100,'19.jpg_.webp','20.jpg_.webp',0),
('chili','Spicy mushroom bean dry snack snack bean curd dry spicy bar casual snack wholesale small package','Shandong province','Hai Yueyuan',35,14.00,200,100,'21.jpg_.webp','22.jpg_.webp',0),
('confectionery','Pumpkin seed crisp 500g old traditional handmade pastry tea specialty office casual snack pumpkin seed sugar','Zhejiang Province','Yamato',250,14.00,400,100,'23.jpg_.webp','24.jpg_.webp',0),
('bread','Xiamen coconut cake nutrition breakfast pastry coconut bread snack food to satisfy the craving for specialty snacks network red small snacks','Fujian Province',' Countless',300,8.00,1000,100,'25.jpg_.webp','26.jpg_.webp',0),
('bread','Hongyi white toast sliced bread Whole box healthy breakfast lazy quick snack snack snack snack snack food','Fujian Province','Rainy',400,9.00,800,100,'27.jpg_.webp','28.jpg_.webp',0),
('meat','Beef stick instant beef jerky bar evening snack Reduce packaged meat deli fat casual snack','Guangdong province','YOME’S/Yumez',860,14.00,1000,100,'29.jpg_.webp','30.webp',0),
('meat','Bibizan small fish small fish dried to satisfy the craving spicy seafood snacks snack food snacks ready-to-eat wine dishes','Hunan province','Opera',70,10.00,1000,100,'31.webp','32.webp',0),
('bread','Yogurt-flavored hemp flower bread whole box of breakfast pastry heart to satisfy hunger late night snack snacks snack food wholesale','Fujian Province','Opera',300,7.00,75,100,'33.webp','34.webp',0),
('chili','Sichuan Grandma sandwich kelp crispy mushroom bean skin roll net celebrity foodie snack office snack snack','Shandong province','Stuffed with crispy mushrooms and kelp',500,12.00,300,100,'35.webp','36.webp',0),
('nut','Padan wood melon seeds crisp sugar fruit sunflower seeds mixed nuts snack individual small package snack food','Fujian province','BUH',50,2.00,6000,100,'37.webp','38.webp',0),
('candy','ango dry milk cake cow zag sugar love crisp cheese stick office children drama snacks snack snack food','Anhui province','Fancy delicacy',125,13.00,1000,100,'39.webp','40.webp',0),
('bread','No second-hand ripped milk cube bread','Fujian Province','love castle',700,59.90,2,100,'41.webp','42.webp',0),
('bread','Panpan French small bread nutrition breakfast pastries stock anti hunger satiating hunger full night snack leisure health zero food','Fujian Province','Panpan',250,7.60,1000,100,'43.webp','44.webp',0),
('bread','Daly Garden Chocolate Pie 2500g breakfast bread Zero food biscuits Full box egg yolk pie pastry gourmet snacks','Hubei Province','Daliyuan',500,14.00,100,100,'45.webp','46.webp',0),
('bread','Yanjin Shop Lactic acid bacteria small pocket bread Whole box mango pineapple cake snack snacks Healthy breakfast food','Hunan province','yanjin shop',100,20.00,2000,100,'47.webp','48.webp',0),
('bread','Blueberry Rock baked toast filled with cheese bread full box cake breakfast nutritious Dragon Boat Festival snacks casual snacks','Anhui Province','Fancy delicacy',250,10.00,300,100,'49.webp','50.webp',0),
('chocolate','Nuts and peanuts filled chocolate energy bar Snickers bar wholesale New Year candy candy to satisfy hunger small snacks','shandong province','Zhu Xiaoer',500,9.00,1000,100,'51.webp','52.webp',0),
('chocolate','Dove Chocolate Silky Milk Black Chocolate Pure cocoa butter with Hand Gift candy casual snacks loose Christmas','Beijing','Dove',112,10.80,2000,100,'53.webp','54.webp',0),
('chocolate','Bibizan small black sandwich cookies chocolate whole box of bulk office snacks snack snack food Internet celebrity','Fujian Province',' BGRIMM',400,7.00,3000,100,'55.webp','56.webp',0),
('chocolate','Indonesia imported Ritz Nabotti soft heart Brownies Lostar chocolate chip cookies small food','Indonesia','Richeese',560,15.00,1000,100,'57.webp','58.webp',0),
('chocolate','Cookies Cranberries chocolate grout night snack Internet celebrity burst individual small package flagship store','shandong province','pyrinone',12,13.68,2000,100,'59.webp','60.webp',0),
('biscuits','Ryopin Shop Tensun Salt small round cake 102gx10 bags of Japanese crispy biscuits salty snack breakfast for children','Guangdong province','Bestore',1020,10.00,300,100,'61.webp','62.webp',0),
('nut','Good product shop beef flavor orchid bean 120gx3 bag broad bean crispy packaging snack food Hu bean seed','Anhui Province','Bestore',360,17.80,700,100,'63.webp','64.webp',0),
('nut','Good shop dried fruit snack gift pack with preserved fruit Mango strawberry pineapple dried casual snack','Hubei Province','Bestore',306,29.80,79,100,'65.webp','66.webp',0),
('chili','Good food shop baked gluten Qiao bean skin small roast spicy spicy strip bag store the same packaged snacks','Hunan province','Bestore',15,18.60,1000,100,'67.webp','68.webp',0),
('nut','Good product shop Fructus Trichosanthis seed Milk fragrance 19 packs about 500g store independent pouch melon seed snack food','Jiangsu province','Bestore',250,26.80,15,100,'69.webp','70.webp',0),
('biscuits','Oreo 40g*6 boxes of mini cocoa Crisp Heart cookies Casual chocolate stuffed snacks with many flavors','Shanghai','OREO',40,18.90,10,100,'71.webp','72.webp',0),
('biscuits','Olio sandwich cookies chocolate snack food multi-flavor snacks snacks bulk 500g about 25 packs','Jiangsu province','OREO',77,29.80,4000,100,'73.webp','74.webp',0),
('biscuits','Mondelez Oreo Chocolate Bar Cocoa Bar Chocolate Bar Wafer 500g*2 bags of mixed flavor snacks','Beijing','OREO',500,22.80,700,100,'75.webp','76.webp',0),
('biscuits','mico mini Oreo Mini Sandwich cookie small round snack macaron mixed small black cookie loose pack','Guangdong province','OREO',376,10.00,1000,100,'77.webp','78.webp',0),
('biscuits','Oreo Wafer biscuit Double heart Crunch bulk multi-flavor mixed snack food individually packaged small snacks for children','Beijing','OREO',500,45.00,1000,100,'79.webp','80.webp',0);

-- 示例评价
INSERT INTO `comment` (`username`,`content`,`send_time`,`goods_id`,`order_id`,`pid`,`level`,`reported`) VALUES
('John','This product exceeded my expectations. The quality is excellent, and it arrived faster than expected. I would definitely recommend it to others.','2023-11-22 20:28:11',1,1,NULL,5,1),
('Tom','I really like this product. It\'s affordable, durable, and easy to use. Highly recommended!','2023-11-23 17:32:31',1,1,NULL,4,0),
('Lucy','Bad!','2023-11-23 14:23:23',1,1,NULL,1,0);

SET FOREIGN_KEY_CHECKS=1;
