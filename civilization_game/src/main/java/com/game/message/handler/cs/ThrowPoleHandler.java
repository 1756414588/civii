package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--钓鱼抛竿
 */
public class ThrowPoleHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.ThrowPoleRq req = msg.getExtension(FishingPb.ThrowPoleRq.ext);
        service.throwPoleRq(req, this);
    }
}
