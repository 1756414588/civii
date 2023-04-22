package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--英雄组合配置
 */
public class GetHeroGroupConfigHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetHeroGroupConfigRq req = msg.getExtension(FishingPb.GetHeroGroupConfigRq.ext);
        service.getHeroGroupConfigRq(req, this);
    }
}
