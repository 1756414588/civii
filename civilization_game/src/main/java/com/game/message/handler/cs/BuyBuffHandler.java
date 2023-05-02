package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BroodWarPb;
import com.game.service.BroodWarService;

/**
 *
 * @date 2021/7/5 16:40
 */
public class BuyBuffHandler extends ClientHandler {
    @Override
    public void action() {
        getService(BroodWarService.class).buyBuff(msg.getExtension(BroodWarPb.BuyBuffRq.ext), this);
    }
}
