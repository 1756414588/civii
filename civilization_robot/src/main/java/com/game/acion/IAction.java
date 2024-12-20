package com.game.acion;

import com.game.domain.Robot;
import com.game.pb.BasePb.Base;

public interface IAction {

	long getId();

	void doAction(MessageEvent event, Robot robot);

	void onResult(MessageEvent event, Robot robot, Base base);

	void registerEvent(Robot robot);

	boolean isCompalte(Robot robot);

	long getRemain();

	int getGroup();

	byte[] getMessage();


}
