package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.EquipPb;
import com.game.service.EquipService;

public class DecompoundEquipHandler extends ClientHandler {
    @Override
    public void action() {
        EquipService euipService = getService(EquipService.class);
        EquipPb.DecompoundEquipRq req = msg.getExtension(EquipPb.DecompoundEquipRq.ext);
        euipService.decompoundEquipRq(req,this);
    }
}