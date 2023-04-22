package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.BuyActPassPortLvRq;
import com.game.service.ActivityService;

/**
 * @Description 购买通行证等级
 * @Date 2021/1/22 14:31
 **/
public class BuyActPassPortLvHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        BuyActPassPortLvRq rq = msg.getExtension(BuyActPassPortLvRq.ext);
        service.buyActPassPortLvRq(rq, this);
    }
}
