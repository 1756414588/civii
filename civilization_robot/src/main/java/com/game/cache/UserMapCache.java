package com.game.cache;

import com.game.domain.WorldPos;
import com.game.pb.CommonPb;
import com.game.spring.SpringUtil;
import com.game.util.LogHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description 用户地图缓存
 * @Date 2022/9/21 20:16
 **/
@Getter
@Setter
public class UserMapCache {

	int maxLevel = 0;

	// 地图缓存数据
	private Map<Integer, WorldPos> worldPosMap = new ConcurrentHashMap<>();

	public void add(CommonPb.WorldEntity worldEntity) {
		if (worldEntity.getEntityType() != 1) {
			return;
		}

		WorldPos worldPos = new WorldPos(worldEntity.getPos());
		worldPos.setLevel(worldEntity.getLevel());

		// 设置地图ID
		StaticWorldMapCache staticWorldMapCache = SpringUtil.getBean(StaticWorldMapCache.class);
		int mapId = staticWorldMapCache.getMapId(worldPos.getX(), worldPos.getY());
		worldPos.setMapId(mapId);

		if (worldPosMap.containsKey(worldPos.getPosValue())) {
			return;
		}
		worldPosMap.put(worldPos.getPosValue(), worldPos);
	}

	public void remove(CommonPb.WorldEntity worldEntity) {
		if (worldEntity.getEntityType() != 1) {
			return;
		}
		WorldPos worldPos = new WorldPos(worldEntity.getPos());
		if (worldPosMap.containsKey(worldPos.getPosValue())) {
			worldPosMap.remove(worldPos.getPosValue());
		}
	}

	public void remove(int x, int y) {
		WorldPos worldPos = new WorldPos(x, y);
		if (worldPosMap.containsKey(worldPos.getPosValue())) {
			worldPosMap.remove(worldPos.getPosValue());
		}
	}


	public List<WorldPos> getPosList(int mapId, int level) {
		return worldPosMap.values().stream().filter(e -> e.getLevel() == level && e.getMapId() == mapId && !e.isAttack()).collect(Collectors.toList());
	}


}
