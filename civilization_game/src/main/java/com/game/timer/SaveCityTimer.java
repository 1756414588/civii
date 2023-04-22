package com.game.timer;


import com.game.server.datafacede.SaveCityServer;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 3分钟定时存储以下城池信息
 */
public class SaveCityTimer extends TimerEvent {

	public SaveCityTimer() {
		super(-1, 3 * TimeHelper.MINUTE_MS);
	}

	@Override
	public void action() {
		SpringUtil.getBean(SaveCityServer.class).saveAll();
	}

}
