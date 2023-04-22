package com.game.manager;

import com.game.constant.BattleEntityType;
import com.game.constant.ChatId;
import com.game.constant.MailId;
import com.game.constant.MapId;
import com.game.constant.WarType;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.dataMgr.StaticZerglMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.City;
import com.game.domain.p.Team;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticMonster;
import com.game.domain.s.StaticWorldActPlan;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticZergMonster;
import com.game.domain.s.StaticZergRound;
import com.game.pb.BasePb.Base;
import com.game.pb.CommonPb;
import com.game.pb.WorldPb;
import com.game.pb.ZergPb.SynZergRq;
import com.game.server.GameServer;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.PlayerCity;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.zerg.PlayerAttack;
import com.game.worldmap.fight.zerg.ZergConst;
import com.game.worldmap.fight.zerg.ZergData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZergManager {

	@Autowired
	private WorldManager worldManager;
	@Autowired
	private StaticWorldMgr staticWorldMgr;
	@Autowired
	private StaticWorldActPlanMgr worldActPlanMgr;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private CityManager cityManager;
	@Autowired
	private StaticZerglMgr staticZerglMgr;
	@Autowired
	private ChatManager chatManager;
	@Autowired
	private BroodWarManager broodWarManager;
	@Autowired
	private WarManager warManager;
	@Autowired
	private WorldTargetManager worldTargetManager;

    public void init() {
        // 拉取最新的虫族主宰信息
        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_13);
        if (worldActPlan == null) {
            LogHelper.CONFIG_LOGGER.info("【虫族主宰】 活动未配置");
            return;
        }
        if (worldData.getZergData() == null) {
            // 初始化活动开始时间

        }
        LogHelper.GAME_LOGGER.info("【虫族主宰】 活动状态:{} 预热时间:{} 开启时间:{} 结束时间:{}", worldActPlan.getState(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));
    }

	/**
	 * 定时维护
	 *
	 * @param worldActPlan
	 */
	public void checkZergWorldActPlan(WorldActPlan worldActPlan) {
		if (!isFinsihCond(WorldActivityConsts.ACTIVITY_13)) {
			return;
        }

		long currentTime = System.currentTimeMillis();
		if (worldActPlan.getState() == WorldActPlanConsts.NOE_OPEN || worldActPlan.getState() == WorldActPlanConsts.END) {//没有开启
			long preheat = worldActPlan.getPreheatTime();
			if (preheat <= currentTime) {//开始预热
				onStateChange(worldActPlan, WorldActPlanConsts.PREHEAT).thenAcceptAsync(e -> {// 预热阶段,初始化虫族主宰数据
                    getZergData();
					syncWorldActivityPlan();// 通知所有玩家活动预热
					synActivityOpen();
					LogHelper.GAME_LOGGER.info("【虫族主宰】 预热时间:{} 开启时间:{} 结束时间:{}", DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));
				});
			}
		} else if (worldActPlan.getState() == WorldActPlanConsts.PREHEAT) {//预热阶段
            getZergData();
			if (worldActPlan.getOpenTime() <= currentTime) {// 活动开启
				onStateChange(worldActPlan, WorldActPlanConsts.OPEN).thenAcceptAsync(e -> {
					syncWorldActivityPlan();// 通知所有玩家活动开始
					StaticWorldCity staticWorldCity = staticWorldMgr.getCity(getZergData().getCityId());
					String Pos = staticWorldCity.getX() + "," + staticWorldCity.getY();
					City city = cityManager.getCity(getZergData().getCityId());
					chatManager.sendWorldChat(ChatId.ZERG_MASTER_OPEN, city.getCityName() == null ? staticWorldCity.getName() : city.getCityName(), Pos);
				});
				LogHelper.GAME_LOGGER.info("【虫族主宰】 活动开始");
			}
		} else if (worldActPlan.getState() == WorldActPlanConsts.OPEN) {//开启阶段
			if (worldActPlan.getEndTime() <= currentTime) {// 活动开启
				onStateChange(worldActPlan, WorldActPlanConsts.DO_END);
				LogHelper.GAME_LOGGER.info("【虫族主宰】 活动结束");
			}
		} else if (worldActPlan.getState() == WorldActPlanConsts.DO_END) {
			doEnd(worldActPlan).thenAcceptAsync(ret -> {
				if (ret == ZergConst.NONE) {
					return;
				}
				if (ret == ZergConst.AHEAD_END) {
					playerManager.getPlayers().forEach((e, f) -> {
						f.getSimpleData().addZergScore(ZergConst.SCORE_WIN);
						playerManager.sendNormalMail(f, MailId.ZERG_WIN_SCORE, String.valueOf(ZergConst.SCORE_WIN));
					});
					chatManager.sendWorldChat(ChatId.ZERG_MASTER_WIN);
				} else if (ret == ZergConst.END) {
					playerManager.getPlayers().forEach((e, f) -> {
						f.getSimpleData().addZergScore(ZergConst.SCORE_ATTEND);
						playerManager.sendNormalMail(f, MailId.ZERG_HAND_SCORE, String.valueOf(ZergConst.SCORE_ATTEND));
					});
					chatManager.sendWorldChat(ChatId.ZERG_MASTER_END);
				}
				syncWorldActivityPlan();
				synActivityOpen();
				LogHelper.GAME_LOGGER.info("【虫族主宰】 结算完毕");
			});
		}
	}

	public boolean isFinsihCond(int actId) {
		if (actId != WorldActivityConsts.ACTIVITY_13) {
			return true;
		}
		long count = cityManager.getSquareFortress().stream().filter(e -> cityManager.getCity(e).getCountry() != 0).count();
		if (count != 3) {//三个国家均在母巢中占有城池
			return false;
		}

		int rank = broodWarManager.getBroodWarRank();
		if (rank < 1) {// 开启过母巢之战
			return false;
		}
		return true;
	}

//    private ZergData initZergData(WorldActPlan worldActPlan) {
//        WorldData worldData = worldManager.getWolrdInfo();
//
//        // 首次初始化
//        if (worldData.getZergData() == null) {
//            worldData.setZergData(new ZergData());
//        }
//
//        ZergData zergData = worldData.getZergData();
//
//        // 初始化boss信息
//        if (zergData.getStartTime() != worldActPlan.getOpenTime()) {
//            StaticMonster staticMonster = staticZerglMgr.getShowMonster(ZergConst.STEP_ATTACK, 1);
//            Team team = staticZerglMgr.initTeam(ZergConst.STEP_ATTACK, 1);
//            zergData.setStartTime(worldActPlan.getOpenTime());
//            zergData.setEndTime(worldActPlan.getEndTime());
//            zergData.setMonsterId(staticMonster.getMonsterId());
//            zergData.setStatus(worldActPlan.getState());
//            zergData.setStep(ZergConst.STEP_INIT);// 初始化步骤
//            zergData.setStepEndTime(0);
//            zergData.setStepParam(new ArrayList<>());
//            zergData.setOpenTimes(zergData.getOpenTimes() + 1);
//            zergData.setTeam(team);
//            zergData.setStep(0);
//
//            // 虫族主宰城池
//            int cityId = cityManager.getSquareFortress().stream().filter(e -> cityManager.getCity(e).getCountry() == 0).findFirst().get();
//            zergData.setCityId(cityId);
//            LogHelper.GAME_LOGGER.info("【虫族主宰】初始化信息 城池ID:{} bossId:{} 开启时间:{}", zergData.getCityId(), zergData.getMonsterId(), DateHelper.getDate(zergData.getStartTime()));
//        }
//
//        return zergData;
//    }

    /**
     * 切换战斗形态1.攻击 2.防守
     */
    public void checkRound(WorldActPlan worldActPlan) {
        // 没有开启则不进行阶段转换
        if (worldActPlan.getState() != WorldActPlanConsts.OPEN) {
            return;
        }

        ZergData zergData = getZergData();

		// 主宰已被击杀
		if (!zergData.getTeam().isAlive()) {
			LogHelper.GAME_LOGGER.info("【虫族主宰】 已击杀");
			worldActPlan.setState(WorldActPlanConsts.DO_END);
			return;
		}

		// 阶段未结束
        long currentTime = System.currentTimeMillis();
		if (zergData.getStepEndTime() > currentTime) {
			return;
		}

        StaticZergRound currentZergRound = zergData.getStep() == ZergConst.STEP_INIT ? null : staticZerglMgr.getRound(zergData.getStep());

        int finishId = currentZergRound != null ? currentZergRound.getRoundFinish() : 0;
        int nextId = currentZergRound != null ? currentZergRound.getNextId() : 1;

//        LogHelper.GAME_LOGGER.info("【虫族主宰.回合】 step:{} stepFinish:{} 当前回合结束值:{} nextId:{}", zergData.getStep(), zergData.getStepFinish(), finishId, nextId);

        if (zergData.getStepFinish() != finishId) {
            return;
        }

        // 开启下一阶段
        StaticZergRound nextRound = staticZerglMgr.getRound(nextId);
        if (nextRound == null) {// 当前阶段结束,没有下一阶段,活动结束
//            worldActPlan.setState(WorldActPlanConsts.DO_END);
            return;
        }

        // 阶段结束时间
        long stepEndTime = zergData.getStartTime() + nextRound.getEndTime();

        zergData.setStep(nextRound.getId());
        zergData.setStepEndTime(stepEndTime);
        zergData.setRecordDate(TimeHelper.getCurrentDay());

        if (nextRound.getType() == ZergConst.STEP_ATTACK) {// 进攻
			doAttackStep(stepEndTime).thenAcceptAsync(e -> {
				synAttackCityWarInfo(e);
				LogHelper.GAME_LOGGER.info("【虫族主宰】 进攻阶段");
			});
		} else {// 防守
            doDefendStep(nextRound, stepEndTime).thenAcceptAsync(e -> {
				for (WarInfo warInfo : e) {
					if (warInfo.getWarType() == WarType.DEFEND_ZERG) {
						synDefenceWarInfo(warInfo);
						createZergMarchAndSyn(warInfo);
					} else if (warInfo.getWarType() == WarType.ATTACK_ZERG) {
						synAttackCityWarInfo(warInfo);
					}
				}
				LogHelper.GAME_LOGGER.info("【虫族主宰】 防守阶段");
				chatManager.sendWorldChat(ChatId.ZERG_MASTER_DEFEND);
			});
		}
	}


	public CompletableFuture<Void> onStateChange(WorldActPlan worldActPlan, int state) {
		worldActPlan.setState(state);
		return CompletableFuture.allOf();
	}


	/**
	 * 处理结束
	 *
	 * @param worldActPlan
	 */
	public CompletableFuture<Integer> doEnd(WorldActPlan worldActPlan) {
		int ret = ZergConst.NONE;
        ZergData zergData = getZergData();

		if (zergData.getStartTime() != worldActPlan.getOpenTime()) {
			return CompletableFuture.completedFuture(ret);
		}

		long currentTime = System.currentTimeMillis();
		if (currentTime < worldActPlan.getEndTime()) {// 提前结束
			ret = ZergConst.AHEAD_END;
		} else {
			ret = ZergConst.END;
		}

		if (worldActPlan.getState() == WorldActPlanConsts.NOE_OPEN) {
			return CompletableFuture.completedFuture(ret);
		}

		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(WorldActivityConsts.ACTIVITY_13);

		long startTime = zergData.getStartTime();// 当前次的开启时间

		int roundType = worldTargetManager.getRoundType(worldManager.getWolrdInfo(), staticWorldActPlan);
		long date = TimeHelper.getTime(startTime, roundType, staticWorldActPlan.getWeekTime(), staticWorldActPlan.getTime());
		long preheat = TimeHelper.getTime(new Date(date), staticWorldActPlan.getPreheat());
		long endTime = date + staticWorldActPlan.getEndTime() * 60 * 1000L;
		worldActPlan.setOpenTime(date);
		worldActPlan.setPreheatTime(preheat);
		worldActPlan.setEndTime(endTime);
		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);

		LogHelper.GAME_LOGGER.info("【虫族主宰】下一轮开启 预热时间:{} 开启时间:{} 结束时间:{}", DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));
		return CompletableFuture.completedFuture(ret);
	}


	/**
	 * 进攻阶段
	 *
	 * @return
	 */
	public CompletableFuture<WarInfo> doAttackStep(long endTime) {
		getZergData().setStepParam(new ArrayList<>());
		WarInfo warInfo = createAttackWar(endTime);
		return CompletableFuture.completedFuture(warInfo);
	}

	public WarInfo createAttackWar(long endTime) {
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(getZergData().getCityId());
		Pos pos = new Pos(staticWorldCity.getX(), staticWorldCity.getY());
		StaticMonster staticMonster = staticZerglMgr.getShowMonster(ZergConst.STEP_ATTACK, 1);
		WarInfo warInfo = warManager.createZergAttackWar(staticMonster.getMonsterId(), endTime, getZergData().getCityId(), pos, WarType.ATTACK_ZERG);
		return warInfo;
	}

	public ZergData getZergData() {
		WorldData worldData = worldManager.getWolrdInfo();

        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_13);
        if (worldActPlan == null) {
            return null;
        }

        if (worldData.getZergData() == null) {
            worldData.setZergData(new ZergData());
        }

        ZergData zergData = worldData.getZergData();

        // 记录数据为另外一次虫族主宰
        if (zergData.getStartTime() != worldActPlan.getOpenTime()) {

            StaticMonster staticMonster = staticZerglMgr.getShowMonster(ZergConst.STEP_ATTACK, 1);
            Team team = createTeamMonsterId(staticMonster.getMonsterId());

            zergData.setStartTime(worldActPlan.getOpenTime());
            zergData.setEndTime(worldActPlan.getEndTime());
            zergData.setMonsterId(staticMonster.getMonsterId());
            zergData.setStatus(worldActPlan.getState());
            zergData.setStep(ZergConst.STEP_INIT);// 初始化步骤
            zergData.setStepFinish(0);
            zergData.setStepEndTime(0);
            zergData.setStepParam(new ArrayList<>());
            zergData.setOpenTimes(zergData.getOpenTimes() + 1);
            zergData.setTeam(team);
            zergData.setStep(0);

            // 虫族主宰城池
            int cityId = cityManager.getSquareFortress().stream().filter(e -> cityManager.getCity(e).getCountry() == 0).findFirst().get();
            zergData.setCityId(cityId);
            LogHelper.GAME_LOGGER.info("【虫族主宰】初始化信息 城池ID:{} bossId:{} 开启时间:{}", zergData.getCityId(), zergData.getMonsterId(), DateHelper.getDate(zergData.getStartTime()));
        }
        return zergData;
    }

	public CompletableFuture<List<WarInfo>> doDefendStep(StaticZergRound staticZergRound, long stepEndTime) {
		List<WarInfo> resultList = new ArrayList<>();
		ZergData zergData = getZergData();
		zergData.getStepParam().clear();

		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		Map<Pos, PlayerCity> playerCityMap = mapInfo.getPlayerCityMap();

		Map<Integer, List<PlayerAttack>> groupMap = playerCityMap.entrySet().parallelStream().map(e -> {
			return new PlayerAttack(e.getKey(), e.getValue());
		}).collect(Collectors.groupingBy(PlayerAttack::getCountry));

		StaticMonster staticMonster = staticZerglMgr.getShowMonster(ZergConst.STEP_DEFEND, staticZergRound.getWave());
		int monsterId = staticMonster.getMonsterId();

		for (int i = 1; i <= 3; i++) {
			if (groupMap.get(i) == null) {// 可能区域都没有人
				continue;
			}
			List<PlayerAttack> targets = groupMap.get(i).stream().limit(10).collect(Collectors.toList());
			targets.forEach(e -> {
				WarInfo warInfo = warManager.createZergDefendWar(monsterId, stepEndTime, e, WarType.DEFEND_ZERG);
				resultList.add(warInfo);
				zergData.getStepParam().add(e.getPlayerCity().getLordId());
			});
		}

		// 防守阶段也能打boss
		WarInfo attackWar = createAttackWar(stepEndTime);
		resultList.add(attackWar);

		return CompletableFuture.completedFuture(resultList);
	}

    public Team createTeamMonsterId(int showId) {
		StaticZergMonster staticZergMonster = staticZerglMgr.getShow(showId);
		List<Integer> monsters = staticZergMonster.getMonsters();
		Team team = new Team();
		for (Integer monsterId : monsters) {
			BattleEntity battleEntity = staticZerglMgr.createMonster(monsterId, BattleEntityType.BIG_MONSTER);
			if (battleEntity != null) {
				team.add(battleEntity);
			}
		}

		return team;
	}


	/**
	 * 给所有玩家推送活动消息
	 */
	public void syncWorldActivityPlan() {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldPb.SyncWorldActivityPlan.Builder builder = WorldPb.SyncWorldActivityPlan.newBuilder();
		for (WorldActPlan p : worldData.getWorldActPlans().values()) {
			StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(p.getId());
			if (staticWorldActPlan == null || staticWorldActPlan.getId() == WorldActivityConsts.ACTIVITY_1) {
				continue;
			}
			WorldPb.WorldActivityPlan.Builder worldActivityPlan = WorldPb.WorldActivityPlan.newBuilder();
			CommonPb.WorldActPlan.Builder worldActPlan = CommonPb.WorldActPlan.newBuilder();
			worldActPlan.setId(p.getId());
			worldActPlan.setPreheatTime(p.getPreheatTime());
			worldActPlan.setOpenTime(p.getOpenTime());
			worldActPlan.setState(p.getState());
			worldActPlan.setEndTime(p.getEndTime());
			worldActivityPlan.setWorldActPlan(worldActPlan);
			worldActivityPlan.addAllPrams(staticWorldActPlan.getContinues());
			builder.addWorldActivityPlan(worldActivityPlan);
		}

		WorldPb.SyncWorldActivityPlan msg = builder.clone().build();

		for (Player player : playerManager.getOnlinePlayer()) {
			SynHelper.synMsgToPlayer(player, WorldPb.SyncWorldActivityPlan.EXT_FIELD_NUMBER, WorldPb.SyncWorldActivityPlan.ext, msg);
		}
	}


	/**
	 * 防守阶段推送
	 *
	 * @param warInfo
	 */
	public void synDefenceWarInfo(WarInfo warInfo) {
		Player target = playerManager.getPlayer(warInfo.getDefencerId());
		if (target == null) {
			return;
		}
		if (target.isLogin && target.getChannelId() == -1 && warInfo != null) {
			WorldPb.SynCityWarRq.Builder builder = WorldPb.SynCityWarRq.newBuilder();
			builder.addAddWarInfo(worldManager.createWarInfo(warInfo, null));
			SynHelper.synMsgToPlayer(target, WorldPb.SynCityWarRq.EXT_FIELD_NUMBER, WorldPb.SynCityWarRq.ext, builder.build());
		}
	}


	/**
	 * 进攻阶段推送
	 *
	 * @param warInfo
	 */
	public void synAttackCityWarInfo(WarInfo warInfo) {
		WorldPb.SynCountryWarRq warRq = WorldPb.SynCountryWarRq.newBuilder().setWarInfo(warInfo.wrapPb(false).build()).build();
		playerManager.getOnlinePlayer().forEach(target -> {
			if (!target.isLogin) {
				return;
			}
			playerManager.synWarInfoToPlayer(target, warRq);
		});
	}

	public void synActivityOpen() {
		SynZergRq.Builder builder = SynZergRq.newBuilder();
		builder.setState(1);
		Base.Builder msg = PbHelper.createSynBase(SynZergRq.EXT_FIELD_NUMBER, SynZergRq.ext, builder.build());
		playerManager.getOnlinePlayer().forEach(target -> {
			if (!target.isLogin) {
				return;
			}
			GameServer.getInstance().sendMsgToPlayer(target, msg);
		});
	}

	/**
	 * 防守阶段创建主宰的行军且广播
	 *
	 * @param warInfo
	 */
	public void createZergMarchAndSyn(WarInfo warInfo) {
		Player player = playerManager.getPlayer(warInfo.getDefencerId());
		March march = worldManager.createZergMarch(player, warInfo, player.getPos());
		warInfo.addAttackMarch(march);

		// 行军数据添加到地图
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		mapInfo.addMarch(march);

		// 推送给国家有城战以及行军数据
		worldManager.synAddCityWar(player, warInfo);
		worldManager.synMarch(march, player.getCountry());
	}

//	public CompletableFuture<March> synMarch(MapInfo mapInfo, March march) {
//		long warId = march.getWarId();
//		IWar war = mapInfo.getWar(warId);
//		if (war == null) {
//			return CompletableFuture.completedFuture(null);
//		}
//		march.setState(MarchState.Waiting);
//		march.setEndTime(war.getEndTime() - 1000);
//		if (march.getMarchType() == MarchType.ZERG_WAR) {
////			warInfo.addCityAttacker(march);
//		} else {
//			war.getDefencer().addMarch(march);
//		}
//		return CompletableFuture.completedFuture(march);
//	}

	public void cannelZergMarch(March march) {
		long warId = march.getWarId();
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		if (mapInfo == null) {
			return;
		}
		IWar war = mapInfo.getWar(warId);
		if (war != null) {
			war.getAttacker().removeMarch(march);
		}
	}

	public void cannelZergHelpMarch(March march) {
		long warId = march.getWarId();
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		if (mapInfo == null) {
			return;
		}

		IWar war = mapInfo.getWar(warId);
		if (war != null) {
			war.getDefencer().removeMarch(march);
		}
	}


	public int isBeaOpenActivity() {
		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData == null) {
			return 0;
		}
		ZergData zergData = worldData.getZergData();
		if (zergData == null) {
			return 0;
		}
		return zergData.getStartTime() > 0 ? 1 : 0;
	}
}
