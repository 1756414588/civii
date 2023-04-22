package com.game.timer;


import com.game.server.datafacede.SaveWorldMapServer;
import com.game.service.WorldService;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 5分钟定时存储一次世界地图信息
 */
public class SaveWorldMapTimer extends TimerEvent {

	public SaveWorldMapTimer() {
		super(-1, 5 * TimeHelper.MINUTE_MS);
	}

	@Override
	public void action() {
		SpringUtil.getBean(SaveWorldMapServer.class).saveAll();
	}

}
