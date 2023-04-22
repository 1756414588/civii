package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.service.WallService;


public class GetWallInfoHandler extends ClientHandler {
    @Override
    public void action () {
        WallService service = getService(WallService.class);
        service.getWallInfo(this);
    }
}

