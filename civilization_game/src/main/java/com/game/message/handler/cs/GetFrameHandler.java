package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.FrameService;

/**
 * @author cpz
 * @date 2021/1/26 19:43
 * @description
 */
public class GetFrameHandler extends ClientHandler {
    @Override
    public void action() {
        RolePb.getFrameRq rq = msg.getExtension(RolePb.getFrameRq.ext);
        getService(FrameService.class).getFrameRq(rq, this);
    }
}
