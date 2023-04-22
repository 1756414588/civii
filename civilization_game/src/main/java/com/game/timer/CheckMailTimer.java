package com.game.timer;

import com.game.manager.PlayerManager;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * @Description TODO
 * @Date 2021/3/31 10:39
 **/
public class CheckMailTimer extends TimerEvent {

    public CheckMailTimer() {
        super(-1, TimeHelper.MINUTE_MS);
    }

    @Override
    public void action() {
        SpringUtil.getBean(PlayerManager.class).getPlayers().forEach((lordId, player) -> {
            player.getMailIds().clear();
        });
    }
}
