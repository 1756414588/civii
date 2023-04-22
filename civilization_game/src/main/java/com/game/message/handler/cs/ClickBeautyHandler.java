package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb.ClickBeautyRq;
import com.game.service.BeautyService;

/**
 * @Description TODO
 * @Date 2021/3/31 18:18
 **/
public class ClickBeautyHandler extends ClientHandler {
    @Override
    public void action() {
        BeautyService service = getService(BeautyService.class);
        ClickBeautyRq req = msg.getExtension(ClickBeautyRq.ext);
        service.clickBeautyRq(req, this);
    }
}
