package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticStaffTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StaticStaffMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;
    private Map<Integer, StaticStaffTask> staffTaskMap = new HashMap<Integer, StaticStaffTask>();

    @Override
    public void init() throws Exception{
        setStaffTaskMap(staticDataDao.selectStaffTaskMap());
    }

    public Map<Integer, StaticStaffTask> getStaffTaskMap() {
        return staffTaskMap;
    }

    public void setStaffTaskMap(Map<Integer, StaticStaffTask> staffTaskMap) {
        this.staffTaskMap = staffTaskMap;
    }

    public StaticStaffTask getStaffTask(int taskId) {
        return staffTaskMap.get(taskId);
    }
}
