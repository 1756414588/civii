package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

/**
 * @author jyb
 * @date 2020/4/29 14:39
 * @description
 */
public class AttendRebelWarHandler extends ClientHandler {
    @Override
    public void action() {
        WorldService service = getService(WorldService.class);
        WorldPb.AttendRebelWarRq req = msg.getExtension(WorldPb.AttendRebelWarRq.ext);
        service.attendRebelWar(this, req);
    }
}
