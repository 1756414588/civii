package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.MissionService;

/**
 * @author jyb
 * @date 2020/4/6 16:21
 * @description
 */
public class GetSweepHeroHandler extends ClientHandler {
    @Override
    public void action() {
        getService(MissionService.class).getSweepHeroRq(this);
    }
}
