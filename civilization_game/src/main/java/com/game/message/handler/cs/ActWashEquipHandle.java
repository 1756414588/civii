package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @author liyue
 */
public class ActWashEquipHandle extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actWashEquip(this);
    }
}
