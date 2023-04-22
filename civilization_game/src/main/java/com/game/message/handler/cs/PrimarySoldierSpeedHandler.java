package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.SoldierPb;
import com.game.service.SoldierService;


public class PrimarySoldierSpeedHandler extends ClientHandler {
    @Override
    public void action () {
        SoldierService service = getService(SoldierService.class);
        SoldierPb.PrimarySoldierSpeedRq req = msg.getExtension(SoldierPb.PrimarySoldierSpeedRq.ext);
        service.primarySoldierSpeed(req, this);
    }
}