package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RiotPb;
import com.game.service.RiotService;

/**
 * @author cpz
 * @date 2020/10/28 1:25
 * @description
 */
public class RiotItemShopHandler extends ClientHandler {
    @Override
    public void action() {
        RiotService service = getService(RiotService.class);
        RiotPb.RiotItemShopRq rq = msg.getExtension(RiotPb.RiotItemShopRq.ext);
        service.riotItemShop(this, rq);
    }
}
