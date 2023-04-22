package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @author cpz
 * @date 2020/11/17 10:59
 * @description
 */
public class ActOpenBuildGiftHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        service.actOpenBuildingGiftRq(this);
    }
}
