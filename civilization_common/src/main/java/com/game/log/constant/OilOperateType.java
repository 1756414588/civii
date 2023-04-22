package com.game.log.constant;

/**
 * 食物资源产出和消耗类型
 */
public enum OilOperateType {

    RES_BUILDING_COLLECT_IN(1, "资源建筑征收产出资源"),

    WORLD_COLLECT_IN(2, "采集世界资源产出资源"),

    SHOP_BUY_IN(3, "商店购买产出资源"),

    RES_MISSION_IN(4, "资源副本产出资源"),

    TASK_AWARD_IN(5, "任务奖励产出资源"),

    ACT_AWARD_IN(6, "活动奖励产出资源"),

    SHOP_EXCHANGE_IN(7, "市场兑换产出资源"),
    CITY_SMALL_GAME(10, "小游戏"),
    DRILL_IN(11, "训练士兵消耗资源"),



    DRILL_OUT(1, "训练士兵消耗资源"),

    MARCH_OUT(2, "部队行军消耗资源"),

    SHOP_EXCHANGE_OUT(3, "市场兑换消耗资源"),

    SHOP_PACK_OUT(4, "市场打包消耗资源"),
    ROB_OUT(11,"城战被抢夺"),
    ;



    private int infoType;

    private String desc;

    OilOperateType(int infoType, String desc) {

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
