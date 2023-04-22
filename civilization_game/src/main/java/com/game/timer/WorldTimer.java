package com.game.timer;

import com.game.service.SuperResService;
import com.game.service.WorldActPlanService;
import com.game.service.WorldTargetTaskService;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * @Description TODO
 * @Date 2021/3/31 10:39
 **/
public class WorldTimer extends TimerEvent {

    public WorldTimer() {
        super(-1, TimeHelper.SECOND_MS);
    }

    @Override
    public void action() {
        SpringUtil.getBean(WorldTargetTaskService.class).timeWorldTarget();
        SpringUtil.getBean(WorldActPlanService.class).checkBroodWar();
        SpringUtil.getBean(SuperResService.class).timeSuperMine();
    }
}
