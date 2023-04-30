package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticJourney;
import com.game.domain.s.StaticJourneyPrice;

/**
*2020年8月17日
*
*halo_game
*StaticJourneyMgr.java
**/
@Component
@LoadData(name = "远征")
public class StaticJourneyMgr extends BaseDataMgr {
	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticJourney> journeyMap = new HashMap<Integer, StaticJourney>();
	private Map<Integer, StaticJourneyPrice> journeyPriceMap = new HashMap<Integer, StaticJourneyPrice>();

	@Override
	public void load() throws Exception {
		journeyMap = staticDataDao.selectStaticJourney();
		journeyPriceMap = staticDataDao.selectStaticJourneyPrice();
	}

	public void init() throws Exception{

	}
	
	public StaticJourney getStaticJourney(int journeyId) {
		return journeyMap.get(journeyId);
	}
	
	public StaticJourneyPrice getStaticJourneyPrice(int journeyTimes) {
		return journeyPriceMap.get(journeyTimes);
	}
}
