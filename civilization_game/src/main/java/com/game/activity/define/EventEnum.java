package com.game.activity.define;

/**
 * 活动事件定义
 */
public enum EventEnum {

    GET_ACTIVITY_AWARD_TIP(1, "领取奖励红点提示"),
    BUILD_LEVEL_UP(2, "建筑升级事件"),
    ACT_SERVER(3, "全服累积类活动"),
    PAY(4, "充值事件"),
    TRAINR(5, "训练"),
    KILL_PLAYER_SOIDLER(6, "击杀敌方士兵"),
    KILL_MONSTER(7, "击杀野怪"),
    CITY_WAR(8, "城战"),
    DROP_DRAWING(9, "掉落图纸"),
    SUB_GOLD(10, "钻石消耗"),
    UP_LEVEL(11, "升级"),
    MARKET_OPEN(15, "市场翻牌"),
    LEVY_RESOURCE(13, "资源征收"),
    JOURNEY_DONE(14, "通关远征"),
    MARKET_BUY(15, "市场购买"),
    MISSSION_DONE(16, "通关战役"),
    EQUIP_WASH(17, "装备精研"),
    HERO_WASH(18, "英雄特训"),
    EQUIP_DONE(19, "装备打造"),
    WORKS_PRODUCE(20, "材料工厂生产"),
    COUNTRY_BUILD(21, "阵营建设"),
    COUNTRY_WAR(22, "阵营战"),
    LOSE_SOLDIER(23, "损兵"),
    RIOT_WAR(24, "参与虫族入侵"),
    COLLECT(25, "采集资源事件"),
    TECH_UP(26, "科技升级"),
    GET_OIL(27, "获得粮食"),
    CITY_WAR_WIN(28, "城战胜利"),
    BUY_ENERGY(29, "体力购买"),
    BUY_JOURNEY(30, "远征次数购买"),
    COMPOUND_OMAMENT(31, "合成配饰"),
    GET_CARD_AWARD(32, "领取月卡/季卡奖励"),
    BREAUTY_SEEKING(33, "美女幽会"),
    DAILY_ACTIVITY(34, "日常"),
    DO_DIAL(35, "转转盘"),
    SYN_ACTIVITY_AND_DISAPPERAR(36, "同时推送两个协议:SynActivity,SynActivityDisappear"),
    EQUIP_ADD(37, "获得装备"),
    TECH_UP_FINISH(38, "科技升级完成"),
    BUILD_UP_FINISH(39, "建造完成升级"),
    CAPTURE_CITY(40, "攻克城市"),
    BUY_PAY_ARMS(41, "购买军备促销"),
    ACT_BUY_GIFT(42, "购买礼包"),
    SEVEN_PM_REWARD(43, "19点开启领奖"),
    TIME_DISAPPEAR(44, "活动到时间消失"),
    BUY_BROOD_BUFF(45, "母巢之战购买buf"),
    BUY_BROOD_DISPEAR(46, "母巢之战关联活动消失"),
    ;

    private int id;
    private String desc;

    private EventEnum(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
