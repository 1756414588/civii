ALTER TABLE `p_world`
    ADD COLUMN `totalMaxOnLineNum` int       NULL COMMENT '总最高在线' AFTER `worldActPlanData`,
    ADD COLUMN `todayMaxOnLineNum` int       NULL COMMENT '今日最高在线' AFTER `totalMaxOnLineNum`,
    ADD COLUMN `refreshTime`       timestamp NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '上一次刷新的时间' AFTER `todayMaxOnLineNum`;


ALTER TABLE `p_lord`
    ADD COLUMN `seasonCard`       bigint(20) NOT NULL DEFAULT 0 COMMENT '季卡到期时间' AFTER `monthCard`,
    ADD COLUMN `seekingTimes`     int(11)    NOT NULL DEFAULT 0 COMMENT '美女约会次数' AFTER `guideKey`,
    ADD COLUMN `safety`           int(11)    NOT NULL DEFAULT 0 COMMENT '美女安全感' AFTER `seekingTimes`,
    ADD COLUMN `sGameTimes`       int(11)    NOT NULL DEFAULT 0 COMMENT '美女小游戏次数' AFTER `safety`,
    ADD COLUMN `buySGameTimes`    int(11)    NOT NULL DEFAULT 0 COMMENT '总共购买小游戏次数' AFTER `sGameTimes`,
    ADD COLUMN `freeSGameEndTime` bigint(20) NOT NULL DEFAULT 0 COMMENT '上一次小游戏次数重置的时间' AFTER `buySGameTimes`,
    ADD COLUMN `firstBReName`     int(11)    NOT NULL DEFAULT 0 COMMENT '美女是否是第一次改名:0是第一次  1.不是第一次' AFTER `freeSGameEndTime`;


ALTER TABLE `p_detail`
    ADD COLUMN `beautyData` blob NULL COMMENT '玩家美女数据' AFTER `effectData`;


ALTER TABLE `p_lord`
    ADD COLUMN `freeSeekingEndTime` bigint(20) NOT NULL DEFAULT 0 COMMENT '上一次约会次数重置的时间' AFTER `freeSGameEndTime`,
    ADD COLUMN `buySeekingTimes`    int(11)    NOT NULL DEFAULT 0 COMMENT '总共购买约会次数' AFTER `buySGameTimes`;


#########2020-6-12 11:02
ALTER TABLE `p_lord`
    MODIFY COLUMN `payStatus` varchar(225) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '每个计费点是否是第一次支付' AFTER `firstBReName`


#########2020-7-22 11:02
ALTER TABLE `p_server_mail`
    MODIFY COLUMN `title_content` varchar(225) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '标题内容' AFTER `title`

#########2020-8-03 11:02
ALTER TABLE `p_lord`
    ADD COLUMN `openSpeak` int(11) NULL DEFAULT 0 COMMENT '聊天是否开启 0未开启 1 开启' AFTER `payStatus`;

##2020-08-13 14:46
ALTER TABLE s_channel
    ADD COLUMN is_review int(2) DEFAULT 0 COMMENT '评审状态0非评审包 1评审' AFTER `plat_no`;
ALTER TABLE u_account
    add COLUMN serverInfos VARCHAR(255) DEFAULT '' COMMENT '用户在对应区服有角色信息' AFTER gameDate;

##2020-08-14 10:34
DROP TABLE IF EXISTS `role_item_log`;
CREATE TABLE `role_item_log`
(
    `log_datetmp` datetime   DEFAULT NULL COMMENT '日志时间',
    `log_date`    datetime   DEFAULT NULL COMMENT '日志时间',
    `role_id`     bigint(20) DEFAULT 0 NOT NULL COMMENT '玩家id',
    `item_id`     int(11)    DEFAULT 0 NOT NULL COMMENT '道具id',
    `state`       int(2)     DEFAULT 0 NOT NULL COMMENT '产出/消耗 0产出 1消耗',
    `use_add`     int(11)    DEFAULT 0 NOT NULL COMMENT '产出/消耗数量',
    `item_source` int(11)    DEFAULT 0 NOT NULL COMMENT '产出/消耗来源'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO data_res
VALUES ("role_item_log", "role_item_log");
INSERT INTO collection_res
VALUES ("role_item_log", "role_item_log.log");

##2020-08-14 16:32
ALTER TABLE p_account
    add COLUMN registerIp CHAR(30) DEFAULT "" COMMENT '注册ip' AFTER gameChannelId;
ALTER TABLE p_account
    add COLUMN lastLoginIp CHAR(30) DEFAULT "" COMMENT '上次登录Ip' AFTER registerIp;
ALTER TABLE p_lord
    add COLUMN systemGold INT(11) DEFAULT 0 NOT NULL COMMENT '系统钻石' AFTER openSpeak;
ALTER TABLE p_lord
    add COLUMN rechargeGold INT(11) DEFAULT 0 NOT NULL COMMENT '充值钻石' AFTER systemGold;


##2020-08-17 16:32

ALTER TABLE `p_lord`
    ADD COLUMN `lastJourney` int(11) DEFAULT 0 NOT NULL COMMENT '征途剩余次数' AFTER `rechargeGold`;

ALTER TABLE `p_lord`
    ADD COLUMN `journeyTimes` int(11) DEFAULT 0 NOT NULL COMMENT '征途剩余次数' AFTER `lastJourney`;

ALTER TABLE `p_lord`
    ADD COLUMN `freeJourneyEndTime` bigint(20) DEFAULT 0 NOT NULL COMMENT '上一次征途免费次数重置的时间' AFTER `journeyTimes`;

ALTER TABLE `p_lord`
    ADD COLUMN `buyJourneyEndTime` bigint(20) DEFAULT 0 NOT NULL COMMENT '上一次征途购买次数的时间' AFTER `freeJourneyEndTime`;

ALTER TABLE `p_lord`
    ADD COLUMN `buyJourneyTimes` int(11) DEFAULT 0 NOT NULL COMMENT '购买征途购买次数' AFTER `buyJourneyEndTime`;

##2020-08-17 19:20
ALTER TABLE vip_exp
    add COLUMN free INT(2) DEFAULT 0 NOT NULL COMMENT '是否免费' AFTER lastLoginTime;

##2020-08-18 21:11
ALTER TABLE chat_log
    add COLUMN camp INT(2) DEFAULT 0 NOT NULL COMMENT '阵营' AFTER account_key;

##2020-08-20 19:12
ALTER TABLE p_detail
    add COLUMN tdData blob COMMENT '塔防数据' AFTER beautyData;

ALTER TABLE p_lord
    add COLUMN rebelCall INT(11) DEFAULT 0 NOT NULL COMMENT '叛军召唤数量' AFTER buyJourneyTimes;
ALTER TABLE p_lord
    add COLUMN killRebel INT(11) DEFAULT 0 NOT NULL COMMENT '击杀叛军数量' AFTER rebelCall;
ALTER TABLE p_lord
    add COLUMN attackPlayerNum INT(11) DEFAULT 0 NOT NULL COMMENT '攻打玩家主城' AFTER killRebel;
ALTER TABLE p_lord
    add COLUMN attackCityNum INT(11) DEFAULT 0 NOT NULL COMMENT '攻打地图上据点次数' AFTER attackPlayerNum;
ALTER TABLE p_lord
    add COLUMN curMainTask INT(11) DEFAULT 0 NOT NULL COMMENT '当前主线任务' AFTER attackCityNum;
ALTER TABLE p_lord
    add COLUMN curMainDupicate INT(11) DEFAULT 0 NOT NULL COMMENT '当前副本进度' AFTER curMainTask;

##2020-09-14 17:52
ALTER TABLE p_account
    add COLUMN isDelete INT(11) DEFAULT 0 NOT NULL COMMENT '角色是否删除(0,未删除  1.已删除)' AFTER lastLoginIp;

##2020-09-14 14:16 美女数据埋点
DROP TABLE IF EXISTS `beauty_item_log`;
CREATE TABLE `beauty_item_log`
(
    `create_date`   datetime(0) NULL DEFAULT NULL COMMENT '日志时间',
    `role_id`       bigint(20)  NOT NULL COMMENT '玩家id',
    `type`          int(2)      NOT NULL COMMENT '1经验值 2魅力值 3亲密度',
    `beauty_id`     int(11)     NOT NULL COMMENT '美女id',
    `change_number` int(11)     NOT NULL COMMENT '亲密度魅力值和经验值的变化数量',
    `item_id`       int(11)     NOT NULL COMMENT '道具id（）',
    `item_add`      int(11)     NOT NULL COMMENT '道具消耗数量',
    `item_source`   int(11)     NOT NULL COMMENT '消耗来源（亲密度：1.免费猜拳 2.半价猜拳 3.全价猜拳 4.赠送鲜花 5赠送钻戒；魅力值：1.赠送耳环 2.赠送香水；经验值：0为约会，消耗时填升级技能id）',
    `state`         int(2)      NOT NULL COMMENT '0产出 1消耗'
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = DYNAMIC;

INSERT INTO data_res
VALUES ("beauty_item_log", "beauty_item_log");
INSERT INTO collection_res
VALUES ("beauty_item_log", "beauty_item_log.log");

##2020-09-15 18:45 塔防
DROP TABLE IF EXISTS `td_log`;
CREATE TABLE `td_log`
(
    `log_date` datetime   NULL     DEFAULT NULL,
    `role_id`  bigint(20) NOT NULL DEFAULT 0,
    `td_id`    int(11)    NOT NULL DEFAULT 0,
    `is_enter` int(2)     NULL     DEFAULT 0,
    `type`     int(2)     NULL     DEFAULT 0,
    `state`    int(2)     NULL     DEFAULT 0,
    `less_hp`  int(11)    NULL     DEFAULT 0
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  ROW_FORMAT = DYNAMIC;
INSERT INTO data_res
VALUES ("td_log", "td_log");
INSERT INTO collection_res
VALUES ("td_log", "td_log.log");

alter table equip_decompound_log
    add column decompose int(11) default 0 COMMENT '获得/分解' after channel;
alter table equip_decompound_log
    add column reason int(11) default 0 COMMENT '来源' after decompose;


ALTER TABLE p_detail
    add COLUMN recordData blob COMMENT 'ui点击记录' AFTER tdData;

ALTER TABLE s_riot_item_shop
    ADD COLUMN propNum int(11) default 0 COMMENT '道具数量' after effect;
ALTER TABLE s_riot_item_shop
    ADD COLUMN propId int(11) default 0 COMMENT '道具id' after propNum;

##2020-11-05 caobing
ALTER TABLE `p_city`
    ADD COLUMN `state` int(11) NOT NULL DEFAULT 0 COMMENT '// 名城状态: 1 正常生产 2.红色图纸生产中 3.红色图纸生产完成' AFTER `nextAttackTime`;

ALTER TABLE `p_world`
    ADD COLUMN `stealCityData` varchar(500) NULL COMMENT '抢夺名称数据' AFTER `worldActPlanData`;

ALTER TABLE `p_lord`
    ADD COLUMN `expertWashSkillTimes` int(11) NOT NULL default 0 COMMENT '秘技精研次数' AFTER `washSkillTimes`;

ALTER TABLE `p_lord`
    ADD COLUMN `buildGift` int(11) NOT NULL DEFAULT 0 COMMENT '建造礼包购买状态' AFTER `curMainDupicate`;

ALTER TABLE `u_pay`
    ADD COLUMN `lv` int(11) NOT NULL DEFAULT 0 COMMENT '付费等级' AFTER `finish_time`;

##2020-11-21 添加登录登出日志 体力增加减少日志
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log`  (
                              `log_date` datetime(0) NULL DEFAULT NULL,
                              `lordId` bigint(20) NULL DEFAULT NULL COMMENT '角色id',
                              `nick` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色昵称',
                              `lv` int(11) NULL DEFAULT NULL COMMENT '等级',
                              `createDate` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                              `loginDate` datetime(0) NULL DEFAULT NULL COMMENT '登录时间',
                              `logoutDate` datetime(0) NULL DEFAULT NULL COMMENT '离线时间',
                              `onlinTime` int(11) NULL DEFAULT 0 COMMENT '在线时长/s'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `energy_log`;
CREATE TABLE `energy_log`  (
                               `log_date` datetime(0) NULL DEFAULT NULL,
                               `lordId` int(11) NULL DEFAULT NULL,
                               `nick` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                               `lv` int(11) NULL DEFAULT NULL,
                               `cost` int(11) NULL DEFAULT NULL,
                               `reason` int(11) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

ALTER TABLE `hero_wash`
    ADD COLUMN `costGold` int(11) NOT NULL DEFAULT 0 COMMENT '洗练钻石' AFTER `channel`;

###CaoBing 2020-11-23
ALTER TABLE `p_lord`
ADD COLUMN `firstPlaySGameTime` bigint(20) NULL COMMENT '首次玩小游戏的时间' AFTER `buildGift`;

###CaoBing 2020-12-01
ALTER TABLE `p_lord`
ADD COLUMN `expertWashSkillTimes` int(11) NULL COMMENT '秘技精研次数' AFTER `washSkillTimes`;

ALTER TABLE `p_city`
ADD COLUMN `breakTime` bigint(20) NULL COMMENT '城池攻破时间' AFTER `state`;

ALTER TABLE `u_server`
    ADD COLUMN `version` int(11) NOT NULL DEFAULT 0 COMMENT '版本' AFTER `accountWhiteList`;

ALTER TABLE `p_lord`
ADD COLUMN `warBookShopRefreshTime` bigint(20) NULL DEFAULT 0 COMMENT '//兵书商城刷新时间' AFTER `firstPlaySGameTime`;

ALTER TABLE `p_country`
ADD COLUMN `tempGovern` blob NULL COMMENT '//(选举期间的临时官员数据)' AFTER `govern`;

ALTER TABLE `p_lord`
ADD COLUMN `warBookShopRefresh` bigint(20) NULL DEFAULT 0 COMMENT '//兵书商城刷新次数' AFTER `warBookShopRefreshTime`;

ALTER TABLE `p_lord`
ADD COLUMN `buyBookShopRefreshTime` bigint(20) NULL DEFAULT 0 COMMENT '//兵书商城购买次数刷新时间' AFTER `warBookShopRefresh`;

ALTER TABLE `p_lord`
    ADD COLUMN `dayRecharge` bigint(11) NULL DEFAULT 0 COMMENT '//每日充值次数' AFTER `buyBookShopRefreshTime`;

DROP TABLE IF EXISTS `hatchery_log`;
CREATE TABLE `hatchery_log`  (
                                 `log_date` datetime(0) NULL DEFAULT NULL,
                                 `lordId` int(11) NULL DEFAULT NULL,
                                 `nick` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
                                 `lv` int(11) NULL DEFAULT NULL,
                                 `title` int(11) NULL DEFAULT NULL,
                                 `point` int(11) NULL DEFAULT NULL,
                                 `source` int(11) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `collection_res` VALUES ('hatchery_log', 'hatchery_log.log');
INSERT INTO `data_res` VALUES ('hatchery_log', 'hatchery_log');

#世界宝箱
ALTER TABLE p_detail add COLUMN worldBox text COMMENT '世界宝箱' AFTER recordData;
#世界宝箱
ALTER TABLE `p_lord`
    ADD COLUMN `wordBoxNum` bigint(11) NULL DEFAULT 0 COMMENT '//宝箱获得次数' AFTER `dayRecharge`;


DROP TABLE IF EXISTS `battle_log`;
CREATE TABLE `battle_log`  (
                                 `log_date` datetime(0) NULL DEFAULT NULL,
                                 `serverId` int(11) NULL DEFAULT NULL,
                                 `channel` int(11) DEFAULT 0,
                                 `attacker` bigint(20) NOT NULL DEFAULT 0,
                                 `defencer` bigint(20) NOT NULL DEFAULT 0,
                                 `got` text DEFAULT NULL,
                                 `lost` text DEFAULT NULL,
                                 `attack_soldier` text DEFAULT NULL,
                                 `defence_soldier` text DEFAULT NULL,
                                 `attack_pos` varchar(255) NULL DEFAULT NULL,
                                 `defence_pos` varchar(255) NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `collection_res` VALUES ('battle_log', 'battle_log.log');
INSERT INTO `data_res` VALUES ('battle_log', 'battle_log');

ALTER TABLE `s_chat`
ADD COLUMN `chatType` int(11) NULL COMMENT '//0.阵营聊天 1.区域聊天 2.私人聊天' AFTER `type`;


#虫族入侵杀虫数
ALTER TABLE `p_lord`
    ADD COLUMN `killRoitNum` bigint(11) NULL DEFAULT 0 COMMENT '虫族入侵杀虫数' AFTER `wordBoxNum`;

ALTER TABLE `u_close_role`
    ADD COLUMN `log_date` datetime NULL COMMENT '操作时间';
ALTER TABLE `u_close_role`
    ADD COLUMN `manager` varchar(255) NULL COMMENT '操作者';
ALTER TABLE `u_close_role`
    ADD COLUMN `nick` varchar(255) NULL COMMENT '昵称';
ALTER TABLE `u_close_role`
    ADD COLUMN `lv` int(11) NULL COMMENT '等级';

#参加活动日志
DROP TABLE IF EXISTS `activity_log`;
CREATE TABLE `activity_log`  (
                               `log_date` datetime(0) NULL DEFAULT NULL,
                               `activityId` int(11) DEFAULT 0,
                               `awardId` int(11) DEFAULT 0,
                               `giftName` varchar(255) DEFAULT '',
                               `isAward` int(2) DEFAULT 0,
                               `roleId` bigint(20) DEFAULT 0
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `collection_res` VALUES ('activity_log', 'activity_log.log');
INSERT INTO `data_res` VALUES ('activity_log', 'activity_log');

#许愿池
DROP TABLE IF EXISTS `act_hope_log`;
CREATE TABLE `act_hope_log`  (
                                 `log_date` datetime(0) NULL DEFAULT NULL,
                                 `serverId` int(11) DEFAULT 0,
                                 `channelId` int(11) DEFAULT 0,
                                 `startTime` datetime(0) NULL DEFAULT NULL,
                                 `level` int(11) DEFAULT 0,
                                 `lordId` bigint(20) DEFAULT 0,
                                 `vip` int(11) DEFAULT 0,
                                 `costGold` int(11) DEFAULT 0,
                                 `getGold` int(11) DEFAULT 0
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `collection_res` VALUES ('act_hope_log', 'act_hope_log.log');
INSERT INTO `data_res` VALUES ('act_hope_log', 'act_hope_log');

#世界宝箱
DROP TABLE IF EXISTS `world_box_log`;
CREATE TABLE `world_box_log`  (
                                 `log_date` datetime(0) NULL DEFAULT NULL,
                                 `lordId` bigint(20) DEFAULT 0,
                                 `nick` varchar(255) DEFAULT '',
                                 `vip` int(11) DEFAULT 0,
                                 `level` int(11) DEFAULT 0,
                                 `count` int(11) DEFAULT 0,
                                 `reason` int(11) DEFAULT 0,
                                 `cur` int(11) DEFAULT 0,
                                 `num` int(11) DEFAULT 0
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `collection_res` VALUES ('world_box_log', 'world_box_log.log');
INSERT INTO `data_res` VALUES ('world_box_log', 'world_box_log');

#个性化
ALTER TABLE `p_lord`
    ADD COLUMN `headIndex` bigint(11) NULL DEFAULT 0 COMMENT '默认头像' AFTER `wordBoxNum`;
ALTER TABLE `p_lord`
    ADD COLUMN `chatIndex` bigint(11) NULL DEFAULT 0 COMMENT '默认聊天' AFTER `headIndex`;
ALTER TABLE `p_detail`
    ADD COLUMN `frame` longtext NULL COMMENT '个性化' AFTER `worldBox`;

#季节,用于判定是否是新服开启
ALTER TABLE `p_world`
ADD COLUMN `seasonUp` int(11) NULL DEFAULT 1 COMMENT '默认为1，新服开启或者老服维护后此值为2' AFTER `refreshTime`;

ALTER TABLE `p_lord`
    ADD COLUMN `bookEffectHoronCd` bigint(20) NULL DEFAULT 0 COMMENT '//兵书对阵营战额外增加荣誉的CD时间' AFTER `killRoitNum`;


#个性签名
ALTER TABLE `p_detail`
    ADD COLUMN `personalSignature` varchar(255) NULL COMMENT '个性签名' AFTER `worldBox`;

#邮件
DROP TABLE IF EXISTS `mail_log`;
CREATE TABLE `mail_log`  (
                                  `log_date` datetime(0) NULL DEFAULT NULL,
                                  `keyId` int(11) DEFAULT 0,
                                  `lordId` bigint(20) DEFAULT 0,
                                  `title` longtext null,
                                  `param` longtext null,
                                  `award` longtext null,
                                  `report` longtext null,
                                  `reportMsg` longtext null,
                                  `createTime` bigint(20) DEFAULT 0,
                                  `heroScots` longtext,
                                  `portrait` bigint(11) DEFAULT 0,
                                  `replyId` bigint(20) DEFAULT 0,
                                  `replyLordId` bigint(20) DEFAULT 0,
                                  `mailKey` int(11) DEFAULT 0,
                                  `mailCollectRes` longtext null,
                                  `soldierRecs` longtext null
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
# INSERT INTO `collection_res` VALUES ('mail_log', 'mail_log.log');
# INSERT INTO `data_res` VALUES ('mail_log', 'mail_log');

alter  table p_world add column riotLevel int(11) default 0 comment '虫族入侵难度';

ALTER TABLE `p_lord`
    ADD COLUMN `skin` int(11) NULL DEFAULT 0 COMMENT '//主城皮肤' AFTER `bookEffectHoronCd`;

alter  table p_world
    ADD COLUMN `seasonUp` int(11) NULL DEFAULT 1 COMMENT '默认为1，新服开启或者老服维护后此值为2' AFTER `refreshTime`;

#日志上报
ALTER TABLE `activity_log`
    ADD COLUMN `vip` int(11) NULL DEFAULT 0 COMMENT 'vip';
ALTER TABLE `activity_log`
    ADD COLUMN `costGold` bigint(20) NULL DEFAULT 0 COMMENT '钻石消耗';
ALTER TABLE `activity_log`
    ADD COLUMN `channel` int(11) NULL DEFAULT 0 COMMENT '渠道';


#英雄搜寻
DROP TABLE IF EXISTS `seek_log`;
CREATE TABLE `seek_log`  (
                             `log_date` datetime(0) NULL DEFAULT NULL,
                             `lordId` bigint(20) DEFAULT 0,
                             `level` int(1) DEFAULT 0,
                             `nick` varchar(255) null,
                             `vip` int(11) DEFAULT 0,
                             `serarchType` int(11) DEFAULT 0,
                             `searchNum` int(11) DEFAULT 0,
                             `costGold` int(11) DEFAULT 0
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `collection_res` VALUES ('seek_log', 'seek_log.log');
INSERT INTO `data_res` VALUES ('seek_log', 'seek_log');

#角色升级添加只会中心等级和科技学院等级
ALTER TABLE `role_exp`
    ADD COLUMN `commandLevel` int(11) NULL DEFAULT 0 COMMENT '指挥中心等级';
ALTER TABLE `role_exp`
    ADD COLUMN `techLevel` int(11) NULL DEFAULT 0 COMMENT '科技学院等级';

#邮件
DROP TABLE IF EXISTS `mail_log`;
CREATE TABLE `mail_log`  (
                             `log_date` datetime(0) NULL DEFAULT NULL,
                             `lordId` bigint(20) DEFAULT 0,
                             `nick` varchar(255) DEFAULT '',
                             `level` int(11) DEFAULT 0,
                             `vip` int(11) DEFAULT 0,
                             `mailId` int(11) DEFAULT 0,
                             `msg` longtext null
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
INSERT INTO `collection_res` VALUES ('mail_log', 'mail_log.log');
INSERT INTO `data_res` VALUES ('mail_log', 'mail_log');


ALTER TABLE `p_activity`
ADD COLUMN `records` longblob NULL COMMENT '奖池抽奖记录' AFTER `params`;

ALTER TABLE `p_lord`
ADD COLUMN `mergeServerStatus` int(11) NULL COMMENT '//合服状态  0:不合服   1:合服' AFTER `skin`;

ALTER TABLE `p_detail`
ADD COLUMN `personChat` longblob NULL COMMENT '私人聊天' AFTER `frame`;

ALTER TABLE `p_detail`
     ADD COLUMN `dailyTask` text COMMENT '日常任务' AFTER `personChat`;

ALTER TABLE `p_detail`
     ADD COLUMN `weekCard` text COMMENT '周卡时间记录' AFTER `dailyTask`;

ALTER TABLE `p_activity`
ADD COLUMN `campMembers` longblob NULL COMMENT '阵营骨干排行' AFTER `records`;

#巨型虫族
ALTER TABLE `p_world`
     ADD COLUMN `bigMonster` text COMMENT '巨型虫族活动' AFTER `riotLevel`;

     ALTER TABLE `p_lord`
ADD COLUMN `isSeven` int(11) NULL DEFAULT 1 COMMENT '1.不推送 2.推送' AFTER `mergeServerStatus`;

#取消自增
Alter table p_lord change lordId lordId bigint(20);

#新增记录群体消耗
ALTER TABLE `p_activity`
ADD COLUMN `record` longblob NULL COMMENT '记录' AFTER `campMembers`;

#巨型虫族战斗记录
ALTER TABLE p_world_map add COLUMN bigMonsterWarData mediumblob AFTER countryWarData;

DROP TABLE IF EXISTS `war_book_log`;
CREATE TABLE `war_book_log`
(
    `log_date`    datetime   DEFAULT NULL COMMENT '日志时间',
    `lordId`     bigint(20) DEFAULT 0 NOT NULL COMMENT '玩家id',
    `nick`     varchar(255)    DEFAULT 0 NOT NULL COMMENT '昵称',
    `level`       int(11)     DEFAULT 0 NOT NULL COMMENT '等级',
    `vip`     int(11)    DEFAULT 0 NOT NULL COMMENT 'vip',
    `bookName` int(11)    DEFAULT 0 NOT NULL COMMENT '兵书名称',
    `reason` int(11)    DEFAULT 0 NOT NULL COMMENT '产出/消耗来源',
    `cost` int(11)    DEFAULT 0 NOT NULL COMMENT '消耗'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

DROP TABLE IF EXISTS `wear_book_log`;
CREATE TABLE `wear_book_log`
(
    `log_date`    datetime   DEFAULT NULL COMMENT '日志时间',
    `lordId`     bigint(20) DEFAULT 0 NOT NULL COMMENT '玩家id',
    `nick`     varchar(255)    DEFAULT 0 NOT NULL COMMENT '昵称',
    `level`       int(11)     DEFAULT 0 NOT NULL COMMENT '等级',
    `vip`     int(11)    DEFAULT 0 NOT NULL COMMENT 'vip',
    `bookName` int(11)    DEFAULT 0 NOT NULL COMMENT '兵书名称'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO `collection_res` VALUES ('war_book_log', 'war_book_log.log');
INSERT INTO `data_res` VALUES ('war_book_log', 'war_book_log');
INSERT INTO `collection_res` VALUES ('wear_book_log', 'wear_book_log.log');
INSERT INTO `data_res` VALUES ('wear_book_log', 'wear_book_log');

#充值服字段提那就
ALTER TABLE u_pay add COLUMN realServer int(11) DEFAULT 0 COMMENT '实际服务器ID';


INSERT INTO `collection_res` VALUES ('omament_log', 'omament_log.log');
INSERT INTO `data_res` VALUES ('omament_log', 'omament_log');
DROP TABLE IF EXISTS `omament_log`;
CREATE TABLE `omament_log`
(
    `log_date`    datetime   DEFAULT NULL COMMENT '日志时间',
    `lordId`     bigint(20) DEFAULT 0 NOT NULL COMMENT '玩家id',
    `nick`     varchar(255)    DEFAULT 0 NOT NULL COMMENT '昵称',
    `level`       int(11)     DEFAULT 0 NOT NULL COMMENT '等级',
    `vip`     int(11)    DEFAULT 0 NOT NULL COMMENT 'vip',
    `omamentId` int(11)    DEFAULT 0 NOT NULL COMMENT '勋章ID',
    `omamentName` varchar(255)   DEFAULT '' NOT NULL COMMENT '勋章名称',
    `reason` int(11)    DEFAULT 0 NOT NULL COMMENT '来源'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

 alter  table p_detail modify  column primaryCollectData  LONGBLOB DEFAULT NULL;


INSERT INTO `collection_res` VALUES ('journey_log', 'journey_log.log');
INSERT INTO `data_res` VALUES ('journey_log', 'journey_log');
DROP TABLE IF EXISTS `journey_log`;
CREATE TABLE `journey_log`
(
    `log_date`    datetime   DEFAULT NULL COMMENT '日志时间',
    `lordId`     bigint(20) DEFAULT 0 NOT NULL COMMENT '玩家id',
    `nick`     varchar(255)    DEFAULT 0 NOT NULL COMMENT '昵称',
    `level`       int(11)     DEFAULT 0 NOT NULL COMMENT '等级',
    `vip`     int(11)    DEFAULT 0 NOT NULL COMMENT 'vip',
    `journeyId` int(11)    DEFAULT 0 NOT NULL COMMENT '关卡ID',
    `result` int(11)    DEFAULT 0 NOT NULL COMMENT '结果',
    `maxJourneyLog` int(11)    DEFAULT 0 NOT NULL COMMENT '玩家最大通过关卡'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

ALTER TABLE `p_account`
ADD COLUMN `channelAccount` varchar(255) NULL DEFAULT '' COMMENT '渠道账号' AFTER `isDelete`;

ALTER TABLE `u_account`
ADD COLUMN `loggedServer` text NULL COMMENT '玩家登录过的服务器' AFTER `serverInfos`;


ALTER TABLE `u_serverinfos`
ADD COLUMN `nick` varchar(255) NOT NULL COMMENT '玩家昵称' AFTER `country`,
ADD COLUMN `level` int(11) NOT NULL COMMENT '玩家等级' AFTER `nick`,
ADD COLUMN `portrait` int(11) NOT NULL COMMENT '玩家头像' AFTER `level`,
ADD COLUMN `lordId` int(11) NOT NULL COMMENT '玩家id' AFTER `portrait`,
ADD COLUMN `createDate` datetime NULL COMMENT '创建时间' AFTER `lordId`;

#新增阵营选票日志记录
CREATE TABLE `glover_log`
(
    `log_date`    datetime   DEFAULT NULL COMMENT '日志时间',
    `lordId`     bigint(20) DEFAULT 0 NOT NULL COMMENT '玩家id',
    `nick`     varchar(255)    DEFAULT 0 NOT NULL COMMENT '昵称',
    `vip`     int(11)    DEFAULT 0 NOT NULL COMMENT 'vip',
    `lv`       int(11)     DEFAULT 0 NOT NULL COMMENT '等级',
    `channel`       int(11)     DEFAULT 0 NOT NULL COMMENT '渠道',
    `ticket` int(11)    DEFAULT 0 NOT NULL COMMENT '变化的选票',
    `vote` int(11)    DEFAULT 0 NOT NULL COMMENT '变化后的选票'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
INSERT INTO `collection_res` VALUES ('glover_log', 'glover_log.log');
INSERT INTO `data_res` VALUES ('glover_log', 'glover_log');

ALTER TABLE `p_detail`
ADD COLUMN `broodInfo` longblob NULL COMMENT '圣域争霸信息' AFTER `weekCard`;

ALTER TABLE `p_brood_war`
ADD COLUMN `occupyPercentage` blob NULL AFTER `occupyTime`;

ALTER TABLE `p_city`
    ADD COLUMN `cityTime` bigint(20) NOT NULL DEFAULT 0 COMMENT '' AFTER `breakTime`,
    ADD COLUMN `flush` int(11) NOT NULL DEFAULT 1 COMMENT '' AFTER `cityTime`,
    ADD COLUMN `exp` int(11) NOT NULL DEFAULT 0 COMMENT '' AFTER `flush`,
    ADD COLUMN `cityName` varchar(255)  DEFAULT NULL COMMENT '' AFTER `exp`;


ALTER TABLE `p_detail`
    ADD COLUMN `buildFortress` blob NULL COMMENT '要塞建设次数' AFTER `broodInfo`;


ALTER TABLE `p_account`
ADD COLUMN `firstLoginDate` datetime(0) NULL COMMENT '当天首次登录时间' AFTER `channelAccount`;


#虫族主宰数据库表和字段2021-12-15
ALTER TABLE `p_world` ADD COLUMN `zerg` blob COMMENT '虫族主宰' AFTER `bigMonster`;
ALTER TABLE `p_world_map` ADD COLUMN `zergWarData` mediumblob COMMENT '虫族主宰数据' AFTER `bigMonsterWarData`;
CREATE TABLE `p_zerg`  (
  `keyId` int(11) NOT NULL AUTO_INCREMENT,
  `cityId` int(11) DEFAULT NULL,
  `mapId` int(11) DEFAULT NULL,
  `x` int(11) DEFAULT NULL,
  `y` int(11) DEFAULT NULL,
  `step` int(11) DEFAULT NULL,
  `wave` int(11) DEFAULT NULL,
  `attacks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `openDate` int(11) DEFAULT NULL,
  `stepEndTime` bigint(20) DEFAULT NULL,
  `preHotTime` bigint(20) DEFAULT NULL,
  `openTime` bigint(20) DEFAULT NULL,
  `endTime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`keyId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

ALTER TABLE `p_detail`
ADD COLUMN `endlessTDInfo` longblob NULL COMMENT '无尽塔防' AFTER `buildFortress`;

ALTER TABLE `p_lord`
ADD COLUMN `tdMoney` int(11) NULL COMMENT '塔防币' AFTER `buyBookShopRefreshTime`;

CREATE TABLE `p_public_data` (
  `id` int(11) NOT NULL,
  `lastSaveTime` bigint(20) DEFAULT NULL,
  `endlessTDRank` longblob,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

#账号服添加日志开关
ALTER TABLE `halo_uc`.`s_version` ADD COLUMN `log_open` int(11) DEFAULT 0 COMMENT '日志是否开启 0未开启 1.开启' AFTER `name`;

#沙盘演武表
CREATE TABLE `p_manoeuvre`  (
  `keyId` bigint(20) NOT NULL AUTO_INCREMENT,
  `startTime` bigint(20) DEFAULT NULL COMMENT '开始时间',
  `status` int(255) DEFAULT NULL COMMENT '活动状态 0.未开启 1.报名阶段 2.准备阶段 3.开始 4.结算',
  `stage` int(11) DEFAULT NULL COMMENT '当前是第几回合1-3',
  `roundOne` int(11) DEFAULT NULL,
  `roundTwo` int(11) DEFAULT NULL,
  `roundThree` int(11) DEFAULT NULL,
  `apply` longblob COMMENT '申请信息',
  `fights` longblob COMMENT '参战玩家信息',
  `roundInfo` longblob COMMENT '回合信息 ',
  `rank` blob COMMENT '排行信息',
  `winer` int(11) DEFAULT 0 COMMENT '胜利国家',
  PRIMARY KEY (`keyId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

ALTER TABLE `p_lord`
ADD COLUMN `clothes` int(11) NULL COMMENT '玩家默认服饰' AFTER `skin`;

#p_lord增加指挥部等级
ALTER TABLE `p_lord`
ADD COLUMN `commandLevel` int(11) NULL DEFAULT NULL COMMENT '指挥部等级' AFTER `tdMoney`;


# 新增沙盘演武积分log数据统计
CREATE TABLE `manoeuvre_log` (
     `keyId` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
     `log_date` datetime DEFAULT NULL COMMENT '日期',
     `role_id` int(11) DEFAULT NULL COMMENT '角色id',
     `nick` varchar(255) DEFAULT NULL COMMENT '角色名称',
     `level` int(11) DEFAULT NULL COMMENT '角色等级',
     `vip_level` int(11) DEFAULT NULL COMMENT 'vip等级',
     `change_point` int(11) DEFAULT NULL COMMENT '变化的积分',
     `item_id` int(11) DEFAULT NULL COMMENT '道具id',
     `item_num` int(11) DEFAULT NULL COMMENT '道具数量',
     `source` int(11) DEFAULT NULL COMMENT '来源',
     `type` int(11) DEFAULT NULL COMMENT '1=消耗 2=获得',
     `point` int(11) DEFAULT NULL COMMENT '剩余积分(变化后)',
     PRIMARY KEY (`keyId`),
     KEY `role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `log_model`.`data_res` (`data_key`, `data_log`) VALUES ('manoeuvre_log', 'manoeuvre_log');
INSERT INTO `log_model`.`collection_res` (`collection_key`, `collection_log`) VALUES ('manoeuvre_log', 'manoeuvre_log.log');
ALTER TABLE `p_city`
ADD COLUMN `firstKill` int(11) NULL DEFAULT 0 COMMENT '首杀国家' AFTER `cityName`;
