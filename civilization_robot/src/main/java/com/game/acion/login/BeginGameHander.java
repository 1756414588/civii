package com.game.acion.login;

import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.manager.RobotManager;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.RolePb.UserLoginRs;
import com.game.server.TimerServer;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;

/**
 * 账号服登录之后拿到用户的key和token进行登录，登录完毕返回的消息
 */
public class BeginGameHander extends MessageHandler {

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base req) {
		UserLoginRs rs = req.getExtension(UserLoginRs.ext);
		LogHelper.CHANNEL_LOGGER.info("账号登录返回:{}", rs);

		RobotManager robotManager = getBean(RobotManager.class);
		Robot robot = robotManager.getRobotByKey(accountKey);

		int state = rs.getState();// 1.角色未创建 2.角色已创建 3.禁止登陆 4.不属于白名单，无法登陆
		switch (state) {
			case 1:
				LogHelper.CHANNEL_LOGGER.info("创建角色");
				TimerServer.getInst().addDelayEvent(new MessageEvent(robot, SpringUtil.getBean(CreateRoleAction.class), 1000L));
				break;
			case 2:
				LogHelper.CHANNEL_LOGGER.info("角色已创建,直接登录");
				TimerServer.getInst().addDelayEvent(new MessageEvent(robot, SpringUtil.getBean(RoleLoginAction.class), 1000L));
				break;
			case 3:
				LogHelper.CHANNEL_LOGGER.info("角色已 【禁止登陆】");
				break;
			case 4:
				LogHelper.CHANNEL_LOGGER.info("不属于白名单【无法登陆】");
				break;
			default:
		}
	}
}
