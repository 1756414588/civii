package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldPvpService;

// 参与皇城血战
public class AttendPvpHandler  extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        service.attendPvp(this);
    }
}