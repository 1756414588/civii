package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.SoldierPb;
import com.game.service.SoldierService;

public class RecruitSoldierHandler extends ClientHandler {

    @Override
    public void action() {
        SoldierService soldierService = getService(SoldierService.class);
        SoldierPb.RecruitSoldierRq req = msg.getExtension(SoldierPb.RecruitSoldierRq.ext);
        soldierService.recruitSoldier(req, this);
    }
}