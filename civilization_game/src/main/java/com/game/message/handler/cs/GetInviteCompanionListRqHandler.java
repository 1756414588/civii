package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.pb.FriendPb;
import com.game.service.FriendService;
import com.game.spring.SpringUtil;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2021/8/5 10:28
 **/
public class GetInviteCompanionListRqHandler extends ClientHandler {
    @Override
    public void action() {
        SpringUtil.getBean(FriendService.class).getInviteCompanionList(msg.getExtension(FriendPb.GetInviteCompanionListRq.ext), this);
    }
}
