package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.SoldierPb;
import com.game.service.SoldierService;


public class RecruitDoneHandler extends ClientHandler {

    @Override
    public void action() {
        SoldierService soldierService = getService(SoldierService.class);
        SoldierPb.RecruitDoneRq req = msg.getExtension(SoldierPb.RecruitDoneRq.ext);
        soldierService.recruitDone(req, this);
    }
}
