package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @author CaoBing
 * @date 2020/10/16 17:57
 */
public class ActPassPortHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        service.actPassPortRq(this);
    }
}
