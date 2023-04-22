package com.game.timer;

import com.game.service.BuildingService;
import com.game.spring.SpringUtil;

public class BuildingUpTimer extends TimerEvent {
    public BuildingUpTimer() {
        super(-1, 1);
    }

    @Override
    public void action() {
        SpringUtil.getBean(BuildingService.class).checkUpBuildings();
    }

}