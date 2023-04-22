package com.game.dataMgr;

import java.util.HashMap;
import java.util.Map;

import com.game.domain.p.ConfigException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticBuySolodierTime;
import com.game.domain.s.StaticCapacityTimes;
import com.game.domain.s.StaticSoldierLv;

@Component
public class StaticSoldierMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticSoldierLv> soldierLvMap = new HashMap<Integer, StaticSoldierLv>();
    private Map<Integer, StaticBuySolodierTime> buySoldierTime = new HashMap<Integer, StaticBuySolodierTime>();
    private Map<Integer, StaticCapacityTimes> capacityTimesMap = new HashMap<Integer, StaticCapacityTimes>();  // 后续优化


    @Override
    public void init() throws Exception{
        soldierLvMap      = staticDataDao.selectStaticSoldierLv();
        buySoldierTime    = staticDataDao.selectStaticBuySolodierTime();
        capacityTimesMap  = staticDataDao.selectStaticCapacityTimesMap();
        check();
    }

    //配置检查
    public void check() throws ConfigException {
        if(soldierLvMap.get(1) == null) {
            throw new ConfigException("soldierLvMap.get(1) == null");
        }
    }

    public Map<Integer, StaticSoldierLv> getSoldierLvMap() {
        return soldierLvMap;
    }

    public void setSoldierLvMap(Map<Integer, StaticSoldierLv> soldierLvMap) {
        this.soldierLvMap = soldierLvMap;
    }


    public Map<Integer, StaticBuySolodierTime> getBuySoldierTime() {
        return buySoldierTime;
    }

    public void setBuySoldierTime(Map<Integer, StaticBuySolodierTime> buySoldierTime) {
        this.buySoldierTime = buySoldierTime;
    }

    public StaticSoldierLv getSoldierLv(int soldierLv) {
        return soldierLvMap.get(soldierLv);
    }

    public Map<Integer, StaticCapacityTimes> getCapacityTimesMap () {
        return capacityTimesMap;
    }

    public void setCapacityTimesMap (Map<Integer, StaticCapacityTimes> capacityTimesMap) {
        this.capacityTimesMap = capacityTimesMap;
    }
}
