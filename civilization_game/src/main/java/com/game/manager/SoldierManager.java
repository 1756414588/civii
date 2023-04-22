package com.game.manager;

import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticSoldierMgr;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.Player;
import com.game.domain.p.BuildQue;
import com.game.domain.p.Building;
import com.game.domain.p.BuildingBase;
import com.game.domain.p.Camp;
import com.game.domain.p.Hero;
import com.game.domain.p.Soldier;
import com.game.domain.p.WorkQue;
import com.game.domain.s.StaticCapacityTimes;
import com.game.domain.s.StaticLimit;
import com.game.domain.s.StaticSoldierLv;
import com.game.domain.s.StaticVip;
import com.game.log.consumer.EventManager;
import com.game.pb.RolePb;
import com.game.pb.RolePb.SynChangeRq;
import com.game.util.LogHelper;
import com.game.util.SynHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class SoldierManager {

	@Autowired
	private StaticSoldierMgr staticSoldierMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private TechManager techManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private HeroManager heroManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private BroodWarManager broodWarManager;
	@Autowired
	private CountryManager countryManager;

	public int calculateCapacity(Player player, Soldier soldier) {
		Building building = player.buildings;
		Camp camp = building.getCamp();
		Map<Integer, BuildingBase> campMap = camp.getCamp();
		int buildingId = getCampId(soldier.getSoldierIndex());
		BuildingBase buildingBase = campMap.get(buildingId);
		if (buildingBase == null) {
			LogHelper.CONFIG_LOGGER.info("calculateCapacity buildingBase is null");
			return 0;
		}

		int buildingLv = buildingBase.getLevel();
		StaticSoldierLv staticSoldierLv = staticSoldierMgr.getSoldierLv(buildingLv);
		if (staticSoldierLv == null) {
			LogHelper.CONFIG_LOGGER.info("calculateCapacity staticSoldierLv is null");
			return 0;
		}
		int lvCapacity = staticSoldierLv.getCapacity();

		Map<Integer, StaticCapacityTimes> staticCapacityTimesMap = staticSoldierMgr.getCapacityTimesMap();
		int largerCapacity = 0;
		// 0~largerTimes-1, 这个地方其实可以优化在启动的时候计算好
		int largerTimes = soldier.getLargerTimes();
		for (int i = 0; i < largerTimes; i++) {
			StaticCapacityTimes staticCapacityTimes = staticCapacityTimesMap.get(i + 1);
			if (staticCapacityTimes == null) {
				LogHelper.CONFIG_LOGGER.info("calculateCapacity staticCapacityTimes is null");
				continue;
			}

			largerCapacity += staticCapacityTimes.getCapacity();
		}
		return lvCapacity + largerCapacity;
	}

	// 获取指定兵类型的容量
	public int getTotalCapacity(Player player, int soldierType) {
		Soldier soldier = player.getSoldier(soldierType);
		int mainCap = calculateCapacity(player, soldier);
		Soldier minitia = player.getSoldier(SoldierIndex.MILITIA);
		int minitiaCap = 0;
		if (minitia != null && minitia.getSoldierType() == soldierType) {
			minitiaCap += calculateCapacity(player, minitia);
		}
		int total = mainCap + minitiaCap;
		return total;
	}


	public int getCampId(int soldierIndex) {
		if (soldierIndex == SoldierType.ROCKET_TYPE) {
			return BuildingId.ROCKET_CAMP;
		} else if (soldierIndex == SoldierType.TANK_TYPE) {
			return BuildingId.TANK_CAMP;
		} else if (soldierIndex == SoldierType.WAR_CAR) {
			return BuildingId.WAR_CAR_CAMP;
		} else if (soldierIndex == SoldierType.MILITIA) {
			return BuildingId.MILITIA_CAMP;
		}

		return -1;
	}

	public int getSoldierType(int buildingId) {
		if (buildingId == BuildingId.ROCKET_CAMP) {
			return SoldierType.ROCKET_TYPE;
		} else if (buildingId == BuildingId.TANK_CAMP) {
			return SoldierType.TANK_TYPE;
		} else if (buildingId == BuildingId.WAR_CAR_CAMP) {
			return SoldierType.WAR_CAR;
		} else if (buildingId == BuildingId.MILITIA_CAMP) {
			return SoldierType.MILITIA;
		}

		return 0;
	}

	// 兵力放在前面三个兵营上, 民兵营不存放兵力
	public int getSoldierNum(Player player, int soldierType) {
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		Soldier soldier = soldierMap.get(soldierType);
		if (soldier == null) {
			return 0;
		}

		return soldier.getNum();
	}

	public void subSoldierNum(Player player, int soldierType, int num, int reason) {
		Map<Integer, Soldier> soldierMap = player.getSoldiers();
		Soldier soldier = soldierMap.get(soldierType);
		if (soldier == null) {
			return;
		}

		if (num <= 0) {
			//LogHelper.CONFIG_LOGGER.info("subSoldierNum num <= 0,  reason=" + reason);
			return;
		}

		if (soldier.getNum() < num) {
			LogHelper.CONFIG_LOGGER.info("soldier.getNum() < num,  reason=" + reason);
			return;
		}
		removeSoldier(soldier, num, Reason.AutoAdd);
		// LogHelper.logSubItem(player, AwardType.SOLDIER, soldierType, num, Reason.AutoAdd);
		eventManager.soldierChange(player, Lists.newArrayList(
			SoldierName.getName(soldierType),
			-num,
			Reason.ReasonName.getName(reason)
		));
	}

	/**
	 * 募兵加速：基础+科技+活动+vip礼包+母巢
	 *
	 * @param player
	 * @param soldierType
	 * @return
	 */
	public double getSoldierSpeed(Player player, int soldierType) {
		StaticLimit staticLimit = staticLimitMgr.getStaticLimit();
		int baseSoldierSpeed = staticLimit.getSoldierSpeed();
		double techFactor = techManager.getSoldierSpeed(player, soldierType);
		techFactor = Math.max(0.0, techFactor);
		double activityFactor = activityManager.actDouble(ActivityConst.ACT_RECRUIT_SOILDER);
		activityFactor = Math.max(0.0, activityFactor);
		double seasonFactor = worldManager.getSoldierEffect();
		StaticVip staticVip = staticVipMgr.getStaticVip(player.getVip());
		int speedCollect = staticVip.getSpeedCollect();
		// 仅仅判断有没有buff
		double vipBuff = (double) playerManager.getSoldierBuff(player);
		if (vipBuff > 0.0) {
			vipBuff = (double) speedCollect / 100.0;
		}
		vipBuff = Math.max(0.0, vipBuff);
		double broodWar = broodWarManager.getSoldierSpeed(player, soldierType);
		double cityBuf = worldManager.getCityBuf(player, CityBuffType.TRAINING);//名城buff
		double countryFactor = countryManager.getSoldierSpeed(player.getCountry(), soldierType);
		//TODO 暂时处理小数问题 跟前端同步
		return 1.0 + techFactor + activityFactor + seasonFactor + vipBuff + broodWar + cityBuf + countryFactor;
	}

	// 处理自动补兵
	public void autoAdd(Player player, List<Integer> heroIds) {
		int techAutoSoldier = techManager.getAutoSoldier(player);
		if (techAutoSoldier != 1) {
			return;
		}

		if (player.getSoldierAuto() != 1) {
			return;
		}

		for (Integer heroId : heroIds) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}
			autoHeroAdd(player, hero);
			RolePb.SynChangeRq.Builder builder = RolePb.SynChangeRq.newBuilder();
			builder.addHeroChange(hero.createHeroChange());
			SynHelper.synMsgToPlayer(player, SynChangeRq.EXT_FIELD_NUMBER, SynChangeRq.ext, builder.build());
		}
	}

	// 给英雄补兵
	public void autoHeroAdd(Player player, Hero hero) {
		// 检查当前英雄的剩余兵力
		int leftSoldier = hero.getSoldierNum() - hero.getCurrentSoliderNum();
		leftSoldier = Math.max(0, leftSoldier);
		if (leftSoldier <= 0) {
			return;
		}
		// 检查对应兵营的兵力
		int soldierType = heroManager.getSoldierType(hero.getHeroId());
		int soldierNum = getSoldierNum(player, soldierType);
		// 检查可以补的兵力值
		int canAdd = Math.min(leftSoldier, soldierNum);
		if (canAdd > 0) {
			// 给将领补兵
			hero.addSoldierNum(canAdd);
			// 给兵营扣兵力
			subSoldierNum(player, soldierType, canAdd, Reason.AutoAdd);
		}
	}

	public void addSoldier(Player player, Soldier soldier, int count, int reason) {
		if (count <= 0) {
			return;
		}

		soldier.setNum(soldier.getNum() + count);
		//LogHelper.CONFIG_LOGGER.info("add solider = " + count + ", reason = " + reason);
		eventManager.soldierChange(player, Lists.newArrayList(
			SoldierName.getName(soldier.getSoldierType()),
			count,
			Reason.ReasonName.getName(reason)
		));
	}

	public void removeSoldier(Soldier soldier, int count, int reason) {
		if (count <= 0) {
			return;
		}

		int res = soldier.getNum() - count;
		res = Math.max(0, res);
		soldier.setNum(res);
		//LogHelper.CONFIG_LOGGER.info("remove solider = " + count + ", reason = " + reason);
	}


	// 检测有当前兵营正在升级
	public boolean isCampBuilding(int soldierIndex, Player player) {
		// 通过soldierType获得buildingId
		int buildingId = getCampId(soldierIndex);
		if (buildingId == 0) {
			return false;
		}

		Building building = player.buildings;
		if (building == null) {
			return false;
		}

		ConcurrentLinkedDeque<BuildQue> buildQues = building.getBuildQues();
		for (BuildQue buildQue : buildQues) {
			if (buildQue.getBuildingId() == buildingId) {
				return true;
			}
		}
		return false;
	}

	public boolean isSoldierTypeOk(int soldierType) {
		// 士兵类型错误
		if (soldierType < SoldierType.ROCKET_TYPE || soldierType > SoldierType.MILITIA) {
			return false;
		}
		return true;
	}

	// 后面的que时间往前面挪
	public void handleMoveTime(LinkedList<WorkQue> workQues) {
		for (int index = 1; index < workQues.size(); index++) {
			WorkQue preElem = workQues.get(index - 1);
			WorkQue curElem = workQues.get(index);
			if (curElem == null) {
				continue;
			}
			curElem.setEndTime(preElem.getEndTime() + curElem.getPeriod());
		}
	}

	public void checkWorkQues(LinkedList<WorkQue> workQues, int reason) {
		long now = System.currentTimeMillis();
		if (!workQues.isEmpty()) {
			WorkQue first = workQues.getFirst();
			if (first.getEndTime() > now + first.getPeriod()) {
				LogHelper.CONFIG_LOGGER.info("workque may be wrong, in reason = " + reason);
				first.setEndTime(now + first.getPeriod());
			}
			handleMoveTime(workQues);
		}
	}
}
