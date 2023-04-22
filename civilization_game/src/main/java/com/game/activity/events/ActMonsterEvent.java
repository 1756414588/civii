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
 * 剿灭虫族 因为领奖奖励和击杀虫族均在外部进行了活动状态更新，故此处肢解调用同一个方法
 *
 * @author 陈奎
 */
@Component
public class ActMonsterEvent extends BaseActivityEvent {

	//private static ActMonsterEvent inst = new ActMonsterEvent();
	//
	//public static ActMonsterEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.KILL_MONSTER, ActivityConst.ACT_MONSTER, this::process);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		actRecord.addCount();
		ActivityBase activityBase = actor.getActivityBase();
		int record = actRecord.getCount();

		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == awardList || awardList.size() == 0) {
			return;
		}

		Map<Integer, Integer> received = actRecord.getReceived();
		Optional<StaticActAward> optional = awardList.stream().filter(e -> e.getCond() <= record && !received.containsKey(e.getKeyId())).findAny();
		if (optional.isPresent()) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		// 没有奖励可领取了,左侧活动上红点关闭
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
