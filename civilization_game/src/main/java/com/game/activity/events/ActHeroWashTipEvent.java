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
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Optional;

/**
 * 不懂看这个例子 基地升级
 */
public class ActHeroWashTipEvent extends BaseActivityEvent {

	private static ActHeroWashTipEvent inst = new ActHeroWashTipEvent();

	public static ActHeroWashTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.HERO_WASH, ActivityConst.ACT_HERO_WASH, this::heroWash);
	}

	public void heroWash(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		long status = actRecord.getStatus(0L);
		status += actor.getChange();
		actRecord.putState(0L, status);
		this.process(activityEnum, actor);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
//		LogHelper.MESSAGE_LOGGER.info("ActBuildingLevelTipEvent");
		ActRecord actRecord = actor.getActRecord();
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (awardList != null) {
			int got = (int) actRecord.getStatus(0L);
			Optional optional = awardList.stream().filter(e -> {
				boolean canAward = got >= e.getCond();
				// 已领取奖励
				if (!actRecord.getReceived().containsKey(e.getKeyId()) && got >= e.getCond()) {
					return true;
				}
				return false;
			}).findFirst();
			if (optional.isPresent()) {
				actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
			}
		}
	}
}
