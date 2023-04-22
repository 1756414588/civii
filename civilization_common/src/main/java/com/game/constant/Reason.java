package com.game.constant;

//日志来源
public interface Reason {
	int UNKOWN = 0; // 未知原因
	int MISSION_HIRE_HERO = 1; // 副本招募英雄
	int BUY_MISSION_RESOURCE_TIMES = 2; // 购买资源副本次数
	int PAY = 3; // 充值
	int BUY_MISSION_EQUIP_PAPER = 4; // 购买副本装备图纸
	int COLLECT_RESOURCE = 5; // 征收资源
	int HIRE_OFFICER = 6; // 雇佣内政官
	int FIGHT_RESOURCE_MISSION = 7; // 攻打资源副本
	int LEVEL_UP_BUILDING = 8; // 升级建筑
	int USE_ITEM = 9; // 使用道具
	int KILL_BUILD_CD = 10; // 建筑加速
	int INIT_PLAYER = 11; // 玩家初始化
	int BUY_BUILD_TEAM = 12; // 购买建造队
	int BUY_PROTECTED_TIME = 13; // 购买城防
	int BUY_DEPOT = 14; // 购买聚宝盆
	int IRON_BUY_DEPOT = 15; // 购买聚宝盆
	int BUY_DEPOT_ITEM = 16; // 聚宝盆获得道具
	int RECRUIT_SOLDIER = 17; // 募兵
	int LARGER_BARRACKS = 18; // 兵营扩容
	int LEVEL_UP_RECRUIT_TIME = 19; // 增加募兵时间
	int KILL_RECRUIT_CD = 20; // 募兵秒CD
	int BUY_SHOP = 21; // 购买军事其他商店道具
	int BUY_VIP_GIFT = 22; // 购买vip礼包
	int BUY_VIP_SHOP = 23; // 购买vip特价道具
	int COMPOUND_ITEM = 24; // 合成装备
	int COMPOUND_KILL_CD = 25; // 打造秒CD
	int SELL_ITEM = 26; // 出售道具
	int WASH_HERO = 27; // 洗练英雄
	int SWEEP_MISSION = 28; // 关卡扫荡
	int BUY_ENERGY = 29; // 购买体力值
	int DECOMPOUSE_EQUIP = 30; // 装备分解
	int MISSION_DONE = 31; // 完成关卡
	int LOOT_COMMON_HERO = 32; // 良将寻访
	int LOOT_GOOD_HERO = 33; // 神将寻访
	int ADVANCE_HERO = 34; // 武将突破
	int INIT_LORD = 35; // 玩家初始化
	int PROP_OPEN_BUILDING = 36; // 资源图纸开放建筑
	int HIRE_RESEARCHER = 37; // 雇佣研究员
	int KILL_TECH_CD = 38; // 秒研究进度CD
	int LEVEL_UP_TECH = 39; // 升级科技
	int TASK_AWARD = 40; // 领取任务奖励
	int CITY_MOVE = 41; // 随机迁城
	int MAKE_PROP = 42; // 生产道具
	int BUY_PROP_WORKQUE = 43; // 购买生产队列
	int BUY_KILL_EQUIP = 44; // 购买国器碎片
	int ACT_AWARD = 45; // 活动奖励
	int ACT_QUOTA = 46; // 半价购买
	int ACT_DIAL_STONE = 47; // 宝石转盘
	int ACT_KONTOW_HERO = 48; // 七星拜将
	int ACT_INVEST = 49; // 投资计划
	int PRIMARY_COLLECT = 50; // 初级资源采集
	int ACT_FOOT = 51; // 屯田活动
	int ACT_MISSION_DROP = 52; // 活动掉落
	int ACT_DIAL_LUCK = 53; // 幸运罗盘
	int KILL_WORLD_MONSTER = 54; // 击杀世界叛军
	int KILL_WALL_CD = 55; // 秒城防cd
	int LEVEL_UP_DEFENDER = 56; // 升级城防军
	int KICKED_OVER = 57; // 玩家被击飞
	int UP_TITLE = 58; // 升级爵位
	int COUNTRY_TASK = 59; // 国家任务
	int COUNTRY_HERO = 60; // 国家名将
	int CANCEL_SOLDIER = 61; // 募兵
	int COUNTRY_GLORY = 62; // 国家荣誉
	int COUNTRY_VOTE = 63; // 国家拉票
	int GM_TOOL = 64; // GM工具
	int MAIL_AWARD = 65; // 邮件奖励
	int FIX_CITY = 66; // 修复城池
	int KILL_WORLD_BOSS = 67; // 击杀世界boss
	int WORLD_TARGET = 68; // 完成世界目标
	int COUNTRY_BUILD = 69; // 国家建设
	int GET_CITY_AWARD = 70; // 据点征收
	int SCOUT = 71; // 侦察
	int ADD_SOLDIER = 72; // 补兵
	int EMBATTLE = 73; // 上阵
	int MAP_MOVE = 74; // 迁城
	int SEND_MAIL = 75; // 发放邮件
	int GENEREL = 76; // 任命将军
	int ATTACK_CITY = 77; // 城战
	int SPEED_MARCH = 78; // 行军加速
	int CANCEL_MARCH = 79; // 行军撤回
	int CALL_TRANSFER = 80; // 国家召唤
	int COUNTRY_WAR = 81; // 国战
	int REBUILD_WARE = 82; // 重建家园
	int WAR = 83; // 战斗
	int WEAR_EQUIP = 84; // 穿装备
	int TAKE_EQUIP = 85; // 脱装备
	int WORLD_BATTLE = 86; // 世界战斗获得
	int QUICK_WAR = 87; // 闪电战
	int UpdateHero = 88; // 英雄属性发生改变
	int InitLord = 89; // 初始化玩家
	int AutoAdd = 90; // 自动补兵
	int MISSION_FAILED = 91; // 副本失败
	int MISSION_WIN = 92; // 副本成功
	int AUTO_BUILD = 93; //
	int LEVEL_UP_LORD = 94; // 玩家升级
	int BUY_STONE_DIAL = 95; // 购买罗盘
	int EXCHANGE_RES = 96; // 补给站兑换资源
	int CHAT = 97; // 聊天
	int FIND_COUNTRY_HERO = 98; // 国家名将寻访
	int GIFT_CODE = 99; // 使用兑换码
	int DEV_CITY = 100; // 开发城池
	int COLLECT_RES_WAR = 101; // 资源遭遇战
	int REFRESH_KONTOW_REFRESH = 102; // 七星拜将刷新
	int WORLD_RESOURCE_COLLECT = 103; // 世界资源采集
	int COMPOUND_KILL_EQUIP = 104; // 合成杀器
	int UP_KILL_EQUIP = 105; // 升级国器
	int SHARE_MAIL = 106; // 分享邮件
	int NEW_LORD = 107; // 新玩家
	int ACT_EXCHANGE_HERO = 108; // 活动兑换英雄
	int ACT_EXCHANGE_ITEM = 109; // 活动兑换道具
	int WORLD_PVP_BATTLE = 110; // 血战要塞
	int DIG_PAPER = 111; // 挖宝
	int EXCHANGE_PAPER = 112; // 血战兑换图纸
	int GREET_BANQUET = 113; // 国宴恭贺
	int TRAIN_COUNTRY_HERO = 114; // 国家名将培养
	int COUNTRY_HERO_ESCAPE = 115; // 国家名将逃跑
	int COUNTRY_HERO_ACTIVATE = 116; // 国家名将激活
	int SOLDIER_REC = 117; // 伤兵恢复
	int FAKE_PAY = 118; // 伪造充值
	int FIRST_PAY = 119; // 首充活动
	int START_PRIMRAY_RESOURCE = 120; // 开始采集初级采集点
	int KILL_CACULATE = 121; // 击杀后结算
	int NEW_STATE = 122; // 新手引导
	int REBUILD_CITY = 123; // 重建城池
	int MISSION_STAR = 124; // 副本星级奖励
	int ACT_SEVEN = 125; // 打世界波次怪
	int ACT_ORANGE_DIAL = 126; // 橙装转盘
	int MAKE_EQUIP = 127; // 合成装备
	int RIOT_WAVE = 128; // 打世界波次怪
	int MARCH_RETURN = 129; // 行军回城
	int STEAL_CITY = 130; // 抢夺名城
	int REFRESH_MEETING_HERO_SOLDIERS = 131;// 刷新指挥部城防部队消耗石油
	int BUY_DEFENSE_HERO_SOLDIER = 132; // 购买城防军兵力
	int AWARD_WORLD_PERSON_TARGET = 133; // 世界目标个人任务领奖
	int AWARD_WORLD_TARGET = 134; // 世界目标世界任务领奖
	int KILL_WORLD_BOSS_EXP = 135; // 击杀世界boss获取经验
	int GM_ADD_GOODS = 136; // GM命令添加物品 所有物品
	int RES_PACKET_COST = 137; // 资源打包花费
	int RES_PACKET_ADD = 138; // 资源打包添加物品

	int SUB_MODIFT_COUNTRY_NAME = 139; // 更改国家名字减少元宝

	int ADD_MODIFT_COUNTRY_NAME = 140; // 更改国家名字失败增加元宝

	int REBUILD_BUILDING_ = 141; // 建筑重建

	int BUY_REBUILD_BUILDING_ = 142; // 购买建筑重建

	int OPEN_RESOURCE_BOX = 143; // 开随机资源宝箱

	int KILL_REBEL_MONSTER = 144; // 伏击叛军

	int REBEL_ITEM_EXCHANGE = 145; // 伏击叛军道具兑换

	int MONTH_CARD_AWARD = 146; // 伏击叛军道具兑换

	int BEAUTY_RENAME = 147; // 美女改名

	int BEAUTY_EXP = 148; // 美女经验值

	int BEAUTY_CHARM_VALUE = 149; // 美女魅力值

	int BEAUTY_INTIMACY_VALUEE = 150; // 美女亲密度

	int ACT_PURPLE_DIAL = 151; // 紫装转盘

	int USE_CDK_AWARD = 152; // 使用cdk
	int FIX_SEVEN_LOGIN = 153; // 补签七日签到
	int ACT_CRYSTAL_DIAL = 154; // 钻石晶体转盘
	int ACT_DAILY_CHECKIN = 155; // 钻石晶体转盘

	int SUB_OMAMENT = 156; // 扣减配饰
	int ADD_OMAMENTE = 157; // 增加配饰
	int COMPOSE_OMAMENTE = 1571; // 合成配饰

	int ADD_FRIEND_SHOP_AWARD = 158; // 增加师徒奖励
	int BUY_EQUIP_SLOP = 159;// 购买装备背包格子
	int WASH_EQUIP = 160; // 洗练装备
	int CREATE_ACCOUNT = 161; // 创建账号

	int SUB_JOURNEY_TIMES = 162; // 扣减远征次数
	int ADD_JOURNEY_TIMES = 163; // 增加远征次数
	int BUY_JOURNEY_TIMES = 164; // 购买远征次数
	int BUY_BEAUTY_PROP = 165; // 购买美女道具
	int TD_STAR_REWARD = 166; // 塔防星级奖励
	int ACT_HOPE = 167; // 许愿池
	int ACT_ARMS_PAY = 168; // 许愿池
	int ACT_EQUIPT_WASH = 169; // 装备精研
	// 虫族入侵
	int RIOT_ITEM_BUY = 170; // 信物兑换
	int RIOT_GOLD_BUY = 171; // 钻石购买
	int RIOT_SCORE_BUY = 172; // 积分购买
	int CITY_SIMALL_GAME = 173;// 主城小游戏
	int PASSPORT_SCORE = 174;// 主城小游戏
	int RIOT_ATTACK = 175;// 虫族入侵防守
	int STEAL_CITY_AWARD = 176;// 抢夺名城奖励
	int ENERGY_RESET = 177; // 体力恢复
	int SUB_EQUIP_EXPERT_WASH_TIMES = 188; // 消耗免费秘技精研次数
	int ACT_DOUBLE_EGG_CHANGE = 189; // 双旦活动兑换
	int ACT_DOUBLE_EGG = 190; // 双旦活动
	int SEND_COUNTRY_GOLD = 200;// 发送阵营邮件
	int DECOMPOUSE_BOOK = 201; // 兵书分解
	int WEAR_BOOK = 202; // 穿兵书
	int TAKE_BOOK = 203; // 脱兵书
	int BOOK_EXCHANGE = 204; // 兵书兑换
	int BOOK_SHOP = 205; // 兵书商城
	int WORLD_BOX_OPEN = 206;// 世界宝箱开启
	int BOOK_STRONG = 207; // 兵书强化
	int ADD_HERO_EXP_BOOK = 208; // 兵书技能增加英雄经验值
	int ADD_AWARD_BY_BOOK = 209; // 兵书技能增加奖励
	int BUY_PASS_PORT_LV = 210;// 购买通行证等级
	int WORLD_RANK_AWARD = 211;// 打世界boss给奖励
	int WORLD_BODD_HOBOR = 212;// 打世界boss给军工
	int AUTO_WARD = 213; // 自动穿戴
	int ACT_HERO_DIAL = 214; // 夺命魅影
	int MY_EQUIP_FRAGMENT = 215; // 夺命魅影装备碎片合成
	int DAILY_ACTIVE = 216; // 每日任务活跃 奖励
	int ACT_HERO_TASK = 217;// 无畏尖兵
	int CREATE_FULL_PLAYER = 218;// 创建角色加入对应阵营发送奖励
	int LUCK_POOL = 219; // 辛运奖池

	int ACT_RECHAR_DIAL = 220; // 充值转盘
	int ACT_RAIDERS = 221; // 夺宝奇兵
	int COMMAND_SKIN = 222;// 主城皮肤
	int LUCK_DIAL = 223;// 好运转盘
	int ACT_EGG = 224; // 幸运砸蛋
	int ACT_SEARCH = 225; // 物质搜寻
	int UP_BEAUTY_KILL = 225;// 升级美女技能
	int BEAUTY_APPOINTMENT = 226;// 美女约会
	int BEAUTY_GAME = 227;// 美女小游戏
	int BEAUTY_GIVING_GIFTS = 228;// 美女送礼
	int AUTO_KILL_REWARD = 229; // 自动杀虫奖励
	int AUTO_KILL = 230; // 自动杀虫
	int BROOD_WAR = 231; // 母巢
	int FORTRESS_BUILD = 232;
	int TELNET = 233;
	int BUY_CONVERT_SHOP = 234; // 无尽塔防兑换商店
	int RANKING_REWARD = 235; // 无尽塔防排行奖励
	int ENDLESS_TD_FIGHT_AUTO = 236; // 无尽塔防自动挑战
	int ENDLESS_TD_START_GAME = 237; // 无尽塔防开始游戏
	int ACT_MATERIAL_SUBSTITUTION = 238; // 材料置换
	int ACT_SPRING_FESTIVAL = 239; // 春节活动
	int ACT_SPRING_GIFT = 240; // 春节特惠
	int RESOURCE_GIFT = 241; // 资源礼包
	int ACT_TD_SEVEN_TASK = 242; // 塔防活动
	int GET_FISH_ATLAS_AWARD = 243; // 渔场图鉴奖励
	int GET_FISH_SHOP_AWARD = 244; // 渔场兑换奖励
	int ROBOT_ADD = 246; // 机器人添加

	int FLAME = 245;

	int ACHI = 247;

	enum ReasonName {
		key_0(UNKOWN, " 未知原因"), key_1(MISSION_HIRE_HERO, "副本招募英雄"), key_2(BUY_MISSION_RESOURCE_TIMES, "购买资源副本次数"), key_3(PAY, "充值"), key_4(BUY_MISSION_EQUIP_PAPER, "购买副本装备图纸"), key_5(COLLECT_RESOURCE, "征收资源"), key_6(HIRE_OFFICER, "雇佣内政官"), key_7(FIGHT_RESOURCE_MISSION, "攻打资源副本"), key_8(LEVEL_UP_BUILDING, "升级建筑"), key_9(USE_ITEM, "使用道具"), key_10(KILL_BUILD_CD, "建筑加速"), key_11(INIT_PLAYER, "玩家初始化"), key_12(BUY_BUILD_TEAM, "购买建造队"), key_13(BUY_PROTECTED_TIME, "购买城防"), key_14(BUY_DEPOT, "购买聚宝盆"), key_15(IRON_BUY_DEPOT, "购买聚宝盆"), key_16(BUY_DEPOT_ITEM, "聚宝盆获得道具"), key_17(RECRUIT_SOLDIER, "募兵"), key_18(LARGER_BARRACKS, "兵营扩容"), key_19(LEVEL_UP_RECRUIT_TIME, "增加募兵时间"), key_20(KILL_RECRUIT_CD, "募兵秒CD"), key_21(BUY_SHOP, "购买军事其他商店道具"), key_22(BUY_VIP_GIFT, "购买vip礼包"), key_23(BUY_VIP_SHOP, "购买vip特价道具"), key_24(COMPOUND_ITEM, "合成装备"), key_25(COMPOUND_KILL_CD, "打造秒CD"), key_26(SELL_ITEM, "出售道具"), key_27(WASH_HERO, "洗练英雄"), key_28(SWEEP_MISSION, "关卡扫荡"), key_29(BUY_ENERGY, "购买体力值"),
		key_30(DECOMPOUSE_EQUIP, "装备分解"), key_31(MISSION_DONE, "完成关卡"), key_32(LOOT_COMMON_HERO, "良将寻访"), key_33(LOOT_GOOD_HERO, "神将寻访"), key_34(ADVANCE_HERO, "武将突破"), key_35(INIT_LORD, "玩家初始化"), key_36(PROP_OPEN_BUILDING, "资源图纸开放建筑"), key_37(HIRE_RESEARCHER, "雇佣研究员"), key_38(KILL_TECH_CD, "秒研究进度CD"), key_39(LEVEL_UP_TECH, "升级科技"), key_40(TASK_AWARD, "领取任务奖励"), key_41(CITY_MOVE, "随机迁城"), key_42(MAKE_PROP, "生产道具"), key_43(BUY_PROP_WORKQUE, "购买生产队列"), key_44(BUY_KILL_EQUIP, "购买国器碎片"), key_45(ACT_AWARD, "活动奖励"), key_46(ACT_QUOTA, "半价购买"), key_47(ACT_DIAL_STONE, "宝石转盘"), key_48(ACT_KONTOW_HERO, "七星拜将"), key_49(ACT_INVEST, "投资计划"), key_50(PRIMARY_COLLECT, "初级资源采集"), key_51(ACT_FOOT, "屯田活动"), key_52(ACT_MISSION_DROP, "活动掉落"), key_53(ACT_DIAL_LUCK, "幸运罗盘"), key_54(KILL_WORLD_MONSTER, "击杀世界叛军"), key_55(KILL_WALL_CD, "秒城防cd"), key_56(LEVEL_UP_DEFENDER, "升级城防军"), key_57(KICKED_OVER, "玩家被击飞"), key_58(UP_TITLE, "升级爵位"), key_59(COUNTRY_TASK, "国家任务"), key_60(COUNTRY_HERO, "国家名将"),
		key_61(CANCEL_SOLDIER, "募兵"), key_62(COUNTRY_GLORY, "国家荣誉"), key_63(COUNTRY_VOTE, "国家拉票"), key_64(GM_TOOL, "GM工具"), key_65(MAIL_AWARD, "邮件奖励"), key_66(FIX_CITY, "修复城池"), key_67(KILL_WORLD_BOSS, "击杀世界boss"), key_68(WORLD_TARGET, "完成世界目标"), key_69(COUNTRY_BUILD, "国家建设"), key_70(GET_CITY_AWARD, "据点征收"), key_71(SCOUT, "侦察"), key_72(ADD_SOLDIER, "补兵"), key_73(EMBATTLE, "上阵"), key_74(MAP_MOVE, "迁城"), key_75(SEND_MAIL, "发放邮件"), key_76(GENEREL, "任命将军"), key_77(ATTACK_CITY, "城战"), key_78(SPEED_MARCH, "行军加速"), key_79(CANCEL_MARCH, "行军撤回"), key_80(CALL_TRANSFER, "国家召唤"), key_81(COUNTRY_WAR, "国战"), key_82(REBUILD_WARE, "重建家园"), key_83(WAR, "战斗"), key_84(WEAR_EQUIP, "穿装备"), key_85(TAKE_EQUIP, "脱装备"), key_86(WORLD_BATTLE, "世界战斗获得"), key_87(QUICK_WAR, "闪电战"), key_88(UpdateHero, "英雄属性发生改变"), key_89(InitLord, "初始化玩家"), key_90(AutoAdd, "自动补兵"), key_91(MISSION_FAILED, "副本失败"), key_92(MISSION_WIN, "副本成功"), key_93(AUTO_BUILD, ""), key_94(LEVEL_UP_LORD, "玩家升级"), key_95(BUY_STONE_DIAL, "购买罗盘"),
		key_96(EXCHANGE_RES, "补给站兑换资源"), key_97(CHAT, "聊天"), key_98(FIND_COUNTRY_HERO, "国家名将寻访"), key_99(GIFT_CODE, "使用兑换码"), key_100(DEV_CITY, "开发城池"), key_101(COLLECT_RES_WAR, "资源遭遇战"), key_102(REFRESH_KONTOW_REFRESH, "七星拜将刷新"), key_103(WORLD_RESOURCE_COLLECT, "世界资源采集"), key_104(COMPOUND_KILL_EQUIP, "合成杀器"), key_105(UP_KILL_EQUIP, "升级国器"), key_106(SHARE_MAIL, "分享邮件"), key_107(NEW_LORD, "新玩家"), key_108(ACT_EXCHANGE_HERO, "活动兑换英雄"), key_109(ACT_EXCHANGE_ITEM, "活动兑换道具"), key_110(WORLD_PVP_BATTLE, "血战要塞"), key_111(DIG_PAPER, "挖宝"), key_112(EXCHANGE_PAPER, "血战兑换图纸"), key_113(GREET_BANQUET, "国宴恭贺"), key_114(TRAIN_COUNTRY_HERO, "国家名将培养"), key_115(COUNTRY_HERO_ESCAPE, "国家名将逃跑"), key_116(COUNTRY_HERO_ACTIVATE, "国家名将激活"), key_117(SOLDIER_REC, "伤兵恢复"), key_118(FAKE_PAY, "伪造充值"), key_119(FIRST_PAY, "首充活动"), key_120(START_PRIMRAY_RESOURCE, "开始采集初级采集点"), key_121(KILL_CACULATE, "击杀后结算"), key_122(NEW_STATE, "新手引导"), key_123(REBUILD_CITY, "重建城池"), key_124(MISSION_STAR, "副本星级奖励"),
		key_125(ACT_SEVEN, "打世界波次怪"), key_126(ACT_ORANGE_DIAL, "橙装转盘"), key_127(MAKE_EQUIP, "合成装备"), key_128(RIOT_WAVE, "打世界波次怪"), key_129(MARCH_RETURN, "行军回城"), key_130(STEAL_CITY, "抢夺名城"), key_131(REFRESH_MEETING_HERO_SOLDIERS, "刷新指挥部城防部队消耗石油"), key_132(BUY_DEFENSE_HERO_SOLDIER, "购买城防军兵力"), key_133(AWARD_WORLD_PERSON_TARGET, "世界目标个人任务领奖"), key_134(AWARD_WORLD_TARGET, "世界目标世界任务领奖"), key_135(KILL_WORLD_BOSS_EXP, "击杀世界boss获取经验"), key_136(GM_ADD_GOODS, "GM命令添加物品所有物品"), key_137(RES_PACKET_COST, "资源打包花费"), key_138(RES_PACKET_ADD, "资源打包添加物品"), key_139(SUB_MODIFT_COUNTRY_NAME, "更改国家名字减少元宝"), key_140(ADD_MODIFT_COUNTRY_NAME, "更改国家名字失败增加元宝"), key_141(REBUILD_BUILDING_, "建筑重建"), key_142(BUY_REBUILD_BUILDING_, "购买建筑重建"), key_143(OPEN_RESOURCE_BOX, "开随机资源宝箱"), key_144(KILL_REBEL_MONSTER, "伏击叛军"), key_145(REBEL_ITEM_EXCHANGE, "伏击叛军道具兑换"), key_146(MONTH_CARD_AWARD, "伏击叛军道具兑换"), key_147(BEAUTY_RENAME, "美女改名"), key_148(BEAUTY_EXP, "美女经验值"), key_149(BEAUTY_CHARM_VALUE, "美女魅力值"),
		key_150(BEAUTY_INTIMACY_VALUEE, "美女亲密度"), key_151(ACT_PURPLE_DIAL, "紫装转盘"), key_152(USE_CDK_AWARD, "使用cdk"), key_153(FIX_SEVEN_LOGIN, "补签七日签到"), key_154(ACT_CRYSTAL_DIAL, "钻石晶体转盘"), key_155(ACT_DAILY_CHECKIN, "钻石晶体转盘"), key_156(SUB_OMAMENT, "扣减配饰"), key_157(ADD_OMAMENTE, "增加配饰"), key_158(ADD_FRIEND_SHOP_AWARD, "增加师徒奖励"), key_159(BUY_EQUIP_SLOP, "购买装备背包格子"), key_160(WASH_EQUIP, "洗练装备"), key_161(CREATE_ACCOUNT, "创建账号"), key_162(SUB_JOURNEY_TIMES, "扣减远征次数"), key_163(ADD_JOURNEY_TIMES, "增加远征次数"), key_164(BUY_JOURNEY_TIMES, "购买远征次数"), key_165(BUY_BEAUTY_PROP, "购买美女道具"), key_166(TD_STAR_REWARD, "塔防星级奖励"), key_167(ACT_HOPE, "许愿池"), key_168(ACT_ARMS_PAY, "许愿池"), key_169(ACT_EQUIPT_WASH, "装备精研"), key_170(RIOT_ITEM_BUY, "信物兑换"), key_171(RIOT_GOLD_BUY, "钻石购买"), key_172(RIOT_SCORE_BUY, "积分购买"), key_173(CITY_SIMALL_GAME, "主城小游戏"), key_174(PASSPORT_SCORE, "主城小游戏"), key_175(RIOT_ATTACK, "虫族入侵防守"), key_176(STEAL_CITY_AWARD, "抢夺名城奖励"), key_177(ENERGY_RESET, "体力恢复"),
		key_188(SUB_EQUIP_EXPERT_WASH_TIMES, "消耗免费秘技精研次数"), key_189(ACT_DOUBLE_EGG_CHANGE, "双旦活动兑换"), key_190(ACT_DOUBLE_EGG, "双旦活动"), key_200(SEND_COUNTRY_GOLD, "发送阵营邮件"), key_201(DECOMPOUSE_BOOK, "兵书分解"), key_202(WEAR_BOOK, "穿兵书"), key_203(TAKE_BOOK, "脱兵书"), key_204(BOOK_EXCHANGE, "兵书兑换"), key_205(BOOK_SHOP, "兵书商城"), key_206(WORLD_BOX_OPEN, "世界宝箱开启"), key_207(WORLD_BOX_OPEN, "兵书强化"), key_208(ADD_HERO_EXP_BOOK, "兵书技能增加英雄经验值"), key_209(ADD_AWARD_BY_BOOK, "兵书技能增加奖励"), key_210(BUY_PASS_PORT_LV, "购买通行证等级"), key_211(WORLD_RANK_AWARD, "打世界boss给奖励"), key_212(WORLD_BODD_HOBOR, "打世界boss给军工"), key_219(LUCK_POOL, "幸运奖池抽奖"), key_222(COMMAND_SKIN, "主城皮肤升级"), key_223(UP_BEAUTY_KILL, "美女升星"), key_224(BEAUTY_APPOINTMENT, "美女约会"), Key_229(AUTO_KILL_REWARD, "自动杀虫奖励"), Key_230(AUTO_KILL, "自动杀虫"), Key_231(BROOD_WAR, "母巢"),;

		int val;
		String name;

		ReasonName(int val, String name) {
			this.val = val;
			this.name = name;
		}

		public static String getName(int val) {
			for (ReasonName n : values()) {
				if (n.val == val) {
					return n.name;
				}
			}
			return key_0.name;
		}
	}
}
