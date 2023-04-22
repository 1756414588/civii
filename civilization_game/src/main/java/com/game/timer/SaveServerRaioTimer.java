package com.game.timer;


import com.game.server.datafacede.SaveServerRadioServer;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 半小时更新一次数据到数据库
 */
public class SaveServerRaioTimer extends TimerEvent {

	public SaveServerRaioTimer() {
		super(-1, 30 * TimeHelper.MINUTE_MS);
	}

	@Override
	public void action() {
		SpringUtil.getBean(SaveServerRadioServer.class).saveAll();
	}

}
