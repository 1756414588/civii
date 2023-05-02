package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BroodWarPb;
import com.game.service.BroodWarService;

/**
 *
 * @date 2021/7/5 9:29
 */
public class BroodWarScoreRankHandler extends ClientHandler {
    @Override
    public void action() {
        BroodWarService service = getService(BroodWarService.class);
        BroodWarPb.BroodWarScoreRankRq req = msg.getExtension(BroodWarPb.BroodWarScoreRankRq.ext);
        service.broodWarScoreRank(req, this);
    }
}
