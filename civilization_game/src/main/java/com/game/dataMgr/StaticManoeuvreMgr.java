package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.Award;
import com.game.domain.s.StaticManoeuvreMatch;
import com.game.domain.s.StaticManoeuvreRankAward;
import com.game.domain.s.StaticManoeuvreShop;
import com.game.worldmap.fight.manoeuvre.ManoeuvreConst;
import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class StaticManoeuvreMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;
	@Getter
	private Map<Integer, StaticManoeuvreShop> shops = new HashMap<>();
	@Getter
	private Map<Integer, List<StaticManoeuvreRankAward>> awardMap = new HashMap<>();
	@Getter
	private Map<Integer, List<StaticManoeuvreMatch>> matchMap = new HashMap<>();
	private HashBasedTable<Integer, Integer, List<Award>> rankAwardMap = HashBasedTable.create();
	private int max_person_killer = 0;

	@Override
	public void init() throws Exception {
		// 匹配
		List<StaticManoeuvreMatch> matchList = staticDataDao.loadStaticManoeuvreMatch();
		matchMap = matchList.stream().collect(Collectors.groupingBy(e -> e.getSort()));

		// 商店
		List<StaticManoeuvreShop> staticManoeuvreShopList = staticDataDao.loadStaticManoeuvreShop();
		shops = staticManoeuvreShopList.stream().collect(Collectors.toMap(StaticManoeuvreShop::getId, Function.identity()));

		// 奖励
		List<StaticManoeuvreRankAward> staticManoeuvreRankAwardList = staticDataDao.loadStaticManoeuvreRankAward();
		awardMap = staticManoeuvreRankAwardList.stream().collect(Collectors.groupingBy(e -> e.getType()));
		initRankAward(staticManoeuvreRankAwardList);
	}

	private void initRankAward(List<StaticManoeuvreRankAward> awardList) {
		for (StaticManoeuvreRankAward e : awardList) {
			int type = e.getType();
			int param = e.getParam();
			if (type == ManoeuvreConst.TYPE_RANK_PERSON) {
				if (param > max_person_killer) {
					max_person_killer = param;
				}
			}

			List<Award> awards = new ArrayList<>();
			for (List<Integer> list : e.getAwardList()) {
				Award award = new Award();
				award.setType(list.get(0));
				award.setId(list.get(1));
				award.setCount(list.get(2));
				awards.add(award);
			}
			rankAwardMap.put(type, param, awards);
		}
	}


	public List<StaticManoeuvreMatch> getNewCourse() {
		List<StaticManoeuvreMatch> result = new ArrayList<>();
		matchMap.values().forEach(e -> {
			int random = new Random().nextInt(2);
			result.add(e.get(random));
		});
		Collections.shuffle(result);
		return result;
	}

	public List<Award> getAward(int type, int param) {
		if (type == ManoeuvreConst.TYPE_RANK_PERSON) {
			if (param > max_person_killer) {
				param = max_person_killer;
			}
		}
		if (rankAwardMap.contains(type, param)) {
			return rankAwardMap.get(type, param);
		}
		return null;
	}
}
