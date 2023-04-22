package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--获得鱼饵
 */
public class GetBaitsHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetBaitsRq req = msg.getExtension(FishingPb.GetBaitsRq.ext);
        service.getBaitsRq(req, this);
    }
}
