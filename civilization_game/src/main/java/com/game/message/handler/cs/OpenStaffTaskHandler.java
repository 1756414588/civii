package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.StaffPb.OpenStaffTaskRq;
import com.game.service.StaffService;

public class OpenStaffTaskHandler extends ClientHandler {
    @Override
    public void action () {
        StaffService service = getService(StaffService.class);
        OpenStaffTaskRq req = msg.getExtension(OpenStaffTaskRq.ext);
        service.openStaffTask(req,this);
    }
}