package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.service.EquipService;

public class WashHeroEquipHandler extends ClientHandler {

    @Override
    public void action() {
        // TODO Auto-generated method stub
        EquipService euipService = getService(EquipService.class);
        EquipPb.WashHeroEquipRq req = msg.getExtension(EquipPb.WashHeroEquipRq.ext);
        euipService.washHeroEquipRq(req, this);
    }
}