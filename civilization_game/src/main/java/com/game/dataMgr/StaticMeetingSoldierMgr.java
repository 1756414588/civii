package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticMeetingSoldier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 *
 * @date 2019/12/17 9:32
 * @description
 */
@Component
@LoadData(name = "议会厅兵力")
public class StaticMeetingSoldierMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;

    private List<StaticMeetingSoldier> staticMeetingSoldiers;

    @Override
    public void load() throws Exception {
        staticMeetingSoldiers = staticDataDao.selectMeetingSoldier();
        staticMeetingSoldiers.sort(Comparator.comparing(StaticMeetingSoldier::getSoldier));
    }

    @Override
    public void init() throws Exception{

    }

    /**
     * 根据怪物数量拿等级补偿
     * @param soldier
     * @return
     */
    public StaticMeetingSoldier getStaticMeetingTask(int soldier) {
        for (StaticMeetingSoldier staticMeetingSoldier : staticMeetingSoldiers) {
            if (soldier <=staticMeetingSoldier.getSoldier()) {
                return staticMeetingSoldier;
            }
        }
        return null;
    }

}
