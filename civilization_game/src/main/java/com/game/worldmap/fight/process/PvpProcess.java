package com.game.worldmap.fight.process;

import com.game.constant.ActPassPortTaskType;
import com.game.constant.ActivityConst;
import com.game.constant.BattleEntityType;
import com.game.constant.CountryConst;
import com.game.constant.CountryTaskType;
import com.game.constant.DailyTaskId;
import com.game.constant.LostTargetReason;
import com.game.constant.MailId;
import com.game.constant.MapId;
import com.game.constant.MarchReason;
import com.game.constant.MarchState;
import com.game.constant.WarState;
import com.game.constant.WarType;
import com.game.constant.WorldBoxTask;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.HeroAddExp;
import com.game.domain.p.Mail;
import com.game.domain.p.Team;
import com.game.domain.p.WorldMap;
import com.game.flame.FlameWarService;
import com.game.manager.DailyTaskManager;
import com.game.manager.WallManager;
import com.game.manager.WorldBoxManager;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerFarWar;
import com.game.pb.WorldPb;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IWar;
import com.google.common.collect.HashBasedTable;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "玩家vs玩家", warType = {WarType.Attack_WARFARE, WarType.ATTACK_FAR}, marthes = {MarchType.AttackCityFar, MarchType.CityFriendAssist})
@Component
public class PvpProcess extends FightProcess {

	@Autowired
	private WorldBoxManager worldBoxManager;

	@Autowired
	private DailyTaskManager dailyTaskManager;

	@Autowired
	private WallManager wallManager;

	@Autowired
	private FlameWarService flameWarService;

	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

		// 注册战斗
		registerProcess(WarState.Waiting, this::warWaiting);
		registerProcess(WarState.Fighting, this::battle);
		registerProcess(WarState.Finish, this::warFinish);

		// 行军处理
		registerMarch(MarchType.AttackCityFar, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.AttackCityFar, MarchState.Waiting, this::marchFight);
		registerMarch(MarchType.AttackCityFar, MarchState.Back, this::doFinishedMarch);

		// 城池驻防行军
		registerMarch(MarchType.CityFriendAssist, MarchState.Begin, this::friendAssistCity);
		registerMarch(MarchType.CityFriendAssist, MarchState.CityAssist, this::friendAssistCityFinish);
		registerMarch(MarchType.CityFriendAssist, MarchState.Back, this::doFinishedMarch);
	}

	/**
	 * 等待
	 *
	 * @param mapInfo
	 * @param war
	 */
	public void warWaiting(MapInfo mapInfo, IWar war) {
		long now = System.currentTimeMillis();
		if (now >= war.getEndTime()) {// 等待结束
			WarInfo warInfo = (WarInfo) war;
			warInfo.setEndTime(warInfo.getEndTime() + 1000L);
			warInfo.setState(WarState.Fighting);
			return;
		}

		long defencerId = war.getDefencer().getId();
		Player target = playerManager.getPlayer(defencerId);
		if (target == null) {
			LogHelper.CONFIG_LOGGER.info("target is null!");
			return;
		}

		WarInfo warInfo = (WarInfo) war;

		Pos defendPos = war.getDefencer().getPos();
		Pos pos = target.getPos();

		if (target.getProectedTime() > now) {// 防守方开启保护套

			for (March march : war.getAttacker().getMarchList()) {
				marchManager.doMarchReturn(mapInfo.getMapId(), march, MarchReason.FarProtectedTime);
				Player player = playerManager.getPlayer(march.getLordId());
				if (player != null) {
					battleMailManager.sendProtectedMail(player, target);
				}
			}

			WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(warInfo);
			worldManager.synRemoveWar(target, synCityWarRq);

			worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
			warInfo.setState(WarState.Finish);
			return;
		}

		if (!defendPos.equals(pos)) {// 已经迁城
			for (March march : war.getAttacker().getMarchList()) {//进攻方行军返回
				marchManager.doMarchReturn(mapInfo.getMapId(), march, MarchReason.HighMove);
				Player player = playerManager.getPlayer(march.getLordId());
				if (player != null) {//不战而逃,我方开始返回
					playerManager.sendNormalMail(player, MailId.ESCAPE_WAR, target.getNick());
				}
			}

			for (March march : war.getDefencer().getMarchList()) {//防守方行军返回
				marchManager.doMarchReturn(mapInfo.getMapId(), march, MarchReason.HighMove);
			}

			WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(warInfo);
			worldManager.synRemoveWar(target, synCityWarRq);

			worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
			warInfo.setState(WarState.Finish);
		}
	}

	public void battle(MapInfo mapInfo, IWar war) {
		WarInfo warInfo = (WarInfo) war;
		doPvpWar(mapInfo, war);
		warInfo.setState(WarState.Finish);
		worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
	}

	// 玩家对玩家:远征或者奔袭
	public void doPvpWar(MapInfo mapInfo, IWar war) {
		WarInfo warInfo = (WarInfo) war;
		Team attacker = battleMgr.initAttackerWarTeam(warInfo, true);

		// 防守方: 援助 + 当前玩家的驻防武将 + 其他玩家驻防武将 + 城防军
		Team defencer = battleMgr.initDefencer(warInfo, BattleEntityType.HERO, false);

		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(attacker, defencer, rand, ActPassPortTaskType.IS_WORLD_WAR);
		activityManager.calcuKillAll(warInfo, attacker, defencer);
		long defencerId = warInfo.getDefencerId();
		Player target = playerManager.getPlayer(defencerId);
		if (target == null) {
			LogHelper.CONFIG_LOGGER.info("target is null!");
			return;
		}

		Player player = playerManager.getPlayer(warInfo.getAttackerId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("attacker is null!");
			return;
		}

		HeroAddExp heroAddExp = worldManager.caculateTeamKill(attacker, attacker.getLordId());
		worldManager.caculateTeamDefenceKill(defencer);
		// 处理玩家扣血
		HashMap<Integer, Integer> defenceRec = new HashMap<Integer, Integer>();
		HashMap<Long, HashMap<Integer, Integer>> allDefenceRec = new HashMap<>();
		worldManager.handleDefenceSoldier(defencer, target, MarchReason.FarAttack, allDefenceRec);
		defenceRec = allDefenceRec.get(target.getLord().getLordId());
		// 同步所有参战人员的属性变化, 处理士兵血量和威望
		HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
		handlePvpWarHp(warInfo, attacker, defencer, allSoldierRec, WarType.ATTACK_FAR);
		if (attacker.isWin()) { // 攻击方胜利, 防守方城池被干飞

			// 国家任务
			countryManager.doCountryTask(player, CountryTaskType.PVP_CITY_WIN, 1);
			// 处理掠夺资源

			// 击飞玩家逻辑
			if (mapInfo.getMapId() == MapId.FIRE_MAP) {
				flameWarService.playerFly(target);
			} else {
				worldManager.playerFly(target); // 玩家被击飞
				handleMarchAward(target, warInfo, attacker, defencer, heroAddExp, defenceRec, allSoldierRec);
			}

			// 这里会遣返所有部队[需要排除当前战斗]
			worldManager.callDefecneOtherMarchReturn(target, warInfo.getWarId(), MarchReason.FarPlayerFly);
			// 所有玩家返回行军
			handleWarMarch(warInfo, mapInfo, MarchReason.FarWarWin);
			// 被击飞玩家发送邮件
			battleMailManager.sendCityReport(target, player);
			countryManager.cityWarHeroLoyalty(target, attacker.getCountry());

			for (Long lord : warInfo.getAttackerPlayers()) {
				Player player1 = playerManager.getPlayer(lord);
				if (player1 == null) {
					continue;
				}
				activityManager.updActPerson(player1, ActivityConst.ACT_CITY_RANK, 1, 0);

				int sortId = 14000 + target.getCommandLv();
				activityManager.updActSeven(player1, ActivityConst.TYPE_ADD, sortId, 0, 1);
				activityManager.updActSeven(player1, ActivityConst.TYPE_ADD, 15000, 0, 1);
				worldBoxManager.calcuPoints(WorldBoxTask.CITY_FIGHT, player1, 1);
			}
		} else { // 防守方胜利, 防守方回城，攻击方也回城
			// 直接回城
			handleWarMarch(warInfo, mapInfo, MarchReason.FarWarFailed);
			// 发送失败邮件
			battleMailManager.sendCityFailWarReport(warInfo, attacker, defencer, heroAddExp, defenceRec, allSoldierRec);

		}
		if (mapInfo.getMapId() == MapId.FIRE_MAP) {
			flameWarService.calKill(player, attacker);
			flameWarService.calKill(target, defencer);

		}
		worldManager.soldierAutoAdd(target);
		synAllPlayerChange(warInfo);
		// 通知参战双方删除战斗
		warManager.handePvpWarRemove(warInfo);
		// 处理所有人的城战
		handlePvpRank(warInfo);
		warInfo.getAttackMarches().forEach(e -> {
			Player p = playerManager.getPlayer(e.getLordId());
			if (p != null) {
				p.addAccackPlayerCity();
				dailyTaskManager.record(DailyTaskId.CITY_WAR, p, 1);
			}
		});
	}

	private void warFinish(MapInfo mapInfo, IWar war) {
		WarInfo warInfo = (WarInfo) war;
		warInfo.setEnd(true);
	}

	/**
	 * @param mapInfo
	 * @param march
	 */
	public void marchFight(MapInfo mapInfo, March march) {
		march.setState(MarchState.Fighting);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	// 检查玩家远征或者奔袭是否有开启保护
	public void marchArrive(MapInfo mapInfo, March march) {
		long now = System.currentTimeMillis();
		long targetId = march.getDefencerId();
		long lordId = march.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("quick player is null!");
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.PLAYER_NULL);
			return;
		}

		Player target = playerManager.getPlayer(targetId);
		if (target == null) {
			LogHelper.CONFIG_LOGGER.info("target player is null!");
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.TARGET_NULL);
			return;
		}

		Pos marchEndPos = march.getEndPos();
		if (marchEndPos != null && marchEndPos.getX() != target.getPosX() && marchEndPos.getY() != target.getPosY()) {
			warManager.handleLostTarget(march, mapInfo, LostTargetReason.TARGET_FLY);
			return;
		}

		// 加入到战役
		warManager.addCityAttender(march, now);
		// 全区域广播
		worldManager.synMarch(mapInfo.getMapId(), march);
	}


	/**
	 * 城池驻防
	 *
	 * @param mapInfo
	 * @param march
	 */
	private void friendAssistCity(MapInfo mapInfo, March march) {
		wallManager.handleAssist(march, mapInfo);
	}

	private void friendAssistCityFinish(MapInfo mapInfo, March march) {
		worldLogic.handleAssistReturn(march, MarchReason.MarchCityAssist);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	public void handleMarchAward(Player target, WarInfo warInfo, Team attacker, Team defencer, HeroAddExp heroAddExp,
		HashMap<Integer, Integer> defenceRec, HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		Player player = playerManager.getPlayer(warInfo.getAttackerId());
		//掉落
		List<Award> robeAward = worldManager.getLost(target);
		//可以抢的金额
		List<Award> gotAward = worldManager.getGot(target);

		// 先给战争发起者
		List<Award> callerAward = new ArrayList<Award>();
		// 分一半
		for (Award award : gotAward) {
			callerAward.add(new Award(award.getKeyId(), award.getType(), award.getId(), award.getCount() / 2));
		}

		// 其他人平分
		ConcurrentLinkedDeque<March> marches = warInfo.getAttackMarches();
		int attackers = marches.stream().collect(Collectors.groupingBy(e -> e.getLordId())).keySet().size() - 1;
		List<Award> othersAward = new ArrayList<Award>();
		if (attackers > 0) {
			for (Award award : callerAward) {
				othersAward.add(new Award(award.getKeyId(), award.getType(), award.getId(), award.getCount()
					/ attackers));
			}
		}
		//掠夺超过上限
		boolean isLimit = worldManager.isLimit(player, callerAward);
		Map<Long, List<BattleEntity>> map = attacker.getAllEnities().stream().collect(Collectors.groupingBy(BattleEntity::getLordId));
		for (long lordId : map.keySet()) {
			if (lordId == player.getLord().getLordId()) {
				continue;
			}
			Player p = playerManager.getPlayer(lordId);
			isLimit = worldManager.isLimit(p, othersAward);
			if (isLimit) {
				break;
			}
		}
		//进攻能抢夺的上限
		callerAward = worldManager.getRobBuyWareHouser(player, callerAward);
		if (isLimit) {
			//掉落上限为 抢夺*1.5
			List<Award> calReward = new ArrayList<>();
			callerAward.forEach(e -> {
				calReward.add(e);
			});
			robeAward = worldManager.getLostMax(calReward);
			for (long lordId : map.keySet()) {
				if (lordId == player.getLord().getLordId()) {
					continue;
				}
				Player p = playerManager.getPlayer(lordId);
				if (p == null) {
					continue;
				}
				//copy一份避免出错
				List<Award> otherscalReward = new ArrayList<>();
				othersAward.forEach(e -> {
					Award award = new Award(e);
					otherscalReward.add(award);
				});
				//其他玩家最大抢夺上限
				List<Award> tmpOthersAward = worldManager.getRobBuyWareHouser(p, otherscalReward);
				//其他人的
				List<Award> otherReward = worldManager.getLostMax(tmpOthersAward);
				//加道损失上面去
				for (Award award : otherReward) {
					for (Award tmp : robeAward) {
						if (award.getType() == tmp.getType() && award.getId() == tmp.getId()) {
							tmp.setCount(tmp.getCount() + award.getCount());
						}
					}
				}
			}
			robeAward = worldManager.getLostMax(target, robeAward);
		}

		// 分配掉落的物品
		for (March march : marches) {
			if (march.getLordId() == warInfo.getAttackerId()) {
				for (Award award : callerAward) {
					march.addAwards(award);
				}
			} else {
				for (Award award : othersAward) {
					march.addAwards(award);
				}
			}

			//双旦活动掉落
			List<Award> actDoubleEgg = activityService.actDoubleEggReward(player, false);
			if (actDoubleEgg.size() > 0) {
				Player p = playerManager.getPlayer(march.getLordId());
				Mail mail = playerManager.sendNormalMail(p, MailId.NATIONL_DAY);
				List<CommonPb.Award> awards = new ArrayList<>();
				actDoubleEgg.forEach(e -> {
					awards.add(e.wrapPb().build());
				});
				mail.setAward(awards);
				mail.setAwardGot(2);
				actDoubleEgg.forEach(e -> {
					march.addAwards(e);
				});
			}
		}
		// 被攻击的人扣除相关物品
		worldManager.handPlayerLost(target, robeAward);
		battleMailManager.sendCityWinWarReport(warInfo, attacker, defencer, robeAward, callerAward, othersAward,
			heroAddExp, defenceRec, allSoldierRec);

	}

	// 城战荣誉
	public void handlePvpRank(WarInfo warInfo) {
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		HashSet<Long> players = new HashSet<Long>();
		for (March march : attackerMarches) {
			players.add(march.getLordId());
		}

		for (March march : defencerMarches) {
			players.add(march.getLordId());
		}

		for (Long lordId : players) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
			countryManager.updCountryHoror(player, CountryConst.RANK_CITY);
		}
	}


	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
		if (worldMap.getFarWarData() == null) {
			return;
		}
		SerFarWar serCityWar = null;
		try {
			serCityWar = SerFarWar.parseFrom(worldMap.getFarWarData());
			if (serCityWar == null) {
				return;
			}

			List<DataPb.WarData> warDatas = serCityWar.getWarDataList();
			if (warDatas == null) {
				LogHelper.CONFIG_LOGGER.info("warDatas is null!");
				return;
			}

			for (DataPb.WarData warData : warDatas) {
				if (warData == null) {
					continue;
				}

				DataPb.PosData pos = warData.getDefencerPos();
				if (pos.getX() == 0 && pos.getY() == 0) {
					continue;
				}

				WarInfo warInfo = warManager.createFarWar(warData, mapInfo);
				if (warInfo.getDefencerPos().getX() == 0 || warInfo.getDefencerPos().getY() == 0) {
					continue;
				}
	//			LogHelper.MESSAGE_LOGGER.info("【战斗.远征战】 warType:{} warId:{} endTime:{}", warInfo.getWarType(), warInfo.getWarId(), DateHelper.getDate(warInfo.getEndTime()));
				mapInfo.addWar(warInfo);
				worldManager.flushWar(warInfo, true, warInfo.getAttackerCountry());
			}
		} catch (InvalidProtocolBufferException e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}
}
