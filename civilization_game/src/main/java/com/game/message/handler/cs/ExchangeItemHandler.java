package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class ExchangeItemHandler  extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.ExchangeItemRq req = msg.getExtension(ActivityPb.ExchangeItemRq.ext);
        service.exchangeItemHandler(req, this);
    }
}