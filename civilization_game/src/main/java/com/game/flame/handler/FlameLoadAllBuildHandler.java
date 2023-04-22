package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

/**
 * 拉取所有的建筑信息
 */
public class FlameLoadAllBuildHandler extends ClientHandler {
    @Override
    public void action() {
        getService(FlameWarService.class).flameBuildInfo(msg.getExtension(FlameWarPb.FlameLoadAllBuildRq.ext),this);
    }
}
