package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.DailyTaskPb;
import com.game.service.DailyTaskService;

/**
 *
 * @date 2021/3/9 20:56
 *
 */
public class DailyTaskCompleteHandler extends ClientHandler {
    @Override
    public void action() {
        getService(DailyTaskService.class).dailyTaskComplete(this, msg.getExtension(DailyTaskPb.DailyTaskCompleteRq.ext));
    }
}
