package com.game.domain;

import com.game.pb.CommonPb.Pos;
import com.game.pb.CommonPb.WorldEntity;
import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class World {

	private int mapId;
	//玩家最高可打等级
	private int maxMonsterLv;

	// 世界实体
	private HashBasedTable<Integer, Integer, Entity> worldEntityMap = HashBasedTable.create();

	// 世界怪物最高等级
	private int monsterLv;

	public void synWorldEntity(WorldEntity worldEntity, int maxMonsterLv, Pos pos) {
		int x = pos.getX();
		int y = pos.getY();

		if (worldEntityMap.contains(x, y)) {
			worldEntityMap.remove(x, y);
		}

		this.maxMonsterLv = maxMonsterLv;
	}

	public void addEntity(WorldEntity worldEntity) {
		Entity entity = new Entity(worldEntity);
		int x = entity.getPos().getX();
		int y = entity.getPos().getY();
		if (worldEntity.getEntityType() == 1 && worldEntity.getLevel() > monsterLv) {
			monsterLv = worldEntity.getLevel();
		}
		worldEntityMap.put(x, y, entity);
	}

	public void updateEntity(WorldEntity worldEntity) {
//		worldEntity.getPos();
	}

	public Entity getEntity(Predicate<Entity> predicate) {
		if (worldEntityMap.isEmpty()) {
			return null;
		}

		List<Entity> list = worldEntityMap.values().stream().filter(predicate).collect(Collectors.toList());
		if (list == null || list.isEmpty()) {
			return null;
		}

		int index = new Random().nextInt(list.size());
		return list.get(index);
	}

	public List<Entity> getEntityList(Predicate<Entity> predicate) {
		if (worldEntityMap.isEmpty()) {
			return null;
		}

		List<Entity> list = worldEntityMap.values().stream().filter(predicate).collect(Collectors.toList());
		if (list == null || list.isEmpty()) {
			return new ArrayList<>();
		}
		return list;
	}
}


