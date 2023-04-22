package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.StaffService;

public class GetStaffTaskHandler extends ClientHandler {
    @Override
    public void action() {
        // TODO Auto-generated method stub
        StaffService service = getService(StaffService.class);
        service.getStaffTask(this);
    }
}