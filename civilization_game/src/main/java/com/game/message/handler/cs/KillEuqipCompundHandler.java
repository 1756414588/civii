package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.KillEquipPb;
import com.game.service.KillEquipService;

// 杀器合成
public class KillEuqipCompundHandler extends ClientHandler {
    @Override
    public void action () {
        KillEquipService service = getService(KillEquipService.class);
        KillEquipPb.CompundRq req = msg.getExtension(KillEquipPb.CompundRq.ext);
        service.compund(req, this);

    }
}
