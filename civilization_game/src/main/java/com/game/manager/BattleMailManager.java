package com.game.manager;

import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.log.domain.BattleLog;
import com.game.log.domain.MailLog;
import com.game.pb.CommonPb;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.game.worldmap.*;
import com.game.worldmap.fight.war.CountryCityWarInfo;
import com.google.common.collect.HashBasedTable;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

// 战斗邮件管理器
@Component
public class BattleMailManager {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticMonsterMgr staticMonsterMgr;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private BattleMgr battleMgr;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private CityManager cityManager;

	@Autowired
	private MailManager mailManager;

	@Autowired
	private HeroManager heroManager;

	@Autowired
	private TechManager techManager;

	@Autowired
	private RiotManager riotManager;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private StaticWorldActPlanMgr staticWorldActPlanMgr;

	@Autowired
	private StaticRiotMgr staticRiotMgr;

	@Autowired
	private WarBookManager warBookManager;

	// 创建战报头(玩家)
	public ReportHead createPlayerReportHead(Player player, Team team, Pos pos) {
		ReportHead reportHead = new ReportHead();
		if (player != null) {
			if (player.getNick() != null) {
				reportHead.setName(player.getNick());
			}
			reportHead.setPortrait(player.getPortrait());
			reportHead.setHeadSculpture(player.getLord().getHeadIndex());
		}
		reportHead.setLost(team.getLost());
		if (player != null) {
			reportHead.setCountry(player.getCountry());
			reportHead.setPortrait(player.getPortrait());
		} else {
			LogHelper.CONFIG_LOGGER.info("player is null!");
		}

		reportHead.setType(TeamType.PLAYER);
		reportHead.setPos(new Pos(pos.getX(), pos.getY()));
		reportHead.setSoldierNum(team.getMaxSoldier());

		return reportHead;
	}

	// 创建战报头(叛军)
	public ReportHead createRoitMonsterHead(Team team, Pos pos, Monster monster) {
		ReportHead reportHead = new ReportHead();
		//int curSoldier = team.getCurSoldier();
		// 玩家
		for (BattleEntity battleEntity : team.getAllEnities()) {
			if (battleEntity == null) {
				continue;
			}

			Integer monsterId = staticWorldMgr.getWorldMonsterId(battleEntity.getEntityId());
			if (monsterId == null) {
				LogHelper.CONFIG_LOGGER.info("monsterId is null");
				continue;
			}
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
			if (staticMonster == null) {
				LogHelper.CONFIG_LOGGER.info("monsterId not exists, id = " + monsterId);
				continue;
			}
			reportHead.setSoldierNum(reportHead.getSoldierNum() + battleEntity.getMaxSoldierNum());
			if (reportHead.getMonsterId() == 0) {
				reportHead.setMonsterId(battleEntity.getEntityId());
			}
		}
		StaticWorldMonster monster1 = staticWorldMgr.getMonster((int) monster.getId());
		if (monster1 != null) {
			reportHead.setName(monster1.getName());
		}
		reportHead.setLost(team.getLost());
		reportHead.setCountry(team.getCountry());
		reportHead.setType(TeamType.REBEL);
		reportHead.setPos(new Pos(pos.getX(), pos.getY()));

		return reportHead;
	}

	// 创建战报头(叛军)
	public ReportHead createMonsterHead(Team team, Pos pos) {
		ReportHead reportHead = new ReportHead();

		// 玩家
		for (BattleEntity battleEntity : team.getAllEnities()) {
			if (battleEntity == null) {
				continue;
			}

			Integer monsterId = staticWorldMgr.getWorldMonsterId(battleEntity.getEntityId());
			if (monsterId == null) {
				LogHelper.CONFIG_LOGGER.info("monsterId is null");
				continue;
			}
			StaticWorldMonster staticMonster = staticWorldMgr.getMonster(monsterId);
			if (staticMonster == null) {
				LogHelper.CONFIG_LOGGER.info("monsterId not exists, id = " + monsterId);
				continue;
			}

			String name = staticMonster.getName();
			if (name != null && StringUtil.isNullOrEmpty(reportHead.getName())
				|| !StringUtil.isNullOrEmpty(reportHead.getName()) && reportHead.getName().equals("unkown")) {
				reportHead.setName(name);
			}
			reportHead.setSoldierNum(reportHead.getSoldierNum() + battleEntity.getMaxSoldierNum());
			if (reportHead.getMonsterId() == 0) {
				reportHead.setMonsterId(battleEntity.getEntityId());
			}
		}

		reportHead.setLost(team.getLost());
		reportHead.setCountry(team.getCountry());
		reportHead.setType(TeamType.REBEL);
		reportHead.setPos(new Pos(pos.getX(), pos.getY()));

		return reportHead;
	}

	// 创建战报头(城池)
	public ReportHead createCityReportHead(int cityId, Team team, Pos pos) {
		ReportHead reportHead = new ReportHead();
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		if (worldCity != null) {
			if (worldCity.getName() != null) {
				reportHead.setName(worldCity.getName());
			} else {
				reportHead.setName("unkown");
			}

			reportHead.setCityId(worldCity.getCityId());
		}

		reportHead.setLost(team.getLost());
		reportHead.setCountry(team.getCountry());
		reportHead.setType(TeamType.NPC_CITY);
		if (pos != null) {
			reportHead.setPos(new Pos(pos.getX(), pos.getY()));
		} else {
			if (worldCity != null) {
				reportHead.setPos(new Pos(worldCity.getX(), worldCity.getY()));
			} else {
				reportHead.setPos(new Pos(0, 0));
			}
		}
		reportHead.setSoldierNum(team.getMaxSoldier());

		return reportHead;
	}

	// 创建战报头(近卫军)
	public ReportHead createGuardReportHead(int monsterId, Team team) {
		ReportHead reportHead = new ReportHead();
		StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(monsterId);
		if (staticMonster == null) {
			LogHelper.CONFIG_LOGGER.info("guard monster not exists, staticMonster is null, monsterId = " + monsterId);
			return reportHead;
		}

		if (staticMonster.getName() != null) {
			reportHead.setName(staticMonster.getName());
		}
		reportHead.setMonsterId(staticMonster.getMonsterId());
		reportHead.setLost(team.getLost());
		reportHead.setCountry(team.getCountry());
		reportHead.setType(TeamType.GUARD_MONSTER);
		reportHead.setSoldierNum(team.getMaxSoldier());

		return reportHead;
	}

	// 创建参与者
	public List<Attender> createAttender(Team team, int warType) {

		List<Attender> list = new ArrayList<Attender>();
		ArrayList<BattleEntity> gameEntities = team.getAllEnities();
		for (BattleEntity battleEntity : gameEntities) {
			Attender attender = new Attender();
			attender.setEntityId(battleEntity.getEntityId());
			attender.setEntityType(battleEntity.getEntityType());
			attender.setEntityLv(battleEntity.getLevel());
			attender.setSoldierType(battleEntity.getSoldierType());
			attender.setKillNum(battleEntity.getKillNum());
			double techAdd = techManager.getHonorAdd(battleEntity.getLordId());
			int lastHonor = getHonor(battleEntity, warType);

			if ((battleEntity.getEntityType() == 1 || battleEntity.getEntityType() == 6) && warType == WarType.ATTACK_COUNTRY) {
				long lordId = battleEntity.getLordId();
				int entityId = battleEntity.getEntityId();
				/**
				 * 兵书计算技能加成
				 */
				Player player = playerManager.getPlayer(lordId);
				if (null != player) {
					Map<Integer, Hero> heroMap = player.getHeros();
					Hero hero = heroMap.get(entityId);
					if (null != hero) {
						Integer heroWarBookSkillEffect = warBookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.COUNTRY_WAR);
						long bookEffectHoronCd = player.getLord().getBookEffectHoronCd();
						long now = System.currentTimeMillis();
						if (null != heroWarBookSkillEffect && bookEffectHoronCd <= now) {
							int killNum = battleEntity.getKillNum();
							if (killNum > 0) {
								playerManager.addAward(player, AwardType.LORD_PROPERTY, LordPropertyType.HONOR, heroWarBookSkillEffect.intValue(), Reason.WAR);
								lastHonor = lastHonor + heroWarBookSkillEffect.intValue();
								warBookManager.updateWarBookBuff(player, hero, BookEffectType.COUNTRY_WAR);
							}
						}
					}
				}
			}
			if (battleEntity.getEntityType() == BattleEntityType.HERO || battleEntity.getEntityType() == BattleEntityType.FRIEND_HERO
				|| battleEntity.getEntityType() == BattleEntityType.WALL_FRIEND_HERO || battleEntity.getEntityType() == BattleEntityType.DEFENSE_ARMY_HERO) {
				Player player = playerManager.getPlayer(battleEntity.getLordId());
				if (player != null) {
					Hero hero = player.getHero(battleEntity.getEntityId());
					if (hero != null) {
						attender.setDivNum(hero.getDiviNum());
					}
				}
			}
			int honor = (int) ((double) lastHonor * (1.0f + techAdd));
			attender.setHonor(honor);
			attender.setQuality(battleEntity.getQuality());
			long lordId = battleEntity.getLordId();
			if (lordId > 0L) {
				Player player = playerManager.getPlayer(lordId);
				if (player != null && player.getNick() != null) {
					attender.setAttenderName(player.getNick());
					attender.setLordId(player.roleId);
				}
			} else {
				if (battleEntity.getEntityType() == BattleEntityType.CITY_MONSTER) {
					StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(battleEntity.getEntityId());
					if (staticMonster != null) {
						attender.setAttenderName(staticMonster.getName());
					}
				} else if (battleEntity.getEntityType() == BattleEntityType.WALL_DEFENCER) {
					long targetId = battleEntity.getWallLordId();
					Player target = playerManager.getPlayer(targetId);
					if (target != null && target.getNick() != null) {
						attender.setAttenderName(target.getNick());


					}
				}
			}

			list.add(attender);

		}

		return list;
	}

	public int getHonor(BattleEntity entity, int warType) {
		if (warType == WarType.ATTACK_COUNTRY) {
			int honor = getLastHonor(entity, 157);
			int entityType = entity.getEntityType();
			return honor;
		} else if (warType == WarType.ATTACK_QUICK || warType == WarType.ATTACK_FAR) {
			return getLastHonor(entity, 158);
		}
		return 0;
	}

	public int getLastHonor(BattleEntity entity, int index) {
		int configNum = staticLimitMgr.getNum(index);
		if (configNum == 0) {
			return 0;
		}

		if (configNum == 1) {
			return entity.getLost();
		}

		if (configNum == 2) {
			return entity.getKillNum();
		}
		return 0;
	}


	// 创建参与者
	public List<Attender> createRebelAttender(Team team) {
		List<Attender> list = new ArrayList<Attender>();
		ArrayList<BattleEntity> gameEntities = team.getAllEnities();
		for (BattleEntity battleEntity : gameEntities) {
			Attender attender = new Attender();
			attender.setEntityId(battleEntity.getEntityId());
			attender.setEntityType(battleEntity.getEntityType());
			attender.setEntityLv(battleEntity.getLevel());
			attender.setSoldierType(battleEntity.getSoldierType());
			attender.setKillNum(battleEntity.getKillNum());
			attender.setHonor(0);
			attender.setQuality(battleEntity.getQuality());
			attender.setLost(battleEntity.getLost());
			long lordId = battleEntity.getLordId();
			if (lordId > 0L) {
				Player player = playerManager.getPlayer(lordId);
				if (player != null && player.getNick() != null) {
					attender.setAttenderName(player.getNick());
					attender.setLordId(player.roleId);
					Hero hero = player.getHero(battleEntity.getEntityId());
					if (hero != null) {
						attender.setDivNum(hero.getDiviNum());
					}
				}
			} else {
				if (battleEntity.getEntityType() == BattleEntityType.REBEL) {
					StaticWorldMonster worldMonster = staticWorldMgr.getMonster(battleEntity.getLevel() + 1000);
					if (worldMonster != null && worldMonster.getName() != null) {
						attender.setAttenderName(worldMonster.getName()); // monster
					}
				} else if (battleEntity.getEntityType() == BattleEntityType.ROIT_MONSTER) {
					StaticWorldMonster worldMonster = staticWorldMgr.getMonster(battleEntity.getEntityId());
					if (worldMonster != null && worldMonster.getName() != null) {
						attender.setAttenderName(worldMonster.getName()); // monster
					}
				} else if (battleEntity.getEntityType() == BattleEntityType.WALL_DEFENCER) {
					long targetId = battleEntity.getWallLordId();
					Player target = playerManager.getPlayer(targetId);
					if (target != null && target.getNick() != null) {
						attender.setAttenderName(target.getNick());
					}
				} else if (battleEntity.getEntityType() == BattleEntityType.BIG_MONSTER) {
					//已经死亡的不放入队列
					if (battleEntity.getMaxSoldierNum() == 0) {
						continue;
					}
					StaticMonster monster = staticMonsterMgr.getStaticMonster(battleEntity.getEntityId());
					if (monster != null && monster.getName() != null) {
						attender.setAttenderName(monster.getName());
					}
				}
			}

			list.add(attender);

		}

		return list;
	}

	// 创建战报:城战
	public Report createReport(WarInfo warInfo, Team teamA, Team teamB) {
		Report report = new Report();
		report.setKeyId(warInfo.getWarId());
		report.setResult(teamA.isWin());

		// 战报头信息
		long lordId = warInfo.getAttackerId();
		Player player = playerManager.getPlayer(lordId);
		ReportHead leftHead = createPlayerReportHead(player, teamA, player.getPos());
		report.setLeftHead(leftHead);

		// 失败
		long targetLordId = warInfo.getDefencerId();
		Player target = playerManager.getPlayer(targetLordId);
		ReportHead rightHead = createPlayerReportHead(target, teamB, target.getPos());
		report.setRightHead(rightHead);

		List<Attender> attackers = createAttender(teamA, WarType.ATTACK_FAR);
		report.setLeftAttender(attackers);
		List<Attender> defenders = createAttender(teamB, WarType.ATTACK_FAR);
		report.setRightAttender(defenders);

		return report;
	}

	// 创建战报:国战
	public Report createCountryReport(WarInfo warInfo, Team teamA, Team teamB) {
		Report report = new Report();
		report.setKeyId(warInfo.getWarId());
		report.setResult(teamA.isWin());

		int cityId = (int) warInfo.getDefencerId();
		// 战报头信息: 成功
		// 战报头有可能是玩家或者近卫军
		if (warInfo.getWarType() == WarType.ATTACK_COUNTRY) {
			// 有可能是近卫军
			if (warInfo.getAttackerCountry() != 0) {
				// 这里还有可能是玩家的近卫军
				long lordId = warInfo.getAttackerId();
				Player player = playerManager.getPlayer(lordId);
				if (player != null) {
					ReportHead leftHead = createPlayerReportHead(player, teamA, player.getPos());
					report.setLeftHead(leftHead);
				} else {
					// 说明有近卫军
					int monsteId = warInfo.getMonsterId();
					if (monsteId != 0) {
						ReportHead leftHead = createGuardReportHead(monsteId, teamA);
						report.setLeftHead(leftHead);
					}
				}
			} else {
				// 说明有近卫军
				int monsteId = warInfo.getMonsterId();
				if (monsteId != 0) {
					ReportHead leftHead = createGuardReportHead(monsteId, teamA);
					report.setLeftHead(leftHead);
				} else {
					LogHelper.CONFIG_LOGGER.info("square monster id is 0, check config!");
				}
			}
		}

		// 失败
		ReportHead rightHead = createCityReportHead(cityId, teamB, warInfo.getDefencerPos());
		report.setRightHead(rightHead);

		List<Attender> attackers = createAttender(teamA, WarType.ATTACK_COUNTRY);
		report.setLeftAttender(attackers);
		List<Attender> defenders = createAttender(teamB, WarType.ATTACK_COUNTRY);
		report.setRightAttender(defenders);

		return report;
	}

	// 计算当前英雄的损兵情况
	public int getLost(Team team, int heroId, long lordId) {
		List<BattleEntity> list = team.getAllEnities();
		BattleEntity battleEntity = null;
		for (BattleEntity elem : list) {
			if (elem.getEntityId() == heroId && elem.getLordId() == lordId && battleMgr.isHeroType(elem.getEntityType())) {
				battleEntity = elem;
			}
		}

		if (battleEntity == null) {
			// LogHelper.ERROR_LOGGER.error("hero not found!");
			return 0;
		}
		return battleEntity.getLost();
	}


	// 计算当前英雄的击杀情况
	public int getKillNum(Team team, int heroId, long lordId) {
		List<BattleEntity> list = team.getAllEnities();
		BattleEntity battleEntity = null;
		for (BattleEntity elem : list) {
			if (elem.getEntityId() == heroId && elem.getLordId() == lordId && battleMgr.isHeroType(elem.getEntityType())) {
				battleEntity = elem;
			}
		}

		if (battleEntity == null) {
			// LogHelper.ERROR_LOGGER.error("hero not found!");
			return 0;
		}
		return battleEntity.getKillNum();
	}

	public List<Award> getCountryWinAward(int soldier) {
		List<Award> awards = new ArrayList<Award>();
		int iron = staticLimitMgr.getNum(40) * soldier;
		int copper = staticLimitMgr.getNum(41) * soldier;

		awards.add(new Award(0, AwardType.RESOURCE, ResourceType.IRON, iron));
		awards.add(new Award(0, AwardType.RESOURCE, ResourceType.COPPER, copper));

		return awards;
	}

	public List<Award> getCountryFailAward(int soldier) {
		List<Award> awards = new ArrayList<Award>();
		int iron = (int) ((double) staticLimitMgr.getNum(37) / 100.0 * soldier);
		int copper = staticLimitMgr.getNum(38) * soldier;

		awards.add(new Award(0, AwardType.RESOURCE, ResourceType.IRON, iron));
		awards.add(new Award(0, AwardType.RESOURCE, ResourceType.COPPER, copper));

		return awards;
	}

	public void handleWinAward(WarInfo warInfo, Team team, int side) {
		ConcurrentLinkedDeque<March> marches = new ConcurrentLinkedDeque<March>();
		if (side == 1) {
			marches = warInfo.getAttackMarches();
		} else if (side == 2) {
			marches = warInfo.getDefenceMarches();
		}
		for (March march : marches) {
			if (march == null) {
				continue;
			}

			List<Integer> heroIds = march.getHeroIds();
			int totalLost = 0;
			for (Integer heroId : heroIds) {
				totalLost += getLost(team, heroId, march.getLordId());
			}

			List<Award> winAward = getCountryWinAward(totalLost);
			if (winAward != null) {
				march.setAwards(winAward);
			}
		}
	}

	public void handleFailedAward(WarInfo warInfo, Team team, int side) {
		ConcurrentLinkedDeque<March> marches = new ConcurrentLinkedDeque<March>();
		if (side == 1) {
			marches = warInfo.getAttackMarches();
		} else if (side == 2) {
			marches = warInfo.getDefenceMarches();
		}

		for (March march : marches) {
			if (march == null) {
				continue;
			}

			List<Integer> heroIds = march.getHeroIds();
			int totalLost = 0;
			for (Integer heroId : heroIds) {
				totalLost += getLost(team, heroId, march.getLordId());
			}

			List<Award> winAward = getCountryFailAward(totalLost);
			if (winAward != null) {
				march.setAwards(winAward);
			}
		}
	}

	public void handleHeroExp(ReportMsg reportMsg, HeroAddExp addExp) {
		long lordId = addExp.getLordId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			//LogHelper.ERROR_LOGGER.error("player is null!");
			return;
		}

		List<HeroInfo> heroInfos = reportMsg.getHeroInfos();
		Map<Integer, Integer> heroAddExp = addExp.getHeroAddExp();
		for (Map.Entry<Integer, Integer> entry : heroAddExp.entrySet()) {
			Integer heroId = entry.getKey();
			if (heroId == null) {
				continue;
			}

			Hero hero = player.getHero(heroId);
			if (hero == null) {
				continue;
			}

			HeroInfo heroInfo = new HeroInfo();
			heroInfo.setHeroId(heroId);
			heroInfo.setLv(hero.getHeroLv());
			heroInfo.setExp(hero.getExp());
			heroInfo.setAddExp(entry.getValue());
			heroInfos.add(heroInfo);
		}
	}

	// 发送国战战报
	public void sendCountryWarReport(WarInfo warInfo,
		Team teamA,
		Team teamB,
		int cityId,
		HeroAddExp heroAddExp,
		HashBasedTable<Long, Integer, Integer> allSoldierRec,
		long cityOldLordId) {
		Report report = createCountryReport(warInfo, teamA, teamB);
		ReportMsg reportMsg = createReportMsg(teamA, teamB);
		if (heroAddExp.getLordId() != 0 &&
			warInfo.getAttackerCountry() != 0) {
			handleHeroExp(reportMsg, heroAddExp);
		}

		ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
		ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();

		int teamAMailId;
		int teamBMailId;

		if (teamA.isWin()) {
			teamAMailId = MailId.CT_WAR_WIN;
			handleWinAward(warInfo, teamA, 1);
		} else {
			teamAMailId = MailId.CT_WAR_FAILED;
			handleFailedAward(warInfo, teamA, 1);
		}

		if (teamB.isWin()) {
			teamBMailId = MailId.COUNTRY_DEFENCE_WIN;
			handleWinAward(warInfo, teamB, 2);
		} else {
			teamBMailId = MailId.COUNTRY_DEFENCE_FAILED;
			handleFailedAward(warInfo, teamB, 2);
		}

		long attackerId = warInfo.getAttackerId();
//        for (March march : attacker) {
//            long lordId = march.getLordId();
//            Player player = playerManager.getPlayer(lordId);
//            if (player == null) {
//                continue;
//            }
//            reportMsg.addAwards(march.getAwards());
//            sendCtWarMail(warInfo, player, attackerId, cityId, march, report, reportMsg, teamAMailId, allSoldierRec);
//        }

		Map<Long, List<Award>> attackAward = getWarAward(attacker);
		Map<Long, List<Award>> listMap = new HashMap<>();
		for (Map.Entry<Long, List<Award>> entry : attackAward.entrySet()) {
			Player player = playerManager.getPlayer(entry.getKey());
			reportMsg.addAwards(entry.getValue());
			sendCtWarMail(warInfo, player, attackerId, cityId, entry.getValue(), report, reportMsg, teamAMailId, allSoldierRec);
			listMap.put(entry.getKey(), entry.getValue());
		}
		for (March march : attacker) {
			List<Award> awards = listMap.get(march.getLordId());
			march.addAllAwards(awards);
		}
//        for (March march : defencer) {
//            long lordId = march.getLordId();
//            Player player = playerManager.getPlayer(lordId);
//            if (player == null) {
//                continue;
//            }
//            reportMsg.addAwards(march.getAwards());
//            sendCtWarMail(warInfo, player, attackerId, cityId, march, report, reportMsg, teamBMailId, allSoldierRec);
//        }

		Map<Long, List<Award>> defencerAward = getWarAward(defencer);
		for (Map.Entry<Long, List<Award>> entry : defencerAward.entrySet()) {
			Player player = playerManager.getPlayer(entry.getKey());
			reportMsg.addAwards(entry.getValue());
			sendCtWarMail(warInfo, player, attackerId, cityId, entry.getValue(), report, reportMsg, teamBMailId, allSoldierRec);
		}

		// 城主防守成功和防守失败都要收到一封邮件
		sendCityOwnerMail(warInfo, attackerId, cityId, cityOldLordId, teamBMailId, report, reportMsg);
	}


	/**
	 * 合并奖励
	 *
	 * @param marches
	 * @return
	 */
	public Map<Long, List<Award>> getWarAward(ConcurrentLinkedDeque<March> marches) {
		Map<Long, List<Award>> awards = new HashMap<>();
		for (March march : marches) {
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
			List<Award> awardList = awards.get(lordId);
			if (awardList == null) {
				awardList = new ArrayList<>();
				awards.put(lordId, awardList);
			}
			if (march.getAwards() != null && march.getAwards().size() > 0) {
				for (Award award : march.getAwards()) {
					boolean flag = false;
					for (Award result : awardList) {
						if (award.getId() == result.getId() && award.getType() == result.getType() && award.getKeyId() == result.getKeyId()) {
							result.setCount(result.getCount() + award.getCount());
							flag = true;
							break;
						}
					}
					if (!flag) {
						awardList.add(award);
					}
				}
			}
		}

		return awards;
	}

	// color=#BDBDBD>国战进攻 %s</font><font color=#AD55FF event=click1>[%s]</font>",
	// "<font color=#BDBDBD>本次战斗中\n%s获得：</font>","<font color=#BDBDBD>生铁x%s</font>","<font color=#BDBDBD>黄铜x%s</font>"]
	public void sendCityOwnerMail(WarInfo warInfo, long attackerId, int cityId,
		long cityLordId, int teamBMailId,
		Report report, ReportMsg reportMsg) {
		// 如果城主在战斗过程中，则不收邮件，如果不在则收到邮件
		if (cityLordId == 0) {
			return;
		}

		ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
		ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();

		HashSet<Long> attenders = new HashSet<Long>();

		for (March march : attacker) {
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
			attenders.add(lordId);
		}

		for (March march : defencer) {
			long lordId = march.getLordId();
			Player player = playerManager.getPlayer(lordId);
			if (player == null) {
				continue;
			}
			attenders.add(lordId);
		}

		if (attenders.contains(cityLordId)) {
			return;
		}

		Player target = playerManager.getPlayer(cityLordId);
		if (target == null) {
			return;
		}

		sendCtWarMailOwner(target, attackerId, cityId, report, reportMsg, teamBMailId);
	}

	// %s[%s]国战进攻 %s[%s]/n本次战斗中，%s获得：/n生铁X%s 黄铜X%s
	// 玩家角色名[坐标]国战进攻 皇城中山城[坐标]/n本次战斗中，玩家角色名获得：/n生铁X生铁数量 黄铜X黄铜数量
	public void sendCtWarMailOwner(Player receiver, long attackerId, int cityId, Report report, ReportMsg reportMsg, int mailId) {
		Player attacker = playerManager.getPlayer(attackerId);
		String name = "null";
		String pos = "null";
		String myName = "null";
		if (attacker != null) {
			name = attacker.getNick();
			pos = attacker.getPosStr();
		}

		if (receiver != null && receiver.getNick() != null) {
			myName = receiver.getNick();
		}
		String cityName = "null";
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		String cityPos = "null";
		if (worldCity != null) {
			int mapId = worldCity.getMapId();
			StaticWorldMap worldMap = staticWorldMgr.getStaticWorldMap(mapId);
			if (worldMap != null) {
				cityName = worldMap.getName() + worldCity.getName();
			}

			cityPos = String.valueOf(worldCity.getX()) + "," + String.valueOf(worldCity.getY());
		}

		String ironStr = String.valueOf(0);
		String copperStr = String.valueOf(0);
		HashMap<Integer, Integer> soldierRec = new HashMap<Integer, Integer>();
		playerManager.sendReportMail(receiver, report, reportMsg, mailId, new ArrayList<Award>(), soldierRec, name, pos, cityName, cityPos, myName, ironStr, copperStr);
	}


	// %s[%s]国战进攻 %s[%s]/n本次战斗中，%s获得：/n生铁X%s 黄铜X%s
	// 玩家角色名[坐标]国战进攻 皇城中山城[坐标]/n本次战斗中，玩家角色名获得：/n生铁X生铁数量 黄铜X黄铜数量
	public void sendCtWarMail(WarInfo warInfo, Player player, long attackerId, int cityId,
		List<Award> awards, Report report, ReportMsg reportMsg,
		int mailId, HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		Player attacker = playerManager.getPlayer(attackerId);
		String name = "null";
		String pos = "null";
		String myName = "null";
		if (attacker != null && attacker.getNick() != null) {
			name = attacker.getNick();
			pos = attacker.getPosStr();
		}

		if (player != null && player.getNick() != null) {
			myName = player.getNick();
		}

		if (warInfo instanceof CountryCityWarInfo) {
			CountryCityWarInfo countryCityWarInfo = (CountryCityWarInfo) warInfo;
			if (countryCityWarInfo.getCityName() != null) {
				name = countryCityWarInfo.getCityName();
				pos = countryCityWarInfo.getAttackerPos().getX() + "," + countryCityWarInfo.getAttackerPos().getY();
			}
		}

		String cityName = "null";
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		String cityPos = "null";
		if (worldCity != null) {
			int mapId = worldCity.getMapId();
			StaticWorldMap worldMap = staticWorldMgr.getStaticWorldMap(mapId);
			if (worldMap != null) {
				cityName = worldMap.getName() + worldCity.getName();
			}

			cityPos = String.valueOf(worldCity.getX()) + "," + String.valueOf(worldCity.getY());
		}

		int iron = 0;
		int copper = 0;
		for (Award award : awards) {
			if (award.getType() == AwardType.RESOURCE) {
				if (award.getId() == ResourceType.IRON) {
					iron = award.getCount();
				} else if (award.getId() == ResourceType.COPPER) {
					copper = award.getCount();
				}

			}
		}

		String ironStr = String.valueOf(iron);
		String copperStr = String.valueOf(copper);
		HashMap<Integer, Integer> soldierRec = getSoldierRecMap(allSoldierRec, player.roleId);

		playerManager.sendReportMail(player, report, reportMsg, mailId, awards, soldierRec, name, pos, cityName, cityPos, myName, ironStr, copperStr);
	}


	// %s[%s]国战进攻 %s[%s]/n本次战斗中，%s获得：/n生铁X%s 黄铜X%s
	// 玩家角色名[坐标]国战进攻 皇城中山城[坐标]/n本次战斗中，玩家角色名获得：/n生铁X生铁数量 黄铜X黄铜数量
	@Deprecated
	public void sendCtWarMail(WarInfo warInfo, Player player, long attackerId, int cityId,
		March march, Report report, ReportMsg reportMsg,
		int mailId, HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		Player attacker = playerManager.getPlayer(attackerId);
		String name = "null";
		String pos = "null";
		String myName = "null";
		if (attacker != null && attacker.getNick() != null) {
			name = attacker.getNick();
			pos = attacker.getPosStr();
		}

		if (player != null && player.getNick() != null) {
			myName = player.getNick();
		}

		if (warInfo instanceof CountryCityWarInfo) {
			CountryCityWarInfo countryCityWarInfo = (CountryCityWarInfo) warInfo;
			if (countryCityWarInfo.getCityName() != null) {
				name = countryCityWarInfo.getCityName();
				pos = countryCityWarInfo.getAttackerPos().getX() + "," + countryCityWarInfo.getAttackerPos().getY();
			}
		}

		String cityName = "null";
		StaticWorldCity worldCity = staticWorldMgr.getCity(cityId);
		String cityPos = "null";
		if (worldCity != null) {
			int mapId = worldCity.getMapId();
			StaticWorldMap worldMap = staticWorldMgr.getStaticWorldMap(mapId);
			if (worldMap != null) {
				cityName = worldMap.getName() + worldCity.getName();
			}

			cityPos = String.valueOf(worldCity.getX()) + "," + String.valueOf(worldCity.getY());
		}

		int iron = 0;
		int copper = 0;
		for (Award award : march.getAwards()) {
			if (award.getType() == AwardType.RESOURCE) {
				if (award.getId() == ResourceType.IRON) {
					iron = award.getCount();
				} else if (award.getId() == ResourceType.COPPER) {
					copper = award.getCount();
				}

			}
		}

		String ironStr = String.valueOf(iron);
		String copperStr = String.valueOf(copper);
		HashMap<Integer, Integer> soldierRec = getSoldierRecMap(allSoldierRec, player.roleId);
		playerManager.sendReportMail(player, report, reportMsg, mailId, march.getAwards(), soldierRec, name, pos, cityName, cityPos, myName, ironStr, copperStr);
	}

	// 创建击杀流寇的邮件
	public Report createKillMonsterReport(Team teamA, Team teamB, Player player, Monster monster) {
		Report report = new Report();
		report.setKeyId(System.currentTimeMillis());
		report.setResult(teamA.isWin());

		// 战报头信息: 成功
		ReportHead leftHead = createPlayerReportHead(player, teamA, player.getPos());
		report.setLeftHead(leftHead);

		// 失败
		ReportHead rightHead = createMonsterHead(teamB, monster.getPos());
		report.setRightHead(rightHead);

		List<Attender> attackers = createRebelAttender(teamA);
		report.setLeftAttender(attackers);
		List<Attender> defenders = createRebelAttender(teamB);
		report.setRightAttender(defenders);

		// 战斗回放
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		fightIn.setLeftInfo(leftAttackInfos);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		fightIn.setRightInfo(rightAttackInfos);

		return report;
	}

	// 创建战报
	public ReportMsg createReportMsg(Team teamA, Team teamB, List<Award> awards, HeroAddExp heroAddExp) {
		ReportMsg reportMsg = new ReportMsg();
		// 战前信息
		FightBefore fightBefore = new FightBefore();
		ArrayList<BattleEntity> teamAEntities = teamA.getAllEnities();
		ArrayList<BattleEntity> teamBEntities = teamB.getAllEnities();
		int teamALen = teamAEntities.size();
		int teamBLen = teamBEntities.size();

		BattleEntity battleEntity;
		List<BattleEntity> leftEntities = fightBefore.getLeftEntities();
		List<BattleEntity> rightEntities = fightBefore.getRightEntities();
		for (int i = 0; i < teamALen; i++) {
			battleEntity = teamAEntities.get(i);
			if (battleEntity == null) {
				continue;
			}
			BattleEntity data = battleEntity.cloneData();
			leftEntities.add(data);
		}

		for (int i = 0; i < teamBLen; i++) {
			battleEntity = teamBEntities.get(i);
			if (battleEntity == null) {
				continue;
			}
			BattleEntity data = battleEntity.cloneData();
			rightEntities.add(data);
		}

		// 战中信息
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		ArrayList<AttackInfo> checkLeft = new ArrayList<AttackInfo>();
		for (int i = 0; i < leftAttackInfos.size() && i < 500; i++) {
			checkLeft.add(leftAttackInfos.get(i));
		}
		fightIn.setLeftInfo(checkLeft);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		ArrayList<AttackInfo> checkRight = new ArrayList<AttackInfo>();
		for (int i = 0; i < rightAttackInfos.size() && i < 500; i++) {
			checkRight.add(rightAttackInfos.get(i));
		}
		fightIn.setRightInfo(checkRight);

		reportMsg.setFightBefore(fightBefore);
		reportMsg.setFightIn(fightIn);
		reportMsg.addAwards(awards);
		handleHeroExp(reportMsg, heroAddExp);

		return reportMsg;
	}

	public ReportMsg createReportMsg(Team teamA, Team teamB) {
		ReportMsg reportMsg = new ReportMsg();
		// 战前信息
		FightBefore fightBefore = new FightBefore();
		ArrayList<BattleEntity> teamAEntities = teamA.getAllEnities();
		ArrayList<BattleEntity> teamBEntities = teamB.getAllEnities();
		int teamALen = teamAEntities.size();
		int teamBLen = teamBEntities.size();

		BattleEntity battleEntity;
		List<BattleEntity> leftEntities = fightBefore.getLeftEntities();
		List<BattleEntity> rightEntities = fightBefore.getRightEntities();
		for (int i = 0; i < teamALen; i++) {
			battleEntity = teamAEntities.get(i);
			if (battleEntity == null) {
				continue;
			}
			BattleEntity data = battleEntity.cloneData();
			leftEntities.add(data);
		}

		for (int i = 0; i < teamBLen; i++) {
			battleEntity = teamBEntities.get(i);
			if (battleEntity == null) {
				continue;
			}
			BattleEntity data = battleEntity.cloneData();
			rightEntities.add(data);
		}

		// 战中信息
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		ArrayList<AttackInfo> checkLeft = new ArrayList<AttackInfo>();
		for (int i = 0; i < leftAttackInfos.size() && i < 500; i++) {
			checkLeft.add(leftAttackInfos.get(i));
		}
		fightIn.setLeftInfo(checkLeft);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		ArrayList<AttackInfo> checkRight = new ArrayList<AttackInfo>();
		for (int i = 0; i < rightAttackInfos.size() && i < 500; i++) {
			checkRight.add(rightAttackInfos.get(i));
		}
		fightIn.setRightInfo(checkRight);

		reportMsg.setFightBefore(fightBefore);
		reportMsg.setFightIn(fightIn);

		return reportMsg;
	}

	// 创建闪电战邮件
	public Report createQuickWarReport(Team teamA, Team teamB, Player player, Player target) {
		Report report = new Report();
		report.setKeyId(System.currentTimeMillis());
		report.setResult(teamA.isWin());

		// 战报头信息: 成功
		ReportHead leftHead = createPlayerReportHead(player, teamA, player.getPos());
		report.setLeftHead(leftHead);

		// 失败
		ReportHead rightHead = createPlayerReportHead(target, teamB, target.getPos());
		report.setRightHead(rightHead);

		List<Attender> attackers = createAttender(teamA, WarType.ATTACK_QUICK);
		report.setLeftAttender(attackers);
		List<Attender> defenders = createAttender(teamB, WarType.ATTACK_QUICK);
		report.setRightAttender(defenders);

		// 战斗回放
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		fightIn.setLeftInfo(leftAttackInfos);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		fightIn.setRightInfo(rightAttackInfos);

		return report;
	}

	public Report createCollectWarReport(Team teamA, Team teamB, Player player, Player target) {
		Report report = new Report();
		report.setKeyId(System.currentTimeMillis());
		report.setResult(teamA.isWin());

		// 战报头信息: 成功
		ReportHead leftHead = createPlayerReportHead(player, teamA, player.getPos());
		report.setLeftHead(leftHead);

		// 失败
		ReportHead rightHead = createPlayerReportHead(target, teamB, target.getPos());
		report.setRightHead(rightHead);

		List<Attender> attackers = createAttender(teamA, WarType.ATTACK_QUICK);
		report.setLeftAttender(attackers);
		List<Attender> defenders = createAttender(teamB, WarType.ATTACK_QUICK);
		report.setRightAttender(defenders);

		// 战斗回放
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		fightIn.setLeftInfo(leftAttackInfos);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		fightIn.setRightInfo(rightAttackInfos);

		return report;
	}


	/**
	 * 参加活动杀怪 有伤兵恢复
	 *
	 * @param teamA
	 * @param teamB
	 * @param player
	 * @param monster
	 * @param awards
	 * @param heroAddExp
	 * @param iron
	 * @param copper
	 * @param soldierRecMap
	 * @param percent
	 */
	public void handleSendKillMonster(Team teamA, Team teamB, Player player, Monster monster, List<Award> awards, HeroAddExp heroAddExp, int iron, int copper, HashMap<Integer, Integer> soldierRecMap, float percent) {
		Report report = createKillMonsterReport(teamA, teamB, player, monster);
		ReportMsg reportMsg = createReportMsg(teamA, teamB, awards, heroAddExp);
		if (report.isResult()) {  // 成功邮件
			String name = player.getNick();
			String pos = player.getPosStr();
			String lv = String.valueOf(monster.getLevel());
			String monsterPosStr = monster.getPosStr();
			String ironStr = String.valueOf(iron);
			String copperStr = String.valueOf(copper);
			//添加worldMonster的ID
			String worldMonster = String.valueOf(monster.getId());
			playerManager.sendReportMailOnActivity(player, report, reportMsg, MailId.KILL_REBEL_WIN, awards, soldierRecMap, percent, name, pos, lv, monsterPosStr, name, ironStr, copperStr, worldMonster);

		} else {  // 失败邮件
			String name = player.getNick();
			if (name == null) {
				name = "unkown";
			}
			String pos = player.getPosStr();
			String lv = String.valueOf(monster.getLevel());
			String monsterPosStr = monster.getPosStr();
			//添加worldMonster的ID
			String worldMonster = String.valueOf(monster.getId());
			playerManager.sendReportMailOnActivity(player, report, reportMsg, MailId.KILL_REBEL_FAILED, awards, soldierRecMap, percent, name, pos, lv, monsterPosStr, worldMonster);
		}
	}

	/**
	 * 不参加活动的杀怪
	 *
	 * @param teamA
	 * @param teamB
	 * @param player
	 * @param monster
	 * @param awards
	 * @param heroAddExp
	 * @param iron
	 * @param copper
	 * @param soldierRecMap
	 */
	public void handleSendKillMonster(Team teamA, Team teamB, Player player, Monster monster, List<Award> awards, HeroAddExp heroAddExp, int iron, int copper, HashMap<Integer, Integer> soldierRecMap) {
		Report report = createKillMonsterReport(teamA, teamB, player, monster);
		ReportMsg reportMsg = createReportMsg(teamA, teamB, awards, heroAddExp);
		if (report.isResult()) {  // 成功邮件
			String name = player.getNick();
			String pos = player.getPosStr();
			String lv = String.valueOf(monster.getLevel());
			String monsterPosStr = monster.getPosStr();
			String ironStr = String.valueOf(iron);
			String copperStr = String.valueOf(copper);
			//添加worldMonster的ID
			String worldMonster = String.valueOf(monster.getId());
			playerManager.sendReportMail(player, report, reportMsg, MailId.KILL_REBEL_WIN, awards, soldierRecMap, name, pos, lv, monsterPosStr, name, ironStr, copperStr, worldMonster);

		} else {  // 失败邮件
			String name = player.getNick();
			if (name == null) {
				name = "unkown";
			}
			String pos = player.getPosStr();
			String lv = String.valueOf(monster.getLevel());
			String monsterPosStr = monster.getPosStr();
			//添加worldMonster的ID
			String worldMonster = String.valueOf(monster.getId());
			playerManager.sendReportMail(player, report, reportMsg, MailId.KILL_REBEL_FAILED, awards, soldierRecMap, name, pos, lv, monsterPosStr, worldMonster);
		}
	}


	public void handleSendKillStaffMonster(Team teamA, Team teamB, Player player, Monster monster, List<Award> awards, HeroAddExp heroAddExp, int iron, int copper, int soldierNum, HashMap<Integer, Integer> soldierRecMap) {
		Report report = createKillMonsterReport(teamA, teamB, player, monster);
		ReportMsg reportMsg = createReportMsg(teamA, teamB, awards, heroAddExp);
		if (report.isResult()) {  // 成功邮件
			String name = player.getNick();
			String pos = player.getPosStr();
			String lv = String.valueOf(monster.getLevel());
			String monsterPosStr = monster.getPosStr();
			String ironStr = String.valueOf(iron);
			String copperStr = String.valueOf(copper);
			String soldierNumStr = String.valueOf(soldierNum);
			//添加worldMonster的ID
			String worldMonster = String.valueOf(monster.getId());
			playerManager.sendReportMail(player, report, reportMsg, MailId.KILL_MEETING_SOLDIER_SUCCESS, awards, soldierRecMap, name, pos, lv, monsterPosStr, name, ironStr, copperStr, soldierNumStr, worldMonster);

		} else {  // 失败邮件
			String name = player.getNick();
			if (name == null) {
				name = "unkown";
			}
			String pos = player.getPosStr();
			String lv = String.valueOf(monster.getLevel());
			String monsterPosStr = monster.getPosStr();
			//添加worldMonster的ID
			String worldMonster = String.valueOf(monster.getId());
			playerManager.sendReportMail(player, report, reportMsg, MailId.KILL_MEETING_SOLDIER_FAIL, awards, soldierRecMap, name, pos, lv, monsterPosStr, worldMonster);
		}
	}

	/**
	 * 闪电战城战报告
	 */
	public void handleSendQuickWar(Team teamA, Team teamB,
		Player player, Player target,
		List<Award> awards, List<Award> got, HeroAddExp heroAddExp,
		WarInfo warInfo, HashMap<Integer, Integer> acttackerRec,
		HashMap<Integer, Integer> defenceRec,
		HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		Report report = createQuickWarReport(teamA, teamB, player, target);
		ReportMsg reportMsg = createReportMsg(teamA, teamB, got, heroAddExp);
		List<CommonPb.Award> lostAward = new ArrayList<>();
		List<CommonPb.Award> robotAward = new ArrayList<>();
		awards.forEach(e -> {
			lostAward.add(PbHelper.createAward(e.getType(), e.getId(), e.getCount()).build());
		});
		got.forEach(e -> {
			robotAward.add(PbHelper.createAward(e.getType(), e.getId(), e.getCount()).build());
		});
		String attackSoldier = report.getLeftHead().toString();
		for (Attender e : report.getLeftAttender()) {
			attackSoldier += e.toString();
		}
		String defenceSoldier = report.getRightHead().toString();
		for (Attender e : report.getRightAttender()) {
			defenceSoldier += e.toString();
		}
		SpringUtil.getBean(com.game.log.LogUser.class).battle_log(BattleLog.builder()
			.attacker(player.roleId)
			.defencer(target.roleId)
			.attackPos(player.getPosStr())
			.defencePos(target.getPosStr())
			.serverId(player.account.getServerId())
			.channel(player.account.getChannel())
			.attackSoldier(attackSoldier)
			.defenceSoldier(defenceSoldier)
			.robots(robotAward)
			.lost(lostAward)
			.build());

		List<Award> tempList = new ArrayList<Award>();
		ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();
		HashSet<Long> players = new HashSet<Long>();
		// 防守者
		if (defencer != null) {
			for (March march : defencer) {
				long lordId = march.getLordId();
				if (lordId == target.getRoleId()) {
					continue;
				}
				Player assist = playerManager.getPlayer(lordId);
				if (assist == null) {
					continue;
				}
				players.add(assist.roleId);
			}
		}

		if (report.isResult()) { // 城战成功
			// 您成功击飞%s的基地/n%s[%s]城战进攻 %s[%s]/n本次战斗中，%s获得：/n生铁X%s 黄铜X%s 石油X%s
			// 人口X%s
			String flyPlayer = target.getNick();
			String name = player.getNick();
			String pos = player.getPosStr();
			String targetName = target.getNick();
			String targetPos = target.getOldPosStr();

			String[] resource = parseRobAward(awards);
			String[] gotResource = parseRobAward(got);
			// A 进攻成功邮件
			playerManager.sendReportMail(player, report, reportMsg, MailId.CITY_WAR_WIN,
				got, acttackerRec, flyPlayer, name, pos, targetName, targetPos, name,
				gotResource[0], gotResource[1], gotResource[2], gotResource[3]);
			// B 防守失败邮件
			String[] param = {name, name, pos, targetName, targetPos, resource[0], resource[1], resource[2], resource[3]};
			report.setResult(!report.isResult());
			playerManager.sendReportMail(target, report, reportMsg, MailId.CITY_DEFENCE_FAILED, new ArrayList<Award>(), defenceRec, param);

			// 支援的防守失败邮件
			// 防守失败
			String[] helpParam = {name, name, pos, targetName, targetPos, "0", "0", "0", "0"};
			for (Long lordId : players) {
				Player assist = playerManager.getPlayer(lordId);
				if (assist != null) {
					HashMap<Integer, Integer> soldierRec = getSoldierRecMap(allSoldierRec, lordId);
					playerManager.sendReportMail(assist, report, reportMsg, MailId.CITY_DEFENCE_FAILED, new ArrayList<Award>(), soldierRec, helpParam);
				}
			}

		} else { // 城战失败
			// 您的城战进攻失败%s[%s]城战进攻%s[%s]
			String[] param = {player.getNick(), player.getPosStr(), target.getNick(), target.getPosStr()};
			playerManager.sendReportMail(player, report, reportMsg, MailId.CITY_ATTACK_FAILED, awards, acttackerRec, param);
			report.setResult(!report.isResult());
			playerManager.sendReportMail(target, report, reportMsg, MailId.CITY_DEFENCE_WIN, new ArrayList<Award>(), defenceRec, param);

			// 支援防守成功邮件
			// 防守成功邮件
			for (Long lordId : players) {
				Player assist = playerManager.getPlayer(lordId);
				if (assist != null) {
					HashMap<Integer, Integer> soldierRec = getSoldierRecMap(allSoldierRec, lordId);
					playerManager.sendReportMail(assist, report, reportMsg, MailId.CITY_DEFENCE_WIN, tempList, soldierRec, param);
				}
			}
		}
	}

	/**
	 * 远征或者奔袭成功
	 */
	public void sendCityWinWarReport(WarInfo warInfo, Team teamA, Team teamB, List<Award> lostAward, List<Award> callerAward, List<Award> othersAward,
		HeroAddExp heroAddExp, HashMap<Integer, Integer> defenceRec, HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		Report report = createReport(warInfo, teamA, teamB);
		ReportMsg reportMsg = createReportMsg(teamA, teamB, callerAward, heroAddExp);
		List<CommonPb.Award> lAward = new ArrayList<>();
		List<CommonPb.Award> robotAward = new ArrayList<>();
		lostAward.forEach(e -> {
			lAward.add(PbHelper.createAward(e.getType(), e.getId(), e.getCount()).build());
		});
		callerAward.forEach(e -> {
			robotAward.add(PbHelper.createAward(e.getType(), e.getId(), e.getCount()).build());
		});
		String attackSoldier = report.getLeftHead().toString();
		for (Attender e : report.getLeftAttender()) {
			attackSoldier += e.toString();
		}
		String defenceSoldier = report.getRightHead().toString();
		for (Attender e : report.getRightAttender()) {
			defenceSoldier += e.toString();
		}

		ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
		ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();

		Player player = playerManager.getPlayer(warInfo.getAttackerId());
		Player target = playerManager.getPlayer(warInfo.getDefencerId());

		// 您成功击飞%s的基地/n%s[%s]城战进攻 %s[%s]/n本次战斗中，你获得：/n生铁X%s 黄铜X%s 石油X%s
		// 人口X%s
		String name = player.getNick();
		String pos = player.getPosStr();
		String targetName = target.getNick();
		String targetPos = target.getOldPosStr();

		String[] lost = parseRobAward(lostAward);
		String[] caller = parseRobAward(callerAward);
//        String[] otherer = parseRobAward(othersAward);

		String[] callParam = {targetName, name, pos, targetName, targetPos, name, caller[0], caller[1], caller[2], caller[3]};

		// A 进攻成功
		HashMap<Integer, Integer> attackRec = getSoldierRecMap(allSoldierRec, player.roleId);
		playerManager.sendReportMail(player, report, reportMsg, MailId.CITY_WAR_WIN, callerAward, attackRec, callParam);

		// B 防守失败邮件
		String[] lostParam = {name, name, pos, targetName, targetPos, lost[0], lost[1], lost[2], lost[3]};
		String[] helpParam = {name, name, pos, targetName, targetPos, "0", "0", "0", "0"};

		SpringUtil.getBean(com.game.log.LogUser.class).battle_log(BattleLog.builder()
			.attacker(warInfo.getAttackerId())
			.defencer(warInfo.getDefencerId())
			.attackPos(player.getPosStr())
			.defencePos(target.getPosStr())
			.serverId(player.account.getServerId())
			.channel(player.account.getChannel())
			.attackSoldier(attackSoldier)
			.defenceSoldier(defenceSoldier)
			.robots(robotAward)
			.lost(lAward)
			.build());

		// 其他进攻者的邮件
		Map<Long, Boolean> alreadyMap = new HashMap<>();
		for (March march : attacker) {
			long lordId = march.getLordId();
			Player p = playerManager.getPlayer(lordId);
			if (p == null) {
				continue;
			}

			if (march.getLordId() == warInfo.getAttackerId()) {
				continue;
			}
			if (alreadyMap.containsKey(lordId)) {
				continue;
			}
			alreadyMap.put(lordId, true);
			othersAward = worldManager.getRobBuyWareHouser(p, othersAward);
			String[] otherer = parseRobAward(othersAward);
			HashMap<Integer, Integer> rec = getSoldierRecMap(allSoldierRec, lordId);
			String[] otherParam = {targetName, name, pos, targetName, targetPos, p.getNick(), otherer[0], otherer[1], otherer[2], otherer[3]};
			playerManager.sendReportMail(p, report, reportMsg, MailId.CITY_WAR_WIN, othersAward, rec, otherParam);
		}

		report.setResult(!report.isResult());
		playerManager.sendReportMail(target, report, reportMsg, MailId.CITY_DEFENCE_FAILED, new ArrayList<Award>(), defenceRec, lostParam);
		// 防守失败
		for (March march : defencer) {
			long lordId = march.getLordId();
			Player p = playerManager.getPlayer(lordId);
			if (p == null) {
				continue;
			}
			HashMap<Integer, Integer> rec = getSoldierRecMap(allSoldierRec, lordId);
			playerManager.sendReportMail(p, report, reportMsg, MailId.CITY_DEFENCE_FAILED, new ArrayList<Award>(), rec, helpParam);
		}

	}

	public HashMap<Integer, Integer> getSoldierRecMap(HashBasedTable<Long, Integer, Integer> allSoldierRec, long roleId) {
		HashMap<Integer, Integer> soldierRec = new HashMap<Integer, Integer>();
		for (int i = 1; i <= 3; i++) {
			Integer soldierNum = allSoldierRec.get(roleId, i);
			if (soldierNum == null) {
				continue;
			}
			soldierRec.put(i, soldierNum);
		}

		return soldierRec;
	}

	/**
	 * （长途，奔袭）城战进攻失败邮件
	 *
	 * @param warInfo
	 * @param teamA
	 * @param teamB
	 * @param heroAddExp
	 */
	public void sendCityFailWarReport(WarInfo warInfo, Team teamA, Team teamB, HeroAddExp heroAddExp,
		HashMap<Integer, Integer> defenceRec,
		HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		List<Award> tempList = new ArrayList<Award>();
		Report report = createReport(warInfo, teamA, teamB);
		ReportMsg reportMsg = createReportMsg(teamA, teamB, tempList, heroAddExp);

		ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();
		ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();

		Player player = playerManager.getPlayer(warInfo.getAttackerId());

		Player target = playerManager.getPlayer(warInfo.getDefencerId());
		String[] param = new String[]{player.getNick(), player.getPosStr(), target.getNick(), target.getPosStr()};

		// 进攻者失败
		HashMap<Integer, Integer> attackRec = getSoldierRecMap(allSoldierRec, player.roleId);
		playerManager.sendReportMail(player, report, reportMsg, MailId.CITY_ATTACK_FAILED, tempList, attackRec, param);

		// 防守成功
		playerManager.sendReportMail(target, report, reportMsg, MailId.CITY_DEFENCE_WIN, tempList, defenceRec, param);

		// 协助进攻
		HashSet<Long> attckers = new HashSet<Long>();
		for (March march : attacker) {
			long lordId = march.getLordId();
			Player p = playerManager.getPlayer(lordId);
			if (p == null || lordId == warInfo.getAttackerId()) {
				continue;
			}
			attckers.add(p.roleId);
		}

		for (Long lordId : attckers) {
			Player p = playerManager.getPlayer(lordId);
			if (p == null) {
				continue;
			}
			HashMap<Integer, Integer> soldierRec = getSoldierRecMap(allSoldierRec, lordId);
			playerManager.sendReportMail(p, report, reportMsg, MailId.CITY_ATTACK_FAILED, tempList, soldierRec, param);
		}

		// 防守成功邮件
		HashSet<Long> defencers = new HashSet<Long>();
		for (March march : defencer) {
			long lordId = march.getLordId();
			Player p = playerManager.getPlayer(lordId);
			if (p == null || lordId == warInfo.getAttackerId()) {
				continue;
			}
			defencers.add(p.roleId);
		}

		for (Long lordId : defencers) {
			Player p = playerManager.getPlayer(lordId);
			if (p == null || lordId == warInfo.getDefencerId()) {
				continue;
			}
			HashMap<Integer, Integer> soldierRec = getSoldierRecMap(allSoldierRec, lordId);
			playerManager.sendReportMail(p, report, reportMsg, MailId.CITY_DEFENCE_WIN, tempList, soldierRec, param);
		}

	}

	// mailId = 2 , 城战报告 %s 对您发起城战，在城战中因您实力不敌被击飞。随机迁移到其他地图
	public void sendCityReport(Player target, Player player) {
		String name = player.getNick();
		playerManager.sendNormalMail(target, MailId.MAIL_FLY, name);
	}

	// mailId = 3, 敌方侦察成功, 侦察来源:[%s]Lv.%s %s [%s]报告指挥官,敌方成功侦察了我军的信息,请留意敌军的行动！
	public Mail sendScotMain(Player target, Player receiver) {
		String country = String.valueOf(target.getCountry());
		String level = String.valueOf(target.getLevel());
		String name = String.valueOf(target.getNick());
		String posStr = String.valueOf(target.getPosStr());
		return playerManager.sendNormalMail(receiver, MailId.SCOTED_MAIL, country, level, name, posStr);
	}

	// mailId = 24 报告指挥官，我方成功抵御了对方一次侦察行动
	public Mail sendScotTargetFailed(Player player, Player target) {
		String country = String.valueOf(player.getCountry());
		String level = String.valueOf(player.getLevel());
		String name = String.valueOf(player.getNick());
		String posStr = String.valueOf(player.getPosStr());
		return playerManager.sendNormalMail(target, MailId.SCOT_TARGET_FAILED, country, level, name, posStr);
	}

	// mailId = 27 报告指挥官，"%s"在众多候选人中军衔最高，成为%s的城主，任期%s天，现在返回您的竞选物资，请查收!生铁：%s
	// 黄铜：%s
	public void sendElectionFailed(Player player, Player owner, List<Award> awards, int cityId, int mailId) {
		int iron = 0;
		int copper = 0;
		for (Award award : awards) {
			if (award.getId() == ResourceType.IRON) {
				iron += award.getCount();
			} else if (award.getId() == ResourceType.COPPER) {
				copper += award.getCount();
			}
		}

		String ownerName = owner != null ? owner.getNick() : "";
		String cityName = cityManager.getCityName(cityId);
		String ironStr = String.valueOf(iron);
		String copperStr = String.valueOf(copper);
		// MailId.CITY_ELECTION_FAILED
		if (mailId == MailId.CITY_ELECTIONOTHER_FAILED) {
			playerManager.sendAttachMail(player, awards, mailId, ironStr, copperStr);
			return;
		}
		playerManager.sendAttachMail(player, awards, mailId, ownerName, cityName, ironStr, copperStr);
	}

	// mailId = 35, 报告指挥官，您在众多候选人中军衔最高，成为%s的城主。任期%s天，城主每天%s点可通过邮件获取生产的物资%s份！
	public void sendElectionOk(Player player, int cityId) {
		String cityName = cityManager.getCityName(cityId);
		playerManager.sendNormalMail(player, MailId.CITY_ELECTION_OK, cityName);
	}


	/**
	 * @param awardList
	 * @return
	 */
	public String[] parseRobAward(List<Award> awardList) {
		String[] robs = new String[4];// 铁，铜，油，人口
		long[] temp = new long[4];
		for (int i = 0; i < 4; i++) {
			temp[i] = 0L;
		}

		if (awardList == null || awardList.isEmpty()) {
			for (int i = 0; i < 4; i++) {
				robs[i] = String.valueOf(temp[i]);
			}
			return robs;
		}

		for (Award award : awardList) {
			if (award.getType() == AwardType.RESOURCE && award.getId() == ResourceType.IRON) {
				temp[0] += +award.getCount();
			} else if (award.getType() == AwardType.RESOURCE && award.getId() == ResourceType.COPPER) {
				temp[1] += +award.getCount();
			} else if (award.getType() == AwardType.RESOURCE && award.getId() == ResourceType.OIL) {
				temp[2] += +award.getCount();
			} else if (award.getType() == AwardType.PERSON) {
				temp[3] += +award.getCount();
			}
		}

		for (int i = 0; i < 4; i++) {
			robs[i] = String.valueOf(temp[i]);
		}
		return robs;
	}

	@Autowired
	StaticSuperResMgr staticSuperResMgr;

	// 采集邮件,6,28,29,51
	// 本次采集：%s*%s/n采集位置 [%s]/n你的将领圆满完成任务，现已回城/n采集时间：
	// %s小时%s分钟/n采集获得：/n%sx%s（采集加成）/nLv.%s %s 经验+%s
	public void sendCollectDone(int mailId, Entity resource, long period,
		long count, int heroId, int heroLv,
		Player player, boolean isWin,
		Player target) {
		if (resource == null) {
			return;
		}

		CommonPb.MailCollectRes.Builder collectRes = CommonPb.MailCollectRes.newBuilder();
		if (resource.getPos() != null) {
			collectRes.setPos(resource.getPos().wrapPb());
		}

		int minutes = (int) (period / TimeHelper.MINUTE_MS);
		collectRes.setPeriod(minutes);
		int resType = 0;
		if (resource.getEntityType() == EntityType.Resource) {
			StaticWorldResource config = staticWorldMgr.getStaticWorldResource((int) resource.getId());
			if (config != null) {
				resType = config.getType();
			}
		} else if (resource.getEntityType() == EntityType.BIG_RESOURCE) {
			StaticSuperRes staticSuperRes = staticSuperResMgr.getStaticSuperRes((int) resource.getId());
			if (staticSuperRes != null) {
				resType = staticSuperRes.getResType();
			}
		} else {
			resType = 6;
		}
		collectRes.setResType(resType);
		collectRes.setHeroId(heroId);
		int heroExp = minutes * staticLimitMgr.getNum(113);
		Hero hero = player.getHero(heroId);
		if (hero != null) {
			heroManager.addExp(hero, player, heroExp, Reason.WORLD_RESOURCE_COLLECT);
		}
		collectRes.setHeroExp(heroExp);
		collectRes.setHeroLv(heroLv);
		collectRes.setResNum((int) count);
		if (target != null) {
			collectRes.setIsWin(isWin);
			if (target.getNick() != null) {
				collectRes.setAttackerName(target.getNick());
			}

			if (target.getPos() != null) {
				collectRes.setAttackerPos(target.getPos().wrapPb());
			}
			collectRes.setAttackerId(target.roleId);
		}

		Mail mail = null;
		if (mailId == MailId.COLLECT_BREAK) {
			if (0 != resType) {
				mail = mailManager.addMail(player, mailId, String.valueOf(resType), String.valueOf(count));
			} else {
				mail = mailManager.addMail(player, mailId);
			}
		} else {
			mail = mailManager.addMail(player, mailId);
		}
		if (mail == null) {
			LogHelper.CONFIG_LOGGER.info("sendReport mail failed, mail = null, mailId = " + mailId);
			return;
		}
		mail.setMailCollectRes(collectRes.build());
		mail.setAwardGot(2); // 如果为空则邮件got为2
		// 处理采集方邮件
		mail.setPortrait(target != null ? target.getPortrait() : player.getPortrait());
		playerManager.synMailToPlayer(player, mail);
		// 武将经验值
		playerManager.synHeroChange(player, heroId, Reason.WORLD_RESOURCE_COLLECT);

		SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder()
			.lordId(mail.getLordId())
			.mailId(mail.getMailId())
			.nick(player.getNick())
			.vip(player.getVip())
			.level(player.getLevel())
			.msg(mailManager.mailToString(mail))
			.build());
	}

	// 39 目标（%s）开启了保护，我方城战军解散。
	public void sendProtectedMail(Player player, Player target) {
		playerManager.sendNormalMail(player, MailId.PROTECT_MARCH_RETRUN, target.getNick());
	}

	// 49 %s%s已取消对%s%s[%s]的进攻，我方城战防守部队解散(远征或者奔袭)
	// 国家-玩家-国家-玩家-[坐标]
	public void sendCancelWar(Player attcker, Player defencer, Player player, int mailId) {
		if (attcker == null || defencer == null) {
			LogHelper.CONFIG_LOGGER.info("attcker == null || defencer == null in sendCancelWar!");
			return;
		}
		String attackCountry = String.valueOf(attcker.getCountry());
		String attckName = String.valueOf(attcker.getNick());

		String defenceCountry = String.valueOf(defencer.getCountry());
		String defenceName = String.valueOf(defencer.getNick());
		String posStr = defencer.getPosStr();
		playerManager.sendNormalMail(player, mailId, attackCountry, attckName, defenceCountry, defenceName, posStr);
	}

	// 暴乱邮件
	// %s 进攻 %s[%s,%s] | 只针对防守方
	// 第一个%s: 西凉军 或者 黄巾军
	// 第二个%s: 玩家名
	// 第三个%s: 玩家坐标x
	// 第四个%s: 玩家坐标y
	// 其余就是: 城防军玩家姓名, 获得的物品
	// 需要生成战报和邮件信息
	// 创建击杀流寇的邮件
	// 玩家, 奖励列表
	public void riotMail(Team teamA,
		Team teamB,
		Player player,
		int worldMonsterId,
		List<Award> awards,
		HeroAddExp heroAddExp,
		HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		StaticWorldMonster worldMonster = staticWorldMgr.getMonster(worldMonsterId);
		HashSet<Long> allPlayers = new HashSet<Long>();
		teamA.getAllEnities().forEach(e -> {
			if (e.getLordId() != 0 && !allPlayers.contains(e.getLordId())) {
				allPlayers.add(e.getLordId());
			}
		});
		//无人防守
		if (allPlayers.size() == 0) {
			allPlayers.add(player.getLord().getLordId());
		}
		//积分奖励
		List<CommonPb.Award> playerAward = new ArrayList<>();
		if (worldMonster.getDropList().size() > 0 && teamA.isWin()) {
			worldMonster.getDropList().forEach(e -> {
				if (e.size() >= 3) {
//                    playerManager.addAward(player, e.get(0), e.get(1), e.get(2), Reason.RIOT_WAVE);
					playerAward.add(PbHelper.createAward(e.get(0), e.get(1), e.get(2)).build());
				}
			});
		}

		StaticRiotAward staticRiotAward = staticRiotMgr.getRiotAwardByWave(player.getSimpleData().getAttackWave() + 1);
		for (Long lordId : allPlayers) {
			Player p = playerManager.getPlayer(lordId);
			if (p == null) {
				continue;
			}
			//计算分数  波次基础分+杀敌分
			CommonPb.TwoInt pAward = getAwards(teamA, p, staticRiotAward);
			CommonPb.RiotAssist.Builder builder = CommonPb.RiotAssist.newBuilder();
			if (p.getNick() != null) {
				builder.setNick(player.getNick());
			} else {
				builder.setNick("unkown");
			}
			builder.addAward(PbHelper.createAward(AwardType.RESOURCE, ResourceType.IRON, pAward.getV1()));
			builder.addAward(PbHelper.createAward(AwardType.RESOURCE, ResourceType.COPPER, pAward.getV2()));
			int riotCoin = 0;
			boolean hasCoin = false;
			if (p.getLord().getLordId() == player.getLord().getLordId() && playerAward.size() > 0) {
				hasCoin = true;
				for (CommonPb.Award a : playerAward) {
					if (a.getType() == AwardType.RIOT_SCORE) {
						riotCoin += a.getCount();
					}
					builder.addAward(a);
				}
			}
			List<Award> mailRewards = new ArrayList<>();
			builder.getAwardList().forEach(e -> {
				playerManager.addAward(p, e.getType(), e.getId(), e.getCount(), Reason.RIOT_WAVE);
				mailRewards.add(new Award(e.getType(), e.getId(), e.getCount()));
			});

			ReportMsg reportMsg = createReportMsg(teamB, teamA, mailRewards, heroAddExp);

			HashMap<Integer, Integer> attackRec = getSoldierRecMap(allSoldierRec, player.roleId);

			Report report = createDefenceRiotReport(teamB, teamA, p);  // 这个战报和杀叛军的一样
			if (hasCoin) {
				int mailId = report.isResult() ? MailId.DEFENCE_RIOT_WIN_HAS_COIN : MailId.DEFENCE_RIOT_FAIL;
				playerManager.sendRoitReportMail(p, report, reportMsg, mailId, mailRewards, attackRec, new ArrayList<>(),
					worldMonster.getName(),
					player.getNick(), player.getPosStr(), p.getNick(),
					String.valueOf(pAward.getV1()), String.valueOf(pAward.getV2()), String.valueOf(riotCoin));
			} else {
				int mailId = report.isResult() ? MailId.DEFENCE_RIOT_WIN_NO_COIN : MailId.DEFENCE_RIOT_FAIL;
				playerManager.sendRoitReportMail(p, report, reportMsg, mailId, mailRewards, attackRec, new ArrayList<>(),
					worldMonster.getName(),
					player.getNick(), player.getPosStr(), p.getNick(),
					String.valueOf(pAward.getV1()), String.valueOf(pAward.getV2()));
			}
			// 同步玩家变化
			playerManager.synChange(p, Reason.RIOT_WAVE);
		}
	}

	/**
	 * 计算奖励
	 *
	 * @param team
	 * @param player
	 * @return
	 */
	private CommonPb.TwoInt getAwards(Team team, Player player, StaticRiotAward award) {
		List<CommonPb.RiotAssist> results = new ArrayList<>();
		int awardIron = 0;
		int awardCopper = 0;
		if (award != null) {
			//基础波次奖励
			awardIron += award.getIron();
			awardCopper += award.getCopper();
		}
		//杀敌数奖励
		int killNum = riotManager.getKillNum(team, player);
		List<StaticRiotAward> riotAwardList = staticRiotMgr.getRiotAwardList();
		for (StaticRiotAward riotAward : riotAwardList) {
			if (killNum >= riotAward.getKillNum()) {
				awardIron += riotAward.getIron();
				awardCopper += riotAward.getCopper();
			}
		}
		return CommonPb.TwoInt.newBuilder().setV1(awardIron).setV2(awardCopper).build();
	}

	// 创建击杀流寇的邮件
	public Report createDefenceRiotReport(Team teamA, Team teamB, Player player) {
		Report report = new Report();
		report.setKeyId(System.currentTimeMillis());
		report.setResult(teamB.isWin());

		// 战报头信息: 成功
		ReportHead leftHead = createMonsterHead(teamA, new Pos());
		report.setLeftHead(leftHead);

		// 失败

		ReportHead rightHead = createPlayerReportHead(player, teamB, player.getPos());
		report.setRightHead(rightHead);

		List<Attender> attackers = createRebelAttender(teamA);
		report.setLeftAttender(attackers);
		List<Attender> defenders = createRebelAttender(teamB);
		report.setRightAttender(defenders);

		// 战斗回放
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		fightIn.setLeftInfo(leftAttackInfos);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		fightIn.setRightInfo(rightAttackInfos);

		return report;
	}

	/**
	 * 伏击叛军邮件
	 *
	 * @param warInfo
	 * @param teamA
	 * @param teamB
	 * @param player
	 * @param monster
	 * @param awards
	 * @param heroAddExp
	 * @param iron
	 * @param copper
	 * @param allSoldierRec
	 */
	public void handleSendKillRebelMonster(WarInfo warInfo, Team teamA, Team teamB, Player player, Monster monster, List<Award> awards, HeroAddExp heroAddExp, int iron, int copper, HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		Report report = createKillRebelMonsterReport(warInfo, teamA, teamB, player, monster);
		ReportMsg reportMsg = createReportMsg(teamA, teamB, awards, heroAddExp);
		StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster((int) monster.getId());
		boolean flag = false;
		StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(WorldActivityConsts.ACTIVITY_2);
		float percent = staticWorldActPlan.getContinues().get(0) * 1.0f / 100;
		if (report.isResult()) {  // 成功邮件
			String monsterPosStr = monster.getPosStr();
			String ironStr = String.valueOf(iron);
			String copperStr = String.valueOf(copper);
			for (Long lordId : warInfo.getAttackerPlayers()) {
				Player attender = playerManager.getPlayer(lordId);
				String name = attender.getNick();
				String pos = attender.getPosStr();
				if (attender == null) {
					continue;
				}
				if (warInfo.getAttackerId() == player.getLord().getLordId()) {
					flag = true;
				}
				Award score = new Award();
				List<Award> drop = new ArrayList<>();
				for (Award award : awards) {
					if (award.getType() == AwardType.REBEL_SCORE) {
						score = award;
					} else {
						drop.add(award);
					}
				}
				reportMsg.addAwards(drop);
				HashMap<Integer, Integer> attackRec = getSoldierRecMap(allSoldierRec, lordId);
				playerManager.sendReportMailOnActivity(attender, report, reportMsg, MailId.ATTACK_REBEL_MONSTER_SUCCESS, drop, attackRec, percent, name, pos, staticWorldMonster.getName(), monsterPosStr, name, ironStr, copperStr, String.valueOf(score.getCount()));
			}
			if (!flag) {
				String name = player.getNick();
				String pos = player.getPosStr();
				reportMsg.addAwards(awards);
				HashMap<Integer, Integer> attackRec = getSoldierRecMap(allSoldierRec, player.getLord().getLordId());
				playerManager.sendReportMailOnActivity(player, report, reportMsg, MailId.REBEL_NOT_ATTEND_SUCCESS, null, attackRec, percent, name, pos, staticWorldMonster.getName(), monsterPosStr);
			}
		} else {  // 失败邮件
			String monsterPosStr = monster.getPosStr();
			for (Long lordId : warInfo.getAttackerPlayers()) {
				Player attender = playerManager.getPlayer(lordId);
				String name = attender.getNick();
				String pos = attender.getPosStr();
				if (attender == null) {
					continue;
				}
				if (warInfo.getAttackerId() == player.getLord().getLordId()) {
					flag = true;
				}
				reportMsg.addAwards(awards);
				HashMap<Integer, Integer> attackRec = getSoldierRecMap(allSoldierRec, lordId);
				playerManager.sendReportMail(attender, report, reportMsg, MailId.ATTACK_REBEL_MONSTER_FAIL, awards, attackRec, name, pos, staticWorldMonster.getName(), monsterPosStr);
			}
			if (!flag) {
				String name = player.getNick();
				String pos = player.getPosStr();
				reportMsg.addAwards(awards);
				HashMap<Integer, Integer> attackRec = getSoldierRecMap(allSoldierRec, player.getLord().getLordId());
				playerManager.sendReportMailOnActivity(player, report, reportMsg, MailId.ATTACK_REBEL_MONSTER_FAIL, awards, attackRec, percent, name, pos, staticWorldMonster.getName(), monsterPosStr);
			}
		}
	}

	// 创建击杀流寇的邮件
	public Report createKillRebelMonsterReport(WarInfo warInfo, Team teamA, Team teamB, Player player, Monster monster) {
		Report report = new Report();
		report.setKeyId(warInfo.getWarId());
		report.setResult(teamA.isWin());

		// 战报头信息: 成功
		ReportHead leftHead = createPlayerReportHead(player, teamA, player.getPos());
		report.setLeftHead(leftHead);

		// 失败
		ReportHead rightHead = createMonsterHead(teamB, monster.getPos());
		report.setRightHead(rightHead);

		List<Attender> attackers = createRebelAttender(teamA);
		report.setLeftAttender(attackers);
		List<Attender> defenders = createRebelAttender(teamB);
		report.setRightAttender(defenders);

		// 战斗回放
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		fightIn.setLeftInfo(leftAttackInfos);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		fightIn.setRightInfo(rightAttackInfos);

		return report;
	}

	/**
	 * 击杀虫族入侵的邮件
	 *
	 * @param teamA
	 * @param teamB
	 * @param player
	 * @param monster
	 * @return
	 */
	public Report createKillRiotMonsterReport(Team teamA, Team teamB, Player player, Monster monster) {
		Report report = new Report();
		report.setKeyId(System.currentTimeMillis());
		report.setResult(teamA.isWin());

		// 战报头信息: 成功
		ReportHead leftHead = createPlayerReportHead(player, teamA, player.getPos());
		report.setLeftHead(leftHead);

		// 失败
		ReportHead rightHead = createRoitMonsterHead(teamB, monster.getPos(), monster);
		report.setRightHead(rightHead);

		List<Attender> attackers = createRebelAttender(teamA);
		report.setLeftAttender(attackers);
		List<Attender> defenders = createRebelAttender(teamB);
		report.setRightAttender(defenders);

		// 战斗回放
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		fightIn.setLeftInfo(leftAttackInfos);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		fightIn.setRightInfo(rightAttackInfos);

		return report;
	}

	/**
	 * 虫族入侵邮件
	 *
	 * @param teamA
	 * @param teamB
	 * @param player
	 * @param monster
	 * @param awards
	 * @param heroAddExp
	 * @param iron
	 * @param copper
	 * @param soldierRecMap
	 * @param percent
	 */
	public void handleSendKillRiotMonster(Team teamA, Team teamB, Player player, Monster monster, List<Award> awards, HeroAddExp heroAddExp, int iron, int copper, HashMap<Integer, Integer> soldierRecMap, float percent) {
		Report report = createKillRiotMonsterReport(teamA, teamB, player, monster);
		ReportMsg reportMsg = createReportMsg(teamA, teamB, awards, heroAddExp);
		StaticWorldMonster staticWorldMonster = staticWorldMgr.getMonster((int) monster.getId());
		if (report.isResult()) {  // 成功邮件
			String ironStr = String.valueOf(iron);
			String copperStr = String.valueOf(copper);
			Award item = new Award();
			List<Award> drop = new ArrayList<>();
			for (Award award : awards) {
				if (award.getType() == AwardType.RIOT_ITEM) {
					item = award;
				} else {
					drop.add(award);
				}
			}
			reportMsg.addAwards(drop);
			//玩家 坐标 怪物名字 怪物坐标 玩家 金币 钢铁 信物
			playerManager.sendReportMailOnActivity(player, report, reportMsg, MailId.KILL_RIOT_WIN, awards, soldierRecMap, percent,
				player.getNick(), player.getPosStr(), staticWorldMonster.getName(), monster.getPosStr(), player.getNick(),
				ironStr, copperStr, String.valueOf(item.getCount()));
//            , monster.getId()+"",item+"",score+""
		} else {  // 失败邮件
			reportMsg.addAwards(awards);
			playerManager.sendReportMail(player, report, reportMsg, MailId.KILL_RIOT_FAIL, awards, soldierRecMap,
				player.getNick(), player.getPosStr(), staticWorldMonster.getName(), monster.getPosStr());

		}

	}
}
