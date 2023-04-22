package com.game.flame;

import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.Pos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class FlameMap extends MapInfo {

	private List<Pos> allPos = new ArrayList<>();
	private Map<Pos, Entity> node = new ConcurrentHashMap<Pos, Entity>();// 已经占用的坐标
	private Map<Integer, List<Pos>> safePos = new ConcurrentHashMap<>();// 安全区坐标
	private Map<Long, FlameWarCity> cityNode = new ConcurrentHashMap<>();// 地图城池
	private Map<Pos, FlameWarResource> resourceNode = new ConcurrentHashMap<>();// 地图矿点

	public void clear() {
		allPos.clear();
		node.clear();
		safePos.clear();
		cityNode.clear();
		resourceNode.clear();
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

	public void setAllPos(List<Pos> allPos) {
		this.allPos = allPos;
	}

	public Map<Pos, Entity> getNode() {
		return node;
	}

	public void setNode(Map<Pos, Entity> node) {
		this.node = node;
	}

	public void setCityNode(Map<Long, FlameWarCity> cityNode) {
		this.cityNode = cityNode;
	}

	public void setResourceNode(Map<Pos, FlameWarResource> resourceNode) {
		this.resourceNode = resourceNode;
	}

	public Map<Integer, List<Pos>> getSafePos() {
		return safePos;
	}

	public void setSafePos(Map<Integer, List<Pos>> safePos) {
		this.safePos = safePos;
	}

	public Entity getNode(Pos pos) {
		return node.get(pos);
	}

	public Map<Long, FlameWarCity> getCityNode() {
		return cityNode;
	}

	public Map<Pos, FlameWarResource> getResourceNode() {
		return resourceNode;
	}

	public synchronized Pos getSafePos(int country) {
		List<Pos> pos = this.safePos.get(country);
		Pos randomPos = pos.get(new Random().nextInt(pos.size()));
		if (node.containsKey(randomPos)) {
			return getSafePos(country);
		}
		return randomPos;
	}

	public synchronized void addNode(Entity node) {
		this.node.put(node.getPos(), node);
		NodeType nodeType = node.getNodeType();
		if (nodeType != null) {
			switch (nodeType) {
				// case Player:
				// playerCityMap.put(node.getId(), (FlamePlayerCity) node);
				// break;
				case Mine:
					resourceNode.put(node.getPos(), (FlameWarResource) node);
					break;
				case City:
					cityNode.put(node.getId(), (FlameWarCity) node);
					break;
			}
		}

	}

	public synchronized Entity removeNode(Entity node) {
		Entity remove = this.node.remove(node.getPos());
		NodeType nodeType = node.getNodeType();
		if (nodeType != null) {
			if (remove != null) {
				switch (node.getNodeType()) {
					// case Player:
					// playerCityMap.remove(node.getId());
					// break;
					case Mine:
						resourceNode.remove(node.getPos());
						break;
					case City:
						cityNode.remove(node.getId());
						break;
				}
			}
		}
		return remove;
	}

	public synchronized Pos getPos(int count) {
		if (count >= 30) {
			return null;
		}
		Pos randomPos = allPos.get(new Random().nextInt(allPos.size()));
		if (node.containsKey(randomPos)) {
			count++;
			return getPos(count);
		}
		return randomPos;

	}


}
