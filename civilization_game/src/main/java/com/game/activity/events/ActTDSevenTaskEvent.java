package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.ActTDSevenType;
import com.game.domain.p.CampMembersRank;
import com.game.domain.p.EndlessTDGameInfo;
import com.game.domain.p.TD;
import com.game.domain.s.StaticTDSevenBoxAward;
import com.game.domain.s.StaticTDSevenTask;
import com.game.manager.TDTaskManager;
import com.game.server.GameServer;
import com.game.spring.SpringUtil;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/27 11:29
 **/
public class ActTDSevenTaskEvent extends BaseActivityEvent {

	private static ActTDSevenTaskEvent inst = new ActTDSevenTaskEvent();

	public static ActTDSevenTaskEvent getInst() {
		return inst;
	}


	@Override
	public void listen() {
		this.listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_TD_SEVEN_TASK, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		Player player = actor.getPlayer();
		ActivityData activityData = actor.getActivityData();

		for (int i = 0; i <= ActTDSevenType.tdTaskType_10; i++) {
			boolean show = tip(player, actRecord, activityData, i);
			if (show) {
				actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, show));
				return;
			}
		}
		actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, false));
	}


	private boolean tip(Player player, ActRecord actRecord, ActivityData activityData, int taskType) {
		if (taskType == 0) {
			return boxTips(player, actRecord, activityData, taskType);
		} else if (taskType == ActTDSevenType.tdTaskType_1) {
			return taskTypeOneAward(player, actRecord, activityData, taskType);
		} else if (taskType == ActTDSevenType.tdTaskType_2 || taskType == ActTDSevenType.tdTaskType_4) {
			return taskPassTdAward(player, actRecord, activityData, taskType);
		} else if (taskType == ActTDSevenType.tdTaskType_6) {
			return taskPassEndTdAward(player, actRecord, activityData, taskType);
		} else if (taskType == ActTDSevenType.tdTaskType_7 || taskType == ActTDSevenType.tdTaskType_8 || taskType == ActTDSevenType.tdTaskType_9) {
			return totalState(player, actRecord, activityData, taskType);
		} else if (taskType == ActTDSevenType.tdTaskType_3 || taskType == ActTDSevenType.tdTaskType_5) {
			return taskThreeStarPassTdAward(player, actRecord, activityData, taskType);
		} else if (taskType == ActTDSevenType.tdTaskType_10) {
			return serverPass(player, actRecord, activityData, taskType);
		}
		return false;
	}

	/**
	 * 宝箱奖励
	 *
	 * @param player
	 * @param actRecord
	 * @return
	 */
	private boolean boxTips(Player player, ActRecord actRecord, ActivityData activityData, int taskType) {
		int awardStatue = actRecord.getRecordNum(0);
//		LogHelper.GAME_LOGGER.info("塔防宝箱记录值:{} ", awardStatue);
		if (awardStatue <= 0) {
			return false;
		}

		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		Map<Integer, StaticTDSevenBoxAward> staticTDSevenBoxAwardMap = tdTaskManager.getStaticTDSevenBoxAwardMap();
		for (StaticTDSevenBoxAward e : staticTDSevenBoxAwardMap.values()) {
			if (e.getCond() <= awardStatue && !actRecord.getReceived().containsKey(e.getKeyId())) {
				return true;
			}
		}

		return false;
	}


	/**
	 * 每日塔防任务 taskType:1
	 *
	 * @param player
	 * @param actRecord
	 * @return
	 */
	public boolean taskTypeOneAward(Player player, ActRecord actRecord, ActivityData activityData, int taskType) {
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		Map<Integer, StaticTDSevenTask> taskMap = tdTaskManager.getTdSevenTaskByType().get(taskType);

		for (StaticTDSevenTask e : taskMap.values()) {
			int currentDay = GameServer.getInstance().currentDay;

			// 今日任务未完成
			long status = actRecord.getStatus(e.getTaskId());
			if (status != currentDay) {
				continue;
			}

			// 未领取奖励
			int receive = actRecord.getReceived(e.getTaskId());
			if (receive != currentDay) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 通关任务taskType:2-4
	 *
	 * @param player
	 * @param actRecord
	 * @return
	 */
	public boolean taskPassTdAward(Player player, ActRecord actRecord, ActivityData activityData, int taskType) {
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		Map<Integer, StaticTDSevenTask> taskMap = tdTaskManager.getTdSevenTaskByType().get(taskType);

		for (StaticTDSevenTask e : taskMap.values()) {
			if (actRecord.getReceived().containsKey(e.getTaskId())) {//已领奖
				continue;
			}

			TD td = player.getTdMap().get(e.getParam().get(0));
			if (td != null && td.getStar() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 通关任务taskType:2-4-6
	 *
	 * @param player
	 * @param actRecord
	 * @return
	 */
	public boolean taskPassEndTdAward(Player player, ActRecord actRecord, ActivityData activityData, int taskType) {
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		Map<Integer, StaticTDSevenTask> taskMap = tdTaskManager.getTdSevenTaskByType().get(taskType);

		for (StaticTDSevenTask e : taskMap.values()) {
			if (actRecord.getReceived().containsKey(e.getTaskId())) {//已领奖
				continue;
			}

			EndlessTDGameInfo endlessTDGameInfo = player.getEndlessTDInfo().getGameInfo();
			if (endlessTDGameInfo != null && endlessTDGameInfo.getLevelId() >= e.getParam().get(0)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * 三星通关taskType:3-5
	 *
	 * @param player
	 * @param actRecord
	 * @param taskType
	 * @return
	 */
	public boolean taskThreeStarPassTdAward(Player player, ActRecord actRecord, ActivityData activityData, int taskType) {
		if (taskType != ActTDSevenType.tdTaskType_3 && taskType != ActTDSevenType.tdTaskType_5) {
			return true;
		}
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		List<StaticTDSevenTask> taskList = tdTaskManager.getTdSortListMap().get(taskType);

		for (StaticTDSevenTask e : taskList) {
			if (actRecord.getReceived().containsKey(e.getTaskId())) {
				continue;
			}
			// 判定第一个是否已经达到条件
			TD td = player.getTdMap().get(e.getParam().get(0));
			if (td != null && td.getStar() >= 3) {
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * 累计成就taskType:7-8-9
	 *
	 * @param player
	 * @param actRecord
	 * @param taskType
	 * @return
	 */
	public boolean totalState(Player player, ActRecord actRecord, ActivityData activityData, int taskType) {
		int state = actRecord.getRecordNum(taskType);
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		List<StaticTDSevenTask> taskList = tdTaskManager.getTdSortListMap().get(taskType);
		for (StaticTDSevenTask e : taskList) {
			if (actRecord.getReceived().containsKey(e.getTaskId())) {//已领取
				continue;
			}
			if (state >= e.getParam().get(0)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * 全服累计taskType:10
	 *
	 * @param player
	 * @param actRecord
	 * @param taskType
	 * @return
	 */
	public boolean serverPass(Player player, ActRecord actRecord, ActivityData activityData, int taskType) {
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		List<StaticTDSevenTask> taskList = tdTaskManager.getTdSortListMap().get(taskType);
		for (StaticTDSevenTask e : taskList) {
			if (actRecord.getReceived().containsKey(e.getTaskId())) {//已领取
				continue;
			}
			LinkedList<CampMembersRank> campMembers = activityData.getCampMembers(e.getParam().get(1));
			if (campMembers != null && campMembers.size() >= e.getParam().get(0)) {
				return true;
			}
		}
		return false;
	}

}
