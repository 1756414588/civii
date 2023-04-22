package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticWorldNewTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jyb
 * @date 2019/12/24 11:06
 * @description
 */
@Component
public class StaticWorldNewTargetMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticWorldNewTarget> staticWorldNewTargets = new ConcurrentHashMap<>();

	@Override
	public void init() throws Exception {
		staticWorldNewTargets = staticDataDao.selectWorldNewTarget();
	}


	public StaticWorldNewTarget getFirstWorldTarget() {
		return staticWorldNewTargets.get(1);
	}

	public StaticWorldNewTarget get(int taskId) {
		return staticWorldNewTargets.get(taskId);
	}
}
