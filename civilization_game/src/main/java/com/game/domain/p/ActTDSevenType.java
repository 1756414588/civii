package com.game.domain.p;

import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticTDSevenTask;
import com.game.server.GameServer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

public class ActTDSevenType {
	private List<Integer> taskTypeList;
	private int LevelId;
	private int start;
	private int param;
	private ActivityBase activityBase;
	private ActRecord actRecord;
	private ActivityData activityData;
	private Player player;
	private Boolean tips = false;
	@Getter
	private List<Integer> taskList = new ArrayList<>();

	public ActTDSevenType(List<Integer> taskTypeList, int levelId, int start, int param) {
		this.taskTypeList = taskTypeList;
		LevelId = levelId;
		this.start = start;
		this.param = param;
	}

	public ActTDSevenType(List<Integer> taskTypeList, int param) {
		this.taskTypeList = taskTypeList;
		this.param = param;
	}

	public void putData(ActivityBase activityBase, ActRecord actRecord, ActivityData activityData, Player player) {
		this.activityBase = activityBase;
		this.actRecord = actRecord;
		this.activityData = activityData;
		this.player = player;
	}

	public List<Integer> getTaskTypeList() {
		return taskTypeList;
	}

	public int getLevelId() {
		return LevelId;
	}

	public int getParam() {
		return param;
	}

	public ActivityBase getActivityBase() {
		return activityBase;
	}

	public ActRecord getActRecord() {
		return actRecord;
	}

	public ActivityData getActivityData() {
		return activityData;
	}

	public long getLordId() {
		return player.roleId;
	}

	public Player getPlayer() {
		return player;
	}

	public int getStart() {
		return start;
	}

	public void putStatus(StaticTDSevenTask e) {
		if (actRecord == null || actRecord.getReceived().containsKey(e.getTaskId())) {
			return;
		}
		int currentDay = GameServer.getInstance().currentDay;
		actRecord.putState(e.getTaskId(), currentDay);
		tips = true;
		taskList.add(e.getTaskId());
	}

	public void addRecord(int recordId, int count) {
		if (actRecord == null) {
			return;
		}
		actRecord.addRecord(recordId, count);
	}

	public void putRecord(int recordId, int count) {
		if (actRecord == null) {
			return;
		}
		actRecord.putRecord(recordId, count);
	}

	public int getRecord(int recordId) {
		if (actRecord == null) {
			return 0;
		}
		return actRecord.getRecordNum(recordId);
	}

	public long getStatus(long sortId) {
		if (actRecord == null) {
			return 0;
		}
		return actRecord.getStatus(sortId);
	}

	public void resetTask(int taskId) {
		if (actRecord == null) {
			return;
		}
		actRecord.getStatus().remove(Long.valueOf(taskId));
		actRecord.getReceived().remove(taskId);
	}

	public Boolean getTips() {
		return tips;
	}

	public void fullService(StaticTDSevenTask task) {
		if (activityData == null || player == null) {
			return;
		}
		LinkedList<CampMembersRank> campMembers = activityData.getCampMembers(LevelId);
		CampMembersRank value = campMembers.stream().filter(e -> e.getLordId() == player.roleId).findFirst().orElse(null);
		if (value == null) {
			campMembers.add(new CampMembersRank(player));
		}
		if (campMembers.size() > task.getParam().get(0)) {
			tips = true;
			taskList.add(task.getTaskId());
		}
	}

	public static final int tdTaskType_1 = 1; // 每日参与塔防1次
	public static final int tdTaskType_2 = 2; // 通关塔防经典模式简单关卡
	public static final int tdTaskType_3 = 3; // 三星通关塔防简单关卡
	public static final int tdTaskType_4 = 4; // 通关塔防困难关卡
	public static final int tdTaskType_5 = 5; // 三星通关塔防困难关卡
	public static final int tdTaskType_6 = 6; // 成功通关无尽模式第*关
	public static final int tdTaskType_7 = 7; // 塔防经典模式获得总星数
	public static final int tdTaskType_8 = 8; // 累计获得塔防无尽模式*积分
	public static final int tdTaskType_9 = 9; // 无尽模式军械商店累计消耗钻石
	public static final int tdTaskType_10 = 10; // 全服累计10人通关无尽模式第5关

}
