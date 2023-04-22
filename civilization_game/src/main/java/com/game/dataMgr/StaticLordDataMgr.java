package com.game.dataMgr;

import java.util.HashMap;
import java.util.Map;

import com.game.domain.s.StaticLost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticLordLv;

@Component
public class StaticLordDataMgr extends BaseDataMgr {
	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticLordLv> lordLvMap;

	private Map<Integer, StaticLost> lostList = new HashMap<Integer, StaticLost>();


	@Override
	public void init() throws Exception{
		lordLvMap = staticDataDao.selectLordLv();
        //setLostList(staticDataDao.selectStaticLost());
	}

	public int maxLevel() {
		return lordLvMap.size();
	}

	public int getExp(int level) {
		StaticLordLv staticLordLv = lordLvMap.get(level);
		if (staticLordLv != null) {
			return staticLordLv.getNeedExp();
		}

		return Integer.MAX_VALUE;
	}

    public StaticLordLv getStaticLordLv(int level) {
        return lordLvMap.get(level);
    }

    public Map<Integer, StaticLost> getLostList() {
        return lostList;
    }

    public void setLostList(Map<Integer, StaticLost> lostList) {
        this.lostList = lostList;
    }
}
