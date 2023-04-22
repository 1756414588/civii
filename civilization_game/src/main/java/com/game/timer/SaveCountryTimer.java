package com.game.timer;


import com.game.server.datafacede.SaveCountryServer;
import com.game.server.datafacede.SaveWorldMapServer;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 5分钟定时存储国家信息
 */
public class SaveCountryTimer extends TimerEvent {

	public SaveCountryTimer() {
		super(-1, 5 * TimeHelper.MINUTE_MS);
	}

	@Override
	public void action() {
		SpringUtil.getBean(SaveCountryServer.class).saveAll();
	}

}
