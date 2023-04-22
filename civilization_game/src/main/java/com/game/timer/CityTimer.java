package com.game.timer;

import com.game.service.CityService;
import com.game.spring.SpringUtil;

// 城池定时器
public class CityTimer extends TimerEvent {
    public CityTimer() {
        super(-1, 1000L);
    }

    @Override
    public void action() {
        SpringUtil.getBean(CityService.class).checkCityLogic();
    }

}