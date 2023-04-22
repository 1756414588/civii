package com.game.timer;

import com.game.service.WorldService;
import com.game.spring.SpringUtil;


/**
 * 世界地图刷怪[maptype = 1, 2]定时器
 **/
public class MonsterFlushTimer extends TimerEvent {
    public MonsterFlushTimer(long time) {
        super(-1, time);
    }

    @Override
    public void action() {
        SpringUtil.getBean(WorldService.class).flushWorldMonster();
    }

}
