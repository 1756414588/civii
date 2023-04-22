package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--玩家钓鱼相关数据
 */
public class GetFishingTeamQueueHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.GetFishingTeamQueueRq req = msg.getExtension(FishingPb.GetFishingTeamQueueRq.ext);
        service.GetFishingTeamQueueRq(req, this);
    }
}
