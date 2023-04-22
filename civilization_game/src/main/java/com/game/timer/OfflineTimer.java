package com.game.timer;

import com.game.manager.PlayerManager;
import com.game.spring.SpringUtil;

/**
 * @author 陈奎
 * @version 1.0
 * @filename
 * @time 2017-5-20 下午6:25:00
 * @describe
 */
public class OfflineTimer extends TimerEvent {

    public OfflineTimer() {
        super(-1, 5000);
    }

    @Override
    public void action() {
        PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
        playerManager.offLine();
    }

}
