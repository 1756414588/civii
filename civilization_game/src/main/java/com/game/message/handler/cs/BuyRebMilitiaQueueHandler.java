package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

/**
 * @author jyb
 * @date 2020/4/20 14:41
 * @description
 */
public class BuyRebMilitiaQueueHandler extends ClientHandler {
    @Override
    public void action() {
        BuildingService service = getService(BuildingService.class);
        BuildingPb.BuyRebMilitiaQueueRq req = msg.getExtension(BuildingPb.BuyRebMilitiaQueueRq.ext);
        service.buyRebMilitiaQueue(this,req);
    }
}
