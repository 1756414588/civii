package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author CaoBing
 * @date 2020/8/12 13:59
 */
public class DoPassPortAwardHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService activityService = getService(ActivityService.class);
        ActivityPb.DoPassPortAwardRq req = msg.getExtension( ActivityPb.DoPassPortAwardRq.ext);
        activityService.doHirDoPassPortAwardRq(req, this);
    }
}
