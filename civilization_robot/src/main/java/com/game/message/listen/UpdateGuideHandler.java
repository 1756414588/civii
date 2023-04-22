package com.game.message.listen;

import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.UpdateGuideRq;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 监听到玩家获取坐标 则所有的玩家攻打自身周围的叛军
 */
public class UpdateGuideHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {

		UpdateGuideRq msg = req.getExtension(UpdateGuideRq.ext);
		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();

//		LogHelper.CHANNEL_LOGGER.info("【更新新手引导步骤】 UpdateGuide :{}", msg.getGuideKey());

		robotList.forEach(e -> {
			e.setGuideKey(msg.getGuideKey());// 设至玩家的新手引导
			e.sendPacket(PacketCreator.create(req));
		});
	}
}
