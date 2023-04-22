package com.game.activity;

import com.game.activity.facede.IActivityEventRet;
import com.game.domain.Player;
import com.game.manager.PlayerManager;
import com.game.pb.ActivityPb;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.Activity;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActTipManager {

	private static ActTipManager inst = new ActTipManager();

	// 标识
	private volatile long identity;
	// 推送状态0未推送 1推送中
	private int pushState = 0;

	// playerId,eventId,IActivityEventRet
	private Map<Long, Map<Long, IActivityEventRet>> oneTipMap = new ConcurrentHashMap<>();
	private Map<Long, Map<Long, IActivityEventRet>> twoTipMap = new ConcurrentHashMap<>();


	public static ActTipManager getInst() {
		return inst;
	}

	/**
	 * 获取
	 *
	 * @return
	 */
	public Map<Long, Map<Long, IActivityEventRet>> getAddTipMap() {
		if (identity % 2 == 0) {
			return oneTipMap;
		} else {
			return twoTipMap;
		}
	}

	public Map<Long, Map<Long, IActivityEventRet>> getPushTipMap() {
		if (identity % 2 == 1) {
			return oneTipMap;
		} else {
			return twoTipMap;
		}
	}

	/**
	 * 添加消息推送
	 *
	 * @param playerId
	 * @param eventRet
	 */
	public boolean addActivityEventRet(Long playerId, IActivityEventRet eventRet) {
		Map<Long, Map<Long, IActivityEventRet>> tipMap = getAddTipMap();
		Map<Long, IActivityEventRet> eventRetMap = null;
		if (tipMap.containsKey(playerId)) {
			eventRetMap = tipMap.get(playerId);
		} else {
			eventRetMap = new ConcurrentHashMap<>();
			tipMap.put(playerId, eventRetMap);
		}
		eventRetMap.put(eventRet.getId(), eventRet);
		return true;
	}

	/**
	 * 推送消息
	 */
	public void pushTip() {
		if (pushState != 0) {
			return;
		}
		// 更改标识
		identity++;
		pushState = 1;

		Map<Long, Map<Long, IActivityEventRet>> tipMap = getPushTipMap();
		try {
			if (tipMap.isEmpty()) {
				return;
			}

			// 推送
			PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
			tipMap.forEach((playerId, tips) -> {
				Player player = playerManager.getPlayer(playerId);
				if (player == null) {
					return;
				}
				ActivityPb.SynActivityDisappearRq.Builder builder = ActivityPb.SynActivityDisappearRq.newBuilder();
				tips.forEach((e, f) -> {
					Object object = f.onResult(player);
					if (object instanceof CommonPb.Activity) {
						CommonPb.Activity activity = (Activity) object;
						builder.addActivity(activity);
					} else if (object instanceof Integer) {
						int r = (int) object;
						if (r != 0) {
							builder.addParam(r);
						}
					}
				});
				SynHelper.synMsgToPlayer(player, ActivityPb.SynActivityDisappearRq.EXT_FIELD_NUMBER, ActivityPb.SynActivityDisappearRq.ext, builder.build());
			});
		} finally {
			tipMap.clear();
			pushState = 0;
		}
	}


}
