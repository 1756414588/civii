package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 *
 * @date 2021/3/22 10:06
 *
 * 累计消费
 */
public class ActGrandRecharegHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actGrandRecharegRq(msg.getExtension(ActivityPb.ActGrandRecharegRq.ext), this);
    }
}
