package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.service.EquipService;

public class BlackSmithFreeCdHandler extends ClientHandler {
    @Override
    public void action() {
        EquipService euipService = getService(EquipService.class);
        EquipPb.BlackSmithFreeCdRq req = msg.getExtension(EquipPb.BlackSmithFreeCdRq.ext);
        euipService.blackSmithFreeCd(req, this);
    }
}