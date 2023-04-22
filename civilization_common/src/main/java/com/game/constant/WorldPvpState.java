package com.game.constant;

// 世界pvp战斗状态
public interface WorldPvpState {
    int INIT  = 0;  // 初始状态(战斗结束后清理战场)
    int START = 1;  // 战斗开始(每周五八点到时间)
    int END = 2;    // 战斗结束(时间到或者防守方坚持1个小时)
    int BANQUET =3; // 国宴状态

}
