package com.game.activity.facede;

import com.game.activity.define.EventEnum;

/**
 * 活动事件接口
 */
public interface IActivityEvent {

    /**
     * 注册到容器
     */
    public void register();

    /**
     * 处理器监听事件
     */
    public void listen();

    // 处理器通用方法
    public void process(EventEnum activityEnum, IActivityActor actor);

}
