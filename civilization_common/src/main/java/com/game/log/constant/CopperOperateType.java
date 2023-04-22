package com.game.log.constant;

/**
 * 钢铁资源产出和消耗类型
 */
public enum CopperOperateType {

    RES_BUILDING_COLLECT_IN(1, "资源建筑征收产出资源"),

    WORLD_MOSTER_IN(2, "攻打世界怪物产出资源"),

    WORLD_COLLECT_IN(3, "采集世界资源产出资源"),

    SHOP_BUY_IN(4, "商店购买产出资源"),

    RES_MISSION_IN(5, "资源副本产出资源"),

    TASK_AWARD_IN(6, "任务奖励产出资源"),

    ACT_AWARD_IN(7, "活动奖励产出资源"),

    SHOP_EXCHANGE_IN(8, "市场兑换产出资源"),
    CITY_SMALL_GAME(10, "小游戏"),


    BUILDING_UP_OUT(1, "建筑升级消耗资源"),

    TEC_RESEARCH_OUT(2, "科技研究消耗资源"),

    WORKSHOP_MAKE_OUT(3, "作坊生产消耗资源"),

    SHOP_EXCHANGE_OUT(4, "市场兑换消耗资源"),

    SHOP_PACK_OUT(5, "市场打包消耗资源"),
    ROB_OUT(11,"城战被抢夺"),
    ;


    private int infoType;

    private String desc;

    CopperOperateType(int infoType, String desc) {

        this.infoType = infoType;
        this.desc = desc;
    }

    public int getInfoType() {
        return infoType;
    }

    public void setInfoType(int infoType) {
        this.infoType = infoType;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
