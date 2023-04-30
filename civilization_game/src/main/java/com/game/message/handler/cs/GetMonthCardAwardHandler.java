package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb;
import com.game.pb.ActivityPb.GetMonthCardAwardRq;
import com.game.service.ActivityService;

/**
*2020年5月27日
*
*halo_game
*GetMonthCardAwardHandler.java
**/
public class GetMonthCardAwardHandler extends ClientHandler {
    @Override
    public void action () {
        ActivityService service = getService(ActivityService.class);
        GetMonthCardAwardRq rq = msg.getExtension(ActivityPb.GetMonthCardAwardRq.ext);
        service.getMonthCardAwardRq(rq, this);
    }
}
