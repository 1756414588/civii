package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.WallPb;
import com.game.service.WallService;

public class LevelUpDefenderHandler extends ClientHandler {
    @Override
    public void action () {
        WallService service = getService(WallService.class);
        WallPb.LevelUpDefenderRq req = msg.getExtension(WallPb.LevelUpDefenderRq.ext);
        service.levelUpDefender(req, this);
    }
}



