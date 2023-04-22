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
public class OpenWorldBoxHandler extends ClientHandler {
    @Override
    public void action() {
        WorldBoxPb.OpenWorldBoxRq rq = msg.getExtension(WorldBoxPb.OpenWorldBoxRq.ext);
        getService(WorldBoxService.class).openWorldBox(rq, this);
    }
}
