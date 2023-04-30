package com.game.constant;

/**
 *
 * @date 2019/12/12 14:03
 * @description
 */
public interface CastleConsts {
    /**
     * 采集部队
     */
    int MINING = 1;

    /**
     * 城防部队
     */
    int DEFENSEARMY = 2;

    /**
     * 采集部队开启[参谋部1级, 指挥官等级]
     */
    int CONDITION_176 = 176;

    /**
     * 城防部队开启[参谋部2级, 指挥官等级]
     */
    int CONDITION_177 = 177;


    /**
     * 天策府开启[参谋部3级, 指挥官等级]
     */
    int CONDITION_178 = 178;

    /**
     * 采集部队坑位开启配置,指挥官等级
     */
    int CONDITION_187 = 187;

    /**
     * 城防部队坑位开启配置，指挥官等级
     */
    int CONDITION_188 = 188;
    /**
     * 参谋部开启条件
     */
    int CONDITION_1023 = 1023;

    /**
     * 玩家周围24个格子刷指挥部的任务怪
     */
    int MEETING_MONSTER_CELL_NUM = 2;

    /**
     * 城防军多久刷新一次兵  60s一次
     */
    int REFRESH_SOLDIER_TIME = 60 * 1000;

    /**
     * 城防军每一次恢复最大兵力的5%
     */
    double REFRESH_SOLDIER_PERCENT = 0.05;
}
