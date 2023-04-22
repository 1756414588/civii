package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.domain.p.ConfigException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticBattle;

@Component
@LoadData(name = "战斗数据")
public class StaticBattleDataMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticBattle> staticBattleMap = new HashMap<Integer, StaticBattle>();

    @Override
    public void load() throws Exception {
        staticBattleMap = staticDataDao.selectStaticBattle();

        //配置检测
        check();
    }

    @Override
    public void init() throws Exception {


    }

    public void check() throws ConfigException {
        StaticBattle staticBattle = getStaticBattle();
        if (staticBattle == null) {
            throw new ConfigException("staticBattle == null");
        }

        List<Integer> floatFactor = staticBattle.getFloatFactor();
        if (floatFactor.size() != 2) {
            throw new ConfigException("staticBattle floatFactor.size() != 2");
        }

        List<List<Integer>> restraintFactor = staticBattle.getRestraintFactor();
        if (restraintFactor.size() != 9) {
            throw new ConfigException("staticBattle restraintFactor.size() != 9");
        }

        for (List<Integer> item : restraintFactor) {
            if (item.size() < 3) {
                throw new ConfigException("staticBattle item.size() < 3");
            }

            if (item.get(1) < 1 || item.get(1) > 3) {
                throw new ConfigException("staticBattle soldier type error, soldier type = " + item.get(1));
            }

            if (item.get(2) < 1 || item.get(2) > 3) {
                throw new ConfigException("staticBattle soldier type error, soldier type = " + item.get(2));
            }
        }
    }

    public int getFactor(int soldierTypeA, int soldierTypeB) {
        StaticBattle staticBattle = getStaticBattle();
        List<List<Integer>> restraintFactor = staticBattle.getRestraintFactor();
        for (List<Integer> item : restraintFactor) {
            if (item.size() < 3) {
                continue;
            }

            if (item.get(1) == soldierTypeA && item.get(2) == soldierTypeB) {
                return item.get(0);
            }
        }

        return 1000;
    }

    public StaticBattle getStaticBattle() {
        return staticBattleMap.get(1);
    }

    public void setStaticBattleMap(Map<Integer, StaticBattle> staticBattleMap) {
        this.staticBattleMap = staticBattleMap;
    }
}
