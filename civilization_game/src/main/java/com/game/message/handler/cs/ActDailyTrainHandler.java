package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @Date 2021/3/16 15:54
 **/
public class ActDailyTrainHandler extends ClientHandler {

    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        service.actDailyTrainRq(this);
    }
}
