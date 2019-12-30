/*
SQLyog Ultimate v11.11 (64 bit)
MySQL - 5.5.5-10.3.10-MariaDB : Database - customize_channel
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`customize_channel` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;

USE `customize_channel`;

/*Table structure for table `client_user` */

DROP TABLE IF EXISTS `client_user`;

CREATE TABLE `client_user` (
  `client_user_id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下单小号登陆账号',
  `password` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下单小号密码',
  `number` int(11) NOT NULL COMMENT '下单小号当日下单数量',
  `status` char(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '下单小号是否被WebSocket账号分配走了(0:未分配, 1:已分配)',
  `client_name` varchar(35) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'WebSocket连接的账号名',
  `client_status` char(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户端状态(0:未登陆, 1:已登陆)',
  `now_date` date DEFAULT NULL COMMENT '当天日期',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`client_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `client_user` */

insert  into `client_user`(`client_user_id`,`name`,`password`,`number`,`status`,`client_name`,`client_status`,`now_date`,`create_time`) values (1,'16568285139','HUjunnan520',0,'0','','','2019-12-23','2019-12-23 14:28:53'),(2,'16568285148','HUjunnan520',0,'0','','','2019-12-23','2019-12-23 14:30:10'),(3,'16568285150','HUjunnan520',0,'0','','','2019-12-23','2019-12-23 14:30:20'),(4,'16568285128','HUjunnan520',0,'0','','','2019-12-24','2019-12-23 14:30:33'),(5,'16568285149','HUjunnan520',0,'0','','','2019-12-22','2019-12-23 14:30:48'),(6,'16568285231','HUjunnan520',0,'0','','','2019-12-21','2019-12-23 14:30:57'),(7,'16568285131','HUjunnan520',0,'0','','','2019-12-22','2019-12-23 14:31:05'),(8,'16568285220','HUjunnan520',0,'0','','','2019-12-21','2019-12-23 14:31:14'),(9,'16568285119','HUjunnan520',0,'0','','','2019-12-20','2019-12-23 14:31:24'),(10,'16568285122','HUjunnan520',0,'0','','','2019-12-20','2019-12-23 14:31:38');

/*Table structure for table `goods` */

DROP TABLE IF EXISTS `goods`;

CREATE TABLE `goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品ID',
  `url` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品链接',
  `pay_amount` varchar(25) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付金额',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

/*Data for the table `goods` */

insert  into `goods`(`id`,`goods_id`,`url`,`pay_amount`,`create_time`) values (1,'A0006630274-pop8013103891','http://item.m.gome.com.cn/product-A0006630274-pop8013103891.html','8000','2019-12-23 20:12:24'),(2,'A0006631091-pop8013099491','http://item.m.gome.com.cn/product-A0006631091-pop8013099491.html','3000','2019-12-23 20:12:44'),(3,'A0006629010-pop8013098723','http://item.m.gome.com.cn/product-A0006629010-pop8013098723.html','4000','2019-12-23 20:13:00'),(4,'A0006629123-pop8013104277','http://item.m.gome.com.cn/product-A0006629123-pop8013104277.html','5600','2019-12-23 20:13:15'),(5,'A0006631151-pop8013104275','http://item.m.gome.com.cn/product-A0006631151-pop8013104275.html','6000','2019-12-23 20:13:32'),(6,'A0006629183-pop8013098950','http://item.m.gome.com.cn/product-A0006629183-pop8013098950.html','6800','2019-12-23 20:13:50');

/*Table structure for table `ip_addr` */

DROP TABLE IF EXISTS `ip_addr`;

CREATE TABLE `ip_addr` (
  `ip_addr_id` int(11) NOT NULL AUTO_INCREMENT,
  `url` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`ip_addr_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ip许可表';

/*Data for the table `ip_addr` */

/*Table structure for table `pay_order` */

DROP TABLE IF EXISTS `pay_order`;

CREATE TABLE `pay_order` (
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `user_name` varchar(35) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下单小号',
  `platform_order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台订单号(99平台订单号)',
  `client_order_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '国美订单号',
  `order_amount` varchar(35) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单金额(单位:分)',
  `pay_amount` varchar(35) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付金额(单位:分)',
  `pay_order_url` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付订单url',
  `client_order_status` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户端订单状态(0:成功, 1:没货, 2:账号次数达到上限, 9:未知状态<其实是空字符串>)',
  `status` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '订单状态(1:支付连接生成成功, 3:支付连接生成失败, 4:请求超时, 5:支付成功(收到回调), 6:前线返回未知状态)',
  `pay_type` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付方式:(0:支付宝, 1:支付宝WAP, 2:微信扫一扫, 3:微信H5, 5:QQ, 6:QQWAP, 7:京东, 8:京东WAP, 9:银联快捷, 10:银联快捷WAP, 11:银联网关, 12:银联扫码, 13:苏宁支付)',
  `notify_url` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下游回调地址',
  `notify_par` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回调参数json',
  `notify_send_notify_count` int(11) NOT NULL COMMENT '发送回调次数',
  `notify_last_send_time` datetime DEFAULT NULL COMMENT '最后一次回调发送时间',
  `return_result` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '返回结果(fail:失败, success:成功)',
  `create_time` datetime NOT NULL COMMENT '订单创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '订单修改时间',
  PRIMARY KEY (`order_id`),
  KEY `platform_order_no` (`platform_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

/*Data for the table `pay_order` */

/*Table structure for table `system_config` */

DROP TABLE IF EXISTS `system_config`;

CREATE TABLE `system_config` (
  `config_id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(40) COLLATE utf8mb4_unicode_ci NOT NULL,
  `value` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '值',
  `description` varchar(35) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

/*Data for the table `system_config` */

insert  into `system_config`(`config_id`,`key`,`value`,`description`,`create_time`) values (1,'privateKey','52A1B74DDAFC4274992E51DDCDFCCD9F','加签用的key','2019-12-29 21:44:12'),(2,'superiorLimit','20','下单小号每日下单量','2019-12-29 21:47:17'),(3,'payOrderStatus','1','下单开关(0:关, 1:开)','2019-12-30 20:31:45');

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
