package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author cpz
 * @date 2021/3/4 16:19
 * @description
 */
public class ActTaskHeroHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actHeroTask(msg.getExtension(ActivityPb.ActTaskHeroRq.ext), this);
    }
}
