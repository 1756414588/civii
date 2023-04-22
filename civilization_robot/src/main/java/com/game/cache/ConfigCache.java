package com.game.cache;

import com.game.dao.p.RobotConfigDao;
import com.game.define.LoadData;
import com.game.domain.RobotConfig;
import com.game.load.ILoadData;
import com.game.server.AppPropertes;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "机器人配置缓存", initSeq = 100)
public class ConfigCache implements ILoadData {

	@Autowired
	private RobotConfigDao robotConfigDao;

	@Autowired
	private AppPropertes appPropertes;

	private Map<String, RobotConfig> configMap = new HashMap<>();

	@Override
	public void load() {
		this.configMap = robotConfigDao.loadConfig();
	}

	final static String AUTO_OPERATE_KEY = "auto_operate";

	@Override
	public void init() {
		appPropertes.setRobotAuto("yes".equals(getValueByKey(AUTO_OPERATE_KEY)));
		appPropertes.setRecordCmd("yes".equals(getValueByKey("record_cmd")));
	}

	public String getValueByKey(String key) {
		if (configMap.isEmpty()) {
			this.configMap = robotConfigDao.loadConfig();
		}
		if (configMap.containsKey(key)) {
			return configMap.get(key).getValue();
		}
		return null;
	}

	/**
	 * 获取账号前缀
	 *
	 * @return
	 */
	public String getAccountPrefix() {
		return getValueByKey("account_prefix");
	}

	/**
	 * 获取账号的初始下表
	 *
	 * @return
	 */
	public int getAccountIndex() {
		String v = getValueByKey("account_index");
		return Integer.valueOf(v.trim());
	}

	/**
	 * 机器人数量
	 *
	 * @return
	 */
	public int getRobotNumber() {
		String v = getValueByKey("robot_number");
		return Integer.valueOf(v.trim());
	}

	public int getServerId() {
		return Integer.valueOf(getValueByKey("server_id"));
	}


}
