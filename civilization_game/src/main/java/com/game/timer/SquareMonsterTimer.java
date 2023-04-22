package com.game.timer;

import com.game.manager.CityManager;
import com.game.spring.SpringUtil;

// 禁卫军定时器
public class SquareMonsterTimer  extends TimerEvent {
    public SquareMonsterTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        SpringUtil.getBean(CityManager.class).attackFamous();
    }

}