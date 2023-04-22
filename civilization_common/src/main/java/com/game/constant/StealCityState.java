package com.game.constant;

public interface StealCityState {
    int INIT  = 0;   // 初始状态(活动结束后清理活动状态信息)
    int START = 1;   // 活动开始(有三个阶段)
    int END   = 2;   // 活动结束(最后一个活动结束)
}
