package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.constant.SimpleId;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.Item;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActExchange;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 奖章兑换
 */
@Component
public class ActMedalExchangeTipEvent extends BaseActivityEvent {

	//private static ActMedalExchangeTipEvent inst = new ActMedalExchangeTipEvent();
	//
	//public static ActMedalExchangeTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_MEDAL_EXCHANGE, this::process);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_DRAGON_BOAT, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActRecord actRecord = actor.getActRecord();
		ActivityBase activityBase = actor.getActivityBase();

		int activityId = activityBase.getActivityId();

		Map<Integer, StaticActExchange> exchangeMap = staticActivityMgr.getActDoubleEggs();
		if (exchangeMap == null || exchangeMap.isEmpty()) {
			return;
		}

		boolean flag = false;
		Iterator<Entry<Integer, StaticActExchange>> it = exchangeMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, StaticActExchange> entry = it.next();
			Integer k = entry.getKey();
			StaticActExchange v = entry.getValue();
			int changeNum = actRecord.getRecord(k);
			if (changeNum >= v.getMaxNum()) {
				continue;
			}
			int propId = getExchangeId(activityId);
			Item item = player.getItem(propId);
			if (item != null && item.getItemNum() >= v.getNeedNum()) {
				flag = true;
				break;
			}
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
		} else {
			actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}


	public int getExchangeId(int activityId) {
		switch (activityId) {
			case ActivityConst.ACT_MEDAL_EXCHANGE:
				return staticLimitMgr.getNum(SimpleId.ACT_MEDAL_PROP);
			case ActivityConst.ACT_DRAGON_BOAT:
				return staticLimitMgr.getNum(SimpleId.ACT_DRAON_BOAT_PROP);
			default:
				return staticLimitMgr.getNum(SimpleId.ACT_DOUBLE_EGG_PROP);
		}
	}
}
