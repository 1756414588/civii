package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class GetResInfoHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetResInfoRq req = msg.getExtension(WorldPb.GetResInfoRq.ext);
        service.getResInfo(req, this);
    }
}
