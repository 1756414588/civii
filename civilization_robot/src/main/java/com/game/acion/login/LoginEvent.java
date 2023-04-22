package com.game.acion.login;

import com.game.acion.IAction;
import com.game.acion.MessageEvent;
import com.game.domain.Robot;
import com.game.domain.p.LoginMessage;
import com.game.packet.Packet;
import com.game.pb.BasePb.Base;

/**
 *
 * @Description
 * @Date 2022/9/19 15:06
 **/

public class LoginEvent implements IAction {

	private LoginMessage loginMessage;
	private Packet packet;

	public LoginEvent(LoginMessage loginMessage) {
		this.loginMessage = loginMessage;
	}

	@Override
	public long getId() {
		return loginMessage.getKeyId();
	}

	@Override
	public void doAction(MessageEvent messageEvent,Robot robot) {
	}

	@Override
	public void onResult(MessageEvent messageEvent,Robot robot, Base base) {
	}

	@Override
	public void registerEvent(Robot robot) {
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
