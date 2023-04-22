package com.game.message.cs;

import com.game.domain.Robot;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb.March;
import com.game.pb.WorldPb.SynMarchRq;
import io.netty.channel.ChannelHandlerContext;

/**
 * 行军信息
 */
public class SynMarchHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		SynMarchRq msg = req.getExtension(SynMarchRq.ext);

		Robot robot = getRobot(accountKey);

		// 添加行军
		if (msg.hasMarch()) {
			March march = msg.getMarch();
			if (march.getLordId() == robot.getLord().getLordId()) {
				march.getHeroIdList().forEach(heroId -> {
					robot.getCache().getHeroCache().getArmys().put(heroId, heroId);
				});

			}
		}

		// 删除行军
		if (msg.hasRemove()) {
			March march = msg.getRemove();
			if (march.getLordId() == robot.getLord().getLordId()) {
				march.getHeroIdList().forEach(heroId -> {
					robot.getCache().getHeroCache().getArmys().remove(heroId);
				});

			}
		}
	}


}
