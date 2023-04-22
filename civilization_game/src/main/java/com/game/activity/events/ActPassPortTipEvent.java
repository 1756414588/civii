package com.game.activity.events;

import com.game.activity.ActivityEventResult;
import com.game.activity.BaseActivityEvent;
import com.game.activity.define.EventEnum;
import com.game.activity.define.SynEnum;
import com.game.activity.facede.IActivityActor;
import com.game.constant.ActPassPortTaskType;
import com.game.constant.ActivityConst;
import com.game.constant.Quality;
import com.game.constant.ResourceType;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.p.ActPassPortTask;
import com.game.domain.p.ActRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticPassPortAward;
import com.game.domain.s.StaticPassPortTask;
import com.game.spring.SpringUtil;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 通行证
 *
 * @author zcp
 * @date 2021/9/6 16:35
 */
public class ActPassPortTipEvent extends BaseActivityEvent {

	private static ActPassPortTipEvent inst = new ActPassPortTipEvent();

	public static ActPassPortTipEvent getInst() {
		return inst;
	}

	@Override
	public void listen() {
		listenEvent(EventEnum.SUB_GOLD, ActivityConst.ACT_PASS_PORT, this::subGold);
		listenEvent(EventEnum.LEVY_RESOURCE, ActivityConst.ACT_PASS_PORT, this::levyResource);
		listenEvent(EventEnum.TRAINR, ActivityConst.ACT_PASS_PORT, this::hireFreeSoldier);
		listenEvent(EventEnum.MISSSION_DONE, ActivityConst.ACT_PASS_PORT, this::missionDone);
		listenEvent(EventEnum.KILL_MONSTER, ActivityConst.ACT_PASS_PORT, this::killMonster);
		listenEvent(EventEnum.JOURNEY_DONE, ActivityConst.ACT_PASS_PORT, this::journeyDone);
		listenEvent(EventEnum.EQUIP_WASH, ActivityConst.ACT_PASS_PORT, this::equipWash);
		listenEvent(EventEnum.MARKET_BUY, ActivityConst.ACT_PASS_PORT, this::markerBuy);
		listenEvent(EventEnum.HERO_WASH, ActivityConst.ACT_PASS_PORT, this::heroWash);
		listenEvent(EventEnum.WORKS_PRODUCE, ActivityConst.ACT_PASS_PORT, this::makePropInWorkShop);
		listenEvent(EventEnum.COUNTRY_BUILD, ActivityConst.ACT_PASS_PORT, this::countryBuild);
		listenEvent(EventEnum.PAY, ActivityConst.ACT_PASS_PORT, this::payMoney);
		listenEvent(EventEnum.COLLECT, ActivityConst.ACT_PASS_PORT, this::collectResource);
		listenEvent(EventEnum.TECH_UP, ActivityConst.ACT_PASS_PORT, this::techUp);
		listenEvent(EventEnum.BUILD_LEVEL_UP, ActivityConst.ACT_PASS_PORT, this::buildingUp);
		listenEvent(EventEnum.GET_OIL, ActivityConst.ACT_PASS_PORT, this::getOil);
		listenEvent(EventEnum.CITY_WAR_WIN, ActivityConst.ACT_PASS_PORT, this::attackWin);
		listenEvent(EventEnum.COUNTRY_WAR, ActivityConst.ACT_PASS_PORT, this::countryWar);
		listenEvent(EventEnum.BUY_ENERGY, ActivityConst.ACT_PASS_PORT, this::buyEnergy);
		listenEvent(EventEnum.BUY_JOURNEY, ActivityConst.ACT_PASS_PORT, this::buyJourney);
		listenEvent(EventEnum.EQUIP_DONE, ActivityConst.ACT_PASS_PORT, this::equipDone);
		listenEvent(EventEnum.LOSE_SOLDIER, ActivityConst.ACT_PASS_PORT, this::lossSoldier);
		listenEvent(EventEnum.COMPOUND_OMAMENT, ActivityConst.ACT_PASS_PORT, this::compoundOmament);
		listenEvent(EventEnum.GET_CARD_AWARD, ActivityConst.ACT_PASS_PORT, this::getCardAward);
		listenEvent(EventEnum.BREAUTY_SEEKING, ActivityConst.ACT_PASS_PORT, this::breautySeeking);
		listenEvent(EventEnum.RIOT_WAR, ActivityConst.ACT_PASS_PORT, this::roitWar);
		listenEvent(EventEnum.DAILY_ACTIVITY, ActivityConst.ACT_PASS_PORT, this::dailyActive);
		listenEvent(EventEnum.GET_ACTIVITY_AWARD_TIP, ActivityConst.ACT_PASS_PORT, this::process);
	}

	private boolean checkFlag(int taskType, IActivityActor actor) {
		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		ActRecord actRecord = actor.getActRecord();
		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();

		List<StaticPassPortAward> passPortList = staticActivityMgr.getPassPortList(actRecord.getAwardId());
		int beforeScore = actRecord.getRecord(0);
		int isBuy = actRecord.getRecord(1);
		int lv = staticActivityMgr.getPassPortLv(beforeScore);
		Optional<StaticPassPortAward> optional = null;
		if (isBuy == 0) {
			optional = passPortList.stream().filter(e -> e.getType() == 1 && e.getLv() <= lv && !actRecord.getReceived().containsKey(e.getId())).findFirst();
			if (lv >= 60 && !optional.isPresent()) {
				return false;
			}
		} else {
			optional = passPortList.stream().filter(e -> e.getLv() <= lv && !actRecord.getReceived().containsKey(e.getId())).findFirst();
			if (lv >= 60 && !optional.isPresent()) {
				return false;
			}
		}

		boolean flag = false;
		Set<Map.Entry<Integer, ActPassPortTask>> entries = tasks.entrySet();
		for (Map.Entry<Integer, ActPassPortTask> entry : entries) {
			ActPassPortTask value = entry.getValue();
			if (taskType == value.getTaskType()) {
				StaticPassPortTask passPortTask = staticActivityMgr.getPassPortTask(value.getId());
				if (null == passPortTask) {
					continue;
				}
				int totalProcess = value.getProcess() + actor.getChange();
				value.setProcess(totalProcess);
				if (value.getIsAward() == 0 && passPortTask != null && totalProcess >= passPortTask.getCond()) {
					flag = true;
					continue;
				}
			}
		}
		return flag;
	}

	/**
	 * 检测金币消耗
	 *
	 * @param activityEnum
	 * @param actor
	 */
	public void subGold(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.EXPEND_GOLD, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 资源征收
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void levyResource(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.LEVY_RESOURCE, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 兵种训练
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void hireFreeSoldier(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.HIRE_FREE_SOLDIER_TIMES, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 战役通关
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void missionDone(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.DONE_MISSION_OR_SWEEP, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 远征通关
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void journeyDone(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.DONE_JOURNEY_OR_SWEEP, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 装备洗练
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void equipWash(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.WASH_EQUIP, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void roitWar(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.MONSTER_INTRUSION, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}


	private void dailyActive(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.DAILY_ACTIVITY, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 英雄特训
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void heroWash(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.HERO_WASH, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void killMonster(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.DONE_FREE_MONSTER, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 市场购买
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void markerBuy(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.BUY_DEPOT_PROP, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void makePropInWorkShop(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.MAKE_PROP_IN_WORKSHOP, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void countryBuild(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.MAKE_COUNTRY, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void payMoney(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.PAY_FREE_MONEY, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 采集资源事件
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void collectResource(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.COLLECT_FREE_RESOURCE, actor);
		switch (actor.getParam2()) {
			case ResourceType.IRON:
				flag = checkFlag(ActPassPortTaskType.COLLECT_IRON, actor);
				break;
			case ResourceType.COPPER:
				flag = checkFlag(ActPassPortTaskType.COLLECT_COPPER, actor);
				break;
			case ResourceType.OIL:
				flag = checkFlag(ActPassPortTaskType.COLLECT_OIL, actor);
				break;
			default:
				break;
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void techUp(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.UP_TECH, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void buildingUp(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.UP_BUILDING, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void getOil(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.GET_OI, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void attackWin(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.SUCCESSFUL_ATTACKS, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void countryWar(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.ATTEND_COUNTRY_WAR, actor);
		//阵营战胜利
		if (actor.getParam2() == 1) {
			boolean flag2 = checkFlag(ActPassPortTaskType.ATTEND_NPC_CITY, actor);
			flag = flag ? true : flag2;
		}
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void buyEnergy(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.BUY_ENERGY, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void buyJourney(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.DONE_JOURNEY, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 装备打造
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void equipDone(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean greenFlag = false;
		boolean blueFlag = false;
		boolean purpleFlag = false;
		if (actor.getParam2() >= Quality.BLUE.get()) {
			greenFlag = checkFlag(ActPassPortTaskType.MAKE_GREEN_EQUIP, actor);
		}
		if (actor.getParam2() >= Quality.GREEN.get()) {
			blueFlag = checkFlag(ActPassPortTaskType.MAKE_BLUE_EQUIP, actor);
		}
		if (actor.getParam2() >= Quality.GOLD.get()) {
			purpleFlag = checkFlag(ActPassPortTaskType.MAKE_PURPLE_EQUIP, actor);
		}
		if (greenFlag || blueFlag || purpleFlag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void lossSoldier(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.LOSS_SOLDIER, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 合成配饰
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void compoundOmament(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.OMAMENT_COMPOUND, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	/**
	 * 领取月卡/季卡奖励
	 *
	 * @param activityEnum
	 * @param actor
	 */
	private void getCardAward(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.GET_CARD_AWARD, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	private void breautySeeking(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		boolean flag = checkFlag(ActPassPortTaskType.BREAUTY_SEEKING, actor);
		if (flag) {
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
		}
	}

	@Override
	public void process(EventEnum activityEnum, IActivityActor actor) {
		ActivityBase activityBase = actor.getActivityBase();
		ActRecord actRecord = actor.getActRecord();

		StaticActivityMgr staticActivityMgr = SpringUtil.getBean(StaticActivityMgr.class);
		List<StaticPassPortAward> passPortList = staticActivityMgr.getPassPortList(actRecord.getAwardId());

		int beforeScore = actRecord.getRecord(0);
		int isBuy = actRecord.getRecord(1);
		int lv = staticActivityMgr.getPassPortLv(beforeScore);
		Optional<StaticPassPortAward> optional = null;
		if (isBuy == 0) {
			optional = passPortList.stream().filter(e -> e.getType() == 1 && e.getLv() <= lv && !actRecord.getReceived().containsKey(e.getId())).findFirst();
			if (lv >= 60 && !optional.isPresent()) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
				return;
			}
		} else {
			optional = passPortList.stream().filter(e -> e.getLv() <= lv && !actRecord.getReceived().containsKey(e.getId())).findFirst();
			if (lv >= 60 && !optional.isPresent()) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
				return;
			}
		}
		if (optional.isPresent()) {// 奖励类型模块
			actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
			return;
		}

		Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();
		List<StaticPassPortTask> taskList = staticActivityMgr.getPassPortTaskList();
		for (StaticPassPortTask e : taskList) {
			if (!tasks.containsKey(e.getId())) {
				continue;
			}
			ActPassPortTask actPassPortTask = tasks.get(e.getId());
			if (actPassPortTask.getIsAward() != 0) {
				continue;
			}
			if (actPassPortTask.getProcess() >= e.getCond()) {
				actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, true));
				return;
			}
		}
		actor.setResult(new ActivityEventResult(activityBase, SynEnum.ACT_TIP_DISAPEAR, false));
	}
}
