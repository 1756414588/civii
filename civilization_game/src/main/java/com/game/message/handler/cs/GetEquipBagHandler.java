package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.EquipService;


public class GetEquipBagHandler extends ClientHandler {

    @Override
    public void action() {
        // TODO Auto-generated method stub
        EquipService euipService = getService(EquipService.class);
        euipService.getEquipBagRq(this);
    }
}
