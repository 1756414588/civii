package com.game.activity.facede;

import com.game.activity.define.EventEnum;

/**
 * 活动事件接口
 */
public interface IActivityEvent {

    /**
     * 处理器监听事件
     */
    void listen();

    void process(EventEnum activityEnum, IActivityActor actor);

}
