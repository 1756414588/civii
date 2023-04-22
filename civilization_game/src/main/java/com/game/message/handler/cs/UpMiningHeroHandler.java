package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CastlePb.MiningUpRq;
import com.game.service.CastleService;

public class UpMiningHeroHandler extends ClientHandler {
    @Override
    public void action () {
        CastleService service = getService(CastleService.class);
        MiningUpRq req = msg.getExtension(MiningUpRq.ext);
        service.upMiningHeroRq(req, this);
    }
}
