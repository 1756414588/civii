package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class UpdateFortressNameHandler extends ClientHandler {
    @Override
    public void action() {
        getService(WorldService.class).updateFortressName(msg.getExtension(WorldPb.GetForTressUpNameRq.ext), this);
    }
}
