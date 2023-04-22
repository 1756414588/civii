package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.AchievementService;

public class AchievementInfoHandler extends ClientHandler {
    @Override
    public void action() {
        getService(AchievementService.class).achievementInfo(this);
    }
}
