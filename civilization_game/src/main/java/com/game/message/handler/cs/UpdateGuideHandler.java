package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.RolePb;
import com.game.service.PlayerService;

/**
 *
 * @date 2020/4/25 14:59
 * @description
 */
public class UpdateGuideHandler  extends ClientHandler {
    @Override
    public void action() {
        RolePb.UpdateGuideRq req = msg.getExtension( RolePb.UpdateGuideRq.ext);
        getService(PlayerService.class).updateGuide( this,req);
    }
}
