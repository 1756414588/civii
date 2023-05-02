package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 *
 */
public class ActMentorScoreRankHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actMentorScoreRank(this,msg.getExtension(ActivityPb.ActMasterRankRq.ext));
    }
}
