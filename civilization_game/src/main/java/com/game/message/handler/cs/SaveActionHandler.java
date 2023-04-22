package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PvpBattlePb;
import com.game.service.WorldPvpService;

public class SaveActionHandler  extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        PvpBattlePb.SaveActionRq req = msg.getExtension(PvpBattlePb.SaveActionRq.ext);
        service.saveAction(req, this);
    }
}