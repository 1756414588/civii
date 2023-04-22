package com.game.timer;

import com.game.service.WorldService;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/** 世界地图刷初级/中心城区资源定时器 **/
public class ResouceFlushTimer extends TimerEvent {
    public ResouceFlushTimer() {
        super(-1, 1000L);
        setEnd(System.currentTimeMillis()  + TimeHelper.MINUTE_MS * 2);
    }

    @Override
    public void action() {
        SpringUtil.getBean(WorldService.class).flushWorldResource();
    }

}
