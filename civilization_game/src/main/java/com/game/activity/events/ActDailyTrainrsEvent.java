package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 日常训练 训练任意数量士兵 训练完士兵能领奖则通知客户端红点 领奖红点走通用ActRewardTipEvent事件
 *
 * @author 陈奎
 */
@Component
public class ActDailyTrainrsEvent extends BaseActivityEvent {

	//private static ActDailyTrainrsEvent inst = new ActDailyTrainrsEvent();
	//
	//public static ActDailyTrainrsEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.TRAINR, ActivityConst.DAILY_TRAINRS, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {

		ActRecord actRecord = actor.getActRecord();
		actRecord.addRecord(1, actor.getChange());
		ActivityBase activityBase = actor.getActivityBase();


		int record = actRecord.getRecord(1);

		Map<Integer, Integer> received = actRecord.getReceived();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (condList == null || condList.size() <= 0) {
			return;
		}

		Optional<StaticActAward> optional = condList.stream().filter(e -> e.getCond() <= record && !received.containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
