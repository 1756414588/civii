package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticWorldBox;
import com.game.domain.s.StaticWorldBoxCollect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cpz
 * @date 2021/1/5 17:42
 * @description
 */
@Component
public class StaticWorldBoxDataMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao dataDao;

    private Map<Integer, StaticWorldBox> staticWorldBoxMap;
    private Map<Integer, StaticWorldBoxCollect> staticWorldBoxCollectMap;

    @Override
    public void init()  throws Exception{
        staticWorldBoxMap = new HashMap<>();
        staticWorldBoxMap = dataDao.loadStaticWorldBox();
        staticWorldBoxCollectMap = new HashMap<>();
        staticWorldBoxCollectMap = dataDao.loadStaticWorldBoxCollect();
    }

    public StaticWorldBox getStaticWorldBox(int boxId) {
        return staticWorldBoxMap.get(boxId);
    }


    public StaticWorldBoxCollect getStaticWorldBoxCollect(int eventId) {
        return staticWorldBoxCollectMap.get(eventId);
    }
}
