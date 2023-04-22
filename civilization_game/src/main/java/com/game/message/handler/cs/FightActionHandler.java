package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.PvpBattlePb;
import com.game.service.WorldPvpService;

public class FightActionHandler  extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        PvpBattlePb.FightActionRq req = msg.getExtension(PvpBattlePb.FightActionRq.ext);
        service.fightAction(req, this);
    }
}