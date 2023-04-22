package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

/**
 * @author zcp
 * @date 2021/3/15 11:18
 * 诵我真名者,永不见bug
 */
public class ActSpecialGiftHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actSpecialGiftRq(msg.getExtension(ActivityPb.ActSpecialGiftRq.ext), this);
    }
}
