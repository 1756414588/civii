package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 大杀四方
 */
public class ActKillAllEvent extends BaseActivityEvent {

	private static ActKillAllEvent inst = new ActKillAllEvent();

	public static ActKillAllEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_KILL_ALL, this::process);
		listenEvent(EventEnum.KILL_PLAYER_SOIDLER, this::killPlayerSoilder);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		// 击杀数量
		int record = actRecord.getRecord(1);
		Map<Integer, Integer> received = actRecord.getReceived();
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());

		Optional<StaticActAward> optional = awardList.stream().filter(e -> e.getCond() <= record && !received.containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		// 没有奖励可领取了,左侧活动上红点关闭
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));

	}

	/**
	 * 击杀累积
	 *
	 * @param activityEnum
	 * @param actor
	 */
	public void killPlayerSoilder(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);

		Map<Integer, Integer> received = actRecord.getReceived();
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());

		int record = actRecord.getRecord(1);
		Optional<StaticActAward> optional = awardList.stream().filter(e -> e.getCond() <= record && !received.containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		// 没有奖励可领取了,左侧活动上红点关闭
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
