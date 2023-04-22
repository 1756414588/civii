package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.p.ConfigException;
import com.game.domain.s.StaticTaskDaily;
import com.game.domain.s.StaticTaskDailyAward;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author zcp
 * @date 2021/3/2 13:59
 * 诵我真名者,永不见bug
 */
@Component
@Getter
public class DailyTaskMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticTaskDaily> dailyMap;
    private Map<Integer, StaticTaskDailyAward> dailyAwardMap;

    @Override
    public void init() throws Exception {
        dailyMap = staticDataDao.loadStaticTaskDaily();
        dailyAwardMap = staticDataDao.loadStaticTaskDailyAward();
        this.check();
    }

    public void check() throws ConfigException {
        if (dailyMap.isEmpty() || dailyAwardMap.isEmpty()) {
            throw new ConfigException("dailyMap 或者 dailyAwardMap 为空,数据异常");
        }
    }

    public StaticTaskDaily getTaskDaily(int key) {
        return dailyMap.get(key);
    }
}
