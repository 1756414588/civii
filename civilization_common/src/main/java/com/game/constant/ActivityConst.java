package com.game.constant;

public class ActivityConst {
    public static final int TYPE_SET = 1;
    public static final int TYPE_ADD = 2;
    /**
     * 活动状态
     **/
    public static final int ACTIVITY_CLOSE = 1;// 活动已关闭或未开启
    public static final int ACTIVITY_TO_BEGIN = 2;// 活动即将开启
    public static final int ACTIVITY_BEGIN = 3;// 活动开启中（begin-end）
    public static final int ACTIVITY_DISPLAY = 4;// 活动结束后的展示阶段（多用于领奖）

    public static final int SEND_MAIL = 2;

    public static final int RANK_1 = 1;// 达到排行活动,当晚19点可领奖{达到某个排行即可领奖,可领取多个}
    public static final int RANK_2 = 2;// 竞争排行活动,活动结束之后根据排行进行领取奖励{根据最终排名,领取对应排名奖励}
    public static final int RANK_3 = 3;    //排行类活动 最好一天19点可以领奖

    /**
     * 永久活动
     **/
    public static final int ACTIVITY_LONG = 9999;// 永久活动标识

    /**
     * 排名活动排名方式
     **/
    public static final int ASC = 0; // 小到大排序
    public static final int DESC = 1; // 大道小排序
    /**
     * 排行活动7点钟才开始进行排行
     ***/
    public static final int RANK_HOUR = 19;

    /*** 活动ID ***/
    public static final int ACT_LEVEL = 1;// 主城升级
    public static final int ACT_SCENE_CITY = 2;// 攻城略地
    public static final int ACT_INVEST = 3;// 成长计划
    public static final int ACT_HIGH_VIP = 4;// 大咖带队
    public static final int ACT_SOILDER_RANK = 5;// 兵力排行榜
    public static final int ACT_CITY_RANK = 6;// 城战排行
    public static final int ACT_TOPUP_RANK = 7;// 充值排行榜
    public static final int ACT_FORGE_RANK = 8;// 锻造排行榜
    public static final int ACT_COUNTRY_RANK = 9;// 国战排行榜
    public static final int ACT_OIL_RANK = 10;// 屯粮排行
    public static final int ACT_WASH_RANK = 11;// 11排行榜
    public static final int ACT_COLLECT_DOUBLE = 12;// 采集翻倍0.5
    public static final int ACT_COMBAT_EXP_DOUBLE = 13;// 副本经验翻倍精研排行
    public static final int ACT_REBEL_DROP = 14;// 叛军图纸掉落翻倍(图纸掉落翻倍)
    public static final int ACT_REBEL_RESOURCE = 15;// 叛军资源(资源掉落翻倍)
    public static final int ACT_COMBAT_DROP = 16;// 副本掉落
    public static final int ACT_KILL_EXP = 17;// 国器暴击的概率翻倍
    public static final int ACT_DESIGN_REDUCE = 18;// 图纸活动(据点生产时间减半)
    public static final int ACT_REBEL_SPEED = 19;// 叛军加速(科技,建筑,募兵加速5分钟)
    public static final int ACT_RECRUIT_SOILDER = 20;// 募兵加速
    public static final int ACT_WORK_SPEED = 21;// 作坊加速
    public static final int ACT_STONE_RANK = 22;// 晶体排行榜
    public static final int ACT_GROW_FOOT = 23;// 基金活动
    public static final int ACT_POWER = 24;// 体力赠送
    public static final int ACT_LEVEL_RANK = 25;// 等级排行榜

    public static final int ACT_STONE_DIAL = 50;// 镔铁转盘
    public static final int ACT_LOGIN_SEVEN = 51;// 七日登录
    public static final int ACT_HERO_KOWTOW = 52;// 七星拜将
    public static final int ACT_LOW_COUNTRY = 53;// 强国策
    public static final int ACT_SER_PAY = 54;// 全服返利
    public static final int ACT_PAY_FIRST = 55;// 首充礼包
    public static final int ACT_BUY_GIFT = 56;// 特价出售
    public static final int ACT_DIAL_LUCK = 57;// 幸运罗盘
    public static final int ACT_ZHENJI_ICON = 58;// 雪夜甄姬
    public static final int ACT_DAY_PAY = 59;// 每日返利  每日特惠
    public static final int ACT_REBEL_CRAZY = 60;// 叛军暴乱
    public static final int ACT_COST_PERSON = 61;// 消费有礼
    public static final int ACT_TOPUP_PERSON = 62;// 充值有礼(个人)
    public static final int ACT_TOPUP_SERVER = 63;// 充值有礼(全服版)
    public static final int Act_gold_hero = 64;// 金将现世
    public static final int ACT_PAY_GIFT = 65;// 特价礼包
    public static final int ACT_REBEL_MOVE_DROP = 66;// 叛军低级迁城掉落
    public static final int ACT_BUILD_RANK = 67;// 建设排行榜
    public static final int ACT_ONLINE_TIME = 68;// 在线有礼
    public static final int ACT_CONTINUOUS_RECHARGE = 69;// 每日充值
    public static final int ACT_GOVEN_CALL = 70;// 官员召唤
    public static final int ACT_IMPERIAL_MINE_COUNT = 71;// 皇城矿产翻倍
    public static final int ACT_MONTH_CARD = 72;// 月卡
    public static final int ACT_LOGIN_VIP = 73;// 登陆送VIP
    public static final int ACT_SEVEN = 74;// 七日狂欢
    public static final int ACT_ORANGE_DIAL = 75; // 橙装转盘
    public static final int ACT_PAY_ICE = 76; // 付费破冰礼包
    public static final int ACT_CRYSTAL_DIAL = 77; // 钻石晶体转盘
    public static final int ACT_PURPLE_DIAL = 78; // 紫装转盘
    public static final int ACT_SEVEN_RECHARGE = 79; // 七日充值
    public static final int ACT_FLASH_GIFT = 80; // 限时礼包
    public static final int ACT_DAILY_CHECKIN = 81; // 30日签到
    public static final int ACT_GOLD_DIAL = 82; // 钻石金币转盘
    public static final int ACT_WORLD_BATTLE = 83; // 世界征战
    public static final int ACT_MONTH_GIFT = 84; // 月卡大礼包
    public static final int ACT_ARMS_PAY = 85; // 军备促销
    public static final int ACT_HOPE = 86; // 许愿池
    public static final int ACT_WASH_EQUIP = 87; // 装备精研
    public static final int ACT_BUILD_QUE = 88; // 建造队列
    public static final int ACT_PASS_PORT = 89; // 通行证
    public static final int ACT_MENTOR_SCORE = 90; // 导师排行
    public static final int ACT_DAYLY_EXPEDITION = 91;    //每日远征
    public static final int ACT_KILL_ALL = 92;    //大杀四方
    public static final int ACT_ZERO_GIFT = 93; //零元礼包
    public static final int ACT_COST_GOLD = 94;    //消费排行
    public static final int ACT_DAILY_MISSION = 95;    //每日战役
    public static final int ACT_HERO_WASH = 96; //紧急特训
    public static final int ACT_DAYLY_RECHARGE = 97;    //每日充值
    public static final int ACT_HALF_WASH = 98; //特训半价
    public static final int ACT_JOURNEY_DOUBLE = 99;//远征翻倍
    public static final int ACT_SEARCH_SURPRISED = 100;//搜寻惊喜
    public static final int ACT_HONOR_DIAL = 101;//军功转盘
    public static final int ACT_DOUBLE_EGG = 102;//双旦活动
    public static final int ACT_DOUBLE_EGG_GIFT = 103;//双旦礼包活动
    public static final int ACT_WORLD_BOX = 104;    //世界宝箱
    public static final int ACT_LUXURY_GIFT = 105;  //豪华礼包
    public static final int ACT_NEW_YEAR_EGG = 106;//新年活动
    public static final int ACT_NEW_YEAR_GIFT = 107;//新年礼包活动
    public static final int ACT_LUCK_POOL = 108;  //幸运奖池
    public static final int ACT_SQUA = 109;//九宫格
    public static final int ACT_HERO_DIAL = 110; // 夺命魅影
    public static final int ACT_SPECIAL_GIFT = 111; //特价礼包
    public static final int ACT_ORDER = 112; // 获取订单
    public static final int ACT_SURIPRISE_GIFT = 113;//惊喜礼包
    public static final int ACT_RAIDERS = 114;//夺宝奇兵
    public static final int RE_DIAL = 115;//充值转盘
    //王牌球手
    public static final int ACT_TASK_HERO = 116;
    public static final int ACT_CAMP_MEMBERS = 117; // 阵营骨干
    public static final int ACT_SEARCH = 118; // 物资搜寻
    public static final int DAILY_TRAINRS = 119;   //日常训练
    public static final int LUCK_DIAL = 120;//好运转盘
    //勇冠三军
    public static final int ACT_WELL_CROWN_THREE_ARMY = 121;
    //奖章兑换
    public static final int ACT_MEDAL_EXCHANGE = 122;
    //累计充值
    public static final int ACT_GRAND_TOTAL = 123;
    //采集资源
    public static final int ACT_COLLECTION_RESOURCE = 125;
    //幸运砸蛋
    public static final int ACT_LUCKLY_EGG = 126;
    //剿灭虫族
    public static final int ACT_MONSTER = 127;
    //端午节活动
    public static final int ACT_DRAGON_BOAT = 128;
    //端午节活动
    public static final int ACT_DRAGON_BOAT_GIFT = 129;
    //美女礼包
    public static final int ACT_BEAUTY_GIFT = 130;
    //材料置换
    public static final int ACT_MATERIAL_SUBSTITUTION = 131;
    //春节活动
    public static final int ACT_SPRING_FESTIVAL = 132;
	// 助力母巢活动
	public static final int BLOOD_ACTIVITY = 133;
    //春节礼包
    public static final int ACT_SPRING_FESTIVAL_GIFT = 134;
    //塔防活动
    public static final int ACT_TD_SEVEN_TASK = 135;
}
