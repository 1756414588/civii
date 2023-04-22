package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticMissionMgr;
import com.game.dataMgr.StaticOpenManger;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticMission;
import com.game.domain.s.StaticVip;
import com.game.domain.s.StaticWarBook;
import com.game.log.constant.*;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.RoleMissonLog;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.cs.GetSweepHeroHandler;
import com.game.message.handler.cs.UpdateSweepHeroHandler;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.FightBefore;
import com.game.pb.CommonPb.LordInfo;
import com.game.pb.CommonPb.MissionInfo;
import com.game.pb.HeroPb;
import com.game.pb.MissionPb;
import com.game.pb.MissionPb.*;
import com.game.spring.SpringUtil;
import com.game.util.*;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liyanze
 * @version 创建时间：2016-12-27 下午10:31:00
 * @declare
 */
@Service
public class MissionService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private MissionManager missionMgr;

	@Autowired
	private StaticMissionMgr staticMissionMgr;

	@Autowired
	private BattleMgr battleDataMgr;

	@Autowired
	private LordManager lordDataManager;

	@Autowired
	private HeroManager heroDataManager;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private KillEquipManager killEquipManager;

	@Autowired
	private ActivityManager activityManager;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private BuildingManager buildingManager;

	@Autowired
	private ChatManager chatManager;

	@Autowired
	private ServerManager serverManager;
	@Autowired
	private LordManager lordManager;
	@Autowired
	private WarBookManager warBookManager;
	@Autowired
	private StaticOpenManger staticOpenManger;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private EventManager eventManager;
    @Autowired
    ActivityEventManager activityEventManager;

	// 获取关卡所有的信息
	public void getAllMissionRq(GetAllMissionRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
		GetAllMissionRs.Builder builder = GetAllMissionRs.newBuilder();
		for (Map.Entry<Integer, Map<Integer, Mission>> mapItem : missions.entrySet()) {
			if (mapItem == null) {
				continue;
			}

			// one MissionMap
			Map<Integer, Mission> missionMap = mapItem.getValue();
			if (missionMap == null) {
				continue;
			}

			for (Map.Entry<Integer, Mission> missionItem : missionMap.entrySet()) {
				if (missionItem == null || missionItem.getValue() == null) {
					continue;
				}
				Mission mission = missionItem.getValue();
				builder.addMission(mission.wrapPb());
			}
		}
		loadNotOpenMission(player, builder);
		handler.sendMsgToPlayer(GameError.OK, GetAllMissionRs.ext, builder.build());
	}

	// 加载未解锁的关卡 发给客户端做预加载
	public void loadNotOpenMission(Player player, GetAllMissionRs.Builder builder) {
		Integer integer = player.getMissions().keySet().stream().sorted(Comparator.comparing(Integer::intValue).reversed()).collect(Collectors.toList()).get(0);
		int mapId = 1;
		if (integer != null) {
			mapId = integer;
		}

		List<StaticMission> staticMissions = staticMissionMgr.getMissionMapByMapId(mapId);
		Map<Integer, Mission> map = player.getMissions().get(mapId);
		if (staticMissions != null && map != null && map.size() != staticMissions.size()) {
			for (StaticMission staticMission : staticMissions) {
				if (map != null && map.containsKey(staticMission.getMissionId())) {
					continue;
				}
				if (staticMission.getMissionType() == MissionType.ResourceLandChip) {
					continue;
				}
				Mission mission = missionMgr.addNextMission(player, staticMission.getMissionId(), null);
				builder.addMission(mission.wrapPb());
			}
		}
	}

	public Mission getMission(Player player, int missionId, int mapId) {
		Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
		Map<Integer, Mission> mapInfo = missions.get(mapId);
		if (mapInfo == null) {
//            LogHelper.CONFIG_LOGGER.info("map is not exists, mapId = " + mapId);
			return null;
		}

		Mission mission = mapInfo.get(missionId);
		if (mission == null) {
//            LogHelper.CONFIG_LOGGER.info("staticMission is null, missionId = " + missionId);
			return null;
		}

		return mission;

	}

	public boolean isEmbattleHero(Player player, int heroId) {
		List<Integer> embattleHero = player.getEmbattleList();
		for (Integer elem : embattleHero) {
			if (elem == heroId) {
				return true;
			}
		}

		return false;
	}

	// 关卡结算, 普通、boss、国器
	public void missionDoneRq(MissionDoneRq req, ClientHandler handler) {
		/*************************** 条件判断 ***********************************/
		Date now = new Date();
		// 判断体力值
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 体力值不满10点不让进副本, 1.普通、2.BOSS
		int missionId = req.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		// 关卡不存在
		if (staticMission == null) {
			LogHelper.CONFIG_LOGGER.info("staticMission is null, missionId = " + missionId);
			handler.sendErrorMsgToPlayer(GameError.MISSION_CONFIG_NOT_EXISTS);
			return;
		}

		// 判断体力值
		int energyCost = staticMission.getWinCost();
		if (energyCost <= 0) {
			LogHelper.CONFIG_LOGGER.info("error energy cost, energy = " + energyCost + ", missionId = " + staticMission.getWinCost());
			handler.sendErrorMsgToPlayer(GameError.ENERGY_COST_ERROR);
			return;
		}

		Lord lord = player.getLord();
		if (lord.getEnergy() < energyCost) {
			handler.sendErrorMsgToPlayer(GameError.ENERGY_NOT_ENOUGH);
			return;
		}

		// 没有出站的英雄
		List<Integer> heroList = req.getHeroIdList();
		if (heroList.size() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_HERO_FIGHT);
			return;

		}
		// 应该用玩家出战的英雄
		// 出战英雄的Id有没有重复
		Set<Integer> checkHeroSet = new HashSet<Integer>();
		Map<Integer, Hero> heros = player.getHeros();
		// 检查出战的英雄Id的合法性
		for (Integer heroId : heroList) {
			Hero hero = heros.get(heroId);
			if (hero == null) {
				handler.sendErrorMsgToPlayer(GameError.HERO_FIGHT_NOT_EXISTS);
				return;
			}
			checkHeroSet.add(heroId);
		}

		// 检查出战的英雄有重复的
		if (checkHeroSet.size() != heroList.size()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
			return;
		}

		// 检查是否是非上阵武将
		for (Integer heroId : heroList) {
			if (!isEmbattleHero(player, heroId)) {
				LogHelper.CONFIG_LOGGER.info("not embattle hero, heroId =" + heroId);
				handler.sendErrorMsgToPlayer(GameError.NOT_EMBATTLE_HERO);
				return;
			}
		}

		// 逻辑区分，降低bug率,耦合度降为0
		int missionType = staticMission.getMissionType();
		if (!isFightMission(missionType)) {

			handler.sendErrorMsgToPlayer(GameError.ERROR_MISSION_TYPE);
			return;
		}

		// 国器副本
		if (staticMission.getMissionType() == MissionType.CountryItem) {
			List<Integer> countryEquip = staticMission.getCountryEquip();
			// 国器
			if (countryEquip != null && countryEquip.size() == 3) {
				int id = countryEquip.get(0);
				if (killEquipManager.reachMaxChip(player, id)) {
					handler.sendErrorMsgToPlayer(GameError.REACH_MAX_COUNTRY_CHIP);
					return;
				}
			}
		}

		int mapId = staticMission.getMapId();
		Mission mission = getMission(player, missionId, mapId);
		if (mission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		int state = mission.getState();
		if (state == MissionStateType.fail) {
			state = MissionStateType.Open;
		}
		// 只有新开启状态的副本才可以打
		if (state <= MissionStateType.Lock || state > MissionStateType.Complete) {
			LogHelper.CONFIG_LOGGER.info("common mission state is wrong, state = " + state);
			handler.sendErrorMsgToPlayer(GameError.MISSION_STATE_WRONG);
			return;
		}

		/*************************** 普通关卡-打过的关卡不能再打 ***********************************/
		if (state == MissionStateType.Complete && missionType == MissionType.CommonMission) {
			// LogHelper.CONFIG_LOGGER.info("mission is complete, state = " + state + ", missionId = " + missionId);
			handler.sendErrorMsgToPlayer(GameError.MISSION_STATE_COMPLETE);
			return;
		}

		/******************************************* 战斗演算 ***********************************/
		// 进入副本,查看战斗
		List<Integer> monsterIds = staticMission.getMonsterIds();
		if (monsterIds == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSION_NO_MONSTER);
			return;
		}

		Team monsterTeam = battleDataMgr.initMonsterTeam(monsterIds, BattleEntityType.MONSTER);
		Team playerTeam = battleDataMgr.initPvePlayerTeam(player, heroList, BattleEntityType.HERO);
		// 战斗前
		MissionDoneRs.Builder builder = MissionDoneRs.newBuilder();
		FightBefore.Builder fightBefore = FightBefore.newBuilder();
		// 玩家
		ArrayList<BattleEntity> playerEntities = playerTeam.getAllEnities();
		for (BattleEntity battleEntity : playerEntities) {
			fightBefore.addLeftEntities(battleEntity.wrapPb());
		}

		// 野怪
		ArrayList<BattleEntity> monsterEntities = monsterTeam.getAllEnities();
		for (BattleEntity battleEntity : monsterEntities) {
			fightBefore.addRightEntities(battleEntity.wrapPb());
		}

		builder.setFightBefore(fightBefore);

		// 随机seed不用存盘，没有回放, 种子需要发送到客户端
		Random rand = new Random(System.currentTimeMillis());
		// seed 开始战斗
		battleDataMgr.doTeamBattle(playerTeam, monsterTeam, rand, !ActPassPortTaskType.IS_WORLD_WAR);
		// 战中信息
		CommonPb.FightIn.Builder fightIn = CommonPb.FightIn.newBuilder();
		// 玩家
		ArrayList<AttackInfo> playerAttackInfos = playerTeam.getAttackInfos();
		for (AttackInfo attackInfo : playerAttackInfos) {
			fightIn.addLeftInfo(attackInfo.wrapPb());
		}
		// 野怪
		ArrayList<AttackInfo> monsterAttackInfos = monsterTeam.getAttackInfos();
		for (AttackInfo attackInfo : monsterAttackInfos) {
			fightIn.addRightInfo(attackInfo.wrapPb());
		}
		builder.setFightIn(fightIn);
		SpringUtil.getBean(EventManager.class).enter_stage(player, staticMission.getMissionId(), "");
		// 失败要特殊处理
		// 根据实际扣除玩家体力
		if (playerTeam.isWin()) {
			chatManager.updateChatShow(ChatShowType.PASS_MISSION, staticMission.getMissionId(), player);
			handleMissionWin(player, staticMission, missionId, playerTeam, mission, handler, builder);
			// 记录玩家当前关卡进度
			staticMission.getNextMission().forEach(e -> {
				StaticMission m = staticMissionMgr.getStaticMission(e);
				if (m.getMissionType() == MissionType.CommonMission || m.getMissionType() == MissionType.BossMission) {
					if (player.getLord().getCurMainDupicate() < e.intValue()) {
						player.getLord().setCurMainDupicate(e);
						SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.add_stage);
					}
				}
			});

			// 更新通行证活动进度
            activityEventManager.activityTip(EventEnum.MISSSION_DONE, player, 1);
//            activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.DONE_MISSION_OR_SWEEP, 1);
//            activityManager.updActData(player, ActivityConst.TYPE_ADD, 0L, 1, ActivityConst.ACT_DAILY_MISSION);
			dailyTaskManager.record(DailyTaskId.CLEARANCE, player, 1);
		} else {
			handleMissionFail(player, staticMission, missionId, playerTeam, mission, handler, builder);
			SpringUtil.getBean(EventManager.class).end_stage(player, staticMission.getMissionId(), "", now, playerTeam.isWin() ? 0 : 1, new ArrayList<>());
		}
		// LogHelper.logMission(player, missionId);
	}

	public void handleMissionWin(Player player, StaticMission staticMission, int missionId, Team playerTeam, Mission mission, ClientHandler handler, MissionDoneRs.Builder builder) {

		Lord lord = player.getLord();
		lordManager.subEnergy(lord, staticMission.getWinCost(), Reason.MISSION_WIN);
		int oldMissionState = mission.getState();
		mission.setState(MissionStateType.Complete);
		// 玩家增加经验
		int roleExp = staticMission.getRoleExp();
		if (staticMission.getMissionType() == MissionType.BossMission) {
			float actFactor = activityManager.actDouble(ActivityConst.ACT_COMBAT_EXP_DOUBLE);
			roleExp = (int) ((1 + actFactor) * (double) roleExp);
		}
		// 第一次通关
		if (oldMissionState == MissionStateType.Open) {
			roleExp = staticMission.getFirstExp();
		}

		if (roleExp > 0) {
			lordDataManager.addExp(player, roleExp, Reason.MISSION_DONE);
		}
		eventManager.costEnergy(player, Lists.newArrayList(roleExp, staticMission.getWinCost()));

		// 如果是国器副本则，随机掉落物品
		if (staticMission.getMissionType() == MissionType.CountryItem) {
			List<Integer> countryEquip = staticMission.getCountryEquip();
			// 国器
			if (countryEquip != null && countryEquip.size() == 3) {
				int chipId = countryEquip.get(0);
				int num = countryEquip.get(2);
				int maxChip = killEquipManager.getMaxChip(player, chipId);
				int own = player.getItemNum(chipId);
				if (own < maxChip) {
					int randNum = RandomHelper.randomInSize(1000);
					if (randNum < num) {
						playerManager.addCountryItem(player, AwardType.PROP, chipId, 1, maxChip, Reason.MISSION_DONE);
						Award award = new Award(0, AwardType.PROP, chipId, 1);
						builder.addOthers(award.wrapPb());
					}
					eventManager.getKillEquip(player, Lists.newArrayList("副本", randNum < num, 0));
				} else {
					LogHelper.CONFIG_LOGGER.info("reach max chip num. ");
				}
			}
		}

		// 活动掉落
		try {
			List<List<Integer>> actItemList = activityManager.actItemDrop(ActivityConst.ACT_COMBAT_DROP);
			if (null != actItemList && actItemList.size() > 0) {
				for (List<Integer> list : actItemList) {
					int type = list.get(0);
					int id = list.get(1);
					int count = list.get(2);
					int keyId = playerManager.addAward(player, type, id, count, Reason.ACT_MISSION_DROP);
					builder.addOthers(PbHelper.createAward(player, type, id, count, keyId));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 资源田建筑, 开启资源田建筑, 服务器后期开启的建筑才发送
		// 服务器所有建筑都生成了，只是开启和未开启
		int buildingId = staticMission.getOpenBuildingId();
		if (buildingId != 0) {
//            BuildingBase buildingBase = player.openResBuilding(buildingId);
//            if (buildingBase != null) {
//                builder.setBuilding(buildingBase.wrapPb());
//            }
//            buildingManager.caculateBattleScore(player);

			buildingManager.synBuildings(player, Arrays.asList(buildingId));
		}

		// 资源田
		List<List<Integer>> resourceLand = staticMission.getResourceLandId();
		if (resourceLand != null && !resourceLand.isEmpty()) {
			List<Integer> param = resourceLand.get(0);
			if (param != null && param.size() == 3) {
				int itemId = param.get(0);
				int maxItemNum = param.get(1);
				int lootRate = param.get(2); // 千分比
				int randNum = RandomHelper.randomInSize(1000);
				if (randNum < lootRate) {
					Item item = player.getItem(itemId);
					Award award = new Award(AwardType.PROP, itemId, 1);
					if (item == null) {
						item = playerManager.addItem(player, itemId, 1, Reason.MISSION_DONE);
						builder.addOthers(award.wrapPb());
					} else {
						if (item.getItemNum() >= maxItemNum) {
							// 已经最大值了
						} else {
							playerManager.addAward(player, award, Reason.MISSION_DONE);
							builder.addOthers(award.wrapPb());
						}
					}
				}
			}
		}

		/**************************** 消息回应 ***********************************/
		MissionInfo.Builder missionInfo = MissionInfo.newBuilder();
		missionInfo.setMissionId(missionId);

		int curSoldier = playerTeam.getCurSoldier();
		int maxSoldier = playerTeam.getMaxSoldier();
		int resPercent = GameHelper.getPercent(curSoldier, maxSoldier);
		List<Integer> starCondition = staticMission.getStarCondition();
		int star = 0;
		if (staticMission.getMissionType() == MissionType.BossMission || staticMission.getMissionType() == MissionType.CountryItem) {
			for (int i = 0; i < starCondition.size(); i++) {
				if (resPercent > starCondition.get(i)) {
					star = i + 1;
				} else {
					break;
				}
			}
		}

		if (star <= 0 && staticMission.getMissionType() == MissionType.BossMission) {
			star = 1; // 至少一星
		} else if (staticMission.getMissionType() == MissionType.CommonMission) {
			star = 3;
		}

		missionInfo.setState(MissionStateType.Complete);
		if (mission.getStar() <= star) {
			mission.setStar(star);
		}
		missionInfo.setStar(mission.getStar());
		mission.setState(MissionStateType.Complete);
		builder.setMissionInfo(missionInfo);
		LordInfo.Builder lordInfo = LordInfo.newBuilder();
		lordInfo.setEnergy(lord.getEnergy());
		lordInfo.setLordExp(lord.getExp());
		lordInfo.setLordLevel(lord.getLevel());
		lordInfo.setEnergyCD(playerManager.getEnergyCD(player));
		builder.setLordInfo(lordInfo);
		if (staticMission.getLootIron() > 0) {
			playerManager.addAward(player, AwardType.RESOURCE, ResourceType.IRON, staticMission.getLootIron(), Reason.FIGHT_RESOURCE_MISSION);

			/**
			 * 攻打资源副本资源产出日志埋点
			 */
			com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.IRON), RoleResourceLog.OPERATE_IN, ResourceType.IRON, ResOperateType.MISSION_DONE_IN.getInfoType(), staticMission.getLootIron(), player.account.getChannel()));
			logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, staticMission.getLootIron(), IronOperateType.MISSION_DONE_IN.getInfoType()), ResourceType.IRON);
		}

		List<Hero> heroes = heroDataManager.addAllHeroExp(player, staticMission.getHeroExp(), Reason.MISSION_WIN);
		List<Integer> embattleList = player.getEmbattleList();
		Map<Integer, Hero> heros = player.getHeros();
		for (Integer heroId : embattleList) {
			Hero hero = heros.get(heroId);
			if (hero == null) {
				continue;
			}

			builder.addHeroInfo(PbHelper.createHeroInfoPb(hero));
		}

		openMission(player, builder, staticMission);
		builder.setIsWin(playerTeam.isWin());
		builder.setResource(player.wrapResourcePb());

		doneMissionTask(player, missionId);
		missionMgr.doBossMission(player, mission, staticMission, false);

		// 发放美女模型
		try {
			List<Integer> beautyAward = staticMission.getBeautyAward();
			if (null != beautyAward && beautyAward.size() > 0) {
				playerManager.addAward(player, beautyAward.get(0), beautyAward.get(1), beautyAward.get(2), Reason.MISSION_WIN);
			}

			// 战役掉落兵书
			boolean open = staticOpenManger.isOpen(68, player);
			if (open) {
				StaticWarBook staticWarBook = warBookManager.missionDropWarBook();
				if (null != staticWarBook) {
					int keyId = playerManager.addAward(player, AwardType.WAR_BOOK, staticWarBook.getId(), 1, Reason.MISSION_WIN);
					builder.addOthers(PbHelper.createAward(player, AwardType.WAR_BOOK, staticWarBook.getId(), 1, keyId));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		handler.sendMsgToPlayer(MissionDoneRs.ext, builder.build());
		heroDataManager.synBattleScoreAndHeroList(player, heroes);
		// System.out.println(builder.build());
		/**
		 * 玩家关卡日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.roleMissonLog(new RoleMissonLog(player.getLord().getLordId(), serverManager.getServerId(), player.account.getChannel(), mission.getMissionId()));
		Date now = new Date();
		List<List<Integer>> rewards = new ArrayList<>();
		builder.getOthersList().forEach(e -> {
			rewards.add(Lists.newArrayList(e.getType(), e.getId(), e.getCount()));
		});
		SpringUtil.getBean(EventManager.class).end_stage(player, staticMission.getMissionId(), "", now, playerTeam.isWin() ? 0 : 1, rewards);
	}

	public void handleMissionFail(Player player, StaticMission staticMission, int missionId, Team playerTeam, Mission mission, ClientHandler handler, MissionDoneRs.Builder builder) {

		Lord lord = player.getLord();
		lordManager.subEnergy(lord, staticMission.getFailedCost(), Reason.MISSION_FAILED);
		mission.setState(MissionStateType.fail);
		/**************************** 消息回应 ***********************************/
		MissionInfo.Builder missionInfo = MissionInfo.newBuilder();
		missionInfo.setMissionId(missionId);

		missionInfo.setStar(0);
		missionInfo.setState(MissionStateType.fail);
		builder.setMissionInfo(missionInfo);
		int addExp = staticMission.getFailExp();
		List<Hero> heroes = null;
		if (addExp >= 0) {
			heroes = heroDataManager.addAllHeroExp(player, addExp, Reason.MISSION_FAILED);
		}
		eventManager.costEnergy(player, Lists.newArrayList(addExp, staticMission.getFailedCost()));
		List<Integer> embattleList = player.getEmbattleList();
		Map<Integer, Hero> heros = player.getHeros();
		for (Integer heroId : embattleList) {
			Hero hero = heros.get(heroId);
			if (hero == null) {
				continue;
			}

			builder.addHeroInfo(PbHelper.createHeroInfoPb(hero));
		}

		LordInfo.Builder lordInfo = LordInfo.newBuilder();
		lordInfo.setEnergy(lord.getEnergy());
		lordInfo.setLordExp(lord.getExp());
		lordInfo.setLordLevel(lord.getLevel());
		lordInfo.setEnergyCD(playerManager.getEnergyCD(player));
		builder.setLordInfo(lordInfo);

		builder.setIsWin(playerTeam.isWin());
		handler.sendMsgToPlayer(MissionDoneRs.ext, builder.build());
		heroDataManager.synBattleScoreAndHeroList(player, heroes);
	}

	public boolean isFightMission(int missionType) {
		return missionType == MissionType.CommonMission || missionType == MissionType.BossMission || missionType == MissionType.CountryItem || missionType == MissionType.ResourceLand || missionType == MissionType.ResourceLandChip;
	}

	public void openMission(Player player, MissionDoneRs.Builder builder, StaticMission staticMission) {
		List<Integer> nextMission = staticMission.getNextMission();
		Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
		int mapId = staticMission.getMapId();
		Map<Integer, Mission> missInfo = missions.get(mapId);
		if (missInfo == null) {
			missInfo = new HashMap<Integer, Mission>();
			missions.put(mapId, missInfo);
		}

		// 需要进行存盘
		for (Integer nextMissionId : nextMission) {
			StaticMission nextStaticMission = staticMissionMgr.getStaticMission(nextMissionId);
			if (nextStaticMission == null) {
				// LogHelper.CONFIG_LOGGER.info("nextStaticMission is null!");
				continue;
			}

			// 检查map是否存在
			Map<Integer, Mission> mapInfo = missions.get(nextStaticMission.getMapId());
			if (mapInfo == null) {
				mapInfo = new HashMap<Integer, Mission>();
				missions.put(nextStaticMission.getMapId(), mapInfo);
			}

			if (!missionMgr.hasMission(player, nextMissionId, nextStaticMission.getMapId())) {
				Mission mission = missionMgr.addNextMission(player, nextMissionId, builder);
				if (missInfo != null) {
					missionMgr.doBossMission(player, mission, nextStaticMission, true);
				}
			}

		}
	}
	// private Logger logger = LoggerFactory.getLogger(getClass());

	// 配置：次数, 时间[分两个协议]
	public void resourceRq(ResourceMissionRq req, ClientHandler handler) {
		// check player
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		// logger.info("player resource start: {} ",player.getResource().wrapPb().build());
		int missionId = req.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		// check mission exists or not.
		if (staticMission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSION_CONFIG_NOT_EXISTS);
			return;
		}

		if (staticMission.getMissionType() != MissionType.ResourceMission) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_MISSION_TYPE);
			return;
		}

		int mapId = staticMission.getMapId();
		Mission mission = getMission(player, missionId, mapId);
		// getMission inner already send error.
		if (mission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		// check left time
		long resoureEndTime = mission.getResourceEndTime();
		long now = System.currentTimeMillis();
		if (resoureEndTime < now) {
			handler.sendErrorMsgToPlayer(GameError.MISSION_RESOURCE_TIME_END);
			return;
		}

		List<Integer> resoureInfo = staticMission.getResource();
		if (resoureInfo.size() != 5) {
			handler.sendErrorMsgToPlayer(GameError.RESOURCE_CONFIG_ERROR);
			return;
		}
		ResourceMissionRs.Builder builder = ResourceMissionRs.newBuilder();
		// 还有攻打次数
		if (mission.getFightTimes() > 0) {
			int fightTimes = Math.min(resoureInfo.get(2), mission.getFightTimes());
			mission.setFightTimes(fightTimes - 1);
			int resourceType = resoureInfo.get(0);
			int resourceNum = resoureInfo.get(1);
			playerManager.addAward(player, AwardType.RESOURCE, resourceType, resourceNum, Reason.FIGHT_RESOURCE_MISSION);
			builder.setAward(PbHelper.createAward(AwardType.RESOURCE, resourceType, resourceNum));
			/**
			 * 攻打资源副本资源产出日志埋点
			 */
			com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
			logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(resourceType), RoleResourceLog.OPERATE_IN, resourceType, ResOperateType.RES_MISSION_IN.getInfoType(), resourceNum, player.account.getChannel()));
			int type = 0;
			int resType = resourceType;
			switch (resType) {
				case ResourceType.IRON:
					type = IronOperateType.RES_MISSION_IN.getInfoType();
					break;
				case ResourceType.COPPER:
					type = CopperOperateType.RES_MISSION_IN.getInfoType();
					break;
				case ResourceType.OIL:
					type = OilOperateType.RES_MISSION_IN.getInfoType();
					break;
				case ResourceType.STONE:
					type = StoneOperateType.RES_MISSION_IN.getInfoType();
					break;
				default:
					break;
			}
			if (type != 0) {
				logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, resourceNum, type), resType);
			}
		} else {
			int buyTimes = mission.getBuyTimes();
			if (buyTimes >= resoureInfo.get(3)) {
				handler.sendErrorMsgToPlayer(GameError.REACH_MAX_BUY_RESOURCE_TIMES);
				return;
			}

			int owned = player.getGold();

			// 获取当前需要的金币
			int currentTimes = buyTimes + 1;
			int need = staticMission.getBuyGold(currentTimes);
			if (need == Integer.MAX_VALUE) {
				handler.sendErrorMsgToPlayer(GameError.RESOURCE_CONFIG_ERROR);
				return;
			}

			if (owned < need) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}

			playerManager.subAward(player, AwardType.GOLD, 0, need, Reason.BUY_MISSION_RESOURCE_TIMES);

			mission.setFightTimes(resoureInfo.get(2));
			mission.setBuyTimes(mission.getBuyTimes() + 1);
		}

		// 返回结果

		builder.setMissionId(missionId);
		builder.setBuyTimes(mission.getBuyTimes());
		builder.setFightTimes(mission.getFightTimes());
		builder.setEndTime(mission.getResourceEndTime());
		builder.setResource(player.wrapResourcePb());
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(ResourceMissionRs.ext, builder.build());

		/**
		 * 玩家关卡日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.roleMissonLog(new RoleMissonLog(player.getLord().getLordId(), serverManager.getServerId(), player.account.getChannel(), mission.getMissionId()));

		// logger.info("player resource end : {} ",player.getResource().wrapPb().build());

	}

	// 装备图纸,元宝购买,有次数上限
	public void equipPaperMissionRq(EquipPaperMissionRq req, ClientHandler handler) {
		// check player
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int missionId = req.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		// check mission exists or not.
		if (staticMission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSION_CONFIG_NOT_EXISTS);
			return;
		}

		if (staticMission.getMissionType() != MissionType.EquipPaper) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_MISSION_TYPE);
			return;
		}

		int mapId = staticMission.getMapId();
		Mission mission = getMission(player, missionId, mapId);
		if (mission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		// add item
		List<List<Integer>> equipPaperList = staticMission.getEquipPaper();
		if (equipPaperList == null) {
			LogHelper.CONFIG_LOGGER.info("equipPaperList is null.");
			handler.sendErrorMsgToPlayer(GameError.EQUIP_PAPER_CONFIG_ERROR);
			return;
		}

		// 检查购买次数是否足够
		int buyTimes = mission.getBuyEquipPaperTimes();
		if (buyTimes >= equipPaperList.size()) {
			handler.sendErrorMsgToPlayer(GameError.REACH_MAX_EQUIP_PAPER_BUY_TIMES);
			return;
		}

		List<Integer> awardList = equipPaperList.get(buyTimes);
		if (awardList == null) {
			LogHelper.CONFIG_LOGGER.info("awardList is null.");
			handler.sendErrorMsgToPlayer(GameError.EQUIP_PAPER_CONFIG_ERROR);
			return;
		}

		// 检查元宝是否充足
		int currentBuyTimes = buyTimes + 1;
		int need = staticMission.getBuyGold(currentBuyTimes);
		if (need == Integer.MAX_VALUE) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_PAPER_CONFIG_ERROR);
			return;
		}

		int owned = player.getGold();
		if (owned < need) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// remove gold
		playerManager.subAward(player, AwardType.GOLD, 1, need, Reason.BUY_MISSION_EQUIP_PAPER);

		// incre times
		mission.increBuyEquipTimes();

		int propId = awardList.get(0);
		int propNum = awardList.get(1);
		// add item
		playerManager.addItem(player, propId, propNum, Reason.BUY_MISSION_EQUIP_PAPER);

		// sync info to client
		EquipPaperMissionRs.Builder builder = EquipPaperMissionRs.newBuilder();
		builder.setMissionId(missionId);
		builder.setGold(player.getGold());
		builder.setProp(PbHelper.createItemPb(propId, propNum));
		builder.setBuyTimes(mission.getBuyEquipPaperTimes());

		handler.sendMsgToPlayer(EquipPaperMissionRs.ext, builder.build());
	}

	// hire hero, set mission state
	// 武将购买,配置两个，送一个，其中一个需要元宝购买
	public void heroHired(MissionPb.HeroMissionRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int missionId = req.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		// 关卡不存在
		if (staticMission == null) {
			LogHelper.CONFIG_LOGGER.info("staticMission is null, missionId = " + missionId);
			handler.sendErrorMsgToPlayer(GameError.MISSION_CONFIG_NOT_EXISTS);
			return;
		}

		int mapId = staticMission.getMapId();
		Mission mission = getMission(player, missionId, mapId);
		if (mission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		int heroId = req.getHeroId();
		int freeHeroId = staticMission.getFreeHeroId();
		if (freeHeroId == heroId) {
			boolean hasFreeHero = heroDataManager.hasHeroType(player, freeHeroId);
			if (hasFreeHero) {
				handler.sendErrorMsgToPlayer(GameError.FREE_HERO_ALREADY_HAS);
				return;
			}

			heroFreeHired(player, heroId, mission, handler);
		} else {
			heroGoldHired(player, staticMission, mission, handler);
		}

		doMissionHired(player, heroId);

	}

	public void heroFreeHired(Player player, int heroId, Mission mission, ClientHandler handler) {
		int missionId = mission.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		if (staticMission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		int freeHeroId = staticMission.getFreeHeroId();
		if (freeHeroId != heroId) {
			handler.sendErrorMsgToPlayer(GameError.NOT_FREE_HERO_ID);
			return;
		}

		// add heroId
		playerManager.addAward(player, AwardType.HERO, freeHeroId, 1, Reason.MISSION_HIRE_HERO);
		Hero hero = player.getHero(freeHeroId);
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}
		int i = 0;
		for (int emHero : player.getEmbattleList()) {
			if (emHero > 0) {
				i++;
			}

		}
		// 107关卡送的英雄 默认满兵上阵
		if (mission.getMissionId() == 107 && i < 2) {
			hero.setCurrentSoliderNum(hero.getSoldierNum());
			player.getEmbattleList().set(1, hero.getHeroId());

		}
		mission.setState(MissionStateType.Complete);
		HeroMissionRs.Builder builder = HeroMissionRs.newBuilder();
		builder.setMissionId(missionId);
		builder.setAddHero(hero.wrapPb());
		builder.setGold(player.getGold());
		builder.setIsHeroBought(true);
		builder.setState(MissionStateType.Complete);
		handler.sendMsgToPlayer(HeroMissionRs.ext, builder.build());

		// 推送上阵信息
		HeroPb.SynEmbattleInfoRq.Builder synEmbattleInfo = HeroPb.SynEmbattleInfoRq.newBuilder();
		synEmbattleInfo.addAllHeroId(player.getEmbattleList());
		SynHelper.synMsgToPlayer(player, HeroPb.SynEmbattleInfoRq.EXT_FIELD_NUMBER, HeroPb.SynEmbattleInfoRq.ext, synEmbattleInfo.build());
	}

	public void heroGoldHired(Player player, StaticMission staticMission, Mission mission, ClientHandler handler) {
		// check price config
		List<Integer> heroInfo = staticMission.getHeroInfo();
		if (heroInfo == null || heroInfo.size() != 2) {
			LogHelper.CONFIG_LOGGER.info("mission hero config error!");
			handler.sendErrorMsgToPlayer(GameError.MISSION_HERO_CONFIG_ERROR);
			return;
		}

		// check price enough or not?
		int heroId = heroInfo.get(0);
		if (heroDataManager.hasHeroType(player, heroId)) {
			handler.sendErrorMsgToPlayer(GameError.HERO_TYPE_ALREADY_EXISTS);
			return;
		}

		int heroPrice = heroInfo.get(1);
		Lord lord = player.getLord();
		int owned = lord.getGold();
		if (owned < heroPrice) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// add heroId
		playerManager.addAward(player, AwardType.HERO, heroId, 1, Reason.MISSION_HIRE_HERO);
		Hero hero = player.getHero(heroId);
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}

		// remove price
		playerManager.subAward(player, AwardType.GOLD, 1, heroPrice, Reason.MISSION_HIRE_HERO);

		HeroMissionRs.Builder builder = HeroMissionRs.newBuilder();
		builder.setMissionId(mission.getMissionId());
		builder.setAddHero(hero.wrapPb());
		builder.setGold(lord.getGold());
		builder.setIsHeroBought(true);
		mission.setHeroBought(true);
		mission.setState(MissionStateType.Complete);
		builder.setState(MissionStateType.Complete);
		handler.sendMsgToPlayer(HeroMissionRs.ext, builder.build());

	}

	/**
	 * Function: 关卡扫荡
	 */
	public void sweepMission(MissionPb.SweepMissionRq req, ClientHandler handler) {
		// check player
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int missionId = req.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		// check mission exists or not.
		if (staticMission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSION_CONFIG_NOT_EXISTS);
			return;
		}

		// 只有boss关卡才能扫荡
		if (staticMission.getMissionType() != MissionType.BossMission) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_MISSION_TYPE);
			return;
		}

		int mapId = staticMission.getMapId();
		Mission mission = getMission(player, missionId, mapId);
		if (mission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		Lord lord = player.getLord();
		int time = lord.getEnergy() / staticMission.getWinCost();
		time = (time >= 5 ? CommonDefine.SWEEP_TIMES : time);
		// 检查体力值是否够
		if (time == 0) {
			handler.sendErrorMsgToPlayer(GameError.ENERGY_COST_ERROR);
			return;
		}
		int cost = staticMission.getWinCost() * time;
		if (mission.getStar() < 1) {
			handler.sendErrorMsgToPlayer(GameError.MISSION_STAR_ERROR);
			return;
		}

		// 小于3星的判断是
		if (mission.getStar() < CommonDefine.MISSION_STAR) {
			// 是否vip开启
			StaticVip staticVip = staticVipMgr.getStaticVip(player.getVip());
			if (staticVip == null) {
				handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
				return;
			}

			// vip等级不够
			if (staticVip.getWipeCombat() != 1) {
				handler.sendErrorMsgToPlayer(GameError.VIP_NOT_ENOUGH);
				return;
			}
		}

		int heroExp = staticMission.getHeroExp() * time;
		int playerExp = staticMission.getRoleExp() * time;

		// 扫荡活动翻倍
		if (staticMission.getMissionType() == MissionType.BossMission) {
			float actFactor = activityManager.actDouble(ActivityConst.ACT_COMBAT_EXP_DOUBLE);
			playerExp = (int) ((1 + actFactor) * (double) playerExp);
		}
		eventManager.costEnergy(player, Lists.newArrayList(playerExp, cost));

		lordDataManager.addExp(player, playerExp, Reason.MISSION_DONE);
		lordDataManager.subEnergy(lord, cost, Reason.SWEEP_MISSION);
		MissionPb.SweepMissionRs.Builder builder = MissionPb.SweepMissionRs.newBuilder();
		builder.setLordLevel(lord.getLevel());
		builder.setLordExp(lord.getExp());
		builder.setEnergy(lord.getEnergy());
		builder.setEnergyCD(playerManager.getEnergyCD(player));
		Map<Integer, Hero> heros = player.getHeros();
		List<Integer> heroList = new ArrayList<>();
		if (player.getSweepHeroList().size() > 0) {
			heroList.addAll(player.getSweepHeroList());
		} else {
			heroList.addAll(player.getEmbattleList());
		}
		List<Hero> heroes = heroDataManager.addAllHeroExp(player, heroList, heroExp, Reason.SWEEP_MISSION);
		for (Integer heroId : heroList) {
			Hero hero = heros.get(heroId);
			if (hero == null) {
				continue;
			}

			builder.addHeroInfo(PbHelper.createHeroInfoPb(hero));
		}

		// 活动掉落
		try {
			if (time > 0) {
				List<Award> awardList = new ArrayList<>();
				for (int i = 0; i < time; i++) {
					List<List<Integer>> actItemList = activityManager.actItemDrop(ActivityConst.ACT_COMBAT_DROP);
					if (null != actItemList && actItemList.size() > 0) {
						for (List<Integer> list : actItemList) {
							int type = list.get(0);
							int id = list.get(1);
							int count = list.get(2);
							int keyId = playerManager.addAward(player, type, id, count, Reason.ACT_MISSION_DROP);
							boolean find = false;
							for (Award award : awardList) {
								if (award.getType() == type && award.getId() == id) {
									award.setCount(award.getCount() + count);
									find = true;
									break;
								}
							}
							if (!find) {
								awardList.add(new Award(keyId, type, id, count));
							}
						}
					}

					// 战役掉落兵书
					boolean open = staticOpenManger.isOpen(68, player);
					if (open) {
						// 战役掉落兵书
						StaticWarBook staticWarBook = warBookManager.missionDropWarBook();
						if (null != staticWarBook) {
							int keyId = playerManager.addAward(player, AwardType.WAR_BOOK, staticWarBook.getId(), 1, Reason.SWEEP_MISSION);
							builder.addOthers(PbHelper.createAward(player, AwardType.WAR_BOOK, staticWarBook.getId(), 1, keyId));
						}
					}
				}

				for (Award award : awardList) {
					builder.addOthers(PbHelper.createAward(player, award.getType(), award.getId(), award.getCount(), award.getKeyId()));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		handler.sendMsgToPlayer(MissionPb.SweepMissionRs.ext, builder.build());
		heroDataManager.synBattleScoreAndHeroList(player, heroes);

		// TODO 战役通关相关事件
        activityEventManager.activityTip(EventEnum.MISSSION_DONE, player, time);
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.DONE_MISSION_OR_SWEEP, time);
//        activityManager.updActData(player, ActivityConst.TYPE_ADD, 0L, time, ActivityConst.ACT_DAILY_MISSION);
		dailyTaskManager.record(DailyTaskId.CLEARANCE, player, time);
	}

	public void doneMissionTask(Player player, int missionId) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(missionId);
		taskManager.doTask(TaskType.DONE_MISSION, player, triggers);
	}

	public void doMissionHired(Player player, int heroId) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(heroId);
		taskManager.doTask(TaskType.MISSION_HIRE_HERO, player, triggers);
		taskManager.doTask(TaskType.MISSION_HIRE_ANY, player, null);
	}

	// 随机英雄
	// 如果两个英雄都没有，则随机一个英雄为免费英雄送出去
	// 如果有一个英雄，则另外一个英雄为出钱英雄
	// 如果玩家都有配置里面的英雄，说明客户端发疯了，乱发
	// 这是一个很长很长的函数,希望有些人能看得懂
	public void heroHiredNew(MissionPb.HeroMissionRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int missionId = req.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		// 关卡不存在
		if (staticMission == null) {
			LogHelper.CONFIG_LOGGER.info("staticMission is null, missionId = " + missionId);
			handler.sendErrorMsgToPlayer(GameError.MISSION_CONFIG_NOT_EXISTS);
			return;
		}

		int mapId = staticMission.getMapId();
		Mission mission = getMission(player, missionId, mapId);
		if (mission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		List<List<Integer>> randHero = staticMission.getRandHero();
		// 英雄副本最少一个英雄
		if (randHero.size() < 1) {
			LogHelper.CONFIG_LOGGER.info("randHero.size() < 1");
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		HashSet<Integer> allHeroIds = new HashSet<Integer>();
		for (List<Integer> heroInfo : randHero) {
			if (heroInfo.size() != 3) {
				LogHelper.CONFIG_LOGGER.info("heroInfo.size() != 3!");
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}
			allHeroIds.add(heroInfo.get(0));
		}

		if (allHeroIds.size() <= 0) {
			LogHelper.CONFIG_LOGGER.info("allHeroId size is <= 0");
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		// 拥有的英雄
		HashSet<Integer> heroHas = getPlayerHas(player, allHeroIds);
		if (heroHas.size() >= allHeroIds.size()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_ALL_HEROES);
			return;
		}

		// 尚未拥有的英雄
		Set<Integer> heroNotHas = new HashSet<Integer>();
		heroNotHas.addAll(allHeroIds);
		heroNotHas.removeAll(heroHas);
		int heroId = 0;
		if (allHeroIds.size() == 1) { // free
			Iterator<Integer> heroIt = allHeroIds.iterator();
			while (heroIt.hasNext()) {
				Integer elem = heroIt.next();
				if (elem != null) {
					heroId = elem;
					break;
				}
			}

			if (heroId == 0) {
				LogHelper.CONFIG_LOGGER.info("heroId is 0.");
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}

			heroFreeHiredNew(player, heroId, mission, handler);
		} else if (allHeroIds.size() == 2) { // check
			if (heroNotHas.size() == 2) {
				// randHero
				int randNum = RandomHelper.threadSafeRand(1, 100);
				int num = 0;
				for (List<Integer> heroInfo : randHero) {
					num += heroInfo.get(1);
					if (randNum <= num) {
						heroId = heroInfo.get(0);
						break;
					}
				}
				// free hero action.
				if (heroId != 0) {
					heroFreeHiredNew(player, heroId, mission, handler);
				} else {
					LogHelper.CONFIG_LOGGER.info("heroId is 0, something wrong here, [heroNotHas.size() == 2]");
				}

			} else if (heroNotHas.size() == 1) { // 已经有一个英雄了, 需要购买了
				Iterator<Integer> heroIt = heroNotHas.iterator();
				while (heroIt.hasNext()) {
					Integer elem = heroIt.next();
					if (elem != null) {
						heroId = elem;
						break;
					}
				}

				if (heroId == 0) {
					LogHelper.CONFIG_LOGGER.info("heroId is 0.");
					handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
					return;
				}

				int heroPrice = 0;
				for (List<Integer> heroInfo : randHero) {
					if (heroInfo.get(0) == heroId) {
						heroPrice = heroInfo.get(2);
						break;
					}
				}

				heroGoldHiredNew(player, heroId, heroPrice, mission, handler);
			}
		} else {
			LogHelper.CONFIG_LOGGER.info("allHeroIds is not 1 or 2.");
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		// 做任务, heroId = 0的英雄不做任务
		if (heroId != 0) {
			doMissionHired(player, heroId);
		} else {
			LogHelper.CONFIG_LOGGER.info("heroId is 0 at last line.");
		}

	}

	public void heroFreeHiredNew(Player player, int freeHeroId, Mission mission, ClientHandler handler) {
		int missionId = mission.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		if (staticMission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		// add heroId
		playerManager.addAward(player, AwardType.HERO, freeHeroId, 1, Reason.MISSION_HIRE_HERO);
		Hero hero = player.getHero(freeHeroId);
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}
		mission.setState(MissionStateType.Complete);
		HeroMissionRs.Builder builder = HeroMissionRs.newBuilder();
		builder.setMissionId(missionId);
		builder.setAddHero(hero.wrapPb());
		builder.setGold(player.getGold());
		builder.setIsHeroBought(false);
		builder.setState(MissionStateType.Complete);
		handler.sendMsgToPlayer(HeroMissionRs.ext, builder.build());
	}

	public HashSet<Integer> getPlayerHas(Player player, HashSet<Integer> allHeroIds) {
		HashSet<Integer> heroHas = new HashSet<Integer>();
		for (Integer heroId : allHeroIds) {
			if (heroDataManager.hasHeroType(player, heroId)) {
				heroHas.add(heroId);
			}
		}

		return heroHas;
	}

	public void heroGoldHiredNew(Player player, int heroId, int heroPrice, Mission mission, ClientHandler handler) {
		// check price config
		Lord lord = player.getLord();
		int owned = lord.getGold();
		if (owned < heroPrice) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// add heroId
		playerManager.addAward(player, AwardType.HERO, heroId, 1, Reason.MISSION_HIRE_HERO);
		Hero hero = player.getHero(heroId);
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
			return;
		}

		// remove price
		playerManager.subAward(player, AwardType.GOLD, 1, heroPrice, Reason.MISSION_HIRE_HERO);

		HeroMissionRs.Builder builder = HeroMissionRs.newBuilder();
		builder.setMissionId(mission.getMissionId());
		builder.setAddHero(hero.wrapPb());
		builder.setGold(lord.getGold());
		builder.setIsHeroBought(true);
		mission.setHeroBought(true);
		mission.setState(MissionStateType.Complete);
		builder.setState(MissionStateType.Complete);
		handler.sendMsgToPlayer(HeroMissionRs.ext, builder.build());
	}

	public void getStartInfoRq(MissionPb.GetStarAwardRq req, ClientHandler handler) {
		// check player
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int missionId = req.getMissionId();
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		// check mission exists or not.
		if (staticMission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSION_CONFIG_NOT_EXISTS);
			return;
		}

		// 只有boss关卡才能领奖
		if (staticMission.getMissionType() != MissionType.BossMission) {
			handler.sendErrorMsgToPlayer(GameError.ERROR_MISSION_TYPE);
			return;
		}

		int mapId = staticMission.getMapId();
		Mission mission = getMission(player, missionId, mapId);
		if (mission == null) {
			handler.sendErrorMsgToPlayer(GameError.MISSIONID_NOT_EXISTS);
			return;
		}

		TreeMap<Integer, TreeMap<Integer, Integer>> missionStar = player.getMissionStar();
		TreeMap<Integer, Integer> stateMap = missionStar.get(missionId);
		if (stateMap == null) {
			stateMap = new TreeMap<Integer, Integer>();
			for (int i = 1; i <= 3; i++) {
				stateMap.put(i, MissionStar.MISSION_STAR_CLOSE);
			}
			missionStar.put(missionId, stateMap);
		}

		int starNum = req.getStarNum();
		Integer state = stateMap.get(starNum);
		if (state == null) {
			handler.sendErrorMsgToPlayer(GameError.STAR_NUM_ERROR);
			return;
		}

		if (state != MissionStar.MISSION_STAR_AWARD_OK) {
			handler.sendErrorMsgToPlayer(GameError.STAR_STATE_ERROR);
			return;
		}

		List<List<Integer>> award = staticMission.getAward(starNum);
		if (award == null || award.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		List<Award> awards = new ArrayList<Award>();
		for (List<Integer> awardItem : award) {
			if (awardItem.size() != 3) {
				continue;
			}
			awards.add(new Award(awardItem.get(0), awardItem.get(1), awardItem.get(2)));
		}

		playerManager.addAward(player, awards, Reason.MISSION_STAR);
		int missionState = MissionStar.MISSION_STAR_AWARD_TAKEN;
		stateMap.put(starNum, missionState);
		MissionPb.GetStarAwardRs.Builder builder = MissionPb.GetStarAwardRs.newBuilder();
		builder.setMissionId(missionId);
		builder.setState(missionState);
		for (Award a : awards) {
			builder.addAward(a.wrapPb());
		}
		LordInfo.Builder lordInfo = LordInfo.newBuilder();
		Lord lord = player.getLord();
		if (lord != null) {
			lordInfo.setEnergy(lord.getEnergy());
			lordInfo.setLordExp(lord.getExp());
			lordInfo.setLordLevel(lord.getLevel());
			lordInfo.setEnergyCD(playerManager.getEnergyCD(player));
			builder.setLordInfo(lordInfo);
		}

		handler.sendMsgToPlayer(MissionPb.GetStarAwardRs.ext, builder.build());
	}

	// 获取星级奖励
	public void getStartInfoRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		Map<Integer, Map<Integer, Mission>> missions = player.getMissions();
		MissionPb.GetAllStarInfoRs.Builder builder = MissionPb.GetAllStarInfoRs.newBuilder();
		TreeMap<Integer, TreeMap<Integer, Integer>> missionStar = player.getMissionStar();
		for (Map.Entry<Integer, Map<Integer, Mission>> mapItem : missions.entrySet()) {
			if (mapItem == null) {
				continue;
			}

			// one MissionMap
			Map<Integer, Mission> missionMap = mapItem.getValue();
			if (missionMap == null) {
				continue;
			}

			for (Map.Entry<Integer, Mission> missionItem : missionMap.entrySet()) {
				if (missionItem == null || missionItem.getValue() == null) {
					continue;
				}
				Mission mission = missionItem.getValue();
				int missionId = mission.getMissionId();
				StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
				if (staticMission == null) {
					continue;
				}

				if (staticMission.getMissionType() == MissionType.BossMission) {
					CommonPb.MissionStarInfo.Builder ms = CommonPb.MissionStarInfo.newBuilder();
					ms.setId(missionId);
					TreeMap<Integer, Integer> stateMap = missionStar.get(missionId);
					if (stateMap == null) {
						stateMap = new TreeMap<Integer, Integer>();
						for (int i = 1; i <= 3; i++) {
							stateMap.put(i, MissionStar.MISSION_STAR_CLOSE);
						}
						missionStar.put(missionId, stateMap);
					}

					for (int i = 1; i <= mission.getStar(); i++) {
						Integer curState = stateMap.get(i);
						if (curState == MissionStar.MISSION_STAR_CLOSE) {
							stateMap.put(i, MissionStar.MISSION_STAR_AWARD_OK);
						}
					}

					for (Integer state : stateMap.values()) {
						ms.addState(state);
					}
					builder.addStarInfo(ms);
				}
			}
		}
		handler.sendMsgToPlayer(GameError.OK, MissionPb.GetAllStarInfoRs.ext, builder.build());
	}

	public void getSweepHeroRq(GetSweepHeroHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		MissionPb.GetSweepHeroRs.Builder builder = MissionPb.GetSweepHeroRs.newBuilder();
		if (player.getSweepHeroList().size() > 0) {
			builder.addAllSweepHero(player.getSweepHeroList());
		} else {
			builder.addAllSweepHero(player.getEmbattleList());
		}
		handler.sendMsgToPlayer(GameError.OK, MissionPb.GetSweepHeroRs.ext, builder.build());
	}

	public void updateSweepHero(UpdateSweepHeroHandler handler, MissionPb.UpdateSweepHeroRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("UpdateSweepHero player is null, roleId = " + handler.getRoleId());
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (rq.getSweepHeroCount() > 4) {
			LogHelper.CONFIG_LOGGER.info("UpdateSweepHero hero count {} error ", rq.getSweepHeroCount());
			handler.sendErrorMsgToPlayer(GameError.SWEEP_HERO_INFO_ERROR);
			return;
		}
		for (Integer hero : rq.getSweepHeroList()) {
			if (hero < 1) {
				LogHelper.CONFIG_LOGGER.info("UpdateSweepHero hero id {} error ", rq.getSweepHeroList());
				handler.sendErrorMsgToPlayer(GameError.SWEEP_HERO_INFO_ERROR);
				return;
			}
		}

		player.getSweepHeroList().clear();
		player.getSweepHeroList().addAll(rq.getSweepHeroList());
		handler.sendMsgToPlayer(GameError.OK, MissionPb.UpdateSweepHeroRs.ext, MissionPb.UpdateSweepHeroRs.newBuilder().build());
	}

	public void refushAllPlayerMission(int missionId, int preMissionId) {
		StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
		StaticMission preStaticMission = staticMissionMgr.getStaticMission(preMissionId);
		playerManager.getPlayers().values().forEach(player -> {
			try {
				Mission preMission = getMission(player, preStaticMission.getMissionId(), preStaticMission.getMapId());
				if (preMission != null) {
					if (preStaticMission.getMissionType() == MissionType.ResourceMission // 资源副本 不论开启状态
						|| preMission.getState() == MissionStateType.Complete) {// 其他副本
						// 直接打开
						Mission misson = getMission(player, staticMission.getMissionId(), staticMission.getMapId());
						if (misson == null) {
							misson = new Mission();
							misson.setMissionId(staticMission.getMissionId());
							misson.setMapId(staticMission.getMapId());
							misson.setState(MissionStateType.Open);
							misson.setStar(0);
							misson.setType(staticMission.getMissionType());

							// 资源副本信息: 剩余时间、关卡攻打次数、关卡购买次数
							List<Integer> resoureInfo = staticMission.getResource();
							if (resoureInfo != null && resoureInfo.size() == 5) {
								misson.setFightTimes(resoureInfo.get(2));
								misson.setResourceEndTime(TimeHelper.getEndTime(resoureInfo.get(4) * 1000L));
							} else {
								misson.setResourceEndTime(0);
								misson.setFightTimes(0);
							}
							// 资源副本购买次数
							misson.setBuyTimes(0);
							// 装备图纸副本
							misson.setBuyEquipPaperTimes(0);
							misson.setCountryItemNum(0);
							misson.setHeroBought(false);
							misson.setResourceLandNum(0);
							missionMgr.addMisson(player, misson);
						}
						int freeHeroId = staticMission.getFreeHeroId();
						boolean hasFreeHero = heroDataManager.hasHeroType(player, freeHeroId);
						if (hasFreeHero) {
							misson.setHeroBought(true);
							misson.setState(MissionStateType.Complete);
						}
					}
				}
			} catch (Exception e) {

			}
		});
	}
}
