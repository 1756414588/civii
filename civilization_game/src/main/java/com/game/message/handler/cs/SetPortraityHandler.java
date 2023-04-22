package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;


public class SetPortraityHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService playerService = getService(PlayerService.class);
        RolePb.SetPortraitRq req = msg.getExtension(RolePb.SetPortraitRq.ext);
        playerService.setPortrait(req, this);
    }
}
