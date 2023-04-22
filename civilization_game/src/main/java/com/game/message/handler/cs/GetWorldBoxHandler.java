package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldBoxPb;
import com.game.pb.WorldPb;
import com.game.service.WorldBoxService;

/**
 *
 * @date 2021/1/7 17:41
 * @description
 */
public class GetWorldBoxHandler extends ClientHandler {
    @Override
    public void action() {
        WorldBoxPb.GetWorldBoxRq rq = msg.getExtension(WorldBoxPb.GetWorldBoxRq.ext);
        getService(WorldBoxService.class).getWorldBox(rq, this);
    }
}
