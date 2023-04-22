package com.game.message.listen;

import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.NewStateRq;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听新手引导
 */
public class ListenNewStateHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {

		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();

		NewStateRq rq = req.getExtension(NewStateRq.ext);
		int newStateId = rq.getNewStateId();

//		LogHelper.CHANNEL_LOGGER.info("【更新新手引导步骤】 NewState :{}", newStateId);

		robotList.forEach(e -> {
			e.setNewStateId(newStateId);// 设至玩家的新手引导
			e.sendPacket(PacketCreator.create(req));
		});
	}

}
