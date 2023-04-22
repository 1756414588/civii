package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.SoldierService;

public class GetSoldierHandler extends ClientHandler {
    @Override
    public void action() {
        // TODO Auto-generated method stub
        SoldierService soldierService = getService(SoldierService.class);
        soldierService.getSoldierRq(this);
    }
}