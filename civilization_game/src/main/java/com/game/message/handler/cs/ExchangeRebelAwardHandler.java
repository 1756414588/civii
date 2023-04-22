package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RebelPb;
import com.game.service.RebelService;

/**
 * @author jyb
 * @date 2020/5/6 11:18
 * @description
 */
public class ExchangeRebelAwardHandler extends ClientHandler {
    @Override
    public void action() {
        RebelPb.ExchangeRebelAwardRq req = msg.getExtension(RebelPb.ExchangeRebelAwardRq.ext);
        getService(RebelService.class).exchangeRebelAward(this, req);
    }
}
