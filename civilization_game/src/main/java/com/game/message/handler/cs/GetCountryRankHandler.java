package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RankPb;
import com.game.service.RankService;

/**
 *
 * @date 2020/5/29 9:17
 * @description
 */
public class GetCountryRankHandler extends ClientHandler {
    @Override
    public void action() {
        // TODO Auto-generated method stub
        RankPb.GetCountryRankRq req = msg.getExtension(RankPb.GetCountryRankRq.ext);
        getService(RankService.class).getCountryRankRq(req, this);
    }
}
