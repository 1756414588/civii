package com.game.domain.p;

import com.game.pb.DataPb;
import com.game.util.LogHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 城防军怪物
public class CityMonster {

	private int cityId;
	private long lastReoverTime;    // 下一次恢复的时间
	private Map<Integer, CityMonsterInfo> monsterInfoMap = new HashMap<Integer, CityMonsterInfo>();


	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public DataPb.CityMonsterData.Builder writeData() {
		DataPb.CityMonsterData.Builder builder = DataPb.CityMonsterData.newBuilder();
		builder.setCityId(cityId);
		for (CityMonsterInfo info : monsterInfoMap.values()) {
			if (info == null) {
				continue;
			}
			builder.addMonsterInfo(info.writeData());
		}

		builder.setLastReoverTime(lastReoverTime);

		return builder;
	}

	public void readData(DataPb.CityMonsterData builder) {
		cityId = builder.getCityId();
		for (DataPb.CityMonsterInfo info : builder.getMonsterInfoList()) {
			CityMonsterInfo elem = new CityMonsterInfo();
			elem.readData(info);
			monsterInfoMap.put(elem.getMonsterId(), elem);
		}

		lastReoverTime = builder.getLastReoverTime();
	}

	public void addMonster(int id, int soldier) {
		CityMonsterInfo cityMonsterInfo = new CityMonsterInfo();
		cityMonsterInfo.setMonsterId(id);
		cityMonsterInfo.setSoldier(soldier);
		cityMonsterInfo.setMaxSoldier(soldier);
		monsterInfoMap.put(id, cityMonsterInfo);
	}

	public void addMonster(CityMonsterInfo cityMonsterInfo) {
		monsterInfoMap.put(cityMonsterInfo.getMonsterId(), cityMonsterInfo);
	}


	public int getSoldier(int id) {
		CityMonsterInfo cityMonster = monsterInfoMap.get(id);
		if (cityMonster == null) {
			LogHelper.CONFIG_LOGGER.info("city monster id:{} not exists!  ", id);
			return 0;
		}

		return cityMonster.getSoldier();
	}

	public int getMaxSoldier(int id) {
		CityMonsterInfo cityMonster = monsterInfoMap.get(id);
		if (cityMonster == null) {
			LogHelper.CONFIG_LOGGER.info("city monster id:{} not exists!", id);
			return 0;
		}

		return cityMonster.getMaxSoldier();
	}


	public long getLastReoverTime() {
		return lastReoverTime;
	}

	public void setLastReoverTime(long lastReoverTime) {
		this.lastReoverTime = lastReoverTime;
	}

	public Map<Integer, CityMonsterInfo> getMonsterInfoMap() {
		return monsterInfoMap;
	}

	public void setMonsterInfoMap(Map<Integer, CityMonsterInfo> monsterInfoMap) {
		this.monsterInfoMap = monsterInfoMap;
	}

	public CityMonsterInfo getCityMonster(int monsterId) {
		return monsterInfoMap.get(monsterId);
	}

	public int getCitySoldier() {
		int total = 0;
		for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
			total += cityMonsterInfo.getSoldier();
		}

		return total;
	}

	public int getCityMaxSoldier() {
		int total = 0;
		for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
			total += cityMonsterInfo.getMaxSoldier();
		}

		return total;
	}

	public List<Integer> getMonsterIds() {
		List<Integer> monsters = new ArrayList<Integer>();
		for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
			monsters.add(cityMonsterInfo.getMonsterId());
		}
		return monsters;
	}

	public boolean isFullHp() {
		for (CityMonsterInfo cityMonsterInfo : monsterInfoMap.values()) {
			if (cityMonsterInfo.getSoldier() < cityMonsterInfo.getMaxSoldier()) {
				return false;
			}
		}

		return true;
	}

	public synchronized void clear() {
		monsterInfoMap.clear();
	}


}
