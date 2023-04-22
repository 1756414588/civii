package com.game.activity.events;

import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.manager.ActivityManager;
import com.game.manager.PlayerManager;
import com.game.pb.ActivityPb;
import com.game.pb.ActivityPb.SynActivityDisappearRq;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 到点活动结束
 */
public class ActCloseByTimeEvent extends BaseActivityEvent {

	private static ActCloseByTimeEvent inst = new ActCloseByTimeEvent();

	public static ActCloseByTimeEvent getInst() {
		return inst;
	}

	private List<Function<Player, Integer>> actPlayerTimerList = new ArrayList<>();
	Map<Integer, Function<Integer, Integer>> actServerTimer = new HashMap<>();

	private Map<Integer, Long> closeMap = new HashMap<>();

	@Override
	public void listen() {
		listenEvent(EventEnum.TIME_DISAPPEAR, this::process);
		this.init();
	}

	private void init() {
		// 惊喜礼包跟个人挂钩
		actPlayerTimerList.add(this::actSuripriseGift);

		// 全服统一关闭活动
		actServerTimer.put(ActivityConst.ACT_HERO_DIAL, this::serverActDisapper);
		actServerTimer.put(ActivityConst.ACT_HOPE, this::serverActDisapper);
		actServerTimer.put(ActivityConst.ACT_SEARCH, this::serverActDisapper);
		actServerTimer.put(ActivityConst.ACT_RAIDERS, this::serverActDisapper);
		actServerTimer.put(ActivityConst.LUCK_DIAL, this::serverActDisapper);
		actServerTimer.put(ActivityConst.ACT_WELL_CROWN_THREE_ARMY, this::serverActDisapper);
		actServerTimer.put(ActivityConst.ACT_LUCKLY_EGG, this::serverActDisapper);
		actServerTimer.put(ActivityConst.ACT_DOUBLE_EGG_GIFT, this::serverActDisapper);
		actServerTimer.put(ActivityConst.ACT_DRAGON_BOAT, this::serverActDisapper);
		actServerTimer.put(ActivityConst.ACT_SPRING_FESTIVAL_GIFT, this::serverActDisapper);
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		List<Integer> paramList = new ArrayList<>();

		// 全服统一关闭活动，到点结束
		actServerTimer.forEach((e, f) -> {
			int activityId = f.apply(e);
			if (activityId != 0) {
				paramList.add(activityId);
			}
		});

		playerManager.getOnlinePlayer().forEach(player -> {
			actPlayerTimerList.forEach(e -> {
				int activityId = e.apply(player);
				if (activityId != 0) {
					paramList.add(activityId);
				}
			});

			if (!paramList.isEmpty()) {
				SynActivityDisappearRq.Builder builder = SynActivityDisappearRq.newBuilder();
				builder.addAllParam(paramList);
				SynHelper.synMsgToPlayer(player, ActivityPb.SynActivityDisappearRq.EXT_FIELD_NUMBER, ActivityPb.SynActivityDisappearRq.ext, builder.build());
			}
		});
	}


	/**
	 * 全服类型活动
	 *
	 * @param activityId
	 * @return
	 */
	private int serverActDisapper(int activityId) {
		long now = System.currentTimeMillis();
		if (closeMap.containsKey(activityId)) {
			long closeTime = closeMap.get(activityId);
			if (now > closeTime) {
				closeMap.remove(activityId);
				return activityId;
			}
			return 0;
		}
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		ActivityBase activityBase = activityManager.getActivityBase(activityId);
		if (activityBase == null) {
			return 0;
		}
		if (activityBase.getStep() == ActivityConst.ACTIVITY_BEGIN) {
			if (!closeMap.containsKey(activityId)) {
				closeMap.put(activityId, activityBase.getEndTime().getTime());
			}
		}
		return 0;
	}

	/**
	 * 惊喜礼包
	 */
	public int actSuripriseGift(Player player) {
		ActivityManager activityManager = SpringUtil.getBean(ActivityManager.class);
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_SURIPRISE_GIFT);
		if (activityBase == null) {
			return 0;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			return 0;
		}
		boolean flag = actRecord.hasNoExprie();
		if (!flag) {
			return 0;
		}
		actRecord.checkExprie();
		if (!actRecord.hasNoExprie()) {
			return ActivityConst.ACT_SURIPRISE_GIFT;

		}
		return 0;
	}


}
