package com.game.message.listen;

import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.MapMoveRq;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 迁城
 */
public class ListenMapMoveHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		MapMoveRq msg = req.getExtension(MapMoveRq.ext);
//		LogHelper.CHANNEL_LOGGER.info("ListenMapMoveHandler msg:{}", msg);
//		if (msg.hasPos()) {// 指定迁城则不跟随
//			return;
//		}
		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();
		robotList.forEach(e -> {
			e.sendPacket(PacketCreator.create(req));
//			LogHelper.CHANNEL_LOGGER.info("ListenMapMoveHandler roldId:{} pos:{}", e.getLord().getLordId(), e.getPos());
		});
	}

}
