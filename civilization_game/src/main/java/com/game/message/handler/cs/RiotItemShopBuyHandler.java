package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RiotPb;
import com.game.service.RiotService;

/**
 *
 * @date 2020/10/28 1:26
 * @description
 */
public class RiotItemShopBuyHandler extends ClientHandler {
    @Override
    public void action() {
        RiotService service = getService(RiotService.class);
        RiotPb.RiotItemShopBuyRq rq = msg.getExtension(RiotPb.RiotItemShopBuyRq.ext);
        service.riotItemShopBuy(this, rq);
    }
}
