package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.constant.BuildingType;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActCommand;
import com.game.manager.ActivityManager;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 不懂看这个例子 基地升级
 */
public class ActBuildingLevelTipEvent extends BaseActivityEvent {

	private static ActBuildingLevelTipEvent inst = new ActBuildingLevelTipEvent();

	public static ActBuildingLevelTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_LEVEL, this::process);
		listenEvent(EventEnum.BUILD_UP_FINISH, ActivityConst.ACT_LEVEL, this::commandLevelUp);
		listenEvent(EventEnum.UP_LEVEL, ActivityConst.ACT_LEVEL, this::levelUp);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();
		int commandLv = player.getCommandLv();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		Map<Integer, StaticActCommand> condMap = staticActivityMgr.getActCommands();
		if (condMap == null) {
			return;
		}

		Map<Integer, Integer> received = actRecord.getReceived();
		Optional<StaticActCommand> optional = condMap.values().stream().filter(e -> e.getLevel() <= commandLv && isComplete(player, e) && !received.containsKey(e.getKeyId())).findFirst();

		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}

	/**
	 * 司令部升级
	 *
	 * @param activityEnum
	 * @param actor
	 */
	public void commandLevelUp(EventEnum activityEnum, IActivityActor actor) {
		int buildingType = actor.getChange();
		Player player = actor.getPlayer();
		if (buildingType != BuildingType.COMMAND && buildingType != BuildingType.TECH) {
			return;
		}

		int commandLv = player.getBuildingLv(BuildingType.COMMAND);

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_LEVEL);

		// 活动未开启
		if (activityBase == null) {
			return;
		}

		Map<Integer, StaticActCommand> condMap = staticActivityMgr.getActCommands();
		if (condMap == null) {
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);


		Map<Integer, Integer> received = actRecord.getReceived();
		Optional<StaticActCommand> optional = condMap.values().stream().filter(e -> e.getLevel() <= commandLv && isComplete(player, e) && !received.containsKey(e.getKeyId())).findFirst();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}

	public void levelUp(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();

		int commandLv = player.getBuildingLv(BuildingType.COMMAND);
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_LEVEL);

		// 活动未开启
		if (activityBase == null) {
			return;
		}

		Map<Integer, StaticActCommand> condMap = staticActivityMgr.getActCommands();
		if (condMap == null) {
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		Map<Integer, Integer> received = actRecord.getReceived();

		Optional<StaticActCommand> optional = condMap.values().stream().filter(e -> e.getLevel() <= commandLv && isComplete(player, e) && !received.containsKey(e.getKeyId())).findFirst();

		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
	}


	private boolean isComplete(Player player, StaticActCommand staticActCommand) {
		if (staticActCommand == null) {
			return true;
		}
		List<Integer> paramList = staticActCommand.getLimit();
		int type = paramList.get(0);
		int value = paramList.get(1);
		if (type == 2) {// 科技馆等级
			return player.getBuildingLv(BuildingType.TECH) >= value;
		} else if (type == 3) {// 指挥官等级
			return player.getLevel() >= value;
		}
		return true;
	}
}
