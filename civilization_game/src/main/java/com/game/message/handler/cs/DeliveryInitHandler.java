package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldService;

/**
 * @author cpz
 * @date 2020/9/4 9:31
 * @description
 */
public class DeliveryInitHandler extends ClientHandler {
    @Override
    public void action() {
        getService(WorldService.class).deliveryInit(this);
    }
}
