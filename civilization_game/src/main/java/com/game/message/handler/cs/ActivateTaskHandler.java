package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.StaffPb;
import com.game.service.StaffService;


public class ActivateTaskHandler  extends ClientHandler {
    @Override
    public void action () {
        StaffService service = getService(StaffService.class);
        StaffPb.ActivateTaskRq req = msg.getExtension(StaffPb.ActivateTaskRq.ext);
        service.activateStaffTask(req,this);
    }
}