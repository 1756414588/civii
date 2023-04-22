package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.service.EquipService;

public class DoneEquipHandler extends ClientHandler {
    @Override
    public void action() {
        EquipService euipService = getService(EquipService.class);
        EquipPb.DoneEquipRq req = msg.getExtension(EquipPb.DoneEquipRq.ext);
        euipService.doneEquip(req, this);
    }
}