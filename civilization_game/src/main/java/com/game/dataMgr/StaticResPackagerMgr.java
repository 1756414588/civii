package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticResPackager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @date 2020/1/13 11:35
 * @description
 */
@Component
@LoadData(name = "资源")
public class StaticResPackagerMgr extends BaseDataMgr {

	@Autowired
	private StaticDataDao staticDataDao;


	private Map<Integer, StaticResPackager> packagerMap = new ConcurrentHashMap<>();

	@Override
	public void load() throws Exception {
		packagerMap = staticDataDao.selectStaticResPackager();
	}

	@Override
	public void init() throws Exception {

	}


	/**
	 * @param resId
	 * @param time  最大为15
	 * @return
	 */
	public StaticResPackager getStaticResPackager(int resId, int time) {
		time = (time >= 15 ? 15 : time);

		for (StaticResPackager staticResPackager : packagerMap.values()) {
			if (staticResPackager.getResType() == resId && staticResPackager.getTime() == time) {
				return staticResPackager;
			}
		}
		return null;
	}
}
