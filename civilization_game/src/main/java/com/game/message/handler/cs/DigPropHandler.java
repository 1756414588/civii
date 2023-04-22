package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PvpBattlePb;
import com.game.service.WorldPvpService;

public class DigPropHandler   extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        PvpBattlePb.DigPropRq req = msg.getExtension(PvpBattlePb.DigPropRq.ext);
        service.digProp(req, this);
    }
}
