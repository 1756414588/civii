package com.game.constant;

public interface ChatId {
    int CITY_ATTACK = 101;        // 我的城池（%s,%s）被%s国的%s发起了城战，请求支援！ ok [区域, finished, 测试ok]
    int ATTACK_CITY = 102;        // 我对%s国的%s（%s,%s）发起了城战，请求支援！ok [区域, finshed, 测试ok]
    int COUNTRY_ATTACK = 105;     // %s国%s对我国的%s%s发起了国战。敌国来犯，我城势单力孤，还请诸位同胞申出援手 ok [区域, finshed,测试ok]
    int MAP_CITY_ATTACK = 106;    // 区域%s%s[%s]由我国%s率众成功占领 ok
    int COLLECTION_CHART = 107;   // %s从%s%s征收了%sX1到自己基地中。ok
    int COLLECTION_CHART_2 = 108; // %s从%s%s征收了%sX2到自己基地中，成功消耗%s！ok
    int GOVERN_CALL = 110;        // %s%s在%s对%s勇士发起了召唤
    int GOT_GOD_HERO = 111;       // %s通过神将寻访获得了神将%s。神将突破后将变得极其强力！ok
    int TECH_RANK = 112;          // %s成为全服第%s个研究%s的玩家！
    int COUNTRY_TASK = 113;       // 恭喜%s在完成国家任务时，获得了额外威望奖励。
    int EQUIP_RANK = 114;         // 恭喜%s成为全服第%s个打造%s的主公
    int ATTACK_COUNTRY = 115;     // 我国的%s对%s%s%s发起了国战。养兵千日，用兵一时，一鸣惊人正在今朝，请给位主公加入 ok [区域,测试1ok，测试2ok]
    int HERO_UP = 116;            // %s通过武将突破获得了%s ok
    int COUNTRY_LOST = 117;       // %s%s[%s]被敌国%s率众成功占领!
    int KING_ONLINE = 118;        // %s%s%s上线了！
    int COUNTRY_GLORY = 121;      // <font color=#ffffff>%s国齐心合力一起完成了国家%s荣誉目标，举国欢庆，大家一起去领取荣誉礼包吧！</font>
    int COUNTRY_ADDS = 122;       // <font color=#ffffff>%s%s修改了新的公告，请大家前往查看</font>
    int BUY_VIP_GIFTS = 123;      // 购买VIP礼包通告[VipChat]
    int GM_NOTICE = 124;          // gm发送的系统公告信息 ok
    int VIP_LEVEL = 125;          // 升级VIP等级
    int NPC_COUNTRY_WAR = 126;    // 近卫军发起国战:我国的%s对%s%s%s发起了国战。请各位指挥官加入！一起拿下它[点击进入国家战争参战]
    int VOTE_GOVERN = 127;          // 恭喜%s当选%s的%s
    int GOT_COUNTRY_HERO = 128;   // <font color=#ffffff>恭喜%s</font><font color=#fd4642>%s</font><font color=#ffffff>获得了国家名将</font><font color=#fd4642>%s</font>
    int FOUND_COUNTRY_HERO = 129; // <font color=#ffffff>%s的国家名将</font><font color=#fd4642>%s</font><font color=#ffffff>已出现在</font><font color=#ad55ff event=click>
    // [%s]</font><font color=#ffffff>各位指挥官快去把他招到麾下吧！</font>
    int RIOT_DEFENCE = 132;       // 暴乱参与驻防
    int FIRST_PAY = 130;          // 恭喜%s成功领取价值168元的首次充值礼包[VipChat]
    int PASS_MISSION = 133;       // 通关第八章副本的前三名玩家

    int HELP_REBLE_WAR = 134;     // 伏击叛军之战请求支援

    int SHARE_REBLE_WAR = 135;     // 伏击叛军之战请求支援

    /**
     * 挖到图纸
     */
    int DIG_PAPER = 142;
    /**
     * 开出荣耀图纸
     */
    int HOURO_PAPER = 143;
    /**
     * 许愿池跑马灯
     */
    int ACT_HOPE = 144;
    /**
     * 恭喜<color=#ce1aee>%s</color>的<color=#efd235>%s</color>在抢夺名城活动中，征收到珍贵的<color=#efd235>%s</color>！
     */
    int STEAL_CITY = 145;

    //英雄晋升
    int HERO_DIVINE = 146;

    //拍了拍           %s拍了拍%s
    int PAI_PAI = 147;

    //世界宝箱跑马灯
    int WORLD_BOX_CHAT = 154;

    int PRO_MILI = 155;//晋升军衔

    int SHARE_HERO = 157;   //分享英雄
    int SYSTEM_SHARE_HEROM = 158;   //系统分享英雄

    int MY_SOUND = 156;//魅影转盘

    int LUCK_POOL = 159;//幸运奖池

    int GET_BEAUTY = 161;//获得美女
    //巨型虫族
    int BIG_MONSTER_HELP = 162;
    //打造秘籍装备
    int DO_EQUIP_GOLD = 163;
    //打造秘籍装备
    int DO_EQUIP_RED = 164;
    //炮塔广播
    int CHAT_165 = 165;
    //任命跑马灯
    int CHAT_166 = 166;
    //占领母巢
    int CHAT_167 = 167;

    int SECOND_HERO_DIVINE = 169;
    // 虫族主宰活动开启
    int ZERG_MASTER_OPEN = 170;
    // 虫族主宰活动开始防守阶段
    int ZERG_MASTER_DEFEND = 171;
    // 虫族主宰活动胜利
    int ZERG_MASTER_WIN = 172;
    // 虫族主宰活动结束
    int ZERG_MASTER_END = 173;
    // 虫族主宰协防
    int ZERG_HELP_DEFENCE = 174;
    // 钓鱼记录分享
    int FISH_RECORD_SHARE = 175;
    // 钓鱼跑马灯
    int FISHING_NOTICE = 176;

	int FLAME_SHOP = 182;
	int FLAME_OCCUPY = 183;

	int FLAME_HELP_ATT = 184;
	int FLAME_HELP_DEF = 185;
}
