package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;


public class BuyEnergyHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService playerService = getService(PlayerService.class);
        RolePb.BuyEnergyRq req = msg.getExtension(RolePb.BuyEnergyRq.ext);
        playerService.buyEnergyRq(req, this);
    }
}
