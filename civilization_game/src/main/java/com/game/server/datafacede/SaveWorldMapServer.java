package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.domain.p.WorldMap;
import com.game.manager.WorldManager;
import com.game.server.GameServer;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import com.game.server.thread.SaveWorldMapThread;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.worldmap.MapInfo;

import java.util.Iterator;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 世界地图数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "地图存储")
@Service
public class SaveWorldMapServer extends SaveServer<WorldMap> {

	public SaveWorldMapServer() {
		super("SAVE_WORLD_MAP_SERVER", 4);
	}

	public SaveThread createThread(String name) {
		return new SaveWorldMapThread(name);
	}

	public void saveData(WorldMap worldMap) {
		if (worldMap == null) {
			LogHelper.ERROR_LOGGER.error("worldMap is null!");
			return;
		}
		SaveThread thread = threadPool.get((worldMap.getMapId() % threadNum));
		thread.add(worldMap);
	}

	@Override
	public void saveAll() {
		WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
		Iterator<MapInfo> iterator = worldManager.getWorldMapInfo().values().iterator();
		long now = System.currentTimeMillis();
		while (iterator.hasNext()) {
			try {
				MapInfo mapInfo = iterator.next();
				mapInfo.setLastSaveTime(now);
				saveData(mapInfo.createWorldMap());
			} catch (Exception e) {
				LogHelper.ERROR_LOGGER.error("SAVE_WORLD_MAP_SERVER:{}", e.getMessage(), e);
			}
		}
	}

}
