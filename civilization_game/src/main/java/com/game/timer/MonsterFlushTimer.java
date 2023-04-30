package com.game.timer;

import com.game.dataMgr.StaticLimitMgr;
import com.game.service.WorldService;
import com.game.spring.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 世界地图刷怪[maptype = 1, 2]定时器
 **/
@Component
public class MonsterFlushTimer extends TimerEvent {

    @Autowired
    StaticLimitMgr staticLimitMgr;

    @Autowired
    WorldService worldService;

    public MonsterFlushTimer() {
        super(-1, 5 * 1000);
    }

    @Override
    public void action() {
        int flush = staticLimitMgr.getNum(7) * 1000;
        worldService.flushWorldMonster();
    }

}
