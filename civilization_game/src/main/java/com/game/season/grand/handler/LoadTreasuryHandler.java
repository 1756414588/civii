package com.game.season.grand.handler;

import com.game.message.handler.ClientHandler;
import com.game.season.SeasonService;

public class LoadTreasuryHandler extends ClientHandler {
    @Override
    public void action() {
        getService(SeasonService.class).loadTreasury(this);
    }
}
