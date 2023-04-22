package com.game.season.talent.handler;

import com.game.message.handler.ClientHandler;
import com.game.season.SeasonService;

public class LoadSeasonTalentHandler extends ClientHandler {
    @Override
    public void action() {
        getService(SeasonService.class).loadSeasonTalent(this);
    }
}
