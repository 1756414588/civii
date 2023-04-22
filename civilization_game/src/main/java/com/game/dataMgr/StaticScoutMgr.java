package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticScout;
import com.game.domain.s.StaticScoutLv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StaticScoutMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticScout> scoutMap = new HashMap<Integer, StaticScout>();

    public StaticDataDao getStaticDataDao() {
        return staticDataDao;
    }

    public void setStaticDataDao(StaticDataDao staticDataDao) {
        this.staticDataDao = staticDataDao;
    }

    public Map<Integer, StaticScout> getScoutMap() {
        return scoutMap;
    }

    public void setScoutMap(Map<Integer, StaticScout> scoutMap) {
        this.scoutMap = scoutMap;
    }

    public Map<Integer, StaticScoutLv> getScoutLvMap() {
        return scoutLvMap;
    }

    public void setScoutLvMap(Map<Integer, StaticScoutLv> scoutLvMap) {
        this.scoutLvMap = scoutLvMap;
    }

    private Map<Integer, StaticScoutLv> scoutLvMap = new HashMap<Integer, StaticScoutLv>();

    @Override
    public void init() throws Exception{
        scoutMap   = staticDataDao.selectScoutMap();
        scoutLvMap = staticDataDao.selectScoutLvMap();

    }

    public StaticScoutLv getScoutLv(int level) {
        return scoutLvMap.get(level);
    }

    public StaticScout getScout(int type) {
        return scoutMap.get(type);
    }

}
