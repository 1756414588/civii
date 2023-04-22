package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActPlayerRank;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.domain.s.StaticActRankDisplay;
import com.game.util.TimeHelper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 消费排行榜
 */
@Component
public class ActCostRankTipEvent extends BaseActivityEvent {

	//private static ActCostRankTipEvent inst = new ActCostRankTipEvent();
	//
	//public static ActCostRankTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_COST_GOLD, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_MENTOR_SCORE, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_TOPUP_RANK, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();

		Date rewardTime = TimeHelper.getRewardTime(activityBase.getEndTime());
		Date now = new Date();
		if (now.before(rewardTime)) {
			return;
		}
		ActivityData activityData = activityManager.getActivity(activityBase);
		if (activityData == null) {
			return;
		}

		ActPlayerRank actPlayerRank = activityData.getLordRank(player.getRoleId());
		if (actPlayerRank == null) {
			return;
		}
		int rank = actPlayerRank.getRank();

		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(actRecord.getAwardId());
		if (displayList == null) {
			return;
		}

//		// 目标排行奖励
		int tempRank = activityManager.obtainAwardGear(rank, displayList);

		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		Optional<StaticActAward> opt = actAwardList.stream().filter(e -> e.getCond() == tempRank).findAny();
		if (!opt.isPresent()) {// 没有奖励可领取了,左侧活动上红点关闭
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
			return;
		}

		StaticActAward staticActAward = opt.get();

		// 奖励未领取
		if (!actRecord.getReceived().containsKey(staticActAward.getKeyId())) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
