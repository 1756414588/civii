package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb;
import com.game.service.BeautyService;

/**
 * 2020年6月6日
 *
 *    halo_game
 * UpBeautySkillHandler.java
 **/
public class UpBeautySkillHandler extends ClientHandler {
    @Override
    public void action() {
        BeautyService service = getService(BeautyService.class);
        BeautyPb.UpNewBeautySkillRq req = msg.getExtension(BeautyPb.UpNewBeautySkillRq.ext);
        service.upBeautySkillRq(req, this);
    }
}
