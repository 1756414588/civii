package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author zcp
 * @date 2021/3/22 9:54
 * 诵我真名者,永不见bug
 */
public class ActWellCrownThreeArmyHandler extends ClientHandler {


    @Override
    public void action() {
        getService(ActivityService.class).actWellCrownThreeArmyRq(msg.getExtension(ActivityPb.ActWellCrownThreeArmyRq.ext), this);
    }
}
