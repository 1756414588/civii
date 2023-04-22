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
import com.game.domain.s.StaticDialAwards;

import com.game.spring.SpringUtil;
import java.util.List;

/**
 * 充值转盘
 */
public class ActRaidersTipEvent extends BaseActivityEvent {

	private static ActRaidersTipEvent inst = new ActRaidersTipEvent();

	public static ActRaidersTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
//		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_RAIDERS, this::actRaiders);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_RAIDERS, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_LUCKLY_EGG, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);

		// 免费次数，总次数
		int free = actRecord.getRecord().size() > 0 ? 0 : 1;
		int readyCount = actRecord.getCount() + free + actRecord.getRecord().size();

		// 最终奖励
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (awardList == null || awardList.isEmpty()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			return;
		}

		StaticActAward award = awardList.get(0);

		// 已经领取最终奖励
		if (actRecord.getReceived().containsKey(award.getKeyId())) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			return;
		}

		List<StaticDialAwards> actAwardList = staticActivityMgr.getStaticDialAwardsList(activityBase.getAwardId());
		int totalSize = actAwardList.size();
		if (readyCount >= totalSize) {// 推送下红点,次数已经完成，最终奖励未领取
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		long state = actRecord.getStatus(0);
		// 从第几档开始计算
		for (int i = readyCount; i < totalSize; i++) {
			StaticDialAwards config = actAwardList.get(i);
			if (state >= config.getCond() && config.getCond() > 0) {
				if (actRecord.getCount() < totalSize - 1) {
					actRecord.addCount();
					state -= config.getCond();
					actRecord.putState(0, state);
				}
			}
		}
		// 推送下红点
		if (actRecord.getCount() > 0) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		} else {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}

	public void actRaiders(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
//		if (actRecord.getCount() == 0 && actRecord.getRecord().size() == 0) {
//			actRecord.addCount();
//		}
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);

		// 这个是总次数
		int readyCount = actRecord.getCount() + actRecord.getRecord().size();

		List<StaticDialAwards> actAwardList = staticActivityMgr.getStaticDialAwardsList(activityBase.getAwardId());
		// 抽满了就不给了
		if (readyCount >= actAwardList.size()) {
			StaticActAward award = staticActivityMgr.getActAward(activityBase.getActivityId());
			if (actRecord.getRecord().size() >= award.getCond() && !actRecord.getReceived().containsKey(award.getKeyId())) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			} else {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			}
			return;
		}
		long state = actRecord.getStatus(0);
		// 从第几档开始计算
		for (int i = readyCount; i < actAwardList.size(); i++) {
			StaticDialAwards config = actAwardList.get(i);
			if (state >= config.getCond() && config.getCond() > 0) {
				actRecord.addCount();
				state -= config.getCond();
				actRecord.putState(0, state);
			}
		}
		// 推送下红点
		if (actRecord.getCount() > 0) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		} else {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}

	/**
	 * 幸运砸蛋
	 *
	 * @param eventEnum
	 * @param actor
	 */
	public void actLuckAge(EventEnum eventEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
//		if (actRecord.getCount() == 0 && actRecord.getRecord().size() == 0) {
//			actRecord.addCount();
//		}
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> rewardList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (rewardList == null || rewardList.isEmpty()) {
			return;
		}

		// 免费次数，总次数
		int free = actRecord.getRecord().size() > 0 ? 0 : 1;
		int readyCount = actRecord.getCount() + free + actRecord.getRecord().size();

		// 最终奖励
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (awardList == null || awardList.isEmpty()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			return;
		}

		StaticActAward award = awardList.get(0);

		// 已经领取最终奖励
		if (actRecord.getReceived().containsKey(award.getKeyId())) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			return;
		}

		List<StaticDialAwards> actAwardList = staticActivityMgr.getStaticDialAwardsList(activityBase.getAwardId());
		int totalSize = actAwardList.size();
		if (readyCount >= totalSize) {// 推送下红点,次数已经完成，最终奖励未领取
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		long state = actRecord.getStatus(0);
		// 从第几档开始计算
		for (int i = readyCount; i < actAwardList.size(); i++) {
			StaticDialAwards config = actAwardList.get(i);
			if (state >= config.getCond() && config.getCond() > 0) {
				actRecord.addCount();
				state -= config.getCond();
				actRecord.putState(0, state);
			}
		}
		// 推送下红点
		if (actRecord.getCount() > 0) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		} else {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}
}
