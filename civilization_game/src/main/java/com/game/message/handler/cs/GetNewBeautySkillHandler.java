package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb.GetNewBeautySkillRq;
import com.game.service.BeautyService;

/**
 * @Description TODO
 * @Date 2021/3/23 15:59
 **/
public class GetNewBeautySkillHandler extends ClientHandler {
    @Override
    public void action() {
        BeautyService service = getService(BeautyService.class);
        GetNewBeautySkillRq rq = msg.getExtension(GetNewBeautySkillRq.ext);
        service.getNewBeautySkillRq(rq, this);
    }
}
