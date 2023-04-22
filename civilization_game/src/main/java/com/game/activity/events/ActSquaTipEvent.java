package com.game.activity.events;

import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.constant.NineCellConst;
import com.game.domain.p.ActRecord;
import org.springframework.stereotype.Component;

//import com.game.dataMgr.StaticNineCellMgr;
//import com.game.domain.s.StaticNineCell;

/**
 * @author zcp
 * @date 2021/9/8 16:05
 */
@Component
public class ActSquaTipEvent extends BaseActivityEvent {
	//private static ActSquaTipEvent inst = new ActSquaTipEvent();
	//
	//public static ActSquaTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		this.listenEvent(EventEnum.EQUIP_WASH, ActivityConst.ACT_SQUA, this::equipWash);
		this.listenEvent(EventEnum.WORKS_PRODUCE, ActivityConst.ACT_SQUA, this::worksProduce);
	}

	public void worksProduce(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		long state = actRecord.getStatus(NineCellConst.CELL_9);
		state = state + actor.getChange();
		actRecord.putState(NineCellConst.CELL_9, state);
		this.process(activityEnum, actor);
	}

	public void equipWash(EventEnum activityEnum, IActivityActor actor) {
		ActRecord actRecord = actor.getActRecord();
		long state = actRecord.getStatus(NineCellConst.CELL_3);
		state = state + actor.getChange();
		actRecord.putState(NineCellConst.CELL_3, state);
		this.process(activityEnum, actor);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
//		ActRecord actRecord = actor.getActRecord();
//		StaticNineCellMgr staticNineCellMgr = SpringUtil.getBean(StaticNineCellMgr.class);
//		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
//		List<StaticActAward> actAwardById = staticActivityMgr.getActAwardById(actRecord.getAwardId());
//		Optional optional = actAwardById.stream().filter(e -> {
//			List<Integer> paramList = e.getParamList();
//			for (Integer x : paramList) {
//				StaticNineCell staticNineCell = staticNineCellMgr.getStaticNineCell(actRecord.getAwardId(), x);
//				if (staticNineCell == null || actRecord.getStatus(x) >= staticNineCell.getCond()) {
//					return true;
//				}
//			}
//			return false;
//		}).findFirst();
//		if (optional.isPresent()) {
//			ActivityEventResult result = new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true);
//			result.setId(actor.getActivityBase().getActivityId());
//			actor.setResult(result);
//		}
	}
}
