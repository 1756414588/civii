package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PvpBattlePb;
import com.game.service.WorldPvpService;


public class ExchangePvpItemHandler extends ClientHandler {
    @Override
    public void action() {
        WorldPvpService service = getService(WorldPvpService.class);
        PvpBattlePb.ExchangeRq req = msg.getExtension(PvpBattlePb.ExchangeRq.ext);
        service.exchangeRq(req, this);
    }
}