package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 *
 * @date 2021/3/4 16:20
 * @description
 */
public class DoActTaskHeroRewardHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).doActHeroTaskReward(msg.getExtension(ActivityPb.DoActTaskHeroRewardRq.ext), this);
    }
}
