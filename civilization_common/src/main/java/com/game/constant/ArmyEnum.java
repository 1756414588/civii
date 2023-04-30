package com.game.constant;

/**
 *
 * @date 2019/12/16 10:52
 * @description
 */
public enum ArmyEnum {

    /**
     * 上阵部队
     */
    ARMY_ONE(1),

    /**
     * 采集部队
     */
    ARMY_TWO(2),

    /**
     * 城防部队
     */
    ARMY_THREE(3),
    ;


    private int type;

    ArmyEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
