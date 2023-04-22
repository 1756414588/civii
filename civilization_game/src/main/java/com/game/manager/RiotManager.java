package com.game.manager;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.dataMgr.StaticRiotMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.log.consumer.EventManager;
import com.game.pb.RiotPb;
import com.game.pb.WorldPb;
import com.game.service.RoitService;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;


// 活动怪管理器
@Component
public class RiotManager {

	@Autowired
	private WorldManager worldManager;
	@Autowired
	private StaticRiotMgr staticRoitMgr;
	@Autowired
	private StaticWorldMgr staticWorldMgr;
	@Autowired
	private StaticMonsterMgr staticMonsterMgr;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private WarManager warManager;
	@Autowired
	private BattleMgr battleMgr;
	@Autowired
	private BattleMailManager battleMailMgr;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private RoitService roitService;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private MarchManager marchManager;
	@Autowired
	private TechManager techManager;


	// 通过areaType获取最多刷新的怪物数量
	// arateType: 地图类型, keyId: 配置Id
	// 返回怪物数量以及怪物Id
	public Map<Integer, Integer> getFirstMaxNum(int areaType, int keyId) {
		StaticRiotMonster riotMonster = staticRoitMgr.getRoitMonster(keyId);
		if (riotMonster == null) {
			LogHelper.CONFIG_LOGGER.info("roitMonster is null.");
			return new HashMap<Integer, Integer>();
		}

		// 怪物数量配置
		HashBasedTable<Integer, Integer, Integer> monster = riotMonster.getMonster();
		if (monster == null) {
			return new HashMap<Integer, Integer>();
		}

		// 怪物数量
		Map<Integer, Map<Integer, Integer>> rowMap = monster.rowMap();
		if (rowMap == null) {
			return new HashMap<Integer, Integer>();
		}

		return rowMap.get(areaType);
	}

	public void flushRiotByKeyId(int keyId) {
		Map<Integer, StaticWorldMap> worldMap = staticWorldMgr.getWorldMap();
		ConcurrentMap<Integer, MapInfo> worldMapInfo = worldManager.getWorldMapInfo();
		for (StaticWorldMap staticWorldMap : worldMap.values()) {
			int areaType = staticWorldMap.getAreaType();
			// key:monsterId, value:monsterNum
			Map<Integer, Integer> mm = getFirstMaxNum(areaType, keyId);
			if (mm == null) {
				LogHelper.CONFIG_LOGGER.info("monster map num is error!");
				continue;
			}

			int mapId = staticWorldMap.getMapId();
			MapInfo mapInfo = worldMapInfo.get(mapId);
			if (mapInfo == null) {
				continue;
			}

			Map<Pos, Monster> monsterMap = mapInfo.getMonsterMap();
			// 对每一种怪进行生成和判断
			for (Map.Entry<Integer, Integer> entrySet : mm.entrySet()) {
				if (entrySet == null) {
					continue;
				}

				int monsterId = entrySet.getKey();
				int maxNum = entrySet.getValue();

				int totalNum = 0;
				for (Monster monster : monsterMap.values()) {
					if (monster.getId() == monsterId) {
						totalNum++;
					}
				}

				// 如果超过最大值，就不要进行生成了
				if (totalNum >= maxNum) {
					continue;
				}

				// 获取当前世界怪物的等级
				StaticWorldMonster worldMonster = staticWorldMgr.getMonster(monsterId);
				if (worldMonster == null) {
					LogHelper.CONFIG_LOGGER.info("worldMonster is null, monsterId = " + monsterId);
					continue;
				}

				for (int num = 1; num <= maxNum; num++) {
					Pos monsterPos = mapInfo.randPickPos();
					if (monsterPos.isError() || !mapInfo.isFreePos(monsterPos)) {
						continue;
					}

					// 创建一个野怪
					worldManager.addRiotMonster(monsterPos,
						monsterId,
						worldMonster.getLevel(),
						mapInfo,
						AddMonsterReason.FLUSH_SPECIAL_MONSTER);
					totalNum++;
					if (totalNum >= maxNum) {
						break;
					}
				}
			}
		}
	}

	/**
	 * 刷新攻城怪物
	 *
	 * @param keyId
	 * @param staticWorldActPlan
	 */
	public void flushWaveMonster(int keyId, StaticWorldActPlan staticWorldActPlan) {
		StaticRoitWaveMonster waveMonster = staticRoitMgr.getWaveMonster(keyId);
		if (waveMonster == null) {
			LogHelper.CONFIG_LOGGER.info("wave monster is null, keyId = " + keyId);
			return;
		}
		// 应该有一个持续时间,持续时间结束就应该刷波次, 猜测是活动开始24小时
		Iterator<Player> iterator = playerManager.getPlayers().values().iterator();
		long now = System.currentTimeMillis();

		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}

			if (player.getCommandLv() < staticWorldActPlan.getContinues().get(3)) {
                continue;
            }

            if (!player.getFullLoad().get()) {
				continue;
			}

			int mapId = worldManager.getMapId(player);
			if (mapId == 0) {
				continue;
			}

			SimpleData simpleData = player.getSimpleData();
			if (simpleData == null) {
				continue;
			}

			if (simpleData.isWaveContinue()) {
				continue;
			}

			Map<Long, WarInfo> riotWar = simpleData.getRiotWar();
			if (riotWar == null) {
				LogHelper.CONFIG_LOGGER.info("riotWar is null");
				continue;
			}

			// 如果有战斗了, 就不要创建其他的战斗了
			if (!riotWar.isEmpty()) {
				continue;
			}

			riotAttackPlayer(staticWorldActPlan, keyId, now, player, simpleData);
		}
	}

	private void riotAttackPlayer(StaticWorldActPlan staticWorldActPlan, int readType, long now, Player player, SimpleData simpleData) {
		int monsterId = staticRoitMgr.getWaveMonster(readType, simpleData.getAttackWave());
		//最大波了
		if (monsterId == 0) {
			return;
		}
		initWaveInfo(simpleData, now);
		WarInfo warInfo = createRiotWar(player, monsterId, staticWorldActPlan.getContinues().get(2));
		if (warInfo == null) {
			LogHelper.CONFIG_LOGGER.info("riot war create failed");
			return;
		}
		//创建一个空的行军
		March march = worldManager.createRiotMarch(player, new ArrayList<>(), warInfo);

		// add march to player
		simpleData.setRiotMarchs(march);
		//只推送自己的
		worldManager.synAddCityWar(player, warInfo);
		worldManager.synMarchToPlayer(player, march);
	}

	// 当前攻打时间 = 20170803, 下一次攻打的时间 = 20170810, 结果为true, 当前打到的波次=0, 下一波刷新时间=now+5分钟
	public void initWaveInfo(SimpleData simpleData, long now) {
		simpleData.setWaveContinue(false);
		simpleData.setNextWaveTime(now + TimeHelper.MINUTE_MS * 5);
	}

	// 开始生成战斗函数
	// 攻击者是活动怪
	// 防守者是玩家
	// 这个战斗应该是绑在人身上, 而不是在地图身上
	public WarInfo createRiotWar(Player player, int monsterId, long periodSecond) {
		Lord lord = player.getLord();
		long period = TimeHelper.SECOND_MS * periodSecond;
		int mapId = worldManager.getMapId(player);
		if (mapId == 0) {
			return null;
		}
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.info("mapInfo is null.");
			return null;
		}

		WarInfo riotWar = warManager.createRiotWar(player, period, monsterId, player.getCountry(), lord.getLordId(), WarType.RIOT_WAR);
		StaticWorldMonster worldMonster = staticWorldMgr.getMonster(monsterId);
		if (worldMonster != null) {
			for (Integer monster : worldMonster.getMonsterIds()) {
				StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monster);
				SquareMonster squareMonster = new SquareMonster();
				squareMonster.setMonsterLv(staticMonster.getLevel());
				squareMonster.setMonsterId(staticMonster.getMonsterId());
				riotWar.updateMonster(monster, squareMonster);
			}
		}

		return riotWar;
	}

	public void synRiotBuff(Player player) {
		SimpleData simpleData = player.getSimpleData();
		if (simpleData == null) {
			return;
		}
		if (!player.isOk()) {
			return;
		}
		Map<Integer, Integer> riotBuff = simpleData.getRiotBuff();
		int attack = riotBuff.containsKey(RiotBuff.ATTACK) ? riotBuff.get(RiotBuff.ATTACK) : 0;
		int defence = riotBuff.containsKey(RiotBuff.DEFENCE) ? riotBuff.get(RiotBuff.DEFENCE) : 0;
		int lessTroops = riotBuff.containsKey(RiotBuff.LESSTROOPS) ? riotBuff.get(RiotBuff.LESSTROOPS) : 0;
		RiotPb.SynRoitBuff.Builder builder = RiotPb.SynRoitBuff.newBuilder();
		builder.setAttack(attack);
		builder.setDefence(defence);
		builder.setLessTroops(lessTroops);
		builder.setRiotItem(simpleData.getRiotItem());
		builder.setRiotScore(simpleData.getRiotScore());
		SynHelper.synMsgToPlayer(player, RiotPb.SynRoitBuff.EXT_FIELD_NUMBER, RiotPb.SynRoitBuff.ext, builder.build());
	}


	// 检查战斗
	public void checkRiotWar() {
		long now = System.currentTimeMillis();
		List<Player> players = new ArrayList<Player>(playerManager.getPlayers().values());
		Iterator<Player> iterator = players.iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (player == null) {
				continue;
			}

			int mapId = worldManager.getMapId(player);
			MapInfo mapInfo = worldManager.getMapInfo(mapId);
			if (mapInfo == null) {
				continue;
			}

			SimpleData simpleData = player.getSimpleData();
			WarInfo warInfo = simpleData.getRiotWarInfo();
			if (warInfo == null) {
				continue;
			}

			if (warInfo.getEndTime() > now) {
				continue;
			}

			if (warInfo.getState() == WarState.Waiting) {
				warInfo.setEndTime(warInfo.getEndTime() + 1000L);
				warInfo.setState(WarState.Fighting);
			} else if (warInfo.getState() == WarState.Fighting) {
				doRiotWar(warInfo);
				simpleData.getRiotWar().clear();
				warInfo.setState(WarState.Finish);
                iterator.remove();
			}
		}
	}


	/**
	 * 虫族入侵第二阶段战斗
	 *
	 * @param warInfo
	 */
	public void doRiotWar(WarInfo warInfo) {
        try {
		StaticWorldMonster worldMonster = staticWorldMgr.getMonster((int) warInfo.getAttackerId());
		if (worldMonster == null) {
			LogHelper.CONFIG_LOGGER.info("worldMonster is null.");
			return;
		}
		Player player = playerManager.getPlayer(warInfo.getDefencerId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("target is null!");
			return;
		}

		List<Integer> monsterIds = worldMonster.getMonsterIds();
		Team monsterTeam = battleMgr.initRiotMonsterTeam(monsterIds, BattleEntityType.ROIT_MONSTER, player);
		// 防守方: 援助 + 当前玩家的驻防武将 + 其他玩家驻防武将 + 城防军
		Team playerTeam = battleMgr.initDefencer(warInfo, BattleEntityType.HERO, false);
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(monsterTeam, playerTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		activityManager.calcuKillAll(warInfo, playerTeam, monsterTeam);

		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("attacker is null!");
			return;
		}

		if (!playerTeam.isWin()) {
			//不能继续了
			player.getSimpleData().setWaveContinue(true);
			//清除信息
			if (player.getSimpleData() != null) {
				player.getSimpleData().getRiotWar().clear();
			}
			WorldActPlan worldActPlan = roitService.getWorldRoitActPlan();
			eventManager.worldActRiot(player, 2,
				Lists.newArrayList(
					worldActPlan.getId(),
					player.getSimpleData().getAttackWave()
				));
		}
		//清除行军信息
		March march = player.getSimpleData().getRiotMarchs();
		if (march != null) {
			march.setState(MarchState.Done);
			worldManager.synMarchToPlayer(player, march);
			marchManager.backKey(march.getKeyId());
			player.getSimpleData().setRiotMarchs(null);
		}

		player.getSimpleData().setAttackWave(player.getSimpleData().getAttackWave() + 1);

		// 防守者经验值
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, playerTeam.getLordId());
		worldManager.caculateTeamDefenceKill(playerTeam);
		// 处理玩家扣血
		HashMap<Integer, Integer> defenceRec = new HashMap<Integer, Integer>();
		HashMap<Long, HashMap<Integer, Integer>> allDefenceRec = new HashMap<>();
		worldManager.handleDefenceSoldier(playerTeam, player, MarchReason.RIOT_WAR_ATTEND, allDefenceRec);
		defenceRec = allDefenceRec.get(player.getLord().getLordId());
		// 同步所有参战人员的属性变化, 处理士兵血量和威望
		HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
		handlePvpWarHp(warInfo, playerTeam, allSoldierRec, WarType.RIOT_WAR);
		// 根据结果不同发送不同的邮件给玩家
		worldManager.soldierAutoAdd(player);
		handePvpWarRemove(warInfo);  // 通知参战双方删除战斗
		battleMailMgr.riotMail(playerTeam, monsterTeam, player,
			worldMonster.getId(), new ArrayList<Award>(),
			heroAddExp, allSoldierRec);
		//战斗结束 移除兵力减少的buff
		player.getSimpleData().getRiotBuff().remove(RiotBuff.LESSTROOPS);
		synRiotBuff(player);
		//防守兵力返回
		warInfo.getDefenceMarches().forEach(march2 -> {
			Player target = playerManager.getPlayer(march2.getLordId());
			// 通知所有部队遣返
			marchManager.doWarMarchReturn(target, warInfo, Reason.RIOT_WAVE);
		});
		if (playerTeam.isWin()) {
			//更新通行证任务
			ActivityEventManager.getInst().activityTip(EventEnum.RIOT_WAR, player, 1, 0);
//            activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.MONSTER_INTRUSION, 1);
            }
        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}


	public void handePvpWarRemove(WarInfo warInfo) {
		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		HashSet<Long> players = new HashSet<Long>();
		for (March march : defencerMarches) {
			players.add(march.getLordId());
		}
		players.add(warInfo.getDefencerId());

		WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(warInfo);
		for (Long lordId : players) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null || !player.isOk()) {
				continue;
			}
			worldManager.synRemoveWar(player, synCityWarRq);
		}
	}


	// [0, 1000)
	// [500000, +~)
	public List<Award> queryKillNum(int killNum) {
		List<StaticRiotAward> riotAwardList = staticRoitMgr.getRiotAwardList();
		for (StaticRiotAward riotAward : riotAwardList) {
			if (killNum >= riotAward.getKillNum()) {

			}
		}
		List<Award> awards = new ArrayList<Award>();
		int configSize = riotAwardList.size();
		StaticRiotAward limitDown0 = riotAwardList.get(0);
		StaticRiotAward limitDown1 = riotAwardList.get(1);
		StaticRiotAward limitUp = riotAwardList.get(configSize - 1);
		if (killNum < limitDown1.getKillNum()) {
			awards.add(new Award(AwardType.RESOURCE, ResourceType.IRON, limitDown0.getIron()));
			awards.add(new Award(AwardType.RESOURCE, ResourceType.COPPER, limitDown0.getCopper()));
			return awards;
		} else if (killNum >= limitUp.getKillNum()) {
			awards.add(new Award(AwardType.RESOURCE, ResourceType.IRON, limitUp.getIron()));
			awards.add(new Award(AwardType.RESOURCE, ResourceType.COPPER, limitUp.getCopper()));
			return awards;
		}

		// 中间
		for (int i = 1; i < configSize; i++) {
			StaticRiotAward riotAward = riotAwardList.get(i);
			if (riotAward == null) {
				continue;
			}
			if (killNum < riotAward.getKillNum()) {
				StaticRiotAward riotAwardPre = riotAwardList.get(i - 1);
				if (riotAwardPre != null) {
					awards.add(new Award(AwardType.RESOURCE, ResourceType.IRON, riotAwardPre.getIron()));
					awards.add(new Award(AwardType.RESOURCE, ResourceType.COPPER, riotAwardPre.getCopper()));
					return awards;
				} else {
					LogHelper.CONFIG_LOGGER.info("config error index = " + i);
				}
				break;
			}
		}
		return awards;
	}


	public void handlePvpWarHp(WarInfo warInfo, Team defencer, HashBasedTable<Long, Integer, Integer> allSoldierRec, int warType) {
		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		for (March march : defencerMarches) {
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			int soilder = 0;

			for (Integer heroId : march.getHeroIds()) {
				Hero hero = player.getHero(heroId);
				if (hero == null) {
					continue;
				}

				BattleEntity battleEntity = defencer.getEntity(heroId, BattleEntityType.FRIEND_HERO, player.roleId);
				if (battleEntity == null) {
					battleEntity = defencer.getEntity(heroId, BattleEntityType.HERO, player.roleId);
					if (battleEntity == null) {
						continue;
					}
				}

				warManager.handlePlayerSoldierRec(player.roleId, hero, battleEntity, allSoldierRec);
				hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
				double techAdd = techManager.getHonorAdd(battleEntity.getLordId());
				int lastHonor = battleMailMgr.getHonor(battleEntity, warType);
				int honor = (int) ((double) lastHonor * (1.0f + techAdd));
				playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, honor,
					Reason.ATTACK_CITY);

				soilder += battleEntity.getMaxSoldierNum() - battleEntity.getCurSoldierNum();
			}

			if (soilder > 0) {
				activityManager.updActPerson(player, ActivityConst.ACT_SOILDER_RANK, soilder, 0);
			}
		}
	}


	/**
	 * 获取玩家总的杀敌数
	 *
	 * @param team
	 * @param player
	 * @return
	 */
	public int getKillNum(Team team, Player player) {
		ArrayList<BattleEntity> gameEntities = team.getAllEnities();
		int killNum = 0;
		for (BattleEntity battleEntity : gameEntities) {
			long lordId = battleEntity.getLordId();
			if (lordId != player.getLord().getLordId()) {
				continue;
			}
			killNum += battleEntity.getKillNum();
		}
		return killNum;
	}
}
