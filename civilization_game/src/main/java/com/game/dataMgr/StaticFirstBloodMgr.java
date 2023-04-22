package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticFirstBloodAward;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyue
 */
@Component
@LoadData(name = "首杀")
public class StaticFirstBloodMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticFirstBloodAward> staticFirstBloodAwardMap = new HashMap<>();

    @Override
    public void load() throws Exception {
        initFirstBloodAward();
    }

    @Override
    public void init() throws Exception {
    }

    public void initFirstBloodAward() {
        if (staticFirstBloodAwardMap.size() > 0) {
            staticFirstBloodAwardMap.clear();
        }
        staticFirstBloodAwardMap.putAll(staticDataDao.loadStaticFirstBloodAward());
    }

    public Map<Integer, StaticFirstBloodAward> getStaticFirstBloodAwardMap() {
        return staticFirstBloodAwardMap;
    }

    public void setStaticFirstBloodAwardMap(Map<Integer, StaticFirstBloodAward> staticFirstBloodAwardMap) {
        this.staticFirstBloodAwardMap = staticFirstBloodAwardMap;
    }
}
