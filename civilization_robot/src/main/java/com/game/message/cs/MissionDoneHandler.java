package com.game.message.cs;

import com.game.domain.Robot;
import com.game.manager.AwardManager;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.MissionPb.MissionDoneRs;
import io.netty.channel.ChannelHandlerContext;

/**
 * 副本奖励
 */
public class MissionDoneHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext cx, int accountKey, Base req) {
		MissionDoneRs msg = req.getExtension(MissionDoneRs.ext);

		Robot robot = getRobot(accountKey);

		AwardManager awardManager = getBean(AwardManager.class);

		if (msg.getOthersCount() > 0) {
			awardManager.addAward(robot, msg.getOthersList());
		}
	}
}
