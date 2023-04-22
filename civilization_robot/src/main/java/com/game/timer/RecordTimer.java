package com.game.timer;


import com.game.define.AppTimer;
import com.game.server.datafacede.SaveRecordServer;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;

@AppTimer(desc = "操作记录存储")
public class RecordTimer extends TimerEvent {

	public RecordTimer() {
		super(-1, 300000);
	}

	@Override
	public void action() {
		SaveRecordServer saveRecordServer = SpringUtil.getBean(SaveRecordServer.class);
		saveRecordServer.saveAll();
		LogHelper.CHANNEL_LOGGER.info("定时存储记录");
	}
}
