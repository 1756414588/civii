package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author cpz
 * @date 2020/11/5 10:13
 * @description
 */
public class ActChrismasBuyHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.ActChrismasBuyRq rq = msg.getExtension(ActivityPb.ActChrismasBuyRq.ext);
        service.actChrismasBuyRq(rq, this);
    }
}
