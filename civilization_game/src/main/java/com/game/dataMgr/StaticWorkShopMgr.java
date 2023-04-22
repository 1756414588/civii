package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticPeople;
import com.game.domain.s.StaticWorkShop;
import com.game.domain.s.StaticWorkShopBuy;
import com.game.util.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@LoadData(name = "商店")
public class StaticWorkShopMgr extends BaseDataMgr {
	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticWorkShop> workShopMap = new HashMap<Integer, StaticWorkShop>();
	private Map<Integer, StaticWorkShopBuy> workShopBuyMap = new HashMap<Integer, StaticWorkShopBuy>();
	private Map<Integer, Integer> qualityMap = new HashMap<Integer, Integer>();
    private Map<Integer, StaticPeople> staticPeopleMap = new HashMap<Integer, StaticPeople>();

    private static final int MAX_LEVEL = 150;

	@Override
	public void load() throws Exception {
		workShopMap = staticDataDao.selectWorkShopMap();
		workShopBuyMap = staticDataDao.selectWorkShopBuyMap();
		staticPeopleMap = staticDataDao.selectPeopleMap();
		qualityMap.clear();
		makeQualityMap();
	}

	@Override
	public void init() throws Exception{
	}

	public Map<Integer, StaticWorkShop> getWorkShopMap() {
		return workShopMap;
	}

	public Map<Integer, StaticWorkShopBuy> getWorkShopBuyMap() {
		return workShopBuyMap;
	}

	public StaticWorkShop getWorkShop(int workShopLv) {
		return workShopMap.get(workShopLv);
	}

	public StaticWorkShopBuy getWorkShopBuy(int buyTimes) {
		return workShopBuyMap.get(buyTimes);
	}

	public void makeQualityMap() {
		for (Map.Entry<Integer, StaticWorkShop> entry : workShopMap.entrySet()) {
			if (entry != null && entry.getValue() != null && entry.getKey() != null) {
				getQualityMap().put(entry.getValue().getQuality(), entry.getKey());
			}
		}
	}

	public Map<Integer, Integer> getQualityMap() {
		return qualityMap;
	}

	public void setQualityMap(Map<Integer, Integer> qualityMap) {
		this.qualityMap = qualityMap;
	}

    public Map<Integer, StaticPeople> getStaticPeopleMap () {
        return staticPeopleMap;
    }

    public void setStaticPeopleMap (Map<Integer, StaticPeople> staticPeopleMap) {
        this.staticPeopleMap = staticPeopleMap;
    }

    public int getBasePeople(int commandLv) {
        StaticPeople staticPeople = staticPeopleMap.get(commandLv);
        if (staticPeople == null) {
            return 0;
        }
        return staticPeople.getBase();
    }

    public int getLimitPeople(int commandLv) {
        StaticPeople staticPeople = staticPeopleMap.get(commandLv);
        if (staticPeople == null) {
            return 0;
        }
        return staticPeople.getLimit();
    }

    public int getCommandLevel() {
        if (workShopMap == null) {
            LogHelper.CONFIG_LOGGER.error("workshop is null!");
            return MAX_LEVEL;
        }

        StaticWorkShop staticWorkShop =  workShopMap.get(1);
        if (staticWorkShop == null) {
            LogHelper.CONFIG_LOGGER.error("staticWorkShop is null!");
            return MAX_LEVEL;
        }

        return staticWorkShop.getCommandLv();
    }

    public int getLordLevel() {
        if (workShopMap == null) {
            LogHelper.CONFIG_LOGGER.error("workshop is null!");
            return MAX_LEVEL;
        }

        StaticWorkShop staticWorkShop =  workShopMap.get(1);
        if (staticWorkShop == null) {
            LogHelper.CONFIG_LOGGER.error("staticWorkShop is null!");
            return MAX_LEVEL;
        }

        return staticWorkShop.getLordLv();
    }
}
