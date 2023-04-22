package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.SoldierPb;
import com.game.service.SoldierService;

public class LargerBarracksHandler extends ClientHandler {

    @Override
    public void action() {
        // TODO Auto-generated method stub
        SoldierService soldierService = getService(SoldierService.class);
        SoldierPb.LargerBarracksRq req = msg.getExtension(SoldierPb.LargerBarracksRq.ext);
        soldierService.largerBarracksRq(req, this);
    }
}