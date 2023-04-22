package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--钓鱼收竿
 */
public class TakeBackPoleHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.TakeBackPoleRq req = msg.getExtension(FishingPb.TakeBackPoleRq.ext);
        service.takeBackPoleRq(req, this);
    }
}
