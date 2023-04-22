package com.game.activity;

import com.game.activity.define.EventEnum;
import com.game.activity.facede.IActivityActor;
import com.game.activity.facede.IActivityEvent;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticTDTaskMgr;
import com.game.manager.ActivityManager;
import com.game.manager.PlayerManager;
import com.game.manager.TDTaskManager;
import com.game.service.ActivityService;
import com.game.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

/**
 * 活动事件处理基础类
 */
public abstract class BaseActivityEvent implements IActivityEvent {

    @Autowired
    protected StaticActivityMgr staticActivityMgr;
    @Autowired
    protected ActivityManager activityManager;
    @Autowired
    protected PlayerManager playerManager;
    @Autowired
    protected StaticLimitMgr staticLimitMgr;
    @Autowired
    protected ActivityService activityService;
    @Autowired
    protected TDTaskManager tdTaskManager;
    @Autowired
    protected StaticTDTaskMgr staticTDTaskMgr;
    @Autowired
    protected ActivityEventManager activityEventManager;
    /**
     * 监听活动事件
     *
     * @param event
     * @param activityId
     * @param fun
     */
    public void listenEvent(EventEnum event, int activityId, BiConsumer<EventEnum, IActivityActor> fun) {
        activityEventManager.addEvent(event, activityId, fun);
    }

    /**
     * 监听事件
     *
     * @param event
     * @param fun
     */
    public void listenEvent(EventEnum event, BiConsumer<EventEnum, IActivityActor> fun) {
        activityEventManager.addEvent(event, fun);
    }

}
