package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

public class OpenAutoHandler extends ClientHandler {
    @Override
    public void action() {
        PlayerService service = getService(PlayerService.class);
        RolePb.OpenAutoRq req = msg.getExtension(RolePb.OpenAutoRq.ext);
        service.openAuto(req, this);
    }
}
