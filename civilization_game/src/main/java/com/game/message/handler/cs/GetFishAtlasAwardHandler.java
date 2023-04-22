package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--获得图鉴奖励
 */
public class GetFishAtlasAwardHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetFishAtlasAwardRq req = msg.getExtension(FishingPb.GetFishAtlasAwardRq.ext);
        service.GetFishAtlasAwardRq(req, this);
    }
}
