package com.game.message.cs;

import com.game.domain.Robot;
import com.game.manager.AwardManager;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.TaskPb.TaskAwardRs;
import io.netty.channel.ChannelHandlerContext;

/**
 * 领取任务奖励
 */
public class TaskAwardHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext cx, int accountKey, Base req) {
		TaskAwardRs msg = req.getExtension(TaskAwardRs.ext);

		Robot robot = getRobot(accountKey);

		AwardManager awardManager = getBean(AwardManager.class);

		if (msg.getAwardCount() > 0) {
			awardManager.addAward(robot, msg.getAwardList());
		}

		if (msg.hasLevelAward()) {
			awardManager.addAward(robot, msg.getLevelAward().getAwardsList());
		}

	}
}
