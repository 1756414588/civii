package com.game.cache;

import com.game.dao.s.StaticConfigDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticMail;
import com.game.load.ILoadData;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "邮件配置缓存", initSeq = 100)
public class StaticMailCache implements ILoadData {

	private Map<Integer, StaticMail> staticMailMap = new HashMap<>();

	@Autowired
	private StaticConfigDao staticDataDao;

	@Override
	public void load() {
	}

	@Override
	public void init() {
		staticMailMap = staticDataDao.selectMail();
	}

	public StaticMail getStaticMail(int mailId) {
		return staticMailMap.get(mailId);
	}

	public boolean canShare(int mailId) {
		if (!staticMailMap.containsKey(mailId)) {
			return false;
		}
		return staticMailMap.get(mailId).getShare() > 0;
	}
}
