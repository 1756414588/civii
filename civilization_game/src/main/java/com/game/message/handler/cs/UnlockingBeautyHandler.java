package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb.UnlockingBeautyRq;
import com.game.service.BeautyService;

/**
 * @Description TODO
 * @Date 2021/3/30 21:01
 **/
public class UnlockingBeautyHandler extends ClientHandler {
    @Override
    public void action() {
        UnlockingBeautyRq req = msg.getExtension(UnlockingBeautyRq.ext);
        getService(BeautyService.class).UnlockingBeautyRq(req, this);
    }
}
