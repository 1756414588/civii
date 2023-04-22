package com.game.action.rs;

import com.game.action.PacketHandler;
import com.game.domain.RobotListen;
import com.game.manager.RobotManager;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.InnerPb.ListenEventRq;
import com.game.pb.InnerPb.ListenEventRs;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 机器人监听网关服行为处理handler
 */
public class ListenEventHandler extends PacketHandler {

	@Override
	public void action(ChannelHandlerContext ctx, Packet packet) {
		ListenEventRq req = getMsg(packet, ListenEventRq.ext);
		for (String uid : req.getEventparamList()) {
			RobotListen robotListen = new RobotListen(Long.valueOf(uid), ctx);
			RobotManager.getInst().putRobotListen(robotListen);
		}

		// 返回消息
		ListenEventRs.Builder builder = ListenEventRs.newBuilder();
		builder.setEventId(req.getEventId());
		builder.addAllEventparam(req.getEventparamList());

		// 返回消息
		Base response = BasePbHelper.createSynBase(ListenEventRs.EXT_FIELD_NUMBER, ListenEventRs.ext, builder.build()).build();
		Packet pack = PacketCreator.create(response);
		ctx.writeAndFlush(pack);

		LogHelper.CHANNEL_LOGGER.info("ListenEventHandler msg:{}", req);
	}
}
