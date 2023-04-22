package com.game.log.constant;

/**
 *
 * 晶体资源产出和消耗类型
 */
public enum StoneOperateType {

    RES_BUILDING_COLLECT_IN(1, "资源建筑征收产出资源"),

    WORLD_COLLECT_IN(2, "采集世界资源产出资源"),

    SHOP_BUY_IN(3, "商店购买产出资源"),

    RES_MISSION_IN(4, "资源副本产出资源"),

    TASK_AWARD_IN(5, "任务奖励产出资源"),

    ACT_AWARD_IN(6, "活动奖励产出资源"),
    CITY_SMALL_GAME(10, "小游戏"),


    UP_KILL_EQUIP_OUT(1, "神器升级消耗资源");



    private int infoType;

    private String desc;

    StoneOperateType(int infoType, String desc) {

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
