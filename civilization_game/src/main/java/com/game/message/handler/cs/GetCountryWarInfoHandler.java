package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

public class GetCountryWarInfoHandler  extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.GetWorldCountryWarRq req = msg.getExtension(WorldPb.GetWorldCountryWarRq.ext);
        service.getCountryWarRq(req, this);
    }
}
