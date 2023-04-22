package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.CastlePb;
import com.game.pb.KillEquipPb;
import com.game.service.CastleService;
import com.game.service.KillEquipService;

/**
 * @author CaoBing
 * @date 2021/3/16 16:52
 */
public class OpenKillEquipHandler  extends ClientHandler {
    @Override
    public void action() {
        KillEquipPb.OpenKillEquipRq rq = msg.getExtension(KillEquipPb.OpenKillEquipRq.ext);
        getService(KillEquipService.class).openKillEquipRq(this,rq);
    }
}
