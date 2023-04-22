package com.game.timer;

import com.game.service.RoitService;
import com.game.spring.SpringUtil;

/**
 * 虫族入侵
 */
public class RoitTimer extends TimerEvent {
    public RoitTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        SpringUtil.getBean(RoitService.class).checkAct();
    }

}

