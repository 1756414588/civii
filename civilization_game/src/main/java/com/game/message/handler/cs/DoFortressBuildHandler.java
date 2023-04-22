package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class DoFortressBuildHandler extends ClientHandler {
    @Override
    public void action() {
        getService(WorldService.class).devFortressRq(msg.getExtension(WorldPb.GetForTressBuildRq.ext), this);
    }
}
