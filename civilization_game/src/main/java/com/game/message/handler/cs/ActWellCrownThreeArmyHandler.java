package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 *
 * @date 2021/3/22 9:54
 *
 */
public class ActWellCrownThreeArmyHandler extends ClientHandler {


    @Override
    public void action() {
        getService(ActivityService.class).actWellCrownThreeArmyRq(msg.getExtension(ActivityPb.ActWellCrownThreeArmyRq.ext), this);
    }
}
