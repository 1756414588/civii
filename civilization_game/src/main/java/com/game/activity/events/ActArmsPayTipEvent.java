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

/**
 * 军备促销
 */
@Component
public class ActArmsPayTipEvent extends BaseActivityEvent {
	//
	//private static ActArmsPayTipEvent inst = new ActArmsPayTipEvent();
	//
	//public static ActArmsPayTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_ARMS_PAY, this::process);
		listenEvent(EventEnum.BUY_PAY_ARMS, ActivityConst.ACT_ARMS_PAY, this::doBuyArmy);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();

		ActivityData activityData = activityManager.getActivity(activityBase);
		int country = player.getLord().getCountry();//玩家阵营
		long score = activityData.getAddtion(country);//阵营分数
		int total = (int) (score / 100);//总宝箱数量
		if (total <= 0) {// 没有宝箱可领取
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		for (StaticActAward e : condList) {
			Integer received = actRecord.getReceived().get(e.getKeyId());
			if (received == null || total - received > 0) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}


	public void doBuyArmy(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();
		Player player = actor.getPlayer();


		ActivityData activityData = activityManager.getActivity(activityBase);
		int country = player.getLord().getCountry();//玩家阵营
		long score = activityData.getAddtion(country);//阵营分数
		int total = (int) (score / 100);//总宝箱数量
		if (total <= 0) {// 没有宝箱可领取
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		for (StaticActAward e : condList) {
			Integer received = actRecord.getReceived().get(e.getKeyId());
			if (received == null || total - received > 0) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}
	}
}
