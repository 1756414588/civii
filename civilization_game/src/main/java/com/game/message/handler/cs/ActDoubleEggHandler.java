package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 *
 * @date 2020/11/5 10:13
 * @description
 */
public class ActDoubleEggHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        service.actDoubleEggRq(msg.getExtension(ActivityPb.ActDoubleEggRq.ext), this);
    }
}
