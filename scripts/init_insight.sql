CREATE DATABASE IF NOT EXISTS insight;

USE insight;

CREATE TABLE `dataset` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project` varchar(100) NOT NULL,
  `dataset` varchar(100) NOT NULL,
  `status` varchar(10) NOT NULL DEFAULT 'NORMAL' COMMENT 'dataset status: NORMAL | BROKEN',
  `broken_msg` text COMMENT 'the broken detail info when dataset is broken',
  `canvas` varchar(5000) NOT NULL DEFAULT '',
  `front_v` varchar(20) NOT NULL DEFAULT 'v0.1',
  `create_user` varchar(255) NOT NULL,
  `create_time` bigint NOT NULL DEFAULT '0',
  `modify_time` bigint NOT NULL DEFAULT '0',
  `extend` varchar(5000) NOT NULL DEFAULT '',
  `translation_types` varchar(100) DEFAULT NULL,
  `access` int DEFAULT '0' COMMENT '0: allow_list, 1:block_list',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_pk` (`project`,`dataset`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='semantic-dataset';

INSERT INTO `dataset` (`project`, `dataset`, `status`, `create_user`, `create_time`, `modify_time`) VALUES
('project', 'SalesWarehouse', 'NORMAL', 'admin', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);