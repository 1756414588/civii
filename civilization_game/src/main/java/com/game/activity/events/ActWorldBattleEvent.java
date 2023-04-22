package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActWorldBattleConst;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.spring.SpringUtil;
import java.util.List;

/**
 * 世界征战
 *
 *
 */
public class ActWorldBattleEvent extends BaseActivityEvent {

	private static ActWorldBattleEvent inst = new ActWorldBattleEvent();

	public static ActWorldBattleEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_WORLD_BATTLE, this::process);
		listenEvent(EventEnum.KILL_MONSTER, ActivityConst.ACT_WORLD_BATTLE, this::killMonster);
		listenEvent(EventEnum.DROP_DRAWING, this::attackPlayerCityWin);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		long status = actRecord.getStatus(ActWorldBattleConst.KILL_MASTER);
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (null == condList || condList.size() == 0) {
			return;
		}
		boolean flag = false;
		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			if (status >= e.getCond() && e.getSortId() == ActWorldBattleConst.KILL_MASTER) {
				if (!actRecord.getReceived().containsKey(keyId)) {
					flag = true;
					break;
				}
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		} else {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}

	/**
	 * 消灭虫族
	 *
	 * @param event
	 * @param actor
	 */
	public void killMonster(EventEnum event, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		long status = actRecord.getStatus(ActWorldBattleConst.KILL_MASTER);
		status += actor.getChange();
		actRecord.putState(ActWorldBattleConst.KILL_MASTER, status);

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			return;
		}
		boolean flag = false;
		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			if (status >= e.getCond() && e.getSortId() == ActWorldBattleConst.KILL_MASTER) {
				if (!actRecord.getReceived().containsKey(keyId)) {
					flag = true;
					break;
				}
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 收集图纸
	 *
	 * @param event
	 * @param actor
	 */
	public void getPanel(EventEnum event, IActivityActor actor) {

	}

	/**
	 * 击飞城池
	 *
	 * @param event
	 * @param actor
	 */
	public void attackPlayerCityWin(EventEnum event, IActivityActor actor) {

	}

}
