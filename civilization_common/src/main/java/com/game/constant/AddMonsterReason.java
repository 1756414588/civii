package com.game.constant;


public interface AddMonsterReason {
    int FLUSH_INIT_MONSTER = 1;
    int FLUSH_ACT_MONSTER = 2;
    int FLUSH_TASK_MONSTER = 3;
    int LOAD_MONSTER = 4;
    int STAFF_MONSTER = 5;
    int COUNTRY_HERO = 6;
    int ADD_FORTRESS_TOP_MONSTER = 7;
    int ADD_FORTRESS_MONSTER = 8;
    int ADD_PLAYER_MONSTER = 9;
    int FLUSH_SPECIAL_MONSTER = 10;  // 第一阶段刷新黄巾军，或者西凉军
    int ADD_MEETING_TASK_MONSTER = 11;//添加指挥部任务刷怪

    int ADD_REBEL_MONSTER = 12;//使用密电刷怪

    int ADD_BIG_MONSTER = 13;   //增加活动怪物
}
