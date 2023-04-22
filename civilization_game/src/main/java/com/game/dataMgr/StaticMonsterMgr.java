package com.game.dataMgr;


import com.game.define.LoadData;
import java.util.HashMap;
import java.util.Map;

import com.game.util.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticMonster;

@Component
@LoadData(name = "怪物配置表")
public class StaticMonsterMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;


	private Map<Integer, StaticMonster> monsterMap = new HashMap<Integer, StaticMonster>();

	@Override
	public void load() throws Exception {
		monsterMap = staticDataDao.selectMonsterMap();
	}

	@Override
	public void init() throws Exception {
	}


	public StaticMonster getStaticMonster(int monsterId) {
		return monsterMap.get(monsterId);
	}


	public int getQuality(int id) {
		StaticMonster staticMonster = monsterMap.get(id);
		if (staticMonster == null) {
			LogHelper.CONFIG_LOGGER.error("StaticMonster is null, id:{}", id);
			return 0;
		}

		return staticMonster.getQuality();
	}


}
