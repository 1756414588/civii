package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.FirstBloodService;

public class GetAllFirstBloodInfoHandler extends ClientHandler {
    @Override
    public void action () {
        getService(FirstBloodService.class).getAllFirstBloodInfo(this);
    }
}
