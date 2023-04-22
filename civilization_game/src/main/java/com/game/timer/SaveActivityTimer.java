package com.game.timer;

import com.game.define.DataFacedeTimer;
import com.game.server.datafacede.SaveActivityServer;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;

/**
 * 5分钟存储一次活动数据信息
 */
@DataFacedeTimer(desc = "")
public class SaveActivityTimer extends TimerEvent {

	public SaveActivityTimer() {
		super(-1, 5 * TimeHelper.MINUTE_MS);
	}

	@Override
	public void action() {
		SpringUtil.getBean(SaveActivityServer.class).saveAll();
	}
}
