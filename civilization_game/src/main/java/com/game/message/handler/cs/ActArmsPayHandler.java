package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @author CaoBing
 * @date 2020/10/11 16:46
 */
public class ActArmsPayHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        service.actArmsPayRq(this);
    }
}
