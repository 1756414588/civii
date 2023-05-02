package com.game.service;

import com.game.constant.AwardType;
import com.game.constant.BroodWarRank;
import com.game.constant.BroodWarState;
import com.game.constant.ChatId;
import com.game.constant.CityId;
import com.game.constant.CityType;
import com.game.constant.ConditionType;
import com.game.constant.GameError;
import com.game.constant.MapId;
import com.game.constant.MarchReason;
import com.game.constant.PropertyType;
import com.game.constant.Reason;
import com.game.constant.ResourceType;
import com.game.constant.SimpleId;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticBroodWarMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldCityTypeMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.BroodWarInfo;
import com.game.domain.p.BroodWarPosition;
import com.game.domain.p.BroodWarReport;
import com.game.domain.p.City;
import com.game.domain.p.Hero;
import com.game.domain.p.RankPvpHero;
import com.game.domain.p.SimpleData;
import com.game.domain.p.Team;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticBroodWarBuff;
import com.game.domain.s.StaticBroodWarCommand;
import com.game.domain.s.StaticBroodWarKillScore;
import com.game.domain.s.StaticBroodWarShop;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticWorldCityType;
import com.game.log.constant.OilOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.domain.NewBroodWarBuyBuffLog;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.BattleMgr;
import com.game.manager.BroodWarManager;
import com.game.manager.ChatManager;
import com.game.manager.CityManager;
import com.game.manager.CondMgr;
import com.game.manager.CountryManager;
import com.game.manager.HeroManager;
import com.game.manager.PlayerManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.BroodWarPb;
import com.game.pb.BroodWarPb.BuyBroodShopRq;
import com.game.pb.BroodWarPb.BuyBroodShopRs;
import com.game.pb.CommonPb;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.RandomUtil;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.worldmap.BroodWar;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.game.worldmap.Turret;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @date 2021/6/17 16:23 母巢之战
 */
@Service
public class BroodWarService {

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private BroodWarManager broodWarManager;
	@Autowired
	private BattleMgr battleMgr;
	@Autowired
	private WorldManager worldManager;
	@Autowired
	private StaticLimitMgr staticLimitMgr;
	@Autowired
	private StaticWorldMgr staticWorldMgr;
	@Autowired
	private StaticWorldCityTypeMgr staticWorldCityTypeMgr;
	@Autowired
	private CityManager cityManager;
	@Autowired
	private BroodWarService broodWarService;
	@Autowired
	private StaticBroodWarMgr staticBroodWarMgr;
	@Autowired
	private HeroManager heroManager;
	@Autowired
	private CondMgr condDataMgr;
	@Autowired
	private StaticLimitMgr limitMgr;
	@Autowired
	private ChatManager chatManager;


	/**
	 * 初始化
	 *
	 * @param rq
	 * @param handler
	 */
	public void broodWarInit(BroodWarPb.BroodWarInitRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		StaticWorldCity city = staticWorldMgr.getCity(CityId.WORLD_CITY_ID);
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		BroodWar broodWar = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
		if (broodWar == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}
		BroodWarInfo broodWarInfo = player.getBroodWarInfo();
		if (broodWarInfo == null) {
			broodWarInfo = new BroodWarInfo();
			broodWarInfo = broodWarManager.initBuff(broodWarInfo);
			player.setBroodWarInfo(broodWarInfo);
		}
		BroodWarPb.BroodWarInitRs.Builder builder = BroodWarPb.BroodWarInitRs.newBuilder();
		for (int buffId : broodWarInfo.getBroodWarBuff().values()) {
			StaticBroodWarBuff buff = broodWarManager.getBuff(buffId);
			int buyCount = broodWarInfo.getBroodWarBuffBuy().getOrDefault(buff.getType(), 0);
			builder.addBuffList(CommonPb.ThreeInt.newBuilder().setV1(buffId).setV2(buff.getLv()).setV3(buyCount).build());
		}
		builder.setFirstAttack(broodWarInfo.getFirstAttack());
		List<Integer> heros = Lists.newArrayList(broodWarInfo.getHeroInfo().keySet());
		Map<Integer, Integer> rankMap = broodWarManager.getRank(player, heros);
		broodWarInfo.getHeroInfo().forEach((e, f) -> {
			builder.addHeroInfo(CommonPb.HeroRankChange.newBuilder().setV1(e).setV2(rankMap.getOrDefault(f, 0)).setV3(f).setV4(broodWar.getId()).build());
		});
		builder.setRank(broodWar.getRank());
		builder.setScore(player.getPvpScore());
		builder.setLastWinCountry(broodWar.getLastCountry());
		builder.setDictator(broodWar.getDictator());
		Player target = playerManager.getPlayer(broodWar.getDictator());
		if (target != null) {
			builder.setDictatorHead(target.getPortrait());
			builder.setDictatorNick(target.getNick());
		}
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);
		if (worldActPlan != null) {
			builder.setNextTime(worldActPlan.getOpenTime());
		}
		builder.setAttackNow(broodWarInfo.getAttackNow());
		builder.setRevive(broodWarInfo.getRevive());
		builder.setCurHonour(broodWarInfo.getDiedSolider() * limitMgr.getNum(SimpleId.BROOD_WAR_DIE_SOLDIERS));
		builder.setTotalKill(broodWarInfo.getTotalKill());
		builder.setMultiKill(broodWarInfo.getMulitKill());
		broodWar.getOccupyTime().forEach((country, time) -> {
			builder.addOccupy(CommonPb.ThreeInt.newBuilder().setV1(country).setV2(broodWar.getOccupyPercentage().get(country)).setV3(time).build());
		});
		builder.setCurentCountry(broodWar.getDefenceCountry());
		builder.setMultiRank(broodWarManager.getScoreRanks(player));
		builder.addAllFightNow(broodWarInfo.getFighNow());
		int defendCountry = broodWar.getDefenceCountry();
		if (broodWar.getState() != BroodWarState.BEGIN_WAR || defendCountry == 0) {
			builder.setOccupyPercentage(false);
		} else {
			Optional<Team> optional = broodWar.getAttackQueue().stream().filter(e -> e.isAlive() && e.getCountry() == defendCountry).findAny();
			if (optional.isPresent()) {
				builder.setOccupyPercentage(true);
			} else {
				builder.setOccupyPercentage(false);
			}
		}
		handler.sendMsgToPlayer(BroodWarPb.BroodWarInitRs.ext, builder.build());
	}

	/**
	 * 购买buff
	 *
	 * @param rq
	 * @param handler
	 */
	public void buyBuff(BroodWarPb.BuyBuffRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		StaticWorldCity city = staticWorldMgr.getCity(CityId.WORLD_CITY_ID);
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		BroodWar broodWar = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
		// 判断增益是否可以购买
		if (!checkCanBuy(broodWar.getState())) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_BUY_NOW);
			return;
		}
		StaticBroodWarBuff staticBroodWarBuff = broodWarManager.getBuff(rq.getBuffId());
		if (staticBroodWarBuff == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		// 数据未初始化
		BroodWarInfo broodWarInfo = player.getBroodWarInfo();
		if (broodWarInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}
		broodWarInfo = broodWarManager.initBuff(broodWarInfo);
		// 未激活
		if (!broodWarInfo.getBroodWarBuff().containsKey(staticBroodWarBuff.getType())) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		boolean isUp = false;
		switch (rq.getState()) {
			case 1:
				// 钻石
				playerManager.subGold(player, staticBroodWarBuff.getGold_cost(), Reason.BROOD_WAR);
				isUp = true;
				break;
			default:
				// 记录下购买次数
				int buyCount = broodWarInfo.getBroodWarBuffBuy().getOrDefault(staticBroodWarBuff.getType(), 0);
				if (buyCount >= limitMgr.getNum(SimpleId.BROOD_WAR_BUF_BUFF)) {
					handler.sendErrorMsgToPlayer(GameError.CAN_NOT_BUY_ENOUGH);
					return;
				}
				// 检查资源条件
				List<List<Long>> resourceCond = staticBroodWarBuff.getCost_res();
				for (List<Long> item : resourceCond) {
					GameError gameError = condDataMgr.onCondition(player, ConditionType.RESOURCE, item);
					if (gameError != GameError.OK) {
						handler.sendErrorMsgToPlayer(gameError);
						return;
					}
				}
				isUp = RandomUtil.getRandomNumber(1000) < staticBroodWarBuff.getRate_res();
				// 金币
				staticBroodWarBuff.getCost_res().forEach(e -> {
					playerManager.subAward(player, e.get(0).intValue(), e.get(1).intValue(), e.get(2), Reason.BROOD_WAR);
				});
				broodWarInfo.getBroodWarBuffBuy().put(staticBroodWarBuff.getType(), buyCount + 1);
				break;
		}
		BroodWarPb.BuyBuffRs.Builder builder = BroodWarPb.BuyBuffRs.newBuilder();
		builder.setState(isUp ? 0 : 1);
		int buffLv = staticBroodWarBuff.getLv();
		if (isUp) {
			StaticBroodWarBuff nextBuff = broodWarManager.getNextBuff(staticBroodWarBuff.getNext_buffId(), staticBroodWarBuff.getType());
			if (nextBuff != null) {
				buffLv = nextBuff.getLv();
				// 更新当前类型触发的buff
				broodWarInfo.getBroodWarBuff().put(nextBuff.getType(), nextBuff.getId());
				// 这个BUFF满级了
				if (nextBuff.getNext_buffId() == 0) {
					// buff升满了 判断下是否技能都满级 激活行军
					boolean isAllMax = true;
					for (Map.Entry<Integer, Integer> entry : broodWarInfo.getBroodWarBuff().entrySet()) {
						StaticBroodWarBuff buffConfig = broodWarManager.getBuff(entry.getValue());
						if (buffConfig == null) {
							continue;
						}
						// 行军 或者是满级buff
						if (buffConfig.getType() == PropertyType.SPEED || buffConfig.getNext_buffId() == 0) {
							continue;
						}
						isAllMax = false;
						break;
					}
					if (isAllMax) {
						// 初始是0级
						StaticBroodWarBuff buffConfig = broodWarManager.getBuff(broodWarInfo.getBroodWarBuff().get(PropertyType.SPEED));
						// 获取下一级行军增益
						broodWarInfo.getBroodWarBuff().put(buffConfig.getType(), buffConfig.getNext_buffId());
						builder.addBuff(CommonPb.TwoInt.newBuilder().setV1(buffConfig.getNext_buffId()).setV2(1).build());
//                        heroManager.caculateAllProperty();
					}
				}
				builder.addBuff(CommonPb.TwoInt.newBuilder().setV1(nextBuff.getId()).setV2(nextBuff.getLv()).build());
			}

			heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
		}
		builder.setGold(player.getGold());
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(BroodWarPb.BuyBuffRs.ext, builder.build());
		SpringUtil.getBean(com.game.log.LogUser.class).broodWarBuyBuffLog(new NewBroodWarBuyBuffLog(player.roleId, broodWarInfo.getBroodWarBuffBuy().getOrDefault(staticBroodWarBuff.getType(), 0), rq.getState(), buffLv, player.getVip(), broodWar.getRank(), staticBroodWarBuff.getType(), player.getGold()));
	}

	/**
	 * @Description 是否可以购买增益
	 * @Param [broodWarState]
	 * @Return boolean
	 * @Date 2021/10/8 17:52
	 **/
	public boolean checkCanBuy(BroodWarState broodWarState) {
		return (broodWarState == BroodWarState.OPEN_BUY || broodWarState == BroodWarState.BEGIN_WAR);
	}

	/**
	 * 出击
	 *
	 * @param handler
	 */
	public void attackBrood(BroodWarPb.AttackBroodRq req, ClientHandler handler) {
		// 需要45级才能宣战
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
//		int heroId = req.getHeroId();
		int cityId = req.getCityId();
		// 找到city的配置
		StaticWorldCity config = staticWorldMgr.getCity(cityId);
		if (config == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		StaticWorldCityType staticWorldCityType = staticWorldCityTypeMgr.getStaticWorldCityType(config.getType());
		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData.getTasks().get(staticWorldCityType.getNeedTarget()) == null) {
			handler.sendErrorMsgToPlayer(GameError.WORLD_TARGET_NOT_OPEN);
			return;
		}

		City city = cityManager.getCity(cityId);
		if (city == null) {
			handler.sendErrorMsgToPlayer(GameError.SAME_COUNTRY);
			return;
		}

		// 需要的等级
		int needLevel = staticLimitMgr.getNum(35);
		if (player.getLevel() < needLevel) {
			handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
			return;
		}

		int playerMapId = worldManager.getMapId(player);
		int cityMapId = config.getMapId();
		// 只能在跟城池相同的地图才能进军
		if (playerMapId != cityMapId) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_CALL_WAR);
			return;
		}

		// 该请求只能对母巢和炮塔行军
		if (config.getType() != CityType.WORLD_FORTRESS && config.getType() != CityType.BROOD_WAR_TURRET) {
			handler.sendErrorMsgToPlayer(GameError.CAN_NOT_CALL_WAR);
			return;
		}

		MapInfo mapInfo = worldManager.getMapInfo(playerMapId);
		if (mapInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		Pos entityPos = new Pos(config.getX(), config.getY());
		Entity entity = mapInfo.getEntity(entityPos);

		// 出兵消耗
		int oilCost = worldManager.getMarchOil(Lists.newArrayList(req.getHeroId()), player, entityPos);
		if (player.getResource(ResourceType.OIL) < oilCost) {
			handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
			return;
		}

		if (entity instanceof BroodWar) {
			BroodWar broodWar = (BroodWar) entity;
			// 检查city是否处于保护时间
			// 非战斗阶段不能行军
			if (broodWar.getState() != BroodWarState.BEGIN_WAR) {
				handler.sendErrorMsgToPlayer(GameError.CITY_IS_PROTECTED);
				return;
			}
		}
		if (entity instanceof Turret) {
			Turret broodWar = (Turret) entity;
			// 检查city是否处于保护时间
			// 非战斗阶段不能行军
			if (broodWar.getState() != BroodWarState.BEGIN_WAR) {
				handler.sendErrorMsgToPlayer(GameError.CITY_IS_PROTECTED);
				return;
			}
		}
		// 创建一个行军
		// 行军英雄
		int heroId = req.getHeroId();
		if (!player.getEmbattleList().contains(heroId)) {
			handler.sendErrorMsgToPlayer(GameError.NO_MARCH_HEROS);
			return;
		}
		if (player.isHeroInMarch(heroId)) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
			return;
		}

		Hero hero = player.getHero(heroId);
		if (hero == null) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
			return;
		}
		BroodWarInfo broodWarInfo = player.getBroodWarInfo();
//        //英雄已经出征
//        if (broodWarInfo.getHeroInfo().values().contains(heroId)) {
//            handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
//            return;
//        }
		// 复活时间
		long revieTime = broodWarInfo.getHeroInfo().getOrDefault(heroId, 0L);
		if (revieTime != 0 && System.currentTimeMillis() < revieTime) {
			handler.sendErrorMsgToPlayer(GameError.HERO_IN_REVIVE);
			return;
		}

		switch (req.getState()) {
			default:
				// 普通出击 不做处理
				break;
			case 1:
				int costGold = staticBroodWarMgr.getCost(broodWarInfo.getFirstAttack(), 0);
				if (costGold > player.getGold()) {
					handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
					return;
				}
				playerManager.subGold(player, costGold, Reason.BROOD_WAR);
				// 优先出击
				broodWarInfo.setFirstAttack(broodWarInfo.getFirstAttack() + 1);
				break;
		}
		playerManager.subAward(player, AwardType.RESOURCE, ResourceType.OIL, oilCost, Reason.BROOD_WAR);
		/** 部队行军资源消耗的日志埋点 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(ResourceType.OIL), RoleResourceLog.OPERATE_OUT, ResourceType.OIL, ResOperateType.MARCH_OUT.getInfoType(), oilCost, player.account.getChannel()));
		logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 1, oilCost, OilOperateType.MARCH_OUT.getInfoType()), ResourceType.OIL);
		broodWarInfo.getHeroInfo().put(heroId, System.currentTimeMillis());
		// 生成行军
		March march = worldManager.createBroodWarMarch(player, heroId, new Pos(config.getX(), config.getY()));
		march.setFightTime(march.getEndTime() + 1000L, MarchReason.CountryAttender);
		march.setAttackerId(player.roleId);
		march.setDefencerId(req.getCityId());
		march.setSide(1);
		march.setType(req.getState());
		march.setMarchType(MarchType.BROOD_WAR); // 需要放到战役里面去
		// 添加行军到玩家身上
		player.addMarch(march);
		// 加到世界地图www中
		worldManager.addMarch(cityMapId, march);
		// 返回消息
		BroodWarPb.AttackBroodRs.Builder builder = BroodWarPb.AttackBroodRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(BroodWarPb.AttackBroodRs.ext, builder.build());
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	@Test
	public void attackBrood(BroodWarPb.AttackBroodRq req, long roleId) {
		// 需要45级才能宣战
		Player player = playerManager.getPlayer(roleId);
		if (player == null) {
			LogHelper.CONFIG_LOGGER.info("player is null");
			return;
		}

		int cityId = req.getCityId();
		// 找到city的配置
		StaticWorldCity config = staticWorldMgr.getCity(cityId);
		if (config == null) {
			LogHelper.CONFIG_LOGGER.info("config is null");
			return;
		}

		City city = cityManager.getCity(cityId);
		if (city == null) {
			LogHelper.CONFIG_LOGGER.info("SAME_COUNTRY is null");
			return;
		}

		// 需要的等级
		int needLevel = staticLimitMgr.getNum(35);
		if (player.getLevel() < needLevel) {
			LogHelper.CONFIG_LOGGER.info("LORD_LV_NOT_ENOUGH is null");
			return;
		}

		int playerMapId = worldManager.getMapId(player);
		int cityMapId = config.getMapId();
		// 只能在跟城池相同的地图才能进军
		if (playerMapId != cityMapId) {
			LogHelper.CONFIG_LOGGER.info("CAN_NOT_CALL_WAR is null");
			return;
		}

		// 该请求只能对母巢和炮塔行军
		if (config.getType() != CityType.WORLD_FORTRESS && config.getType() != CityType.BROOD_WAR_TURRET) {
			LogHelper.CONFIG_LOGGER.info("CAN_NOT_CALL_WAR is null");
			return;
		}

		MapInfo mapInfo = worldManager.getMapInfo(playerMapId);
		if (mapInfo == null) {
			LogHelper.CONFIG_LOGGER.info("SERVER_EXCEPTION is null");
			return;
		}
		Entity entity = mapInfo.getEntity(new Pos(config.getX(), config.getY()));
		if (entity instanceof BroodWar) {
			BroodWar broodWar = (BroodWar) entity;
			// 检查city是否处于保护时间
			// 非战斗阶段不能行军
			if (broodWar.getState() != BroodWarState.BEGIN_WAR) {
				LogHelper.CONFIG_LOGGER.info("CITY_IS_PROTECTED is null");
				return;
			}
		}
		// 创建一个行军
		// 行军英雄
		int heroId = req.getHeroId();
		if (!player.getEmbattleList().contains(heroId)) {
			LogHelper.CONFIG_LOGGER.info("NO_MARCH_HEROS is null");
			return;
		}

		if (player.isHeroInMarch(heroId)) {
			LogHelper.CONFIG_LOGGER.info("HERO_IN_MARCH is null");
			return;
		}

		Hero hero = player.getHero(heroId);
		if (hero == null) {
			return;
		}
		BroodWarInfo broodWarInfo = player.getBroodWarInfo();
		// 生成行军
		March march = worldManager.createBroodWarMarch(player, heroId, new Pos(config.getX(), config.getY()));
		march.setFightTime(march.getEndTime() + 1000L, MarchReason.CountryAttender);
		march.setAttackerId(player.roleId);
		march.setDefencerId(req.getCityId());
		march.setSide(1);
		march.setType(req.getState());
		march.setMarchType(MarchType.BROOD_WAR); // 需要放到战役里面去
		// 添加行军到玩家身上
		march.setType(RandomUtil.getRandomNumber(2));
		player.addMarch(march);
		// 加到世界地图www中
		worldManager.addMarch(cityMapId, march);
		worldManager.synMarch(mapInfo.getMapId(), march);
		// 玩家保护时间去掉
		playerManager.handleClearProtected(player);
	}

	/**
	 * 立即出战
	 *
	 * @param rq
	 * @param handler
	 */
	public void fightNow(BroodWarPb.FightNowRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int heroId = rq.getHeroId();
		BroodWarInfo info = player.getBroodWarInfo();
		// 英雄未出击
//            if (!info.getHeroInfo().containsKey(heroId)) {
//                handler.sendErrorMsgToPlayer(GameError.PROTOCAL_ERROR);
//                return;
//            }

		March march = player.getHeroMarch(heroId);
		if (march == null) {
			LogHelper.CONFIG_LOGGER.info("FightNowMark 未找到行军 playerId:{} heroId:{}", handler.getRoleId(), heroId);
			handler.sendErrorMsgToPlayer(GameError.PROTOCAL_ERROR);
			return;
		}
		int marchId = march.getKeyId();
		Pos pos = march.getEndPos();

		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		Entity entity = mapInfo.getEntity(pos);

		if (entity == null) {
			LogHelper.CONFIG_LOGGER.info("FightNowMark 行军目标不存在 playerId:{} heroId:{} marchId:{}", handler.getRoleId(), heroId, marchId);
			handler.sendErrorMsgToPlayer(GameError.WAR_NOT_EXISTS);
			return;
		}
		if (!(entity instanceof BroodWar)) {
			LogHelper.CONFIG_LOGGER.info("FightNowMark 目标队行非母巢 playerId:{} heroId:{} marchId:{}", handler.getRoleId(), heroId, marchId);
			handler.sendErrorMsgToPlayer(GameError.WAR_NOT_EXISTS);
			return;
		}

		BroodWar broodWar = (BroodWar) entity;

		if (broodWar.getState() != BroodWarState.BEGIN_WAR) {
			LogHelper.CONFIG_LOGGER.info("FightNowMark 非战斗状态 playerId:{} heroId:{} marchId:{}", handler.getRoleId(), heroId, marchId);
			handler.sendErrorMsgToPlayer(GameError.WAR_END_OR_NOT_EXIST);
			return;
		}

		int costGold = staticBroodWarMgr.getCost(info.getAttackNow(), 1);
		if (costGold > player.getGold()) {
			LogHelper.CONFIG_LOGGER.info("FightNowMark 砖石不足 playerId:{} heroId:{} marchId:{}", handler.getRoleId(), heroId, marchId);
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// 找到对应的team
		Optional<Team> optional = broodWar.getAttackQueue().stream().filter(e -> e.getMarchId() == marchId).findFirst();
//			Optional<Team> optional = broodWar.getAttackQueue().stream().filter(e -> e.getLordId() == player.roleId && e.getCurSoldier() > 0 && e.getAllEnities().get(0).getEntityId() == heroId && e.getRank() != BroodWarRank.ATTACK_NOW).findFirst();

		if (!optional.isPresent()) {
			LogHelper.CONFIG_LOGGER.info("FightNowMark 未找到队伍 playerId:{} heroId:{} marchId:{} ", handler.getRoleId(), heroId, marchId);
			handler.sendErrorMsgToPlayer(GameError.WAR_NOT_EXISTS);
			return;
		}

		Team team = optional.get();
		if (team.getRank() == BroodWarRank.ATTACK_NOW) {
			LogHelper.CONFIG_LOGGER.info("FightNowMark 已是立即出击 playerId:{} heroId:{} marchId:{} ", handler.getRoleId(), heroId, marchId);
			handler.sendErrorMsgToPlayer(GameError.WAR_NOT_EXISTS);
			return;
		}

		if (team.getCurSoldier() <= 0) {
			LogHelper.CONFIG_LOGGER.info("FightNowMark 已战败 playerId:{} heroId:{} marchId:{} ", handler.getRoleId(), heroId, marchId);
			handler.sendErrorMsgToPlayer(GameError.WAR_NOT_EXISTS);
			return;
		}

		// 设至为立即进攻
		team.setRank(BroodWarRank.ATTACK_NOW);
		team.setParam(System.currentTimeMillis());

		LogHelper.CONFIG_LOGGER.info("【母巢之战】 FightNowMark 立即进攻 playerId:{} heroId:{} marchId:{} cityId:{}", player.getRoleId(), heroId, team.getMarchId(), broodWar.getId());

		playerManager.subGold(player, costGold, Reason.BROOD_WAR);
		info.setAttackNow(info.getAttackNow() + 1);
		info.getFighNow().add(heroId);

		BroodWarPb.FightNowRs.Builder builder = BroodWarPb.FightNowRs.newBuilder();
		builder.setGold(player.getGold());
		builder.addAllFightNow(info.getFighNow());
		handler.sendMsgToPlayer(BroodWarPb.FightNowRs.ext, builder.build());
	}

	/**
	 * 战报查询
	 *
	 * @param rq
	 * @param handler
	 */
	public void broodWarRecord(BroodWarPb.BroodWarRecordRq rq, ClientHandler handler) {
		// 需要45级才能宣战
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int cityId = rq.getCityId();
		StaticWorldCity city = staticWorldMgr.getCity(cityId);
		if (city == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		BroodWar broodWar = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));

		BroodWarPb.BroodWarRecordRs.Builder builder = BroodWarPb.BroodWarRecordRs.newBuilder();
		long self = broodWar.getReports().stream().filter(e -> e.getAttacker() == player.roleId || e.getDefencer() == player.roleId).count();
		List<BroodWarReport> list = broodWar.getReports().stream().filter(e -> {
			if (rq.getState() == 1) {
				return true;
			}
			if (e.getDefencer() == player.roleId || e.getAttacker() == player.roleId) {
				return true;
			}
			return false;
		}).sorted(Comparator.comparing(BroodWarReport::getKeyId).reversed()).collect(Collectors.toList());
		int pageIndex = rq.getPageIndex() - 1;
		int size = rq.getSize();
		int start = pageIndex * size;
		int end = Math.min(start + size, list.size());
		if (list.size() < start) {
			return;
		}
		if (end < start) {
			return;
		}
		list = list.subList(start, end);
		for (int i = 0; i < list.size(); i++) {
			builder.addReport(list.get(i).getReport().wrapPb().build());
		}
		builder.setTotalSize(broodWar.getReports().size());
		builder.setSelfTotal(Long.valueOf(self).intValue());
		handler.sendMsgToPlayer(BroodWarPb.BroodWarRecordRs.ext, builder.build());
	}

	/**
	 * 战报回放
	 *
	 * @param rq
	 * @param handler
	 */
	public void broodWarPlayBack(BroodWarPb.BroodWarPlayBackRq rq, ClientHandler handler) {
		// 需要45级才能宣战
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		StaticWorldCity city = staticWorldMgr.getCity(rq.getCityId());
		if (city == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		BroodWar broodWar = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
		long id = rq.getId();
		Optional<BroodWarReport> optional = broodWar.getReports().parallelStream().filter(e -> e.getReport().getKeyId() == id).findFirst();
		if (!optional.isPresent()) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		BroodWarReport report = optional.get();
		if (report == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		BroodWarPb.BroodWarPlayBackRs.Builder builder = BroodWarPb.BroodWarPlayBackRs.newBuilder();
//		broodWar.getReports().forEach(e -> {
//			if (e.getAttacker() == player.roleId || e.getDefencer() == player.roleId) {
//				builder.addReport(e.getReportMsg().wrapPb().build());
//			}
//		});
		builder.addReport(report.getReportMsg().wrapPb().build());

		handler.sendMsgToPlayer(BroodWarPb.BroodWarPlayBackRs.ext, builder.build());
	}

	/**
	 * 任命信息
	 *
	 * @param rq
	 * @param handler
	 */
	public void appointInfo(BroodWarPb.AppointInfoRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		WorldData worldData = worldManager.getWolrdInfo();
		BroodWarPb.AppointInfoRs.Builder builder = BroodWarPb.AppointInfoRs.newBuilder();
		worldData.getAppoints().forEach((e, f) -> {
			BroodWarPb.Appoint.Builder apooint = BroodWarPb.Appoint.newBuilder();
			apooint.setCommand(e);
			if (f != null) {
				Player p = playerManager.getPlayer(f.getLordId());
				if (p != null) {
					apooint.setLordId(f.getLordId());
					apooint.setNick(p.getNick());
					apooint.setLevel(p.getLevel());
					apooint.setPortrait(p.getPortrait());
					apooint.setCountry(p.getCountry());
					apooint.setTitle(p.getTitle());
					apooint.setHeadIndex(p.getLord().getHeadIndex());
				}
			}
			builder.addAppoint(apooint);
		});
		handler.sendMsgToPlayer(BroodWarPb.AppointInfoRs.ext, builder.build());
	}

	/**
	 * 战力排行榜
	 *
	 * @param rq
	 * @param handler
	 */
	public void broodWarScoreRank(BroodWarPb.BroodWarScoreRankRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		WorldData worldData = worldManager.getWolrdInfo();
//        int country = rq.getCountry();
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		StaticWorldCity city = staticWorldMgr.getCity(CityId.WORLD_CITY_ID);
		BroodWar entity = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
		BroodWarPb.BroodWarScoreRankRs.Builder builder = BroodWarPb.BroodWarScoreRankRs.newBuilder();
		for (int key : SpringUtil.getBean(CountryManager.class).getCountrys().keySet()) {
			final int countryId = key;
			List<Player> list = playerManager.getAllPlayer().values().parallelStream().filter(e -> e.getCountry() == countryId).sorted(Comparator.comparingInt(Player::getMaxScore).reversed()).collect(Collectors.toList());
			// 最多20人
			int size = Math.min(20, list.size());
			for (int i = 0; i < size; i++) {
				Player p = list.get(i);
				BroodWarPb.Appoint.Builder apooint = BroodWarPb.Appoint.newBuilder();
				if (p.getBroodWarPosition() != null) {
					apooint.setCommand(p.getBroodWarPosition().getPosition());
				}
				apooint.setLordId(p.roleId);
				apooint.setNick(p.getNick());
				apooint.setLevel(p.getLevel());
				apooint.setPortrait(p.getPortrait());
				apooint.setCountry(p.getCountry());
				apooint.setTitle(p.getTitle());
				apooint.setHeadIndex(p.getLord().getHeadIndex());
				apooint.setScore(p.getMaxScore());
				builder.addAppoint(apooint);
			}
		}
		handler.sendMsgToPlayer(BroodWarPb.BroodWarScoreRankRs.ext, builder.build());
	}

	/**
	 * 任命
	 *
	 * @param rq
	 * @param handler
	 */
	public void broodWarAppoint(BroodWarPb.BroodWarAppointRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		WorldData worldData = worldManager.getWolrdInfo();
		BroodWarPosition dictator = worldData.getAppoints().get(1);
		if (dictator == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (dictator.getLordId() != player.roleId) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOVERN);
			return;
		}

		int position = rq.getCommand();
		long targetId = rq.getLordId();

		Player target = playerManager.getPlayer(targetId);
		if (target == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (target.getBroodWarPosition() != null && target.getBroodWarPosition().getPosition() == position) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		StaticBroodWarCommand commandConfig = staticBroodWarMgr.getCommandMap().get(position);
		if (commandConfig == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		BroodWarPosition oldPosition = worldData.getAppoints().get(position);
		if (oldPosition != null) {
			Player oldTarget = playerManager.getPlayer(oldPosition.getLordId());
			if (oldTarget != null) {
				oldTarget.setBroodWarPosition(null);
			}
			oldPosition.setLordId(targetId);
		}
		target.setBroodWarPosition(oldPosition);
		BroodWarPb.BroodWarAppointRs.Builder builder = BroodWarPb.BroodWarAppointRs.newBuilder();
		handler.sendMsgToPlayer(BroodWarPb.BroodWarAppointRs.ext, builder.build());
		BroodWarPb.SynBroodWarAppointRq.Builder push = BroodWarPb.SynBroodWarAppointRq.newBuilder();
		BroodWarPb.Appoint.Builder apooint = BroodWarPb.Appoint.newBuilder();
		apooint.setCommand(position);
		apooint.setLordId(target.roleId);
		apooint.setNick(target.getNick());
		apooint.setLevel(target.getLevel());
		apooint.setPortrait(target.getPortrait());
		apooint.setCountry(target.getCountry());
		apooint.setTitle(target.getTitle());
		apooint.setHeadIndex(target.getLord().getHeadIndex());
		push.setAppoint(apooint);
		BroodWarPb.SynBroodWarAppointRq msg = push.build();
		playerManager.getOnlinePlayer().forEach(e -> {
			SynHelper.synMsgToPlayer(e, BroodWarPb.SynBroodWarAppointRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarAppointRq.ext, msg);
		});
		chatManager.sendWorldChat(ChatId.CHAT_166, player.getNick(), target.getNick(), commandConfig.getName());
	}

	/**
	 * 名人堂
	 *
	 * @param rq
	 * @param handler
	 */
	public void broodWarHOF(BroodWarPb.BroodWarHOFRq rq, ClientHandler handler) {
		WorldData worldData = worldManager.getWolrdInfo();
		BroodWarPb.BroodWarHOFRs.Builder builder = BroodWarPb.BroodWarHOFRs.newBuilder();
		worldData.getHofs().forEach(e -> {
			Player target = playerManager.getPlayer(e.getLordId());
			BroodWarPb.HOF.Builder hof = BroodWarPb.HOF.newBuilder();
			hof.setRank(e.getRank());
			hof.setDate(e.getTime());
			hof.setLordId(e.getLordId());
			hof.setNick(target.getNick());
			hof.setLevel(target.getLevel());
			hof.setPortrait(target.getPortrait());
			hof.setCountry(target.getCountry());
			hof.setTitle(target.getTitle());
			hof.setHeadIndex(target.getLord().getHeadIndex());
			builder.addHof(hof);
		});
		handler.sendMsgToPlayer(BroodWarPb.BroodWarHOFRs.ext, builder.build());
	}

	/**
	 * 英雄恢复
	 *
	 * @param rq
	 * @param handler
	 */
	public void broodWarRelieve(BroodWarPb.BroodWarRelieveRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		BroodWarInfo broodWarInfo = player.getBroodWarInfo();
		int heroId = rq.getHeroId();
		// 英雄已恢复
		if (!broodWarInfo.getHeroInfo().containsKey(heroId)) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int cost = staticBroodWarMgr.getCost(broodWarInfo.getRevive(), 2);
		if (cost > player.getGold()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		playerManager.subGold(player, cost, Reason.BROOD_WAR);
		broodWarInfo.setRevive(broodWarInfo.getRevive() + 1);
		broodWarInfo.getHeroInfo().put(heroId, System.currentTimeMillis());
		BroodWarPb.BroodWarRelieveRs.Builder builder = BroodWarPb.BroodWarRelieveRs.newBuilder();
		builder.setCostGold(cost);
		handler.sendMsgToPlayer(BroodWarPb.BroodWarRelieveRs.ext, builder.build());
	}

	/**
	 * 历史战况信息
	 *
	 * @param rq
	 * @param handler
	 */
	public void broodWarIntegralRank(BroodWarPb.BroodWarIntegralRankRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		StaticWorldCity city = staticWorldMgr.getCity(CityId.WORLD_CITY_ID);
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		BroodWar broodWar = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
		if (broodWar == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}
		// 默认0
		int index = rq.getPageIndex() - 1;
		int pageSize = rq.getSize();
		int startIndex = index * pageSize;
		// copy一份,避免报错
		List<RankPvpHero> heroes = broodWarManager.getRanks().stream().collect(Collectors.toList());
		int total = heroes.size();
		if (startIndex > total) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		int maxSize = Math.min(startIndex + pageSize, total);
		RankPvpHero self = null;
		int selfRank = 0;
		boolean inRank = heroes.parallelStream().anyMatch(e -> e.getLordId() == player.roleId);
		if (inRank) {
			for (RankPvpHero hero : heroes) {
				selfRank++;
				if (hero.getLordId() == player.roleId) {
					self = hero;
					break;
				}
			}
		}
		BroodWarPb.BroodWarIntegralRankRs.Builder builder = BroodWarPb.BroodWarIntegralRankRs.newBuilder();
		int count = 0;
		StaticBroodWarKillScore staticBroodWarKillScore = staticBroodWarMgr.getMulitScores().stream().sorted(Comparator.comparing(StaticBroodWarKillScore::getRank).reversed()).collect(Collectors.toList()).get(0);
		int limit = staticBroodWarKillScore == null ? 15 : staticBroodWarKillScore.getRank();
		for (int i = startIndex; i < maxSize; i++) {
			count++;
			RankPvpHero info = heroes.get(i);
			Player p = playerManager.getPlayer(info.getLordId());
			BroodWarInfo broodWarInfo = p.getBroodWarInfo();

			BroodWarPb.Integral.Builder integral = BroodWarPb.Integral.newBuilder();
			integral.setRank(i + 1);
			integral.setLordId(info.getLordId());
			integral.setNick(p.getNick());
			integral.setLevel(p.getLevel());
			integral.setPortrait(p.getPortrait());
			integral.setCountry(p.getCountry());
			integral.setTitle(p.getTitle());
			integral.setHeadIndex(p.getLord().getHeadIndex());
			integral.setKill(broodWarInfo.getMulitKill());
			integral.setScore(broodWarManager.getRewardScore(i + 1, broodWarInfo.getTotalKill(), 1));
			builder.addIntegral(integral);
			if (count == limit) {
				break;
			}
		}
		builder.setSelfRank(selfRank);
		builder.setSelfSocre(broodWarManager.getRewardScore(broodWarManager.getScoreRanks(player), player.getBroodWarInfo().getTotalKill(), 1));
		builder.setTotalSize(count);
		LogHelper.GAME_DEBUG.debug("历史战况排名->[{}]", builder.build());
		handler.sendMsgToPlayer(BroodWarPb.BroodWarIntegralRankRs.ext, builder.build());
	}

	public void broodWarSoldier(BroodWarPb.BroodWarSoldierRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		StaticWorldCity city = staticWorldMgr.getCity(rq.getCityId());
		if (city == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		BroodWar broodWar = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
		if (broodWar == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		int defendCountry = broodWar.getDefenceCountry();

		BroodWarPb.BroodWarSoldierRs.Builder builder = BroodWarPb.BroodWarSoldierRs.newBuilder();
		broodWar.getAttackQueue().stream().filter(e -> e.getCountry() != defendCountry && e.getCountry() != 0).collect(Collectors.groupingBy(e -> e.getCountry())).forEach((country, l) -> {
			builder.addAttackCountry(country);
		});

		long maxAttackSoider = broodWar.getAttackQueue().stream().filter(e -> e.getCountry() != defendCountry && e.getCountry() != 0).mapToInt(Team::getCurSoldier).sum();
		builder.setAttackSoldier(maxAttackSoider);
		// 防守的兵力要包含虫族
		long maxDefecneSolider = broodWar.getAttackQueue().stream().filter(e -> e.getCountry() == defendCountry || e.getCountry() == 0).mapToInt(Team::getCurSoldier).sum();
		builder.setDefenceSoldier(maxDefecneSolider);
		builder.setDefenceTime(broodWar.getOccupyTime().getOrDefault(broodWar.getDefenceCountry(), 0));
		builder.setDefenceCountry(defendCountry);
		handler.sendMsgToPlayer(BroodWarPb.BroodWarSoldierRs.ext, builder.build());
	}

	/**
	 * 对话母巢商品
	 *
	 * @param rq
	 * @param handler
	 */
	public void buyBroodShopRq(BuyBroodShopRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		StaticBroodWarShop staticBroodWarShop = staticBroodWarMgr.getShopMap().get(rq.getId());
		if (staticBroodWarShop == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_EXIST_SHOP);
			return;
		}

		SimpleData simpleData = player.getSimpleData();
		if (simpleData.getPvpScore() < staticBroodWarShop.getScore()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_PVP_SCORE);
			return;
		}

		// 扣除母巢积分
		simpleData.setPvpScore(simpleData.getPvpScore() - staticBroodWarShop.getScore());
		playerManager.addAward(player, staticBroodWarShop.getType(), staticBroodWarShop.getPropId(), staticBroodWarShop.getNum(), Reason.BROOD_WAR);

		BuyBroodShopRs.Builder builder = BuyBroodShopRs.newBuilder();
		builder.setScore(simpleData.getPvpScore());
		builder.addAwards(PbHelper.createAward(staticBroodWarShop.getType(), staticBroodWarShop.getPropId(), staticBroodWarShop.getNum()));

		handler.sendMsgToPlayer(BroodWarPb.BuyBroodShopRs.ext, builder.build());
	}
}
