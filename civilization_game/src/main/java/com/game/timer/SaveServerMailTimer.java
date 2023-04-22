package com.game.timer;


import com.game.server.datafacede.SaveServerMailServer;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 半小时更新一次数据到数据库
 */
public class SaveServerMailTimer extends TimerEvent {

	public SaveServerMailTimer() {
		super(-1, 30 * TimeHelper.MINUTE_MS);
	}

	@Override
	public void action() {
		SpringUtil.getBean(SaveServerMailServer.class).saveAll();
	}

}
