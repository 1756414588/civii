package com.game.timer;


// 战斗定时器
public class WarTimer extends TimerEvent {
    public WarTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
//        SpringUtil.getBean(WarManager.class).doTimerLogic();
    }
}
