package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticBulletWarLevel;
import com.game.domain.s.StaticEndlessArmory;
import com.game.domain.s.StaticEndlessAward;
import com.game.domain.s.StaticEndlessBaseinfo;
import com.game.domain.s.StaticEndlessItem;
import com.game.domain.s.StaticEndlessLevel;
import com.game.domain.s.StaticEndlessTDDropLimit;
import com.game.domain.s.StaticTowerWarBonus;
import com.game.domain.s.StaticTowerWarLevel;
import com.game.domain.s.StaticTowerWarMap;
import com.game.domain.s.StaticTowerWarMonster;
import com.game.domain.s.StaticTowerWarTower;
import com.game.domain.s.StaticTowerWarWave;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @Description
 * @Date 2023/4/6 17:21
 **/

@Component
@LoadData(name = "塔防配置")
public class StaticTDMgr extends BaseDataMgr {

	private Map<Integer, StaticTowerWarMap> towerWarMapMap = new HashMap<>(); // 地图
	private Map<Integer, StaticTowerWarLevel> towerWarLevelMap = new HashMap<>(); // 关卡
	private Map<Integer, StaticTowerWarWave> towerWarWaveMap = new HashMap<>(); // 波次
	private Map<Integer, StaticTowerWarMonster> towerWarMonsterMap = new HashMap<>(); // 敌人
	private Map<Integer, StaticTowerWarTower> towerWarTowerMap = new HashMap<>(); // 炮塔
	private Map<Integer, StaticTowerWarBonus> towerWarBonusMap = new HashMap<>(); // 战力加成
	private Map<Integer, StaticTowerWarBonus> endlessTowerWarBonusMap = new HashMap<>(); // 无尽炮塔战力加成
	private Map<Integer, StaticEndlessArmory> endlessShopMap = new HashMap<>(); // 兑换商店
	private Map<Integer, StaticEndlessArmory> endlessArmoryMap = new HashMap<>(); // 军械商店
	private List<StaticEndlessAward> endlessAwardList = new ArrayList<>(); // 无尽塔防排行奖励
	private Map<Integer, StaticEndlessBaseinfo> endlessBaseInfoMap = new HashMap<>(); // 无尽塔防基本数据
	private Map<Integer, StaticEndlessLevel> endlessLevelMap = new HashMap<>(); // 无尽塔防关卡数据
	private Map<Integer, StaticEndlessItem> endlessItemMap = new HashMap<>(); // 无尽塔道具数据
	private Map<Integer, StaticEndlessTDDropLimit> endlessTDDropLimitMap = new HashMap<>();// 无尽塔防限制信息
	private Map<Integer, StaticTowerWarTower> tdEndlessTowerMap = new HashMap<>();// 无尽塔防炮塔信息
	private Map<Integer, StaticTowerWarMonster> tdEndlessMonsterMap = new HashMap<>();// 无尽塔敌人塔信息
	private Map<Integer, StaticTowerWarWave> tdEndlessWaveMap = new HashMap<>();// 无尽塔波次

	private Map<Integer, StaticBulletWarLevel> staticBulletWarLevelMap = new HashMap<>();

	@Autowired
	private StaticDataDao staticDataDao;

	@Override
	public void load() throws Exception {
		clearIni();
		towerWarMapMap = staticDataDao.loadAllStaticTowerWarMap();
		towerWarLevelMap = staticDataDao.loadAllStaticTowerWarLevel();
		towerWarWaveMap = staticDataDao.loadStaticTowerWarWave();
		towerWarBonusMap = staticDataDao.loadStaticTowerWarBonus();
		staticDataDao.loadAllStaticTowerWarMonster().forEach(e -> {
			towerWarMonsterMap.put(e.getId(), e);
		});
		staticDataDao.loadStaticTowerWarTower().forEach(e -> {
			towerWarTowerMap.put(e.getId(), e);
		});
		endlessTowerWarBonusMap = staticDataDao.loadStaticEndlessBonus();
		endlessShopMap = staticDataDao.loadStaticEndlessShop();
		endlessArmoryMap = staticDataDao.loadStaticEndlessArmory();
		endlessAwardList = staticDataDao.loadStaticEndlessAward();
		endlessBaseInfoMap = staticDataDao.loadStaticEndlessBaseInfo();
		endlessLevelMap = staticDataDao.loadStaticEndlessLevel();
		endlessItemMap = staticDataDao.loadStaticEndlessItem();
		endlessTDDropLimitMap = staticDataDao.loadEndlessTDDropLimit();
		tdEndlessTowerMap = staticDataDao.loadEndlessTower();
		tdEndlessMonsterMap = staticDataDao.loadEndlessMonster();
		tdEndlessWaveMap = staticDataDao.loadEndlessWave();

		this.staticBulletWarLevelMap = staticDataDao.loadBulletWar();
	}

	@Override
	public void init() throws Exception {
	}

	public void clearIni() {
		towerWarMapMap.clear();
		towerWarLevelMap.clear();
		towerWarWaveMap.clear();
		towerWarMonsterMap.clear();
		towerWarTowerMap.clear();
		towerWarBonusMap.clear();
		endlessTowerWarBonusMap.clear();
		endlessShopMap.clear();
		endlessArmoryMap.clear();
		endlessAwardList.clear();
		endlessBaseInfoMap.clear();
		endlessLevelMap.clear();
		endlessItemMap.clear();
		endlessTDDropLimitMap.clear();
		tdEndlessTowerMap.clear();
		tdEndlessMonsterMap.clear();
		tdEndlessWaveMap.clear();
	}


	public Map<Integer, StaticTowerWarWave> getTdEndlessWaveMap() {
		return tdEndlessWaveMap;
	}

	public StaticTowerWarWave getTdEndlessWave(int id) {
		return getTdEndlessWaveMap().get(id);
	}

	public Map<Integer, StaticTowerWarMonster> getTdEndlessMonsterMap() {
		return tdEndlessMonsterMap;
	}

	public StaticTowerWarMonster getTdEndlessMonster(int id) {
		return getTdEndlessMonsterMap().get(id);
	}

	public Map<Integer, StaticEndlessLevel> getEndlessLevelMap() {
		return endlessLevelMap;
	}

	public StaticEndlessLevel getEndlessLevel(int id) {
		return getEndlessLevelMap().get(id);
	}

	public void setEndlessLevelMap(Map<Integer, StaticEndlessLevel> endlessLevelMap) {
		this.endlessLevelMap = endlessLevelMap;
	}

	public Map<Integer, StaticTowerWarTower> getTdEndlessTower() {
		return tdEndlessTowerMap;
	}

	public StaticEndlessTDDropLimit getEndlessTDDropLimit(int id) {
		return endlessTDDropLimitMap.get(id);
	}

	public Map<Integer, StaticTowerWarMap> getTowerWarMapMap() {
		return towerWarMapMap;
	}

	public Map<Integer, StaticTowerWarLevel> getTowerWarLevelMap() {
		return towerWarLevelMap;
	}

	public StaticTowerWarLevel getTowerWarLevel(int levelId) {
		return this.towerWarLevelMap.get(levelId);
	}

	public Map<Integer, StaticTowerWarWave> getTowerWarWaveMap() {
		return towerWarWaveMap;
	}

	public StaticTowerWarWave getTowerWarWave(int waveId) {
		return towerWarWaveMap.get(waveId);
	}

	public Map<Integer, StaticTowerWarMonster> getTowerWarMonsterMap() {
		return towerWarMonsterMap;
	}

	public StaticTowerWarMonster getTowerWarMonster(int monsteId) {
		return towerWarMonsterMap.get(monsteId);
	}

	public Map<Integer, StaticTowerWarTower> getTowerWarTowerMap() {
		return towerWarTowerMap;
	}

	public Map<Integer, StaticTowerWarBonus> getTowerWarBonusMap() {
		return towerWarBonusMap;
	}

	public void setTowerWarBonusMap(Map<Integer, StaticTowerWarBonus> towerWarBonusMap) {
		this.towerWarBonusMap = towerWarBonusMap;
	}

	public StaticTowerWarBonus getTowerWarBonusMap(Integer id) {
		return towerWarBonusMap.get(id);
	}

	public Map<Integer, StaticTowerWarBonus> getEndlessTowerWarBonusMap() {
		return endlessTowerWarBonusMap;
	}

	public StaticTowerWarBonus getEndlessTowerWarBonusMap(Integer id) {
		return endlessTowerWarBonusMap.get(id);
	}

	public void setEndlessTowerWarBonusMap(Map<Integer, StaticTowerWarBonus> endlessTowerWarBonusMap) {
		this.endlessTowerWarBonusMap = endlessTowerWarBonusMap;
	}

	public Map<Integer, StaticEndlessArmory> getEndlessArmoryMap() {
		return endlessArmoryMap;
	}

	public StaticEndlessArmory getEndlessArmory(int id) {
		return endlessArmoryMap.get(id);
	}

	public Map<Integer, StaticEndlessArmory> getEndlessShopMap() {
		return endlessShopMap;
	}

	public StaticEndlessArmory getEndlessShopMap(int id) {
		return endlessShopMap.get(id);
	}

	public List<StaticEndlessAward> getEndlessAwardList() {
		return endlessAwardList;
	}

	public StaticEndlessBaseinfo getEndlessBaseInfo() {
		return endlessBaseInfoMap.get(1);
	}

	public StaticEndlessItem getEndlessItem(int id) {
		return endlessItemMap.get(id);
	}

	public Map<Integer, StaticEndlessItem> getEndlessItemMap() {
		return endlessItemMap;
	}

	public StaticBulletWarLevel getStaticBulletWarLevel(int level) {
		return staticBulletWarLevelMap.get(level);
	}

	public Map<Integer, StaticBulletWarLevel> getAllStaticBulletWarLevel() {
		return this.staticBulletWarLevelMap;
	}
}
