package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PvpBattlePb;
import com.game.service.WorldPvpService;

public class SoloActionHandler extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        PvpBattlePb.SoloActionRq req = msg.getExtension(PvpBattlePb.SoloActionRq.ext);
        service.soloAction(req, this);
    }
}