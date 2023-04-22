package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.AchievementService;

public class AchievementDetailAwardHandler extends ClientHandler {
    @Override
    public void action() {
        getService(AchievementService.class).recAchiInfoAward(msg.getExtension(ActivityPb.AchievementInfoAwardRq.ext), this);
    }
}
