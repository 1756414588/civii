package com.game.manager;

import com.game.domain.RobotListen;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @Description 机器人监听管理
 * @Date 2022/9/9 11:30
 **/

public class RobotManager {

	public static RobotManager inst = new RobotManager();

	private Map<Long, RobotListen> listenMap = new ConcurrentHashMap<>();

	public static RobotManager getInst() {
		return inst;
	}


	public void putRobotListen(RobotListen robotListen) {
		listenMap.put(robotListen.getListenUid(), robotListen);
	}

	public List<RobotListen> getListenRobotList() {
		return new ArrayList<>(listenMap.values());
	}
}
