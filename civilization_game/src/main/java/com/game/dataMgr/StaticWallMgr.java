package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticWallMonsterLv;
import com.game.util.LogHelper;
import com.google.common.collect.HashBasedTable;

@Component
@LoadData(name = "城墙")
public class StaticWallMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;

	//    private List<StaticWallMonster> wallMonsters = new ArrayList<StaticWallMonster>();
	private Map<Integer, StaticWallMonsterLv> monsteLvs = new HashMap<Integer, StaticWallMonsterLv>();
	private HashBasedTable<Integer, Integer, Integer> monsterMapper = HashBasedTable.create();

	@Override
	public void load() throws Exception {
		monsteLvs = staticDataDao.selectWallMonsterLv();
		monsterMapper.clear();
		makeMapper();
	}

	@Override
	public void init() throws Exception {

	}

	public void makeMapper() {
		for (StaticWallMonsterLv monsterLv : monsteLvs.values()) {
			monsterMapper.put(monsterLv.getDefenceLv(), monsterLv.getQuality(), monsterLv.getId());
		}
	}

	// 获取当前城防军的属性
	public StaticWallMonsterLv getWallMonster(int level, int quality) {
		Integer id = monsterMapper.get(level, quality);
		if (id == null) {
			return null;
		}

		return monsteLvs.get(id);
	}

	public int getQuality(int id) {
		StaticWallMonsterLv staticWallMonsterLv = monsteLvs.get(id);
		if (staticWallMonsterLv == null) {
			LogHelper.CONFIG_LOGGER.error("staticWallMonsterLv is null! id:{}", id);
			return 0;
		}

		return staticWallMonsterLv.getQuality();
	}
}
