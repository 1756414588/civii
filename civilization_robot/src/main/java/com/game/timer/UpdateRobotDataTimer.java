package com.game.timer;


import com.game.cache.RobotDataCache;
import com.game.define.AppTimer;
import com.game.server.datafacede.SaveRobotDataServer;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;

@AppTimer(desc = "定时存储机器人数据")
public class UpdateRobotDataTimer extends TimerEvent {

	public UpdateRobotDataTimer() {
		super(-1, 5000);
	}

	@Override
	public void action() {

		RobotDataCache robotDataCache = SpringUtil.getBean(RobotDataCache.class);

		// 存储服务
		SaveRobotDataServer saveRobotDataServer = SpringUtil.getBean(SaveRobotDataServer.class);

		int curDate = TimeHelper.getCurrentDay();
		long curTime = System.currentTimeMillis();
		robotDataCache.getDataMap().values().stream().filter(e -> e.getLoginDate() == curDate && e.getLastSaveTime() < curTime).forEach((e -> {
			e.setLastSaveTime(curTime + 300000);// 下次更新时间为5分钟后
			saveRobotDataServer.saveData(e);
			LogHelper.CHANNEL_LOGGER.info("定时存储 accountKeyId:{}", e.getAccountKey());
		}));
	}
}
