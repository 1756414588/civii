package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BroodWarPb;
import com.game.service.BroodWarService;

/**
 * @author zcp
 * @date 2021/7/5 9:29
 */
public class BroodWarIntegralHandler extends ClientHandler {
    @Override
    public void action() {
        BroodWarService service = getService(BroodWarService.class);
        BroodWarPb.BroodWarIntegralRankRq req = msg.getExtension(BroodWarPb.BroodWarIntegralRankRq.ext);
        service.broodWarIntegralRank(req, this);
    }
}
