package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.HashMap;
import java.util.Map;

import com.game.domain.s.StaticLost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticLordLv;

@Component
@LoadData(name = "角色相关配置")
public class StaticLordDataMgr extends BaseDataMgr {
	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticLordLv> lordLvMap;

	private Map<Integer, StaticLost> lostList = new HashMap<Integer, StaticLost>();


	@Override
	public void load() throws Exception {
		lordLvMap = staticDataDao.selectLordLv();
	}

	@Override
	public void init() throws Exception{

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
