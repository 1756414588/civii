package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.domain.WorldData;
import com.game.domain.p.World;
import com.game.manager.WorldManager;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import com.game.server.thread.SaveWorldThread;
import com.game.util.LogHelper;

import com.game.spring.SpringUtil;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 世界数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "世界存储")
@Service
public class SaveWorldServer extends SaveServer<World> {

	public SaveWorldServer() {
		super("SAVE_WORLD_SERVER", 1);
	}

	public SaveThread createThread(String name) {
		return new SaveWorldThread(name);
	}

	public void saveData(World world) {
		SaveThread thread = threadPool.get((world.getKeyId() % threadNum));
		thread.add(world);
	}

	// 保存世界数据
	@Override
	public void saveAll() {
		WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
		try {
			WorldData worldData = worldManager.getWolrdInfo();
			if (worldData == null) {
				LogHelper.SAVE_LOGGER.info("worldData not find");
				return;
			}
			long now = System.currentTimeMillis();
			worldData.setLastSaveTime(now);
			saveData(new World(worldData));
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("SAVE_WORLD_SERVER:{}", e.getMessage(), e);
		}
	}
}
