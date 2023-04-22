package com.game.server.datafacede;

import com.game.constant.CityType;
import com.game.constant.MapId;
import com.game.dataMgr.StaticWorldMgr;
import com.game.define.DataFacede;
import com.game.domain.Brood;
import com.game.domain.WorldData;
import com.game.domain.p.BroodWarData;
import com.game.domain.s.StaticWorldCity;
import com.game.manager.BroodWarManager;
import com.game.manager.WorldManager;
import com.game.server.GameServer;
import com.game.server.thread.SaveBroodWarThread;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import com.game.worldmap.BroodWar;
import com.game.worldmap.MapInfo;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 母巢之战数据存储服务
 * @Date 2022/9/9 11:30
 **/

@DataFacede(desc = "保存母巢之战")
@Service
public class SaveBroodWarServer extends SaveServer<Brood> {

	public SaveBroodWarServer() {
		super("SAVE_BROOD_WAR_SERVER", 1);
	}

	@Override
	public SaveThread createThread(String name) {
		return new SaveBroodWarThread(name);
	}

	@Override
	public void saveData(Brood brood) {
		SaveThread thread = threadPool.get(0);
		thread.add(brood);
	}

	/**
	 * 存储下所有数据
	 */
	@Override
	public void saveAll() {
		WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
		StaticWorldMgr staticWorldMgr = SpringUtil.getBean(StaticWorldMgr.class);
		try {
			MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
			Brood brood = new Brood();
			for (StaticWorldCity cityConfig : staticWorldMgr.getCityMap().values()) {
				if (cityConfig.getType() != CityType.BROOD_WAR_TURRET && cityConfig.getType() != CityType.WORLD_FORTRESS) {
					continue;
				}
				BroodWar broodWar = (BroodWar) mapInfo.getEntity(cityConfig.getX(), cityConfig.getY());
				brood.addBroodWar(cityConfig.getType(), broodWar);
			}
			WorldData worldData = worldManager.getWolrdInfo();
			// 添加任命
			worldData.getAppoints().forEach((e, f) -> {
				brood.addBroodWarPosition(f);
			});
            // 添加更新
            saveData(brood);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error("SAVE_BROOD_WAR_SERVER:{}", e.getMessage(), e);
		}
	}
}
