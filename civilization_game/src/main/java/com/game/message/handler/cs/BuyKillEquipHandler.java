package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.KillEquipPb;
import com.game.service.KillEquipService;

public class BuyKillEquipHandler extends ClientHandler {
    @Override
    public void action () {
        KillEquipService service = getService(KillEquipService.class);
        KillEquipPb.BuyKillRq req = msg.getExtension(KillEquipPb.BuyKillRq.ext);

        service.buyKillEquip(req, this);
    }
}