package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

/**
 * @author jyb
 * @date 2020/1/9 10:46
 * @description
 */
public class BuyUpBuildQueHandler  extends ClientHandler {
    @Override
    public void action() {
        BuildingPb.BuyUpBuildQueRq req = msg.getExtension(BuildingPb.BuyUpBuildQueRq.ext);
        BuildingService service = getService(BuildingService.class);
        service.buyUpBuildQue(this,req);
    }
}
