package com.game.activity.actor;

import com.game.activity.BaseActivityActor;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;

/**
 * 常规红点提示
 * GetActivityAward主要使用
 * 其他模块均可使用（如有不同之处，请勿添加修改变量，新建actor）
 *
 *
 */
public class CommonTipActor extends BaseActivityActor {

    public CommonTipActor(Player player, ActRecord actRecord, ActivityBase activityBase) {
        super(player, actRecord, activityBase);
    }


}
