package com.game.dao.p;

import com.game.domain.p.RobotData;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;

public interface RobotDataDao {

	@MapKey("accountKey")
	public Map<Integer, RobotData> load();

	public void insert(RobotData robotData);

	public void update(RobotData robotData);

}
