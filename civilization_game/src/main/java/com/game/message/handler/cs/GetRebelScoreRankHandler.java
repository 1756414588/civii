package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.RebelService;

/**
 * @author jyb
 * @date 2020/5/7 15:53
 * @description
 */
public class GetRebelScoreRankHandler extends ClientHandler {
    @Override
    public void action() {
        getService(RebelService.class).getRebelScoreRank(this);
    }
}
