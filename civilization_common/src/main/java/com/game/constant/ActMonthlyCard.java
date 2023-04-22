package com.game.constant;

import lombok.Getter;

public enum ActMonthlyCard {
    MONTHLY_CARD(1101, "月卡", 1),
    SEASON_CARD(1102, "季卡", 2),
    IRON_CARD(1103, "金币周卡", 3),
    COPPER_CARD(1104, "钢铁周卡", 3),
    OIL_CARD(1105, "食物周卡", 3),
    STONE_CARD(1106, "晶体周卡", 3),
    AUTO_CARD(1107, "自动杀虫", 4),
    ;

    int key;
    String name;
    @Getter
    int type;


    private ActMonthlyCard(int key, String name, int type) {
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ActMonthlyCard{" +
                "val=" + key +
                ", name='" + name + '\'' +
                '}';
    }
}
