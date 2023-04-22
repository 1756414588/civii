package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.dataMgr.BaseDataMgr;
import com.game.define.LoadData;
import com.game.domain.s.StaticCityGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 * @date 2020/10/29 18:28
 * @description
 */
@Component
@LoadData(name = "小游戏配置")
public class StaticCityGameMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao dataDao;

    private Map<Integer, StaticCityGame> cityGameMap;

    @Override
    public void load() throws Exception {
        cityGameMap = dataDao.loadStaticCityGame();
        if (cityGameMap == null || cityGameMap.size() == 0) {
            throw new Exception("主城小游戏配置不存在!");
        }
    }

    @Override
    public void init() throws Exception {
    }

    public Map<Integer, StaticCityGame> getCityGameMap() {
        return cityGameMap;
    }

    public void setCityGameMap(Map<Integer, StaticCityGame> cityGameMap) {
        this.cityGameMap = cityGameMap;
    }

    public int getMaxTimes() {
        return cityGameMap.values().stream().mapToInt(e -> e.getMaxTimes()).sum();
    }

    public int getOneTimes() {
        return cityGameMap.values().stream().filter(e -> e.getType() == 1).mapToInt(e -> e.getMaxTimes()).sum();
    }
}
