package com.game.message.cs;

import com.game.domain.Robot;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.WorldPb.AttackRebelRs;
import io.netty.channel.ChannelHandlerContext;

/**
 * 攻打野怪后，立马行军加速
 */
public class AttackRebelHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		AttackRebelRs msg = req.getExtension(AttackRebelRs.ext);
		Robot robot = getRobot(accountKey);
		if (req.getCode() == 200) {
			msg.getMarch();
		}
	}
}
