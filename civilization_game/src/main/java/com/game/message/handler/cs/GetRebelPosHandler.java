package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;
import com.game.spring.SpringUtil;

/**
 *
 * @date 2020/12/25 10:19
 * @description
 */
public class GetRebelPosHandler extends ClientHandler {
    @Override
    public void action() {
        WorldPb.GetRebelPosRq rq = msg.getExtension(WorldPb.GetRebelPosRq.ext);
        SpringUtil.getBean(WorldService.class).getRebelPosRq(rq, this);
    }
}
