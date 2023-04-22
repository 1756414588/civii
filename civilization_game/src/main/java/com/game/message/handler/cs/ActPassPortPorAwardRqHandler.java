package com.game.message.handler.cs;


import com.game.message.handler.ClientHandler;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/7/2 18:39
 **/
public class ActPassPortPorAwardRqHandler extends ClientHandler {

    @Override
    public void action() {
        ActivityService service = getService(ActivityService.class);
        service.actPassPortPorAwardRq(this);
    }
}
