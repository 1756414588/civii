package com.game.flame;

import com.game.util.RandomUtil;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Pos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FlameMap extends MapInfo {

	private List<Pos> allPos = new ArrayList<>();
	private Map<Integer, List<Pos>> safePos = new ConcurrentHashMap<>();// 安全区坐标
	private Map<Long, FlameWarCity> cityNode = new ConcurrentHashMap<>();// 地图城池
	private Map<Pos, FlameWarResource> resourceNode = new ConcurrentHashMap<>();// 地图矿点

	private ConcurrentLinkedQueue<Pos> freeList = new ConcurrentLinkedQueue<>();

	public void clear() {
		allPos.clear();
		safePos.clear();
		cityNode.clear();
		resourceNode.clear();
		freeList.clear();
		posTake.clear();
		getPlayerCityMap().clear();
		getWarMap().clear();
	}

	public FlameMap() {
	}

	public FlameMap(int mapId) {
		super(mapId);
	}

	public List<Pos> getAllPos() {
		return allPos;
	}

	public Map<Pos, Entity> getNode() {
		return posTake;
	}

	public Map<Integer, List<Pos>> getSafePos() {
		return safePos;
	}


	public Entity getNode(Pos pos) {
		return posTake.get(pos);
	}

	public Map<Long, FlameWarCity> getCityNode() {
		return cityNode;
	}

	public Map<Pos, FlameWarResource> getResourceNode() {
		return resourceNode;
	}

	public Pos getSafePos(int country) {
		List<Pos> safeList = this.safePos.get(country);

		List<Pos> posList = new ArrayList<>();
		for (Pos pos : safeList) {
			if (posTake.containsKey(pos)) {
				continue;
			}
			posList.add(pos);
		}

		// 安全区已占满,则到地图上随机占领一个
		if (posList.isEmpty()) {
			return getPos(30);
		}

		int size = posList.size();
		int index = RandomUtil.getRandomNumber(size);
		return posList.get(index);
	}

	@Override
	public boolean addPos(Pos pos, Entity node) {
		Entity entity = posTake.putIfAbsent(pos, node);
		if (entity != null) {
			return false;
		}

		// 下面是添加成功后的逻辑
		if (freeList.contains(pos)) {
			freeList.remove(pos);
		}

		NodeType nodeType = node.getNodeType();
		if (nodeType == null) {
			return true;
		}
		switch (nodeType) {
			case Mine:
				resourceNode.put(pos, (FlameWarResource) node);
				break;
			case City:
				cityNode.put(node.getId(), (FlameWarCity) node);
				break;
		}
		return true;
	}

	@Override
	public void removePos(Pos pos) {
		Entity entity = posTake.remove(pos);
		if (entity == null) {
			return;
		}
		NodeType nodeType = entity.getNodeType();
		if (nodeType == null) {
			return;
		}
		switch (entity.getNodeType()) {
			case Mine:
				resourceNode.remove(entity.getPos());
				break;
			case City:
				cityNode.remove(entity.getId());
				break;
		}
	}

	public Pos getPos(int count) {

		count = count < 1 ? 1 : count;
		count = count > 30 ? 30 : count;

		Queue<Pos> posQueue = getEmptyList();

		int c = 0;
		do {
			if (posQueue.isEmpty()) {
				return new Pos();
			}
			Pos pos = posQueue.poll();
			if (pos == null || pos.isError()) {
				return new Pos();
			}
			// 坐标未被占用
			if (!posTake.containsKey(pos)) {
				return pos;
			}
		} while (c++ < count);
		return new Pos();
	}

	public Queue<Pos> getEmptyList() {

		if (!freeList.isEmpty()) {
			return freeList;
		}

		List<Pos> tempList = new ArrayList<>();
		for (Pos pos : allPos) {
			if (posTake.containsKey(pos)) {
				continue;
			}
			tempList.add(pos);
		}

		Collections.shuffle(tempList);
		for (Pos pos : tempList) {
			freeList.add(pos);
		}
		return freeList;
	}

	@Override
	public Pos randPickPos() {
		return getPos(1);
	}

}
