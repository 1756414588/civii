package com.game.dao.p;

import com.game.domain.RobotConfig;
import java.util.Map;
import org.apache.ibatis.annotations.MapKey;

public interface RobotConfigDao {

	@MapKey("key")
	public Map<String, RobotConfig> loadConfig();

}
