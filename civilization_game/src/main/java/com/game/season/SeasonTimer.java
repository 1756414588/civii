package com.game.season;

import com.game.spring.SpringUtil;
import com.game.timer.TimerEvent;

public class SeasonTimer extends TimerEvent {

    public SeasonTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        SpringUtil.getBean(SeasonManager.class).flushSeason();
    }
}
