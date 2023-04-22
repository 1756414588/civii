package com.game.constant;

/**
 *
 * @date 2021/1/5 17:48
 * @description
 */
public enum WorldBoxTask {
    KILL_MONSTER(1),  //消灭虫族
    BUILD_COUNTRY(2),//阵营建设
    CITY_FIGHT(3),//城战
    COUNTRY_FIGHT(4),   //阵营战
    DO_RECHARGE(5), //充值
    CAMP_SYNERGY(6),  //阵营协同
    WORLD_TASK(7),  //世界进程
    ;

    int val;

    WorldBoxTask(int val) {
        this.val = val;
    }

    public int getVal() {
        return this.val;
    }
}
