package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WallPb;
import com.game.service.WallService;


public class KickMarchHandler extends ClientHandler {
    @Override
    public void action () {
        WallService service = getService(WallService.class);
        WallPb.KickMarchRq req = msg.getExtension(WallPb.KickMarchRq.ext);
        service.kickMarchRq(req, this);
    }
}


