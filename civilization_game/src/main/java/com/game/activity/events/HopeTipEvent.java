package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActHope;
import com.game.pb.ActivityPb;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import java.util.Map;


/**
 * 许愿池许愿之后，红点关闭还是提示
 */
public class HopeTipEvent extends BaseActivityEvent {

	private static HopeTipEvent inst = new HopeTipEvent();

	public static HopeTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_HOPE, this::process);
	}

	@Override
	public void process(EventEnum eventEnum, IActivityActor actor) {
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		Map<Integer, StaticActHope> staticActHopeMap = staticActivityMgr.getStaticActHopeMap();
		if (null == staticActHopeMap || staticActHopeMap.size() == 0) {
			return;
		}

		ActRecord actRecord = actor.getActRecord();
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();

		int levelTop = 0;
		for (StaticActHope e : staticActHopeMap.values()) {
			if (e != null && !actRecord.getReceived().containsKey(e.getLevel()) && e.getCost() <= player.getGold()) {// 未领取奖励
				if (player.getGold() >= e.getCost()) {//
					actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				}
				return;
			}
			levelTop = levelTop < e.getLevel() ? e.getLevel() : levelTop;
		}

		if (levelTop != 0 && actRecord.getReceived().containsKey(levelTop)) {// 最高奖励已许愿,则关闭活动
			ActivityPb.SynActivityDisappearRq.Builder builder = ActivityPb.SynActivityDisappearRq.newBuilder();
			builder.addParam(activityBase.getActivityId());
			SynHelper.synMsgToPlayer(player, ActivityPb.SynActivityDisappearRq.EXT_FIELD_NUMBER, ActivityPb.SynActivityDisappearRq.ext, builder.build());
		} else {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
		}
	}
}
