package com.game.acion;

import com.game.domain.Robot;
import com.game.domain.p.RobotMessage;
import com.game.manager.MessageEventManager;
import com.game.pb.BasePb.Base;
import com.game.spring.SpringUtil;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description
 * @Date 2022/9/22 10:36
 **/

@Setter
@Getter
public class EventAction implements IAction {

	protected MessageEvent messageEvent;

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public void doAction(MessageEvent messageEvent, Robot robot) {
		robot.sendPacket(messageEvent.createPacket());
	}

	@Override
	public void onResult(MessageEvent messageEvent, Robot robot, Base base) {
		if (messageEvent != null) {
			messageEvent.complate(robot, base);
		}
	}

	@Override
	public void registerEvent(Robot robot) {
		SpringUtil.getBean(MessageEventManager.class).registerEvent(messageEvent);
	}

	@Override
	public boolean isCompalte(Robot robot) {
		return false;
	}

	@Override
	public long getRemain() {
		return 0;
	}

	@Override
	public int getGroup() {
		return 0;
	}

	@Override
	public byte[] getMessage() {
		return null;
	}
}
