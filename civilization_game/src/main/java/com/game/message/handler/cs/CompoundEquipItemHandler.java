package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.service.EquipService;

public class CompoundEquipItemHandler extends ClientHandler {
    @Override
    public void action() {
        // TODO Auto-generated method stub
        EquipService euipService = getService(EquipService.class);
        EquipPb.CompoundEquipRq req = msg.getExtension(EquipPb.CompoundEquipRq.ext);
        euipService.compoundEquipItem(req, this);
    }
}