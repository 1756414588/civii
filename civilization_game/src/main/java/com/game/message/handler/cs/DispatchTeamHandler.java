package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FishingPb;
import com.game.service.FishingService;

/**
 * 渔场--派遣队伍
 */
public class DispatchTeamHandler extends ClientHandler {

    @Override
    public void action() {
        FishingService service = getService(FishingService.class);
        FishingPb.DispatchTeamRq req = msg.getExtension(FishingPb.DispatchTeamRq.ext);
        service.dispatchTeamRq(req, this);
    }
}
