package com.game.worldmap;

import com.alibaba.fastjson.JSONObject;
import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.flame.FlameWarManager;
import com.game.flame.FlameWarService;
import com.game.log.constant.*;
import com.game.log.consumer.EventManager;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.pb.BuildingPb;
import com.game.pb.BuildingPb.SynActivityDerateTimeRq;
import com.game.pb.CommonPb;
import com.game.pb.CountryPb.SynCountryHeroRq;
import com.game.service.*;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
public class WorldLogic {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private WorldManager worldManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private BattleMgr battleMgr;
	@Autowired
	private StaticWorldMgr staticWorldMgr;
	@Autowired
	private WarManager warManager;
	@Autowired
	private StaticLimitMgr staticLimitMgr;
	@Autowired
	private SoldierManager soldierManager;
	@Autowired
	private StaticPropMgr staticPropMgr;
	@Autowired
	private BattleMailManager battleMailMgr;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private CountryManager countryManager;
	@Autowired
	private StaticCountryMgr staticCountryMgr;
	@Autowired
	private HeroManager heroManager;
	@Autowired
	private TestManager testManager;
	@Autowired
	private StaticActivityMgr staticActivityMgr;
	@Autowired
	private CastleService castleService;
	@Autowired
	private WorldTargetTaskService worldTargetTaskService;
	@Autowired
	private StaticRebelMgr staticRebelMgr;
	@Autowired
	private WorldActPlanService worldActPlanService;
	@Autowired
	private TechManager techManager;
	@Autowired
	private RiotManager riotManager;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private WarBookManager bookManager;
	@Autowired
	private WorldBoxManager worldBoxManager;
	@Autowired
	private EventManager eventManager;
	@Autowired
	private RoitService roitService;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	SuperResService superResService;
	@Autowired
	FlameWarManager flameWarManager;
	@Autowired
	FlameWarService flameWarService;
	@Autowired
	MarchManager marchManager;
	@Autowired
	AchievementService achievementService;
	@Autowired
	ActivityEventManager activityEventManager;
	public void handleRebel(StaticWorldMonster staticWorldMonster, Player player, Monster monster, March march, MapInfo mapInfo) {
		List<Integer> heroIds = march.getHeroIds();
		List<Integer> monsterIds = staticWorldMonster.getMonsterIds();
		Team monsterTeam = battleMgr.initMonsterTeam(monsterIds, BattleEntityType.REBEL);
		Team playerTeam = battleMgr.initPlayerTeam(player, heroIds, BattleEntityType.HERO);
		// 随机seed不用存盘，没有回放, 种子需要发送到客户端
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		// 计算经验值
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, player.roleId);
		// 处理玩家扣血
		HashMap<Integer, Integer> solderRecMap = new HashMap<>();
		worldManager.caculatePlayer(playerTeam, player, solderRecMap);

		// 处理夜袭虫群
		int monsterLevel = monster.getLevel();
		List<Integer> addtion = staticLimitMgr.getAddtion(249);// 夜袭虫群活动覆盖的虫子的等级
		float percent = 0f;
		if (null != addtion && addtion.size() > 0 && addtion.contains(monsterLevel)) {
			percent = worldActPlanService.doActivity5(player, solderRecMap);
		}

		if (playerTeam.isWin()) {
			List<Award> awards = getMonsterAwards(player, staticWorldMonster);
			if (awards != null && !awards.isEmpty()) {
				march.addAllAwards(awards);
			}

			// 只是叛军有效
			player.setMaxMonsterLv(monster.getLevel());
			player.incrementKillMonsterNum();
			SimpleData simpleData = player.getSimpleData();
			simpleData.setKillRebelTimes(simpleData.getKillRebelTimes() + 1);

			if (testManager.isOpenTestMode()) {
				Lord lord = player.getLord();
				if (lord != null) {
					lord.setKillMonsterNum(lord.getKillMonsterNum() + 30);
				}
			}

			worldManager.doKillWorldMonster(WorldTargetType.KILL_MONSTER, player);
//            activityManager.updateActMonster(player);
			// 清除野怪
//			worldManager.clearMonsterPos(mapInfo, monster.getPos());
//			// send mail
//
//			// 同步野怪
//			worldManager.synEntityRemove(monster, mapInfo.getMapId(), monster.getPos());

			mapInfo.clearPos(monster.getPos());
			// 触发任务[只有叛军触发]
			doKillMonster(player, monster.getLevel());
			// 处理自动补兵
			// soldierManager.autoAdd(player, march.getHeroIds());
			// 发送邮件
			int iron = 0;
			int copper = 0;
			for (Award award : awards) {
				if (award.getType() == AwardType.RESOURCE && award.getId() == ResourceType.IRON) {
					iron = award.getCount();

					// 美女系统加成攻打虫族金币产出
//                    List<Integer> beautySkillEffect = beautyManager.getBeautySkillEffect(player, BeautySkillType.SPEED_UP_MONSTER_IRON);
//                    if (null != beautySkillEffect) {
//                        Integer effectValue = beautySkillEffect.get(1);
//                        double pre = effectValue / DevideFactor.PERCENT_NUM;
//                        iron = (int) (iron * (1 + pre));
//                    }

					// 获取兵书技能加成
					if (heroIds.size() == 1) {
						Hero hero = player.getHero(heroIds.get(0));
						Integer heroWarBookSkillEffect = bookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.KILL_MONSTER_ALONE);
						if (null != heroWarBookSkillEffect) {
							double pre = heroWarBookSkillEffect / 1000.0f;
							iron = (int) (iron * (1 + pre));
						}
					}

					// 金币增益道具加成
					simpleData = player.getSimpleData();
					if (simpleData != null) {
						HashMap<Integer, Buff> buffMap = simpleData.getBuffMap();
						Buff buff = buffMap.get(BuffId.GOLD_GAIN);
						if (buff != null && buff.getEndTime() >= System.currentTimeMillis()) {
							float value = buff.getValue();
							iron += (iron * value);
						}
					}
					award.setCount(iron);
				} else if (award.getType() == AwardType.RESOURCE && award.getId() == ResourceType.COPPER) {
					copper = award.getCount();

					// 美女系统加成攻打虫族钢铁产出
//                    List<Integer> beautySkillEffect = beautyManager.getBeautySkillEffect(player, BeautySkillType.SPEED_UP_MONSTER_COPPER);
//                    if (null != beautySkillEffect) {
//                        Integer effectValue = beautySkillEffect.get(1);
//                        double pre = effectValue / DevideFactor.PERCENT_NUM;
//                        copper = (int) (copper * (1 + pre));
//                    }

					// 获取兵书技能加成
					if (heroIds.size() == 1) {
						Hero hero = player.getHero(heroIds.get(0));
						Integer heroWarBookSkillEffect = bookManager.getHeroWarBookSkillEffect(hero.getHeroBooks(), hero.getHeroId(), BookEffectType.KILL_MONSTER_ALONE);
						if (null != heroWarBookSkillEffect) {
							double pre = heroWarBookSkillEffect / 1000.0f;
							copper = (int) (copper * (1 + pre));
						}
					}

					// 钢铁增益道具加成
					simpleData = player.getSimpleData();
					if (simpleData != null) {
						HashMap<Integer, Buff> buffMap = simpleData.getBuffMap();
						Buff buff = buffMap.get(BuffId.STEEL_GAIN);
						if (buff != null && buff.getEndTime() >= System.currentTimeMillis()) {
							float value = buff.getValue();
							copper += (copper * value);
						}
					}
					award.setCount(copper);
				}
			}

			battleMailMgr.handleSendKillMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, iron, copper, solderRecMap, percent);
			// TODO jyb世界目标击杀叛军
			worldTargetTaskService.doKillMosnster(player);
			// TODO 击杀虫子事件影响的活动
			activityEventManager.activityTip(EventEnum.KILL_MONSTER, player, 1, monster.getLevel());
			// TODO 通行证
//            activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.DONE_FREE_MONSTER, 1);
			// TODO 世界宝箱
			worldBoxManager.calcuPoints(WorldBoxTask.KILL_MONSTER, player, 1);
			if (percent > 0) {
				eventManager.worldActNightAttack(player, Lists.newArrayList(WorldActivityConsts.ACTIVITY_5, staticWorldMonster.getId(), staticWorldMonster.getLevel(), playerTeam.getLost(), heroIds, awards));
			}
			// TODO 日常任务
			dailyTaskManager.record(DailyTaskId.KILL_MONSTER, player, 1);
			// 部队回城
			marchManager.handleAttackMonsterMarchReturn(march, MarchReason.KillRebelWin);
			worldManager.synMarch(mapInfo.getMapId(), march);
			// 自动杀虫解锁对应等级
			if (staticLimitMgr.getNum(SimpleId.AUTO_MIN) >= playerTeam.getLost()) {
				int lv = player.getSimpleData().getAutoMaxKillLevel();
				if (lv < monsterLevel) {
					player.getSimpleData().setAutoMaxKillLevel(monsterLevel);
				}
			}
			achievementService.addAndUpdate(player, AchiType.AT_30,1);
		} else {
			// send mail
			marchManager.handleAttackMonsterMarchReturn(march, MarchReason.KillRebelFailed);
			worldManager.synMarch(mapInfo.getMapId(), march);
			battleMailMgr.handleSendKillMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, 0, 0, solderRecMap, percent);
			monster.setStatus(0);
		}

		if (staticWorldMonster.getType() == 1 && playerTeam.isWin()) {// 判断攻打的是不是虫族
			zergAccelerate(player);// 虫族加速活动
		}
		SpringUtil.getBean(EventManager.class).attack_rebel(player, Lists.newArrayList(staticWorldMonster.getLevel(), JSONObject.toJSON(solderRecMap).toString(), march.getAwards()));
	}


	public void zergAccelerate(Player player) {
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_REBEL_SPEED);
		if (activityBase != null && activityBase.getStep() == ActivityConst.ACTIVITY_BEGIN) {// 虫族加速活动是否开启
			BuildingPb.SynActivityDerateTimeRq.Builder activitybuild = BuildingPb.SynActivityDerateTimeRq.newBuilder();
			activitybuild.clear();
			Random r = new Random();
			int number = r.nextInt(1000);
			int probability = staticLimitMgr.getNum(225);
			if (number < probability) {// 是否触发活动加速
				Building buildings = player.buildings;
				if (buildings == null) {

					return;
				}
				// 检查当前建造队列类型
				ConcurrentLinkedDeque<BuildQue> buildQues = buildings.getBuildQues();
				List<BuildQue> gtFourbuildQues = new ArrayList<BuildQue>();// 建筑完成时长大于4分钟的，要加速的建筑
				List<BuildQue> ltFourbuildQues = new ArrayList<BuildQue>();// 建筑完成时长小于4分钟的，要加速的建筑

				int buildingSize = buildQues.size();// 判断有没有在建造的建筑
				long now = System.currentTimeMillis();
				if (buildingSize != 0) {
					for (BuildQue buildQue : buildQues) {
						if (buildQue.getFreeTimes() == 0 || buildQue.getActivityDerateCD() == 1) {// 免费时长未用时不触发活动气泡,已有活动气泡时也不参与活动
							continue;
						}
						if (buildQue.getEndTime() - now > 240000) {

							gtFourbuildQues.add(buildQue);
						} else {

							ltFourbuildQues.add(buildQue);
						}
					}
				}
				if (gtFourbuildQues.size() > 0) {
					gtFourbuildQues.get(r.nextInt(gtFourbuildQues.size()));
				} else if (ltFourbuildQues.size() > 0) {
					ltFourbuildQues.get(r.nextInt(ltFourbuildQues.size()));
				}
				Tech tech = player.getTech();
				// 检查是否存在可以秒CD的研发队列
				LinkedList<TechQue> techQues = tech.getTechQues();
				LinkedList<TechQue> gtFourtechQues = new LinkedList<>();
				LinkedList<TechQue> ltFourtechQues = new LinkedList<>();
				TechQue techQueTemp = null;
				TechQue ltTechQueTemp = null;
				// 检查是否有免费次数
				EmployInfo employInfo = player.getEmployInfo();

				for (TechQue techQue : techQues) {
					if ((employInfo.getResearcherId() != 0 && techQue.getSpeed() == 0) || techQue.getActivityDerateCD() == 1) {// 免费时长未用时不触发活动气泡,已有活动气泡时也不参与活动
						continue;
					}
					if (techQue.getEndTime() - now > 240000) {

						gtFourtechQues.add(techQue);
					} else {

						ltFourtechQues.add(techQue);
					}
				}
				if (gtFourtechQues.size() > 0) {
					gtFourtechQues.get(r.nextInt(gtFourtechQues.size()));
				} else if (ltFourtechQues.size() > 0) {
					ltFourtechQues.get(r.nextInt(ltFourtechQues.size()));
				}
				if (gtFourtechQues.size() > 0) {
					techQueTemp = gtFourtechQues.get(0);
				} else if (ltFourtechQues.size() > 0) {
					ltTechQueTemp = ltFourtechQues.get(0);
				}

				// 是否有空余的队列（兵力加速）
				LinkedList<WorkQue> workQues = null;
				WorkQue workQueTemp = null;
				CommonPb.WorkQue.Builder workQue = CommonPb.WorkQue.newBuilder();
				CommonPb.BuildQue.Builder buildQue = CommonPb.BuildQue.newBuilder();
				BuildQue buildQueTemp = null;
				BuildQue ltBuildQueTemp = null;
				Map<Integer, Soldier> soldiers = player.getSoldiers();
				List<Soldier> SoldierList = new ArrayList<>();
				Soldier soldier = null;
				int soldiersNumber = soldiers.size();
				if (soldiersNumber != 0) {
					for (Map.Entry<Integer, Soldier> soldierTemp : soldiers.entrySet()) {
						if (soldierTemp.getValue().getWorkQues().size() > 0 && soldierTemp.getValue().getWorkQues().get(0).getActivityDerateCD() != 1) {
							SoldierList.add(soldierTemp.getValue());
						}
					}
					if (SoldierList.size() > 0) {
						soldier = SoldierList.get(r.nextInt(SoldierList.size()));// 要加速的兵
						workQues = soldier.getWorkQues();
						workQueTemp = workQues.get(0);

						if (workQueTemp.getActivityDerateCD() != 1) {
							workQue.setKeyId(workQueTemp.getKeyId());
							workQue.setBuildingId(workQueTemp.getBuildingId());
							workQue.setPeriod(workQueTemp.getPeriod());
							workQue.setEndTime(workQueTemp.getEndTime());
							workQue.setEmployWork(workQueTemp.getEmployWork());
							workQue.setOil(workQueTemp.getOil());
							workQue.setIron(workQueTemp.getIron());
							workQue.setActivityDerateCD(workQueTemp.getActivityDerateCD());

						}
					}
				}
				if (gtFourbuildQues.size() > 0) {
					buildQueTemp = gtFourbuildQues.get(0);
				} else if (ltFourbuildQues.size() > 0) {
					ltBuildQueTemp = ltFourbuildQues.get(0);
				}
				if (buildQueTemp != null) {
					buildQue.setBuildingId(buildQueTemp.getBuildingId());
					buildQue.setReBuildingId(buildQueTemp.getReBuildingId());

					buildQue.setPeriod(buildQueTemp.getPeriod());
					buildQue.setEndTime(buildQueTemp.getEndTime());
					buildQue.setBuildQueType(buildQueTemp.getBuildQueType());
				} else if (ltBuildQueTemp != null) {
					buildQue.setBuildingId(ltBuildQueTemp.getBuildingId());
					buildQue.setReBuildingId(ltBuildQueTemp.getReBuildingId());
					buildQue.setPeriod(ltBuildQueTemp.getPeriod());
					buildQue.setEndTime(ltBuildQueTemp.getEndTime());
					buildQue.setBuildQueType(ltBuildQueTemp.getBuildQueType());
				}
				// cd时长大于4分钟的
				if (buildQueTemp != null || workQueTemp != null || techQueTemp != null) {
					if (buildQueTemp != null && workQueTemp != null && techQueTemp != null) {
						int numTemp = r.nextInt(9);
						if (numTemp < 3) {
							workQues.get(0).setActivityDerateCD(1);
							workQue.setActivityDerateCD(1);
							activitybuild.setWorkQue(workQue);

						} else if (numTemp >= 3 && numTemp < 6) {
							for (BuildQue buildAddCD : buildQues) {
								if (buildQue.getBuildingId() == buildAddCD.getBuildingId()) {
									buildAddCD.setActivityDerateCD(1);
								}
							}
							buildQue.setActivityDerateCD(1);
							activitybuild.setBuildQue(buildQue);

						} else {

							techQueTemp.setActivityDerateCD(1);
							activitybuild.setTechQue(techManager.wrapTechQuePb(player, techQueTemp));

						}
					} else if (buildQueTemp != null && workQueTemp != null) {
						int numTemp = r.nextInt(8);
						if (numTemp < 4) {
							for (BuildQue buildAddCD : buildQues) {
								if (buildQue.getBuildingId() == buildAddCD.getBuildingId()) {
									buildAddCD.setActivityDerateCD(1);
								}
							}
							buildQue.setActivityDerateCD(1);
							activitybuild.setBuildQue(buildQue);
						} else {
							workQues.get(0).setActivityDerateCD(1);
							workQue.setActivityDerateCD(1);
							activitybuild.setWorkQue(workQue);
						}
					} else if (buildQueTemp != null && techQueTemp != null) {
						int numTemp = r.nextInt(8);
						if (numTemp < 4) {
							for (BuildQue buildAddCD : buildQues) {
								if (buildQue.getBuildingId() == buildAddCD.getBuildingId()) {
									buildAddCD.setActivityDerateCD(1);
								}
							}
							buildQue.setActivityDerateCD(1);
							activitybuild.setBuildQue(buildQue);
						} else {

							techQueTemp.setActivityDerateCD(1);

							activitybuild.setTechQue(techManager.wrapTechQuePb(player, techQueTemp));
						}
					} else if (workQueTemp != null && techQueTemp != null) {
						int numTemp = r.nextInt(8);
						if (numTemp < 4) {
							workQues.get(0).setActivityDerateCD(1);
							workQue.setActivityDerateCD(1);
							activitybuild.setWorkQue(workQue);
						} else {

							techQueTemp.setActivityDerateCD(1);

							activitybuild.setTechQue(techManager.wrapTechQuePb(player, techQueTemp));
						}
					} else if (workQueTemp != null) {
						workQues.get(0).setActivityDerateCD(1);
						workQue.setActivityDerateCD(1);
						activitybuild.setWorkQue(workQue);

					} else if (buildQueTemp != null) {
						for (BuildQue buildAddCD : buildQues) {
							if (buildQue.getBuildingId() == buildAddCD.getBuildingId()) {
								buildAddCD.setActivityDerateCD(1);
							}
						}
						buildQue.setActivityDerateCD(1);
						activitybuild.setBuildQue(buildQue);

					} else if (techQueTemp != null) {

						techQueTemp.setActivityDerateCD(1);

						activitybuild.setTechQue(techManager.wrapTechQuePb(player, techQueTemp));
					}
				} else {
					if (ltBuildQueTemp != null && ltTechQueTemp != null) {
						int numTemp = r.nextInt(6);
						if (numTemp < 3) {
							for (BuildQue buildAddCD : buildQues) {
								if (buildQue.getBuildingId() == buildAddCD.getBuildingId()) {
									buildAddCD.setActivityDerateCD(1);
								}
							}
							buildQue.setActivityDerateCD(1);
							activitybuild.setBuildQue(buildQue);

						} else {

							ltTechQueTemp.setActivityDerateCD(1);

							activitybuild.setTechQue(techManager.wrapTechQuePb(player, ltTechQueTemp));

						}
					} else if (ltBuildQueTemp != null && ltTechQueTemp != null) {
						int numTemp = r.nextInt(8);
						if (numTemp < 4) {
							for (BuildQue buildAddCD : buildQues) {
								if (buildQue.getBuildingId() == buildAddCD.getBuildingId()) {
									buildAddCD.setActivityDerateCD(1);
								}
							}
							buildQue.setActivityDerateCD(1);
							activitybuild.setBuildQue(buildQue);
						} else {

							ltTechQueTemp.setActivityDerateCD(1);

							activitybuild.setTechQue(techManager.wrapTechQuePb(player, ltTechQueTemp));
						}
					} else if (ltBuildQueTemp != null) {
						for (BuildQue buildAddCD : buildQues) {
							if (buildQue.getBuildingId() == buildAddCD.getBuildingId()) {
								buildAddCD.setActivityDerateCD(1);
							}
						}
						buildQue.setActivityDerateCD(1);
						activitybuild.setBuildQue(buildQue);

					} else if (ltTechQueTemp != null) {

						ltTechQueTemp.setActivityDerateCD(1);

						activitybuild.setTechQue(techManager.wrapTechQuePb(player, ltTechQueTemp));
					}
				}
			}
			SynHelper.synMsgToPlayer(player, SynActivityDerateTimeRq.EXT_FIELD_NUMBER, BuildingPb.SynActivityDerateTimeRq.ext, activitybuild.build());
		}
	}

	public void handleActMonster(StaticWorldMonster staticWorldMonster, Player player, Monster monster, March march, MapInfo mapInfo) {
		List<Integer> monsterIds = staticWorldMonster.getMonsterIds();
		List<Integer> heroIds = march.getHeroIds();
		Team monsterTeam = battleMgr.initMonsterTeam(monsterIds, BattleEntityType.REBEL);
		Team playerTeam = battleMgr.initPlayerTeam(player, heroIds, BattleEntityType.HERO);

		// 随机seed不用存盘，没有回放, 种子需要发送到客户端
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		// 计算经验值
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, player.roleId);
		// 处理玩家扣血
		HashMap<Integer, Integer> soldierRecMap = new HashMap<Integer, Integer>();
		worldManager.caculatePlayer(playerTeam, player, soldierRecMap);

		if (playerTeam.isWin()) {
			List<Award> awards = getActAwards(player);
			if (awards != null && !awards.isEmpty()) {
				march.addAllAwards(awards);
			}
			// 部队回城
			marchManager.handleMarchReturn(march, MarchReason.KillRebelWin);
			// 清除野怪
//			worldManager.clearMonsterPos(mapInfo, monster.getPos());
			mapInfo.clearPos(monster.getPos());
			// send mail
			// 全区域广播
			worldManager.synMarch(mapInfo.getMapId(), march);

			// 同步野怪
//			worldManager.synEntityRemove(monster, mapInfo.getMapId(), monster.getPos());


			// 处理自动补兵
			// soldierManager.autoAdd(player, march.getHeroIds());

			// 发送邮件
			int iron = staticWorldMonster.getIron();
			int copper = staticWorldMonster.getCopper();
			battleMailMgr.handleSendKillMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, iron, copper, soldierRecMap);

		} else {
			// 部队回城
			marchManager.handleMarchReturn(march, MarchReason.KillRebelFailed);
			worldManager.synMarch(mapInfo.getMapId(), march);
			battleMailMgr.handleSendKillMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, 0, 0, soldierRecMap);
			monster.setStatus(0); // 表示没人打了
		}

		playerManager.synChange(player, Reason.KILL_WORLD_MONSTER);
	}

	// 击杀国家名将
	public void handleKillCountryHero(StaticWorldMonster config, Player player, Monster monster, March march, MapInfo mapInfo) {
		List<Integer> monsterIds = config.getMonsterIds();
		List<Integer> heroIds = march.getHeroIds();
		Team monsterTeam = battleMgr.initMonsterTeam(monsterIds, BattleEntityType.REBEL);
		Team playerTeam = battleMgr.initPlayerTeam(player, heroIds, BattleEntityType.HERO);
		// 随机seed不用存盘，没有回放, 种子需要发送到客户端
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		HeroAddExp heroAddExp = new HeroAddExp();
		// 处理玩家扣血
		HashMap<Integer, Integer> soldierRecMap = new HashMap<Integer, Integer>();
		worldManager.caculatePlayer(playerTeam, player, soldierRecMap);
		if (playerTeam.isWin()) {
			// 部队回城
			marchManager.handleMarchReturn(march, MarchReason.KillRebelWin);
			// 全区域广播
			worldManager.synMarch(mapInfo.getMapId(), march);
			// 处理自动补兵
			// soldierManager.autoAdd(player, march.getHeroIds());
			handleWinCountryHero(player, config);
			battleMailMgr.handleSendKillMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, 0, 0, soldierRecMap);

		} else {
			// 部队回城
			marchManager.handleMarchReturn(march, MarchReason.KillRebelFailed);
			worldManager.synMarch(mapInfo.getMapId(), march);
			monster.setStatus(0); // 表示没人打了
			handleFailCountryHero(player, config, 2);
			battleMailMgr.handleSendKillMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, 0, 0, soldierRecMap);
		}
		playerManager.synChange(player, Reason.KILL_WORLD_MONSTER);
		// 检查野怪
		checkCountryMonster(config, mapInfo, monster);

	}

	public void handleKillStaffMonster(StaticWorldMonster staticWorldMonster, Player player, Monster monster, March march, MapInfo mapInfo) {
		List<Integer> monsterIds = staticWorldMonster.getMonsterIds();
		List<Integer> heroIds = march.getHeroIds();
		Team monsterTeam = battleMgr.initMonsterTeam(monsterIds, BattleEntityType.REBEL);
		Team playerTeam = battleMgr.initPlayerTeam(player, heroIds, BattleEntityType.HERO);
		Random rand = new Random(System.currentTimeMillis());
		battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, player.roleId);
		HashMap<Integer, Integer> soldierRecMap = new HashMap<Integer, Integer>();
		worldManager.caculatePlayer(playerTeam, player, soldierRecMap);
		if (playerTeam.isWin()) {
			int iron = 0;
			int copper = 0;
			int soldierNum = 0;
			List<Award> awards = getMonsterAwards(player, staticWorldMonster);
			if (awards != null && !awards.isEmpty()) {
				march.addAllAwards(awards);
				// 道具直接加到人身上去
				for (Award award : awards) {
					if (award.getType() == AwardType.STAFF_SOLDIER) {
						soldierNum += award.getCount();
					} else if (award.getType() == AwardType.RESOURCE) {
						if (award.getId() == ResourceType.IRON) {
							iron += award.getCount();
						}
						if (award.getId() == ResourceType.COPPER) {
							copper += award.getCount();
						}

					}
				}
			}
			// 记录到国家中去
			// countryManager.updateSoldierNum(player, soldierNum);
			// handleMarchReturn(march, MarchReason.KillRebelWin);
			marchManager.handleMarchReturn(march, MarchReason.KillRebelWin);
			mapInfo.clearPos(monster.getPos());

//			worldManager.clearMonsterPos(mapInfo, monster.getPos());
			worldManager.synMarch(mapInfo.getMapId(), march);
//			worldManager.synEntityRemove(monster, mapInfo.getMapId(), monster.getPos());
			soldierManager.autoAdd(player, march.getHeroIds());
			battleMailMgr.handleSendKillStaffMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, iron, copper, soldierNum, soldierRecMap);
			try {
				castleService.doMeetingTask((int) monster.getId(), player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// handleMarchReturn(march, MarchReason.KillRebelFailed);
			marchManager.handleMarchReturn(march, MarchReason.KillRebelFailed);
			worldManager.synMarch(mapInfo.getMapId(), march);
			battleMailMgr.handleSendKillStaffMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, 0, 0, 0, soldierRecMap);
			monster.setStatus(0);
		}

		playerManager.synChange(player, Reason.KILL_WORLD_MONSTER);
	}

	public void checkCountryMonster(StaticWorldMonster config, MapInfo mapInfo, Monster monster) {
		int monsterId = config.getId();
		Integer heroId = staticCountryMgr.getHeroIdByMonsterId(monsterId);
		if (heroId == null) {
			LogHelper.CONFIG_LOGGER.info("heroId is null, monsterId = " + monsterId);
			return;
		}

		CountryHero countryHero = countryManager.getCountryHero(heroId);
		if (countryHero == null) {
			LogHelper.CONFIG_LOGGER.info("country hero is null!");
			return;
		}

		if (countryHero.getFightTimes() <= 0) {
//			worldManager.synEntityRemove(monster, mapInfo.getMapId(), monster.getPos());
//			worldManager.clearMonsterPos(mapInfo, monster.getPos());

			mapInfo.clearPos(monster.getPos());

		}

	}

	public void handleWinCountryHero(Player player, StaticWorldMonster config) {
		int monsterId = config.getId();
		Integer heroId = staticCountryMgr.getHeroIdByMonsterId(config.getId());
		if (heroId == null) {
			LogHelper.CONFIG_LOGGER.info("heroId is null, monsterId = " + monsterId);
			return;
		}

		CountryHero countryHero = countryManager.getCountryHero(heroId);
		if (countryHero == null) {
			LogHelper.CONFIG_LOGGER.info("country hero is null, heroId =" + heroId + ", monsterId =" + monsterId);
			return;

		}

		// 检查国家名将获得的概率
		StaticCountryHero configHero = staticCountryMgr.getCountryHero(heroId);
		int getType = 0;
		if (configHero.getCountry() == player.getCountry()) {
			getType = 1;
		} else {
			getType = 2;
		}

		int heroRate = staticCountryMgr.heroGetRate(getType, countryHero.getHeroLv(), countryHero.getHeroId());
		int randNum = RandomHelper.threadSafeRand(1, 100);
		int subLv = staticLimitMgr.getNum(141);
		if (randNum > heroRate) {
			countryHero.subLv(subLv);
			handleFailCountryHero(player, config, 3);
			return;
		}
		long period = staticLimitMgr.getNum(142) * TimeHelper.HOUR_MS;
		countryHero.setLoyaltyEndTime(System.currentTimeMillis() + period);
		countryHero.setState(HeroState.NO_ACTIVATE);
		countryHero.setLordId(player.roleId);
		countryHero.setFightTimes(0);
		countryHero.addOccurRound(1);
		countryHero.subLv(subLv);
		Hero hero = heroManager.addHero(player, heroId, Reason.COUNTRY_HERO);
		hero.setActivate(HeroState.NO_ACTIVATE);
		hero.setHeroLv(countryHero.getHeroLv());
		hero.setLoyalty(100);
		SynCountryHeroRq.Builder builder = SynCountryHeroRq.newBuilder();
		builder.setHero(hero.wrapPb());
		builder.setFightTimes(countryHero.getFightTimes());
		builder.setIsSuccess(1);
		builder.setFightHeroId(heroId);
		builder.setState(countryHero.getState());
		builder.setEndTime(countryHero.getLoyaltyEndTime());
		long lordId = countryHero.getLordId();
		if (lordId != 0) {
			Player target = playerManager.getPlayer(lordId);
			if (target != null && target.getNick() != null) {
				builder.setNick(target.getNick());
				builder.setCountry(target.getCountry());
			}
		}
		SynHelper.synMsgToPlayer(player, SynCountryHeroRq.EXT_FIELD_NUMBER, SynCountryHeroRq.ext, builder.build());
		countryManager.sendGotHeroMail(player, heroId);
		countryManager.sendChatCountryHero(player, heroId);
	}

	public void handleFailCountryHero(Player player, StaticWorldMonster config, int result) {
		int monsterId = config.getId();
		Integer heroId = staticCountryMgr.getHeroIdByMonsterId(monsterId);
		if (heroId == null) {
			LogHelper.CONFIG_LOGGER.info("heroId is null, monsterId = " + monsterId);
			return;
		}

		CountryHero countryHero = countryManager.getCountryHero(heroId);
		if (countryHero == null) {
			LogHelper.CONFIG_LOGGER.info("country hero is null!");
			return;
		}

		countryHero.subFightTimes(1);
		if (countryHero.getFightTimes() <= 0) {
			countryHero.addOccurRound(1);
			countryManager.countryHeroEscape(countryHero.getHeroId(), true);
		}

		SynCountryHeroRq.Builder builder = SynCountryHeroRq.newBuilder();
		builder.setFightTimes(countryHero.getFightTimes());
		builder.setIsSuccess(result);
		builder.setFightHeroId(heroId);
		builder.setState(countryHero.getState());
		long lordId = countryHero.getLordId();
		if (lordId != 0) {
			Player target = playerManager.getPlayer(lordId);
			if (target != null && target.getNick() != null) {
				builder.setNick(target.getNick());
				builder.setCountry(target.getCountry());
			}
		}
		builder.setHeroLv(countryHero.getHeroLv());
		SynHelper.synMsgToPlayer(player, SynCountryHeroRq.EXT_FIELD_NUMBER, SynCountryHeroRq.ext, builder.build());
	}

	public List<Award> getMonsterAwards(Player player, StaticWorldMonster staticWorldMonster) {
		List<Award> awards = new ArrayList<Award>();
		if (staticWorldMonster == null) {
			return awards;
		}

		int iron = staticWorldMonster.getIron();
		int copper = staticWorldMonster.getCopper();

		// 资源掉落翻倍
		float resourceRatio = activityManager.actDouble(ActivityConst.ACT_REBEL_RESOURCE);
		resourceRatio += worldManager.getCityBuf(player, CityBuffType.ATT);
		iron = (int) ((1f + resourceRatio) * iron);
		copper = (int) ((1f + resourceRatio) * copper);

		if (iron > 0) {
			awards.add(new Award(0, AwardType.RESOURCE, ResourceType.IRON, iron));
		}

		if (copper > 0) {
			awards.add(new Award(0, AwardType.RESOURCE, ResourceType.COPPER, copper));
		}

		// 图纸掉落翻倍
		int rebelDropRatio = (int) activityManager.actDouble(ActivityConst.ACT_REBEL_DROP);
		// 低级城迁翻倍
		int rebelMoveDropRatio = (int) activityManager.actDouble(ActivityConst.ACT_REBEL_MOVE_DROP);

		List<List<Integer>> staticDropList = staticWorldMonster.getDropList();
		List<List<Integer>> dropList = new ArrayList<>();
		staticDropList.forEach(e -> {
			dropList.add(new ArrayList(e));
		});

		// 美女系统加成获得蓝色图纸的概率
//        List<Integer> effect = beautyManager.getBeautySkillEffect(player, BeautySkillType.ADD_BLUE_PAPER_PROBABILITY);
//        if (null != effect) {
//            for (List<Integer> drop : dropList) {
//                if (drop.get(0) == AwardType.PROP) {
//                    StaticProp staticProp = staticPropMgr.getStaticProp(drop.get(1));
//                    if (staticProp.getPropType() == ItemType.EQUIP_PAPER) {
//                        if (staticProp.getColor() == 3) {
//                            Integer effectValue = effect.get(1);
//                            int Probabilit = (int) (drop.get(3) * (1 + effectValue / DevideFactor.PERCENT_NUM));
//                            drop.set(3, Probabilit);
//                        }
//                    }
//                }
//            }
//        }

		// 伏击叛军特殊掉落
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_2);
		if (worldActPlan != null && worldActPlan.getState() == WorldActPlanConsts.OPEN) {
			Award award = staticRebelMgr.getrRebelZergDropAWard(staticWorldMonster.getLevel());
			if (award != null) {
				awards.add(award);
			}
		}
		// 双旦活动掉落
		List<Award> actDoubleEgg = activityService.actDoubleEggReward(player, true);
		awards.addAll(actDoubleEgg);
		List<List<Integer>> extraDrop = staticWorldMonster.getExtraDrop();
		if (extraDrop != null && !extraDrop.isEmpty()) {
			for (List<Integer> integers : extraDrop) {
				int randNum = RandomHelper.threadSafeRand(1, 100);
				if (randNum <= integers.get(3)) {
					awards.add(new Award(0, integers.get(0), integers.get(1), integers.get(2)));

				}
			}
		}
		if (dropList != null && dropList.size() >= 1) {
			// 先计算总权重
			int total = 0;
			for (List<Integer> itemLoot : dropList) {
				if (itemLoot == null || itemLoot.size() != 4) {
					continue;
				}
				total += itemLoot.get(3);
			}
			// logger.error("击杀世界野怪随机掉落物品的总权重>>>>>>>>>>>"+total);

			int randNum = RandomHelper.threadSafeRand(1, total);
			// logger.error("击杀世界野怪随机掉落物品的randNum>>>>>>>>>>>"+randNum);
			int checkNum = 0;
			for (List<Integer> itemLoot : dropList) {
				if (itemLoot == null || itemLoot.size() != 4) {
					continue;
				}
				int type = itemLoot.get(0);
				int id = itemLoot.get(1);
				int count = itemLoot.get(2);
				checkNum += itemLoot.get(3);
				// logger.error("击杀世界野怪随机掉落物品的checkNum>>>>>>>>>>>"+checkNum);
				if (randNum <= checkNum) {
					if (type == AwardType.EMPTY_LOOT) {
						return awards;
					}

					if (count > 0 && type == AwardType.PROP) {
						StaticProp staticProp = staticPropMgr.getStaticProp(id);
						if (rebelDropRatio != 0 && staticProp.getPropType() == 23) {
							count += rebelDropRatio;
						} else if (rebelMoveDropRatio != 0 && id == 30) {
							count += rebelMoveDropRatio;
						}
					}

					// 空掉落不加入
					awards.add(new Award(0, type, id, count));
					// logger.error("击杀世界野怪随机掉落物品的award>>>>>>>>>>>"+type+","+id+","+count);
					break;
				}
			}
		}
		return awards;
	}

	public List<Award> getActAwards(StaticWorldMonster staticWorldMonster, List<List<Integer>> dropList) {
		List<Award> awards = new ArrayList<Award>();
		if (staticWorldMonster == null) {
			return awards;
		}

		int iron = staticWorldMonster.getIron();
		int copper = staticWorldMonster.getCopper();

		// 资源掉落翻倍
		float resourceRatio = activityManager.actDouble(ActivityConst.ACT_REBEL_RESOURCE);
		iron = (int) ((1f + resourceRatio) * iron);
		copper = (int) ((1f + resourceRatio) * copper);

		if (iron > 0) {
			awards.add(new Award(0, AwardType.RESOURCE, ResourceType.IRON, iron));
		}

		if (copper > 0) {
			awards.add(new Award(0, AwardType.RESOURCE, ResourceType.COPPER, copper));
		}

		// 图纸掉落翻倍
		int rebelDropRatio = (int) activityManager.actDouble(ActivityConst.ACT_REBEL_DROP);
		// 低级城迁翻倍
		int rebelMoveDropRatio = (int) activityManager.actDouble(ActivityConst.ACT_REBEL_MOVE_DROP);

		if (dropList != null && dropList.size() > 1) {
			// 先计算总权重
			int total = 0;
			for (List<Integer> itemLoot : dropList) {
				if (itemLoot == null || itemLoot.size() != 4) {
					continue;
				}
				total += itemLoot.get(3);
			}

			int randNum = RandomHelper.threadSafeRand(1, total);
			int checkNum = 0;
			for (List<Integer> itemLoot : dropList) {
				if (itemLoot == null || itemLoot.size() != 4) {
					continue;
				}
				int type = itemLoot.get(0);
				int id = itemLoot.get(1);
				int count = itemLoot.get(2);
				checkNum += itemLoot.get(3);
				if (randNum <= checkNum) {
					if (type == AwardType.EMPTY_LOOT) {
						return awards;
					}

					if (count > 0 && type == AwardType.PROP) {
						StaticProp staticProp = staticPropMgr.getStaticProp(id);
						if (rebelDropRatio != 0 && staticProp.getPropType() == 23) {
							count += rebelDropRatio;
						} else if (rebelMoveDropRatio != 0 && id == 30) {
							count += rebelMoveDropRatio;
						}
					}

					// 空掉落不加入
					awards.add(new Award(0, type, id, count));
					break;
				}
			}
		}

		return awards;
	}

	public void handleAssistReturn(March march, int reason) {
		// 城防中移除玩家信息
		long lordId = march.getAssistId();
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			LoggerFactory.getLogger(getClass()).error("驻防撤回异常 玩家不存在 ->[{}]", lordId);
			return;
		}
		// 回城
		march.setState(MarchState.FightOver);
		// 开始掉头
		march.swapPos(reason);

		// 兵书对行军的影响值
		List<Integer> heroIds = march.getHeroIds();
		float bookEffectMarch = bookManager.getBookEffectMarch(player, heroIds);
		// 重新计算时间
		long period = worldManager.getPeriod(player, march.getStartPos(), march.getEndPos(), bookEffectMarch);
		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + march.getPeriod());
		Wall wall = player.getWall();
		if (wall == null) {
			LoggerFactory.getLogger(getClass()).error("驻防撤回异常 城墙不存在 ->[{}]", lordId);
			return;
		}
		Map<Integer, WallFriend> wallFriends = wall.getWallFriends();
		List<Integer> keys = new ArrayList<>(wallFriends.keySet());
		for (Integer key : keys) {
			WallFriend friend = wallFriends.get(key);
			if (friend == null) {
				continue;
			}
			if (friend.getLordId() == march.getLordId() && heroIds.contains(friend.getHeroId())) {
				wallFriends.remove(key);
			}
		}
	}

	public void handleCollectWar(int mailId, March hasMarch, Player defencer, Player attacker, boolean isWin, Entity resource, long collectTime) {
		List<Integer> heroIds = hasMarch.getHeroIds();
		if (heroIds != null && heroIds.size() == 1) {
			int heroId = heroIds.get(0);
			// 计算采集时间
			Hero hero = defencer.getHero(heroId);
			List<Award> awards = hasMarch.getAwards();
			long count = 0;
			if (awards != null && awards.size() == 1) {
				count = awards.get(0).getCount();
			}
			if (hero != null) {
				battleMailMgr.sendCollectDone(mailId, resource, collectTime, count, heroId, hero.getHeroLv(), defencer, isWin, attacker);
			}
		}
	}

	public long getCollectTime(March march) {
		long leftTime = march.getEndTime() - System.currentTimeMillis();
		leftTime = Math.max(0, leftTime);
		long collectTime = march.getPeriod() - leftTime;
		collectTime = Math.max(0, collectTime);

		return collectTime;
	}

	/**
	 * Function:触发击杀叛军任务
	 */
	public void doKillMonster(Player player, int monsterLv) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(monsterLv);
		taskManager.doTask(TaskType.KILL_REBEL, player, triggers);
		taskManager.doTask(TaskType.KILL_MUTIL_REBEL, player, null);
		countryManager.doCountryTask(player, CountryTaskType.KILL_REBEL, 1);

	}

	public List<Award> getActAwards(Player player) {
		StaticWorldMonster worldMonster = staticWorldMgr.getMonster(1101);
		List<List<Integer>> dropList = worldMonster.getDropList();
		if (dropList == null || dropList.isEmpty()) {
			return new ArrayList<Award>();
		}

		StaticExchangeHero config = staticActivityMgr.getExchangeHero(200);
		Map<Integer, Integer> takeItem = new TreeMap<Integer, Integer>();
		for (List<Integer> elem : dropList) {
			if (elem.size() != 4) {
				LogHelper.CONFIG_LOGGER.info("elem.size() != 4");
				continue;
			}
			takeItem.put(elem.get(1), 0);
		}

		Map<Integer, Item> itemMap = player.getItemMap();
		for (Item item : itemMap.values()) {
			if (item == null) {
				continue;
			}

			if (takeItem.containsKey(item.getItemId())) {
				takeItem.put(item.getItemId(), item.getItemNum());
			}

		}

		int totalCheck = 0;
		for (Integer num : takeItem.values()) {
			if (num > 0) {
				totalCheck++;
			}
		}

		if (totalCheck <= 3 || totalCheck >= 6) {
			return getMonsterAwards(player, worldMonster);
		} else if (totalCheck == 4) {
			List<Integer> loot1 = config.getLoot2();
			if (loot1 == null || loot1.size() != 2) {
				LogHelper.CONFIG_LOGGER.info("loot1 == null || loot1.size() != 2");
				return new ArrayList<Award>();
			}
			List<List<Integer>> makeLoot = new ArrayList<List<Integer>>();
			makeLoot.addAll(dropList);
			for (int index = 0; index < makeLoot.size(); index++) {
				int itemId = makeLoot.get(index).get(1);
				if (takeItem.get(itemId) > 0) {
					makeLoot.get(index).set(3, loot1.get(0));
				} else {
					makeLoot.get(index).set(3, loot1.get(1));
				}
			}
			return getActAwards(worldMonster, makeLoot);
		} else if (totalCheck == 5) {
			List<Integer> loot2 = config.getLoot2();
			if (loot2 == null || loot2.size() != 2) {
				LogHelper.CONFIG_LOGGER.info("loot2 == null || loot2.size() != 2");
				return new ArrayList<Award>();
			}

			List<List<Integer>> makeLoot = new ArrayList<List<Integer>>();
			makeLoot.addAll(dropList);
			for (int index = 0; index < makeLoot.size(); index++) {
				int itemId = makeLoot.get(index).get(1);
				if (takeItem.get(itemId) > 0) {
					makeLoot.get(index).set(3, loot2.get(0));
				} else {
					makeLoot.get(index).set(3, loot2.get(1));
				}
			}
			return getActAwards(worldMonster, makeLoot);
		}

		return new ArrayList<Award>();
	}

	public void handleRoit(StaticWorldMonster staticWorldMonster, Player player, Monster monster, March march, MapInfo mapInfo) {
		List<Integer> heroIds = march.getHeroIds();
		List<Integer> monsterIds = staticWorldMonster.getMonsterIds();
		Team monsterTeam = battleMgr.initMonsterTeam(monsterIds, BattleEntityType.REBEL);
		Team playerTeam = battleMgr.initPlayerTeam(player, heroIds, BattleEntityType.HERO);
		// 随机seed不用存盘，没有回放, 种子需要发送到客户端
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleMgr.doTeamBattle(playerTeam, monsterTeam, rand, ActPassPortTaskType.IS_WORLD_WAR);
		// 计算经验值
		HeroAddExp heroAddExp = worldManager.caculateTeamKill(playerTeam, player.roleId);
		// 处理玩家扣血
		HashMap<Integer, Integer> solderRecMap = new HashMap<>();
		worldManager.caculatePlayer(playerTeam, player, solderRecMap);

		float percent = 0f;

		if (playerTeam.isWin()) {
			List<Award> awards = getMonsterAwards(player, staticWorldMonster);
			if (awards != null && !awards.isEmpty()) {
				march.addAllAwards(awards);
			}

			if (testManager.isOpenTestMode()) {
				Lord lord = player.getLord();
				if (lord != null) {
					lord.setKillMonsterNum(lord.getKillMonsterNum() + 30);
				}
			}

			worldManager.doKillWorldMonster(WorldTargetType.KILL_ROIT, player);

			// 部队回城
			marchManager.handleMarchReturn(march, MarchReason.KillRebelWin);
			worldManager.synMarch(mapInfo.getMapId(), march);
			// 清除野怪
//			worldManager.clearMonsterPos(mapInfo, monster.getPos());
//			// send mail
//			// 同步野怪
//			worldManager.synEntityRemove(monster, mapInfo.getMapId(), monster.getPos());
			mapInfo.clearPos(monster.getPos());
			// 发送邮件
			int iron = 0;
			int copper = 0;
			for (Award award : awards) {
				if (award.getType() == AwardType.RESOURCE && award.getId() == ResourceType.IRON) {
					iron = award.getCount();

					// 美女系统加成攻打虫族金币产出
//                    List<Integer> beautySkillEffect = beautyManager.getBeautySkillEffect(player, BeautySkillType.SPEED_UP_MONSTER_IRON);
//                    if (null != beautySkillEffect) {
//                        Integer effectValue = beautySkillEffect.get(1);
//                        double pre = effectValue / DevideFactor.PERCENT_NUM;
//                        iron = (int) (iron * (1 + pre));
//                    }
				} else if (award.getType() == AwardType.RESOURCE && award.getId() == ResourceType.COPPER) {
					copper = award.getCount();

					// 美女系统加成攻打虫族钢铁产出
//                    List<Integer> beautySkillEffect = beautyManager.getBeautySkillEffect(player, BeautySkillType.SPEED_UP_MONSTER_COPPER);
//                    if (null != beautySkillEffect) {
//                        Integer effectValue = beautySkillEffect.get(1);
//                        double pre = effectValue / DevideFactor.PERCENT_NUM;
//                        copper = (int) (copper * (1 + pre));
//                    }
				}
			}

			battleMailMgr.handleSendKillRiotMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, iron, copper, solderRecMap, percent);
			riotManager.synRiotBuff(player);
			WorldActPlan worldActPlan = roitService.getWorldRoitActPlan();
			if (worldActPlan != null) {
				eventManager.worldActRiot(player, 0, Lists.newArrayList(worldActPlan.getId(), staticWorldMonster.getId(), staticWorldMonster.getLevel()));
			}
			player.getLord().addKillRoitNum();
			// 更新通行证任务
//			activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.MONSTER_INTRUSION, 1);
			activityEventManager.activityTip(EventEnum.RIOT_WAR, player, 1, 0);
		} else {
			// send mail
			marchManager.handleMarchReturn(march, MarchReason.KillRebelFailed);
			worldManager.synMarch(mapInfo.getMapId(), march);
			battleMailMgr.handleSendKillRiotMonster(playerTeam, monsterTeam, player, monster, march.getAwards(), heroAddExp, 0, 0, solderRecMap, percent);
			monster.setStatus(0);
		}
		playerManager.synChange(player, Reason.KILL_WORLD_MONSTER);
	}

	// 同步奖励
	public void synRewards(March march, MapInfo mapInfo) {
		long lordId = march.getLordId();
		// 找到玩家
		Player player = playerManager.getPlayer(lordId);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("return player is null!");
			return;
		}

		int marchType = march.getMarchType();
		int reason = 0;
		if (marchType == 1) {
			reason = Reason.KILL_WORLD_MONSTER;
		}

		List<Award> awards = march.getAwards();
		if (awards != null && !awards.isEmpty()) {
			playerManager.addAward(player, awards, reason);

			/**
			 * 攻打世界野怪资源产出日志埋点
			 */
			com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			for (Award award : awards) {
				if (award.getType() == AwardType.RESOURCE) {
					if (marchType == MarchType.AttackMonster) {
						logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(award.getId()), RoleResourceLog.OPERATE_IN, award.getId(), ResOperateType.WORLD_MOSTER_IN.getInfoType(), award.getCount(), player.account.getChannel()));
						int type = 0;
						int resType = award.getId();
						switch (resType) {
							case ResourceType.IRON:
								type = IronOperateType.WORLD_MOSTER_IN.getInfoType();
								break;
							case ResourceType.COPPER:
								type = CopperOperateType.WORLD_MOSTER_IN.getInfoType();
								break;
							default:
								break;
						}
						if (type != 0) {
							logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, award.getCount(), type), resType);
						}

					} else if (marchType == MarchType.CollectResource) {
						logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(award.getId()), RoleResourceLog.OPERATE_IN, award.getId(), ResOperateType.WORLD_COLLECT_IN.getInfoType(), award.getCount(), player.account.getChannel()));
						int type = 0;
						int resType = award.getId();
						switch (resType) {
							case ResourceType.IRON:
								type = IronOperateType.WORLD_COLLECT_IN.getInfoType();
								break;
							case ResourceType.COPPER:
								type = CopperOperateType.WORLD_COLLECT_IN.getInfoType();
								break;
							case ResourceType.OIL:
								type = OilOperateType.WORLD_COLLECT_IN.getInfoType();
								break;
							case ResourceType.STONE:
								type = StoneOperateType.WORLD_COLLECT_IN.getInfoType();
								break;
							default:
								break;
						}
						if (type != 0) {
							logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, award.getCount(), type), resType);
						}
						// 更新资源采集活动
						activityManager.updCollectionResource(player, award);
					}
				}
			}
		}
		// 同步玩家数据
		playerManager.synChange(player, 0);
		march.setState(MarchState.Back);
		worldManager.synMarch(mapInfo.getMapId(), march);
	}
}