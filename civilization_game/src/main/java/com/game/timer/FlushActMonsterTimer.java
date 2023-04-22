package com.game.timer;

// 活动怪刷新定时器
public class FlushActMonsterTimer extends TimerEvent {
    public FlushActMonsterTimer() {
        super(-1, 3600000L);
    }

    @Override
    public void action() {
       // SpringUtil.getBean(WorldService.class).flushActMonster();
    }

}