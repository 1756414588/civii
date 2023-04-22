package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WallPb;
import com.game.service.WallService;


public class ChangeHeroPosHandler  extends ClientHandler {
    @Override
    public void action () {
        WallService service = getService(WallService.class);
        WallPb.ChangeHeroPosRq req = msg.getExtension(WallPb.ChangeHeroPosRq.ext);

        service.changeHeroPos(req, this);
    }
}

