package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldTargetTaskService;

/**
 *
 * @date 2019/12/26 14:49
 * @description
 */
@Deprecated
public class AttackWorldBossHandler extends ClientHandler {
    @Override
    public void action() {
            //getService(WorldTargetTaskService.class).attackWorldBoss(this);
    }
}
