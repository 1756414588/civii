package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--积分商店配置
 */
public class GetPointsShopConfigHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetPointsShopConfigRq req = msg.getExtension(FishingPb.GetPointsShopConfigRq.ext);
        service.getPointsShopConfigRq(req, this);
    }
}
