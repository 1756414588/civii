package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 * @author jyb
 * @date 2020/5/26 16:06
 * @description
 */
public class RebelFightShareHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.RebelFightShareRq req = msg.getExtension(WorldPb.RebelFightShareRq.ext);
        service.rebelFightShareRq(req,this);
    }
}
