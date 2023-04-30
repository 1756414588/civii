package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticWorldCityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @date 2020/5/23 16:50
 * @description
 */
@Component
@LoadData(name = "世界据点")
public class StaticWorldCityTypeMgr extends BaseDataMgr {

    private Map<Integer, StaticWorldCityType> cityTypeMap = new HashMap<>();
    @Autowired
    private StaticDataDao staticDataDao;

    @Override
    public void load() throws Exception {
        cityTypeMap = staticDataDao.selectStaticWorldCityType();
    }

    @Override
    public void init() throws Exception{
    }

    public  StaticWorldCityType getStaticWorldCityType(int cityType){
        return  cityTypeMap.get(cityType);

    }
}
