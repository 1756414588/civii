package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.actor.EquipAddActor;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.constant.Quality;
import com.game.constant.TaskType;
import com.game.domain.Player;
import com.game.domain.p.ActPassPortTask;
import com.game.domain.p.ActRecord;
import com.game.domain.p.Tech;
import com.game.domain.p.TechInfo;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticEquip;
import com.game.domain.s.StaticHeroTask;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 王牌球手
 *
 *
 * @date 2021/9/6 14:11
 */
@Component
public class ActTaskHeroTipEvent extends BaseActivityEvent {
	//
	//private static ActTaskHeroTipEvent inst = new ActTaskHeroTipEvent();
	//
	//public static ActTaskHeroTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		this.listenEvent(EventEnum.KILL_MONSTER, ActivityConst.ACT_TASK_HERO, this::killMonster);
		this.listenEvent(EventEnum.SUB_GOLD, ActivityConst.ACT_TASK_HERO, this::subGold);
		this.listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_TASK_HERO, this::process);
		this.listenEvent(EventEnum.EQUIP_ADD, this::equipAdd);
		this.listenEvent(EventEnum.BUILD_UP_FINISH, ActivityConst.ACT_TASK_HERO, this::buildLvUp);
		this.listenEvent(EventEnum.TECH_UP_FINISH, ActivityConst.ACT_TASK_HERO, this::techUpFinish);
		this.listenEvent(EventEnum.CAPTURE_CITY, ActivityConst.ACT_TASK_HERO, this::camptureCity);
	}

	public void subGold(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();
		List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
		if (condList == null || condList.size() == 0) {
			return;
		}

		if (tasks.size() == 0) {
			condList.forEach(e -> {
				tasks.put(e.getId(), new ActPassPortTask(e));
			});
		}

		Optional<StaticHeroTask> optional = condList.stream().filter(e -> e.getJumpType() == TaskType.COST_GOLD).findFirst();
		if (!optional.isPresent()) {
			return;
		}

		StaticHeroTask staticHeroTask = optional.get();
		ActPassPortTask value = tasks.get(staticHeroTask.getId());

		// 已经领奖
		if (actRecord.getReceived().containsKey(staticHeroTask.getId())) {
			return;
		}

		// 已经达成领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			return;
		}

		int totalProcess = value.getProcess() + actor.getChange();
		value.setProcess(totalProcess);

		// 完成奖励,且没有领取
		if (totalProcess >= staticHeroTask.getCond()) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void killMonster(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();
		List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
		if (condList == null || condList.size() == 0) {
			return;
		}
		if (tasks.size() == 0) {
			condList.forEach(e -> {
				tasks.put(e.getId(), new ActPassPortTask(e));
			});
		}

		Optional<StaticHeroTask> optional = condList.stream().filter(e -> e.getJumpType() == TaskType.KILL_REBEL).findFirst();
		if (!optional.isPresent()) {
			return;
		}

		StaticHeroTask staticHeroTask = optional.get();
		ActPassPortTask value = tasks.get(staticHeroTask.getId());

		// 已经领奖
		if (actRecord.getReceived().containsKey(staticHeroTask.getId())) {
			return;
		}

		// 已经达成领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			return;
		}

		// 添加记录
		if (staticHeroTask.getParam().contains(actor.getParam2())) {
			int totalProcess = value.getProcess() + 1;
			value.setProcess(totalProcess);

			// 完成任务,可领奖
			if (value.getProcess() >= staticHeroTask.getCond()) {
				actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
			}
		}
	}

	/**
	 * 装备收集
	 *
	 * @param activityEnum
	 * @param actor
	 */
	public void equipAdd(EventEnum activityEnum, IActivityActor actor) {
		EquipAddActor equipAddActor = (EquipAddActor) actor;
		Player player = actor.getPlayer();

		StaticEquip staticEquip = equipAddActor.getStaticEquip();
		if (staticEquip.getQuality() < Quality.BLUE.get()) {
			return;
		}

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_TASK_HERO);
		if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			return;
		}

		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();

		List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
		if (condList == null || condList.size() == 0) {
			return;
		}

		if (tasks.size() == 0) {
			condList.forEach(e -> {
				tasks.put(e.getId(), new ActPassPortTask(e));
			});
		}

		Optional<StaticHeroTask> optional = condList.stream().filter(e -> e.getJumpType() == TaskType.COLLECT_EQUIP).findFirst();
		if (!optional.isPresent()) {
			return;
		}

		StaticHeroTask staticHeroTask = optional.get();
		ActPassPortTask value = tasks.get(staticHeroTask.getId());

		// 已经领奖
		if (actRecord.getReceived().containsKey(staticHeroTask.getId())) {
			return;
		}

		// 已经达成领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			return;
		}

		int totalProcess = value.getProcess() + 1;
		value.setProcess(totalProcess);

		// 完成任务,可领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void buildLvUp(EventEnum activityEnum, IActivityActor actor) {

		Player player = actor.getPlayer();
		int buildType = actor.getChange();
		int buildLv = actor.getParam2();

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_TASK_HERO);
		if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			return;
		}

		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();

		List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
		if (condList == null || condList.size() == 0) {
			return;
		}
		Optional<StaticHeroTask> optional = condList.stream().filter(e -> e.getJumpType() == TaskType.BUILDING_LEVELUP).findFirst();
		if (!optional.isPresent()) {
			return;
		}

		StaticHeroTask staticHeroTask = optional.get();
		int type = staticHeroTask.getParam().get(0);
		int level = staticHeroTask.getParam().get(1);
		if (type != buildType || level > buildLv) {
			return;
		}

		if (tasks.size() == 0) {
			condList.forEach(e -> {
				tasks.put(e.getId(), new ActPassPortTask(e));
			});
		}

		ActPassPortTask value = tasks.get(staticHeroTask.getId());

		// 已经领奖
		if (actRecord.getReceived().containsKey(staticHeroTask.getId())) {
			return;
		}

		// 已经达成领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			return;
		}

		value.setProcess(buildLv);

		// 完成任务,可领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void techUpFinish(EventEnum activityEnum, IActivityActor actor) {

		Player player = actor.getPlayer();
		int techType = actor.getChange();
		int techLv = actor.getParam2();

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_TASK_HERO);
		if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			return;
		}

		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();

		List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
		if (condList == null || condList.size() == 0) {
			return;
		}

		// 获取科技升级
		Optional<StaticHeroTask> optional = condList.stream().filter(e -> e.getJumpType() == TaskType.FINISH_TECH && e.getParam().get(0) == techType).findFirst();
		if (!optional.isPresent()) {
			return;
		}

		if (tasks.size() == 0) {
			condList.forEach(e -> {
				tasks.put(e.getId(), new ActPassPortTask(e));
			});
		}

		StaticHeroTask staticHeroTask = optional.get();
		ActPassPortTask value = tasks.get(staticHeroTask.getId());

		// 已经领奖
		if (actRecord.getReceived().containsKey(staticHeroTask.getId())) {
			return;
		}

		// 已经达成领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			return;
		}

		value.setProcess(value.getProcess() + 1);

		// 完成任务,可领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void camptureCity(EventEnum activityEnum, IActivityActor actor) {

		Player player = actor.getPlayer();
		int cityType = actor.getChange();

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_TASK_HERO);
		if (activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			return;
		}

		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();

		List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
		if (condList == null || condList.size() == 0) {
			return;
		}

		Optional<StaticHeroTask> optional = condList.stream().filter(e -> e.getJumpType() == TaskType.CAPTURE_CITY && e.getParam().contains(cityType)).findFirst();
		if (!optional.isPresent()) {
			return;
		}

		if (tasks.size() == 0) {
			condList.forEach(e -> {
				tasks.put(e.getId(), new ActPassPortTask(e));
			});
		}

		StaticHeroTask staticHeroTask = optional.get();
		ActPassPortTask value = tasks.get(staticHeroTask.getId());

		// 已经领奖
		if (actRecord.getReceived().containsKey(staticHeroTask.getId())) {
			return;
		}

		// 已经达成领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			return;
		}

		value.setProcess(value.getProcess() + 1);

		// 完成任务,可领奖
		if (value.getProcess() >= staticHeroTask.getCond()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {

		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
		if (condList == null || condList.size() == 0) {
			return;
		}

		boolean isComplate = false;
		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();

		// 遍历
		for (StaticHeroTask e : condList) {
			int taskType = e.getJumpType();
			if (taskType == TaskType.BUILDING_LEVELUP) {
				int totalProcess = player.getBuildingLv(e.getParam().get(0));
				if (totalProcess >= e.getCond() && !actRecord.getReceived().containsKey(e.getId())) {
					isComplate = true;
					break;
				}
			} else if (taskType == TaskType.FINISH_TECH) {
				Tech tech = player.getTech();
				if (tech != null) {
					TechInfo info = tech.getTechInfo(e.getParam().get(0));
					if (info != null && info.getLevel() >= e.getCond() && !actRecord.getReceived().containsKey(e.getId())) {
						isComplate = true;
						break;
					}
				}
			} else {
				ActPassPortTask actPassPortTask = tasks.get(e.getId());
				int process = actPassPortTask.getProcess();
				if (process >= e.getCond() && !actRecord.getReceived().containsKey(e.getId())) {
					isComplate = true;
					break;
				}
			}

			playerManager.synActivity(player, ActivityConst.ACT_TASK_HERO);

		}

		// 奖励已经领取完
		if (!isComplate) {
			// 判断是否已领完6个奖励,并且球王没有领
			if (actRecord.getReceived().size() >= 6 && !actRecord.getReceived().containsKey(0)) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			} else {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			}
		}
	}
}
