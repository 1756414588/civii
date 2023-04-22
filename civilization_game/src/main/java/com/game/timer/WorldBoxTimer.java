package com.game.timer;

import com.game.service.WorldBoxService;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 *
 * @date 2021/1/5 17:57
 * @description
 */
public class WorldBoxTimer extends TimerEvent {

    public WorldBoxTimer() {
        super(-1, TimeHelper.SECOND_MS);
    }

    @Override
    public void action() {
        SpringUtil.getBean(WorldBoxService.class).checkBoxOpen();
    }
}
