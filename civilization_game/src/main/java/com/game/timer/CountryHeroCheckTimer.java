package com.game.timer;

import com.game.service.CountryService;
import com.game.spring.SpringUtil;

// 国家英雄定时器
public class CountryHeroCheckTimer extends TimerEvent {
    public CountryHeroCheckTimer() {
        super(-1, 5000);
    }
    @Override
    public void action() {
        SpringUtil.getBean(CountryService.class).checkCountryHero();
    }

}