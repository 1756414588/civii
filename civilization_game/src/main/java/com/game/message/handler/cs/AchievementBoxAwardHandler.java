package com.game.message.handler.cs;

import com.game.message.handler.ClientHandler;
import com.game.packet.Packet;
import com.game.pb.ActivityPb;
import com.game.service.AchievementService;
import io.netty.channel.ChannelHandlerContext;

public class AchievementBoxAwardHandler extends ClientHandler {
    @Override
    public void action() {
        getService(AchievementService.class).recAchiBoxAward(msg.getExtension(ActivityPb.AchievementBoxAwardRq.ext),this);
    }

//    @Override
//    public void action(ChannelHandlerContext ctx, Packet packet, long roleId) {
//
//    }
}
