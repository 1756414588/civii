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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 按当前排名计算 RANK_2
 */
@Component
public class RankTipEvent extends BaseActivityEvent {

	//private static RankTipEvent inst = new RankTipEvent();
	//
	//public static RankTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_LEVEL_RANK, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_ORANGE_DIAL, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_PURPLE_DIAL, this::process);
//		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_HERO_DIAL, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();


//		LogHelper.MESSAGE_LOGGER.info("RankTipEvent activityId:{}", activityBase.getActivityId());

		ActivityData activityData = activityManager.getActivity(activityBase);
		if (activityData == null) {
			return;
		}

		ActPlayerRank actRank = activityData.getLordRank(player.roleId);
		if (actRank == null || actRank.getRank() <= 0) {
			return;
		}

		int rank = actRank.getRank();
		Map<Integer, Integer> received = actRecord.getReceived();

		// 奖励列表
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		List<StaticActAward> list = awardList.stream().filter(e -> e.getCond() >= rank && !received.containsKey(e.getKeyId())).collect(Collectors.toList());

		if (!list.isEmpty()) {
			ActivityEventResult result = new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true);
			actor.setResult(result);
			return;
		}

		// 没有奖励可领取了,左侧活动上红点关闭
		ActivityEventResult result = new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false);
		actor.setResult(result);
	}

}
