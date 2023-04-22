package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldBoxPb;
import com.game.pb.WorldPb;
import com.game.service.WorldBoxService;

/**
 * @author cpz
 * @date 2021/1/7 17:41
 * @description
 */
public class TopWolrdBoxHandler extends ClientHandler {
    @Override
    public void action() {
        WorldBoxPb.TopWolrdBoxRq rq = msg.getExtension(WorldBoxPb.TopWolrdBoxRq.ext);
        getService(WorldBoxService.class).topWolrdBox(rq, this);
    }
}
