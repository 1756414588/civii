package com.game.server.datafacede;

import com.game.define.DataFacede;
import com.game.domain.Record;
import com.game.domain.Robot;
import com.game.manager.RecordManager;
import com.game.manager.RobotManager;
import com.game.server.thread.SaveRecordThread;
import com.game.server.thread.SaveServer;
import com.game.server.thread.SaveThread;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 *
 * @Description 玩家数据存储服务
 * @Date 2022/9/14 11:30
 **/

@DataFacede(desc = "记录存储")
@Service
public class SaveRecordServer extends SaveServer<Record> {

	public SaveRecordServer() {
		super("SAVE_RECORD_SERVER", 128);
	}

	public SaveThread createThread(String name) {
		return new SaveRecordThread(name);
	}

	@Override
	public void saveData(Record record) {
		SaveThread thread = threadPool.get(record.getAccountKey() % threadNum);
		thread.add(record);
	}

	@Override
	public void saveAll() {
		RecordManager manager = SpringUtil.getBean(RecordManager.class);

		RobotManager robotManager = SpringUtil.getBean(RobotManager.class);

		List<Robot> list = robotManager.getRobotMap().values().stream().filter(e -> e.isLogin()).collect(Collectors.toList());
		list.forEach(robot -> {
			Record record = manager.getRecord(robot.getId());
			if (record != null) {
				saveData(record);
			}
		});
	}
}
