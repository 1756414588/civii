package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import org.springframework.stereotype.Component;

/**
 * 金币转盘
 *
 * @author cpz
 */
@Component
public class ActTurntableTipEvent extends BaseActivityEvent {
    //
    //private static ActTurntableTipEvent inst = new ActTurntableTipEvent();
    //
    //public static ActTurntableTipEvent getInst() {
    //    return inst;
    //}

    @Override
    public void listen() {
        listenEvent(EventEnum.SUB_GOLD, ActivityConst.ACT_CRYSTAL_DIAL, this::subGold);
        listenEvent(EventEnum.SUB_GOLD, ActivityConst.ACT_GOLD_DIAL, this::subGold);
        listenEvent(EventEnum.SUB_GOLD, ActivityConst.ACT_HONOR_DIAL, this::subGold);
        listenEvent(EventEnum.SUB_GOLD, ActivityConst.ACT_PURPLE_DIAL, this::subGold);
    }

    public void subGold(EventEnum activityEnum, IActivityActor actor) {

        ActivityBase activityBase = actor.getActivityBase();
        ActRecord activity = actor.getActRecord();

        int state = activity.getRecord(0);
        state = state + actor.getChange();
        activity.putRecord(0, state);

        int onePrice = activityBase.getActivityId() == ActivityConst.ACT_CRYSTAL_DIAL ? staticLimitMgr.getNum(230) : staticLimitMgr.getNum(246);
        int dialCount = activity.getRecord(1);
        int freeCount = state / onePrice - dialCount;
        freeCount = freeCount < 0 ? 0 : freeCount;
        if (freeCount > 0) {
            actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
        }
    }

    @Override
    public void process(EventEnum activityEnum, IActivityActor actor) {

    }
}
