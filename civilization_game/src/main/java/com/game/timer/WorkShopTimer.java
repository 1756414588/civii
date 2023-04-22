package com.game.timer;

import com.game.service.WorkShopService;
import com.game.spring.SpringUtil;


public class WorkShopTimer  extends TimerEvent {
    public WorkShopTimer() {
        super(-1, 1000);
    }

    @Override
    public void action() {
        SpringUtil.getBean(WorkShopService.class).checkWsQue();
    }

}