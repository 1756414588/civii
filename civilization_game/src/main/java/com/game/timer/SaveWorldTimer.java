package com.game.timer;

import com.game.domain.WorldData;
import com.game.domain.p.World;
import com.game.manager.WorldManager;
import com.game.server.datafacede.SaveWorldServer;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;

/**
 * 5分钟定时保存以下世界信息
 */
public class SaveWorldTimer extends TimerEvent {

	public SaveWorldTimer() {
		super(-1, 5 * TimeHelper.MINUTE_MS);
	}

	@Override
	public void action() {
		SpringUtil.getBean(SaveWorldServer.class).saveAll();
		checkNewSeason();
	}


	/**
	 *
	 */
	public void checkNewSeason() {
		WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
		WorldData data = worldManager.getWolrdInfo();
		if (data == null) {
			LogHelper.CONFIG_LOGGER.info("data is null");
			return;
		}
		worldManager.checkNewSeason(data);
	}

}

