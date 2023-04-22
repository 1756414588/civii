package com.game.worldmap.fight.process;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticFirstBloodMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.City;
import com.game.domain.p.CityMonster;
import com.game.domain.p.CityMonsterInfo;
import com.game.domain.p.CtyDaily;
import com.game.domain.p.HeroAddExp;
import com.game.domain.p.SquareMonster;
import com.game.domain.p.Team;
import com.game.domain.p.WorldMap;
import com.game.domain.s.StaticFirstBloodAward;
import com.game.domain.s.StaticWorldCity;
import com.game.manager.ChatManager;
import com.game.manager.DailyTaskManager;
import com.game.manager.TaskManager;
import com.game.manager.WorldBoxManager;
import com.game.pb.DataPb;
import com.game.pb.SerializePb.SerCountryWar;
import com.game.service.AchievementService;
import com.game.service.SuperResService;
import com.game.service.WorldTargetTaskService;
import com.game.util.LogHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.*;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.google.common.collect.HashBasedTable;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Fight(warName = "国战", warType = {WarType.ATTACK_COUNTRY}, marthes = {MarchType.CountryWar})
@Component
public class CountryWarProcess extends FightProcess {

	@Autowired
	private StaticLimitMgr staticLimitMgr;
	@Autowired
	private StaticFirstBloodMgr staticFirstBloodMgr;
	@Autowired
	private WorldBoxManager worldBoxManager;
	@Autowired
	private SuperResService superResService;
	@Autowired
	private WorldTargetTaskService worldTargetTaskService;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private ChatManager chatManager;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	ActivityEventManager activityEventManager;

	@Override
	public void init(int[] warTypes, int[] marches) {
		this.warTypes = warTypes;
		this.marches = marches;

		// 注册战斗
		registerProcess(WarState.Waiting, this::waiting);
		registerProcess(WarState.Fighting, this::battle);
		registerProcess(WarState.Finish, this::warFinish);

		// 注册行军
		registerMarch(MarchType.CountryWar, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.CountryWar, MarchState.Waiting, this::marchFighting);
		registerMarch(MarchType.CountryWar, MarchState.Back, this::doFinishedMarch);

	}

	private void marchArrive(MapInfo mapInfo, March march) {
		long now = System.currentTimeMillis();
		march.setState(MarchState.Waiting);

		// 战斗已不存在
		CountryCityWarInfo countryWar = (CountryCityWarInfo) mapInfo.getWar(march.getWarId());
		if (countryWar == null) {
			marchManager.handleMarchReturn(march, MarchReason.CountryAttender);
			return;
		}

		// 战斗已结束
		if (countryWar.getEndTime() <= now) {
			marchManager.handleMarchReturn(march, MarchReason.CountryAttender);
			worldManager.synMarch(mapInfo.getMapId(), march);
			return;
		}

		march.setPeriod(countryWar.getEndTime() - now);
		march.setEndTime(System.currentTimeMillis() + march.getPeriod());
		march.setWarId(countryWar.getWarId());
		warManager.synWarInfo(countryWar);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	private void marchFighting(MapInfo mapInfo, March march) {
		march.setState(MarchState.Fighting);
		march.setPeriod(1000L);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	private void waiting(MapInfo mapInfo, IWar war) {
		if (war == null) {
			return;
		}
		// 未到开战时间
		if (war.getEndTime() > System.currentTimeMillis()) {
			return;
		}
		CountryCityWarInfo warInfo = (CountryCityWarInfo) war;
		warInfo.setEndTime(warInfo.getEndTime() + 1000L);
		warInfo.setState(WarState.Fighting);
	}

	public void battle(MapInfo mapInfo, IWar war) {
		if (war == null || war.getState() != WarState.Fighting) {
			return;
		}

		CountryCityWarInfo warInfo = (CountryCityWarInfo) war;
		doCountryWar(mapInfo, warInfo);

		warInfo.setState(WarState.Finish);
		warInfo.setEndTime(System.currentTimeMillis());

		warManager.synWarInfoToWorld(warInfo);
		// 删除战斗
		worldManager.flushWar(warInfo, false, warInfo.getAttackerCountry());
	}

	private void warFinish(MapInfo mapInfo, IWar war) {
		CountryCityWarInfo warInfo = (CountryCityWarInfo) war;
		warInfo.setEnd(true);
	}

	@Autowired
	AchievementService achievementService;

	public void doCountryWar(MapInfo mapInfo, WarInfo warInfo) {
		Team attacker = null;
		Map<Integer, SquareMonster> monsterMap = warInfo.getMonsters();
		if (!monsterMap.isEmpty()) {
			// 生成禁卫军, attackerId就是禁卫军Id
			List<Integer> monsters = new ArrayList<Integer>();
			for (SquareMonster monster : monsterMap.values()) {
				monsters.add(monster.getMonsterId());
			}
			attacker = battleMgr.initMonsterTeam(monsters, BattleEntityType.GUARD_MONSTER);
		}
		if (attacker == null) {
			attacker = battleMgr.initAttackerWarTeam(warInfo, true);
		} else {
			attacker.addTeam(battleMgr.initAttackerWarTeam(warInfo, true));
		}
		attacker.setCountry(warInfo.getAttackerCountry());
		// 守城军先上,在上守城的玩家
		int cityId = (int) warInfo.getDefencerId();
		Team cityTeam = battleMgr.initCityTeam(cityId);
		Team defencer = battleMgr.initDefenceWarTeam(warInfo, false);
		defencer.setCountry(warInfo.getDefencerCountry());
		cityTeam.addTeam(defencer);
		if (staticLimitMgr.isSimpleWarOpen()) {
			// cityTeam.clear();
		}
		Random rand = new Random(System.currentTimeMillis());
		// 开始国战
		battleMgr.doTeamBattle(attacker, cityTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		activityManager.calcuKillAll(warInfo, attacker, cityTeam);
		HeroAddExp heroAddExp = null;
		if (warInfo.getWarType() == WarType.ATTACK_COUNTRY) {
			heroAddExp = worldManager.caculateTeamKill(attacker, warInfo.getAttackerId());
		} else if (warInfo.getAttackerType() == 1) {
			heroAddExp = new HeroAddExp();
		}
		worldManager.caculateTeamDefenceKill(defencer);
		HashBasedTable<Long, Integer, Integer> allSoldierRec = HashBasedTable.create();
		// 同步国战后相关变化[血量和威望]
		handlePvpWarHp(warInfo, attacker, defencer, allSoldierRec, WarType.ATTACK_COUNTRY);
		long cityOldLordId = cityManager.getCityLordId(cityId);
		if (attacker.isWin()) { // 攻击方胜利
			// 国家日志
			countryWinDaily(cityId, mapInfo.getMapId(), warInfo);
			countryFailedDaily(cityId, mapInfo.getMapId(), warInfo);

			// 处理城池被攻破
			cityManager.handlerCityBreak(cityId, warInfo);

			// 这个一定要放到城池攻破的前面
			worldManager.attackCityTarget(cityId, attacker.getCountry());

			doWarTask(cityId, warInfo);
			warManager.clearWarAttender(cityId);
			handleWarAttener(warInfo, cityId);
			handleCityElection(cityId, warInfo);
			handleCityProtected(cityId);
			battleMailManager.sendCountryWarReport(warInfo, attacker, cityTeam, cityId, heroAddExp, allSoldierRec, cityOldLordId);
			// 国战后返回
			handleCountryWarMarch(warInfo, mapInfo, MarchReason.CtWarWin);

			handlerCountryChat(warInfo, cityId);
			// 国家任务
			doAttackCountryTask(warInfo);

			// 遣返所有人
			cancelCountryWarWhenOccupy(mapInfo, cityId, warInfo.getWarId());

			//国战排行榜  只有胜利进攻的一方加
			activityManager.updActCountryRank(warInfo);

			StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
			Player player = playerManager.getPlayer(warInfo.getAttackerId());
			if (player != null) {
				List<Player> list = new ArrayList<>();
				Iterator<March> it = warInfo.getAttackMarches().iterator();
				Set<Long> set = new HashSet<>();
				while (it.hasNext()) {
					March march = it.next();
					Player attackers = playerManager.getPlayer(march.getLordId());
					if (attackers == null || set.contains(attackers.getLord().getLordId())) {
						continue;
					}
					set.add(attackers.getLord().getLordId());
					list.add(attackers);
					if(staticWorldCity.getMapId() == MapId.CENTER_MAP_ID){
						achievementService.addAndUpdate(attackers,AchiType.AT_19,1);
					}
				}
				Map<Integer, StaticFirstBloodAward> awardMap = staticFirstBloodMgr.getStaticFirstBloodAwardMap();

                if (staticWorldCity != null && !mapInfo.getCityFirstBlood().containsKey(staticWorldCity.getType())) {
                    mapInfo.setFirstBlood(staticWorldCity.getType(), player, list, mapInfo.getMapId());
                    if (awardMap != null && awardMap.get(staticWorldCity.getType()) != null) {
                        List<List<Integer>> lists = awardMap.get(staticWorldCity.getType()).getAward();
                        ArrayList<Award> awards = new ArrayList<>();
                        for (List<Integer> i : lists) {
                            awards.add(new Award(i.get(0), i.get(1), i.get(2)));
                        }
                        playerManager.sendAttachMail(player, awards, MailId.ACT_FIRST_BLOOD_AWARD, String.valueOf(mapInfo.getMapId()), String.valueOf(staticWorldCity.getType()));
                        for (Player p : list) {
                            if (player.roleId.equals(p.roleId)) {
                                continue;
                            }
                            playerManager.sendAttachMail(p, awards, MailId.ACT_FIRST_BLOOD_AWARD, String.valueOf(mapInfo.getMapId()), String.valueOf(staticWorldCity.getType()));
                        }
                    }
                }

                if (staticWorldCity.getMapId() != MapId.CENTER_MAP_ID) {
                    City city = cityManager.getCity(cityId);
                    if (city != null && city.getFirstKill() == 0) {
                        if (awardMap != null && awardMap.get(staticWorldCity.getType()) != null) {
                            List<List<Integer>> lists = awardMap.get(staticWorldCity.getType()).getAward();
                            ArrayList<Award> awards = new ArrayList<>();
                            for (List<Integer> i : lists) {
                                awards.add(new Award(i.get(0), i.get(1), i.get(2)));
                            }
                            playerManager.sendAttachMail(player, awards, MailId.ACT_FIRST_BLOOD_AWARD, String.valueOf(mapInfo.getMapId()), String.valueOf(staticWorldCity.getType()));
                            for (Player p : list) {
                                if (player.roleId.equals(p.roleId)) {
                                    continue;
                                }
                                playerManager.sendAttachMail(p, awards, MailId.ACT_FIRST_BLOOD_AWARD, String.valueOf(mapInfo.getMapId()), String.valueOf(staticWorldCity.getType()));
                            }
                        }
                        city.setFirstKill(player.getCountry());
                    }
                }
                for (Player p : list) {
                    activityEventManager.activityTip(EventEnum.COUNTRY_WAR, p, 1, 1);
                    worldBoxManager.calcuPoints(WorldBoxTask.COUNTRY_FIGHT, p, 1);
                }
                cityManager.handlerActHeroTask(cityId, list);
                set.clear();
            }
            superResService.changeSuperState(cityId);
        } else {
            Player player = playerManager.getPlayer(warInfo.getAttackerId());
            if (player != null) {
                List<Player> list = new ArrayList<>();
                Iterator<March> it = warInfo.getAttackMarches().iterator();
                Set<Long> set = new HashSet<>();
                while (it.hasNext()) {
                    March march = it.next();
                    Player attackers = playerManager.getPlayer(march.getLordId());
                    if (attackers == null || set.contains(attackers.getLord().getLordId())) {
                        continue;
                    }
                    set.add(attackers.getLord().getLordId());
                    list.add(attackers);
                }
                for (Player p : list) {
                    activityEventManager.activityTip(EventEnum.COUNTRY_WAR, p, 1, 0);
                }
                set.clear();
            }

			// 发送国战邮件
			battleMailManager.sendCountryWarReport(warInfo, attacker, cityTeam, cityId, heroAddExp, allSoldierRec, cityOldLordId);
			// 国战后返回
			handleCountryWarMarch(warInfo, mapInfo, MarchReason.CtWarFailed);
		}

		synAllPlayerChange(warInfo);
		// 攻城掠地活动（防守者参与国战）
		activityManager.updActSceneCity(cityId, attacker.getAllEnities(), attacker.isWin());
		activityManager.updActSceneCity(cityId, defencer.getAllEnities(), false);

		//activityManager.updActCountryRank(defencer.getAllEnities());
		// 更新城池怪物血量
		handleCitySoldier(cityTeam, cityId);
		// 同步城池信息
		worldManager.synMapCity(mapInfo.getMapId(), cityId);
		// 国家排行榜
		handleCtWarRank(warInfo);
		//TODO jyb输赢都要计算世界目标损失兵力
		worldTargetTaskService.attackCity(attacker, defencer, cityId);
		//记录玩家攻城守城信息
		attacker.getAllEnities().forEach(e -> {
			Player p = playerManager.getPlayer(e.getLordId());
			if (p != null) {
				p.addAccackCityNum();
				dailyTaskManager.record(DailyTaskId.COUNTRY_WAR, p, 1);
			}
		});
		defencer.getAllEnities().forEach(e -> {
			Player p = playerManager.getPlayer(e.getLordId());
			if (p != null) {
				p.addAccackCityNum();
				dailyTaskManager.record(DailyTaskId.COUNTRY_WAR, p, 1);
			}
		});
	}


	// 让国战的所有人返程
	public void ctWarMarchReturn(IWar war, MapInfo mapInfo, int reason) {
		HashSet<Long> players = new HashSet<Long>();
		ConcurrentLinkedDeque<March> attackerMarches = war.getAttacker().getMarchList();
		for (March march : attackerMarches) {
			handleCountryReturn(march, reason);
			worldManager.synMarch(mapInfo.getMapId(), march);
			players.add(march.getLordId());
		}

		ConcurrentLinkedDeque<March> defencerMarches = war.getDefencer().getMarchList();
		for (March march : defencerMarches) {
			handleCountryReturn(march, reason);
			worldManager.synMarch(mapInfo.getMapId(), march);
		}

		Pos pos = war.getDefencer().getPos();
		String param = pos.getX() + "," + pos.getY();
		for (Long id : players) {
			Player target = playerManager.getPlayer(id);
			if (target != null && target.isLogin && target.getChannelId() == -1) {
				playerManager.sendNormalMail(target, MailId.COUNTRY_WAR_RETURN, param);
			}
		}

	}

	// 防守方
	// 我国XX区域YY据点[坐标]被ZZ国NNN率众成功占领[需要考虑群雄]
	public void countryFailedDaily(int cityId, int mapId, WarInfo warInfo) {
		CtyDaily ctyDaily = new CtyDaily();
		ctyDaily.setDailyId(CountryDailyId.War_Failed_Id);
		ctyDaily.setTime(System.currentTimeMillis());
		ctyDaily.setMapId(mapId);
		ctyDaily.setCityId(cityId);
		ctyDaily.setCountry(warInfo.getAttackerCountry());
		long attackId = warInfo.getAttackerId();
		Player player = playerManager.getPlayer(attackId);
		if (player != null) {
			ctyDaily.setPlayerName(player.getNick());
		} else {
			CountryCityWarInfo countryCityWarInfo = (CountryCityWarInfo) warInfo;
			if (countryCityWarInfo.getCityName() != null) {
				ctyDaily.setPlayerName(countryCityWarInfo.getCityName());
			}
		}
		countryManager.addCountryDaily(warInfo.getDefencerCountry(), ctyDaily);
	}

	/**
	 * 取消当前城池的其他国战信息
	 *
	 * @param mapInfo
	 * @param cityId
	 * @param finishWarId
	 */
	public void cancelCountryWarWhenOccupy(MapInfo mapInfo, int cityId, long finishWarId) {

		mapInfo.getWarMap().values().stream().filter(e -> e.getWarType() == WarType.ATTACK_COUNTRY && e.getDefencer().getId() == cityId && e.getWarId() != finishWarId).forEach(war -> {

			if (war instanceof CountryCityWarInfo) {
				CountryCityWarInfo warInfo = (CountryCityWarInfo) war;

				// 劝返行军
				ctWarMarchReturn(warInfo, mapInfo, MarchReason.CtWarProtectedTime);

				//遣返的部队也需要同步战斗信息
				warManager.synWarInfoToWorld(warInfo);
				worldManager.flushWar(warInfo, false, war.getAttacker().getCountry());

				// 设置战斗为结束状态
				warInfo.setEnd(true);
			}
		});
	}

	// YY区域ZZ据点[坐标]由我国XXX率众成功占领
	public void countryWinDaily(int cityId, int mapId, WarInfo warInfo) {
		CtyDaily ctyDaily = new CtyDaily();
		ctyDaily.setDailyId(CountryDailyId.War_Win_Id);
		ctyDaily.setTime(System.currentTimeMillis());
		ctyDaily.setMapId(mapId);
		ctyDaily.setCityId(cityId);
		ctyDaily.setCountry(warInfo.getAttackerCountry());
		long attackId = warInfo.getAttackerId();
		Player player = playerManager.getPlayer(attackId);
		if (player != null) {
			ctyDaily.setPlayerName(player.getNick());
		} else {
			CountryCityWarInfo countryCityWarInfo = (CountryCityWarInfo) warInfo;
			if (countryCityWarInfo.getCityName() != null) {
				ctyDaily.setPlayerName(countryCityWarInfo.getCityName());
			}
		}
		countryManager.addCountryDaily(warInfo.getAttackerCountry(), ctyDaily);

	}

	// 国战荣誉
	public void handleCtWarRank(WarInfo warInfo) {
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
			countryManager.updCountryHoror(player, CountryConst.RANK_STATE);
		}
	}

	// 国家任务: 攻下任何一个城市
	public void doAttackCountryTask(WarInfo warInfo) {
		ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
		if (attacker.isEmpty()) {
			return;
		}

		HashSet<Long> players = new HashSet<Long>();
		for (March march : attacker) {
			players.add(march.getLordId());
		}

		for (Long lordId : players) {
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
			countryManager.doCountryTask(player, CountryTaskType.ATTACK_POINT, 1);
		}
	}

	public void handlerCountryChat(WarInfo warInfo, int cityId) {
		Player caller = playerManager.getPlayer(warInfo.getAttackerId());
		Player target = playerManager.getPlayer(warInfo.getDefencerId());
		if (caller != null) {
			StaticWorldCity staticCity = staticWorldMgr.getCity(cityId);
			if (staticCity != null) {
				String mapId = String.valueOf(staticCity.getMapId());
				String pos = String.valueOf(staticCity.getX() + "," + staticCity.getY());
				String params[] = {mapId, String.valueOf(staticCity.getCityId()), pos, caller.getNick()};
				if (caller != null) {
					chatManager.sendCountryChat(caller.getCountry(), ChatId.MAP_CITY_ATTACK, params);
				}
				//敌方阵营占领成功
				if (target != null) {
					chatManager.sendCountryChat(target.getCountry(), ChatId.COUNTRY_LOST, params);
				}
			}
		}
	}

	// 打下来之后增加保护罩
	public void handleCityProtected(int cityId) {
		City city = cityManager.getCity(cityId);
		if (city == null) {
			LogHelper.CONFIG_LOGGER.info("city is null!");
			return;
		}

		// 类型为8的不加保护罩
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		if (worldCity.getType() == 8) {
			return;
		}

		long configTime = staticLimitMgr.getNum(72) * TimeHelper.SECOND_MS;
		long now = System.currentTimeMillis();
		if (city.getProtectedTime() <= now) {
			city.setProtectedTime(now);
		}

		if (worldCity.getType() == CityType.SQUARE_FORTRESS) {
			// pass
		} else {
			city.setProtectedTime(now + configTime);
		}

		if (city.getState() != CityState.COMMON_MAKE_ITEM) {
			city.setProtectedTime(now);
		}
	}

	public void handleCityElection(int cityId, WarInfo warInfo) {
		HashSet<Long> warAttenders = cityManager.getWarAttenders(cityId);
		if (warAttenders == null) {
			warAttenders = new HashSet<Long>();
		}

		ConcurrentLinkedDeque<March> attackerMarches = warInfo.getAttackMarches();
		for (March march : attackerMarches) {
			warAttenders.add(march.getLordId());
		}
	}

	public void doWarTask(int cityId, WarInfo warInfo) {
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
		if (staticWorldCity == null) {
			return;
		}

		int cityType = staticWorldCity.getType();
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(cityType);
		ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
		for (March march : attacker) {
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}

			taskManager.doTask(TaskType.CAPTURE_CITY, player, triggers);
			// 攻克营地,市政等
			int sortId = 4000 + cityType;
			activityManager.updActSeven(player, ActivityConst.TYPE_ADD, sortId, 0, 1);
		}

	}

	// 更新城池怪物血量
	public void handleCitySoldier(Team cityTeam, int cityId) {
		// 7,8,9 不用扣血
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		if (worldCity == null) {
			LogHelper.CONFIG_LOGGER.info("worldCity is null, cityId = " + cityId);
			return;
		}

		City cityInfo = cityManager.getCity(cityId);

		// 如果是名城和四方要塞, 且没有打下来，城池怪物是不回血的
		if (isFamousAndFortress(worldCity.getType()) && cityInfo.getCountry() == 0) {
			return;
		}

		ArrayList<BattleEntity> allEnities = cityTeam.getAllEnities();
		// 计算城池血量
		CityMonster cityMonster = cityManager.getCityMonster(cityId);
		if (cityMonster == null) {
			LogHelper.CONFIG_LOGGER.info("cityMonster is null!");
			return;
		}

		if (cityMonster.isFullHp()) {
			StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityMonster.getCityId());
			if (staticWorldCity != null) {
				cityMonster.setLastReoverTime(System.currentTimeMillis() + staticWorldCity.getRecoverTime()
					* TimeHelper.SECOND_MS);
			}
		}

		Map<Integer, CityMonsterInfo> monsterInfoMap = cityMonster.getMonsterInfoMap();
		if (monsterInfoMap == null || monsterInfoMap.isEmpty()) {
			LogHelper.CONFIG_LOGGER.info("monsterInfoMap == null || monsterInfoMap.isEmpty() !!");
			return;
		}

		for (BattleEntity entity : allEnities) {
			if (entity.getEntityType() == BattleEntityType.CITY_MONSTER) {
				CityMonsterInfo info = monsterInfoMap.get(entity.getEntityId());
				if (info == null) {
					continue;
				}

				info.setSoldier(entity.getCurSoldierNum());
			}
		}

	}

	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
		if (worldMap.getCountryWarData() == null) {
			return;
		}

		SerCountryWar serCountryWar = null;
		try {
			serCountryWar = SerCountryWar.parseFrom(worldMap.getCountryWarData());

			if (serCountryWar == null) {
				return;
			}

			List<DataPb.WarData> warDatas = serCountryWar.getWarDataList();
			if (warDatas == null) {
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

				WarInfo warInfo = warManager.createCountryWar(warData, mapInfo);
				mapInfo.addWar(warInfo);


				if (warInfo.getAttackerCountry() != 0 || warInfo.getDefencerCountry() != 0) {
					worldManager.flushWar(warInfo, true, warInfo.getAttackerCountry());
				}

			}
		} catch (InvalidProtocolBufferException e) {
		}
	}
}
