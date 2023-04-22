package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.DepotService;

/**
 * @author jyb
 * @date 2020/1/13 15:50
 * @description
 */
public class GetResourcePacketHandler extends ClientHandler {
    @Override
    public void action() {
        getService(DepotService.class).getResourcePacket(this);
    }
}
