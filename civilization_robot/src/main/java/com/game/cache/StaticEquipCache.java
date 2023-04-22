package com.game.cache;

import com.game.dao.s.StaticConfigDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticEquip;
import com.game.load.ILoadData;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "机器人配置缓存", initSeq = 100)
public class StaticEquipCache implements ILoadData {

	private Map<Integer, StaticEquip> staticEquipMap = new HashMap<>();

	@Autowired
	private StaticConfigDao staticDataDao;

	@Override
	public void load() {
		staticEquipMap = staticDataDao.selectEquipMap();
	}

	@Override
	public void init() {
	}


}
