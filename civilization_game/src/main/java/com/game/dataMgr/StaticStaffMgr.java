package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticStaffTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@LoadData(name = "参谋部任务")
public class StaticStaffMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;
	private Map<Integer, StaticStaffTask> staffTaskMap = new HashMap<Integer, StaticStaffTask>();

	@Override
	public void load() throws Exception {
		setStaffTaskMap(staticDataDao.selectStaffTaskMap());
	}

	@Override
	public void init() throws Exception {
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
