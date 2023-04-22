package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--鱼饵图鉴
 */
public class GetReachBaitAtlasHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetReachBaitAtlasRq req = msg.getExtension(FishingPb.GetReachBaitAtlasRq.ext);
        service.getReachBaitAtlasRq(req, this);
    }
}
