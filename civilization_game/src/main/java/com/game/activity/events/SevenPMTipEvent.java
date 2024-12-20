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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * RankType:1--->活动首日晚7点开始按排名计算奖励，活动结束后未领取奖励将以邮件的方式补发
 */
@Component
public class SevenPMTipEvent extends BaseActivityEvent {

	//private static SevenPMTipEvent inst = new SevenPMTipEvent();
	//
	//public static SevenPMTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.SEVEN_PM_REWARD, ActivityConst.ACT_SOILDER_RANK, this::process);
		listenEvent(EventEnum.SEVEN_PM_REWARD, ActivityConst.ACT_FORGE_RANK, this::process);
		listenEvent(EventEnum.SEVEN_PM_REWARD, ActivityConst.ACT_COUNTRY_RANK, this::process);
		listenEvent(EventEnum.SEVEN_PM_REWARD, ActivityConst.ACT_CITY_RANK, this::process);
		listenEvent(EventEnum.SEVEN_PM_REWARD, ActivityConst.ACT_OIL_RANK, this::process);
		listenEvent(EventEnum.SEVEN_PM_REWARD, ActivityConst.ACT_WASH_RANK, this::process);
		listenEvent(EventEnum.SEVEN_PM_REWARD, ActivityConst.ACT_STONE_RANK, this::process);
		listenEvent(EventEnum.SEVEN_PM_REWARD, ActivityConst.ACT_BUILD_RANK, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();

		ActivityData activityData = activityManager.getActivity(activityBase);
		if (activityData == null) {
			return;
		}

		int historyRank = (int) activityData.getStatus(player.getRoleId());
		if (historyRank <= 0) {
			return;
		}

		// 奖励列表
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());

		// 领奖记录
		Map<Integer, Integer> received = actRecord.getReceived();

		// 是否有未领奖
		Optional<StaticActAward> optional = awardList.stream().filter(e -> e.getCond() >= historyRank && !received.containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		// 没有奖励可领取了,左侧活动上红点关闭
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));

	}


	public void soilderRank(EventEnum activityEnum, IActivityActor actor) {

	}

	public void stoneRank(EventEnum activityEnum, IActivityActor actor) {

	}

	public void oilRank(EventEnum activityEnum, IActivityActor actor) {

	}

}
