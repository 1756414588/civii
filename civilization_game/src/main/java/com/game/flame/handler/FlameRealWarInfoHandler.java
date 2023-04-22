package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;

public class FlameRealWarInfoHandler extends ClientHandler {
    @Override
    public void action() {
        getService(FlameWarService.class).flameFlameRealWarInfo(this);
    }
}
