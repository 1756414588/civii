package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActivityConst;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActSeven;
import com.game.manager.HeroManager;
import com.game.manager.KillEquipManager;
import com.game.manager.MissionManager;
import com.game.manager.WorldManager;
import com.game.spring.SpringUtil;
import com.game.util.DateHelper;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;

/**
 * 七日狂欢领取奖励之后,红点消失还是提示
 */
@Component
public class SevenDayTipEvent extends BaseActivityEvent {

	@Override
	public void listen() {
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_SEVEN, this::process);
	}

	//@Autowired
	//StaticActivityMgr staticActivityMgr;

	@Override
	public void process(EventEnum eventEnum, IActivityActor actor) {
		Player player = actor.getPlayer();
		ActivityBase activityBase = actor.getActivityBase();
		Iterator<StaticActSeven> it = staticActivityMgr.getSevens().values().iterator();
		Date createDate = player.account.getCreateDate();
		int state = DateHelper.dayiy(createDate, new Date());
		if (it == null) {
			return;
		}

		ActRecord actRecord = actor.getActRecord();

		while (it.hasNext()) {
			StaticActSeven actSeven = it.next();
			int keyId = actSeven.getKeyId();
			int cond = status(player, actRecord, actSeven.getSortId());
			if (state <= 7 && state >= actSeven.getDay()) {
				if (cond != 0 && cond >= actSeven.getCond() && !actRecord.getReceived().containsKey(keyId)) {
					actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
					return;
				}
			}
		}

		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}

	/**
	 * 当前完成值
	 *
	 * @param player
	 * @param activity
	 * @param sortId
	 * @return
	 */
	private int status(Player player, ActRecord activity, int sortId) {
		int cond = sortId / 1000;
		if (cond == 5) {// 副本通过
			MissionManager missionManager = SpringUtil.getBean(MissionManager.class);
			return missionManager.pssBossMission(player, sortId % 1000);
		} else if (cond == 6) {// 收集武将
			HeroManager heroManager = SpringUtil.getBean(HeroManager.class);
			return heroManager.getQualityNum(player, sortId % 1000);
		} else if (cond == 7) {// 武将洗练[完成, ok1]
			int quality = sortId % 1000;
			return player.getWashHeroMax(quality);
		} else if (cond == 8) {// 装备收集[完成, ok1]
			int quality = (sortId % 1000) / 100;
			int equipType = sortId % 100;
			return player.getEquipMake(quality, equipType);
			// return equipManager.getQualityNum(player,quality, equipType);

		} else if (cond == 9) {// 装备洗练[完成, ok1]
			int quality = (sortId % 1000) / 100;
			return player.getWashEquipMax(quality);
		} else if (cond == 10) {// 打造杀器
			int level = sortId % 1000;
			KillEquipManager killEquipManager = SpringUtil.getBean(KillEquipManager.class);
			return killEquipManager.getLevelNum(player, level);
		} else if (cond == 11) {// 玩家等级
			return player.getLevel();
		} else if (cond == 12) {// 司令部等级
			return player.getCommandLv();
		} else if (cond == 13) {// 战斗力[完成, ok1]
			return player.getMaxScore();
		} else if (cond == 16) {// 地图最多拥有城池
			WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
			return worldManager.getCityNum(sortId % 1000, player);
		} else {// 取记录值
			return (int) activity.getStatus(sortId);
		}
	}
}
