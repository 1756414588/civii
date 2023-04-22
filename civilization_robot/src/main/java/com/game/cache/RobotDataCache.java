package com.game.cache;

import com.alibaba.fastjson.JSONObject;
import com.game.dao.p.RobotDataDao;
import com.game.define.LoadData;
import com.game.domain.p.RobotData;
import com.game.load.ILoadData;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @Description
 * @Date 2022/10/20 20:00
 **/

@Component
@LoadData(name = "机器人缓存", initSeq = 100)
public class RobotDataCache implements ILoadData {

	@Getter
	private Map<Integer, RobotData> dataMap = new ConcurrentHashMap<>();

	@Autowired
	private RobotDataDao robotDataDao;

	@Override
	public void load() {
		dataMap = robotDataDao.load();
		LogHelper.CHANNEL_LOGGER.info("机器人缓存");
	}

	@Override
	public void init() {
	}

	public RobotData getRobotData(int accountKey) {
		return dataMap.get(accountKey);
	}

	public RobotData create(String account, int serverId, JSONObject data) {
		RobotData robotData = new RobotData();
		robotData.setAccount(account);
		robotData.setAccountKey(data.getInteger("keyId"));
		robotData.setToken(data.getString("token"));
		robotData.setServerId(serverId);
		robotData.setLoginDate(0);
		robotData.setGuildId(1);
		robotData.setGuildState(0);
		robotData.setCreateDate(TimeHelper.getCurrentDay());
		robotDataDao.insert(robotData);
		dataMap.put(robotData.getAccountKey(), robotData);
		return robotData;
	}


	public void update(RobotData robotData) {
		robotDataDao.update(robotData);
	}


}
