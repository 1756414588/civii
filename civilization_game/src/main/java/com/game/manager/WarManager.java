package com.game.manager;

import com.game.constant.*;
import com.game.dataMgr.StaticSuperResMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.StaticSuperRes;
import com.game.flame.FlameWarManager;
import com.game.pb.DataPb;
import com.game.pb.DataPb.MarchData;
import com.game.pb.WorldPb;
import com.game.server.exec.LoginExecutor;
import com.game.service.RebelService;
import com.game.spring.SpringUtil;
import com.game.worldmap.*;
import com.game.worldmap.fight.Fighter;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.BigMonsterWarInfo;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.game.worldmap.fight.war.ZergWarInfo;
import com.game.worldmap.fight.zerg.PlayerAttack;
import com.game.worldmap.fight.zerg.ZergFighter;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

// 战争管理器
// 奔袭战、远征战、国战、攻城战,伏击叛军
@Component
public class WarManager {

	@Autowired
	private WorldManager worldManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private CityManager cityManager;
	@Autowired
	private HeroManager heroManager;
	@Autowired
	private WarBookManager warBookManager;
	@Autowired
	private BigMonsterManager bigMonsterManager;
	@Autowired
	private StaticSuperResMgr staticSuperResMgr;
	@Autowired
	private FlameWarManager flameWarManager;
	@Autowired
	private ZergManager zergManager;


	/**
	 * 创建奔袭战
	 */
	public WarInfo createFarWar(long period, Player attacker, Player defender, Pos attackerPos, Pos defenderPos, int warType, long warId) {
		WarInfo warInfo = new WarInfo();
		warInfo.setWarId(warId);
		warInfo.setEndTime(System.currentTimeMillis() + period);
		warInfo.setWarType(warType);
		warInfo.setState(1);

		warInfo.attacker = new Fighter(attacker.roleId, 0, attacker.getCountry(), attackerPos);
		warInfo.defender = new Fighter(defender.roleId, 0, defender.getCountry(), defenderPos);
		worldManager.flushWar(warInfo, true, attacker.getCountry());
		return warInfo;
	}

	public WarInfo createFarWar(DataPb.WarData warData, MapInfo mapInfo) {
		WarInfo warInfo = new WarInfo();
		warInfo.setWarId(warData.getWarId());
		warInfo.setEndTime(warData.getEndTime());
		warInfo.setWarType(warData.getWarType());
		warInfo.setState(warData.getState());
		warInfo.setCityWarType(warData.getCityWarType());

		warInfo.attacker = new Fighter(warData.getAttackerId(), warData.getAttackerType(), warData.getAttackerCountry(), warData.getHelpTime(), new Pos(warData.getAttackerPos()));
		warInfo.defender = new Fighter(warData.getDefencerId(), warData.getDefencerType(), warData.getDefencerCountry(), warData.getDefencerHelpTime(), new Pos(warData.getDefencerPos()));

		// 怪物
		for (DataPb.SquareMonsterData monsterData : warData.getMonsterDataList()) {
			SquareMonster squareMonster = new SquareMonster();
			squareMonster.read(monsterData);
			warInfo.updateMonster(monsterData.getMonsterId(), squareMonster);
		}

		// 行军
		List<MarchData> attackerList = warData.getAttackerList();
		if (attackerList != null && !attackerList.isEmpty() && mapInfo != null) {
			attackerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.attacker.addMarch(march);
				}
			});
		}

		List<DataPb.MarchData> defencerList = warData.getDefencerList();
		if (defencerList != null && !defencerList.isEmpty() && mapInfo != null) {
			defencerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.getDefencer().addMarch(march);
				}
			});
		}

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		warData.getCompanionMap().getLordIdList().forEach(e -> {
			Player player = playerManager.getPlayer(e);
			if (player != null) {
				warInfo.getCompanionMap().put(e, player);
			}
		});

		return warInfo;
	}

	public CountryCityWarInfo createCountryWar(long period, long attackerId, int country, int cityId, Pos attackerPos,
		Pos defenderPos, int warType) {
		int mapId = worldManager.getMapId(defenderPos.getX(), defenderPos.getY());
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			return null;
		}
		long warId = mapInfo.maxKey();
		CountryCityWarInfo warInfo = (CountryCityWarInfo) mapInfo.getWarMap().get(warId);
		if (warInfo != null) {
			return warInfo;
		}

		warInfo = new CountryCityWarInfo();
		warInfo.setWarId(warId);
		warInfo.setEndTime(System.currentTimeMillis() + period);
		warInfo.setWarType(warType);
		warInfo.setState(WarState.Waiting);
		warInfo.setCityId(cityId);

		warInfo.attacker = new Fighter(attackerId, 0, country, attackerPos);

		int defCountry = 0;
		// 找城池Id
		City city = cityManager.getCity(cityId);
		if (city != null) {
			defCountry = city.getCountry();
		}
		warInfo.defender = new Fighter(cityId, 1, defCountry, defenderPos);

		//进攻必须有国家才加入
		if (country != 0 || warInfo.getDefencerCountry() != 0) {
			worldManager.flushWar(warInfo, true, country);
		}

		// 插入数据库
		return warInfo;
	}

	public CountryCityWarInfo createCountryWar(DataPb.WarData warData, MapInfo mapInfo) {

		CountryCityWarInfo warInfo = new CountryCityWarInfo();
		warInfo.setWarId(warData.getWarId());
		warInfo.setEndTime(warData.getEndTime());
		warInfo.setWarType(warData.getWarType());
		warInfo.setState(warData.getState());
		warInfo.setCityWarType(warData.getCityWarType());

		warInfo.attacker = new Fighter(warData.getAttackerId(), warData.getAttackerType(), warData.getAttackerCountry(), warData.getHelpTime(), new Pos(warData.getAttackerPos()));
		warInfo.defender = new Fighter(warData.getDefencerId(), warData.getDefencerType(), warData.getDefencerCountry(), warData.getDefencerHelpTime(), new Pos(warData.getDefencerPos()));

		warInfo.setCityId((int) warData.getDefencerId());
		warInfo.setCityName("");

		// 怪物
		for (DataPb.SquareMonsterData monsterData : warData.getMonsterDataList()) {
			SquareMonster squareMonster = new SquareMonster();
			squareMonster.read(monsterData);
			warInfo.updateMonster(monsterData.getMonsterId(), squareMonster);
		}

		// 行军
		List<MarchData> attackerList = warData.getAttackerList();
		if (attackerList != null && !attackerList.isEmpty() && mapInfo != null) {
			attackerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.attacker.addMarch(march);
				}
			});
		}

		List<DataPb.MarchData> defencerList = warData.getDefencerList();
		if (defencerList != null && !defencerList.isEmpty() && mapInfo != null) {
			defencerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.getDefencer().addMarch(march);
				}
			});
		}

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		warData.getCompanionMap().getLordIdList().forEach(e -> {
			Player player = playerManager.getPlayer(e);
			if (player != null) {
				warInfo.getCompanionMap().put(e, player);
			}
		});
		// 插入数据库
		return warInfo;
	}

	/**
	 * 怪物攻击玩家城池
	 */
	public CountryCityWarInfo createMonsterActCountryWar(long period, long monsterId, int country, City city, Pos attackerPos,
		Pos defenderPos, int warType) {
		int mapId = worldManager.getMapId(defenderPos.getX(), defenderPos.getY());
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			return null;
		}
		long warId = mapInfo.maxKey();
		CountryCityWarInfo warInfo = (CountryCityWarInfo) mapInfo.getWarMap().get(warId);
		if (warInfo != null) {
			return warInfo;
		}

		warInfo = new CountryCityWarInfo();
		warInfo.setWarId(warId);
		warInfo.setEndTime(System.currentTimeMillis() + period);
		warInfo.setWarType(warType);
		warInfo.setState(WarState.Waiting);
		warInfo.setCityId(city.getCityId());
		warInfo.setCityName(city.getCityName());

		warInfo.attacker = new Fighter(monsterId, 1, country, attackerPos);
		warInfo.defender = new Fighter(city.getCityId(), 1, city.getCountry(), defenderPos);

		//进攻必须有国家才加入
		if (country != 0 || warInfo.getDefencerCountry() != 0) {
			worldManager.flushWar(warInfo, true, country);
		}

		// 插入数据库
		return warInfo;
	}

	public CountryCityWarInfo createMonsterActCountryWar(DataPb.WarData warData, MapInfo mapInfo) {
		CountryCityWarInfo warInfo = new CountryCityWarInfo();
		warInfo.setWarId(warData.getWarId());
		warInfo.setEndTime(warData.getEndTime());
		warInfo.setWarType(warData.getWarType());
		warInfo.setState(warData.getState());
		warInfo.setCityWarType(warData.getCityWarType());

		warInfo.attacker = new Fighter(warData.getAttackerId(), warData.getAttackerType(), warData.getAttackerCountry(), warData.getHelpTime(), new Pos(warData.getAttackerPos()));
		warInfo.defender = new Fighter(warData.getDefencerId(), warData.getDefencerType(), warData.getDefencerCountry(), warData.getDefencerHelpTime(), new Pos(warData.getDefencerPos()));

		warInfo.setCityId((int) warData.getDefencerId());
		warInfo.setCityName("");

		// 怪物
		for (DataPb.SquareMonsterData monsterData : warData.getMonsterDataList()) {
			SquareMonster squareMonster = new SquareMonster();
			squareMonster.read(monsterData);
			warInfo.updateMonster(monsterData.getMonsterId(), squareMonster);
		}

		// 行军
		List<MarchData> attackerList = warData.getAttackerList();
		if (attackerList != null && !attackerList.isEmpty() && mapInfo != null) {
			attackerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.attacker.addMarch(march);
				}
			});
		}

		List<DataPb.MarchData> defencerList = warData.getDefencerList();
		if (defencerList != null && !defencerList.isEmpty() && mapInfo != null) {
			defencerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.getDefencer().addMarch(march);
				}
			});
		}

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		warData.getCompanionMap().getLordIdList().forEach(e -> {
			Player player = playerManager.getPlayer(e);
			if (player != null) {
				warInfo.getCompanionMap().put(e, player);
			}
		});

		return warInfo;
	}

	// addCityAttender
	public void addCityAttender(March march, long now) {
		Pos pos = march.getEndPos();
		int mapId = worldManager.getMapId(pos.getX(), pos.getY());
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			handleLostTarget(march, null, LostTargetReason.MAP_NULL);
			return;
		}

		IWar war = mapInfo.getWarMap().get(march.getWarId());
		if (war == null) {
			handleLostTarget(march, null, LostTargetReason.CITY_WARS_NULL);
			return;
		}

		/**
		 * 战斗已取消
		 */
		if (war.getEndTime() <= now) {
			worldManager.handleMiddleReturn(march, MarchReason.LostTarget);
			worldManager.synMarch(mapInfo.getMapId(), march);
			return;
		}

		march.setState(MarchState.Waiting); // 远征进入等待操作结束
		march.setWarId(war.getWarId());
		// 设置战斗时间
		march.setEndTime(war.getEndTime() - 1000L);

	}

	public void sendLostTarget(March march, int reason) {
		long lordId = march.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return;
		}

		playerManager.sendNormalMail(player, MailId.LOST_TARGET);
	}

	public void handleLostTarget(March march, MapInfo mapInfo, int reason) {
		sendLostTarget(march, reason);
		worldManager.handleMiddleReturn(march, MarchReason.LostTarget);
		if (mapInfo != null) {
			worldManager.synMarch(mapInfo.getMapId(), march);
		}
	}


	/**
	 * 伏击叛军回城1s
	 *
	 * @param march
	 * @param reason
	 */
	public void handleRebelMarchReturn(March march, int reason) {
		// 回城
		march.setState(MarchState.FightOver);
		// 开始掉头
		march.swapPos(reason);
		int period = (int) Math.ceil(worldManager.distance(march.getStartPos(), march.getEndPos()) * 1.0f * 0.5) * 1000;
		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
		// march.setEndTime(System.currentTimeMillis() + period + 1000);
	}

	public void synWarInfo(IWar war) {
		SpringUtil.getBean(LoginExecutor.class).add(() -> {
			WorldPb.SynCountryWarRq joinWarPB = WorldPb.SynCountryWarRq.newBuilder().setWarInfo(war.wrapPb(true).build()).build();
			WorldPb.SynCountryWarRq unjoinWarPB = WorldPb.SynCountryWarRq.newBuilder().setWarInfo(war.wrapPb(false).build()).build();

			playerManager.getOnlinePlayer().forEach(target -> {
				if (!target.isLogin) {
					return;
				}
				boolean isJoin = war.isJoin(target);
				playerManager.synWarInfoToPlayer(target, isJoin ? joinWarPB : unjoinWarPB);
			});
		});
	}

	public void synRebelWarInfo(IWar war) {
		SpringUtil.getBean(LoginExecutor.class).add(() -> {
			WorldPb.SynRebelWarRq joinWarPB = WorldPb.SynRebelWarRq.newBuilder().setWarInfo(war.wrapPb(true).build()).build();
			WorldPb.SynRebelWarRq unjoinWarPB = WorldPb.SynRebelWarRq.newBuilder().setWarInfo(war.wrapPb(false).build()).build();
			playerManager.getOnlinePlayer().forEach(target -> {
				if (!target.isLogin) {
					return;
				}
				boolean isJoin = war.isJoin(target);
				playerManager.synReWarInfoToPlayer(target, isJoin ? joinWarPB : unjoinWarPB);
			});
		});
	}

	public void synRebelWarInfoToWorld(IWar war) {
		SpringUtil.getBean(LoginExecutor.class).add(() -> {
			WorldPb.SynRebelWarRq joinWarPB = WorldPb.SynRebelWarRq.newBuilder().setWarInfo(war.wrapPb(true).build()).build();
			WorldPb.SynRebelWarRq unjoinWarPB = WorldPb.SynRebelWarRq.newBuilder().setWarInfo(war.wrapPb(false).build()).build();
			playerManager.getOnlinePlayer().forEach(player -> {
				if (!player.isLogin) {
					return;
				}
				boolean isJoin = war.isJoin(player);
				playerManager.synReWarInfoToPlayer(player, isJoin ? joinWarPB : unjoinWarPB);
			});
		});
	}

	public void synWarInfoToWorld(IWar war) {
		SpringUtil.getBean(LoginExecutor.class).add(() -> {
			WorldPb.SynCountryWarRq joinWarPB = WorldPb.SynCountryWarRq.newBuilder().setWarInfo(war.wrapPb(true).build()).build();
			WorldPb.SynCountryWarRq unjoinWarPB = WorldPb.SynCountryWarRq.newBuilder().setWarInfo(war.wrapPb(false).build()).build();
			playerManager.getOnlinePlayer().forEach(player -> {
				if (!player.isLogin) {
					return;
				}
				boolean isJoin = war.isJoin(player);
				playerManager.synWarInfoToPlayer(player, isJoin ? joinWarPB : unjoinWarPB);
			});
		});
	}


	public void synWarInfo(IWar war, int attackerCountry, int defenceCountry) {
		SpringUtil.getBean(LoginExecutor.class).add(() -> {
			WorldPb.SynCountryWarRq joinWarPB = WorldPb.SynCountryWarRq.newBuilder().setWarInfo(war.wrapPb(true).build()).build();
			WorldPb.SynCountryWarRq unjoinWarPB = WorldPb.SynCountryWarRq.newBuilder().setWarInfo(war.wrapPb(false).build()).build();
			playerManager.getOnlinePlayer().forEach(player -> {
				if (!player.isLogin) {
					return;
				}
				if (player.getCountry() == attackerCountry || player.getCountry() == defenceCountry) {
					boolean isJoin = war.isJoin(player);
					playerManager.synWarInfoToPlayer(player, isJoin ? joinWarPB : unjoinWarPB);
				}
			});
		});
	}

	public void handePvpWarRemove(WarInfo warInfo) {
		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		ConcurrentLinkedDeque<March> defencerMarches = warInfo.getDefenceMarches();
		HashSet<Long> players = new HashSet<Long>();
		for (March march : attackerMarches) {
			players.add(march.getLordId());
		}

		for (March march : defencerMarches) {
			players.add(march.getLordId());
		}

		players.add(warInfo.getAttackerId());
		players.add(warInfo.getDefencerId());

		WorldPb.SynCityWarRq synCityWarRq = worldManager.createSynCityWar(warInfo);
		for (Long lordId : players) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null || !player.isLogin || player.getChannelId() == -1) {
				continue;
			}
			worldManager.synRemoveWar(player, synCityWarRq);
		}
	}

	// 扣除血量, 以及增加荣誉值
	public void handlRebeleWarChange(ConcurrentLinkedDeque<March> marches, Team team,
		HashBasedTable<Long, Integer, Integer> allSoldierRec, int warType) {
		for (March march : marches) {
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
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

				handlePlayerSoldierRec(player.roleId, hero, battleEntity, allSoldierRec);
				hero.setCurrentSoliderNum(battleEntity.getLastCurSoldierNum());
			}

		}
	}

	public void clearWarAttender(int cityId) {
		HashSet<Long> warAttenders = cityManager.getWarAttenders(cityId);
		if (warAttenders != null && !warAttenders.isEmpty()) {
			warAttenders.clear();
		}
	}

	public void handlePlayerSoldierRec(long lordId, Hero hero, BattleEntity battleEntity,
		HashBasedTable<Long, Integer, Integer> soldierRec) {
		//兵书技能加成效果
		Integer heroWarBookSkillEffect = warBookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.SOLDIER_REC);

		int currentSoldier = hero.getCurrentSoliderNum();
		int lastSoldier = battleEntity.getLastCurSoldierNum();
		int diff = currentSoldier - lastSoldier;
		if (diff > 0) {
			int soldierType = heroManager.getSoldierType(hero.getHeroId());
			Integer soldierNum = soldierRec.get(lordId, soldierType);
			if (soldierNum == null) {
				soldierRec.put(lordId, soldierType, diff);
			} else {
				soldierRec.put(lordId, soldierType, soldierNum + diff);
			}

			//兵书技能加成效果
			if (null != heroWarBookSkillEffect) {
				Player player = playerManager.getPlayer(lordId);
				if (null != player) {
					float soldierAdd = heroWarBookSkillEffect.intValue() / 1000.00f;
					int rec = (int) (soldierAdd * (float) diff);
					playerManager.addAward(player, AwardType.SOLDIER, battleEntity.getSoldierType(), rec, Reason.SOLDIER_REC);
				}
			}
		}
	}

	/**
	 * 虫族入侵
	 *
	 * @param player
	 * @param period
	 * @param attackerId
	 * @param country
	 * @param defencerId
	 * @param warType
	 * @return
	 */
	public WarInfo createRiotWar(Player player, long period, long attackerId, int country, long defencerId, int warType) {
		SimpleData simpleData = player.getSimpleData();
		Map<Long, WarInfo> riotWar = simpleData.getRiotWar();
		if (!riotWar.isEmpty()) {
			for (WarInfo warInfo : riotWar.values()) {
				return warInfo;
			}
		}

		long warId = player.maxKey();
		WarInfo warInfo = riotWar.get(warId);
		if (warInfo != null) {
			return warInfo;
		}

		warInfo = new WarInfo();
		warInfo.setWarId(warId);
		warInfo.setWarType(warType);
		warInfo.setCityWarType(warType);
		warInfo.setEndTime(System.currentTimeMillis() + period);
		warInfo.setState(WarState.Waiting);

		Pos pos = player.getPos();

		warInfo.attacker = new Fighter(attackerId, 0, country, pos);
		warInfo.defender = new Fighter(defencerId, 1, 0, pos);

		if (riotWar.isEmpty()) {
			riotWar.put(warId, warInfo);
		}

		return warInfo;
	}

	public WarInfo createRiotWar(DataPb.WarData warData) {
		WarInfo warInfo = new WarInfo();
		warInfo.setWarId(warData.getWarId());
		warInfo.setEndTime(warData.getEndTime());
		warInfo.setWarType(warData.getWarType());
		warInfo.setState(warData.getState());
		warInfo.setCityWarType(warData.getCityWarType());

		warInfo.attacker = new Fighter(warData.getAttackerId(), warData.getAttackerType(), warData.getAttackerCountry(), warData.getHelpTime(), new Pos(warData.getAttackerPos()));
		warInfo.defender = new Fighter(warData.getDefencerId(), warData.getDefencerType(), warData.getDefencerCountry(), warData.getDefencerHelpTime(), new Pos(warData.getDefencerPos()));

		// 怪物
		for (DataPb.SquareMonsterData monsterData : warData.getMonsterDataList()) {
			SquareMonster squareMonster = new SquareMonster();
			squareMonster.read(monsterData);
			warInfo.updateMonster(monsterData.getMonsterId(), squareMonster);
		}

		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		warData.getCompanionMap().getLordIdList().forEach(e -> {
			Player player = playerManager.getPlayer(e);
			if (player != null) {
				warInfo.getCompanionMap().put(e, player);
			}
		});
		return warInfo;
	}


	public WarInfo createRebelWar(long period, long attackerId, int country, long defencerId, Pos attackerPos, Pos defenderPos, MapInfo mapInfo) {
		long warId = mapInfo.maxKey();
		Map<Long, WarInfo> rebelWar = mapInfo.getRebelWarMap();
		WarInfo warInfo = rebelWar.get(warId);
		if (warInfo != null) {
			return warInfo;
		}
		warInfo = new WarInfo();
		warInfo.setWarId(warId);
		warInfo.setEndTime(System.currentTimeMillis() + period);
		warInfo.setState(WarState.Waiting);
		warInfo.setWarType(WarType.REBEL_WAR);

		warInfo.attacker = new Fighter(attackerId, 0, country, attackerPos);
		warInfo.defender = new Fighter(defencerId, 2, 0, defenderPos);

		worldManager.handleRebelWarSoldier(warInfo);
		worldManager.flushWar(warInfo, true, country);
		return warInfo;
	}

	/**
	 * 创建战斗（巨型虫族的）
	 *
	 * @param attackerId
	 * @param country
	 * @param defencerId
	 * @param attackerPos
	 * @param defenderPos
	 * @param warType
	 * @param mapInfo
	 * @return
	 */
	public BigMonsterWarInfo createBigMonsterWar(long attackerId, int country, long defencerId, Pos attackerPos, Pos defenderPos, int warType, MapInfo mapInfo) {
		long warId = mapInfo.maxKey();
		IWar war = mapInfo.getWarMap().values().stream().filter(e -> e.getWarType() == WarType.BIGMONSTER_WAR).filter(e -> {
			BigMonsterWarInfo warInfo = (BigMonsterWarInfo) e;
			return warInfo.getPos().equals(defenderPos) && warInfo.getCountry() == country;
		}).findFirst().orElse(null);
		if (war != null) {
			return (BigMonsterWarInfo) war;
		}

		BigMonster bigMonster = mapInfo.getBigMonsterMap().get(defenderPos);
		BigMonsterWarInfo warInfo = new BigMonsterWarInfo();
		warInfo.setWarId(warId);
		warInfo.setEndTime(bigMonster.getLeaveTime());
		warInfo.setWarType(warType);
		warInfo.setState(WarState.Waiting);

		warInfo.attacker = new Fighter(attackerId, 0, country, attackerPos);
		warInfo.defender = new Fighter(defencerId, 2, 0, defenderPos);

		//
		warInfo.setCountry(country);
		warInfo.setPos(defenderPos);

		mapInfo.getWarMap().put(warId, warInfo);
		worldManager.flushWar(warInfo, true, country);
		return warInfo;
	}

	public BigMonsterWarInfo createBigMonsterWar(DataPb.WarData warData, MapInfo mapInfo) {
		BigMonsterWarInfo warInfo = new BigMonsterWarInfo();

		Pos defenderPos = new Pos(warData.getDefencerPos());

		warInfo.setWarId(warData.getWarId());
		warInfo.setEndTime(warData.getEndTime());
		warInfo.setWarType(warData.getWarType());
		warInfo.setCityWarType(warData.getCityWarType());
		warInfo.setState(warData.getState());
		warInfo.setPos(defenderPos);

		warInfo.setCountry(warData.getAttackerCountry());

		warInfo.attacker = new Fighter(warData.getAttackerId(), warData.getAttackerType(), warData.getAttackerCountry(), new Pos(warData.getAttackerPos()));
		warInfo.defender = new Fighter(warData.getDefencerId(), warData.getDefencerType(), warData.getDefencerCountry(), defenderPos);

		warInfo.setAttackerHelpTime(warData.getHelpTime());
		warInfo.setDefencerHelpTime(warData.getDefencerHelpTime());

		// 进攻的行军
		List<DataPb.MarchData> attackerList = warData.getAttackerList();
		if (attackerList != null && !attackerList.isEmpty()) {
			attackerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.attacker.addMarch(march);
				}
			});
		}

		List<DataPb.MarchData> defencerList = warData.getDefencerList();
		if (defencerList != null && !defencerList.isEmpty()) {
			defencerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.defender.addMarch(march);
				}
			});
		}

		warData.getCompanionMap().getLordIdList().forEach(e -> {
			Player player = playerManager.getPlayer(e);
			if (player != null) {
				warInfo.getCompanionMap().put(e, player);
			}
		});
		mapInfo.getWarMap().put(warData.getWarId(), warInfo);
		if (warData.getAttackerCountry() != 0) {
			worldManager.flushWar(warInfo, true, warData.getAttackerCountry());
		}

		return warInfo;
	}

	/**
	 * 遣返所有伏击叛军的部队
	 *
	 * @param warInfo
	 */
	public void handleRebelMarchReturn(MapInfo mapInfo, WarInfo warInfo) {
		// 全区域广播
		for (March march : warInfo.getAttackMarches()) {
			//回城
			handleRebelMarchReturn(march, MarchReason.REBEL_MONSTER_MISS);
			//同步
			worldManager.synMarch(mapInfo.getMapId(), march);
			Player player = playerManager.getPlayer(march.getLordId());
			if (player == null) {
				continue;
			}
			playerManager.sendNormalMail(player, MailId.REBEL_MONSTER_MISS);
		}
		warInfo.setState(WarState.Finish);
	}


	/**
	 * 遣返所有怪物id 所对应的部队
	 *
	 * @param
	 * @param mapInfo
	 */
	public void cancelRebelWar(Monster rebelMonster, MapInfo mapInfo, long warId) {
		for (Map.Entry<Long, WarInfo> entry : mapInfo.getRebelWarMap().entrySet()) {
			WarInfo warInfo = entry.getValue();
			if (warInfo.getWarId() == warId) {
				continue;
			}
			if (warInfo.getDefencerId() == rebelMonster.getId() && warInfo.getDefencerPos().equals(rebelMonster.getPos())) {
				handleRebelMarchReturn(mapInfo, warInfo);
			}
		}
	}

	public void handleSuperTarget(March march, int mailId, SuperResource resource) {
		long lordId = march.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			return;
		}
		StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes(resource.getResId());
		playerManager.sendNormalMail(player, mailId, staticSuperRes.getName(), resource.getPosStr());
		worldManager.handleMiddleReturn(march, MarchReason.LostTarget);
	}


	public ZergWarInfo createZergAttackWar(int monsterId, long entTime, int cityId, Pos pos, int warType) {
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		City city = cityManager.getCity(cityId);
		long warId = mapInfo.maxKey();

		ZergWarInfo warInfo = new ZergWarInfo();
		warInfo.setWarId(warId);
		warInfo.setEndTime(entTime - 1500);
		warInfo.setMapId(MapId.CENTER_MAP_ID);
		warInfo.setState(WarState.Waiting);
		warInfo.setWarType(warType);
		warInfo.setCityWarType(warType);
		warInfo.setCityId(city.getCityId());

		warInfo.attacker = new Fighter(0, 0, 0, new Pos(0, 0));
		warInfo.defender = new ZergFighter(monsterId, pos, zergManager.getZergData().getTeam());

		mapInfo.addWar(warInfo);
		return warInfo;
	}

	public WarInfo createZergDefendWar(int monsterId, long endTime, PlayerAttack defencer, int warType) {
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);

		long warId = mapInfo.maxKey();

		ZergWarInfo warInfo = new ZergWarInfo();
		warInfo.setWarId(warId);
		warInfo.setMapId(MapId.CENTER_MAP_ID);
		warInfo.setEndTime(endTime - 1500);
		warInfo.setWarType(warType);
		warInfo.setCityWarType(warType);
		warInfo.setState(WarState.Waiting);

		Pos pos = defencer.getPos();

		Team team = zergManager.createTeamMonsterId(monsterId);
		warInfo.attacker = new ZergFighter(monsterId, pos, team);
		warInfo.defender = new Fighter(defencer.getPlayerCity().getLordId(), 0, defencer.getCountry(), pos);

		mapInfo.addWar(warInfo);
		return warInfo;
	}

	public WarInfo createZergWar(DataPb.WarData warData, MapInfo mapInfo) {

		ZergWarInfo warInfo = new ZergWarInfo();

		warInfo.setWarId(warData.getWarId());
		warInfo.setMapId(MapId.CENTER_MAP_ID);
		warInfo.setEndTime(warData.getEndTime());
		warInfo.setWarType(warData.getWarType());
		warInfo.setCityWarType(warData.getCityWarType());
		warInfo.setState(warData.getState());

		if (warData.getWarType() == WarType.ATTACK_ZERG) {
			warInfo.attacker = new Fighter(warData.getAttackerId(), warData.getAttackerType(), warData.getAttackerCountry(), warData.getHelpTime(), new Pos(warData.getAttackerPos()));
			warInfo.defender = new ZergFighter(warData.getDefencerId(), new Pos(warData.getDefencerPos()), zergManager.getZergData().getTeam());
		} else {
			Team team = zergManager.createTeamMonsterId((int) warData.getAttackerId());
			warInfo.attacker = new ZergFighter((int) warData.getAttackerId(), new Pos(warData.getAttackerPos()), team);
			warInfo.defender = new Fighter(warData.getDefencerId(), warData.getDefencerType(), warData.getDefencerCountry(), warData.getDefencerHelpTime(), new Pos(warData.getDefencerPos()));
		}

		warInfo.setAttackerHelpTime(warData.getHelpTime());
		warInfo.setDefencerHelpTime(warData.getDefencerHelpTime());

		List<DataPb.MarchData> attackerList = warData.getAttackerList();
		if (attackerList != null && !attackerList.isEmpty() && mapInfo != null) {
			attackerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.attacker.addMarch(march);
				}
			});
		}
		List<DataPb.MarchData> defencerList = warData.getDefencerList();
		if (defencerList != null && !defencerList.isEmpty() && mapInfo != null) {
			defencerList.forEach(x -> {
				March march = mapInfo.getMarch(x.getKeyId());
				if (march != null) {
					warInfo.defender.addMarch(march);
				}
			});
		}
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		warData.getCompanionMap().getLordIdList().forEach(e -> {
			Player player = playerManager.getPlayer(e);
			if (player != null) {
				warInfo.getCompanionMap().put(e, player);
			}
		});

		return warInfo;
	}

	@Autowired
	RebelService rebelService;

	public void checkWarWorldActPlan( ) {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_2);
		if (worldActPlan == null) {
			return ;
		}
		rebelService.refreshRebelActivity();
	}

}
