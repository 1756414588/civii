package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PvpBattlePb;
import com.game.service.WorldPvpService;

public class GoActionHandler  extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        PvpBattlePb.GoActionRq req = msg.getExtension(PvpBattlePb.GoActionRq.ext);
        service.goAction(req, this);
    }
}