package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class KillWorldBossHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.KillWorldBossRq req = msg.getExtension(WorldPb.KillWorldBossRq.ext);
        service.killWorldBoss(req, this);
    }
}
