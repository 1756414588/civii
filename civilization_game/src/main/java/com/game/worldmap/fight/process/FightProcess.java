package com.game.worldmap.fight.process;

import com.game.constant.*;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.Hero;
import com.game.domain.p.Team;
import com.game.domain.p.WorldActPlan;
import com.game.domain.p.WorldMap;
import com.game.flame.FlameWarService;
import com.game.manager.ActivityManager;
import com.game.manager.BattleMailManager;
import com.game.manager.BattleMgr;
import com.game.manager.CityManager;
import com.game.manager.CountryManager;
import com.game.manager.DailyTaskManager;
import com.game.manager.HeroManager;
import com.game.manager.MarchManager;
import com.game.manager.PlayerManager;
import com.game.manager.SoldierManager;
import com.game.manager.TaskManager;
import com.game.manager.TechManager;
import com.game.manager.WarBookManager;
import com.game.manager.WarManager;
import com.game.manager.WorldBoxManager;
import com.game.manager.WorldManager;
import com.game.pb.WorldPb;
import com.game.service.AchievementService;
import com.game.service.ActivityService;
import com.game.util.LogHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.WarInfo;
import com.game.worldmap.WorldLogic;
import com.game.worldmap.fight.IFightProcess;
import com.game.worldmap.fight.IWar;
import com.google.common.collect.HashBasedTable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 战斗模块:处理行军和战斗
 */
public abstract class FightProcess implements IFightProcess {

	// 包含的战斗类型
	protected int[] warTypes;
	// 行军类型处理
	protected int[] marches;
	// 战斗处理器 warType,处理方法
	protected Map<Integer, BiConsumer<MapInfo, IWar>> fighterProcesses = new HashMap<>();
	// 行军处理器 marchType,state,处理方法
	protected HashBasedTable<Integer, Integer, BiConsumer<MapInfo, March>> marchProcess = HashBasedTable.create();
	// 世界活动处理
	protected HashBasedTable<Integer, Integer, Predicate<WorldActPlan>> worldActPlans = HashBasedTable.create();

	@Autowired
	protected WarManager warManager;
	@Autowired
	protected WorldManager worldManager;
	@Autowired
	protected CityManager cityManager;
	@Autowired
	protected PlayerManager playerManager;
	@Autowired
	protected BattleMgr battleMgr;
	@Autowired
	protected TechManager techManager;
	@Autowired
	protected BattleMailManager battleMailManager;
	@Autowired
	protected ActivityManager activityManager;
	@Autowired
	protected HeroManager heroManager;
	@Autowired
	protected WarBookManager warBookManager;
	@Autowired
	protected StaticWorldMgr staticWorldMgr;
	@Autowired
	protected WorldBoxManager worldBoxManager;
	@Autowired
	protected DailyTaskManager dailyTaskManager;
	@Autowired
	protected CountryManager countryManager;
	@Autowired
	protected ActivityService activityService;
	@Autowired
	protected WorldLogic worldLogic;
	@Autowired
	protected MarchManager marchManager;
	@Autowired
	protected SoldierManager soldierManager;
	@Autowired
	protected TaskManager taskManager;
	@Autowired
	FlameWarService flameWarService;
	@Autowired
	AchievementService achievementService;

	@Override
	public void process(MapInfo mapInfo, IWar war) {
		if (fighterProcesses.containsKey(war.getState())) {
			fighterProcesses.get(war.getState()).accept(mapInfo, war);
		}
	}

	@Override
	public void doMarch(MapInfo mapInfo, March march) {
		if (marchProcess.contains(march.getMarchType(), march.getState())) {

			marchProcess.get(march.getMarchType(), march.getState()).accept(mapInfo, march);
		}
	}

	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
	}

	@Override
	public void doWorldActPlan(WorldActPlan worldActPlan) {
	}

	public void registerMarch(int marchType, int state, BiConsumer<MapInfo, March> consumer) {
		marchProcess.put(marchType, state, consumer);
	}

	public void registerProcess(int state, BiConsumer<MapInfo, IWar> process) {
		fighterProcesses.put(state, process);
	}


	/**
	 * 行军完成
	 *
	 * @param mapInfo
	 * @param march
	 */
	public void doFinishedMarch(MapInfo mapInfo, March march) {
		long lordId = march.getLordId();
		// 找到玩家
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return;
		}

		march.setState(MarchState.Done);
		worldManager.synMarch(mapInfo.getMapId(), march);
		playerManager.handlerMarch(player);

		soldierManager.autoAdd(player, march.getHeroIds());
		playerManager.synSoldierChange(player, Reason.MARCH_RETURN);
		marchManager.backKey(march.getKeyId());

		// 返程任务
		taskManager.doTask(TaskType.ARMMY_RETURN, player, null);
	}

	/**
	 * 行军检测(处理出现行军不返回情况)
	 *
	 * @param mapInfo
	 * @param march
	 */
	@Override
	public void checkMarch(MapInfo mapInfo, March march) {
	}

	// 远征或者奔袭扣除血量
	public void handlePvpWarHp(WarInfo warInfo, Team attacker, Team defencer, HashBasedTable<Long, Integer, Integer> allSoldierRec, int warType) {
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		handleWarChange(attackerMarches, attacker, allSoldierRec, warType);
		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		handleWarChange(defencerMarches, defencer, allSoldierRec, warType);
	}

	// 虫族攻打玩家
	public void handlePvpWarHp(WarInfo warInfo, Team defencer, HashBasedTable<Long, Integer, Integer> allSoldierRec, int warType) {
		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		handleWarChange(defencerMarches, defencer, allSoldierRec, warType);
	}


	// 扣除血量, 以及增加荣誉值
	public void handleWarChange(ConcurrentLinkedDeque<March> marches, Team team, HashBasedTable<Long, Integer, Integer> allSoldierRec, int warType) {
		for (March march : marches) {
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

				BattleEntity battleEntity = team.getEntity(heroId, BattleEntityType.FRIEND_HERO, player.roleId);
				if (battleEntity == null) {
					battleEntity = team.getEntity(heroId, BattleEntityType.HERO, player.roleId);
					if (battleEntity == null) {
						continue;
					}
				}

				warManager.handlePlayerSoldierRec(player.roleId, hero, battleEntity, allSoldierRec);
				hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
				double techAdd = techManager.getHonorAdd(battleEntity.getLordId());
				int lastHonor = battleMailManager.getHonor(battleEntity, warType);
				int honor = (int) ((double) lastHonor * (1.0f + techAdd));
				playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, honor, Reason.ATTACK_CITY);
				soilder += battleEntity.getMaxSoldierNum() - battleEntity.getCurSoldierNum();
				if(warType==WarType.ATTACK_COUNTRY || warType==WarType.ATTACK_FAR){
					achievementService.addAndUpdate(player,AchiType.AT_31,battleEntity.getKillNum());
				}
			}

			if (soilder > 0) {
				activityManager.updActPerson(player, ActivityConst.ACT_SOILDER_RANK, soilder, 0);
				achievementService.addAndUpdate(player,AchiType.AT_25,soilder);
			}
		}
	}

	public void handleCountryReturn(March march, int reason) {
		long lordId = march.getLordId();
		Player player = playerManager.getPlayer(lordId);
		march.setState(MarchState.FightOver);
		march.swapPos(reason);

		//兵书对行军的影响值
		List<Integer> heroIds = march.getHeroIds();
		float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);
		long period = worldManager.getPeriod(player, march.getEndPos(), march.getStartPos(), bookEffectMarch);

		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
	}

	public boolean isFamousAndFortress(int type) {
		return type == CityType.FAMOUS_CITY || type == CityType.SQUARE_FORTRESS;
	}

	// 防守方胜利, 防守方回城，攻击方也回城
	public void handleCountryWarMarch(WarInfo warInfo, MapInfo mapInfo, int reason) {
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		for (March march : attackerMarches) {
			handleCountryReturn(march, reason);
			worldManager.synMarch(mapInfo.getMapId(), march);
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player != null) {
				// 处理自动补兵
				//soldierManager.autoAdd(player, march.getHeroIds());
				playerManager.synChange(player, Reason.COUNTRY_WAR);
			}

		}

		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		for (March march : defencerMarches) {
			handleCountryReturn(march, reason);
			worldManager.synMarch(mapInfo.getMapId(), march);
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player != null) {
				// 处理自动补兵
				//soldierManager.autoAdd(player, march.getHeroIds());
				playerManager.synChange(player, Reason.COUNTRY_WAR);
			}
		}
	}

	public void synAllPlayerChange(WarInfo warInfo) {
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		HashSet<Long> players = new HashSet<Long>();
		for (March march : attackerMarches) {
			players.add(march.getLordId());
		}

		for (March march : defencerMarches) {
			players.add(march.getLordId());
		}

		if (warInfo.getWarType() == WarType.ATTACK_QUICK || warInfo.getWarType() == WarType.ATTACK_FAR
			|| warInfo.getWarType() == WarType.Attack_WARFARE) {
			players.add(warInfo.getAttackerId());
		}

		for (Long lordId : players) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
			playerManager.synChange(player, Reason.ATTACK_CITY);
		}
	}

	public void handleWarAttener(WarInfo warInfo, int cityId) {
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		for (March march : attackerMarches) {
			if (march != null) {
				cityManager.addWarAttender(cityId, march.getLordId());
			}

		}
	}

	// 防守方胜利, 防守方回城，攻击方也回城
	public void handleWarMarch(WarInfo warInfo, MapInfo mapInfo, int reason) {
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		for (March march : attackerMarches) {
			marchManager.handleMarchReturn(march, reason);
			worldManager.synMarch(mapInfo.getMapId(), march);
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player != null) {
				// 处理自动补兵
				//soldierManager.autoAdd(player, march.getHeroIds());
				playerManager.synChange(player, Reason.ATTACK_CITY);
			}
		}

		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		for (March march : defencerMarches) {
			marchManager.handleMarchReturn(march, reason);
			worldManager.synMarch(mapInfo.getMapId(), march);
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player != null) {
				// 处理自动补兵
				//soldierManager.autoAdd(player, march.getHeroIds());
				playerManager.synChange(player, Reason.ATTACK_CITY);
			}
		}
	}
}
