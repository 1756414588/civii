package com.game.constant;

/**
 *
 * @date 2019/12/13 14:10
 * @description
 */
public interface MeetingTaskStateConsts {
    /**
     *   0 未开启任务
     */
    int  NOT_OPEN = 0;
    /**
     * 1 开启任务 ，任务中
     */
    int OPEN  =1;


    int NEXT_STEP_OPEN =2;

    /**
     *  2 已完成 未激活
     */
    int SUCCESS_NO_OPEN = 3;


    /**
     * 3 已完成 激活
     */
    int SUCCESS_OPEN = 4;


}
