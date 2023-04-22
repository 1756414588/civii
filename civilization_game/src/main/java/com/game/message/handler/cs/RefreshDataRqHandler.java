package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

public class RefreshDataRqHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService playerService = getService(PlayerService.class);
        RolePb.RefreshDataRq req = msg.getExtension(RolePb.RefreshDataRq.ext);
        playerService.refreshDataRq(req, this);
    }
}

