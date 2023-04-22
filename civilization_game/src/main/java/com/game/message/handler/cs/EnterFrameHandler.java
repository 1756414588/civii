package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.FrameService;

/**
 * @author cpz
 * @date 2021/1/26 19:43
 * @description
 */
public class EnterFrameHandler extends ClientHandler {
    @Override
    public void action() {
        RolePb.enterFrameRq rq = msg.getExtension(RolePb.enterFrameRq.ext);
        getService(FrameService.class).enterFrameRq(rq, this);
    }
}
