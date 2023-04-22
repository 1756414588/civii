package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @Date 2021/3/26 14:12
 **/
public class ActCollectionResourceHandler extends ClientHandler {
    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        service.actCollectionResourceRq(this);
    }
}
