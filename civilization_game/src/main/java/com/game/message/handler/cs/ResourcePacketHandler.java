package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.DepotPb;
import com.game.service.DepotService;

/**
 *
 * @date 2020/1/13 14:47
 * @description
 */
public class ResourcePacketHandler extends ClientHandler {
    @Override
    public void action() {
        DepotService service = getService(DepotService.class);
        DepotPb.ResourcePacketRq req = msg.getExtension(DepotPb.ResourcePacketRq.ext);
        service.resourcePacket(this, req);
    }
}
