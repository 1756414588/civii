package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class FortressHandler extends ClientHandler {
    @Override
    public void action() {
        getService(WorldService.class).queryFortressInfo(msg.getExtension(WorldPb.GetForTressRq.ext), this);
    }
}
