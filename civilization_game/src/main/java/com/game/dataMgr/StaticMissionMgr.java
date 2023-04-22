package com.game.dataMgr;

import com.game.define.LoadData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.MissionType;
import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticMission;

@Component
@LoadData(name = "关卡")
public class StaticMissionMgr extends BaseDataMgr {
	@Autowired
	private StaticDataDao staticDataDao;

	private Map<Integer, StaticMission> missionMap = new HashMap<Integer, StaticMission>();
	private Map<Integer, List<StaticMission>> missionMapIds = new HashMap<>();

	@Override
	public void load() throws Exception {
		missionMap.clear();
		missionMapIds.clear();
		missionMap = staticDataDao.selectMissionMap();
		missionMapIds = missionMap.values().stream().collect(Collectors.groupingBy(StaticMission::getMapId));
		checkConfig();
	}

	@Override
	public void init() throws Exception{

	}

	public StaticMission getStaticMission(int missionId) {
		StaticMission staticMission = missionMap.get(missionId);
		return staticMission;
	}


	public void checkConfig() {
		for (Map.Entry<Integer, StaticMission> entry : missionMap.entrySet()) {
			StaticMission staticMission = entry.getValue();
			if (staticMission == null)
				throw new IllegalArgumentException();
			List<Integer> starCondition = staticMission.getStarCondition();
			if (staticMission.getMissionType() == MissionType.BossMission) {
				if (starCondition == null) {
					throw new IllegalArgumentException();
				}

				if (starCondition.size() != 3) {
					throw new IllegalArgumentException();
				}
			}
		}
	}

	public Map<Integer, StaticMission> getMissionMap() {
		return missionMap;
	}

	public List<StaticMission> getMissionMapByMapId(int mapId) {
		return missionMapIds.get(mapId);
	}
}
