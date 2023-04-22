package com.game.message.listen;

import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.MissionPb.MissionDoneRq;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * 刷副本
 */
public class ListenMissionDoneHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		MissionDoneRq msg = req.getExtension(MissionDoneRq.ext);

		// 关卡ID
		int missionId = msg.getMissionId();

		RobotManager robotManager = getBean(RobotManager.class);
		List<Robot> robotList = robotManager.getRobotList();
		for (Robot robot : robotList) {

			List<Integer> upHeros = robot.getCache().getHeroCache().getEmbattles();

			if (upHeros.isEmpty()) {
				LogHelper.CHANNEL_LOGGER.info("上阵武将为空 robotId:{}", robot.getId());
				continue;
			}

			MissionDoneRq.Builder missionDoneRq = MissionDoneRq.newBuilder();
			missionDoneRq.setMissionId(missionId);
			upHeros.forEach(heroId -> {
				if (heroId == 0) {
					return;
				}
				missionDoneRq.addHeroId(heroId);
			});
			robot.sendPacket(PacketCreator.create(req));
		}
	}
}
