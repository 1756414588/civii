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
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 勇冠三军活动事件
 *
 * @author zcp
 * @date 2021/9/6 9:39
 */
@Component
public class ActWeelCrownThreeTipEvent extends BaseActivityEvent {

	//private static ActWeelCrownThreeTipEvent inst = new ActWeelCrownThreeTipEvent();
	//
	//public static ActWeelCrownThreeTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		this.listenEvent(EventEnum.SUB_GOLD, ActivityConst.ACT_WELL_CROWN_THREE_ARMY, this::process);
		this.listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_WELL_CROWN_THREE_ARMY, this::reward);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		if (player.getLevel() < 48) {
			return;
		}
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord activity = actor.getActRecord();
		ActivityData activityData = actor.getActivityData();
		int selfGold = activity.getRecord(0);
		selfGold += actor.getChange();
		activity.putRecord(0, selfGold);

		int countryGold = activityData.getRecord(player.getCountry());
		countryGold += actor.getChange();
		activityData.putRecord(player.getCountry(), countryGold);
		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		boolean flag = false;
		for (StaticActAward award : actAwardList) {
			List<Integer> param = StringUtil.stringToList(award.getParam());
			if (param.size() < 2) {
				continue;
			}
			// 个人达标,且阵营达标
			if (selfGold >= param.get(0) && countryGold >= param.get(1)) {
				//没有领取记录
				if (!activity.getReceived().containsKey(award.getKeyId())) {
					flag = true;
					break;
				}
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	public void reward(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();

		ActivityData activityData = activityManager.getActivity(activityBase.getActivityId());
		if (activityData == null) {
			return;
		}

		int selfGold = actRecord.getRecord(0);
		int countryGold = activityData.getRecord(player.getCountry());

		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		boolean flag = false;
		for (StaticActAward award : actAwardList) {
			List<Integer> param = StringUtil.stringToList(award.getParam());
			if (param.size() < 2) {
				continue;
			}
			// 个人达标,且阵营达标
			if (selfGold >= param.get(0) && countryGold >= param.get(1)) {
				//没有领取记录
				if (!actRecord.getReceived().containsKey(award.getKeyId())) {
					flag = true;
					break;
				}
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		} else {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}
}
