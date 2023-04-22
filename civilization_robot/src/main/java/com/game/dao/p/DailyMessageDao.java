package com.game.dao.p;

import com.game.domain.p.DailyMessage;
import java.util.List;

public interface DailyMessageDao {

	public List<DailyMessage> load();

	public long queryMaxKeyId();

	public void insert(DailyMessage message);

	public void update(DailyMessage message);

}
