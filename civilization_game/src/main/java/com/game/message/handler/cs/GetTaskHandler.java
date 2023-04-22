package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.TaskService;

// 获取所有任务
public class GetTaskHandler extends ClientHandler {
    @Override
    public void action () {
        TaskService service = getService(TaskService.class);
        service.getTaskRq(this);
    }
}