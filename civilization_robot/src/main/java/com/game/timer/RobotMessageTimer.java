package com.game.timer;

import com.game.acion.IAction;
import com.game.define.AppTimer;
import com.game.domain.Robot;
import com.game.manager.MessageEventManager;
import com.game.manager.MessageManager;
import com.game.manager.RobotManager;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.stream.Collectors;


@AppTimer(desc = "机器人行为")
public class RobotMessageTimer extends TimerEvent {

	public RobotMessageTimer() {
		super(-1, 300);
	}

	@Override
	public void action() {
		MessageEventManager actionManager = SpringUtil.getBean(MessageEventManager.class);
		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);
		MessageManager messageManager = SpringUtil.getBean(MessageManager.class);

		long recordId = SpringUtil.getBean(MessageManager.class).getMaxGuildId();

		List<Robot> list = robotManager.getRobotMap().values().stream().filter(e -> e.isLogin() && e.getGuildId() != recordId).collect(Collectors.toList());
		list.forEach(robot -> {

			IAction action = actionManager.getAction(robot.getGuildId());
			if (!action.isCompalte(robot)) {
				return;
			}

			// 获取下一步的指令
			long cmdKey = messageManager.getNext(robot.getGuildId());
			IAction next = actionManager.getAction(cmdKey);

			// 事件执行完毕
			if (next == null) {
				return;
			}

			// 非同一组事件
			if (action.getGroup() != next.getGroup()) {
				return;
			}

			next.registerEvent(robot);
		});

	}
}
