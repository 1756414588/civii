package com.game.message.listen;

import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.Pos;
import com.game.pb.WorldPb.GetMapRq;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听到玩家获取坐标
 */
public class ListenGetMapHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();
		robotList.forEach(e -> {
			GetMapRq.Builder builder = GetMapRq.newBuilder();
			Pos.Builder pos = Pos.newBuilder();
			pos.setX(e.getLord().getPosX());
			pos.setY(e.getLord().getPosY());
			builder.setPos(pos.build());
			Base base = BasePbHelper.createRqBase(GetMapRq.EXT_FIELD_NUMBER, GetMapRq.ext, builder.build()).build();
			e.sendPacket(PacketCreator.create(base));
//			LogHelper.CHANNEL_LOGGER.info("ListenGetMapHandler pos:{}", pos.build());
		});
	}
}
