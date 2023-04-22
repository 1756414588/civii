package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class AttendPvpCityHandler extends ClientHandler {
    @Override
    public void action () {
        WorldService service = getService(WorldService.class);
        WorldPb.AttendPvpCityRq req = msg.getExtension(WorldPb.AttendPvpCityRq.ext);
        service.attendPvpCityRq(req, this);
    }
}
