package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CastlePb.MiningDownRq;
import com.game.service.CastleService;

public class DownMiningHeroHandler extends ClientHandler {
    @Override
    public void action () {
        CastleService service = getService(CastleService.class);
        MiningDownRq req = msg.getExtension(MiningDownRq.ext);
        service.downMiningHeroRq(req, this);
    }
}
