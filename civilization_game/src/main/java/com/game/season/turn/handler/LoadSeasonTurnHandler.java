package com.game.season.turn.handler;

import com.game.message.handler.ClientHandler;
import com.game.season.SeasonService;

public class LoadSeasonTurnHandler extends ClientHandler {
    @Override
    public void action() {
        getService(SeasonService.class).loadTurn(this);
    }
}
