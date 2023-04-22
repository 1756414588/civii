package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldBoxPb;
import com.game.service.WorldBoxService;

/**
 *
 * @date 2021/1/7 17:41
 * @description
 */
public class DropWolrdBoxHandler extends ClientHandler {
    @Override
    public void action() {
        WorldBoxPb.DropWolrdBoxRq rq = msg.getExtension(WorldBoxPb.DropWolrdBoxRq.ext);
        getService(WorldBoxService.class).dropWolrdBox(rq, this);
    }
}
