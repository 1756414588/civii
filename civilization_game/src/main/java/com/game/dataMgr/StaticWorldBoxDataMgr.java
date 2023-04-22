package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticWorldBox;
import com.game.domain.s.StaticWorldBoxCollect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @date 2021/1/5 17:42
 * @description
 */
@Component
@LoadData(name = "世界宝箱")
public class StaticWorldBoxDataMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao dataDao;

    private Map<Integer, StaticWorldBox> staticWorldBoxMap;
    private Map<Integer, StaticWorldBoxCollect> staticWorldBoxCollectMap;

    @Override
    public void load() throws Exception {
        staticWorldBoxMap = new HashMap<>();
        staticWorldBoxMap = dataDao.loadStaticWorldBox();
        staticWorldBoxCollectMap = new HashMap<>();
        staticWorldBoxCollectMap = dataDao.loadStaticWorldBoxCollect();
    }

    @Override
    public void init()  throws Exception{
    }

    public StaticWorldBox getStaticWorldBox(int boxId) {
        return staticWorldBoxMap.get(boxId);
    }


    public StaticWorldBoxCollect getStaticWorldBoxCollect(int eventId) {
        return staticWorldBoxCollectMap.get(eventId);
    }
}
