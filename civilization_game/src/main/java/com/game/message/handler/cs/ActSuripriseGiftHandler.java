package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author zcp
 * @date 2021/3/10 10:45
 * 诵我真名者,永不见bug
 */
public class ActSuripriseGiftHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actSuripriseGift(msg.getExtension(ActivityPb.ActSuripriseGiftRq.ext), this);
    }
}