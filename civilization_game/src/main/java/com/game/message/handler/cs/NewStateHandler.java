package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

public class NewStateHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService service = getService(PlayerService.class);
        RolePb.NewStateRq req = msg.getExtension(RolePb.NewStateRq.ext);
        service.newStateRq(req, this);
    }
}