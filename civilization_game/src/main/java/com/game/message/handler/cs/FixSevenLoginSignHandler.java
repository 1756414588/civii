package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;


/**
 *
 * @date 2020/6/14 10:33
 * @description
 */
public class FixSevenLoginSignHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.FixSevenLoginSignRq req = msg.getExtension(ActivityPb.FixSevenLoginSignRq.ext);
        service.fixSevenLoginSign(req, this);
    }
}
