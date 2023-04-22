package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.p.ActRecord;
import com.game.domain.s.StaticActAward;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 寻宝之路
 */
public class ActSearchTipEvent extends BaseActivityEvent {

	private static ActSearchTipEvent inst = new ActSearchTipEvent();

	public static ActSearchTipEvent getInst() {
		return inst;
	}


	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_SEARCH, this::reward);
		listenEvent(EventEnum.PAY, ActivityConst.ACT_SEARCH, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());

		// 第几天
		int day = actRecord.getCount();
		if (day <= 0) {
			return;
		}
		//判定前一天是否完成 如果完成就加一天 开启一个新宝箱
		List<StaticActAward> list = condList.stream().filter(e -> e.getSortId() <= day && !actRecord.getReceived().containsKey(e.getKeyId())).collect(Collectors.toList());
		if (list == null || list.isEmpty()) {
			return;
		}
		boolean flag = false;
		for (StaticActAward e : list) {
			// 奖励已经领取
			if (actRecord.getReceived().containsKey(e.getKeyId())) {
				continue;
			}
			//
			actRecord.getStatus(e.getSortId());
			int status = (int) actRecord.getStatus(e.getSortId());// 充值金额
			if (status >= e.getCond()) {
				flag = true;
				break;
			}
		}

		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void reward(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());

		// 第几天
		int day = actRecord.getCount();
		if (day <= 0) {
			return;
		}
		//判定前一天是否完成 如果完成就加一天 开启一个新宝箱
		List<StaticActAward> list = condList.stream().filter(e -> e.getSortId() <= day && !actRecord.getReceived().containsKey(e.getKeyId())).collect(Collectors.toList());
		if (list == null || list.isEmpty()) {// 没有可领取的
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, false));
			return;
		}
		boolean flag = false;
		for (StaticActAward e : list) {
			// 奖励已经领取
			if (actRecord.getReceived().containsKey(e.getKeyId())) {
				continue;
			}
			//
			actRecord.getStatus(e.getSortId());
			int status = (int) actRecord.getStatus(e.getSortId());// 充值金额
			if (status >= e.getCond()) {
				flag = true;
				break;
			}
		}

		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		} else {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}

	/**
	 * @param eventEnum
	 * @param actor
	 */
	public void pay(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());

		// 第几天
		int day = actRecord.getCount();
		if (day <= 0) {
			return;
		}

		//判定前一天是否完成 如果完成就加一天 开启一个新宝箱
		List<StaticActAward> list = condList.stream().filter(e -> e.getSortId() <= day).collect(Collectors.toList());
		if (list == null || list.isEmpty()) {
			return;
		}
		for (StaticActAward e : list) {
			if (actRecord.getReceived().containsKey(e.getKeyId())) {
				continue;
			}
			actRecord.getStatus(e.getSortId());
			int status = (int) actRecord.getStatus(e.getSortId());// 充值金额
			if (status >= e.getCond()) {
				actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}
	}
}
