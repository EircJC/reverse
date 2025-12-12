# ************************************************************
# Sequel Pro SQL dump
# Version 5446
#
# https://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 8.0.24)
# Database: texas
# Generation Time: 2025-12-12 02:30:10 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table t_game_log
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_game_log`;

CREATE TABLE `t_game_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `roomType` varchar(50) DEFAULT NULL COMMENT '房间信息（普通场）',
  `roomLevel` varchar(50) DEFAULT NULL COMMENT '房间信息（级别）',
  `dealer` text COMMENT '庄家',
  `smallBet` text COMMENT '小盲',
  `bigBet` text COMMENT '大盲',
  `roundInfo` text COMMENT '第一圈（底牌圈）',
  `betpoolInfo` text COMMENT '分奖池信息',
  `startTime` datetime DEFAULT NULL COMMENT '游戏开始时间',
  `endTime` datetime DEFAULT NULL COMMENT '游戏结算时间',
  `countBetpool` int DEFAULT NULL COMMENT '底池总金额',
  `cut` int DEFAULT NULL COMMENT '抽成',
  `communityCards` text COMMENT ' 公共牌',
  `playersInitInfo` longtext COMMENT '房间中玩家的初始信息',
  `playersFinalInfo` longtext COMMENT '房间中玩家的最终信息 ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='游戏日志';



# Dump of table t_player
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_player`;

CREATE TABLE `t_player` (
  `id` int NOT NULL AUTO_INCREMENT,
  `wallet_address` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '钱包地址',
  `username` varchar(200) NOT NULL COMMENT '用户名',
  `userpwd` varchar(100) NOT NULL COMMENT '用户密码',
  `nickname` varchar(50) DEFAULT '' COMMENT '用户昵称',
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '邮箱',
  `phone` varchar(20) DEFAULT '' COMMENT '手机',
  `chips` bigint DEFAULT '1000' COMMENT '筹码数',
  `pic_logo` varchar(200) DEFAULT '0' COMMENT '头像地址',
  `regdate` datetime DEFAULT NULL COMMENT '注册时间',
  `status` char(1) DEFAULT '1' COMMENT '状态1正常，2冻结',
  `isrobot` char(1) DEFAULT '0' COMMENT '是否是机器人',
  `type` varchar(15) DEFAULT 'normal',
  `remark` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;

LOCK TABLES `t_player` WRITE;
/*!40000 ALTER TABLE `t_player` DISABLE KEYS */;

INSERT INTO `t_player` (`id`, `wallet_address`, `username`, `userpwd`, `nickname`, `email`, `phone`, `chips`, `pic_logo`, `regdate`, `status`, `isrobot`, `type`, `remark`)
VALUES
	(2494,NULL,'jiangchao','e10adc3949ba59abbe56e057f20f883e',NULL,NULL,NULL,1000,'https://s1.ax1x.com/2022/09/18/xpLpdS.png','2022-09-06 21:13:04','1','0','normal',NULL),
	(2495,NULL,'jc','e10adc3949ba59abbe56e057f20f883e',NULL,NULL,NULL,1000,'https://s1.ax1x.com/2022/09/18/xpqvsP.jpg','2022-09-06 21:13:36','1','0','normal',NULL),
	(2504,NULL,'jc1','e10adc3949ba59abbe56e057f20f883e',NULL,NULL,NULL,1000,'https://s1.ax1x.com/2022/09/18/xpqvsP.jpg','2022-09-06 21:13:36','1','0','normal',NULL);

/*!40000 ALTER TABLE `t_player` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_player_chips_log
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_player_chips_log`;

CREATE TABLE `t_player_chips_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(30) NOT NULL COMMENT '用户名',
  `chips` bigint DEFAULT '1000000' COMMENT '筹码数',
  `state` int DEFAULT '1',
  `date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 ROW_FORMAT=COMPACT;



# Dump of table t_skill_card
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_skill_card`;

CREATE TABLE `t_skill_card` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `skill_card_no` varchar(50) DEFAULT NULL COMMENT '技能卡编号',
  `skill_dictionary_no` varchar(50) NOT NULL COMMENT '技能字典编号）',
  `tokenId` varchar(32) DEFAULT NULL COMMENT '合约TokenId',
  `player_oid` int NOT NULL COMMENT '玩家主键id',
  `count` int DEFAULT NULL COMMENT '使用数量，-1为无限量',
  `type` varchar(1) NOT NULL DEFAULT '1' COMMENT '类型',
  `status` varchar(1) NOT NULL DEFAULT '1' COMMENT '状态（0:停用; 1:启用）',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `skill_card_no` (`skill_card_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='技能卡列表';



# Dump of table t_skill_combination
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_skill_combination`;

CREATE TABLE `t_skill_combination` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `skill_combination_no` varchar(50) DEFAULT NULL COMMENT '技能卡组编号',
  `skill_combination_name` varchar(50) DEFAULT NULL COMMENT '卡组名称',
  `skill_card_no` varchar(50) DEFAULT NULL COMMENT '技能卡编号',
  `player_oid` int NOT NULL COMMENT '玩家主键id',
  `type` varchar(1) NOT NULL DEFAULT '1' COMMENT '类型',
  `status` varchar(1) NOT NULL DEFAULT '1' COMMENT '状态（0:停用; 1:启用）',
  `description` text COMMENT '描述',
  `constrains` text COMMENT '限制条件',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `skill_combination_no` (`skill_combination_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='技能卡组';



# Dump of table t_skill_dictionary
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_skill_dictionary`;

CREATE TABLE `t_skill_dictionary` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `skill_dictionary_no` varchar(50) DEFAULT NULL COMMENT '技能字典编号',
  `skill_name_zh` varchar(50) DEFAULT NULL COMMENT '技能名称（中文）',
  `skill_name_en` varchar(50) DEFAULT NULL COMMENT '技能名称（英文）',
  `power` int DEFAULT NULL COMMENT '能耗',
  `image` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '图片',
  `type` varchar(1) DEFAULT NULL COMMENT '类型（1:主动; 2:防御； 3:陷阱）',
  `point_to` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '是否指向性技能（主动技能特有规则）',
  `level` varchar(3) DEFAULT NULL COMMENT '级别（0:N; 1:R; 2:SR; 3:SSR; 4:UR）',
  `status` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '1' COMMENT '状态',
  `use_round` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '使用回合(P:翻牌前；F:翻牌；T:转牌；R:河牌)',
  `trap_action` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '陷阱触发动作，只有type=3启用(C:过牌；F:弃牌；B:下注；S:使用技能卡)',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级：0最小',
  `description` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '描述',
  `constrains` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '限制条件',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `skill_dictionary_no` (`skill_dictionary_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='技能字典';

LOCK TABLES `t_skill_dictionary` WRITE;
/*!40000 ALTER TABLE `t_skill_dictionary` DISABLE KEYS */;

INSERT INTO `t_skill_dictionary` (`id`, `skill_dictionary_no`, `skill_name_zh`, `skill_name_en`, `power`, `image`, `type`, `point_to`, `level`, `status`, `use_round`, `trap_action`, `priority`, `description`, `constrains`, `remark`, `create_time`)
VALUES
	(1,'SDN_ACTIVE_1','冻结',NULL,2,'texasImages/zhudong.png','1','1','0','1','PFT',NULL,0,'指定玩家下回合无法使用技能',NULL,'1',NULL),
	(2,'SDN_ACTIVE_2','全场静默',NULL,4,'texasImages/zhudong.png','1','0','2','1','PFT',NULL,0,'所有玩家下回合无法使用技能','','1',NULL),
	(3,'SDN_ACTIVE_3','穿甲弹',NULL,7,'texasImages/zhudong.png','1','1','3','0','PFTR',NULL,0,'指定一个玩家消除身上所有防御','指向性抵抗法术无法防御','1',NULL),
	(4,'SDN_ACTIVE_4','跟随',NULL,6,'texasImages/zhudong.png','1','1','2','0','PFT',NULL,0,'指定玩家下次操作为跟注或过牌','','1',NULL),
	(5,'SDN_ACTIVE_5','孤注一掷',NULL,9,'texasImages/zhudong.png','1','0','4','0','PF',NULL,0,'全部玩家All In，然后触发死亡沉默','只能翻牌和翻牌前使用','1',NULL),
	(6,'SDN_ACTIVE_6','更多选择',NULL,4,'texasImages/zhudong.png','1','0','1','0','PFTR',NULL,0,'从牌库抽2张技能卡','','1',NULL),
	(7,'SDN_ACTIVE_7','背刺',NULL,10,'texasImages/zhudong.png','1','1','3','1','PFTR',NULL,0,'指定一个玩家重发其手牌','','1',NULL),
	(8,'SDN_ACTIVE_8','一呼百应',NULL,7,'texasImages/zhudong.png','1','0','3','0','PFT',NULL,0,'本回合未弃牌玩家下个操作全部跟注或过牌','','1',NULL),
	(9,'SDN_ACTIVE_9','死亡沉默',NULL,7,'texasImages/zhudong.png','1','0','3','0','PFTR',NULL,0,'所有玩家无法使用技能直到本局游戏结束，本回合生效','','1',NULL),
	(10,'SDN_ACTIVE_10','狂妄',NULL,5,'texasImages/zhudong.png','1','1','2','0','PFT',NULL,0,'指定玩家下一次操作为All In','','1',NULL),
	(11,'SDN_ACTIVE_11','倒转乾坤',NULL,13,'texasImages/zhudong.png','1','0','4','0','FTR',NULL,0,'重发任意一条街公共牌','','1',NULL),
	(12,'SDN_ACTIVE_12','解脱',NULL,1,'texasImages/zhudong.png','1','0','1','0','PFTR',NULL,0,'随机放弃手里2张技能卡获得4个能量','','1',NULL),
	(13,'SDN_ACTIVE_13','减负',NULL,0,'texasImages/zhudong.png','1','0','1','0','PFTR',NULL,0,'随机放弃手里1张技能卡获得2个能量','','1',NULL),
	(14,'SDN_ACTIVE_14','恩赐',NULL,0,'texasImages/zhudong.png','1','0','0','0','PFTR',NULL,0,'获得1个能量','','1',NULL),
	(15,'SDN_ACTIVE_15','吸收',NULL,1,'texasImages/zhudong.png','1','1','2','0','PFTR',NULL,0,'吸收指定玩家2个能量','','1',NULL),
	(16,'SDN_ACTIVE_16','高级吸收',NULL,2,'texasImages/zhudong.png','1','1','3','0','PFTR',NULL,0,'吸收指定玩家3个能量','','1',NULL),
	(17,'SDN_ACTIVE_17','损人利己',NULL,6,'texasImages/zhudong.png','1','0','2','0','PFTR',NULL,0,'除自己外全场玩家随机弃掉手中2个技能卡','','1',NULL),
	(18,'SDN_ACTIVE_18','流放',NULL,0,'texasImages/zhudong.png','1','0','0','0','PFTR',NULL,0,'放弃一张技能卡再重新抽一张','','1',NULL),
	(19,'SDN_ACTIVE_19','幸运',NULL,3,'texasImages/zhudong.png','1','0','3','0','PFTR',NULL,0,'抽一张技能卡并且手中所有技能卡减少1个能量消耗','','1',NULL),
	(20,'SDN_ACTIVE_20','宁有种乎',NULL,9,'texasImages/zhudong.png','1','0','4','0','P',NULL,0,'全部玩家重新发手牌，然后触发死亡沉默','','1',NULL),
	(21,'SDN_ACTIVE_21','放弃',NULL,12,'texasImages/zhudong.png','1','1','4','0','PFT',NULL,0,'指定玩家弃牌','场上玩家数量少于3位或选择已经Allin玩家时技能失效','1',NULL),
	(22,'SDN_ACTIVE_22','卸甲归田',NULL,10,'texasImages/zhudong.png','1','0','3','0','PFTR',NULL,0,'取消场上所有防御','','1',NULL),
	(23,'SDN_ACTIVE_23','正气凛然',NULL,12,'texasImages/zhudong.png','1','0','4','0','PFTR',NULL,9,'取消场上所有陷阱和防御','','1',NULL),
	(24,'SDN_ACTIVE_24','群体逆转',NULL,8,'texasImages/zhudong.png','1','0','3','0','PFT',NULL,0,'全部玩家重新更换一张手牌','','1',NULL),
	(25,'SDN_ACTIVE_25','单向逆转',NULL,6,'texasImages/zhudong.png','1','1','2','0','PFTR',NULL,0,'指定玩家随机更换一张手牌','可以对自己使用','1',NULL),
	(26,'SDN_ACTIVE_26','照明弹',NULL,10,'texasImages/zhudong.png','1','0','3','0','PFTR',NULL,9,'取消场上所有陷阱','','1',NULL),
	(27,'SDN_DEFENSE_1','无懈可击',NULL,4,'texasImages/fangyu.png','2','0','3','0','PFTR',NULL,0,'抵抗一次指向性技能攻击','','1',NULL),
	(28,'SDN_DEFENSE_2','崇高牺牲',NULL,5,'texasImages/fangyu.png','2','0','2','0','PFTR',NULL,0,'由其他玩家替代法术攻击','如果场内无其他第三方玩家则本技能失效','1',NULL),
	(29,'SDN_DEFENSE_3','铜墙铁壁',NULL,7,'texasImages/fangyu.png','2','0','3','0','PFTR',NULL,0,'本局游戏不受任何指向性技能攻击，持续到结束','','1',NULL),
	(30,'SDN_DEFENSE_4','敏捷',NULL,3,'texasImages/fangyu.png','2','0','3','1','PFTR',NULL,0,'躲避一次陷阱','','1',NULL),
	(31,'SDN_TRAP_1','反抗失败',NULL,3,'texasImages/xianjing.png','3','0','2','0','PFT','B',0,'加注无效','改为跟注或过牌','1',NULL),
	(32,'SDN_TRAP_2','背叛',NULL,9,'texasImages/xianjing.png','3','0','4','1','PFT','B',0,'加注者弃牌','自己除外','1',NULL),
	(33,'SDN_TRAP_3','负担加重',NULL,1,'texasImages/xianjing.png','3','0','1','0','PFTR','S',0,'下一个使用技能的玩家释放技能后额外消耗2个能量','额外消耗能量不够扣减则对方能量剩余为0,自己除外','0',NULL),
	(34,'SDN_TRAP_4','措手不及',NULL,4,'texasImages/xianjing.png','3','0','3','0','PFT','B',0,'加注者重发手牌','自己也会触发','1',NULL),
	(35,'SDN_TRAP_5','复制',NULL,2,'texasImages/xianjing.png','3','0','2','1','PFT','B',0,'复制加注者1张技能卡并获得1个能量','自己除外','1',NULL),
	(36,'SDN_TRAP_6','迟缓',NULL,2,'texasImages/xianjing.png','3','0','2','0','PFT','S',0,'下一个使用技能的玩家下回合不发放技能卡，回合数可叠加','自己除外','1',NULL),
	(37,'SDN_TRAP_7','高级迟缓',NULL,5,'texasImages/xianjing.png','3','0','3','0','PFT','S',0,'下一个使用技能的玩家本局不发放技能卡','自己除外','1',NULL),
	(38,'SDN_TRAP_8','自信',NULL,4,'texasImages/xianjing.png','3','0','2','0','PFT','F',0,'下一个弃牌玩家改为过牌或跟注','自己除外','1',NULL),
	(39,'SDN_TRAP_9','失算',NULL,1,'texasImages/xianjing.png','3','0','1','0','PFTR','C',0,'下一个过牌玩家失去3个能量','自己除外','1',NULL);

/*!40000 ALTER TABLE `t_skill_dictionary` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_skill_dictionary_tmp
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_skill_dictionary_tmp`;

CREATE TABLE `t_skill_dictionary_tmp` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `skill_dictionary_no` varchar(50) DEFAULT NULL COMMENT '技能字典编号',
  `skill_name_zh` varchar(50) DEFAULT NULL COMMENT '技能名称（中文）',
  `skill_name_en` varchar(50) DEFAULT NULL COMMENT '技能名称（英文）',
  `power` int DEFAULT NULL COMMENT '能耗',
  `image` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '图片',
  `type` varchar(1) DEFAULT NULL COMMENT '类型（1:主动; 2:防御； 3:陷阱）',
  `point_to` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '0' COMMENT '是否指向性技能（主动技能特有规则）',
  `level` varchar(3) DEFAULT NULL COMMENT '级别（0:N; 1:R; 2:SR; 3:SSR; 4:UR）',
  `status` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '1' COMMENT '状态',
  `use_round` varchar(10) DEFAULT NULL COMMENT '使用回合(P:翻牌前；F:翻牌；T:转牌；R:河牌)',
  `trap_action` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '陷阱触发动作，只有type=3启用(C:过牌；F:弃牌；B:下注；S:使用技能卡)',
  `description` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '描述',
  `constrains` varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '限制条件',
  `remark` varchar(50) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `skill_dictionary_no` (`skill_dictionary_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='技能字典';

LOCK TABLES `t_skill_dictionary_tmp` WRITE;
/*!40000 ALTER TABLE `t_skill_dictionary_tmp` DISABLE KEYS */;

INSERT INTO `t_skill_dictionary_tmp` (`id`, `skill_dictionary_no`, `skill_name_zh`, `skill_name_en`, `power`, `image`, `type`, `point_to`, `level`, `status`, `use_round`, `trap_action`, `description`, `constrains`, `remark`, `create_time`)
VALUES
	(1,'SDN_ACTIVE_1','冻结',NULL,2,'texasImages/zhudong.png','1','1','0','0','PFT',NULL,'指定玩家下回合无法使用技能',NULL,'1',NULL),
	(2,'SDN_ACTIVE_2','全场静默',NULL,4,'texasImages/zhudong.png','1','0','2','0','PFT',NULL,'所有玩家下回合无法使用技能','','1',NULL),
	(3,'SDN_DEFENSE_1','无懈可击',NULL,4,'texasImages/fangyu.png','2','0','3','0','PFTR',NULL,'抵抗一次指向性技能攻击','','1',NULL),
	(4,'SDN_ACTIVE_4','跟随',NULL,6,'texasImages/zhudong.png','1','1','2','0','PFT',NULL,'指定玩家下次操作为跟注或过牌','','1',NULL),
	(5,'SDN_ACTIVE_5','孤注一掷',NULL,9,'texasImages/zhudong.png','1','0','4','0','PF',NULL,'全部玩家All In，然后触发死亡沉默','只能翻牌和翻牌前使用','1',NULL),
	(6,'SDN__ACTIVE6','更多选择',NULL,4,'texasImages/zhudong.png','1','0','1','0','PFTR',NULL,'从牌库抽2张技能卡','','1',NULL),
	(7,'SDN_ACTIVE_7','背刺',NULL,10,'texasImages/zhudong.png','1','1','3','1','PFTR',NULL,'指定一个玩家重发其手牌','','1',NULL),
	(8,'SDN_DEFENSE_2','崇高牺牲',NULL,5,'texasImages/fangyu.png','2','0','2','1','PFTR',NULL,'由其他玩家替代法术攻击','如果场内无其他第三方玩家则本技能失效','1',NULL),
	(9,'SDN_ACTIVE_9','死亡沉默',NULL,7,'texasImages/zhudong.png','1','0','3','0','PFTR',NULL,'所有玩家无法使用技能直到本局游戏结束，本回合生效','','1',NULL),
	(10,'SDN_ACTIVE_10','狂妄',NULL,5,'texasImages/zhudong.png','1','1','2','0','PFT',NULL,'指定玩家下一次操作为All In','','1',NULL),
	(11,'SDN_ACTIVE_11','倒转乾坤',NULL,13,'texasImages/zhudong.png','1','0','4','0','FTR',NULL,'重发任意一条街公共牌','','1',NULL),
	(12,'SDN_ACTIVE_12','解脱',NULL,1,'texasImages/zhudong.png','1','0','1','0','PFTR',NULL,'随机放弃手里2张技能卡获得4个能量','','1',NULL),
	(13,'SDN_ACTIVE_13','减负',NULL,0,'texasImages/zhudong.png','1','0','1','0','PFTR',NULL,'随机放弃手里1张技能卡获得2个能量','','1',NULL),
	(14,'SDN_ACTIVE_14','恩赐',NULL,0,'texasImages/zhudong.png','1','0','0','0','PFTR',NULL,'获得1个能量','','1',NULL),
	(15,'SDN_ACTIVE_15','吸收',NULL,1,'texasImages/zhudong.png','1','1','2','0','PFTR',NULL,'吸收指定玩家2个能量','','1',NULL),
	(16,'SDN_ACTIVE_16','高级吸收',NULL,2,'texasImages/zhudong.png','1','1','3','0','PFTR',NULL,'吸收指定玩家3个能量','','1',NULL),
	(17,'SDN_ACTIVE_17','损人利己',NULL,6,'texasImages/zhudong.png','1','0','2','0','PFTR',NULL,'除自己外全场玩家随机弃掉手中2个技能卡','','1',NULL),
	(18,'SDN_ACTIVE_18','流放',NULL,0,'texasImages/zhudong.png','1','0','0','0','PFTR',NULL,'放弃一张技能卡再重新抽一张','','1',NULL),
	(19,'SDN_ACTIVE_19','幸运',NULL,3,'texasImages/zhudong.png','1','0','3','0','PFTR',NULL,'抽一张技能卡并且手中所有技能卡减少1个能量消耗','','1',NULL),
	(20,'SDN_ACTIVE_20','宁有种乎',NULL,9,'texasImages/zhudong.png','1','0','4','0','P',NULL,'全部玩家重新发手牌，然后触发死亡沉默','','1',NULL),
	(21,'SDN_ACTIVE_21','放弃',NULL,12,'texasImages/zhudong.png','1','1','4','1','PFT',NULL,'指定玩家弃牌','场上玩家数量少于3位或选择已经Allin玩家时技能失效','1',NULL),
	(22,'SDN_TRAP_1','反抗失败',NULL,3,'texasImages/xianjing.png','3','0','2','0','PFT','B','加注无效','改为跟注或过牌',NULL,NULL),
	(23,'SDN_TRAP_2','背叛',NULL,9,'texasImages/xianjing.png','3','0','4','0','PFT','B','加注者弃牌','自己除外',NULL,NULL),
	(24,'SDN_ACTIVE_24','群体逆转',NULL,8,'texasImages/zhudong.png','1','0','3','0','PFT',NULL,'全部玩家重新更换一张手牌','','1',NULL),
	(25,'SDN_ACTIVE_25','单向逆转',NULL,6,'texasImages/zhudong.png','1','1','2','1','PFTR',NULL,'指定玩家随机更换一张手牌','可以对自己使用','1',NULL),
	(26,'SDN_ACTIVE_26','照明弹',NULL,10,'texasImages/zhudong.png','1','0','3','0','PFTR',NULL,'取消场上所有陷阱','',NULL,NULL),
	(27,'SDN_ACTIVE_22','卸甲归田',NULL,10,'texasImages/zhudong.png','1','0','3','0','PFTR',NULL,'取消场上所有防御','',NULL,NULL),
	(28,'SDN_ACTIVE_23','正气凛然',NULL,12,'texasImages/zhudong.png','1','0','4','0','PFTR',NULL,'取消场上所有陷阱和防御','',NULL,NULL),
	(29,'SDN_TRAP_3','负担加重',NULL,1,'texasImages/xianjing.png','3','0','1','0','PFTR','S','下一个使用技能的玩家释放技能后额外消耗2个能量','额外消耗能量不够扣减则对方能量剩余为0',NULL,NULL),
	(30,'SDN_TRAP_4','措手不及',NULL,4,'texasImages/xianjing.png','3','0','3','0','PFT','B','加注者重发手牌','自己也会触发',NULL,NULL),
	(31,'SDN_TRAP_5','复制',NULL,2,'texasImages/xianjing.png','3','0','2','0','PFT','B','复制加注者1张技能卡并获得1个能量','自己除外',NULL,NULL),
	(32,'SDN_DEFENSE_3','铜墙铁壁',NULL,7,'texasImages/fangyu.png','2','0','3','0','PFTR',NULL,'本局游戏不受任何指向性技能攻击，持续到结束','','1',NULL),
	(33,'SDN_DEFENSE_4','敏捷',NULL,3,'texasImages/fangyu.png','2','0','3','0','PFTR',NULL,'躲避一次陷阱','',NULL,NULL),
	(34,'SDN_TRAP_6','迟缓',NULL,2,'texasImages/xianjing.png','3','0','2','0','PFT','S','下一个使用技能的玩家下回合不发放技能卡，回合数可叠加','自己除外',NULL,NULL),
	(35,'SDN_TRAP_7','高级迟缓',NULL,5,'texasImages/xianjing.png','3','0','3','0','PFT','S','下一个使用技能的玩家本局不发放技能卡','自己除外',NULL,NULL),
	(36,'SDN_ACTIVE_8','一呼百应',NULL,7,'texasImages/zhudong.png','1','0','3','0','PFT',NULL,'本回合未弃牌玩家下个操作全部跟注或过牌','','1',NULL),
	(37,'SDN_ACTIVE_3','穿甲弹',NULL,7,'texasImages/zhudong.png','1','1','3','1','PFTR',NULL,'指定一个玩家消除身上所有防御','指向性抵抗法术无法防御','1',NULL),
	(38,'SDN_TRAP_8','自信',NULL,4,'texasImages/xianjing.png','3','0','2','1','PFT','F','下一个弃牌玩家改为过牌或跟注','自己除外','0',NULL),
	(39,'SDN_TRAP_9','失算',NULL,1,'texasImages/xianjing.png','3','0','1','1','PFTR','C','下一个过牌玩家失去3个能量','自己除外','0',NULL);

/*!40000 ALTER TABLE `t_skill_dictionary_tmp` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table t_system_log
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_system_log`;

CREATE TABLE `t_system_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userid` int DEFAULT NULL COMMENT '用户编号',
  `type` varchar(50) DEFAULT NULL COMMENT '类型',
  `operation` varchar(200) DEFAULT NULL COMMENT '操作',
  `content` text COMMENT '内容',
  `datetime` datetime DEFAULT NULL COMMENT '时间',
  `machine` varchar(100) DEFAULT NULL COMMENT '机器码',
  `clienttype` varchar(20) DEFAULT NULL COMMENT '客户端类型',
  `token` varchar(100) DEFAULT NULL COMMENT '记号',
  `appversion` varchar(15) DEFAULT NULL COMMENT 'app版本',
  `ip` varchar(30) DEFAULT NULL COMMENT 'IP',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;



# Dump of table t_transaction_log
# ------------------------------------------------------------

DROP TABLE IF EXISTS `t_transaction_log`;

CREATE TABLE `t_transaction_log` (
  `transactionHash` varchar(200) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `from` varchar(200) NOT NULL,
  `time` datetime NOT NULL,
  `value` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`transactionHash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
