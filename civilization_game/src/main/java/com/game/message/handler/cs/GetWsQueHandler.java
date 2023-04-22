package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorkShopService;


public class GetWsQueHandler extends ClientHandler {
    @Override
    public void action () {
        WorkShopService service = getService(WorkShopService.class);
        service.getWsQue(this);

    }
}
