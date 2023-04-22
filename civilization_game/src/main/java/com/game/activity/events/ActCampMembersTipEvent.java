package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.CampMembersRank;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.manager.ActivityManager;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 骨干阵营排行
 */
public class ActCampMembersTipEvent extends BaseActivityEvent {

	private static ActCampMembersTipEvent inst = new ActCampMembersTipEvent();

	public static ActCampMembersTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_CAMP_MEMBERS, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);

		Date rewardTime = TimeHelper.getRewardTime(activityBase.getEndTime());
		Date now = new Date();
		if (now.before(rewardTime)) {
			return;
		}

		ActivityData activityData = activityManager.getActivity(activityBase);
		if (activityData == null) {
			return;
		}

//		ActPlayerRank persionRank = activityData.getLordRank(player.getRoleId());
//		if (persionRank == null) {
//			return;
//		}

		CampMembersRank campMembersRank = activityData.getCampMembersRank(player);
		if (campMembersRank == null) {
			return;
		}
		int rank = campMembersRank.getRank();
		if (rank <= 0) {
			return;
		}

		List<StaticActAward> list = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		Optional<StaticActAward> optional = list.stream().filter(e -> e.getCond() >= rank && !actRecord.getReceived().containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		// 没有奖励可领取了,左侧活动上红点关闭
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
