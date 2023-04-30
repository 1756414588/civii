package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

/**
 *
 * @date 2020/4/20 13:58
 * @description
 */
public class BuyRebuildQueueHandler extends ClientHandler {
    @Override
    public void action() {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.BuyRebuildQueueRq req = msg.getExtension(BuildingPb.BuyRebuildQueueRq.ext);
        service.buyRebuildQueue(this,req);
    }
}
