package com.game.timer;

import com.game.service.PlayerService;
import com.game.spring.SpringUtil;

public class RestoreTimer extends TimerEvent {
    public RestoreTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        SpringUtil.getBean(PlayerService.class).restoreDataTimerLogic();
    }

}
