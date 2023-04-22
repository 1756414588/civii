package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.message.handler.DealType;
import com.game.pb.WorldPb;
import com.game.server.GameServer;
import com.game.service.WorldService;

public class MapMoveHandler extends ClientHandler {
    @Override
    public void action() {
        GameServer.getInstance().mainLogicServer.addCommand(() -> {
            WorldService service = getService(WorldService.class);
            WorldPb.MapMoveRq req = msg.getExtension(WorldPb.MapMoveRq.ext);
            service.mapMoveRq(req, this);
        }, DealType.MAIN);
    }
}