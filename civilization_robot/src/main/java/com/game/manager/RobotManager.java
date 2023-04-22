package com.game.manager;

import com.game.define.LoadData;
import com.game.domain.Robot;
import com.game.domain.p.RobotData;
import com.game.load.ILoadData;
import com.game.network.robot.RobotNet;
import com.game.packet.Packet;
import com.game.util.LogHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "机器人管理", initSeq = 1000)
public class RobotManager implements ILoadData {

	@Getter
	private Map<Integer, Robot> robotMap = new ConcurrentHashMap<>();

	@Override
	public void load() {
	}

	@Override
	public void init() {
	}

	public void createRobotThenLogin(RobotData robotData) {
		Robot robot = new Robot(robotData);

		// 创建机器人
		RobotNet robotNet = new RobotNet(robot);
		robot.setRobotNet(robotNet);
		// 赋值数据
		robotNet.initRobot();
		// 记录
		robotMap.put(robotData.getAccountKey(), robot);
		// 机器人启动
		robotNet.startConnect();

		LogHelper.CHANNEL_LOGGER.info("RobotManager 初始化机器人数量 :{}", robotMap.size());
	}

	public void removeRobot(Robot robot) {

		robotMap.remove(robot);
	}

	/**
	 * 通过账号ID获取机器人
	 *
	 * @param accountKey
	 * @return
	 */
	public Robot getRobotByKey(int accountKey) {
		return robotMap.get(accountKey);
	}

	public List<Robot> getRobotList() {
		return new ArrayList<>(robotMap.values());
	}

	public void broadcast(Packet packet) {
		Iterator<Entry<Integer, Robot>> it = this.robotMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Robot> entry = it.next();
			entry.getValue().listen(packet);
		}
	}
}
