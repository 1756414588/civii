package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb;
import com.game.service.FriendService;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/7/13 10:44
 **/
public class RemoveMasterRqHandler extends ClientHandler {
    @Override
    public void action() {
        getService(FriendService.class).removeMasterRq(msg.getExtension(FriendPb.RemoveMasterRq.ext), this);
    }
}
