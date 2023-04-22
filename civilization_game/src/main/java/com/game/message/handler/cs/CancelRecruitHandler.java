package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.SoldierPb;
import com.game.service.SoldierService;

public class CancelRecruitHandler extends ClientHandler {

    @Override
    public void action() {
        SoldierService soldierService = getService(SoldierService.class);
        SoldierPb.CancelRecruitRq req = msg.getExtension(SoldierPb.CancelRecruitRq.ext);
        soldierService.cancelRecruit(req, this);
    }
}
