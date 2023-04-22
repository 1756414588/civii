package com.game.message.cs;

import com.game.acion.LoginAction;
import com.game.acion.MessageEvent;
import com.game.cache.CacheManager;
import com.game.domain.Robot;
import com.game.manager.MessageEventManager;
import com.game.message.MessageHandler;
import com.game.pb.BasePb.Base;
import com.game.util.LogHelper;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @Description 【未注册的】通用消息接收
 * @Date 2022/9/19 11:12
 **/

@Component
public class CommonPacketHandler extends MessageHandler {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private MessageEventManager actionManager;

	@Override
	public void action(ChannelHandlerContext ctx, int accountKey, Base base) {

		Robot robot = getRobot(accountKey);

		// 登录未处理完毕,则继续处理登录信息
		if (isLoginAction(robot, base)) {
			return;
		}

		// 调用触发事件
		long eventId = base.getParam();
		if (actionManager.getListenEvents().contains(accountKey, eventId)) {
			MessageEvent messageEvent = actionManager.getEvent(accountKey, eventId);
			messageEvent.getAction().onResult(messageEvent, robot, base);
			actionManager.getListenEvents().remove(accountKey, base.getParam());
		}
	}


	private boolean isLoginAction(Robot robot, Base base) {
		// 已登录完毕
		if (!cacheManager.isContain(robot.getId())) {
			return false;
		}

		MessageEvent messageEvent = cacheManager.getLoginAction(robot.getId(), base);
		if (messageEvent == null) {
			return true;
		}

		LogHelper.CHANNEL_LOGGER.info("[登录.返回] accountKey:{} eventId:{} cmd:{} code:{}", robot.getId(), base.getParam(), base.getCommand(), base.getCode());

		// 处理登录消息
		LoginAction loginAction = (LoginAction) messageEvent.getAction();
		loginAction.onResult(messageEvent, robot, base);

		// 消耗掉登录消息
		cacheManager.remove(robot.getId(), base.getParam());

		// 登录消息都消耗掉了
		if (!cacheManager.isContain(robot.getId())) {
			robot.setLogin(true);

			LogHelper.CHANNEL_LOGGER.info("【登录完成】 robot:{}", robot.getId());
			loginAction.onLoginSuccess(robot);
		}
		return true;
	}
}
