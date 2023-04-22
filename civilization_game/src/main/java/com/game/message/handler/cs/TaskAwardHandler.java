package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.TaskPb;
import com.game.service.TaskService;

public class TaskAwardHandler extends ClientHandler {
    @Override
    public void action () {
        TaskService service = getService(TaskService.class);
        TaskPb.TaskAwardRq req = msg.getExtension(TaskPb.TaskAwardRq.ext);
        service.taskAwardRq(req, this);
    }
}
