package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldTargetTaskService;

/**
 *
 * @date 2019/12/26 11:28
 * @description
 */
public class GetWorldBossInfoHandler extends ClientHandler {
    @Override
    public void action() {
        getService(WorldTargetTaskService.class).getWorldBossInfo(this);
    }
}
