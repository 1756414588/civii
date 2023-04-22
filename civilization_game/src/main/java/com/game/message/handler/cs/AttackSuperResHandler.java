package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.SuperResService;

public class AttackSuperResHandler extends ClientHandler {
    @Override
    public void action() {
        getService(SuperResService.class).attackResRq(msg.getExtension(WorldPb.AttkerSuperResRq.ext), this);
    }
}
