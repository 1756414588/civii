package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CastlePb;
import com.game.service.CastleService;

/**
 *
 * @date 2019/12/19 15:42
 * @description
 */
public class BuyDefenseSoldiersHandler extends ClientHandler {
    @Override
    public void action() {
        CastlePb.BuyDefenseSoldiersRq rq = msg.getExtension(CastlePb.BuyDefenseSoldiersRq.ext);
        getService(CastleService.class).buyDefenseSoldier(this, rq);
    }
}
