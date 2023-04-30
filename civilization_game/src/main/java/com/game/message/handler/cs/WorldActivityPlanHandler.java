package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.WorldActPlanService;

/**
 *
 * @date 2020/3/31 15:48
 * @description
 */
public class WorldActivityPlanHandler extends ClientHandler {
    @Override
    public void action() {
        getService(WorldActPlanService.class).WorldActivityPlan(this);
    }
}
