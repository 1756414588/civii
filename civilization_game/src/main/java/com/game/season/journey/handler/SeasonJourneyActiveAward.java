package com.game.season.journey.handler;

import com.game.message.handler.ClientHandler;
import com.game.pb.SeasonActivityPb;
import com.game.season.SeasonService;

public class SeasonJourneyActiveAward extends ClientHandler {
    @Override
    public void action() {
        getService(SeasonService.class).seasonJourneyActiveAward(msg.getExtension(SeasonActivityPb.SeasonJourneyActiveAwardRq.ext),this);
    }
}
