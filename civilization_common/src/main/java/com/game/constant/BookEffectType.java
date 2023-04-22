package com.game.constant;

/**
 * @author CaoBing
 * @date 2020/12/29 16:20
 */
public interface BookEffectType {
    int SOLDIER_TYPE_111 = 111;//(穿戴skillId = 111)突击兵
    int SOLDIER_TYPE_112 = 112;//(穿戴skillId = 112)陆战队
    int SOLDIER_TYPE_113 = 113;//(穿戴skillId = 113)侦察兵
    int SOLDIER_TYPE_114 = 114;//(穿戴skillId = 114)幻影坦克
    int SOLDIER_TYPE_115 = 115;//(穿戴skillId = 115)天启坦克
    int SOLDIER_TYPE_116 = 116;//(穿戴skillId = 116)光棱坦克
    int SOLDIER_TYPE_117 = 117;//(穿戴skillId = 117)火箭炮
    int SOLDIER_TYPE_118 = 118;//(穿戴skillId = 118)反坦克炮
    int SOLDIER_TYPE_119 = 119;//(穿戴skillId = 119)高射炮

    /**
     * 攻击((测试通过))
     */
    int ATTCK = 1;

    /**
     * 防御((测试通过))
     */
    int DEFENCE = 2;

    /**
     * 兵力((测试通过))
     */
    int SOLDIER_NUM = 3;

    /**
     * 强攻((测试通过))
     */
    int STRONG_ATTACK = 4;

    /**
     * 强防((测试通过))
     */
    int STRONG_DEFENCE = 5;

    /**
     * 攻城((测试通过))
     */
    int ATTACK_CITY = 6;

    /**
     * 守城((测试通过))
     */
    int DEFENCE_CITY = 7;

    /**
     * 暴击((测试通过))
     */
    int CRITI = 8;

    /**
     * 闪避((测试通过))
     */
    int MISS = 9;

    /**
     * 每少一排兵增加的百分比伤害(OK)
     */
    int BOOK_EFFECT_10 = 10;

    /**
     * 攻城属性在守城时也生效
     */
    int BOOK_EFFECT_11 = 11;

    /**
     * 战斗中收到炮兵兵种伤害降低千分比
     */
    int BOOK_EFFECT_12 = 12;

    /**
     * 战斗中每排兵出场对幻影坦克的首次伤害必定暴击
     */
    int BOOK_EFFECT_13 = 13;

    /**
     * 战斗中对步兵兵种伤害增加千分比
     */
    int BOOK_EFFECT_14 = 14;

    /**
     * 战斗中每击杀一排兵，伤害减免增加千分比(OK)
     */
    int BOOK_EFFECT_15 = 15;

    /**
     * 战斗中对反坦克炮暴击增加30%
     */
    int BOOK_EFFECT_16 = 16;

    /**
     * 战斗中每多一排兵待命，伤害增加千分比(OK)
     */
    int BOOK_EFFECT_17 = 17;

    /**
     * 战斗中对坦克兵种伤害增加千分比
     */
    int BOOK_EFFECT_18 = 18;

    /**
     * 战斗中对陆战队伤害增加千分比
     */
    int BOOK_EFFECT_19 = 19;

    int SPEED_UP_COLLECT = 20; //采集增加50%(测试通过)
    int COUNTRY_WAR = 21;    //参与阵营战并且有杀敌时可获得100k军功，每30分钟触发一次(测试通过)
    int KILL_MONSTER_ALONE = 22;//若英雄单独击杀虫族，则额外获得此次击杀虫族的资源千分比(测试通过)
    int REVIVE_HERO_ROYAL_CITY = 23;//英雄可在母巢之战中免费复活一次 (saveAction)(测试通过)
    int GET_HERO_EXP = 24;//英雄每天获得xx点体力扫荡副本获得的经验(测试通过)
    int SPEED_UP_MARCH = 25;//	部队在世界地图上的行军速度增加50%，出征部队中不全拥有此特技的只增加20%(测试通过)
    int SOLDIER_REC = 26; // 英雄兵力损失时,在兵营中额外增加20%伤兵恢复,可与部队重建道具叠加
}
