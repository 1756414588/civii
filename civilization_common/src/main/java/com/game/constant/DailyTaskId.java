package com.game.constant;

/**
 * @author zcp
 * @date 2021/3/11 21:14
 * 诵我真名者,永不见bug
 */
public enum DailyTaskId {
    LOGIN(1, "每日登陆"),
    UP_BUILD(2, "升级建筑"),
    RESEARCH_TECH(3, "研究科技"),
    CLEARANCE(4, "通关战役"),
    KILL_MONSTER(5, "剿灭虫族"),
    APPOINTMENT(6, "秘书约会"),
    PLAY_GAME(7, "秘书游戏"),
    GIFTS(8, "秘书赠礼"),
    BUY_ENERGY(9, "购买体力"),
    EXPEDITION(10, "通关远征"),
    COMPOSE(11, "合成勋章"),
    TRAIN_SOLDIERS(12, "训练士兵"),
    IMPOSE(13, "征收资源"),
    BUILD_COUNTRY(14, "阵营建设"),
    CITY_WAR(15, "参与城战"),
    COUNTRY_WAR(16, "参与阵营战"),
    WASH_HERO(17, "英雄特训"),
    WASH_EQUIP(18, "装备精研"),
    MONTH_CARD(19, "月卡状态"),
    RECHARGE(20, "充值任意金额"),
    ;

    int val;
    String desc;

    DailyTaskId(int val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    public int get() {
        return val;
    }
}
