package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticMeetingCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @date 2019/12/16 10:10
 * @description
 */
@Component
@LoadData(name = "议会厅")
public class StaticMeetingCommandMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticMeetingCommand> meetingTasks = new ConcurrentHashMap<>();

    @Override
    public void load() throws Exception {
        meetingTasks = staticDataDao.selectMeetingCommand();
    }

    @Override
    public void init() throws Exception {
    }

    public StaticMeetingCommand getStaticMeetingCommand(int id) {
        return meetingTasks.get(id);
    }
}
