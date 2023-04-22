package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.DailyTaskMgr;
import com.game.dataMgr.StaticOpenManger;
import com.game.domain.Player;
import com.game.domain.p.PlayerDailyTask;
import com.game.domain.s.StaticTaskDaily;
import com.game.domain.s.StaticTaskDailyAward;
import com.game.log.consumer.EventManager;
import com.game.manager.ActivityManager;
import com.game.manager.PlayerManager;
import com.game.manager.TaskManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.DailyTaskPb;
import com.game.util.BasePbHelper;
import com.game.util.PbHelper;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zcp
 * @date 2021/3/2 14:04 诵我真名者,永不见bug
 */
@Service
public class DailyTaskService {

	private static final int complate = 1;
	@Autowired
	private DailyTaskMgr dailyTaskMgr;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private StaticOpenManger staticOpenManger;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	ActivityEventManager activityEventManager;

	/**
	 * 每日任务列表
	 *
	 * @param handler
	 */
	public void dailyTask(ClientHandler handler, DailyTaskPb.DailyTaskRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		PlayerDailyTask dailyTask = player.getPlayerDailyTask();
		DailyTaskPb.DailyTaskRs.Builder builder = DailyTaskPb.DailyTaskRs.newBuilder();

		//根据等级开启
		if (!staticOpenManger.isOpen(OpenConsts.OPEN_69, player)) {
			builder.setIsOpen(0);
			handler.sendMsgToPlayer(DailyTaskPb.DailyTaskRs.ext, builder.build());
			return;
		}
		builder.setIsOpen(1);
		builder.setActive(dailyTask.getActive());
		long time = TimeHelper.getZeroTimeMs();
		builder.setRefushTime(time);
		dailyTaskMgr.getDailyMap().values().forEach(e -> {
			builder.addTask(DailyTaskPb.DailyTask.newBuilder()
				.setId(e.getId())
				.setName(e.getName())
				.setNeedTime(e.getNeedTime())
				.setAward(e.getAward())
				.setAsset(e.getAsset())
				.setTime(dailyTask.getNeedTime(e.getId()))
				.setState(dailyTask.getTaskState(e.getId()))
				.build());
		});
		dailyTaskMgr.getDailyAwardMap().values().forEach(e -> {
			builder.addActiveAward(DailyTaskPb.ActiveAward.newBuilder()
				.setId(e.getId())
				.setState(dailyTask.getActiveState(e.getId()))
				.addAllAward(PbHelper.createListAward(e.getAward()))
				.setActive(e.getNeedNum())
				.build());
		});
		handler.sendMsgToPlayer(DailyTaskPb.DailyTaskRs.ext, builder.build());
	}

	/**
	 * 每日任务奖励领取
	 *
	 * @param handler
	 */
	public void dailyTaskComplete(ClientHandler handler, DailyTaskPb.DailyTaskCompleteRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int taskId = rq.getTaskId();
		StaticTaskDaily staticTaskDaily = dailyTaskMgr.getTaskDaily(taskId);

		PlayerDailyTask playerDailyTask = player.getPlayerDailyTask();

		int times = playerDailyTask.getNeedTime(taskId);
		if (times < staticTaskDaily.getNeedTime()) {
			handler.sendErrorMsgToPlayer(GameError.DAILY_TASK_TIME);
			return;
		}
		int state = playerDailyTask.getTaskState(taskId);
		if (state == complate) {
			handler.sendErrorMsgToPlayer(GameError.DAILY_TASK_COMPLETE);
			return;
		}
		//设置已完成
		playerDailyTask.getTaskState().put(taskId, 1);
		playerDailyTask.addActive(staticTaskDaily.getAward());
		DailyTaskPb.DailyTaskCompleteRs.Builder builder = DailyTaskPb.DailyTaskCompleteRs.newBuilder();
		builder.setActive(playerDailyTask.getActive());
		builder.setTask(DailyTaskPb.DailyTask.newBuilder()
			.setId(staticTaskDaily.getId())
			.setName(staticTaskDaily.getName())
			.setNeedTime(staticTaskDaily.getNeedTime())
			.setAward(staticTaskDaily.getAward())
			.setAsset(staticTaskDaily.getAsset())
			.setTime(times)
			.setState(complate)
			.build());
		builder.setComplate(taskManager.getComplateTask(player));
		handler.sendMsgToPlayer(DailyTaskPb.DailyTaskCompleteRs.ext, builder.build());
		activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.DAILY_TASK, 0, 1);
		eventManager.dailyActive(player, Lists.newArrayList(
			staticTaskDaily.getAward()
		));
		//更新通行证任务
//        activityManager.updatePassPort(player,playerDailyTask.getActive());
		activityEventManager.activityTip(EventEnum.DAILY_ACTIVITY, player, staticTaskDaily.getAward(), 0);
	}

	/**
	 * 活跃奖励领取
	 *
	 * @param handler
	 * @param rq
	 */
	public void dailyActiveAward(ClientHandler handler, DailyTaskPb.DailyActiveAwardRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int activeId = rq.getActiveId();
		PlayerDailyTask playerDailyTask = player.getPlayerDailyTask();
		DailyTaskPb.DailyActiveAwardRs.Builder builder = DailyTaskPb.DailyActiveAwardRs.newBuilder();
		for (StaticTaskDailyAward dailyAward : dailyTaskMgr.getDailyAwardMap().values()) {
			boolean record = activeId == dailyAward.getId() || activeId == 0;
			if (record) {
				//活跃不够
				if (playerDailyTask.getActive() < dailyAward.getNeedNum()) {
					continue;
				}
				//已领取
				if (playerDailyTask.getActiveState(dailyAward.getId()) == complate) {
					continue;
				}
				builder.addAllAward(PbHelper.createListAward(dailyAward.getAward()));
				dailyAward.getAward().forEach(award -> {
					playerManager.addAward(player, award.get(0), award.get(1), award.get(2), Reason.DAILY_ACTIVE);
				});
				playerDailyTask.getActiveState().put(dailyAward.getId(), complate);
				builder.addActiveState(CommonPb.TwoInt.newBuilder()
					.setV1(dailyAward.getId())
					.setV2(complate).build());
			}
		}
		handler.sendMsgToPlayer(DailyTaskPb.DailyActiveAwardRs.ext, builder.build());
	}


}
