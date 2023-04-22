package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @author cpz
 * @date 2020/10/28 7:52
 * @description
 */
public class GetBuildGiftHandler extends ClientHandler {
    @Override
    public void action() {
        getService(ActivityService.class).actBuildingGiftRq(this);
    }
}
