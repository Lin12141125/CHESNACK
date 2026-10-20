CREATE DATABASE IF NOT EXISTS user;
use user;

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `login`;
CREATE TABLE `login` (
  `email` varchar(50) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(30) NOT NULL,
  `role` tinyint(1) DEFAULT NULL COMMENT '1 - admin  2 - user',
  PRIMARY KEY (`email`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;


DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(50) NOT NULL,
  `good_id` varchar(70) NOT NULL,
  `amount` int NOT NULL,
  `price` decimal(18,2) NOT NULL,
  `time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(50) NOT NULL,
  `good_id` varchar(70) NOT NULL,
  `amount` int NOT NULL,
  `price` decimal(18,2) NOT NULL,
  `time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `content` varchar(1000) NOT NULL,
  `send_time` datetime NOT NULL,
  `goods_id` varchar(70) NOT NULL,
  `order_id` int NOT NULL,
  `pid` int DEFAULT NULL COMMENT '父评论ID，如果不为空，则为二级评论',
  `level` int DEFAULT NULL COMMENT '评论星级(1-5)',
  `reported` int DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb3;

INSERT INTO `comment` VALUES ('1', 'John', 'This product exceeded my expectations. The quality is excellent, and it arrived faster than expected. I would definitely recommend it to others.', '2023-11-22 20:28:11', '1', '3', null, '5', '1');
INSERT INTO `comment` VALUES ('2', 'Tom', 'I really like this product. It\'s affordable, durable, and easy to use. Highly recommended!', '2023-11-23 17:32:31', '1', '3', null, '4', '0');
INSERT INTO `comment` VALUES ('3', 'Lucy', 'Bad!', '2023-11-23 14:23:23', '1', '3', null, '1', '0');


DROP TABLE IF EXISTS `community`;
CREATE TABLE `community` (
  `id` int NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `content` varchar(1000) DEFAULT NULL,
  `send_time` varchar(50) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `account` varchar(50) DEFAULT NULL,
  `img1` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;


DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `followee` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;


DROP TABLE IF EXISTS `community_comment`;
CREATE TABLE `community_comment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `content` varchar(1000) NOT NULL,
  `send_time` datetime NOT NULL,
  `community_id` int NOT NULL,
  `pid` int DEFAULT NULL COMMENT '父评论ID，如果不为空，则为二级评论',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb3;