package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActSevenConst;
import com.game.constant.ActivityConst;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActSeven;
import com.game.util.DateHelper;
import com.game.util.TimeHelper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 七日狂欢实践
 *
 * @author zcp
 * @date 2021/8/30 9:53
 */
@Component
public class ActSevenTipEvent extends BaseActivityEvent {

	//private static ActSevenTipEvent inst = new ActSevenTipEvent();
	//
	//public static ActSevenTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		this.listenEvent(EventEnum.KILL_MONSTER, ActivityConst.ACT_SEVEN, this::killMonster);
		this.listenEvent(EventEnum.MARKET_OPEN, ActivityConst.ACT_SEVEN, this::markeyOpen);
		this.listenEvent(EventEnum.EQUIP_WASH, ActivityConst.ACT_SEVEN, this::equipWash);
		this.listenEvent(EventEnum.EQUIP_ADD, this::equipAdd);
	}

	public void equipWash(EventEnum activityEnum, IActivityActor actor) {
		if (beforeCheck(actor)) {
			return;
		}
		long status = calcuProgress(actor, ActSevenConst.WASH_EQUPT);
		boolean flag = checkFlag(ActSevenConst.WASH_EQUPT, status, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void markeyOpen(EventEnum activityEnum, IActivityActor actor) {
		if (beforeCheck(actor)) {
			return;
		}

		long status = calcuProgress(actor, ActSevenConst.Marker_Flop);
		boolean flag = checkFlag(ActSevenConst.Marker_Flop, status, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void equipAdd(EventEnum eventEnum, IActivityActor actor) {
		if (beforeCheck(actor)) {
			return;
		}
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_SEVEN);
		if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			return;
		}

		// 获取玩家角色创建日期与当前的时间差
		Player player = actor.getPlayer();
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			return;
		}

		int day = checkDay(player.account.getCreateDate());

		// 筛选装备拥有
		List<StaticActSeven> list = staticActivityMgr.getSevens().values().stream().filter(e -> e.getType() == 3 && e.getSortId() / 1000 == 8 && e.getDay() <= day).collect(Collectors.toList());
		for (StaticActSeven e : list) {
			int sortId = e.getSortId();
			int quality = (sortId % 1000) / 100;
			int equipType = sortId % 100;
			int status = player.getEquipMake(quality, equipType);
			if (status >= e.getCond() && !actRecord.getReceived().containsKey(e.getKeyId())) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}
	}

	public void killMonster(EventEnum activityEnum, IActivityActor actor) {
		if (beforeCheck(actor)) {
			return;
		}
		ActRecord actRecord = actor.getActRecord();
		// 获取玩家角色创建日期与当前的时间差
		Player player = actor.getPlayer();
		int day = checkDay(player.account.getCreateDate());

		long status = calcuProgress(actor, ActSevenConst.KILL_REBEL_NEW);
		long killRebel = 0;
		if (actor.getParam2() >= 6 && actor.getParam2() <= 10) {
			killRebel = calcuProgress(actor, ActSevenConst.KILL_REBEL);
		}
		boolean flag = false;
		for (StaticActSeven config : staticActivityMgr.getSevens().values()) {
			if (config.getSortId() == ActSevenConst.KILL_REBEL_NEW && config.getDay() <= day) {
				if (config.getCond() <= status) {
					// 已领取奖励
					if (!actRecord.getReceived().containsKey(config.getKeyId())) {
						flag = true;
						break;
					}
				}
			} else if (config.getSortId() == ActSevenConst.KILL_REBEL && config.getDay() <= day) {
				if (config.getCond() <= killRebel) {
					// 已领取奖励
					if (!actRecord.getReceived().containsKey(config.getKeyId())) {
						flag = true;
						break;
					}
				}
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private boolean checkFlag(int taskType, long status, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		// 获取玩家角色创建日期与当前的时间差
		Date createDate = actor.getPlayer().account.getCreateDate();
		int day = checkDay(createDate);
		boolean flag = false;
		for (StaticActSeven config : staticActivityMgr.getSevens().values()) {
			if (config.getSortId() == taskType && config.getDay() <= day) {
				if (config.getCond() <= status) {
					// 已领取奖励
					if (!actRecord.getReceived().containsKey(config.getKeyId())) {
						flag = true;
						break;
					}
				}
			}
		}
		return flag;
	}

	private boolean beforeCheck(IActivityActor actor) {
		Player player = actor.getPlayer();
		Date createDate = player.account.getCreateDate();
		int dayiy = DateHelper.dayiy(createDate, new Date());
		if (dayiy > 7) {
			return true;
		}
		return false;
	}

	private long calcuProgress(IActivityActor actor, int taskType) {
		ActRecord actRecord = actor.getActRecord();
		long status = actRecord.getStatus(taskType);
		status += actor.getChange();
		actRecord.putState(taskType, status);
		return status;
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {

	}

	private int checkDay(Date createDate) {
		//判断玩家创建角色时间
		Date currentDate = new Date();
		int day = TimeHelper.whichDay(0, currentDate, createDate);

//		LogHelper.GAME_LOGGER.info("创建时间{} 当前日期 {} 计算 {}", createDate, currentDate, day);
		return day;
	}

}
