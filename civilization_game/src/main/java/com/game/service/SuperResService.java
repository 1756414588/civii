package com.game.service;

import com.game.manager.MarchManager;
import com.game.worldmap.SuperGuard;
import com.game.worldmap.SuperResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.ActPassPortTaskType;
import com.game.constant.AwardType;
import com.game.constant.BattleEntityType;
import com.game.constant.CityType;
import com.game.constant.DevideFactor;
import com.game.constant.GameError;
import com.game.constant.LostTargetReason;
import com.game.constant.MailId;
import com.game.constant.MapId;
import com.game.constant.MarchReason;
import com.game.constant.MarchState;
import com.game.constant.ResourceType;
import com.game.dataMgr.StaticHeroMgr;
import com.game.dataMgr.StaticSuperResMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.City;
import com.game.domain.p.Hero;
import com.game.domain.p.Report;
import com.game.domain.p.ReportMsg;
import com.game.domain.p.Team;
import com.game.domain.s.StaticFortressLv;
import com.game.domain.s.StaticHero;
import com.game.domain.s.StaticSuperRes;
import com.game.domain.s.StaticWorldCity;
import com.game.manager.BattleMailManager;
import com.game.manager.BattleMgr;
import com.game.manager.CityManager;
import com.game.manager.PlayerManager;
import com.game.manager.TechManager;
import com.game.manager.WarManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.WorldPb;
import com.game.util.TimeHelper;
import com.game.worldmap.Entity;
import com.game.worldmap.EntityType;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.game.worldmap.WorldLogic;

@Component
public class SuperResService {

	@Autowired
	WorldManager worldManager;

	@Autowired
	CityManager cityManager;

	@Autowired
	StaticSuperResMgr staticSuperResMgr;

	@Autowired
	StaticWorldMgr staticWorldMgr;

	@Autowired
	WarManager warManager;

	@Autowired
	PlayerManager playerManager;

	@Autowired
	WorldService worldService;

	@Autowired
	StaticHeroMgr staticHeroMgr;
	@Autowired
	WorldLogic worldLogic;
	@Autowired
	BattleMgr battleMgr;
	@Autowired
	BattleMailManager battleMailMgr;
	@Autowired
	StaticHeroMgr heroMgr;
	@Autowired
	MarchManager marchManager;

	public void timeSuperMine() {
		try {
			MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
			if (mapInfo == null) {
				return;
			}
			long now = System.currentTimeMillis();
			Map<Integer, List<SuperResource>> superResMap = mapInfo.getSuperResMap();
			Iterator<List<SuperResource>> iterator = superResMap.values().iterator();
			while (iterator.hasNext()) {
				List<SuperResource> next = iterator.next();
				Iterator<SuperResource> iterator1 = next.iterator();
				while (iterator1.hasNext()) {
					SuperResource resource = iterator1.next();
					switch (resource.getState()) {
						// 生产中
						case SuperResource.STATE_PRODUCED:
							processProducedState(mapInfo, resource, now);
							break;
						// 停产中
						case SuperResource.STATE_STOP:
							processStopState(mapInfo, resource, now);
							break;
						// 重置中
						case SuperResource.STATE_RESET:
							boolean b = processResetState(mapInfo, resource, now);
							if (!b) {
								iterator1.remove();
							}
							break;
						default:
							break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理生产状态矿点
	 *
	 * @param superResource
	 * @param now
	 */
	private void processProducedState(MapInfo mapInfo, SuperResource superResource, long now) {
		// 计算矿点资源
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(superResource.getResId());
		int remaining = superResource.calcCollectRemaining(staticSuperRes.getSpeed());

		if (remaining <= 0) {
			System.err.println("processProducedState=============" + remaining);
			resetStateSuperMine(mapInfo, superResource, now);
			return;
		}
		boolean flag = false;
		ConcurrentLinkedDeque<SuperGuard> collectArmy = superResource.getCollectArmy();
		Iterator<SuperGuard> iterator = collectArmy.iterator();
		while (iterator.hasNext()) {
			SuperGuard superGuard = iterator.next();
			if (superGuard.getMarch().getEndTime() < now) {
				int i = finishCollect(mapInfo, superGuard, now, superResource);// 结束采集
				March march = superGuard.getMarch();
				Player player = playerManager.getPlayer(march.getLordId());
				Hero hero = player.getHero(march.getHeroIds().get(0));
				battleMailMgr.sendCollectDone(MailId.COLLECT_WIN, superResource, superGuard.calcCollectedTime(now), i, hero.getHeroId(), hero.getHeroLv(), player, false, null);
				flag = true;
				iterator.remove();
			}
		}
		if (flag) {
			superResource.reCalcAllCollectArmyTime(now, staticSuperRes);// 重新计算分布时间
		}
	}

	/**
	 * 停产状态处理
	 *
	 * @param sm
	 * @param now
	 */
	private void processStopState(MapInfo mapInfo, SuperResource sm, long now) {
		if (sm.getNextTime() < now) {
			resetStateSuperMine(mapInfo, sm, now);
		} else {
			// checkHelpArmyReturn(sm, now);
		}
	}

	/**
	 * 重置超级矿点 (1.资源采集完成会重置 2.停产维持一定时间会重置)
	 *
	 * @param sm
	 * @param now
	 */
	public void resetStateSuperMine(MapInfo mapInfo, SuperResource sm, long now) {
		worldManager.clearResourcePos(mapInfo, sm.getPos());// 清除地图资源
		worldManager.synEntityRemove(sm, mapInfo.getMapId(), sm.getPos());// 同步地块信息
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(sm.getResId());
		returnAllArmy(mapInfo, sm, now);// 撤回所有部队,包括驻防部队
		sm.setResetState(now, staticSuperRes);

	}

	/**
	 * 重置状态处理
	 *
	 * @param sm
	 * @param now
	 */
	private boolean processResetState(MapInfo mapInfo, SuperResource sm, long now) {
		if (now > sm.getNextTime()) {
			return refreshSuperMine(mapInfo, sm);
		}
		return true;
	}

	/**
	 * 刷新超级矿点 , 从重置状态 -> 生产状态
	 *
	 * @param sm
	 */
	public boolean refreshSuperMine(MapInfo mapInfo, SuperResource sm) {
		if (sm.getState() != SuperResource.STATE_RESET) {
			return true;
		}
		City capitalCity = cityManager.checkAndGetHome(sm.getCountry());
		if (capitalCity == null) {
			return true;
		}
		StaticFortressLv staticCityDev = staticSuperResMgr.getStaticCityDev(capitalCity.getCityLv());
		List<SuperResource> superResources = mapInfo.getSuperResMap().get(sm.getCountry());
		if (superResources == null) {
			return true;
		}
		if (superResources.size() > staticCityDev.getResourceNum()) {
			return false;
		}
		Map<Pos, Integer> emptyPos = calcSuperMinePos(mapInfo, 1, sm.getCountry(), capitalCity, staticCityDev.getResourceNum(), superResources);
		List<Entity> list = new ArrayList<>();
		for (Map.Entry<Pos, Integer> kv : emptyPos.entrySet()) {
			int cityId = kv.getValue();
			Pos pos = kv.getKey();
			StaticSuperRes sSm = staticSuperResMgr.getSuperMineRandom();
			sm.reset(pos, sSm, cityId); // 修改状态
			worldManager.addSuperResource(mapInfo, pos, sm);// 添加到地图
			list.add(sm);
		}
		worldManager.synEntityAddRq(list);
		return true;
	}

	public Map<Pos, Integer> calcSuperMinePos(MapInfo mapInfo, int needAdd, int country, City capitalCity, int resourceNum, List<SuperResource> myCampSuperMineList) {
		// 自阵营占据的据点
		List<City> myCampCityList = cityManager.getCityMap().values().stream().filter(x -> x.getCityType() == CityType.FAMOUS_CITY && x.getCountry() == country).collect(Collectors.toList());
		myCampCityList.add(capitalCity); // 加上自己的都城
		Collections.shuffle(myCampCityList);
		// 每个据点周围最多的数量
		final int maxCellCnt = (int) Math.ceil(resourceNum * 1.0 / myCampCityList.size());
		Map<Pos, Integer> emptyPosMap = new HashMap<>(); // key:pos, val:cityId
		for (City c : myCampCityList) {
			if (needAdd > 0) {
				// 该据点已有的超级矿点个数
				int hasCnt = (int) myCampSuperMineList.stream().filter(sm -> sm.getCityId() == c.getCityId()).count();

				int cityNeed = maxCellCnt - hasCnt; // 需要的数量

				// 数量已经足够
				if (cityNeed < 1) {
					continue;
				}
				StaticWorldCity sCity = staticWorldMgr.getCity(c.getCityId());
				while (cityNeed > 0 && needAdd > 0) {
					Pos pos = mapInfo.randomCityRangePos(sCity);
					if (pos != null && !emptyPosMap.containsKey(pos)) {
						emptyPosMap.put(pos, c.getCityId());
						cityNeed--;
						needAdd--;
					}
				}
			}
		}
		return emptyPosMap;
	}

	/**
	 * 返回该矿点的所有采集部队和驻防部队
	 */
	private void returnAllArmy(MapInfo mapInfo, SuperResource sm, long now) {
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(sm.getResId());

		sm.getCollectArmy().forEach(x -> {
			int count = finishCollect(mapInfo, x, now, sm);
			Integer integer = x.getMarch().getHeroIds().get(0);
			Player player = playerManager.getPlayer(x.getMarch().getLordId());
			Hero hero = player.getHero(integer);
			if (staticSuperRes != null) {
				playerManager.sendNormalMail(player, MailId.SUPER_CHANGE_STATE, staticSuperRes.getName(), sm.getPosStr());
			}
			battleMailMgr.sendCollectDone(MailId.COLLECT_WIN, sm, x.calcCollectedTime(now), count, x.getMarch().getHeroIds().get(0), hero.getHeroLv(), player, false, null);
		});
		for (March march : sm.getHelpArmy()) {// 驻防部队也返回
			marchManager.handleMarchReturn(march, MarchReason.CollectDone);
			worldManager.synMarch(mapInfo.getMapId(), march);
			Player defence1 = playerManager.getPlayer(march.getLordId());
			if (defence1 != null) {
				StaticHero staticHero = heroMgr.getStaticHero(march.getHeroIds().get(0));
				playerManager.sendNormalMail(defence1, MailId.SUPER_ZF_FAIL, staticSuperRes != null ? staticSuperRes.getName() : "", sm.getPosStr(), staticHero != null ? staticHero.getHeroName() : "");
				// playerManager.sendNormalMail(defence1, MailId.SUPER_ZF_FAIL, staticSuperRes.getName(), resource.getPosStr(), staticHero.getHeroName());
			}
		}
		// 清空部队
		sm.getCollectArmy().clear();
		sm.getHelpArmy().clear();

	}

	@Autowired
	TechManager techManager;

	// 处理部队采集完成的逻辑
	public int finishCollect(MapInfo mapInfo, SuperGuard sg, long now, SuperResource sm) {
		int total = 0;
		try {
			StaticSuperRes sSm = staticSuperResMgr.getStaticSuperRes(sm.getResId());
			long collectedTime = sg.calcCollectedTime(now);
			int gainRes = (int) Math.floor((collectedTime * 1.0 / TimeHelper.HOUR_MS) * sSm.getSpeed()); // 计算采集数量
			total = (int) (gainRes * (1.0 + sg.getMarch().getAddFactor() / DevideFactor.PERCENT_NUM));
			System.err.println("finishCollect============" + (1.0 + sg.getMarch().getAddFactor() / 100));
			System.err.println("lordId =" + sg.getMarch().getLordId() + "===" + "gainRes=" + gainRes + "===total=" + total);
			sm.addConvertRes(gainRes);// 处理已被采集走的资源（加成不计算在内）
			List<Award> list = new ArrayList<>();
			Award award = new Award(AwardType.RESOURCE, sSm.getResType(), total);
			list.add(award);
			sg.getMarch().addAllAwards(list);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			marchManager.handleMarchReturn(sg.getMarch(), MarchReason.CollectDone);
			worldManager.synMarch(mapInfo.getMapId(), sg.getMarch());
		}
		return total;
	}

	// 大型资源点驻军
	public void marchEndHelpSuperMineLogic(MapInfo mapInfo, March march) {
		Pos pos = march.getEndPos();
		Entity entity = mapInfo.getEntity(pos);
		if (entity == null || !(entity instanceof SuperResource)) {
			warManager.handleLostTarget(march, mapInfo, MailId.LOST_TARGET);
			return;
		}
		SuperResource resource = (SuperResource) entity;
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(resource.getResId());
		if (staticSuperRes == null) {
			warManager.handleLostTarget(march, mapInfo, MailId.LOST_TARGET);
			return;
		}
		if (resource.getState() != SuperResource.STATE_PRODUCED) {
			worldManager.handleMiddleReturn(march, MarchReason.LostTarget);
			worldManager.synMarch(mapInfo.getMapId(), march);
			return;
		}
		if (resource.getHelpArmyCount() >= staticSuperRes.getAssitNum()) {
			warManager.handleSuperTarget(march, MailId.SUPER_ASS_MAX, resource);
			worldManager.synMarch(mapInfo.getMapId(), march);
			return;
		}
		Player player = playerManager.getPlayer(march.getLordId());
		if (player == null) {
			return;
		}
		// 这一串操作主要是为了让前端消除世界上面的行军线
		march.setState(MarchState.Done);
		worldManager.synMarch(mapInfo.getMapId(), march);
		// 紧接着发synMarch Remove
		playerManager.synRemoveMarch(player, worldManager.wrapMarchPb(march).build());
		// 从玩家marchPlayer身上删除这个行军
		playerManager.handlerMarch(player);
		// 从世界地图删除这个行军
		worldManager.removeMarch(mapInfo.getMapId(), march);
		List<Integer> heroIds = march.getHeroIds();
		// 生成多个武将驻防
		for (Integer heroId : heroIds) {
			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}
			// 生成行军
			March newMarch = worldManager.createSuperResMarch(player, heroId, march.getEndPos());
			player.addMarch(newMarch);
			worldManager.addMarch(mapInfo.getMapId(), newMarch);
			if (resource.getHelpArmyCount() >= staticSuperRes.getAssitNum()) {
				warManager.handleSuperTarget(newMarch, MailId.SUPER_ASS_MAX, resource);
				worldManager.synMarch(mapInfo.getMapId(), newMarch);
				continue;
			}
			WorldPb.SynMarchRq.Builder builder = WorldPb.SynMarchRq.newBuilder();
			builder.setMarch(worldManager.wrapMarchPb(newMarch));
			resource.getHelpArmy().add(newMarch); // 驻防部队加入进去
			playerManager.synMarchToPlayer(player, builder.build());
		}
	}

	public void superResAttack(March march, MapInfo mapInfo) {
		Player player = playerManager.getPlayer(march.getLordId());
		if (player == null) {
			return;
		}
		Entity entity = mapInfo.getEntity(march.getEndPos());
		if (entity == null || entity.getEntityType() != EntityType.BIG_RESOURCE) {
			// 目标点丢失, 发送邮件
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.QUICK_ENTITY_NULL);
			return;
		}
		SuperResource resource = (SuperResource) entity;
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(resource.getResId());
		if (staticSuperRes == null) {
			return;
		}
		Player defencer = null;// 邮件右侧头部人选
		if (resource.getHelpArmy() != null && !resource.getHelpArmy().isEmpty()) {
			March first = resource.getHelpArmy().getFirst();
			defencer = playerManager.getPlayer(first.getLordId());
		}
		if (defencer == null) {
			if (resource.getCollectArmy() != null && !resource.getCollectArmy().isEmpty()) {
				SuperGuard first = resource.getCollectArmy().getFirst();
				defencer = playerManager.getPlayer(first.getMarch().getLordId());
			}
		}
		if (defencer == null) {
			worldManager.handleMiddleReturn(march, MarchReason.LostTarget);
			worldManager.synMarch(mapInfo.getMapId(), march);
			playerManager.sendReportMail(player, null, null, MailId.SUPER_ATT_SUCCESS, new ArrayList<Award>(), null, staticSuperRes.getName(), resource.getPosStr(), "2");
			return;
		}
		long now = System.currentTimeMillis();
		Team attackerTeam = battleMgr.initPlayerTeam(player, march.getHeroIds(), BattleEntityType.HERO, false, 0, true);
		Team defenceTeam = battleMgr.createSuperResDefer(resource);
		Random rand = new Random(System.currentTimeMillis());
		battleMgr.doTeamBattle(attackerTeam, defenceTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);

		worldManager.caculateTeamKill(attackerTeam, player.roleId);
		worldManager.caculateTeamDefenceKill(defenceTeam);

		// 计算攻击方血量
		HashMap<Integer, Integer> attackRecMap = new HashMap<Integer, Integer>();
		worldManager.caculatePlayer(attackerTeam, player, attackRecMap);
		// 计算防守方血量
		HashMap<Integer, Integer> defenceRecMap = new HashMap<Integer, Integer>();
		worldManager.caculateSuperResDefPlayer(defenceTeam, defenceRecMap);
		String[] param = {staticSuperRes.getName(), resource.getPosStr()};
		Report collectWarReport = battleMailMgr.createCollectWarReport(attackerTeam, defenceTeam, player, defencer);
		ReportMsg reportMsg = battleMailMgr.createReportMsg(attackerTeam, defenceTeam);
		int attMailId = MailId.SUPER_ATT_SUCCESS;
		if (attackerTeam.isWin()) {
			HashSet<Player> set = new HashSet<>();
			ConcurrentLinkedDeque<SuperGuard> collectArmy = resource.getCollectArmy();
			Iterator<SuperGuard> iterator = collectArmy.iterator();
			while (iterator.hasNext()) {
				SuperGuard x = iterator.next();
				Player defence1 = playerManager.getPlayer(x.getMarch().getLordId());
				if (defence1 != null) {
					// 防守采集邮件
					finishCollect(mapInfo, x, now, resource);
					worldLogic.handleCollectWar(MailId.COLLECT_BREAK, x.getMarch(), defence1, player, false, resource, x.calcCollectedTime(now));
					set.add(defence1);
					iterator.remove();
				}
			}
			ConcurrentLinkedDeque<March> helpArmy = resource.getHelpArmy();
			Iterator<March> iterator1 = helpArmy.iterator();
			while (iterator1.hasNext()) {
				March x = iterator1.next();
				Player defence1 = playerManager.getPlayer(x.getLordId());
				if (defence1 != null) {
					set.add(defence1);
					// 此处发防守失败报告
					marchManager.handleMarchReturn(x, MarchReason.CollectDone);
					worldManager.synMarch(mapInfo.getMapId(), x);
					StaticHero staticHero = heroMgr.getStaticHero(x.getHeroIds().get(0));
					playerManager.sendNormalMail(defence1, MailId.SUPER_ZF_FAIL, staticSuperRes.getName(), resource.getPosStr(), staticHero.getHeroName());
					iterator1.remove();
				}
			}
			// 此处发防守失败战斗邮件
			set.forEach(x -> {
				playerManager.sendReportMail(x, collectWarReport, reportMsg, MailId.SUPER_DEF_FAIL, new ArrayList<Award>(), attackRecMap, param);
			});
		} else {
			attMailId = MailId.SUPER_ATT_FAIL;
			ArrayList<BattleEntity> allEnities = defenceTeam.getAllEnities();
			HashSet<Player> set = new HashSet<>();
			for (BattleEntity allEnity : allEnities) {
				Player player1 = playerManager.getPlayer(allEnity.getLordId());
				if (player1 == null) {
					continue;
				}
				set.add(player1);
				if (allEnity.getCurSoldierNum() == 0) {
					// 采集将
					if (allEnity.getEntityType() == BattleEntityType.HERO) {
						ConcurrentLinkedDeque<SuperGuard> collectArmy = resource.getCollectArmy();
						Iterator<SuperGuard> iterator = collectArmy.iterator();
						while (iterator.hasNext()) {
							SuperGuard superGuard = iterator.next();
							if (superGuard.getMarch() != null && superGuard.getMarch().getHeroIds().get(0) == allEnity.getEntityId() && superGuard.getMarch().getLordId() == allEnity.getLordId()) {
								finishCollect(mapInfo, superGuard, now, resource);
								// 采集报告
								worldLogic.handleCollectWar(MailId.COLLECT_BREAK, superGuard.getMarch(), player1, player, false, resource, superGuard.calcCollectedTime(now));
								iterator.remove();
							}
						}
					} else {
						ConcurrentLinkedDeque<March> helpArmy = resource.getHelpArmy();
						Iterator<March> iterator = helpArmy.iterator();
						while (iterator.hasNext()) {
							March next = iterator.next();
							if (next.getLordId() == allEnity.getLordId() && allEnity.getEntityId() == next.getHeroIds().get(0)) {
								marchManager.handleMarchReturn(next, MarchReason.QuickAttackWin);// 进攻部队回城
								worldManager.synMarch(mapInfo.getMapId(), next);
								Player player2 = playerManager.getPlayer(next.getLordId());
								// 此处发防守失败报告
								StaticHero staticHero = heroMgr.getStaticHero(next.getHeroIds().get(0));
								playerManager.sendNormalMail(player2, MailId.SUPER_ZF_FAIL, staticSuperRes.getName(), resource.getPosStr(), staticHero.getHeroName());
								iterator.remove();
							}
						}
					}
				}
			}
			resource.reCalcAllCollectArmyTime(now, staticSuperRes);
			// 此处发防守成功战斗邮件
			set.forEach(x -> {
				playerManager.sendReportMail(x, collectWarReport, reportMsg, MailId.SUPER_DEF_SUCCESS, new ArrayList<Award>(), attackRecMap, param);
			});
		}
		// 进攻者战斗
		playerManager.sendReportMail(player, collectWarReport, reportMsg, attMailId, new ArrayList<Award>(), attackRecMap, param);
		marchManager.handleMarchReturn(march, MarchReason.QuickAttackWin);// 进攻部队回城
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	/**
	 * 驻军时间到，自动撤军
	 *
	 * @param mapInfo
	 * @param march
	 */
	public void retreatHelpArmy(MapInfo mapInfo, March march) {
		Pos pos = march.getEndPos();
		SuperResource superResource = mapInfo.getSuperPosResMap().get(pos);
		StaticSuperRes staticSuperRes = null;
		if (superResource != null) {
			superResource.getHelpArmy().remove(march);
			staticSuperRes = staticSuperResMgr.getStaticSuperRes(superResource.getResId());
		}
		Player player = playerManager.getPlayer(march.getLordId());
		if (player != null) {
			if (staticSuperRes != null) {
				StaticHero staticHero = staticHeroMgr.getStaticHero(march.getHeroIds().get(0));
				playerManager.sendNormalMail(player, MailId.SUPER_ZF_SUCCESS, staticSuperRes.getName(), superResource.getPosStr(), staticHero.getHeroName());
			}
		}
		marchManager.handleMarchReturn(march, MarchReason.CollectDone);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	public void marchEndcollectSuperMineLogic(MapInfo mapInfo, March march) {
		Pos pos = march.getEndPos();
		Entity entity = mapInfo.getEntity(pos);
		if (entity == null || !(entity instanceof SuperResource)) {
			// 矿点丢失
			warManager.handleLostTarget(march, mapInfo, MailId.LOST_TARGET);
			return;
		}
		SuperResource resource = (SuperResource) entity;
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(resource.getResId());
		if (staticSuperRes == null) {
			warManager.handleLostTarget(march, mapInfo, MailId.LOST_TARGET);
			return;
		}
		if (resource.getState() == SuperResource.STATE_RESET || resource.getState() == SuperResource.STATE_STOP) {
			warManager.handleSuperTarget(march, MailId.SUPER_CHANGE_STATE, resource);
			worldManager.synMarch(mapInfo.getMapId(), march);
			return;
		}
		ConcurrentLinkedDeque<SuperGuard> collectArmyList = resource.getCollectArmy();
		if (collectArmyList.size() >= staticSuperRes.getCollectNum()) {
			// 人满了返回部队 ,可能需要发邮件
			warManager.handleSuperTarget(march, MailId.SUPER_COL_MAX, resource);
			worldManager.synMarch(mapInfo.getMapId(), march);
			return;
		}
		int remaining = resource.calcCollectRemaining(staticSuperRes.getSpeed());// 剩余资源
		if (remaining <= 0) { // 没有资源了
			warManager.handleLostTarget(march, mapInfo, MarchReason.CANCEL_SUPERRES_BACK);
			return;
		}
		// 加入采集
		int quality = staticHeroMgr.getQuality(march.getHeroIds().get(0));
		long maxTime = worldManager.getCollectPeriod(quality) * TimeHelper.SECOND_MS;// 当前将最大可采集时间
		resource.joinCollect(march, staticSuperRes, maxTime);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	// 打城成功之后 如果打的是名城 需要改变 该名城矿状态
	public void changeSuperState(int cityId) {
		City city = cityManager.getCity(cityId);
		if (city == null) {
			return;
		}
		long now = System.currentTimeMillis();
		StaticWorldCity staticWorldCity = staticWorldMgr.getCityMap().get(city.getCityId());
		if (staticWorldCity != null && staticWorldCity.getType() == CityType.FAMOUS_CITY) {
			MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
			if (mapInfo == null) {
				return;
			}
			Map<Integer, List<SuperResource>> superResMap = mapInfo.getSuperResMap();
			superResMap.values().forEach(x -> {
				x.forEach(cit -> {
					if (cit.getCityId() == cityId) {
						// 打下城之后 自己家里的矿恢复生产
						if (cit.getCountry() == city.getCountry()) {
							StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(cit.getResId());
							cit.setStopToProducedState(now, staticSuperRes);
							// todo 通知行军状态
							List<Entity> list = new ArrayList<>();
							list.add(cit);
							worldManager.synEntityAddRq(list);
						} else {
							// 打下城之后 让别人家里处于生产的矿停产
							if (cit.getState() == SuperResource.STATE_PRODUCED) {

								StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(cit.getResId());
								if (staticSuperRes != null) {
									returnAllArmy(mapInfo, cit, now);// 撤回所有部队,包括驻防部队
									cit.setState(SuperResource.STATE_STOP);
									cit.setStopState(now, staticSuperRes);
									List<Entity> list = new ArrayList<>();
									list.add(cit);
									worldManager.synEntityAddRq(list);
								}
							}
						}
					}
				});
			});
		}
	}

	/**
	 * 攻打
	 *
	 * @param req
	 * @param handler
	 */
	public void attackResRq(WorldPb.AttkerSuperResRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int mapId = worldManager.getMapId(player);
		if (mapId != MapId.CENTER_MAP_ID) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_ATTACK_PLAYER);
			return;
		}
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}
		CommonPb.Pos pos = req.getPos();
		// 目标点
		Pos targetPos = new Pos(pos.getX(), pos.getY());
		Entity entity = mapInfo.getEntity(targetPos);
		if (entity == null || entity.getEntityType() != EntityType.BIG_RESOURCE) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_ATTACK_PLAYER);
			return;
		}
		SuperResource resource = (SuperResource) entity;
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(resource.getResId());
		if (resource.getState() != SuperResource.STATE_PRODUCED || staticSuperRes == null) {
			handler.sendErrorMsgToPlayer(GameError.MARCH_ERR);
			return;
		}
		int type = req.getType();
		if (type == 1) {
			if (resource.getCountry() == player.getCountry()) {
				handler.sendErrorMsgToPlayer(GameError.MARCH_ERR);
				return;
			}
		} else {
			// 驻防大于10 不让驻防
			if (resource.getCountry() != player.getCountry()) {
				handler.sendErrorMsgToPlayer(GameError.MARCH_ERR);
				return;
			}
			if (resource.getHelpArmyCount() > staticSuperRes.getAssitNum()) {
				handler.sendErrorMsgToPlayer(GameError.MARCH_ERR);
				return;
			}
		}
		// 行军英雄
		List<Integer> heroIds = req.getHeroIdList();
		if (heroIds.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.NO_MARCH_HEROS);
			return;
		}
		Map<Integer, Hero> heroMap = player.getHeros();
		// 检测英雄是否重复
		HashSet<Integer> checkHero = new HashSet<Integer>();
		// 检查英雄是否上阵
		for (Integer heroId : heroIds) {
			if (!worldService.isEmbattle(player, heroId)) {
				handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
				return;
			}
			if (player.isHeroInMarch(heroId)) {
				handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
				return;
			}
			checkHero.add(heroId);
		}
		// 有相同的英雄出征
		if (checkHero.size() != heroIds.size()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
			return;
		}
		// 检查英雄是否可以出征
		for (Integer heroId : heroIds) {
			Hero hero = heroMap.get(heroId);
			if (hero == null) {
				handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
				return;
			}
			if (!playerManager.isHeroFree(player, heroId)) {
				handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
				return;
			}
			// 检查武将带兵量
			if (hero.getCurrentSoliderNum() <= 0) {
				handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
				return;
			}
		}
		// 出兵消耗
		int oilCost = worldManager.getMarchOil(heroIds, player, targetPos);
		if (player.getResource(ResourceType.OIL) < oilCost) {
			handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
			return;
		}
		// 如果采集点上有驻军,则取消自己城墙保护
		if (type == 1) {
			March collectMarch = mapInfo.getMarch(resource.getPos());
			if (collectMarch != null) {
				player.getLord().setProtectedTime(System.currentTimeMillis());
			}
		}

		March march = worldManager.createMarch(player, heroIds, targetPos);
		march.setMarchType(type == 1 ? MarchType.SUPER_ATTACK : MarchType.SUPER_ASSIST);
		// 添加行军到玩家身上
		player.addMarch(march);
		// 加到世界地图中
		worldManager.addMarch(mapId, march);
		WorldPb.AttkerSuperResRs.Builder builder = WorldPb.AttkerSuperResRs.newBuilder();
		builder.setMarch(worldManager.wrapMarchPb(march));
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(WorldPb.AttkerSuperResRs.ext, builder.build());
		worldManager.synMarch(mapInfo.getMapId(), march);
		player.getLord().setProtectedTime(System.currentTimeMillis());
		worldManager.synAllProtected(player.getLord().getMapId(), player);
	}

	public void getAllResInfo(WorldPb.DoResourceInfoRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		CommonPb.Pos pos = rq.getPos();
		// 目标点
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		if (mapInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}
		Pos targetPos = new Pos(pos.getX(), pos.getY());
		Entity entity = mapInfo.getEntity(targetPos);
		if (entity == null || entity.getEntityType() != EntityType.BIG_RESOURCE) {
			return;
		}
		SuperResource resource = (SuperResource) entity;
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(resource.getResId());
		if (staticSuperRes == null) {
			return;
		}
		int type = rq.getType();
		WorldPb.DoResourceInfoRs.Builder builder = WorldPb.DoResourceInfoRs.newBuilder();
		builder.setPos(pos);
		builder.setType(type);
		builder.setResId(resource.getResId());
		builder.setTime(resource.getNextTime());
		builder.setCountry(resource.getCountry());

		if (type == 1) {

			resource.getHelpArmy().forEach(x -> {
				x.getHeroIds().forEach(heroId -> {
					CommonPb.WallFriend.Builder builder1 = CommonPb.WallFriend.newBuilder();
					long lordId = x.getLordId();
					Player player1 = playerManager.getPlayer(lordId);
					if (player1 != null) {
						// builder1.setKeyId(x.getKeyId());
						builder1.setLordLv(player1.getLevel());
						builder1.setName(player1.getNick());
						builder1.setHeroId(heroId);
						builder1.setLordId(lordId);
						builder1.setMarchId(x.getKeyId());
						Hero hero = player1.getHero(heroId);
						if (hero != null) {
							builder1.setHeroSoldier(hero.getCurrentSoliderNum());
						}
					}
					builder.addArmy(builder1);
				});

			});
		} else {
			int i = resource.calcCollectRemaining(staticSuperRes.getSpeed());
			builder.setMaxResCount(staticSuperRes.getResourceNum());
			builder.setResCount(i);
			builder.setSpeed(staticSuperRes.getSpeed());
			ConcurrentLinkedDeque<SuperGuard> collectArmy = resource.getCollectArmy();
			collectArmy.forEach(x -> {
				CommonPb.SuperResInfo.Builder builder1 = CommonPb.SuperResInfo.newBuilder();
				long lordId = x.getMarch().getLordId();
				Player player1 = playerManager.getPlayer(lordId);
				if (player1 != null) {
					int heroId = x.getMarch().getHeroIds().get(0);
					builder1.setHeroId(heroId);
					builder1.setLv(player1.getLevel());
					builder1.setNick(player1.getNick());
					Hero hero = player1.getHero(heroId);
					if (hero != null) {
						builder1.setSoildNum(hero.getCurrentSoliderNum());
						builder1.setMaxSoildNum(hero.getSoldierNum());
						builder1.setHeroLv(hero.getHeroLv());
					}
				}
				builder.addInfo(builder1);
			});
		}
		handler.sendMsgToPlayer(WorldPb.DoResourceInfoRs.ext, builder.build());
	}
}
