package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author CaoBing
 * @date 2020/10/19 9:45
 */
public class DoPayArmsAwardHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService activityService = getService(ActivityService.class);
        ActivityPb.DoPayArmsAwardRq req = msg.getExtension(ActivityPb.DoPayArmsAwardRq.ext);
        activityService.doPayArmsAward(req, this);
    }
}
