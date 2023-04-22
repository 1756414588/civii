package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.service.RebelService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/17 17:15
 **/
public class RebelGuideAwardHandler extends ClientHandler {

    @Override
    public void action() {
        getService(RebelService.class).rebelGuideAwardRq(this);
    }
}
