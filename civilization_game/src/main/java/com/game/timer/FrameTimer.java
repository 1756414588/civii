package com.game.timer;

import com.game.manager.CommandSkinManager;
import com.game.manager.PersonalityManager;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 *
 * @date 2021/1/5 17:57
 * @description
 */
public class FrameTimer extends TimerEvent {

    public FrameTimer() {
        super(-1, TimeHelper.SECOND_MS);
    }

    @Override
    public void action() {
        SpringUtil.getBean(PersonalityManager.class).overdue();
        SpringUtil.getBean(CommandSkinManager.class).checkCommandSkin();
    }
}
