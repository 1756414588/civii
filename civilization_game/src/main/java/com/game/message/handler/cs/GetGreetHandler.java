package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.service.WorldPvpService;

public class GetGreetHandler extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        service.getGreetRq(this);
    }
}
