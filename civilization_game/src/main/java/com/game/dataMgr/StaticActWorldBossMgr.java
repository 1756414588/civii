package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticActWorldBoss;
import com.game.domain.s.StaticLairRank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @date 2019/12/26 14:21
 * @description
 */
@Component
@LoadData(name = "世界boss配置")
public class StaticActWorldBossMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;
	private Map<Integer, StaticActWorldBoss> boss;
	private Map<Integer, List<StaticLairRank>> lairRank = new HashMap<>();
	private List<StaticLairRank> lairRankList;


	@Override
	public void load() {
		boss = staticDataDao.selectActWorldBoss();
		lairRankList = staticDataDao.getStaticLairRank();
		lairRankList.forEach(x -> {
			List<StaticLairRank> staticLairRanks = lairRank.computeIfAbsent(x.getType(), a -> new ArrayList<>());
			staticLairRanks.add(x);
		});
	}

	@Override
	public void init() throws Exception {
	}

	public StaticActWorldBoss getStaticActWorldBoss(int id) {
		return boss.get(id);
	}

	public List<StaticLairRank> getLairRankList(int type) {
		return lairRank.get(type);
	}


}
