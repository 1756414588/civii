package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PvpBattlePb;
import com.game.service.WorldPvpService;

public class MassHeroHandler extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        PvpBattlePb.MassActionRq req = msg.getExtension(PvpBattlePb.MassActionRq.ext);
        service.massHero(req, this);
    }
}