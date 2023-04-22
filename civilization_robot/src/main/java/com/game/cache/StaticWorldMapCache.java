package com.game.cache;

import com.game.dao.s.StaticConfigDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticWorldMap;
import com.game.load.ILoadData;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @Description
 * @Date 2022/9/20 16:50
 **/

@LoadData(name = "地图基本数据", initSeq = 1000)
@Component
public class StaticWorldMapCache implements ILoadData {

	private Map<Integer, StaticWorldMap> worldMaps = new HashMap<>();

	@Autowired
	private StaticConfigDao staticDataDao;

	@Override
	public void load() {
		worldMaps = staticDataDao.selectWorldMap();
	}

	@Override
	public void init() {
	}

	public int getMapId(int posX, int posY) {
		for (StaticWorldMap staticWorldMap : worldMaps.values()) {
			if (staticWorldMap == null) {
				continue;
			}

			if (posX >= staticWorldMap.getX1() &&
				posX <= staticWorldMap.getX2() &&
				posY >= staticWorldMap.getY1() &&
				posY <= staticWorldMap.getY2()) {
				return staticWorldMap.getMapId();
			}
		}
		return 0;
	}

	public StaticWorldMap getStaticWorldMap(int posX, int posY) {
		for (StaticWorldMap staticWorldMap : worldMaps.values()) {
			if (staticWorldMap == null) {
				continue;
			}

			if (posX >= staticWorldMap.getX1() &&
				posX <= staticWorldMap.getX2() &&
				posY >= staticWorldMap.getY1() &&
				posY <= staticWorldMap.getY2()) {
				return staticWorldMap;
			}
		}
		return null;
	}

	public Map<Integer, StaticWorldMap> getWorldMaps() {
		return worldMaps;
	}
}
