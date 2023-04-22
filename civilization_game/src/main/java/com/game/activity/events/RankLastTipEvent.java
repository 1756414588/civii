package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.CampMembersRank;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.domain.s.StaticActRankDisplay;
import com.game.util.TimeHelper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * 排名奖励，最后一天才能领奖 RANK_3
 */
@Component
public class RankLastTipEvent extends BaseActivityEvent {

	//private static RankLastTipEvent inst = new RankLastTipEvent();
	//
	//public static RankLastTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_TOPUP_RANK, this::process);
//		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_MENTOR_SCORE, this::process);
	}


	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();
		int activityId = actor.getActivityId();

//		LogHelper.MESSAGE_LOGGER.info("RankLastTipEvent activityId:{}", actor.getActivityId());

		//StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		//ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);

		Date rewardTime = TimeHelper.getRewardTime(activityBase.getEndTime());
		Date now = new Date();
		if (now.before(rewardTime)) {
			return;
		}
		ActivityData activityData = activityManager.getActivity(activityBase);
		if (activityData == null) {
			return;
		}
		int rank = 0;

		CampMembersRank campMembersRank = activityData.getCampMembersRank(player);
		if (campMembersRank == null) {
			return;
		}
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(actRecord.getAwardId());
		if (displayList == null) {
			return;
		}

		// 排行
		rank = campMembersRank.getRank();
		if (rank == 0) {
			return;
		}

		// 目标排行奖励
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

		// 导师排行,充值排行,ACT_COST_GOLD
//        if (activityId == ActivityConst.ACT_MENTOR_SCORE || activityId == ActivityConst.ACT_TOPUP_RANK || activityId == ActivityConst.ACT_COST_GOLD) {
//            StaticActAward preReward = staticActivityMgr.getActAward(keyId - 1);
//            if (actAward.getCond() > 1 && preReward != null) {
//                if (rank <= preReward.getCond()) {
//                    handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
//                    return;
//                }
//            }
//        }
	}
}
