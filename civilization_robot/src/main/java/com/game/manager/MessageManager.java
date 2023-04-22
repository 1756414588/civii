package com.game.manager;

import com.game.dao.p.DailyMessageDao;
import com.game.dao.p.RobotMessageDao;
import com.game.define.LoadData;
import com.game.domain.p.DailyMessage;
import com.game.domain.p.RobotData;
import com.game.domain.p.RobotMessage;
import com.game.load.ILoadData;
import com.game.packet.Packet;
import com.game.pb.InnerPb.ListenEventRq;
import com.game.pb.InnerPb.ListenEventRs;
import com.game.server.AppPropertes;
import com.game.server.exec.RobotCmdExecutor;
import com.game.server.work.CmdWork;
import com.game.util.TimeHelper;
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
	@Getter
	private List<DailyMessage> dailyMessageList = new ArrayList<>();

	private Map<Long, Long> nextCmd = new HashMap<>();

	private Map<Long, RobotMessage> cmdMap = new HashMap<>();

	// groupId,robot_message[keyId]
	private Map<Integer, Long> groupComplates = new HashMap<>();
	// 接收消息
	private Map<Integer, Long> respondMap = new HashMap<>();

	private Set<Integer> filters = new HashSet<>();

	@Getter
	private long maxGuildId;

	@Getter
	private long maxDailyId;

	@Autowired
	private RobotMessageDao robotMessageDao;
	@Autowired
	private DailyMessageDao dailyMessageDao;
	@Autowired
	private AppPropertes appPropertes;
	@Autowired
	private RobotCmdExecutor robotCmdExecutor;


	@Override
	public void load() {
		robotMessageList = robotMessageDao.load();
		dailyMessageList = dailyMessageDao.load();
		maxDailyId = dailyMessageDao.queryMaxKeyId();
		long preId = 0;

		for (RobotMessage robotMessage : robotMessageList) {
			cmdMap.put(robotMessage.getKeyId(), robotMessage);
			nextCmd.put(preId, robotMessage.getKeyId());
			preId = robotMessage.getKeyId();

			// 事件标志
			groupComplates.put(robotMessage.getDiffHour(), robotMessage.getKeyId());

			// 最终的消息编号
			maxGuildId = robotMessage.getKeyId();
		}
		// 过滤的消息
		filters.add(ListenEventRq.EXT_FIELD_NUMBER);
		filters.add(ListenEventRs.EXT_FIELD_NUMBER);

		repairRemainTime();
	}

	@Override
	public void init() {
	}


	public void repairRemainTime() {
		long count = robotMessageList.stream().filter(e -> e.getRemainTime() == 0).count();
		if (count < 10) {
			return;
		}
		long preCreateTime = 0L;
		long hour = 0;
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

			int remainTime = (int) (robotMessage.getCreateTime() - preCreateTime);
			preCreateTime = robotMessage.getCreateTime();
			robotMessage.setRemainTime(remainTime);
			if (oldRemainTime == 0) {

				// 将操作时间改为同玩家一致
				if (hour == 0) {
					hour = robotMessage.getCreateTime();
				}
				long diffHour = (robotMessage.getCreateTime() - hour) / 3600000;
				robotMessage.setDiffHour((int) diffHour);

				robotMessageDao.update(robotMessage);
			}
		}

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


	public boolean isComplate(RobotData robotData) {
		return robotData.getGuildId() >= maxGuildId;
	}

	public boolean isComplateDaily(RobotData robotData) {
		int today = TimeHelper.getCurrentDay();
		int createDay = robotData.getCreateDate();
		if (today == createDay) {//创角当天需要完成新手引导才参与
			return robotData.getGuildId() == maxGuildId;
		} else {//其他时间需要完成日常
			return robotData.getDailyDate() == today && robotData.getMessageId() == maxDailyId;
		}

	}
}
