package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.p.ActTDSevenType;
import com.game.domain.s.StaticTDSevenBoxAward;
import com.game.domain.s.StaticTDSevenTask;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @Description
 * @Date 2023/4/6 17:26
 **/

@Component
@LoadData(name = "塔防任务配置")
public class StaticTDTaskMgr extends BaseDataMgr {

	// 塔防活动任务
	private Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap = new HashMap<>();
	private Map<Integer, Map<Integer, StaticTDSevenTask>> tdSevenTaskByType = new HashMap<>();
	// 塔防活动宝箱奖励
	private Map<Integer, StaticTDSevenBoxAward> staticTDSevenBoxAwardMap = new HashMap<>();
	// 类型按顺序排列
	@Getter
	private Map<Integer, List<StaticTDSevenTask>> tdSortListMap = new HashMap<>();

	@Autowired
	private StaticDataDao staticDataDao;

	@Override
	public void load() throws Exception {
		staticTDSevenTaskMap.clear();
		staticTDSevenBoxAwardMap.clear();
		tdSevenTaskByType.clear();
		staticTDSevenTaskMap = staticDataDao.loadStaticTDSevenTaskMap();
		staticTDSevenTaskMap.values().forEach(e -> {
			tdSevenTaskByType.computeIfAbsent(e.getTaskType(), x -> new HashMap<>()).put(e.getTaskId(), e);

			//
			List<StaticTDSevenTask> list = tdSortListMap.get(e.getTaskType());
			if (list == null) {
				list = new ArrayList<>();
				tdSortListMap.put(e.getTaskType(), list);
			}
			list.add(e);
			list.sort(Comparator.comparing(StaticTDSevenTask::getTaskId));
		});
		staticTDSevenBoxAwardMap = staticDataDao.loadStaticTDSevenBoxAwardMap();

	}

	@Override
	public void init() throws Exception {

	}

	public Map<Integer, StaticTDSevenTask> getStaticTDSevenTaskMap() {
		return staticTDSevenTaskMap;
	}

	public void setStaticTDSevenTaskMap(Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		this.staticTDSevenTaskMap = staticTDSevenTaskMap;
	}

	public Map<Integer, Map<Integer, StaticTDSevenTask>> getTdSevenTaskByType() {
		return tdSevenTaskByType;
	}

	public void setTdSevenTaskByType(Map<Integer, Map<Integer, StaticTDSevenTask>> tdSevenTaskByType) {
		this.tdSevenTaskByType = tdSevenTaskByType;
	}

	public Map<Integer, StaticTDSevenBoxAward> getStaticTDSevenBoxAwardMap() {
		return staticTDSevenBoxAwardMap;
	}

	public void setStaticTDSevenBoxAwardMap(Map<Integer, StaticTDSevenBoxAward> staticTDSevenBoxAwardMap) {
		this.staticTDSevenBoxAwardMap = staticTDSevenBoxAwardMap;
	}

	public Map<Integer, List<StaticTDSevenTask>> getTdSortListMap() {
		return tdSortListMap;
	}

	public void setTdSortListMap(Map<Integer, List<StaticTDSevenTask>> tdSortListMap) {
		this.tdSortListMap = tdSortListMap;
	}
}
