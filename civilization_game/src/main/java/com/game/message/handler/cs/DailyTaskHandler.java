package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.DailyTaskPb;
import com.game.service.DailyTaskService;

/**
 * @author zcp
 * @date 2021/3/9 20:56
 * 诵我真名者,永不见bug
 */
public class DailyTaskHandler extends ClientHandler {
    @Override
    public void action() {
        getService(DailyTaskService.class).dailyTask(this, msg.getExtension(DailyTaskPb.DailyTaskRq.ext));
    }
}
