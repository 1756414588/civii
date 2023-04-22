package com.game.timer;


import com.game.service.PlayerService;
import com.game.spring.SpringUtil;

// 恢复洗练次数
public class WashTimeTimer extends TimerEvent {
    public WashTimeTimer () {
        super(-1, 1000);
    }

    @Override
    public void action() {
        SpringUtil.getBean(PlayerService.class).recoverWashTimes();
    }

}