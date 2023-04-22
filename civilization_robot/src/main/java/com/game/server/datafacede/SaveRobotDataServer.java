package com.game.server.datafacede;

import com.game.cache.RobotDataCache;
import com.game.define.DataFacede;
import com.game.domain.p.RobotData;
import com.game.server.thread.SaveRobotDataThread;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 机器人数据
 * @Date 2022/10/26 11:30
 **/

@DataFacede(desc = "机器人数据")
@Service
public class SaveRobotDataServer extends SaveServer<RobotData> {

	public SaveRobotDataServer() {
		super("SAVE_RECORD_SERVER", 128);
	}

	public SaveThread createThread(String name) {
		return new SaveRobotDataThread(name);
	}

	@Override
	public void saveData(RobotData robotData) {
		SaveThread thread = threadPool.get(robotData.getAccountKey() % threadNum);
		thread.add(robotData);
	}

	@Override
	public void saveAll() {
		RobotDataCache robotDataCache = SpringUtil.getBean(RobotDataCache.class);
		List<RobotData> list = robotDataCache.getDataMap().values().stream().collect(Collectors.toList());
		list.forEach(robotData -> {
			robotData.setOnline(0);
			saveData(robotData);
		});
	}
}
