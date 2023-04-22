package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Optional;

/**
 * 成长基金
 */
public class InvestTipEvent extends BaseActivityEvent {

	private static InvestTipEvent inst = new InvestTipEvent();

	public static InvestTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_INVEST, this::process);
		listenEvent(EventEnum.UP_LEVEL, ActivityConst.ACT_INVEST, this::levelUp);
		listenEvent(EventEnum.PAY, ActivityConst.ACT_INVEST, this::levelUp);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();
		long status = actRecord.getStatus(0);
		if (status != 1) {//未参与
			if (player.getVip() >= 3 && player.getGold() >= 1000) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			return;
		}

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);

		// 活动奖励列表
		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		int level = player.getLevel();

		// 条件足够领奖
		Optional<StaticActAward> optional = actAwardList.stream().filter(e -> e.getCond() <= level && !actRecord.getReceived().containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {//还存在可领未领的奖励
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}

	public void levelUp(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();
		long status = actRecord.getStatus(0);
		if (status != 1) {//未参与
			if (player.getVip() >= 3 && player.getGold() >= 1000) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
			return;
		}
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);

		// 活动奖励列表
		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		int level = player.getLevel();

		// 升级可领取奖励
		Optional<StaticActAward> optional = actAwardList.stream().filter(e -> e.getCond() <= level && !actRecord.getReceived().containsKey(e.getKeyId())).findFirst();
		if (optional.isPresent()) {//还存在可领未领的奖励
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
	}

}
