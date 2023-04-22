package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticFortressBuild;
import com.game.domain.s.StaticFortressLv;
import com.game.domain.s.StaticSuperRes;

@Component
@LoadData(name = "超级矿")
public class StaticSuperResMgr extends BaseDataMgr {

    private List<StaticSuperRes> resList = new ArrayList<>();
    private Map<Integer, StaticSuperRes> resMap = new HashMap<>();
    private Map<Integer, StaticFortressLv> fortressLvMap = new HashMap<>();
    private Map<Integer, StaticFortressBuild> fortressBuildMap = new HashMap<>();

    @Autowired
    StaticDataDao staticDataDao;

    @Override
    public void load() throws Exception {
        this.resList = staticDataDao.querySuperRes();
        resList.forEach(x -> {
            resMap.put(x.getResId(), x);
        });
        this.fortressLvMap = staticDataDao.queryFortressLv();
        this.fortressBuildMap = staticDataDao.queryFortressBuild();
    }

    @Override
    public void init() {

    }

    public StaticSuperRes getStaticSuperRes(int resId) {
        return resMap.get(resId);
    }

    public StaticFortressLv getStaticCityDev(int lv) {
        return fortressLvMap.get(lv);
    }

    public StaticFortressBuild getFortressBuild(int lv) {
        return fortressBuildMap.get(lv);
    }

    public StaticSuperRes getSuperMineRandom() {
        Collections.shuffle(this.resList);
        return resList.get(0);
    }

}
