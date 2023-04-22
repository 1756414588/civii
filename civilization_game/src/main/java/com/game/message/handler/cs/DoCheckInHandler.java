package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.DoDailyCheckInRq;
import com.game.service.ActivityService;


public class DoCheckInHandler extends ClientHandler {
    @Override
    public void action() {
        DoDailyCheckInRq req = msg.getExtension(DoDailyCheckInRq.ext);
        ActivityService service = getService(ActivityService.class);
        service.doCheckInRq(req, this);
    }
}
