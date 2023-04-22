package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RebelPb;
import com.game.service.RebelService;

/**
 * @author jyb
 * @date 2020/4/28 15:52
 * @description
 */
public class UseRebelPropHandler extends ClientHandler {
    @Override
    public void action() {
        RebelService service = getService(RebelService.class);
        RebelPb.UseRebelPropRq req = msg.getExtension(RebelPb.UseRebelPropRq.ext);
        service.useRebelProp(req, this);
    }
}
