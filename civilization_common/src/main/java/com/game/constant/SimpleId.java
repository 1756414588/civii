package com.game.constant;

// 具体含义见配置表s_simple_config
// 使用方法staticLimit.getNum(SimpleId.x);
public interface SimpleId {
	int COLLECT_NUM = 1; // 玩家周围初级采集生成个数
	int COLLECT_TYPE_NUM = 2; // 初级资源种类
	int COLLECT_FLUSH = 3; // 初级资源点刷新间隔(秒)
	int COLLECT_CELL_NUM = 4; // 玩家周围几个格子刷初级采集
	int WORLD_MONSTER_CELL_NUM = 5; // 玩家周围几个格子刷叛军
	int WORLD_MONSTER_NUM = 6; // 玩家周围叛军生成个数
	int RESOURCE_CELL_NUM = 9; // 资源点周围几个格子不能有叛军
	int REBAL_ATTACK_LV = 36; // 增援等级限制
	int COST_CARD = 182;
	int MINING_LEVEL_NUM = 187; // 采集部队坑位开启配置
	int PROP_CHANGE_GOLD = 192; // 活动结束道具兑换金币数量
	int KILL_RIOT_MAX = 255; // 虫族入侵每日杀虫数
	int HERO_ADVANCE_ADD = 256; // 突破成功加的进度
	int ACT_KILL_ALL = 259; // 大杀四方计算杀敌数的战斗类型
	int ACT_DOUBLE_EGG_KILL_WORM = 263; // 双蛋活动击飞虫族获得礼包概率
	int ACT_DOUBLE_EGG_FLY_PERSION = 264; // 双蛋活动击飞玩家获得礼包概率
	int ACT_DOUBLE_EGG_PROP = 265; /// 双蛋活动道具id
	int ACT_DOUBLE_EGG_DAYLY_MAX_PROP = 266; /// 双蛋活动每日最大获得道具ID
	int ACT_COUNTRY_MAIL = 271; // 阵营邮件ID
	int WORLD_BOX_EXCHANGE = 274; // 世界宝箱兑换条件
	int WORLD_BOX_DOUBLE = 275; // 触发双倍的概率
	int WORLD_BOX_MAX = 276; // 276 世界宝箱总格子数量（最多同时拥有几个宝箱）
	int WORLD_BOX_DAY_OPEN = 277;// 世界宝箱每日可开启数量
	int WORLD_BOX_DAY_POINT = 278;// 世界宝箱活动每日可获取的贡献值
	int WORLD_BOX_TOTAL_POINT = 279;// 世界宝箱活动可累积的贡献值
	int WORLD_BOX_OPEN_KEY = 280;// 世界宝箱钥匙
	int LUCK_POOL_INI_GOLD = 281;// 幸运奖池活动奖池初始钻石数量
	int LUCK_POOL_ONE_GOLD = 283;// 幸运奖池抽一次所需要的充值钻石额度
	int WORLD_BOX_THREE_TIMES = 284;// 世界宝箱前三次获得的宝箱
	int LUCK_POOL_MIN_GOLD = 285;// 幸运奖池最低钻石数量
	int LUXURY_GIFT = 286; // 豪华礼包充值需要金额
	int WEAR_HERO = 290; // 需要自带一套白色装备的英雄id
	int WEAR_EQUIP = 291; // 需要穿戴的白色装备id
	int WORK_SHOP_ADDITIONAL_COUNT = 299; // 材料工厂额外添加一个位置
	int CREATE_FULL_PLAYER_GOLD = 302; // 推荐阵营奖励邮件，奖励300钻石 邮件Id 43
	int CREATE_FULL_PLAYER_VIP = 303; // 推荐阵营奖励邮件，奖励vip点数60 邮件Id 43
	int WIN_OR_LOSE_THE_GAME = 305; // 小游戏好感加成[胜,负]
	int APPOINTMENT_INTIMACYVALUE = 306; // 约会好感加成
	int EVERYDAY_BEAUTY_GAME_TIMES = 307; // 美女每日游戏次数
	int TASK_HERO = 308; // 王牌球手
	// 巨型虫族每日最大获得奖励数量
	int BIG_MONSTER_REWARD = 310;
	// 小游戏虫子数量
	int CITY_GAME_WORMS = 311;
	// 奖章活动兑换道具
	int ACT_MEDAL_PROP = 312;
	int CLICK_BEAUTY = 314; // 美女点击
	// 损兵低于n解锁该等级
	int AUTO_MIN = 315;
	// 兵力池最大兵力
	int AUTO_MAX_SOLDIER = 316;
	// 离线多久停止自动杀虫
	int AUTO_LEAVE_TIME = 317;
	// 损兵范围
	int AUTO_SOLDIER_COST = 318;
	// 结算时长
	int AUTO_REWARD_TIME = 319;
	// 大V带队等级限制
	int ACT_HIGHT_VIP = 330;
	int COUNTRY_MAIL_LEVEL = 331;
	// 秘书礼包
	int BEAUTY_GIFT = 332;
	// 阵营邮件发送时阵营排行名次限制
	int COUNTRY_MAIL_RANK_LIMIT = 335;
	// 端午活动兑换道具
	int ACT_DRAON_BOAT_PROP = 337;
	// 英雄洗练
	int WASH_HERO = 338;
	// 预备役任务敌军刷新范围格子数
	int MEETING_MONSTER_CELL_NUM = 339;
	// 母巢之战一点损兵对应获得军功数量
	int BROOD_WAR_DIE_SOLDIERS = 344;
	// 母巢购买buff次数
	int BROOD_WAR_BUF_BUFF = 348;
	// 母巢进度增加量
	int BROOD_WAR_PROCESS = 350;
	// 母巢进度减少千分比
	int BROOD_WAR_PROCESS_LIMIT = 351;
	int BROOD_WAR_TURN_FIGHT_TIME = 354;
	int BROOD_WAR_REVIVE_TIME = 355;
	// 邀请战友列表战友显示数量
	int INVITE_COMPANION_LIST_NUM = 356;
	int ACTPOWER_MAKEUP = 358;
	// 官员选举持续时间c
	int VOTE_TIME = 359;
	//春节活动春节灯笼价格
	int LANTERN_PRICE = 360;
	int HERO_TALNET = 361;
	//切磋功能的等级开启条件
	int DUEL_LIMIT = 363;
	//春节灯笼活动结束后兑换资源
	int LANTERN_AWARD = 368;

	int MAX_BOOK_NUM = 370;

	int CHAT_LEVEL = 236;
	int PER_CHAT_LEVEL = 371;

	int FIRE_OUT_TIME = 373;
	int FIRE_MAX_RESOURCE = 374;
	int FIRE_MAX_ENTER = 375;

	int CITY_REMARK_TIME=376;
	int CITY_REMARK_COUNT=377;
}
