package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 *
 * @date 2020/11/5 10:13
 * @description
 */
public class ActChrismasRewardHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.ActChrismasRewardRq rq = msg.getExtension(ActivityPb.ActChrismasRewardRq.ext);
        service.actChrismasRewardRq(rq, this);
    }
}
