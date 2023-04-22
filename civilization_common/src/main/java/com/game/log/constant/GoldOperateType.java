package com.game.log.constant;


/**
 * 钻石资源产出和消耗类型
 */
public enum GoldOperateType {

    TASK_AWARD_IN(1, "任务奖励产出资源"),//ok

    MAIL_IN(2, "邮件奖励产出资源"),//ok

    PROP_IN(3, "道具使用产出资源"),//ok

    WORLD_COLLECT_IN(4, "采集世界资源产出资源"),//ok

    SHOP_FLIP_IN(5, "市场翻牌产出数量"),//ok

    ACT_AWARD_IN(6, "活动奖励产出资源"),//ok

    RECHARGE_IN(7, "充值产出数量"),//ok

    ACT_ADD_AWARD_IN(8, "领取活动奖励产出资源"),//ok

    WORLD_PROCESS_IN(9, "世界进程产出数量"),//ok


    BARRACKS_EXPANSION_OUT(1, "兵营扩建消耗钻石数量"),//ok

    PACK_EXPANSION_OUT(2, "背包扩容消耗钻石数量"),//ok

    SPECIALLY_TRAINED_OUT(3, "英雄特训消耗钻石数量"),//ok

    ELITE_CARDS_OUT(4, "精英英雄抽卡消耗钻石数量"),//ok

    EPIC_CARDS_OUT(5, "史诗英雄抽卡消耗钻石数量"),//ok

    SPEED_TRAIN_OUT(6, "加速训练士兵消耗钻石数量"),//ok

    SPY_ON_OUT(7, "侦查消耗钻石数量"),//ok

    SHOP_BUY_OUT(8, "商店购买消耗资源"),//ok

    GEAR_LAP_OUT(9, "装备精研消耗钻石数量"),//ok

    EQUIPMENT_DRAW_OUT(10, "装备图纸副本消耗钻石数量"),//ok

    MARKET_BUY_OUT(11, "市场购买道具消耗钻石数量"),//ok

    SHOP_PACK_OUT(12, "市场打包消耗资源"),//ok

    CAMP_ELECTION_OUT(13, "拉票消耗钻石数量"),//ok

    KILL_EQUIP_OUT(14, "神器碎片购买消耗钻石数量"),//ok

    UP_DEFENSE_OUT(15, "城防军升级消耗钻石数量"),//ok

    CD_DEFENSE_OUT(16, "城防军秒CD消耗钻石数量"),//ok

    ORDNANCE_EMPLOYMENT_OUT(17, "军械厂雇佣消耗钻石数量"),//ok

    COMMAND_CENTER_EMPLOYMENT_OUT(18, "指挥中心雇佣消耗钻石数量"),//ok

    TECHNOLOGY_EMPLOYMENT_OUT(19, "科技学院雇佣消耗钻石数量"),//ok

    PERSONAL_EMAIL_OUT(20, "个人邮件消耗钻石数量"),//ok

    BUY_QUEUE_OUT(21, "购买建造队列消耗钻石数量"),//ok

    APPOINTED_OFFICIALS_OUT(22, "任命官员消耗钻石数量"),//ok

    RES_MISSION_BUY_OUT(23, "资源副本购买消耗钻石数量"),//ok

    LUCK_DIAL_OUT(24, "幸运转盘抽奖消耗钻石数量"),//ok

    BUY_PHYSICAL_OUT(25, "体力购买消耗钻石数量"),//ok

    BUY_VIP_OUT(26, "VIP礼包购买消耗钻石数量"),//ok

    SPEED_BUILD_OUT(27, "建筑升级加速消耗钻石数量"),//ok

    SPEED_UP_TECHNOLOGY_OUT(28, "科技升级加速消耗钻石数量"),//ok

    SPEED_BUILDING_EQUIPMENT_OUT(29, "打造装备加速消耗钻石数量"),//ok

    SECRETS_LAP_OUT(30, "装备秘技精研消耗钻石数量"),//ok

    MATERIAL_EXPANSION_OUT(31, "材料工厂扩建消耗钻石数量"),//ok

    HERO_REFRESH_OUT(32, "绝版英雄刷新消耗钻石数量"),//ok

    BUY_HERO_OUT(33, "绝版英雄道具购买消耗钻石数量"),//0k

    PURP_DIAL_OUT(34, "紫装转盘抽奖消耗钻石数量"),//ok

    BUY_WORM_TIME_OUT(35, "购买打通虫穴次数消耗钻石数量"),//ok

    BROOD_WAR_OUT(36, "母巢之战操作消耗钻石数量"),//ok

    PURCHASE_RHETORIC_OUT(37, "买酒套话消耗钻石数量"),//ok

    OTHER_OUT(38, "其他活动消耗钻石数量"),//0k

    HOPE_OUT(100, "许愿池消耗"),//0k
    HOPE_IN(101, "许愿池产出"),
    ;//0k

    private int infoType;

    private String desc;

    GoldOperateType(int infoType, String desc) {

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
