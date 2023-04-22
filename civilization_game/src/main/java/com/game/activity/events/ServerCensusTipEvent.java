package com.game.activity.events;

import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.constant.SimpleId;
import com.game.domain.ActivityData;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActAward;
import com.game.pb.ActivityPb;
import com.game.util.PbHelper;
import com.game.util.SynHelper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 常规全服统计类活动,有变化则全服推送
 */
@Component
public class ServerCensusTipEvent extends BaseActivityEvent {

	//private static ServerCensusTipEvent inst = new ServerCensusTipEvent();
	//
	//public static ServerCensusTipEvent getInst() {
	//	return inst;
	//}

	@Override
	public void listen() {
		listenEvent(EventEnum.ACT_SERVER, ActivityConst.ACT_HIGH_VIP, this::process);
		listenEvent(EventEnum.ACT_SERVER, ActivityConst.ACT_SER_PAY, this::process);
		listenEvent(EventEnum.ACT_SERVER, ActivityConst.ACT_TOPUP_SERVER, this::process);
	}



	@Override
	public void process(EventEnum eventEnum, IActivityActor activityActor) {
		int activityId = activityActor.getActivityId();
		ActivityData activityData = activityActor.getActivityData();
		ActivityBase activityBase = activityActor.getActivityBase();

		//StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (awardList == null || awardList.isEmpty()) {
			return;
		}

//		LogHelper.MESSAGE_LOGGER.info("ServerCensusTipEvent");

		// 全服活动已达成的领取奖励
		List<StaticActAward> finishList = awardList.stream().filter(e -> activityData.getStatus(e.getSortId()) >= e.getCond()).collect(Collectors.toList());

		//PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		//ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);

		// 领奖设置等级条件
		int limitLevel = 0;
		if (activityId == ActivityConst.ACT_HIGH_VIP) {
			limitLevel = staticLimitMgr.getNum(SimpleId.ACT_HIGHT_VIP);
		}

		List<Player> list = new ArrayList<>();
		if (limitLevel == 0) {
			Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
			while (it.hasNext()) {
				Player player = it.next();
				ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
				for (StaticActAward e : finishList) {
					if (!actRecord.getReceived().containsKey(e.getKeyId())) {
						list.add(player);
						break;
					}
				}
			}
		} else {
			Iterator<Player> it = playerManager.getOnlinePlayer().iterator();
			while (it.hasNext()) {
				Player player = it.next();
				if (player.getLevel() >= limitLevel) {
					ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
					for (StaticActAward e : finishList) {
						if (!actRecord.getReceived().containsKey(e.getKeyId())) {
							list.add(player);
							break;
						}
					}
				}
			}
		}

		// 通知该玩家活动奖励能领取
		if (!list.isEmpty()) {
			ActivityPb.SynActivityDisappearRq.Builder builder = ActivityPb.SynActivityDisappearRq.newBuilder();
			builder.addActivity(PbHelper.createActivityPb(activityBase, activityBase.getEndTime(), true, true));
			list.forEach(e -> {
				SynHelper.synMsgToPlayer(e, ActivityPb.SynActivityDisappearRq.EXT_FIELD_NUMBER, ActivityPb.SynActivityDisappearRq.ext, builder.build());
			});
		}
	}
}
