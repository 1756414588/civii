package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--钓鱼等级配置
 */
public class GetFishingLevelConfigHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetFishingLevelConfigRq req = msg.getExtension(FishingPb.GetFishingLevelConfigRq.ext);
        service.getFishingLevelConfigRq(req, this);
    }
}
