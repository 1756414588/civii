package com.game.season.journey.handler;

import com.game.message.handler.ClientHandler;
import com.game.season.SeasonService;

public class LoadSeasonJourneyInfo extends ClientHandler {
    @Override
    public void action() {
        getService(SeasonService.class).loadSeasonJourney(this);
    }
}
