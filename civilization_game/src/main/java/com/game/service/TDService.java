package com.game.service;

import com.game.constant.AwardType;
import com.game.constant.BuildingType;
import com.game.constant.EndlessTDItemEffectType;
import com.game.constant.GameError;
import com.game.constant.ItemType;
import com.game.constant.LordPropertyType;
import com.game.constant.Reason;
import com.game.constant.TaskType;
import com.game.constant.TdDropLimitId;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticTDMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticBulletWarLevel;
import com.game.domain.s.StaticEndlessArmory;
import com.game.domain.s.StaticEndlessBaseinfo;
import com.game.domain.s.StaticEndlessItem;
import com.game.domain.s.StaticEndlessLevel;
import com.game.domain.s.StaticEndlessTDDropLimit;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticTowerWarBonus;
import com.game.domain.s.StaticTowerWarLevel;
import com.game.domain.s.StaticTowerWarMonster;
import com.game.domain.s.StaticTowerWarWave;
import com.game.job.ServerStatisticsJob;
import com.game.log.LogUser;
import com.game.log.consumer.EventManager;
import com.game.log.domain.EndlessTDErrorLog;
import com.game.log.domain.EndlessTDLog;
import com.game.log.domain.TDLog;
import com.game.manager.PlayerManager;
import com.game.manager.TDManager;
import com.game.manager.TDTaskManager;
import com.game.manager.TaskManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.EndlessTDShopGoods;
import com.game.pb.CommonPb.Prop;
import com.game.pb.CommonPb.TDRankInfo;
import com.game.pb.TDPb;
import com.game.pb.TDPb.BattleShopRs;
import com.game.pb.TDPb.BuyBattleShopRq;
import com.game.pb.TDPb.BuyBattleShopRs;
import com.game.pb.TDPb.BuyConvertShopRq;
import com.game.pb.TDPb.BuyConvertShopRs;
import com.game.pb.TDPb.ConvertShopRs;
import com.game.pb.TDPb.EndlessTDOverRq;
import com.game.pb.TDPb.EndlessTDOverRs;
import com.game.pb.TDPb.EndlessTDRankRs;
import com.game.pb.TDPb.EndlessTDReportRq;
import com.game.pb.TDPb.EndlessTDReportRs;
import com.game.pb.TDPb.EndlessTowerDefenseInitRs;
import com.game.pb.TDPb.FightAutoRs;
import com.game.pb.TDPb.PlayEndlessTDRq;
import com.game.pb.TDPb.PlayEndlessTDRs;
import com.game.pb.TDPb.QuartermasterWarehouseRs;
import com.game.pb.TDPb.ReceiveRankAwardRs;
import com.game.pb.TDPb.RefreshBattleShopRs;
import com.game.pb.TDPb.SelectEndlessTDProRq;
import com.game.pb.TDPb.SelectEndlessTDProRs;
import com.game.pb.TDPb.UseEndlessTDProRq;
import com.game.pb.TDPb.UseEndlessTDProRs;
import com.game.server.GameServer;
import com.game.util.RandomUtil;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zcp
 * @date 2020/8/19 14:58
 * @description
 */
@Service
public class TDService {

	@Autowired
	private StaticTDMgr staticTDMgr;
	@Autowired
	private TDManager tdManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private StaticPropMgr staticPropMgr;

	public void bounsInit(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		List<Map.Entry<Integer, Integer>> list = player.getTdBouns().entrySet().stream().sorted(Comparator.comparingInt(e -> e.getKey())).collect(Collectors.toList());
		TDPb.TDBounsRs.Builder builder = TDPb.TDBounsRs.newBuilder();
		tdManager.getBounds(player).forEach(e -> {
			builder.addPowers(e);
		});
		player.getTdBouns().forEach((e, f) -> {
			StaticTowerWarBonus bonus = staticTDMgr.getTowerWarBonusMap(f);
			builder.addBounds(CommonPb.ThreeInt.newBuilder().setV1(e).setV2(f).setV3(bonus.getEffect()).build());
		});
		handler.sendMsgToPlayer(TDPb.TDBounsRs.ext, builder.build());
	}

	/**
	 * 地图初始化
	 *
	 * @param req
	 */
	public void mapInit(TDPb.TDMapInitRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		// 第一次进入
//        if (player.getTdMap().size() == 0) {
		// 初始化
		initPlayerTdMap(player);
//        }
		// 第一次初始化检查下战力
//        manager.openBouns(player);
		TDPb.TDMapInitRs.Builder builder = TDPb.TDMapInitRs.newBuilder();
		staticTDMgr.getTowerWarMapMap().forEach((e, f) -> {
			f.getLevel_list().forEach(levelId -> {
				if (levelId != 999) {
					StaticTowerWarLevel towerWarLevel = staticTDMgr.getTowerWarLevel(levelId);
					builder.addMap(mapInfo(towerWarLevel, player.getTdMap().get(levelId), f.getDifficulty()));
				}
			});
		});
		handler.sendMsgToPlayer(TDPb.TDMapInitRs.ext, builder.build());
	}

	/**
	 * 关卡详情
	 *
	 * @param req
	 */
	public void towerDetail(TDPb.TowerWarRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		StaticTowerWarLevel towerWarLevel = staticTDMgr.getTowerWarLevel(req.getTowerId());
		if (req.getIsGuide()) {
			towerWarLevel = staticTDMgr.getTowerWarLevel(999);
		}

		if (towerWarLevel == null) {
			handler.sendErrorMsgToPlayer(GameError.TOWER_NON_EXISTENT);
			return;
		}
		boolean isEnter = true;
		if (towerWarLevel.getCondition().get(0) < 0) { // -1未开启
			isEnter = false;
		} else {
			if (towerWarLevel.getCondition().get(0) == 1 && towerWarLevel.getCondition().get(1) > player.getBuildingLv(BuildingType.COMMAND)) {
				isEnter = false;
			}
		}
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		TDLog log = TDLog.builder().roleId(player.getLord().getLordId()).tdId(towerWarLevel.getId()).type(0).isEnter(isEnter ? 0 : 1).build();
		logUser.recordTDLog(log);// data日志耗时
		if (!isEnter) { // -1未开启
			handler.sendErrorMsgToPlayer(GameError.TOWER_NOT_OPEN);
			return;
		}

		TD td = player.getTdMap().get(towerWarLevel.getId());
		// if (td.getState() != OpenState.OPEN.val) {
		// handler.sendErrorMsgToPlayer(GameError.TOWER_NOT_OPEN);
		// return;
		// }

		TDPb.TowerWarRs.Builder builder = TDPb.TowerWarRs.newBuilder();
		builder.setTowerId(towerWarLevel.getId());
		builder.setLifePoint(towerWarLevel.getLife_point());
		/** 路线信息 */
		towerWarLevel.getWay_list().forEach((e, f) -> {
			CommonPb.WayList.Builder wayBuilder = CommonPb.WayList.newBuilder();
			wayBuilder.setIndex(e);
			f.forEach(pos -> {
				wayBuilder.addPos(pos);
			});
			builder.addWayList(wayBuilder);
		});
		/** 关卡中防御塔基座的位点坐标的集合 */
		towerWarLevel.getTower_base_list().forEach(baseList -> {
			builder.addBaseList(CommonPb.TwoDouble.newBuilder().setXPos(baseList.get(0)).setYPos(baseList.get(1)).build());
		});
		/** 关卡中防御塔基座的位点坐标的集合 */
		towerWarLevel.getWay_point_list().forEach(pointList -> {
			builder.addWayPointList(CommonPb.TwoDouble.newBuilder().setXPos(pointList.get(0)).setYPos(pointList.get(1)).build());
		});
		/** 摄像机移动范围 */
		towerWarLevel.getCamera_limit().forEach(e -> {
			builder.addCameraLimit(e);
		});

		List<Map.Entry<Integer, List<List<Integer>>>> sortWaveList = towerWarLevel.getWave_list().entrySet().stream().sorted(Comparator.comparingInt(e -> e.getKey())).collect(Collectors.toList());
		Map<Integer, StaticTowerWarMonster> monsterMap = new HashMap<>();
		sortWaveList.forEach(e -> {
			List<List<Integer>> waveList = e.getValue();
			waveList.forEach(waveArr -> {
				int waveId = waveArr.get(0);
				// 波次信息
				StaticTowerWarWave wave = staticTDMgr.getTowerWarWave(waveId);
				wave.getMonster_list().forEach(monsterPair -> {
					Double monsterId = monsterPair.get(1);
					StaticTowerWarMonster monster = staticTDMgr.getTowerWarMonster(monsterId.intValue());
					monsterMap.put(monster.getId(), monster);
				});
			});
		});
		monsterMap.values().forEach(monster -> {
			builder.addMonster(CommonPb.Monster.newBuilder().setId(monster.getId()).setName(monster.getName()).setModel(monster.getModel()).setHp(monster.getHp()).setDefence(monster.getDefence()).setSpeed(monster.getSpeed()).setDamage(monster.getDamage()).setAddSupplies(monster.getAward()).setScale(monster.getScale()).build());
		});
		// 波次信息
		sortWaveList.forEach((e) -> {
			// 触发时间
			int tigerTime = e.getKey();

			CommonPb.WaveList.Builder waveBuilder = CommonPb.WaveList.newBuilder();
			waveBuilder.setTriggerTime(tigerTime);
			List<List<Integer>> waveList = e.getValue();
			waveList.forEach(waveArr -> {
				int waveId = waveArr.get(0);
				// 线路
				int route = waveArr.get(1);
				CommonPb.WaveInfo.Builder waveInfo = CommonPb.WaveInfo.newBuilder();
				waveInfo.setRoute(route);
				// 波次信息
				StaticTowerWarWave wave = staticTDMgr.getTowerWarWave(waveId);
				wave.getMonster_list().forEach(monsterPair -> {
					// 触发时间
					Double monsterTime = monsterPair.get(0);

					// 对应敌人id
					Double monsterId = monsterPair.get(1);
					waveInfo.addMonster(CommonPb.IntDouble.newBuilder().setV1(monsterId.intValue()).setV2(monsterTime).build());
				});
				waveBuilder.addWaveInfo(waveInfo);
			});
			builder.addWaveList(waveBuilder);
		});
		towerWarLevel.getTower_list().forEach(towerList -> {
			int type = towerList.get(0);
			int level = towerList.get(1);
			builder.addTowerMax(CommonPb.TwoInt.newBuilder().setV1(type).setV2(level));
		});
		builder.setCondition(CommonPb.TwoInt.newBuilder().setV1(towerWarLevel.getCondition().get(0)).setV2(towerWarLevel.getCondition().get(1)).build());
		builder.setBaseSupplies(towerWarLevel.getBase_supplies());
		builder.addAllStartLimit(towerWarLevel.getStart_limit());
		player.getTdBouns().forEach((e, f) -> {
			StaticTowerWarBonus bonus = staticTDMgr.getTowerWarBonusMap(f);
			builder.addBounds(CommonPb.ThreeInt.newBuilder().setV1(e).setV2(f).setV3(bonus.getEffect()).build());
		});

		handler.sendMsgToPlayer(TDPb.TowerWarRs.ext, builder.build());
		TDTaskManager.staticRefreshTask(player, new ActTDSevenType(Lists.newArrayList(ActTDSevenType.tdTaskType_1), 1));
	}

	/**
	 * 战斗结果上报
	 *
	 * @param report
	 */
	public void towerWarReport(TDPb.TowerWarReportRq report, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		StaticTowerWarLevel towerWarLevel = staticTDMgr.getTowerWarLevel(report.getLevelId());
		if (towerWarLevel == null) {
			handler.sendErrorMsgToPlayer(GameError.TOWER_NON_EXISTENT);
			return;
		}

		List<Integer> condition = towerWarLevel.getCondition();
		if (condition != null && condition.size() == 2) {
			if (condition.get(0) == 1) {
				Command command = player.getBuildings().getCommand();
				if (command.getLv() < condition.get(1)) {
					handler.sendErrorMsgToPlayer(GameError.TOWER_NON_EXISTENT);
					return;
				}
			}
		}
		TD td = player.getTdMap().get(towerWarLevel.getId());
		if (report.getIsWin()) {
			if (report.getStarNum() > td.getStar()) {
				td.setStar(report.getStarNum());
			}

			// 根据星级把奖励开启来
			td.openStarReward(report.getStarNum());
			if (td.getState() == OpenState.OPEN.val) {
				td.setState(OpenState.NO_RECEIVED.val);
			}
			TD nextTD = player.getTdMap().get(towerWarLevel.getId() + 1);
			if (nextTD != null && nextTD.getState() == OpenState.CLOSE.val) {
				nextTD.setState(OpenState.OPEN.val);
			}
			// 塔防活动记录
			ArrayList<Integer> list = null;
			if (td.getLevelId() < 100) {
				list = Lists.newArrayList(ActTDSevenType.tdTaskType_2, ActTDSevenType.tdTaskType_3, ActTDSevenType.tdTaskType_7);
			} else {
				list = Lists.newArrayList(ActTDSevenType.tdTaskType_4, ActTDSevenType.tdTaskType_5, ActTDSevenType.tdTaskType_7);
			}
			TDTaskManager.staticRefreshTask(player, new ActTDSevenType(list, td.getLevelId(), td.getStar(), 1));
		}
		eventManager.tdDone(player, Lists.newArrayList(td.getLevelId(), towerWarLevel.getName(), report.getIsWin()));
		// 记录日志信息
		LogUser logUser = SpringUtil.getBean(LogUser.class);
		TDLog log = TDLog.builder().roleId(player.getLord().getLordId()).tdId(towerWarLevel.getId()).type(1).isEnter(1).state(report.getIsWin() ? 0 : 1).lessHp(report.getLessHp()).build();
		logUser.recordTDLog(log);

		TDPb.TowerWarReportRs.Builder builder = TDPb.TowerWarReportRs.newBuilder();
		builder.setCode(GameError.OK.getCode());
		handler.sendMsgToPlayer(TDPb.TowerWarReportRs.ext, builder.build());
		SpringUtil.getBean(TaskManager.class).doTask(TaskType.COMPLETE_TOWER_DEFENSE, player, Lists.newArrayList(td.getLevelId()));
	}

	/**
	 * 领取星级奖励
	 *
	 * @param req
	 */
	public void towerReward(TDPb.TowerRewardRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		StaticTowerWarLevel towerWarLevel = staticTDMgr.getTowerWarLevel(req.getLevelId());
		if (towerWarLevel == null) {
			handler.sendErrorMsgToPlayer(GameError.TOWER_NON_EXISTENT);
			return;
		}

		TD td = player.getTdMap().get(towerWarLevel.getId());

		TDPb.TowerRewardRs.Builder builder = TDPb.TowerRewardRs.newBuilder();
		for (int i = 1; i <= 3; i++) {
			int star = i;
			if (td.getStarRewardStatus(star) != TD.HAVE_REWARD) {
				// handler.sendErrorMsgToPlayer(GameError.TOWER_NON_REWARD);
				continue;
			}
			List<List<Integer>> awardList = getAward_List(star, towerWarLevel);
			awardList.forEach(award -> {
				int type = award.get(0);
				int id = award.get(1);
				int count = award.get(2);
				playerManager.addAward(player, type, id, count, Reason.TD_STAR_REWARD);
				builder.addAward(CommonPb.Award.newBuilder().setId(id).setType(type).setCount(count));
			});
			// 设置星级奖励已领取
			td.rewardStarReward(star);
		}
		if (td.getState() == OpenState.NO_RECEIVED.val) {
			td.setState(OpenState.PASSED.val);
		}
		handler.sendMsgToPlayer(TDPb.TowerRewardRs.ext, builder.build());
	}

	private List<List<Integer>> getAward_List(int star, StaticTowerWarLevel towerWarLevel) {
		switch (star) {
		case 1:
			return towerWarLevel.getAward_list_1();
		case 2:
			return towerWarLevel.getAward_list_2();
		case 3:
			return towerWarLevel.getAward_list_3();
		}
		return new ArrayList<>();
	}

	/**
	 * 塔防地图初始化
	 *
	 * @param req
	 * @param handler
	 */
	public void towerInit(TDPb.TDTowerInitRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		TDPb.TDTowerInitRs.Builder builder = TDPb.TDTowerInitRs.newBuilder();
		staticTDMgr.getTowerWarTowerMap().forEach((e, f) -> {
			builder.addTowers(CommonPb.TowerInfo.newBuilder().setId(f.getId()).setName(f.getName()).setIcon(f.getIcon()).setModel(f.getModel()).setType(f.getType()).setLevel(f.getLevel()).setDamage(f.getDamage()).setAttackRate(f.getAttack_rate()).setAttackRange(f.getAttack_range()).setDamageArea(f.getDamage_area()).setBuildCost(f.getBuild_cost()).setUpgradeTarget(f.getUpgrade_target()).setRecoverPrice(f.getRecover_price()).setBulletModel(f.getBullet_model()).setBulletSpeed(f.getBullet_speed()).setDesc(f.getDesc()).build());
		});
		handler.sendMsgToPlayer(TDPb.TDTowerInitRs.ext, builder.build());
	}

	enum OpenState {
		CLOSE(0), // 未开启
		PASSED(1), // 已通关(奖励已领完)
		OPEN(2), // 已开启
		NO_RECEIVED(3) // 奖励待领取
		;

		OpenState(int val) {
			this.val = val;
		}

		int val;
	}

	public void initPlayerTdMap(Player player) {
		int tdMapSize = player.getTdMap().size();
		boolean isFirst = tdMapSize == 0;
		if (isFirst || tdMapSize != staticTDMgr.getTowerWarLevelMap().size()) {
			staticTDMgr.getTowerWarMapMap().forEach((e, f) -> {
				f.getLevel_list().forEach(levelId -> {
					if (player.getTdMap() == null || player.getTdMap().get(levelId) == null) {
						StaticTowerWarLevel level = staticTDMgr.getTowerWarLevel(levelId);
						TD td = new TD(levelId, OpenState.CLOSE.val, 0, level.getStart_limit());
						player.getTdMap().put(levelId, td);
					}
				});
			});
			// 把第一个关卡开启
			if (isFirst) {
				player.getTdMap().get(1).setState(OpenState.OPEN.val);
				player.getTdMap().get(101).setState(OpenState.OPEN.val);
			}
		}
	}

	private CommonPb.MapInfo mapInfo(StaticTowerWarLevel towerWarLevel, TD td, int difficulty) {
		CommonPb.MapInfo.Builder builder = CommonPb.MapInfo.newBuilder();
		builder.setLevelId(td.getLevelId());
		builder.setState(td.getState());
		builder.setStarNum(td.getStar());
		builder.setLevelType(difficulty);
		builder.addAward(awardInfo(td, 1, towerWarLevel.getAward_list_1()));
		builder.addAward(awardInfo(td, 2, towerWarLevel.getAward_list_2()));
		builder.addAward(awardInfo(td, 3, towerWarLevel.getAward_list_3()));
		return builder.build();
	}

	private CommonPb.AwardInfo awardInfo(TD td, int star, List<List<Integer>> awardList) {
		CommonPb.AwardInfo.Builder builder = CommonPb.AwardInfo.newBuilder();
		builder.setStarLevel(star);
		builder.setAwardState(td.getStarRewardStatus(star));
		awardList.forEach(e -> {
			CommonPb.Award.Builder award = CommonPb.Award.newBuilder();
			award.setType(e.get(0));
			award.setId(e.get(1));
			award.setCount(e.get(2));
			builder.addItem(award);
		});
		return builder.build();
	}

	public void openTd(Player player, Integer id) {
		TD td = player.getTdMap().get(id);
		td.setStar(1);
		// 根据星级把奖励开启来
		td.openStarReward(1);
		td.setState(OpenState.NO_RECEIVED.val);
		TD nextTD = player.getTdMap().get(id + 1);
		if (nextTD != null && nextTD.getState() == OpenState.CLOSE.val) {
			nextTD.setState(OpenState.OPEN.val);
		}
	}

	// 无尽塔防初始化协议
	public void endlessTowerDefenseInitRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		EndlessTowerDefenseInitRs.Builder builder = EndlessTowerDefenseInitRs.newBuilder();
		if (!tdManager.checkEndlessTDOpen(player)) {
			handler.sendMsgToPlayer(EndlessTowerDefenseInitRs.ext, builder.build());
			return;
		}
		playerManager.refEndlessTD(player);
		tdManager.getEndlessTDInfo(player).wrapPb(builder);
		builder.setTdMoney(player.getTdMoney());
		tdManager.getEndlessTDInfo(player).getEndlessTDBonus().forEach((e, f) -> {
			StaticTowerWarBonus bonus = staticTDMgr.getEndlessTowerWarBonusMap(f);
			builder.addBuff(CommonPb.ThreeInt.newBuilder().setV1(e).setV2(f).setV3(bonus.getEffect()).build());
		});
		tdManager.getBounds(player).forEach(e -> {
			builder.addPowers(e);
		});
		// 返给前端本周前三名的玩家
		TDRankInfo.Builder rankInfoBuilder = TDRankInfo.newBuilder();
		rankInfoBuilder.setRankdate("0");
		for (EndlessTDRank weekEndlessTDRank : tdManager.getWeekEndlessTDRanks()) {
			rankInfoBuilder.addTdRank(weekEndlessTDRank.wrapTDRank());
			if (rankInfoBuilder.getTdRankBuilderList().size() >= 3) {
				break;
			}
		}
		builder.setTdRankInfo(rankInfoBuilder);
		builder.setDiscount(tdManager.getDiscountGoods(player));
		handler.sendMsgToPlayer(EndlessTowerDefenseInitRs.ext, builder.build());
	}

	// 无尽塔防排行榜数据
	public void endlessTDRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		EndlessTDRankRs.Builder builder = EndlessTDRankRs.newBuilder();
		builder.setSelfRank(tdManager.getPlayerEndlessTDRank(player).wrapTDRank());
		TDRankInfo.Builder rankInfoBuilder = TDRankInfo.newBuilder();
		rankInfoBuilder.setRankdate("0");
		int limit = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.RANK_COUNT).getLimit();
		List<EndlessTDRank> weekEndlessTDRanks = tdManager.getWeekEndlessTDRanks();
		limit = weekEndlessTDRanks.size() > limit ? limit : weekEndlessTDRanks.size();
		weekEndlessTDRanks.subList(0, limit).forEach(e -> {
			rankInfoBuilder.addTdRank(e.wrapTDRank());
		});
		builder.addTdRankInfo(rankInfoBuilder);
		builder.addAllTdRankInfo(tdManager.getHistoryRankBuilders());
		handler.sendMsgToPlayer(EndlessTDRankRs.ext, builder.build());
	}

	// 兑换商店
	public void convertShopRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		playerManager.refEndlessTD(player);
		ConvertShopRs.Builder builder = ConvertShopRs.newBuilder();
		List<EndlessTDShopGoods> convertShopList = tdManager.getConvertShopList(player);
		if (convertShopList == null || convertShopList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		builder.addAllGoods(convertShopList);
		builder.setRefreshTime(TimeHelper.getTimeOfDay(24));
		handler.sendMsgToPlayer(ConvertShopRs.ext, builder.build());
	}

	// 兑换商店兑换奖励
	public void buyConvertShopRq(ClientHandler handler, BuyConvertShopRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int id = rq.getPos();
		int count = rq.getCount() == 0 ? 1 : rq.getCount();
		StaticEndlessArmory endlessArmory = staticTDMgr.getEndlessShopMap(id);
		if (endlessArmory == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		int priceSum = count * endlessArmory.getCoin_price();
		EndlessTDInfo endlessTDInfo = tdManager.getEndlessTDInfo(player);
		Map<Integer, Integer> convertShopInfo = endlessTDInfo.getConvertShopInfo();
		int times = convertShopInfo.computeIfAbsent(id, x -> 0);
		int timesSum = times + count;
		if (timesSum > endlessArmory.getBuy_time()) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_BUY_ENOUGH);
			return;
		}
		if (player.getTdMoney() < priceSum) {
			handler.sendErrorMsgToPlayer(GameError.TD_MONEY_NOT_ENOUGH);
			return;
		}
		List<List<Integer>> prop = endlessArmory.getProp();
		if (prop == null || prop.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		playerManager.subAward(player, AwardType.LORD_PROPERTY, LordPropertyType.TD_MONEY, priceSum, Reason.BUY_CONVERT_SHOP);
		convertShopInfo.put(id, timesSum);
		List<EndlessTDShopGoods> convertShopList = tdManager.getConvertShopList(player);
		BuyConvertShopRs.Builder builder = BuyConvertShopRs.newBuilder();
		prop.forEach(e -> {
			if (e == null || e.size() != 3) {
				return;
			}
			Award award = new Award(e.get(0), e.get(1), count);
			playerManager.addAward(player, award, Reason.BUY_CONVERT_SHOP);
			builder.addAward(award.wrapPb().build());
		});
		if (convertShopList == null || convertShopList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		builder.addAllGoods(convertShopList);
		builder.setTdMoney(player.getTdMoney());
		handler.sendMsgToPlayer(BuyConvertShopRs.ext, builder.build());
	}

	// 军械商店
	public void battleShopRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		playerManager.refEndlessTD(player);
		BattleShopRs.Builder builder = BattleShopRs.newBuilder();
		List<EndlessTDShopGoods> armoryShopList = tdManager.getArmoryShopList(player);
		if (armoryShopList == null || armoryShopList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		builder.addAllGoods(armoryShopList);
		builder.setRefreshTime(TimeHelper.getTimeOfDay(24));
		builder.setRefreshGold(tdManager.getShopRefreshConsume(player));
		handler.sendMsgToPlayer(BattleShopRs.ext, builder.build());
	}

	// 购买军械商店物品
	public void buyBattleShopRq(ClientHandler handler, BuyBattleShopRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int id = rq.getPos();
		int count = rq.getCount() == 0 ? 1 : rq.getCount();
		StaticEndlessArmory endlessArmory = staticTDMgr.getEndlessArmory(id);
		if (endlessArmory == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		int priceSum = count * endlessArmory.getPrice();
		EndlessTDInfo endlessTDInfo = tdManager.getEndlessTDInfo(player);
		if (!endlessTDInfo.getArmoryShop().containsKey(id)) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
		}
		Map<Integer, Integer> armoryShopInfo = endlessTDInfo.getArmoryShopInfo();
		int times = armoryShopInfo.computeIfAbsent(id, x -> 0);
		int timesSum = times + count;
		if (timesSum > endlessArmory.getBuy_time()) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_BUY_ENOUGH);
			return;
		}
		Map<Integer, Integer> armoryShop = endlessTDInfo.getArmoryShop();
		Integer orDefault = armoryShop.getOrDefault(id, 0);
		if (orDefault != 0) {
			double discount = Double.valueOf(orDefault) / 100;
			priceSum = (int) Math.ceil(priceSum * discount);
		}
		if (player.getGold() < priceSum) {
			handler.sendErrorMsgToPlayer(GameError.TD_MONEY_NOT_ENOUGH);
			return;
		}
		List<List<Integer>> prop = endlessArmory.getProp();
		if (prop == null || prop.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		playerManager.subAward(player, AwardType.GOLD, 0, priceSum, Reason.BUY_CONVERT_SHOP);
		armoryShopInfo.put(id, timesSum);
		BuyBattleShopRs.Builder builder = BuyBattleShopRs.newBuilder();
		prop.forEach(e -> {
			if (e == null || e.size() != 3) {
				return;
			}
			Award award = new Award(e.get(0), e.get(1), count);
			playerManager.addAward(player, award, Reason.BUY_CONVERT_SHOP);
			builder.addAward(award.wrapPb());
		});

		List<EndlessTDShopGoods> armoryShopList = tdManager.getArmoryShopList(player);
		if (armoryShopList == null || armoryShopList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		builder.addAllGoods(armoryShopList);
		builder.setGold(player.getGold());
		builder.setDiscount(tdManager.getDiscountGoods(player));
		handler.sendMsgToPlayer(BuyBattleShopRs.ext, builder.build());
		TDTaskManager.staticRefreshTask(player, new ActTDSevenType(Lists.newArrayList(ActTDSevenType.tdTaskType_9), priceSum));
	}

	// 刷新玩家的军械商店物品
	public void refreshBattleShopRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int shopRefreshConsume = tdManager.getShopRefreshConsume(player);
		if (player.getGold() < shopRefreshConsume) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		// 刷新商店
		tdManager.refreshArmoryShop(player);
		EndlessTDInfo endlessTDInfo = tdManager.getEndlessTDInfo(player);
		endlessTDInfo.getArmoryShopInfo().clear();
		playerManager.subAward(player, AwardType.GOLD, 0, shopRefreshConsume, Reason.BUY_CONVERT_SHOP);
		Map<Integer, Integer> refreshShopTimes = endlessTDInfo.getRefreshShopTimes();
		refreshShopTimes.put(1, refreshShopTimes.computeIfAbsent(1, x -> 0) + 1);
		List<EndlessTDShopGoods> armoryShopList = tdManager.getArmoryShopList(player);
		if (armoryShopList == null || armoryShopList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		RefreshBattleShopRs.Builder builder = RefreshBattleShopRs.newBuilder();
		builder.addAllGoods(armoryShopList);
		builder.setRefreshTime(TimeHelper.getTimeOfDay(24));
		builder.setGold(player.getGold());
		builder.setRefreshGold(tdManager.getShopRefreshConsume(player));
		handler.sendMsgToPlayer(RefreshBattleShopRs.ext, builder.build());
		TDTaskManager.staticRefreshTask(player, new ActTDSevenType(Lists.newArrayList(ActTDSevenType.tdTaskType_9), shopRefreshConsume));

	}

	// 军需仓库
	public void quartermasterWarehouseRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Map<Integer, Item> itemMap = player.getItemMap();
		List<Item> collect = itemMap.values().stream().filter(e -> {
			StaticProp staticProp = staticPropMgr.getStaticProp(e.getItemId());
			return staticProp != null && staticProp.getPropType() == ItemType.ENDLESS_TD && e.getItemNum() > 0;
		}).collect(Collectors.toList());
		QuartermasterWarehouseRs.Builder builder = QuartermasterWarehouseRs.newBuilder();
		collect.forEach(e -> {
			builder.addProp(e.wrapPb());
		});
		handler.sendMsgToPlayer(QuartermasterWarehouseRs.ext, builder.build());
	}

	// 领取排行奖励
	public void receiveRankAwardRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		EndlessTDInfo endlessTDInfo = tdManager.getEndlessTDInfo(player);
		if (!endlessTDInfo.isRankAward()) {
			handler.sendErrorMsgToPlayer(GameError.ALREADY_GET_AWARD);
			return;
		}
		int lastWeekRank = endlessTDInfo.getLastWeekRank();
		List<Award> rankAwards = tdManager.getRankAwards(lastWeekRank);
		if (rankAwards.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ON_THE_LIST);
			return;
		}
		endlessTDInfo.setRankAward(false);
		ReceiveRankAwardRs.Builder builder = ReceiveRankAwardRs.newBuilder();
		rankAwards.forEach(e -> {
			playerManager.addAward(player, e, Reason.RANKING_REWARD);
			builder.addAward(e.wrapPb());
		});
		builder.setRankAward(endlessTDInfo.isRankAward());
		handler.sendMsgToPlayer(ReceiveRankAwardRs.ext, builder.build());
	}

	// 自动战斗
	public void fightAutoRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		EndlessTDInfo endlessTDInfo = tdManager.getEndlessTDInfo(player);
		if (endlessTDInfo.getRemainingTimes() < 1) {
			handler.sendErrorMsgToPlayer(GameError.REMAINING_TIMES_NOT_ENOUGH);
			return;
		}
		int weekMaxFraction = endlessTDInfo.getWeekMaxFraction();
		if (weekMaxFraction == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CHALLENGES_THIS_WEEK);
			return;
		}
		endlessTDInfo.deductRemainingTimes();
		EndlessTDGameInfo gameInfo = tdManager.getGameInfo(player);
		gameInfo.init();
		gameInfo.getSelectPropId().clear();
		StaticEndlessBaseinfo endlessBaseInfo = staticTDMgr.getEndlessBaseInfo();
		int tdMoney = weekMaxFraction / endlessBaseInfo.getCoin_reward();
		Award award = new Award(AwardType.LORD_PROPERTY, LordPropertyType.TD_MONEY, tdMoney);
		playerManager.addAward(player, award, Reason.ENDLESS_TD_FIGHT_AUTO);
		FightAutoRs.Builder builder = FightAutoRs.newBuilder();
		builder.setRemainingTimes(endlessTDInfo.getRemainingTimes());
		builder.setFraction(weekMaxFraction);
		builder.setAward(award.wrapPb());
		builder.setTdMoney(player.getTdMoney());
		handler.sendMsgToPlayer(FightAutoRs.ext, builder.build());
		TDTaskManager.staticRefreshTask(player, new ActTDSevenType(Lists.newArrayList(ActTDSevenType.tdTaskType_8), weekMaxFraction));
		TDTaskManager.staticRefreshTask(player, new ActTDSevenType(Lists.newArrayList(ActTDSevenType.tdTaskType_1), 2));
	}

	// 开始无尽模式挑战
	public void playEndlessTDRq(ClientHandler handler, PlayEndlessTDRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (!tdManager.checkEndlessTDOpen(player)) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		EndlessTDGameInfo gameInfo = tdManager.getGameInfo(player);
		EndlessTDInfo endlessTDInfo = tdManager.getEndlessTDInfo(player);
		gameInfo.putToken();
		if (gameInfo.getWave() == 0) {
			if (endlessTDInfo.getRemainingTimes() <= 0) {
				handler.sendErrorMsgToPlayer(GameError.REMAINING_TIMES_NOT_ENOUGH);
				return;
			}
			List<Integer> propIdList = rq.getPropIdList();
			HashSet<Integer> limit = new HashSet<>();
			// 校验道具同类型的只能一个
			for (Integer proId : propIdList) {
				Item item = player.getItem(proId);
				if (item == null || item.getItemNum() < 1) {
					handler.sendErrorMsgToPlayer(GameError.COUNT_ERROR);
					return;
				}
				StaticEndlessItem endlessItem = staticTDMgr.getEndlessItem(proId);
				if (endlessItem == null) {
					handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
					return;
				}
				if (limit.contains(endlessItem.getColor())) {
					handler.sendErrorMsgToPlayer(GameError.DUPLICATE_TYPE_CANNOT_BE_SELECTED);
					return;
				}
				limit.add(endlessItem.getColor());
			}
			propIdList.forEach(propId -> {
				playerManager.subItem(player, propId, 1, Reason.ENDLESS_TD_START_GAME);
			});
			gameInfo.getSelectPropId().clear();
			gameInfo.getSelectPropId().addAll(propIdList);
			// 设置挑战开始时间
			gameInfo.setStartDate(GameServer.getInstance().currentDay);
			newGame(handler, rq, player);
		} else if (rq.getRePlay()) {
			if (gameInfo.getWave() >= 4) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			}
			newGame(handler, rq, player);
		} else {
			continueTheGame(handler, player);
		}
		gameInfo.setCrtime(rq.getCrtime());
		TDTaskManager.staticRefreshTask(player, new ActTDSevenType(Lists.newArrayList(ActTDSevenType.tdTaskType_1), 2));
	}

	// 新的游戏
	public void newGame(ClientHandler handler, PlayEndlessTDRq rq, Player player) {
		// 初始化游戏基础数据
		tdManager.putGameInfo(player);
		EndlessTDGameInfo gameInfo = tdManager.getGameInfo(player);
		// 处理玩家选择的道具
		gameInfo.getSelectPropId().forEach(propId -> {
			tdManager.obtainProp(player, propId, 1);
		});
		// 处理数据并返还到前端
		PlayEndlessTDRs.Builder builder = tdManager.getPlayEndlessTDRs(player);
		handler.sendMsgToPlayer(PlayEndlessTDRs.ext, builder.build());
	}

	// 继续游戏
	public void continueTheGame(ClientHandler handler, Player player) {
		// 处理数据并返还到前端
		PlayEndlessTDRs.Builder builder = tdManager.getPlayEndlessTDRs(player);
		handler.sendMsgToPlayer(PlayEndlessTDRs.ext, builder.build());
	}

	// 无尽塔防关卡小结算
	public void endlessTDReportRq(ClientHandler handler, EndlessTDReportRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		EndlessTDGameInfo gameInfo = tdManager.getGameInfo(player);
		// 关卡id
		int levelId = rq.getLevelId();
		// token
		String token = rq.getToken();
		// 校验token
		if (gameInfo.checkToken(String.valueOf(levelId), token)) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		// 二次校验
		int endcombat = rq.getEndcombat();
		int verEndCombat = gameInfo.getCrtime() - rq.getLifePoint() * 3 - gameInfo.getCrtime() % 10;
		if (endcombat != verEndCombat) {
			// 发送日志至GM后台
			EndlessTDErrorLog endlessTDErrorLog = new EndlessTDErrorLog(player.roleId, player.getNick(), player.getAccount().getServerId(), gameInfo.getWave(), gameInfo.getLevelId(), 0, 0, 0L, 0, rq.getLifePoint(), gameInfo.getLifePoint(), "3", gameInfo.getStartDate());
			
			SpringUtil.getBean(ServerStatisticsJob.class).recordEndlessTDErrorData(endlessTDErrorLog);
			// handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			// return;
		}
		rq.getLevelId();
		gameInfo.gameSettlement(rq);
		tdManager.checkFraction(player, rq.getFraction(), rq.getLifePoint());
		int luckId = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.INCREASE_LUCK).getLimit();
		int addId = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.INCREASE_PROP).getLimit();
		int awardNum = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.NUMBER_OF_AWARDS).getLimit();
		List<List<Integer>> unique = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.UNIQUE).getParam2();
		StaticEndlessLevel endlessLevel = staticTDMgr.getEndlessLevel(gameInfo.getLevelId());
		List<List<Integer>> drop = endlessLevel.getDrop();
		// 更好的掉落概率
		if (tdManager.checkSpecialItem(player, luckId) > 0) {
			drop = endlessLevel.getBetterdrop();
		}
		// 更多的掉落个数
		if (tdManager.checkSpecialItem(player, addId) > 0) {
			awardNum += 1;
		}
		// 整局游戏里存在获取次数限制的道具[道具id, 获取次数]
		Map<Integer, StaticEndlessItem> itemMap = new HashMap<>(staticTDMgr.getEndlessItemMap());
		unique.forEach(e -> {
			int propId = e.get(0);
			if (tdManager.checkSpecialItem(player, propId) >= e.get(1)) {
				itemMap.remove(propId);
			}
		});
		// 在怪物减速大于等于30%后，无法掉落选定的道具
		int slowDownLimit = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.MONSTER_SLOW_DOWN).getLimit();
		List<Integer> slowDownParam = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.MONSTER_SLOW_DOWN).getParam();
		if (gameInfo.getPropBuff().getOrDefault(EndlessTDItemEffectType.type_1, 0) >= slowDownLimit) {
			slowDownParam.forEach(e -> {
				itemMap.remove(e);
			});
		}
		// 当升级效率大于X后无法选择的道具
		int towerUpgradeLimit = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.TOWER_UPGRADE_LIMIT).getLimit();
		List<Integer> towerUpgradeParam = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.TOWER_UPGRADE_LIMIT).getParam();
		if (gameInfo.getPropBuff().getOrDefault(EndlessTDItemEffectType.type_23, 0) >= towerUpgradeLimit) {
			towerUpgradeParam.forEach(e -> {
				itemMap.remove(e);
			});
		}
		Map<Integer, Integer> awardItems = gameInfo.getAwardItems();
		for (int i = 0; i < awardNum; i++) {
			int randomTotal = 0;
			for (List<Integer> list : drop) {
				randomTotal += list.get(1);
			}
			int color = 0;
			int randomNumber = RandomUtil.getRandomNumber(randomTotal);
			randomTotal = 0;
			for (List<Integer> list : drop) {
				randomTotal += list.get(1);
				if (randomTotal > randomNumber) {
					color = list.get(0);
					break;
				}
			}
			List<StaticEndlessItem> randomAwards = Lists.newArrayList();
			for (StaticEndlessItem value : itemMap.values()) {
				if (value.getColor() == color && !awardItems.containsKey(value.getPropId())) {
					randomAwards.add(value);
				}
			}
			Collections.shuffle(randomAwards);
			if (!randomAwards.isEmpty()) {
				StaticEndlessItem staticEndlessItem = randomAwards.get(0);
				awardItems.put(staticEndlessItem.getPropId(), 1);
			}
		}
		if (awardItems.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		EndlessTDReportRs.Builder builder = EndlessTDReportRs.newBuilder();
		// 测试用 掉落指定道具
		StaticEndlessTDDropLimit endlessTDDropLimit = staticTDMgr.getEndlessTDDropLimit(TdDropLimitId.TEST_IDENTICAL_PROP);
		if (endlessTDDropLimit != null && endlessTDDropLimit.getLimit() == 1 && !endlessTDDropLimit.getParam().isEmpty()) {
			awardItems.clear();
			endlessTDDropLimit.getParam().forEach(e -> {
				awardItems.put(e, 1);
			});
		}
		awardItems.forEach((k, v) -> {
			builder.addProp(Prop.newBuilder().setPropId(k).setPropNum(v).build());
		});

		handler.sendMsgToPlayer(EndlessTDReportRs.ext, builder.build());
		TDTaskManager.staticRefreshTask(player, new ActTDSevenType(Lists.newArrayList(ActTDSevenType.tdTaskType_6, ActTDSevenType.tdTaskType_10), levelId, 0, 0));
	}

	// 无尽塔防选择道具
	public void selectEndlessTDProRq(ClientHandler handler, SelectEndlessTDProRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int propId = rq.getPropId();
		EndlessTDGameInfo gameInfo = tdManager.getGameInfo(player);
		if (!gameInfo.getAwardItems().containsKey(propId)) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		gameInfo.putToken();
		tdManager.obtainProp(player, propId, 1);
		tdManager.nextLevel(player);
		gameInfo.getAwardItems().clear();
		SelectEndlessTDProRs.Builder builder = SelectEndlessTDProRs.newBuilder();
		builder.setBase(tdManager.getBaseBuilder(player));
		handler.sendMsgToPlayer(SelectEndlessTDProRs.ext, builder.build());
	}

	// 无尽塔防使用道具
	public void useEndlessTDProRq(ClientHandler handler, UseEndlessTDProRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int propId = rq.getPropId();
		EndlessTDGameInfo gameInfo = tdManager.getGameInfo(player);
		Integer propCount = gameInfo.getItemMap().get(propId);
		if (propCount == null || propCount <= 0) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		tdManager.obtainProp(player, propId, 0);
		UseEndlessTDProRs.Builder builder = UseEndlessTDProRs.newBuilder();
		builder.addAllPassiveProps(gameInfo.takeEffectWrapPb());
		builder.addAllProp(gameInfo.itemMapWrapPb());
		handler.sendMsgToPlayer(UseEndlessTDProRs.ext, builder.build());
	}

	// 无尽塔防挑战结束
	public void endlessTDOverRq(ClientHandler handler, EndlessTDOverRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		EndlessTDGameInfo gameInfo = tdManager.getGameInfo(player);
		EndlessTDInfo endlessTDInfo = tdManager.getEndlessTDInfo(player);
		String token = rq.getToken();
		// 校验token
		if (gameInfo.checkToken(String.valueOf(gameInfo.getWave()), token)) {
			handler.sendErrorMsgToPlayer(GameError.TOKEN_LOST);
			return;
		}
		int startDate = gameInfo.getStartDate();
		if (gameInfo.getStartDate() == GameServer.getInstance().currentDay) {
			endlessTDInfo.deductRemainingTimes();
		}
		tdManager.checkFraction(player, rq.getFraction(), 0);
		int fraction = gameInfo.getFraction();
		EndlessTDOverRs.Builder builder = EndlessTDOverRs.newBuilder();
		builder.setBestResults(fraction > endlessTDInfo.getWeekMaxFraction());
		endlessTDInfo.putWeekMaxFraction(fraction);
		tdManager.updateWeekEndlessTDRank(player);
		builder.setFraction(fraction);
		builder.setRemainingTimes(endlessTDInfo.getRemainingTimes());
		StaticEndlessBaseinfo endlessBaseInfo = staticTDMgr.getEndlessBaseInfo();
		int tdMoney = fraction / endlessBaseInfo.getCoin_reward();
		Award award = new Award(AwardType.LORD_PROPERTY, LordPropertyType.TD_MONEY, tdMoney);
		playerManager.addAward(player, award, Reason.ENDLESS_TD_FIGHT_AUTO);
		builder.setAward(award.wrapPb());
		handler.sendMsgToPlayer(EndlessTDOverRs.ext, builder.build());
		SpringUtil.getBean(LogUser.class).getEndlessTDLog(new EndlessTDLog().builder().lordId(player.roleId).nick(player.getNick()).serverId(player.getAccount().getServerId()).fraction(fraction).levelTime(gameInfo.getLevelTime()).levelFraction(gameInfo.getLevelFraction()).startTime(startDate).endTime(GameServer.getInstance().currentDay).build());
		gameInfo.init();
		gameInfo.getSelectPropId().clear();
		TDTaskManager.staticRefreshTask(player, new ActTDSevenType(Lists.newArrayList(ActTDSevenType.tdTaskType_8), fraction));
	}

	// 无尽塔防炮塔初始化
	public void endlessTDTowerInitRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		TDPb.EndlessTDTowerInitRs.Builder builder = TDPb.EndlessTDTowerInitRs.newBuilder();
		staticTDMgr.getTdEndlessTower().forEach((e, f) -> {
			builder.addTowers(CommonPb.TowerInfo.newBuilder().setId(f.getId()).setName(f.getName()).setIcon(f.getIcon()).setModel(f.getModel()).setType(f.getType()).setLevel(f.getLevel()).setDamage(f.getDamage()).setAttackRate(f.getAttack_rate()).setAttackRange(f.getAttack_range()).setDamageArea(f.getDamage_area()).setBuildCost(f.getBuild_cost()).setUpgradeTarget(f.getUpgrade_target()).setRecoverPrice(f.getRecover_price()).setBulletModel(f.getBullet_model()).setBulletSpeed(f.getBullet_speed()).setDesc(f.getDesc()).build());
		});
		handler.sendMsgToPlayer(TDPb.EndlessTDTowerInitRs.ext, builder.build());
	}

	/**
	 * 拉取关卡信息
	 *
	 * @param handler
	 */
	public void bulletWarInfo(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Map<Integer, StaticBulletWarLevel> allStaticBulletWarLevel = staticTDMgr.getAllStaticBulletWarLevel();
		BulletWarInfo bulletWarInfo = player.getBulletWarInfo();
		TDPb.BulletWarInfoRs.Builder builder = TDPb.BulletWarInfoRs.newBuilder();
		builder.setMaxLevel(bulletWarInfo.getMaxId());
		Map<Integer, CommonPb.TwoInt> map = bulletWarInfo.getMap();
		allStaticBulletWarLevel.values().forEach(x -> {
			if (x.getExtra_award() != null) {
				CommonPb.TwoInt twoInt = map.get(x.getId());
				if (twoInt != null) {
					CommonPb.BulletWarInfo.Builder builder1 = CommonPb.BulletWarInfo.newBuilder();
					builder1.setLevelId(x.getId());
					builder1.setAwardState(twoInt);
					builder.addInfo(builder1);
				}
			}
		});
		handler.sendMsgToPlayer(TDPb.BulletWarInfoRs.ext, builder.build());
	}

	// 领取奖励
	public void bulletWarAward(TDPb.BulletWarLevelAwardRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int levelId = rq.getLevelId();
		StaticBulletWarLevel staticBulletWarLevel = staticTDMgr.getStaticBulletWarLevel(levelId);
		if (staticBulletWarLevel == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		BulletWarInfo bulletWarInfo = player.getBulletWarInfo();
		Map<Integer, CommonPb.TwoInt> map = bulletWarInfo.getMap();
		int type = rq.getType();
		List<List<Integer>> award;
		if (type == 1) {
			// 领取关卡奖励
			CommonPb.TwoInt twoInt = map.get(levelId);
			if (twoInt == null) {
				CommonPb.TwoInt.Builder builder = CommonPb.TwoInt.newBuilder();
				builder.setV1(2);
				map.put(levelId, builder.build());
			} else {
				if (twoInt.getV1() == 2) {
					handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
					return;
				}
			}
			award = staticBulletWarLevel.getAward();
			if (levelId > bulletWarInfo.getMaxId()) {
				bulletWarInfo.setMaxId(levelId);
			}
		} else {
			CommonPb.TwoInt twoInt = map.get(levelId);
			if (twoInt == null) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			}
			award = staticBulletWarLevel.getExtra_award();
			CommonPb.TwoInt.Builder builder = twoInt.toBuilder();
			builder.setV2(2);
			map.put(levelId, builder.build());
		}
		TDPb.BulletWarLevelAwardRs.Builder builder = TDPb.BulletWarLevelAwardRs.newBuilder();
		builder.setLevelId(levelId);
		builder.setType(type);
		award.forEach(x -> {
			playerManager.addAward(player, x.get(0), x.get(1), x.get(2), Reason.TD_STAR_REWARD);
			builder.addAward(CommonPb.Award.newBuilder().setType(x.get(0)).setId(x.get(1)).setCount(x.get(2)).build());
		});
		handler.sendMsgToPlayer(TDPb.BulletWarLevelAwardRs.ext, builder.build());
	}
}
