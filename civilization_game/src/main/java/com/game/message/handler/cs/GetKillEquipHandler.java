package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.KillEquipService;

public class GetKillEquipHandler extends ClientHandler {
    @Override
    public void action () {
        KillEquipService service = getService(KillEquipService.class);
        service.getSkilEquip(this);
    }
}