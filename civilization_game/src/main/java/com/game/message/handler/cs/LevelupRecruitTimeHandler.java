package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.SoldierPb;
import com.game.service.SoldierService;


public class LevelupRecruitTimeHandler extends ClientHandler {

    @Override
    public void action() {
        SoldierService soldierService = getService(SoldierService.class);
        SoldierPb.LevelupRecruitTimeRq req = msg.getExtension(SoldierPb.LevelupRecruitTimeRq.ext);
        soldierService.levelupRecruitTimeRq(req, this);
    }
}