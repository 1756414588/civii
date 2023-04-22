package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoPurpDialRq;
import com.game.service.ActivityService;

public class DoPurpDialHandler extends ClientHandler {
    @Override
    public void action() {
        DoPurpDialRq req = msg.getExtension(DoPurpDialRq.ext);
        ActivityService service = getService(ActivityService.class);
        service.doPurpDialRq(req, this);
    }
}
