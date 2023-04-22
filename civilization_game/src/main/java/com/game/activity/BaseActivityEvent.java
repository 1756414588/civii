package com.game.activity;

import com.game.activity.define.EventEnum;
import com.game.activity.facede.IActivityActor;
import com.game.activity.facede.IActivityEvent;

import java.util.function.BiConsumer;

/**
 * 活动事件处理基础类
 */
public abstract class BaseActivityEvent implements IActivityEvent {

    @Override
    public void register() {
        this.listen();
    }

    /**
     * 监听活动事件
     *
     * @param event
     * @param activityId
     * @param fun
     */
    public void listenEvent(EventEnum event, int activityId, BiConsumer<EventEnum, IActivityActor> fun) {
        ActivityEventManager.getInst().addEvent(event, activityId, fun);
    }

    /**
     * 监听事件
     *
     * @param event
     * @param fun
     */
    public void listenEvent(EventEnum event, BiConsumer<EventEnum, IActivityActor> fun) {
        ActivityEventManager.getInst().addEvent(event, fun);
    }

}
