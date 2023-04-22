package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.ActivityPb.TDTaskAwardRq;
import com.game.service.ActivityService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/26 14:05
 **/
public class TDTaskAwardHandler extends ClientHandler {

    @Override
    public void action() {
        getService(ActivityService.class).tdTaskAwardRq(this,msg.getExtension(TDTaskAwardRq.ext));
    }
}
