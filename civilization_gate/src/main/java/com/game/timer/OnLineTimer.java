package com.game.timer;

import com.game.define.AppTimer;
import com.game.manager.UserClientManager;
import com.game.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @Description 在线人数统计
 * @Date 2022/9/9 11:30
 **/

@AppTimer(desc = "在线人数统计")
public class OnLineTimer extends TimerEvent {

	public static Logger logger = LoggerFactory.getLogger("ONLINE");

	private Packet packet;

	public OnLineTimer() {
		super(-1, 1000);
	}


	@Override
	public void action() {
		long count = UserClientManager.getInst().getChannels().values().stream().filter(e -> e.getRoleId() != 0).count();
		logger.info("[在线人数:{} ]", count);
	}

}
