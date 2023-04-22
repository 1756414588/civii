package com.game.acion.login;

import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.CreateRoleRs;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 创建角色
 */
public class CreateRoleHandler extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		CreateRoleRs rs = req.getExtension(CreateRoleRs.ext);
		int state = rs.getState();

		LogHelper.CHANNEL_LOGGER.info("创建角色返回:{}", rs);
		Robot robot = getRobot(accountKey);

		//1.成功  2.名字被占用  3.角色已创建(返回nick和portrait)
		switch (state) {
			case 1:
				LogHelper.CHANNEL_LOGGER.info("角色创建成功,随后登录");
				TimerServer.getInst().addDelayEvent(new MessageEvent(robot, SpringUtil.getBean(RoleLoginAction.class), 3000L));
				break;
			case 2:
				break;
			case 3:
				LogHelper.CHANNEL_LOGGER.info("角色已创建,直接登录");
				TimerServer.getInst().addDelayEvent(new MessageEvent(robot, SpringUtil.getBean(RoleLoginAction.class), 3000L));
				break;
			default:
		}
	}
}
