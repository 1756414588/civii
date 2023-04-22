package com.game.season.journey.entity;

public enum JourneyType {
    HERO(1, "获得赛季英雄"),

    UP_LEVEL(2, "升级"),

    RESEARCH_TECH(3, "XX军职达到十夫长"),

    COMP(4, "完成3次宏伟宝库任务"),

    SKILL_LEVEL(5, "XX技能升到2级"),
    
    AWARD(6, "领取1次宏伟宝库奖励"),;

    int val;
    String desc;

    JourneyType(int val, String desc) {
        this.val = val;
        this.desc = desc;
    }

    public int get() {
        return val;
    }
}
