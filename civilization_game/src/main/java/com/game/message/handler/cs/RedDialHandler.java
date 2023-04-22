package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.service.ActivityService;

public class RedDialHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        ActivityPb.RedDialRq req = msg.getExtension(ActivityPb.RedDialRq.ext);
        service.redDial(req, this);
    }
}
