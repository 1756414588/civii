package com.game.server.thread;

import com.game.domain.p.WorldMap;
import com.game.manager.WorldManager;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @Description 地图数据存储线程
 * @Date 2022/9/9 11:30
 **/

public class SaveWorldMapThread extends SaveThread<WorldMap> {

	// 命令执行队列
	private LinkedBlockingQueue<Integer> world_map_queue = new LinkedBlockingQueue<Integer>();
	private HashMap<Integer, WorldMap> worldMaps = new HashMap<Integer, WorldMap>();

	public SaveWorldMapThread(String threadName) {
		super(threadName);
	}

	public void run() {
		stop = false;
		done = false;
		while (!stop || !world_map_queue.isEmpty()) {
			WorldMap worldMap = null;
			synchronized (this) {
				Integer mapId = world_map_queue.poll();
				if (mapId != null) {
					worldMap = worldMaps.remove(mapId);
				}
			}
			if (worldMap == null) {
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					LogHelper.ERROR_LOGGER.error(threadName + " Wait Exception:" + e.getMessage());

				}
			} else {
				try {
					WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
					if (worldMap.getMapData() != null) {
						worldManager.updateWorldMap(worldMap);
					}
					if (logFlag) {
						saveCount++;
					}
				} catch (Exception e) {
					LogHelper.ERROR_LOGGER.error("Map Exception:" + worldMap.getMapId(), e);
				}
			}
		}

		done = true;
		LogHelper.SAVE_LOGGER.error(threadName + " stopped!! save count :" + saveCount);
	}

	@Override
	public void add(WorldMap worldMap) {
		try {
			synchronized (this) {
				if (!worldMaps.containsKey(worldMap.getMapId())) {
					this.world_map_queue.add(worldMap.getMapId());
				}
				this.worldMaps.put(worldMap.getMapId(), worldMap);
				notify();
			}
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(threadName + " Notify Exception:" + e.getMessage(), e);
		}
	}
}
