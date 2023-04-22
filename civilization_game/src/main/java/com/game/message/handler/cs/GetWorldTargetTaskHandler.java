package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldTargetTaskService;

/**
 * @author jyb
 * @date 2019/12/24 14:22
 * @description
 */
public class GetWorldTargetTaskHandler extends ClientHandler {
    @Override
    public void action() {
        getService(WorldTargetTaskService.class).GetWorldTargetTask(this);
    }
}
