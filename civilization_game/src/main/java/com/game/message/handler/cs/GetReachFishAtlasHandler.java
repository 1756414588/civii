package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--鱼类图鉴
 */
public class GetReachFishAtlasHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetReachFishAtlasRq req = msg.getExtension(FishingPb.GetReachFishAtlasRq.ext);
        service.getReachFishAtlasRq(req, this);
    }
}
