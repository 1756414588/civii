package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--英雄上阵下阵
 */
public class PickHeroHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.PickHeroRq req = msg.getExtension(FishingPb.PickHeroRq.ext);
        service.pickHeroRq(req, this);
    }
}
