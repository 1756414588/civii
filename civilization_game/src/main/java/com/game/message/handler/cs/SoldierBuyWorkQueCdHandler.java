package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.SoldierPb;
import com.game.service.SoldierService;

public class SoldierBuyWorkQueCdHandler extends ClientHandler {

    @Override
    public void action() {
        SoldierService soldierService = getService(SoldierService.class);
        SoldierPb.RecruitWorkQueCdRq req = msg.getExtension(SoldierPb.RecruitWorkQueCdRq.ext);
        soldierService.buyWorkQueCd(req, this);
    }
}
