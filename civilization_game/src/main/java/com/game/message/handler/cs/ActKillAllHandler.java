package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author cpz
 * @date 2020/11/5 10:13
 * @description
 */
public class ActKillAllHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.ActKillAllRq rq = msg.getExtension(ActivityPb.ActKillAllRq.ext);
        service.actKillAllRq(rq, this);
    }
}