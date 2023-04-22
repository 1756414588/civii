package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--积分兑换商品
 */
public class PointsExchangeHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.PointsExchangeRq req = msg.getExtension(FishingPb.PointsExchangeRq.ext);
        service.pointsExchangeRq(req, this);
    }
}
