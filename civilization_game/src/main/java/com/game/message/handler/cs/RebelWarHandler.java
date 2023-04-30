package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 *
 * @date 2020/4/29 10:46
 * @description
 */
public class RebelWarHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.RebelWarRq req = msg.getExtension(WorldPb.RebelWarRq.ext);
        service.rebelWar(req,this);
    }
}
