package com.game.timer;

import com.game.acion.IAction;
import com.game.define.AppTimer;
import com.game.domain.Robot;
import com.game.manager.LoginManager;
import com.game.manager.MessageManager;
import com.game.manager.DailyEventManager;
import com.game.manager.RobotManager;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 *
 * @Description日常任务
 * @Date 2022/10/20 17:32
 **/
@AppTimer(desc = "日常处理")
public class DailyTimer extends TimerEvent {

	public DailyTimer() {
		super(-1, 1000);
	}

	@Override
	public void action() {
		LoginManager loginManager = SpringUtil.getBean(LoginManager.class);
		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);
		DailyEventManager dailyManager = SpringUtil.getBean(DailyEventManager.class);
		MessageManager messageManager = SpringUtil.getBean(MessageManager.class);

		int today = TimeHelper.getCurrentDay();

		loginManager.getOnlineMap().forEach((id, robotData) -> {

			// 今天创建的账号直跑新手引导
			if (robotData.getCreateDate() == today) {
				return;
			}

			Robot robot = robotManager.getRobotByKey(id);
			if (robot == null) {
				return;
			}

			// 新手引导未完成
			if (!messageManager.isComplate(robotData)) {
				return;
			}

			long messageId = robotData.getMessageId();

			// 今日的第一个日常
			if (messageId == 0) {
				IAction fristAction = dailyManager.getByChild(messageId);
				fristAction.registerEvent(robot);
				return;
			}

			IAction action = dailyManager.getAction(messageId);
			if (action == null) {
				return;
			}

			// 当前行为已完成
			if (action.isCompalte(robot)) {// 完成当前的
				IAction next = dailyManager.getByChild(messageId);
				if (next == null) {
					return;
				}
				next.registerEvent(robot);
			}
		});
	}
}
