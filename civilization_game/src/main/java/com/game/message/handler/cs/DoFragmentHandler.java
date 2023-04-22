package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.PropPb;
import com.game.service.ActivityService;

public class DoFragmentHandler  extends ClientHandler {
    @Override
    public void action() {


        PropPb.DoFragmentRq req = msg.getExtension(PropPb.DoFragmentRq.ext);
        ActivityService service = getService(ActivityService.class);
        service.actFragment(req, this);

    }
}
