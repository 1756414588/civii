package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.KillEquipPb;
import com.game.service.KillEquipService;

public class UpKillEquipHandler extends ClientHandler {
    @Override
    public void action () {
        KillEquipService service = getService(KillEquipService.class);
        KillEquipPb.UpKillRq req = msg.getExtension(KillEquipPb.UpKillRq.ext);
        service.upKillEquip(req, this);
    }
}