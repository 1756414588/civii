package com.game.manager;

import com.game.Loading;
import com.game.activity.ActivityEventManager;
import com.game.activity.actor.TdActor;
import com.game.activity.define.EventEnum;
import com.game.constant.ActivityConst;
import com.game.dao.s.StaticDataDao;
import com.game.dataMgr.BaseDataMgr;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticTDTaskMgr;
import com.game.define.LoadData;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.ActTDSevenType;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticTDSevenBoxAward;
import com.game.domain.s.StaticTDSevenTask;
import com.game.server.GameServer;
import com.game.service.ActivityService;
import com.game.util.DateHelper;
import com.game.spring.SpringUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/25 10:42
 **/
@Getter
@Setter
@Component
@LoadData(name = "塔防任务", type = Loading.LOAD_USER_DB)
public class TDTaskManager extends BaseManager {

	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private StaticActivityMgr staticActivityMgr;
	@Autowired
	ActivityEventManager activityEventManager;
	@Autowired
	private StaticTDTaskMgr staticTDTaskMgr;

	private Map<Integer, TDTask> tdTaskMap;


	@Override
	public void init() throws Exception {
		tdTaskMap = new HashMap<>();
		tdTaskMap.put(ActTDSevenType.tdTaskType_1, this::refreshTask_1);
		tdTaskMap.put(ActTDSevenType.tdTaskType_2, this::refreshTask_2);
		tdTaskMap.put(ActTDSevenType.tdTaskType_3, this::refreshTask_3);
		tdTaskMap.put(ActTDSevenType.tdTaskType_4, this::refreshTask_4);
		tdTaskMap.put(ActTDSevenType.tdTaskType_5, this::refreshTask_5);
		tdTaskMap.put(ActTDSevenType.tdTaskType_6, this::refreshTask_6);
		tdTaskMap.put(ActTDSevenType.tdTaskType_7, this::refreshTask_7);
		tdTaskMap.put(ActTDSevenType.tdTaskType_8, this::refreshTask_8);
		tdTaskMap.put(ActTDSevenType.tdTaskType_9, this::refreshTask_9);
		tdTaskMap.put(ActTDSevenType.tdTaskType_10, this::refreshTask_10);
	}

	public interface TDTask {

		void refreshTask(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap);
	}

	public void refreshTask(Player player, ActTDSevenType actTDSevenType) {
		if (actTDSevenType == null || player == null || actTDSevenType.getTaskTypeList() == null) {
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_TD_SEVEN_TASK);
		if (activityBase == null) {
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			return;
		}
		Date createDate = player.account.getCreateDate();
		int state = DateHelper.dayiy(createDate, new Date());
		// 超过7天不开该活动
		if (state > activityBase.getStaticActivity().getLess()) {
			return;
		}

		actTDSevenType.putData(activityBase, actRecord, activityData, player);
		actTDSevenType.getTaskTypeList().forEach(e -> {
			TDTask tdTask = tdTaskMap.get(e);
			Map<Integer, StaticTDSevenTask> taskMap = staticTDTaskMgr.getTdSevenTaskByType().get(e);
			if (tdTask != null && taskMap != null) {
				tdTask.refreshTask(actTDSevenType, taskMap);
			}
		});

		activityEventManager.activityTip(EventEnum.GET_ACTIVITY_AWARD_TIP, new TdActor(player, actRecord, activityData, activityBase));
	}

	// 每日参与塔防1次
	public void refreshTask_1(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		staticTDSevenTaskMap.values().stream().filter(e -> e.getParam().get(0) == actTDSevenType.getParam()).forEach(e -> {
			if ((int) actTDSevenType.getStatus(e.getTaskId()) != GameServer.getInstance().currentDay) {
				actTDSevenType.resetTask(e.getTaskId());
			}
			actTDSevenType.putStatus(e);
		});
	}

	// 通关塔防经典模式简单关卡
	public void refreshTask_2(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		staticTDSevenTaskMap.values().stream().filter(e -> e.getParam().get(0) == actTDSevenType.getLevelId()).forEach(e -> {
			actTDSevenType.putStatus(e);
		});
	}

	// 三星通关塔防简单关卡
	public void refreshTask_3(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		staticTDSevenTaskMap.values().stream().filter(e -> e.getParam().get(0) == actTDSevenType.getLevelId() && actTDSevenType.getStart() == 3).forEach(e -> {
			actTDSevenType.putStatus(e);
		});
	}

	// 通关塔防困难关卡
	public void refreshTask_4(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		staticTDSevenTaskMap.values().stream().filter(e -> e.getParam().get(0) == actTDSevenType.getLevelId()).forEach(e -> {
			actTDSevenType.putStatus(e);
		});
	}

	// 三星通关塔防困难关卡
	public void refreshTask_5(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		staticTDSevenTaskMap.values().stream().filter(e -> e.getParam().get(0) == actTDSevenType.getLevelId() && actTDSevenType.getStart() == 3).forEach(e -> {
			actTDSevenType.putStatus(e);
		});
	}

	// 成功通关无尽模式第*关
	public void refreshTask_6(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		staticTDSevenTaskMap.values().stream().filter(e -> e.getParam().get(0) == actTDSevenType.getLevelId()).forEach(e -> {
			actTDSevenType.putStatus(e);
		});
	}

	// 塔防经典模式获得总星数
	public void refreshTask_7(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		int starSum = actTDSevenType.getPlayer().getTdMap().values().stream().mapToInt(e -> e.getStar()).sum();
		actTDSevenType.putRecord(ActTDSevenType.tdTaskType_7, starSum);
		staticTDSevenTaskMap.values().stream().filter(e -> starSum >= e.getParam().get(0)).forEach(e -> {
			actTDSevenType.putStatus(e);
		});
	}

	// 累计获得塔防无尽模式*积分
	public void refreshTask_8(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		actTDSevenType.addRecord(ActTDSevenType.tdTaskType_8, actTDSevenType.getParam());
		staticTDSevenTaskMap.values().stream().filter(e -> actTDSevenType.getRecord(e.getTaskType()) >= e.getParam().get(0)).forEach(e -> {
			actTDSevenType.putStatus(e);
		});
	}

	// 无尽模式军械商店累计消耗钻石
	public void refreshTask_9(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		actTDSevenType.addRecord(ActTDSevenType.tdTaskType_9, actTDSevenType.getParam());
		staticTDSevenTaskMap.values().stream().filter(e -> actTDSevenType.getRecord(e.getTaskType()) >= e.getParam().get(0)).forEach(e -> {
			actTDSevenType.putStatus(e);
		});
	}

	public void refreshTask_10(ActTDSevenType actTDSevenType, Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap) {
		staticTDSevenTaskMap.values().stream().filter(e -> e.getParam().get(1) == actTDSevenType.getLevelId()).forEach(e -> {
			actTDSevenType.fullService(e);
		});
		if (actTDSevenType.getTips()) {
			SpringUtil.getBean(ActivityService.class).synActivity(actTDSevenType.getActivityBase(), actTDSevenType.getActivityData(), EventEnum.GET_ACTIVITY_AWARD_TIP);
		}
	}

	public static void staticRefreshTask(Player player, ActTDSevenType actTDSevenType) {
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		tdTaskManager.refreshTask(player, actTDSevenType);
	}
}
