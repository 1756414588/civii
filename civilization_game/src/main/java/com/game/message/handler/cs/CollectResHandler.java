package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.WorldPb;
import com.game.service.WorldService;

// 采集资源
public class CollectResHandler  extends ClientHandler {
    @Override
    public void action () {
        WorldService service = getService(WorldService.class);
        WorldPb.CollectResRq req = msg.getExtension(WorldPb.CollectResRq.ext);
        service.collectResRq(req, this);
    }
}
