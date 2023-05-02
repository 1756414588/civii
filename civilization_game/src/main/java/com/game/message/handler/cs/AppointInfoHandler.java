package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BroodWarPb;
import com.game.service.BroodWarService;

/**
 *
 * @date 2021/7/5 9:29
 */
public class AppointInfoHandler extends ClientHandler {
    @Override
    public void action() {
        BroodWarService service = getService(BroodWarService.class);
        BroodWarPb.AppointInfoRq req = msg.getExtension(BroodWarPb.AppointInfoRq.ext);
        service.appointInfo(req, this);
    }
}
