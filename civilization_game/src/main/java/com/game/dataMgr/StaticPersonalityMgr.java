package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticPersonality;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author cpz
 * @date 2021/1/26 14:18
 * @description
 */
@Component
@LoadData(name = "个人相关")
public class StaticPersonalityMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao dataDao;

	@Getter
	private Map<Integer, StaticPersonality> dataMap = new ConcurrentHashMap<>();

	@Override
	public void load() throws Exception {
		dataMap = dataDao.loadStaticPersonality();
	}

	@Override
	public void init() throws Exception {

	}

	public List<StaticPersonality> getByType(int type) {
		List<StaticPersonality> list = dataMap.values().stream().filter(e -> e.getType() == type).collect(Collectors.toList());
		return list;
	}

	public StaticPersonality get(int id) {
		return dataMap.get(id);
	}
}
