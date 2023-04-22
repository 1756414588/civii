package com.game.season.journey.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class SeasonJourneyAwardHandler extends ClientHandler {
    @Override
    public void action() {
        getService(SeasonService.class).journeyTaskComplete(this,msg.getExtension(SeasonActivityPb.SeasonJourneyCompleteRq.ext));
    }
}
