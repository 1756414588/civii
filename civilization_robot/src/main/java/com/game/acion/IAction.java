package com.game.acion;

import com.game.domain.Robot;
import com.game.domain.p.RobotMessage;
import com.game.pb.BasePb.Base;

public interface IAction {

	long getId();

	void doAction(MessageEvent messageEvent, Robot robot);

	void onResult(MessageEvent messageEvent, Robot robot, Base base);

	void registerEvent(Robot robot);

	boolean isCompalte(Robot robot);

	long getRemain();

	RobotMessage getRobotMessage();

}
