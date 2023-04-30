package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 *
 * @date 2020/5/22 9:54
 * @description
 */
public class RebelFightHelpHandler extends ClientHandler {
    @Override
    public void action() {
        WorldPb.RebelFightHelpRq rq = msg.getExtension(WorldPb.RebelFightHelpRq.ext);
        getService(WorldService.class).rebelFightHelpRq(rq,this);
    }
}
