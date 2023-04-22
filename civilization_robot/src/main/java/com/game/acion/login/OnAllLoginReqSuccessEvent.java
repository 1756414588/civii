package com.game.acion.login;

import com.game.acion.IAction;
import com.game.acion.LoginAction;
import com.game.acion.MessageEvent;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.manager.MessageEventManager;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import org.springframework.stereotype.Component;

/**
 *
 * @Description 所有的登录请求已成功
 * @Date 2022/9/19 11:23
 **/

@Component
public class OnAllLoginReqSuccessEvent extends LoginAction {

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {

		LogHelper.CHANNEL_LOGGER.info("登录完成后开始自主操作");

		Record record = robot.getRecord();
		MessageEventManager actionManager = SpringUtil.getBean(MessageEventManager.class);
		IAction action = actionManager.getAction(record.getRecordId());
		if (action == null) {
			return;
		}

		// 机器人操作是否已完成,未完成则继续执行
		if (!action.isCompalte(robot)) {
			action.registerEvent(robot);
		}
	}
}
