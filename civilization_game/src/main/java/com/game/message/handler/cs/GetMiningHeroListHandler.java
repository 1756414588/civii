package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CastlePb;
import com.game.service.CastleService;

public class GetMiningHeroListHandler extends ClientHandler {
    @Override
    public void action () {
        CastleService service = getService(CastleService.class);
        CastlePb.GetMiningHeroListRq rq = msg.getExtension(CastlePb.GetMiningHeroListRq.ext);
        service.getMiningHeroListRq(rq,this);
    }
}
