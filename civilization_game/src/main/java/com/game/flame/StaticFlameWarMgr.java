package com.game.flame;

import com.game.dao.s.StaticDataDao;
import com.game.dataMgr.BaseDataMgr;
import com.game.flame.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StaticFlameWarMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;

	private StaticFlameMap staticFlameMap;

	private Map<Integer, StaticFlameBuild> flameBuildMap = new HashMap<>();

	private Map<Integer, StaticFlameBuff> flameBuffMap = new LinkedHashMap<>();

	private Map<Integer, StaticFlameBuff> nextflameBuffMap = new HashMap<>();

	private Map<Integer, StaticFlameShop> flameShopMap = new HashMap<>();

	private List<StaticFlameSafe> flameSafe = new ArrayList<>();

	private Map<Integer, StaticFlameMine> flameMineMap = new HashMap<>();

	private List<StaticFlameFlushMine> flameFlushMineMap = new ArrayList<>();

	private List<StaticFlameRankGear> flameRankGears = new ArrayList<>();

	private Map<Integer, StaticFlameRankCamp> flameRankCamps = new HashMap<>();

	private List<StaticFlameKill> flameKills = new ArrayList<>();

	@Override
	public void init() throws Exception {
		// staticFlameMap = staticDataDao.loadFlameMap();
		flameBuildMap = staticDataDao.loadFlameBuild();
		flameBuffMap = staticDataDao.loadFlameBuff();
		flameShopMap = staticDataDao.loadFlameShop();
		flameBuffMap.values().forEach(x -> {
			nextflameBuffMap.put(x.getNextBuffId(), x);
		});
		flameSafe = staticDataDao.loadFlameSafe();
		flameMineMap = staticDataDao.loadFlameMin();
		flameFlushMineMap = staticDataDao.loadFlameMinFlush();
		flameRankGears = staticDataDao.loadFlameRankGears();
		flameRankCamps = staticDataDao.loadFlameRankCamp();
		flameKills = staticDataDao.loadFlameKill();
	}

	public StaticFlameMap getStaticFlameMap() {
		return staticFlameMap;
	}

	public void setStaticFlameMap(StaticFlameMap staticFlameMap) {
		this.staticFlameMap = staticFlameMap;
	}

	public Map<Integer, StaticFlameBuild> getFlameBuildMap() {
		return flameBuildMap;
	}

	public void setFlameBuildMap(Map<Integer, StaticFlameBuild> flameBuildMap) {
		this.flameBuildMap = flameBuildMap;
	}

	public Map<Integer, StaticFlameBuff> getFlameBuffMap() {
		return flameBuffMap;
	}

	public void setFlameBuffMap(Map<Integer, StaticFlameBuff> flameBuffMap) {
		this.flameBuffMap = flameBuffMap;
	}

	public Map<Integer, StaticFlameShop> getFlameShopMap() {
		return flameShopMap;
	}

	public void setFlameShopMap(Map<Integer, StaticFlameShop> flameShopMap) {
		this.flameShopMap = flameShopMap;
	}

	public List<StaticFlameSafe> getFlameSafe() {
		return flameSafe;
	}

	public void setFlameSafe(List<StaticFlameSafe> flameSafe) {
		this.flameSafe = flameSafe;
	}

	public StaticFlameBuff getBuffById(int id) {

		return flameBuffMap.get(id);
	}

	public StaticFlameBuff getBuffByNextId(int nextId) {
		return nextflameBuffMap.get(nextId);
	}

	public StaticFlameShop getStaticFlameShop(int keyId) {
		return flameShopMap.get(keyId);
	}

	public StaticFlameBuild getStaticFlameBuild(long id) {
		return flameBuildMap.get((int) id);
	}

	public StaticFlameMine getStaticFlameMine(int id) {
		return flameMineMap.get(id);
	}

	public Map<Integer, StaticFlameBuff> getNextflameBuffMap() {
		return nextflameBuffMap;
	}

	public void setNextflameBuffMap(Map<Integer, StaticFlameBuff> nextflameBuffMap) {
		this.nextflameBuffMap = nextflameBuffMap;
	}

	public Map<Integer, StaticFlameMine> getFlameMineMap() {
		return flameMineMap;
	}

	public void setFlameMineMap(Map<Integer, StaticFlameMine> flameMineMap) {
		this.flameMineMap = flameMineMap;
	}

	public List<StaticFlameFlushMine> getFlameFlushMineMap() {
		return flameFlushMineMap;
	}

	public void setFlameFlushMineMap(List<StaticFlameFlushMine> flameFlushMineMap) {
		this.flameFlushMineMap = flameFlushMineMap;
	}

	public StaticFlameFlushMine getFlameFlushMine(int id) {
		return flameFlushMineMap.get(id);
	}


	// public List<StaticFlameRankGear> getFlameRankGears() {
	// return flameRankGears;
	// }

	public void setFlameRankGears(List<StaticFlameRankGear> flameRankGears) {
		this.flameRankGears = flameRankGears;
	}

	public StaticFlameRankCamp getStaticFlameRankCamp(int rank) {
		return flameRankCamps.get(rank);
	}

	public StaticFlameRankGear getFlameRankGears(long cond) {
		for (int i = flameRankGears.size() - 1; i >= 0; i--) {
			StaticFlameRankGear staticFlameRankGear = flameRankGears.get(i);
			if (cond > staticFlameRankGear.getGear()) {
				return staticFlameRankGear;
			}
		}
		return null;
	}


	public long getStaticFlameKill(long cond) {
		if (!flameKills.isEmpty()) {
			StaticFlameKill staticFlameKill = flameKills.get(0);
			long l = cond / staticFlameKill.getCond();
			return l * staticFlameKill.getRes();
		}
		return 0L;

	}
}
