package com.game.dao.p;

import java.util.List;

import com.game.domain.p.WorldMap;

public interface WorldMapDao {

	WorldMap selectWolrdMap(int mapId);

	void insertWorldMap(WorldMap worldMap);

	void updateWorldMap(WorldMap worldMap);

	List<WorldMap> selectWorldMapList();
}