package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.MissionPb;
import com.game.service.MissionService;

/**
 * @author jyb
 * @date 2020/4/6 16:32
 * @description
 */
public class UpdateSweepHeroHandler extends ClientHandler {
    @Override
    public void action() {
        MissionPb.UpdateSweepHeroRq req = msg.getExtension(MissionPb.UpdateSweepHeroRq.ext);
        getService(MissionService.class).updateSweepHero(this, req);
    }
}
