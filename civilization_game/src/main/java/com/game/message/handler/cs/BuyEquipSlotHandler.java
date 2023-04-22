package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.service.EquipService;

public class BuyEquipSlotHandler extends ClientHandler {

    @Override
    public void action() {
        // TODO Auto-generated method stub
        EquipService euipService = getService(EquipService.class);
        EquipPb.BuyEquipSlotRq req = msg.getExtension(EquipPb.BuyEquipSlotRq.ext);
        euipService.buyEquipSlotRq(req, this);
    }
}