package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WallPb;
import com.game.service.WallService;

public class KillDefenderCdHandler  extends ClientHandler {
    @Override
    public void action () {
        WallService service = getService(WallService.class);
        WallPb.KillDefenderCdRq req = msg.getExtension(WallPb.KillDefenderCdRq.ext);
        service.killWallCd(req, this);
    }
}



