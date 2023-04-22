package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RiotPb;
import com.game.service.RiotService;

/**
 * @author cpz
 * @date 2020/10/28 1:26
 * @description
 */
public class RiotScoreShopBuyHandler extends ClientHandler {
    @Override
    public void action() {
        RiotService service = getService(RiotService.class);
        RiotPb.RiotScoreShopBuyRq rq = msg.getExtension(RiotPb.RiotScoreShopBuyRq.ext);
        service.riotScoreShopBuy(this, rq);
    }
}
