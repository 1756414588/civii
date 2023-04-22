package com.game.timer;

import com.game.service.WorldPvpService;
import com.game.spring.SpringUtil;

// wolrd pvp time
public class WorldPvpTimer extends TimerEvent {
    public WorldPvpTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        SpringUtil.getBean(WorldPvpService.class).checkWar();
    }

}
