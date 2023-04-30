package com.game.constant;

/**
 *
 * @date 2020/5/14 15:18
 * @description 世界地图开启规则 4 个阶段对应的世界进程id
 */
public enum MapOpenByTargetEnum {

    /**
     * 0阶段表示世界地图没开启 -1 小雨3的都做-1处理 未开启
     */
    STEP_ZERO(-1),


    /**
     * 一阶段
     */
    STEP_ONE(1),

    /**
     * 二阶段
     */
    STEP_TWO(6),

    /**
     * 三阶段
     */
    STEP_THREE(7),

    /**
     * 四阶段
     */
    STEP_FOUR(10),

    ;


    private int worldTargetId;


    MapOpenByTargetEnum(int worldTargetId) {

        this.worldTargetId = worldTargetId;
    }


    public int getWorldTargetId() {
        return worldTargetId;
    }

    public static MapOpenByTargetEnum get(int worldTargetId) {
        if (worldTargetId >= MapOpenByTargetEnum.STEP_ONE.getWorldTargetId() && worldTargetId < MapOpenByTargetEnum.STEP_TWO.getWorldTargetId()) {
            return MapOpenByTargetEnum.STEP_ONE;
        } else if (worldTargetId >= MapOpenByTargetEnum.STEP_TWO.getWorldTargetId() && worldTargetId < MapOpenByTargetEnum.STEP_THREE.getWorldTargetId()) {
            return MapOpenByTargetEnum.STEP_TWO;

        } else if (worldTargetId >= MapOpenByTargetEnum.STEP_THREE.getWorldTargetId() && worldTargetId < MapOpenByTargetEnum.STEP_FOUR.getWorldTargetId()) {
            return MapOpenByTargetEnum.STEP_THREE;
        } else if (worldTargetId >= MapOpenByTargetEnum.STEP_FOUR.getWorldTargetId()) {
            return MapOpenByTargetEnum.STEP_FOUR;
        } else {
            return MapOpenByTargetEnum.STEP_ZERO;
        }

    }


    public static MapOpenByTargetEnum getMapOpenByTargetEnum(int worldTargetId) {
        for (MapOpenByTargetEnum mapOpenByTargetEnum : MapOpenByTargetEnum.values())
            if (mapOpenByTargetEnum.getWorldTargetId() == worldTargetId) {
                return mapOpenByTargetEnum;
            }
        return null;
    }
}
