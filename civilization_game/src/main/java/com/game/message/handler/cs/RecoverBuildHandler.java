package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.BuildingPb;
import com.game.service.BuildingService;

/**
 * @author jyb
 * @date 2020/6/30 19:13
 * @description
 */
public class RecoverBuildHandler extends ClientHandler {
    @Override
    public void action() {
        BuildingService soldierService = getService(BuildingService.class);
        BuildingPb.RecoverBuildRq req = msg.getExtension(BuildingPb.RecoverBuildRq.ext);
        soldierService.recoverBuild(req, this);
    }
}
