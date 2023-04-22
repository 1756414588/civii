package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticResPackager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jyb
 * @date 2020/1/13 11:35
 * @description
 */
@Component
public class StaticResPackagerMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;


    private Map<Integer, StaticResPackager> packagerMap = new ConcurrentHashMap<>();

    @Override
    public void init() throws Exception{
        packagerMap = staticDataDao.selectStaticResPackager();
    }


    /**
     *
     * @param resId
     * @param time 最大为15
     * @return
     */
    public StaticResPackager getStaticResPackager(int resId, int time) {
        time = (time >= 15 ? 15 : time);

        for (StaticResPackager staticResPackager : packagerMap.values()) {
            if (staticResPackager.getResType() == resId && staticResPackager.getTime() == time) {
                return staticResPackager;
            }
        }
        return null;
    }
}
