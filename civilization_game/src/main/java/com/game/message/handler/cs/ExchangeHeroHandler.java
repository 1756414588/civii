package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class ExchangeHeroHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.ExchangeHeroRq req = msg.getExtension(ActivityPb.ExchangeHeroRq.ext);
        service.exchangeHeroHandler(req, this);
    }
}