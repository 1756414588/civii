package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticMeetingCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jyb
 * @date 2019/12/16 10:10
 * @description
 */
@Component
public class StaticMeetingCommandMgr extends BaseDataMgr {

    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticMeetingCommand> meetingTasks = new ConcurrentHashMap<>();

    @Override
    public void init() throws Exception {
        meetingTasks = staticDataDao.selectMeetingCommand();
    }

    public StaticMeetingCommand getStaticMeetingCommand(int id) {
        return meetingTasks.get(id);
    }
}
