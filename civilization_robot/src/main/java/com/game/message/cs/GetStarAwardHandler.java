package com.game.message.cs;

import com.game.domain.Robot;
import com.game.manager.AwardManager;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.MissionPb.GetStarAwardRs;
import io.netty.channel.ChannelHandlerContext;

/**
 * 获取星级奖励
 */
public class GetStarAwardHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext cx, int accountKey, Base req) {
		GetStarAwardRs msg = req.getExtension(GetStarAwardRs.ext);

		Robot robot = getRobot(accountKey);

		AwardManager awardManager = getBean(AwardManager.class);

		if (msg.getAwardCount() > 0) {
			awardManager.addAward(robot, msg.getAwardList());
		}

	}
}
