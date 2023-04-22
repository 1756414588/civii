package com.game.timer;

import com.game.service.WorldService;
import com.game.spring.SpringUtil;

// 世界刷新定时器
public class FlushFortressTimer  extends TimerEvent {
    public FlushFortressTimer(long time) {

        super(-1, time);
    }

    @Override
    public void action() {
        SpringUtil.getBean(WorldService.class).flushWorldFortressObject();
    }

}

