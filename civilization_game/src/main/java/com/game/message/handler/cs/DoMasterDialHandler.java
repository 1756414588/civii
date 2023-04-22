package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoMasterDialRq;
import com.game.service.ActivityService;

public class DoMasterDialHandler extends ClientHandler {

    @Override
    public void action() {
        DoMasterDialRq req = msg.getExtension(DoMasterDialRq.ext);
        ActivityService service = getService(ActivityService.class);
        service.doMasterDialRq(req,this);
    }
}
