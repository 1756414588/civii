package com.game.manager;

import com.game.dao.p.RobotMessageDao;
import com.game.define.LoadData;
import com.game.domain.p.RobotMessage;
import com.game.load.ILoadData;
import com.game.packet.Packet;
import com.game.pb.InnerPb.ListenEventRq;
import com.game.pb.InnerPb.ListenEventRs;
import com.game.server.AppPropertes;
import com.game.server.exec.RobotCmdExecutor;
import com.game.server.work.CmdWork;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "请求消息管理", initSeq = 1000)
public class MessageManager implements ILoadData {

	@Getter
	private List<RobotMessage> robotMessageList = new ArrayList<>();

	private Map<Long, Long> nextCmd = new HashMap<>();

	private Map<Long, RobotMessage> cmdMap = new HashMap<>();
	// 接收消息
	private Map<Integer, Long> respondMap = new HashMap<>();

	private Set<Integer> filters = new HashSet<>();

	@Autowired
	private RobotMessageDao robotMessageDao;
	@Autowired
	private AppPropertes appPropertes;
	@Autowired
	private RobotCmdExecutor robotCmdExecutor;

	@Override
	public void load() {
		robotMessageList = robotMessageDao.load();
		long preId = 0;

		for (RobotMessage robotMessage : robotMessageList) {
			cmdMap.put(robotMessage.getKeyId(), robotMessage);
			nextCmd.put(preId, robotMessage.getKeyId());
			preId = robotMessage.getKeyId();
		}
		// 过滤的消息
		filters.add(ListenEventRq.EXT_FIELD_NUMBER);
		filters.add(ListenEventRs.EXT_FIELD_NUMBER);

		repairRemainTime();
	}

	public void repairRemainTime() {
		long count = robotMessageList.stream().filter(e -> e.getRemainTime() == 0).count();
		if (count < 10) {
			return;
		}
		long preCreateTime = 0L;
		for (RobotMessage robotMessage : robotMessageList) {
			int oldRemainTime = robotMessage.getRemainTime();

			if (preCreateTime == 0L) {// 第一条消息延时10秒
				robotMessage.setRemainTime(10000);
				preCreateTime = robotMessage.getCreateTime();
				if (oldRemainTime == 0) {
					robotMessageDao.update(robotMessage);
				}
				continue;
			}

			// 将操作时间改为同玩家一致

			int remainTime = (int) (robotMessage.getCreateTime() - preCreateTime);
			preCreateTime = robotMessage.getCreateTime();
			robotMessage.setRemainTime(remainTime);
			if (oldRemainTime == 0) {
				robotMessageDao.update(robotMessage);
			}
		}

	}

	@Override
	public void init() {
	}

	public void recordCmd(Packet packet) {
		if (!appPropertes.isRecordCmd()) {
			return;
		}
		if (filters.contains(packet.getCmd())) {
			return;
		}
		robotCmdExecutor.add(new CmdWork(packet));
	}

	/**
	 * 下一个指令
	 *
	 * @param preId
	 * @return
	 */
	public long getNext(long preId) {
		return nextCmd.get(preId);
	}
}
