package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author CaoBing
 * @date 2020/10/11 17:02
 */
public class BuyPayArmsHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.BuyPayArmsRq req = msg.getExtension(ActivityPb.BuyPayArmsRq.ext);
        service.buyPayArmsRq(req,this);
    }
}
