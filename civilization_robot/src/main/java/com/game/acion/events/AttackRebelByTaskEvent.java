package com.game.acion.events;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.constant.GameError;
import com.game.domain.Robot;
import com.game.pb.BasePb.Base;

/**
 *
 * @Description 攻打叛军
 * @Date 2022/9/23 17:28
 **/

public class AttackRebelByTaskEvent extends MessageEvent {

	private int rebelLevel;

	// 领取任务奖励
	private MessageEvent messageEvent;


	public AttackRebelByTaskEvent(MessageEvent messageEvent, long delayTime) {
		super(messageEvent.getRobot(), null, delayTime);
	}

	private void initAction() {

	}

	@Override
	public void complate(Robot robot, Base base) {
		if (base.getCode() == GameError.OK.getCode()) {

		}
	}
}
