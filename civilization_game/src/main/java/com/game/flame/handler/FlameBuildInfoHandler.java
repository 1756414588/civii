package com.game.flame.handler;

import com.game.flame.FlameWarService;
import com.game.message.handler.ClientHandler;
import com.game.pb.FlameWarPb;

/**
 * 查看建筑信息
 */
public class FlameBuildInfoHandler extends ClientHandler {
	@Override
    public void action() {
        getService(FlameWarService.class).flameBuildInfo(msg.getExtension(FlameWarPb.FlameBuildInfoRq.ext),this);
    }
}
