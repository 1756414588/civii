package com.game.acion.login;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.manager.DailyEventManager;
import com.game.manager.MessageEventManager;
import com.game.manager.MessageManager;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import org.springframework.stereotype.Component;

/**
 * @Author 陈奎
 * @Description 所有的登录请求已成功
 * @Date 2022/9/19 11:23
 **/

@Component
public class OnAllLoginReqSuccessEvent extends EnterGameAction {

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {

		LogHelper.CHANNEL_LOGGER.info("登录完成后开始自主操作");

		MessageEventManager actionManager = SpringUtil.getBean(MessageEventManager.class);
		MessageManager messageManager = SpringUtil.getBean(MessageManager.class);

		RobotData robotData = robot.getData();
		if (!messageManager.isComplate(robotData)) {// 新手引导已全部完成
			IAction action = actionManager.getAction(robot.getGuildId());
			if (action == null) {
				return;
			}

			// 机器人操作是否已完成,未完成则继续执行
			if (!action.isCompalte(robot)) {
				action.registerEvent(robot);
			}
			return;
		}

		// 当前创建的账号直做新手引导
		if (robotData.getCreateDate() == TimeHelper.getCurrentDay()) {
			return;
		}

		int today = TimeHelper.getCurrentDay();
		DailyEventManager dailyEventManager = SpringUtil.getBean(DailyEventManager.class);
		if (robotData.getDailyDate() == today && robotData.getMessageId() != 0) {
			IAction action = dailyEventManager.getAction(robotData.getMessageId());
			if (action == null) {
				return;
			}
			
			// 操作未完成则进行注册
			if (!action.isCompalte(robot)) {
				action.registerEvent(robot);
			}
		}
	}
}
