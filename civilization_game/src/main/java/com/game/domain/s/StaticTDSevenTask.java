package com.game.domain.s;

import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.ActTDSevenType;
import com.game.domain.p.EndlessTDInfo;
import com.game.domain.p.TD;
import com.game.manager.ActivityManager;
import com.game.pb.CommonPb.ActivityCond;
import com.game.pb.CommonPb.ActivityCondState;
import com.game.pb.CommonPb.Award;
import com.game.server.GameServer;
import com.game.spring.SpringUtil;
import java.util.List;

public class StaticTDSevenTask {


	private int taskId;
	private int taskType;
	private String typeChild;
	private List<Integer> param;
	private List<Integer> awardList;
	private String desc;
	private String asset;
	private String forward;
	private int type;

	public ActivityCondState.Builder warp(Player player, ActRecord actRecord) {
		ActivityCondState.Builder builder = ActivityCondState.newBuilder();
		ActivityCond.Builder b = ActivityCond.newBuilder();
		b.setKeyId(taskId);
		if (actRecord.getReceived().containsKey(taskId)) {
			b.setIsAward(1);
		} else {
			b.setIsAward(0);
		}
		ActivityData activityData = SpringUtil.getBean(ActivityManager.class).getActivity(actRecord.getActivityId());
		if (type == 2) {
			builder.setState(actRecord.getRecordNum(taskType));
			b.setCond(param.get(0));
		} else if (type == 3) {
			if (activityData != null) {
				builder.setState(activityData.getCampMembers(param.get(1)).size());
				b.setCond(param.get(0));
			}
		} else {
			int currentDay = GameServer.getInstance().currentDay;
			if (taskType == ActTDSevenType.tdTaskType_2 || taskType == ActTDSevenType.tdTaskType_4) {//通关关卡
				TD td = player.getTdMap().get(param.get(0));
				builder.setState(td == null ? 0 : td.getStar() > 0 ? 1 : 0);
				actRecord.putState(taskId, currentDay);
			} else if (taskType == ActTDSevenType.tdTaskType_6) {//防守无尽
				EndlessTDInfo endlessTDInfo = player.getEndlessTDInfo();
				builder.setState(endlessTDInfo.getGameInfo() == null ? 0 : endlessTDInfo.getGameInfo().getLevelId() >= param.get(0) ? 1 : 0);
				actRecord.putState(taskId, currentDay);
			} else if (taskType == ActTDSevenType.tdTaskType_3 || taskType == ActTDSevenType.tdTaskType_5) {//3星通关关卡
				TD td = player.getTdMap().get(param.get(0));
				builder.setState(td == null ? 0 : td.getStar() >= 3 ? 1 : 0);
				actRecord.putState(taskId, currentDay);
			} else {//taskType-->1
				int status = (int) actRecord.getStatus(taskId);
				int receive = actRecord.getReceived(taskId);
				if (receive == currentDay) {//已领奖
					b.setIsAward(1);
				} else {
					b.setIsAward(0);
				}

				if (status == currentDay) {//未完成
					builder.setState(1);
				} else {
					builder.setState(0);
				}
			}
			b.setCond(1);
		}
		if (awardList != null && awardList.size() == 3) {
			b.addAward(Award.newBuilder().setType(awardList.get(0)).setId(awardList.get(1)).setCount(awardList.get(2)).build());
		}
		builder.setActivityCond(b);
		return builder;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getTaskType() {
		return taskType;
	}

	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}

	public String getTypeChild() {
		return typeChild;
	}

	public void setTypeChild(String typeChild) {
		this.typeChild = typeChild;
	}

	public List<Integer> getParam() {
		return param;
	}

	public void setParam(List<Integer> param) {
		this.param = param;
	}

	public List<Integer> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<Integer> awardList) {
		this.awardList = awardList;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset(String asset) {
		this.asset = asset;
	}

	public String getForward() {
		return forward;
	}

	public void setForward(String forward) {
		this.forward = forward;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "StaticTDSevenTask{" + "taskId=" + taskId + ", taskType=" + taskType + ", typeChild='" + typeChild + '\'' + ", param=" + param + ", awardList=" + awardList + ", desc='" + desc + '\'' + ", asset='" + asset + '\'' + ", forward='" + forward + '\'' + ", type=" + type + '}';
	}

}