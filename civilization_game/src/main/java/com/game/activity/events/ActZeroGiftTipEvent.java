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
import com.game.domain.s.StaticActFreeBuy;
import com.game.util.TimeHelper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 0元礼包
 */
@Component
public class ActZeroGiftTipEvent extends BaseActivityEvent {
    //
    //private static ActZeroGiftTipEvent inst = new ActZeroGiftTipEvent();
    //
    //public static ActZeroGiftTipEvent getInst() {
    //    return inst;
    //}

    @Override
    public void listen() {
        listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_ZERO_GIFT, this::process);
    }

    @Override
    public void process(EventEnum activityEnum, IActivityActor actor) {
        ActRecord actRecord = actor.getActRecord();
        ActivityBase activityBase = actor.getActivityBase();
        List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
        List<StaticActFreeBuy> footList = staticActivityMgr.getActFreeBuy(activityBase.getAwardId());
        if (footList == null || footList.isEmpty()) {
            return;
        }

        for (StaticActFreeBuy foot : footList) {
            if (null == foot) {
                continue;
            }
            int sortId = foot.getSortId();
            long buyTime = actRecord.getStatus(sortId);
            if (buyTime == 0L) {
                continue;
            }

            // 购买的第几天
            int state = TimeHelper.equation(buyTime, TimeHelper.curentTime()) + 1;
            Optional<StaticActAward> optional = condList.stream().filter(e -> e.getSortId() == foot.getSortId() && e.getCond() <= state && !actRecord.getReceived().containsKey(e.getKeyId())).findFirst();
            if (optional.isPresent()) {
                actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, true));
                return;
            }
        }
        actor.setResult(new ActivityEventResult(actor.getActivityBase(), SynEnum.ACT_TIP_DISAPEAR, false));
    }
}
