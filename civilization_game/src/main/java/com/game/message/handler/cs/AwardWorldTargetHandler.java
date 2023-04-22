package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldTargetTaskService;

/**
 * @author jyb
 * @date 2019/12/25 16:54
 * @description
 */
public class AwardWorldTargetHandler extends ClientHandler {
    @Override
    public void action() {
        WorldPb.AwardWorldTargetRq awardWorldTargetRq = msg.getExtension(WorldPb.AwardWorldTargetRq.ext);
        getService(WorldTargetTaskService.class).awardWorldTarget(awardWorldTargetRq, this);
    }
}
