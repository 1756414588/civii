package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.service.EquipService;

public class HireBlackSmithHandler extends ClientHandler {
    @Override
    public void action() {
        // TODO Auto-generated method stub
        EquipService euipService = getService(EquipService.class);
        EquipPb.HireBlackSmithRq req = msg.getExtension(EquipPb.HireBlackSmithRq.ext);
        euipService.hireBlackSmith(req, this);
    }
}