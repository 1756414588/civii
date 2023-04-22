package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RiotPb;
import com.game.service.RiotService;

/**
 * @author cpz
 * @date 2020/10/27 12:07
 * @description
 */
public class AttendRiotCityHandler extends ClientHandler {
    @Override
    public void action() {
        RiotService service = getService(RiotService.class);
        RiotPb.AttendRiotCityRq rq = msg.getExtension(RiotPb.AttendRiotCityRq.ext);
        service.attendRiotCityRq(rq, this);
    }
}
