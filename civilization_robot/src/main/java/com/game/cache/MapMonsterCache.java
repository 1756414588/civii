package com.game.cache;

import com.game.define.LoadData;
import com.game.domain.WorldPos;
import com.game.load.ILoadData;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.Pos;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @Description
 * @Date 2022/9/16 11:12
 **/

@LoadData(name = "地图", initSeq = 1001)
@Component
public class MapMonsterCache implements ILoadData {

	// mapId,pos,level
	private Map<Integer, Map<WorldPos, WorldPos>> monsterPosMap = new ConcurrentHashMap<>();

	@Autowired
	private StaticWorldMapCache staticWorldMapCache;


	@Override
	public void load() {
	}

	@Override
	public void init() {
		staticWorldMapCache.getWorldMaps().forEach((e, f) -> {
			Map<WorldPos, WorldPos> mapInfo = new ConcurrentHashMap<>();
			monsterPosMap.put(f.getMapId(), mapInfo);
		});
	}


	/**
	 * 野怪消失
	 *
	 * @param worldEntity
	 * @param pos
	 */
	public void synEntityRq(CommonPb.WorldEntity worldEntity, Pos pos) {
		if (worldEntity.getEntityType() != 1) {
			return;
		}

		WorldPos worldPos = new WorldPos(pos.getX(), pos.getY(), worldEntity.getLevel());
		int mapId = staticWorldMapCache.getMapId(pos.getX(), pos.getY());

		Map<WorldPos, WorldPos> mapInfo = monsterPosMap.get(mapId);

		if (mapInfo.containsKey(worldPos)) {
			mapInfo.remove(worldPos);
		}
	}

	/**
	 * 添加野怪信息
	 *
	 * @param worldEntity
	 */
	public void entityAdd(CommonPb.WorldEntity worldEntity) {
		if (worldEntity.getEntityType() != 1) {
			return;
		}

		int x = worldEntity.getPos().getX();
		int y = worldEntity.getPos().getY();

		WorldPos worldPos = new WorldPos(x, y, worldEntity.getLevel());
		int mapId = staticWorldMapCache.getMapId(x, y);
		Map<WorldPos, WorldPos> mapInfo = monsterPosMap.get(mapId);

		if (mapInfo.containsKey(worldPos)) {
			return;
		}

		mapInfo.put(worldPos, worldPos);
	}

	/**
	 * 野怪更新
	 *
	 * @param worldEntity
	 */
	public void synEntityUpdateRq(CommonPb.WorldEntity worldEntity) {
		if (worldEntity.getEntityType() != 1) {
			return;
		}
		int x = worldEntity.getPos().getX();
		int y = worldEntity.getPos().getY();

		WorldPos worldPos = new WorldPos(x, y, worldEntity.getLevel());
		int mapId = staticWorldMapCache.getMapId(x, y);
		Map<WorldPos, WorldPos> mapInfo = monsterPosMap.get(mapId);

		mapInfo.put(worldPos, worldPos);
	}

	public List<WorldPos> getMonsterByLv(int mapId, int level) {
		Map<WorldPos, WorldPos> mapInfo = monsterPosMap.get(mapId);
		return mapInfo.values().stream().filter(e -> e.getLevel() == level).collect(Collectors.toList());
	}


	public Map<WorldPos, WorldPos> getMapPos(int mapId) {
		return monsterPosMap.get(mapId);
	}

	public Map<Integer, Map<WorldPos, WorldPos>> getMonsterPosMap() {
		return monsterPosMap;
	}


}
