package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.service.ZergService;

public class GetZergShopHandler extends ClientHandler {
    @Override
    public void action() {
        ZergService service = getService(ZergService.class);
        service.getZergShopRq(this);
    }
}
