package com.game.dataMgr;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticJourney;
import com.game.domain.s.StaticJourneyPrice;

/**
*2020年8月17日
*@CaoBing
*halo_game
*StaticJourneyMgr.java
**/
@Component
public class StaticJourneyMgr extends BaseDataMgr {
	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticJourney> journeyMap = new HashMap<Integer, StaticJourney>();
	private Map<Integer, StaticJourneyPrice> journeyPriceMap = new HashMap<Integer, StaticJourneyPrice>();
	
	public void init() throws Exception{
		journeyMap = staticDataDao.selectStaticJourney();
		journeyPriceMap = staticDataDao.selectStaticJourneyPrice();
	}
	
	public StaticJourney getStaticJourney(int journeyId) {
		return journeyMap.get(journeyId);
	}
	
	public StaticJourneyPrice getStaticJourneyPrice(int journeyTimes) {
		return journeyPriceMap.get(journeyTimes);
	}
}
