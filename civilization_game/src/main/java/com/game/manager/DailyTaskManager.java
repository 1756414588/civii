package com.game.manager;

import com.game.constant.DailyTaskId;
import com.game.dataMgr.DailyTaskMgr;
import com.game.dataMgr.StaticOpenManger;
import com.game.domain.Player;
import com.game.domain.p.PlayerDailyTask;
import com.game.domain.s.StaticTaskDaily;
import com.game.log.consumer.EventManager;
import com.game.pb.DailyTaskPb;
import com.game.util.SynHelper;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zcp
 * @date 2021/3/11 21:13 诵我真名者,永不见bug
 */
@Component
public class DailyTaskManager {

	@Autowired
	private StaticOpenManger staticOpenManger;
	@Autowired
	private DailyTaskMgr dailyTaskMgr;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private EventManager eventManager;

	public interface BaseDailyTask {

		void action(Player player, int countdowm, int taskId);
	}

	private Map<DailyTaskId, BaseDailyTask> actionMap;

	@PostConstruct
	protected void init() {
		actionMap = new HashMap<>();
		actionMap.put(DailyTaskId.LOGIN, this::set);
		actionMap.put(DailyTaskId.UP_BUILD, this::add);
		actionMap.put(DailyTaskId.RESEARCH_TECH, this::add);
		actionMap.put(DailyTaskId.CLEARANCE, this::add);
		actionMap.put(DailyTaskId.KILL_MONSTER, this::add);
		actionMap.put(DailyTaskId.APPOINTMENT, this::add);
		actionMap.put(DailyTaskId.PLAY_GAME, this::add);
		actionMap.put(DailyTaskId.GIFTS, this::add);
		actionMap.put(DailyTaskId.BUY_ENERGY, this::add);
		actionMap.put(DailyTaskId.EXPEDITION, this::add);
		actionMap.put(DailyTaskId.COMPOSE, this::add);
		actionMap.put(DailyTaskId.TRAIN_SOLDIERS, this::add);
		actionMap.put(DailyTaskId.IMPOSE, this::add);
		actionMap.put(DailyTaskId.BUILD_COUNTRY, this::add);
		actionMap.put(DailyTaskId.CITY_WAR, this::add);
		actionMap.put(DailyTaskId.COUNTRY_WAR, this::add);
		actionMap.put(DailyTaskId.WASH_HERO, this::add);
		actionMap.put(DailyTaskId.WASH_EQUIP, this::add);
		actionMap.put(DailyTaskId.MONTH_CARD, this::add);
		actionMap.put(DailyTaskId.RECHARGE, this::add);
	}

	public void record(DailyTaskId taskId, Player player, int countdown) {
		//根据等级开启
//        if (!staticOpenManger.isOpen(OpenConsts.OPEN_69, player)) {
//            return;
//        }
		actionMap.get(taskId).action(player, countdown, taskId.get());
	}

	/**
	 * 设置
	 *
	 * @param player
	 * @param countdowm
	 */
	private void set(Player player, int countdowm, int taskId) {
		StaticTaskDaily daily = dailyTaskMgr.getTaskDaily(taskId);

		int count = player.getPlayerDailyTask().getTaskCount(taskId);
		boolean preComplete = preComplete(player, count, taskId);

		int result = countdowm > daily.getNeedTime() ? daily.getNeedTime() : countdowm;
		player.getPlayerDailyTask().getTaskCount().put(taskId, result);
		pushTaskComplete(player, countdowm, taskId, preComplete);
	}

	/**
	 * 递增
	 *
	 * @param player
	 * @param countdowm
	 * @param taskId
	 */
	private void add(Player player, int countdowm, int taskId) {
		int count = player.getPlayerDailyTask().getTaskCount(taskId);
		boolean preComplete = preComplete(player, count, taskId);
		count += countdowm;
		StaticTaskDaily daily = dailyTaskMgr.getTaskDaily(taskId);
		int result = count > daily.getNeedTime() ? daily.getNeedTime() : count;
		player.getPlayerDailyTask().getTaskCount().put(taskId, result);
		pushTaskComplete(player, count, taskId, preComplete);
		eventManager.dailyTask(player, Lists.newArrayList(
			count,
			countdowm
		));
	}

	/**
	 * 是否已经完成
	 *
	 * @param player
	 * @param cond
	 * @param taskId
	 * @return
	 */
	private boolean preComplete(Player player, int cond, int taskId) {
		StaticTaskDaily staticTaskDaily = dailyTaskMgr.getTaskDaily(taskId);
		PlayerDailyTask playerDailyTask = player.getPlayerDailyTask();
		return cond >= staticTaskDaily.getNeedTime() && playerDailyTask.getTaskState(taskId) == 0;
	}

	/**
	 * @param player
	 * @param cond
	 * @param taskId
	 */
	private void pushTaskComplete(Player player, int cond, int taskId, boolean preComplete) {
		if (preComplete) {// 已经完成,已经推送
			return;
		}
		StaticTaskDaily staticTaskDaily = dailyTaskMgr.getTaskDaily(taskId);
		PlayerDailyTask playerDailyTask = player.getPlayerDailyTask();
		boolean isComplete = cond >= staticTaskDaily.getNeedTime() && playerDailyTask.getTaskState(taskId) == 0;
		if (isComplete) {
			DailyTaskPb.SynDailyTaskRq builder = DailyTaskPb.SynDailyTaskRq.newBuilder()
				.setTask(DailyTaskPb.DailyTask.newBuilder()
					.setId(staticTaskDaily.getId())
					.setName(staticTaskDaily.getName())
					.setNeedTime(staticTaskDaily.getNeedTime())
					.setAward(staticTaskDaily.getAward())
					.setAsset(staticTaskDaily.getAsset())
					.setTime(cond)
					.setState(playerDailyTask.getTaskState(staticTaskDaily.getId()))
					.build())
				.setComplate(taskManager.getComplateTask(player))
				.build();
			SynHelper.synMsgToPlayer(player, DailyTaskPb.SynDailyTaskRq.EXT_FIELD_NUMBER, DailyTaskPb.SynDailyTaskRq.ext, builder);
		}
	}

	public void completeAllTask(Player player) {
		for (Map.Entry<DailyTaskId, BaseDailyTask> entry : actionMap.entrySet()) {
			entry.getValue().action(player, 5000, entry.getKey().get());
		}
	}
}

