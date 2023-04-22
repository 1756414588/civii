package com.game.constant;

/**
 * @author jyb
 * @date 2019/12/25 11:13
 * @description
 */
public interface TaskState {
    int DOING = 0; //进行中
    int SUCCESS =1; //已完成，未领奖
    int AWARD = 2;  //已经领奖
}
