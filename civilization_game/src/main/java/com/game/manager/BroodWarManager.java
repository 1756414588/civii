package com.game.manager;

import com.game.Loading;
import com.game.constant.*;
import com.game.dataMgr.StaticBroodWarMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMailDataMgr;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.define.LoadData;
import com.game.domain.CountryData;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.AttackInfo;
import com.game.domain.p.Attender;
import com.game.domain.Award;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.BroodWarData;
import com.game.domain.p.BroodWarDictater;
import com.game.domain.p.BroodWarHofData;
import com.game.domain.p.BroodWarInfo;
import com.game.domain.p.BroodWarPosition;
import com.game.domain.p.BroodWarReport;
import com.game.domain.p.BroodWarReportData;
import com.game.domain.p.City;
import com.game.domain.p.CtyGovern;
import com.game.domain.p.FightIn;
import com.game.domain.p.Hero;
import com.game.domain.p.LineEntity;
import com.game.domain.p.Property;
import com.game.domain.p.RankPvpHero;
import com.game.domain.p.Report;
import com.game.domain.p.ReportHead;
import com.game.domain.p.ReportMsg;
import com.game.domain.p.Team;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticBroodWarBuff;
import com.game.domain.s.StaticBroodWarCommand;
import com.game.domain.s.StaticBroodWarKillScore;
import com.game.domain.s.StaticMail;
import com.game.domain.s.StaticWorldActPlan;
import com.game.domain.s.StaticWorldCity;
import com.game.log.domain.NewBroodWarBattleLog;
import com.game.message.handler.DealType;
import com.game.pb.BroodWarPb;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.WorldPb;
import com.game.server.GameServer;
import com.game.server.ICommand;
import com.game.server.LogicServer;
import com.game.service.AchievementService;
import com.game.service.WorldActPlanService;
import com.game.service.WorldService;
import com.game.timer.TimerEvent;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.RandomUtil;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.BroodWar;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.Pos;
import com.game.worldmap.Turret;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @date 2021/6/17 14:51 母巢之战
 */
@Component
@LoadData(name = "母巢之战", type = Loading.LOAD_USER_DB, initSeq = 2000)
public class BroodWarManager extends BaseManager {

	@Autowired
	private StaticBroodWarMgr broodWarMgr;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private WorldManager worldManager;
	@Autowired
	private WorldService worldService;
	@Autowired
	private WarManager warManager;
	@Autowired
	private BattleMgr battleMgr;
	@Autowired
	private BattleMailManager battleMailManager;
	@Autowired
	private StaticWorldMgr staticWorldMgr;
	@Autowired
	private WorldActPlanService worldActPlanService;
	@Autowired
	private StaticWorldActPlanMgr worldActPlanMgr;
	@Autowired
	private CountryManager countryManager;
	@Autowired
	private StaticLimitMgr limitMgr;
	@Autowired
	private ChatManager chatManager;
	@Autowired
	private StaticMailDataMgr staticMailDataMgr;
	@Autowired
	private CityManager cityManager;
	@Autowired
	private HeroManager heroManager;
	@Autowired
	private MarchManager marchManager;
	@Autowired
	LogicServer logicServer;

	@Getter
	private Map<BroodWarState, AbstractBroodWarState> stateMap = new ConcurrentHashMap<>();

	@Getter
	private LinkedList<RankPvpHero> ranks = new LinkedList<>();

	/**
	 * 争霸是否开始
	 */
	@Getter
	private boolean isOpen = false;

	/**
	 * 母巢的实体
	 */
	private volatile BroodWar broodWar = new BroodWar();

	/**
	 * 每多少s进度增加1%
	 */
	private int process = 15;

	/**
	 * 切换阵营时减少的时间比
	 */
	private float limitProcess = 0.3F;

	/**
	 * 英雄恢复时间 默认2分钟
	 */
	private long REVIVE_TIME = TimeHelper.TWO_MINUTE_MS;

	/**
	 * 炮塔攻击时长
	 */
	private long TURN_FIGHT_TIME = TimeHelper.MINUTE_MS;

	/**
	 * 记录实体一段时间累战斗的次数
	 */
	private Map<Long, Integer> recordBroodBattleLog = new HashMap<>();

	/**
	 * 上次记录日志的时间
	 */
	private long lastRecordLogTime = 0l;

	public interface AbstractBroodWarState {

		/**
		 * 状态机
		 *
		 * @param broodWar 母巢之战不同状态机制下执行的操作
		 */
		void action(BroodWar broodWar);
	}


	@Override
	public void load() throws Exception {
		stateMap.put(BroodWarState.WAIT, this::checkIsOpen);
		stateMap.put(BroodWarState.OPEN_BUY, this::changeToWarBegin);
		stateMap.put(BroodWarState.BEGIN_WAR, this::checkBroodWar);
		stateMap.put(BroodWarState.END_WAR, this::endWar);
		process = limitMgr.getNum(SimpleId.BROOD_WAR_PROCESS);
		limitProcess = limitMgr.getNum(SimpleId.BROOD_WAR_PROCESS_LIMIT) / 1000F;
		REVIVE_TIME = limitMgr.getNum(SimpleId.BROOD_WAR_REVIVE_TIME) * TimeHelper.SECOND_MS;
		TURN_FIGHT_TIME = limitMgr.getNum(SimpleId.BROOD_WAR_TURN_FIGHT_TIME) * TimeHelper.SECOND_MS;
	}


	@Override
	public void init() throws Exception {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);
		if (worldActPlan != null) {
			LogHelper.GAME_LOGGER.info("【母巢之战】 init 活动状态:{} 预热时间:{} 开启时间:{} 结束时间:{}", worldActPlan.getState(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));
		}

		/**
		 * 初始化母巢数据
		 */
		loadHistory();
		openBroodWarCheck();
		lastRecordLogTime = System.currentTimeMillis();
	}


	/**
	 * 开启活动检测
	 */
	public void openBroodWarCheck() {
		List<StaticWorldCity> citys = staticWorldMgr.getCityMap().values().stream().filter(e -> e.getType() == CityType.WORLD_FORTRESS || e.getType() == CityType.BROOD_WAR_TURRET).collect(Collectors.toList());
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		for (StaticWorldCity city : citys) {
			BroodWar entity = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
			if (entity.getState() == null) {
				entity.setState(BroodWarState.WAIT);
			}
//			LogHelper.GAME_DEBUG.info("母巢之战!预热开启活动检测 城池类型[{}] 防守阵营->[{}] 当前状态[{}]", entity.getCityType(), entity.getDefenceCountry(), entity.getState());
			TimerEvent event = new TimerEvent(-1, TimeHelper.SECOND_MS) {
				@Override
				public void action() {
					checkBroodWarState(entity);
					if (entity.getCityType() == CityType.BROOD_WAR_TURRET && isOpen) {
						turretFight((Turret) entity);
					}
				}
			};
			entity.setEvent(event);
			 logicServer.addTimer(event);
		}
	}

	public void gmOpenBroodWarCheck() {
		List<StaticWorldCity> citys = staticWorldMgr.getCityMap().values().stream().filter(e -> e.getType() == CityType.WORLD_FORTRESS || e.getType() == CityType.BROOD_WAR_TURRET).collect(Collectors.toList());
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		for (StaticWorldCity city : citys) {
			BroodWar entity = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
			entity.setState(BroodWarState.OPEN_BUY);
//			LogHelper.GAME_DEBUG.info("母巢之战!预热开启活动检测 城池类型[{}] 防守阵营->[{}] 当前状态[{}]", entity.getCityType(), entity.getDefenceCountry(), entity.getState());
			TimerEvent event = new TimerEvent(-1, TimeHelper.SECOND_MS) {
				@Override
				public void action() {
					checkBroodWarState(entity);
					if (entity.getCityType() == CityType.BROOD_WAR_TURRET && isOpen) {
						turretFight((Turret) entity);
					}
				}
			};
			entity.setEvent(event);
			 logicServer.addTimer(event);
		}
	}

	/**
	 * 定时检测玩家
	 *
	 * @param broodWar
	 */
	public void checkBroodWarState(BroodWar broodWar) {
		BroodWarState state = broodWar.getState();
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);

		checkHeroMarchReturn(worldActPlan, broodWar);

//		LogHelper.GAME_LOGGER.info("母巢之战 活动状态:{} 预热时间:{} 开启时间:{} 结束时间:{}", worldActPlan.getState(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()));
		AbstractBroodWarState command = getStateMap().get(state);
		if (command == null) {
			return;
		}

		command.action(broodWar);
	}


	/**
	 * 加载历史数据
	 */
	private void loadHistory() {
		// 加载名人堂数据
		List<BroodWarHofData> hofs = broodWarMgr.loadBroodWarHof();
		WorldData worldData = worldManager.getWolrdInfo();
		hofs.stream().forEach(e -> {
			BroodWarDictater dictater = new BroodWarDictater();
			dictater.loadData(e);
			worldData.getHofs().add(dictater);
			ranks.addAll(worldData.getRankList());
		});
		// TODO 加载任命信息
		List<BroodWarPosition> positions = broodWarMgr.loadPostion();
		positions.forEach(e -> {
			worldData.getAppoints().put(e.getPosition(), e);
			Player p = playerManager.getPlayer(e.getLordId());
			if (p != null) {
				p.setBroodWarPosition(e);
			}
		});

		// 加载母巢数据
		Map<Integer, BroodWarData> broodWarDataMap = broodWarMgr.loadBroodWar();
		// 第一次进入 初始化母巢和炮塔
		if (broodWarDataMap.size() == 0) {
			initAllPveBoss();
		}
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		for (StaticWorldCity cityConfig : staticWorldMgr.getCityMap().values()) {
			if (cityConfig.getType() != CityType.BROOD_WAR_TURRET && cityConfig.getType() != CityType.WORLD_FORTRESS) {
				continue;
			}
			Pos pos = new Pos(cityConfig.getX(), cityConfig.getY());
			BroodWar entity = (BroodWar) mapInfo.getEntity(pos);
			if (entity == null) {
				continue;
			}
			BroodWarData data = broodWarDataMap.get(cityConfig.getCityId());
			if (data != null) {
				entity.reLoadData(data);
			}
			mapInfo.addBroodEntity(pos, entity);
			if (cityConfig.getType() == CityType.WORLD_FORTRESS) {
				this.broodWar = entity;
			}
		}
		// 加载战报
		List<BroodWarReportData> list = broodWarMgr.loadBroodWarReport();
		list.stream().forEach(e -> {
			try {
				DataPb.BroodWarReportData data = DataPb.BroodWarReportData.parseFrom(e.getReport());
				BroodWarReport r = new BroodWarReport();
				r.loadData(data);
				broodWar.getReports().add(r);
			} catch (InvalidProtocolBufferException ex) {
				LogHelper.ERROR_LOGGER.error(ex.getMessage(), ex);
			}
		});
	}


	public void endWar(BroodWar broodWar) {
//		LogHelper.GAME_LOGGER.info("母巢之战 endWar id:{}", broodWar.getId());
		if (broodWar.getCityType() == CityType.WORLD_FORTRESS) {
			isOpen = false;
		}
		broodWar.setState(BroodWarState.WAIT);
//		handleAllMarchReturn(broodWar);
		// 炮台不用做操作
		if (broodWar.getCityType() == CityType.BROOD_WAR_TURRET) {
			stopTimer(broodWar);
			return;
		}
		// 更新母巢的届数
		broodWar.setRank(broodWar.getRank() + 1);
		// 更新活动下次开启的时间
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);
		worldActPlanService.activityEnd(worldActPlan);
		pushWorldActPlanEnd(worldActPlan);
		// 计算占领进度
		int winCountry = getWinCountry(broodWar);
		// 重置独裁官
		broodWar.setDictator(0);
		if (winCountry == 0) {
			// 没有一个阵营参与
			broodWar.afterEnd();
			// 发送下一次的母巢之战的信息
			resetAllPlayerInfo(worldActPlan.getOpenTime());
			// 通知前端活动结束了
			BroodWarPb.SynBroodWarResultRq msg = BroodWarPb.SynBroodWarResultRq.newBuilder().build();
			playerManager.getOnlinePlayer().forEach(player -> {
				SynHelper.synMsgToPlayer(player, BroodWarPb.SynBroodWarResultRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarResultRq.ext, msg);
				heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
				playerManager.synChange(player, Reason.BROOD_WAR);
			});
			LogHelper.GAME_LOGGER.info("母巢之战结束 活动结束时间->[{}]", TimeHelper.getFormatData(new Date()));
			stopTimer(broodWar);
			return;
		}
		broodWar.setLastCountry(winCountry);
		CountryData countryData = countryManager.getCountry(winCountry);
		recordAppoints(broodWar, worldData);
		recordDictator(broodWar, countryData, worldData);
		// 更新地图母巢归属
		changeCityCountry(broodWar, winCountry);
		// 计算参战人员积分
		ranks.forEach(e -> {
			calculateScore(e, winCountry);
		});
		resetAllPlayerInfo(worldActPlan.getOpenTime());
		// 通知前端活动结束了
		BroodWarPb.SynBroodWarResultRq msg = BroodWarPb.SynBroodWarResultRq.newBuilder().build();
		playerManager.getOnlinePlayer().forEach(player -> {
			SynHelper.synMsgToPlayer(player, BroodWarPb.SynBroodWarResultRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarResultRq.ext, msg);
			heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
			playerManager.synChange(player, Reason.BROOD_WAR);
		});
		LogHelper.GAME_LOGGER.info("母巢之战结束 活动结束时间->[{}]", TimeHelper.getFormatData(new Date()));
		broodWar.afterEnd();
		// 发送下一次的母巢之战的信息
		stopTimer(broodWar);
	}

	/**
	 * 每一轮都检测玩家返回
	 *
	 * @param broodWar
	 */
	public void checkHeroMarchReturn(WorldActPlan worldActPlan, BroodWar broodWar) {
		LinkedList<Team> attackQueue = broodWar.getAttackQueue();

		// 队列为空
		if (attackQueue.isEmpty()) {
			return;
		}

		boolean armyRuturn = false;
		if (broodWar.getState() == BroodWarState.END_WAR) {
			armyRuturn = true;
		}

		if (worldActPlan != null && worldActPlan.getEndTime() + 6000 < TimeHelper.curentTime()) {
			armyRuturn = true;
		}

		// 部队空了,或者时间到了都需要返回
		emptyMarchReturn(attackQueue, armyRuturn);
	}

	public void emptyMarchReturn(LinkedList<Team> queue, boolean isRuten) {
		Iterator<Team> it = queue.iterator();
		while (it.hasNext()) {
			Team team = it.next();
			if (team.getCurSoldier() > 0 && !isRuten) {
				continue;
			}

			Player p = playerManager.getPlayer(team.getLordId());
			if (p == null) {
				continue;
			}

			March m = p.getMarch(team.getMarchId());
			if (m != null) {
				marchManager.doMarchReturn(m, p, Reason.BROOD_WAR);
				LogHelper.GAME_LOGGER.info("【母巢之战】 部队返回 playerId:{} heroId:{} marchId:{} status:{} startPos:{} endPos:{}", m.getLordId(), m.getHeroIds().get(0), m.getKeyId(), m.getState(), m.getStartPos(), m.getEndPos());
			} else {
				LogHelper.GAME_LOGGER.info("【母巢之战】 部队返回行军为空 playerId:{} heroId:{} marchId:{}", p.getRoleId(), team.getAllEnities().get(0).getEntityId(), team.getMarchId());
			}

			it.remove();
		}
	}

	/**
	 * 记录名人堂 设置独裁者
	 *
	 * @param broodWar
	 * @param countryData
	 * @param worldData
	 */
	private void recordDictator(BroodWar broodWar, CountryData countryData, WorldData worldData) {
		for (CtyGovern cityGovern : countryData.getGoverns().values()) {
			if (cityGovern.getGovernId() == CountryConst.GOVERN_KING) {
				broodWar.setDictator(cityGovern.getLordId());
				BroodWarPosition dicotr = worldData.getAppoints().get(1);
				dicotr.setLordId(cityGovern.getLordId());
				Player player = playerManager.getPlayer(cityGovern.getLordId());
				playerManager.addAward(player, AwardType.COMMAND_SKIN, CommandSkinId.skin3, 1, Reason.COMMAND_SKIN);
				player.setBroodWarPosition(dicotr);
				BroodWarDictater broodWarDictater = new BroodWarDictater();
				broodWarDictater.setLordId(cityGovern.getLordId());
				broodWarDictater.setRank(broodWar.getRank());
				broodWarDictater.setTime(System.currentTimeMillis());
				worldData.getHofs().offerFirst(broodWarDictater);
				broodWarMgr.insertBroodWarHof(new BroodWarHofData(broodWar.getRank(), player.roleId));
				break;
			}
		}
	}

	/**
	 * 重置指挥官信息
	 *
	 * @param broodWar
	 * @param worldData
	 */
	private void recordAppoints(BroodWar broodWar, WorldData worldData) {
		worldData.getAppoints().clear();
		broodWarMgr.getCommandMap().forEach((command, config) -> {
			BroodWarPosition position = new BroodWarPosition();
			position.setRank(broodWar.getRank());
			position.setPosition(command);
			worldData.getAppoints().put(command, position);
		});
	}

	/**
	 * 获取赢了的阵营
	 *
	 * @param broodWar
	 * @return
	 */
	private int getWinCountry(BroodWar broodWar) {
		int percentage = 0;
		int winCountry = 0;
		int tmp = 0;
		int tmpCountry = 0;
		// 先比较阵营的占领进度
		for (Map.Entry<Integer, Integer> entry : broodWar.getOccupyPercentage().entrySet()) {
			int country = entry.getKey();
			int value = entry.getValue();
			if (value >= percentage) {
				tmp = percentage;
				tmpCountry = country;
				percentage = value;
				winCountry = country;
			}
		}
		// 进度为0 则说明没有一个阵营参加了战斗
		if (percentage == 0) {
			return 0;
		}
		// 如果第一名和第二名的进度一样 则比较两者的时间
		if (percentage == tmp) {
			Integer win = broodWar.getOccupyTime().get(winCountry);
			Integer tmp1 = broodWar.getOccupyTime().get(tmpCountry);
			return win.intValue() > tmp1.intValue() ? winCountry : tmpCountry;

		}
		return winCountry;
	}

	/**
	 * 推送下活动结束信息
	 *
	 * @param worldActPlan
	 */
	private void pushWorldActPlanEnd(WorldActPlan worldActPlan) {
		StaticWorldActPlan staticWorldActPlan = worldActPlanMgr.get(worldActPlan.getId());
		WorldPb.SyncWorldActivityPlan.Builder builder = WorldPb.SyncWorldActivityPlan.newBuilder();
		WorldPb.WorldActivityPlan.Builder worldActivityPlan = WorldPb.WorldActivityPlan.newBuilder();
		CommonPb.WorldActPlan.Builder worldActPlanPb = CommonPb.WorldActPlan.newBuilder();
		worldActPlanPb.setId(worldActPlan.getId());
		worldActPlanPb.setPreheatTime(worldActPlan.getPreheatTime());
		worldActPlanPb.setOpenTime(worldActPlan.getOpenTime());
		worldActPlanPb.setState(worldActPlan.getState());
		worldActPlanPb.setEndTime(worldActPlan.getEndTime());
		worldActivityPlan.setWorldActPlan(worldActPlanPb);
		worldActivityPlan.addAllPrams(staticWorldActPlan != null ? staticWorldActPlan.getContinues() : new ArrayList<>());
		builder.addWorldActivityPlan(worldActivityPlan);
		WorldPb.SyncWorldActivityPlan msg = builder.clone().build();
		for (Player player : playerManager.getOnlinePlayer()) {
			SynHelper.synMsgToPlayer(player, WorldPb.SyncWorldActivityPlan.EXT_FIELD_NUMBER, WorldPb.SyncWorldActivityPlan.ext, msg);
		}
	}

	@Autowired
	AchievementService achievementService;

	private void calculateScore(RankPvpHero rankPvpHero, int winCountry) {
		Player player = playerManager.getPlayer(rankPvpHero.getLordId());
		BroodWarInfo info = player.getBroodWarInfo();
		int multiRank = getScoreRanks(player);
		int totalKill = info.getTotalKill();
		int rewardScore = getRewardScore(multiRank, totalKill, 2);
		if (rewardScore > 0) {
			addScore(player, rewardScore);
		}
		int num = limitMgr.getNum(SimpleId.BROOD_WAR_DIE_SOLDIERS);
		int honur = num * info.getDiedSolider();
		if (honur > 0) {
			List<Award> awards = new ArrayList<>();
			awards.add(new Award(AwardType.LORD_PROPERTY, LordPropertyType.HONOR, honur));
			playerManager.sendAttachMail(player, awards, MailId.BROOD_WAR_DIE_SOLDIERS, Integer.toString(winCountry), Integer.toString(rewardScore), Integer.toString(info.getDiedSolider()), Integer.toString(honur), Integer.toString(1));
		} else {
			playerManager.sendNormalMail(player, MailId.BROOD_WAR_DIE_SOLDIERS, Integer.toString(winCountry), Integer.toString(rewardScore), Integer.toString(info.getDiedSolider()), Integer.toString(honur));
		}
		StaticMail staticMail = null;
		if (player.getCountry() == winCountry) {
			staticMail = staticMailDataMgr.getStaticMail(MailId.BROOW_WAR_WIN);
		} else {
			staticMail = staticMailDataMgr.getStaticMail(MailId.BROOW_WAR_LOSE);
		}
		if (staticMail == null) {
			return;
		}
		if (staticMail.getAward() == null) {
			return;
		}
		List<Award> list = new ArrayList<>();
		staticMail.getAward().forEach(e -> {
			list.add(new Award(e.get(0), e.get(1), e.get(2)));
		});
		playerManager.sendAttachMail(player, list, staticMail.getMailId());
		achievementService.addAndUpdate(player, AchiType.AT_21, 1);
		if (player.getCountry() == winCountry) {
			achievementService.addAndUpdate(player, AchiType.AT_23, 1);
		}
	}

	/**
	 * 获取奖励积分 排名0不记录 state{0:累杀 1:连杀 3:总积分}
	 *
	 * @param multiRank
	 * @param totalKill
	 * @return
	 */
	public int getRewardScore(int multiRank, int totalKill, int state) {
		int rewardScore = 0;
		// 累杀
		StaticBroodWarKillScore configScore = null;
		for (StaticBroodWarKillScore config : broodWarMgr.getKillScores()) {
			if (totalKill < config.getKill()) {
				break;
			}
			configScore = config;
		}
		if (configScore != null) {
			rewardScore = configScore.getScore();
			configScore = null;
		}

		// 连杀
		int cumulativeKillScore = 0;
		if (multiRank > 0) {
			for (StaticBroodWarKillScore config : broodWarMgr.getMulitScores()) {
				if (multiRank <= config.getRank()) {
					configScore = config;
					break;
				}
			}
		}
		if (configScore != null) {
			cumulativeKillScore = configScore.getScore();
		}
		switch (state) {
			case 0:
				return rewardScore;
			case 1:
				return cumulativeKillScore;
			case 2:
				return rewardScore + cumulativeKillScore;
			default:
				break;
		}
		return 0;
	}

	public void stopTimer(BroodWar broodWar) {
//        LogHelper.GAME_LOGGER.info("母巢之战结束 清理线程");
		 logicServer.removeTimer(broodWar.getEvent());
	}


	/**
	 * 定时 战斗检测 立即出击=优先出击>普通出击
	 */
	public void checkBroodWar(BroodWar broodWar) {

		long curTime = TimeHelper.curentTime();
		if (broodWar.getCityType() == CityType.WORLD_FORTRESS) {
			checkBroodWarReport(curTime);
		}
		if (curTime >= broodWar.getEndTime() || !isOpen) {
			broodWar.setState(BroodWarState.END_WAR);
			return;
		}
//		LogHelper.GAME_LOGGER.info("母巢之战 checkBroodWar id:{}", broodWar.getId());
		pushHeroRankChange(broodWar);

		// 检查进度
		if (progress(broodWar)) {
			broodWar.setState(BroodWarState.END_WAR);
			return;
		}

		// 判断下进攻方有没有人
		if (broodWar.getAttackQueue().size() == 0) {
			return;
		}

		if (curTime >= broodWar.getNextAttackTime()) {
			LinkedList<Team> attackQueue = getAttackQue(broodWar);
			LinkedList<Team> defendQueue = getDefendQue(broodWar);

			attackNow(broodWar, attackQueue, defendQueue, curTime);
			int winCountry = simpleAttack(broodWar, attackQueue, defendQueue, curTime);

			// 更改立即出战人员的队列顺序
			resetAttackQue(broodWar);

			// 攻防切换
			if (winCountry != 0) {
				swapSides(broodWar, winCountry);
			}

			broodWar.setNextAttackTime(System.currentTimeMillis() + 3 * TimeHelper.SECOND_MS);
//			LogHelper.GAME_LOGGER.info("【母巢之战】 攻击结束 城池编号:{} ", broodWar.getId());
		}
	}

	private LinkedList<Team> getAttackQue(BroodWar broodWar) {
		LinkedList<Team> result = new LinkedList();
		for (Team e : broodWar.getAttackQueue()) {
			if (e.getCurSoldier() <= 0) {
				continue;
			}
			if (e.getCountry() != broodWar.getDefenceCountry()) {
				result.add(e);
			}
		}

		// 将立即出击的队伍移动到队伍最前面
		List<Team> list = result.stream().filter(e -> e.getRank() == BroodWarRank.ATTACK_NOW).sorted(Comparator.comparing(Team::getParam)).collect(Collectors.toList());
		if (list != null && !list.isEmpty()) {
			result.removeAll(list);
			for (int i = 0; i < list.size(); i++) {
				result.add(0, list.get(i));
			}
		}
		return result;
	}

	private LinkedList<Team> getDefendQue(BroodWar broodWar) {
		LinkedList<Team> result = new LinkedList();
		for (Team e : broodWar.getAttackQueue()) {
			if (e.getCurSoldier() <= 0) {
				continue;
			}
			if (e.getCountry() == broodWar.getDefenceCountry()) {
				result.add(e);
			}
		}

		// 将立即出击的队伍移动到队伍最前面
		List<Team> list = result.stream().filter(e -> e.getRank() == BroodWarRank.ATTACK_NOW).sorted(Comparator.comparing(Team::getParam)).collect(Collectors.toList());
		if (list != null && !list.isEmpty()) {
			result.removeAll(list);
			for (int i = 0; i < list.size(); i++) {
				result.add(0, list.get(i));
			}
		}
		return result;
	}

	/**
	 * 重置攻击队列
	 *
	 * @param broodWar
	 */
	private void resetAttackQue(BroodWar broodWar) {
		LinkedList<Team> attackQue = broodWar.getAttackQueue();
		List<Team> list = attackQue.stream().filter(e -> e.getParam() > 0).sorted(Comparator.comparing(Team::getParam)).collect(LinkedList::new, (l, elem) -> {
			elem.setParam(0);
			l.add(elem);
		}, LinkedList::addAll);

		// 将立即攻击的玩家按
		attackQue.removeAll(list);
		attackQue.addAll(list);
	}

	/**
	 * @Description 每十五分钟记录一次母巢 炮塔的战斗次数
	 * @Param [curTime]
	 * @Return void
	 * @Date 2021/9/22 9:59
	 **/
	public void checkBroodWarReport(long curTime) {
		isOpen = true;
		// 记录15分钟累战斗的次数
		if (curTime - lastRecordLogTime >= 15 * TimeHelper.MINUTE_MS) {
			for (Entry<Long, Integer> entry : recordBroodBattleLog.entrySet()) {
				SpringUtil.getBean(com.game.log.LogUser.class).broodWarEntityBattleCountLog(new NewBroodWarBattleLog(entry.getKey(), lastRecordLogTime, curTime, entry.getValue(), broodWar.getRank()));
			}
			recordBroodBattleLog.clear();
			lastRecordLogTime = curTime;
		}
	}

	/**
	 * 推送单场战斗信息
	 *
	 * @param curentTime
	 * @param team
	 */
	private void pushBroodWarReport(long curentTime, BroodWarInfo info, Player player, Team team) {
		if (team.getTeamType() == TeamType.NPC_CITY) {
			return;
		}
		int attackHeroId = team.getAllEnities().get(0).getEntityId();
		BroodWarPb.SynBroodWarRq.Builder builder = BroodWarPb.SynBroodWarRq.newBuilder();
		builder.setTime(curentTime);
		builder.setHeroId(attackHeroId);
		builder.setLessSoldier(team.getLessSoldier());

		for (Report report : team.getReports()) {
			builder.addWarInfo(report.wrapPb(player).build());
		}

		team.getReports().clear();
		builder.setCurHonour(player.getBroodWarInfo().getDiedSolider() * limitMgr.getNum(SimpleId.BROOD_WAR_DIE_SOLDIERS));
		builder.setTotalKill(player.getBroodWarInfo().getTotalKill());
		builder.setMultiKill(player.getBroodWarInfo().getMulitKill());
		builder.setMultiRank(getScoreRanks(player));
		builder.setReviveTime(CommonPb.PairIntLong.newBuilder().setV1(team.getAllEnities().get(0).getEntityId()).setV2(0).setV3(info.getHeroInfo().getOrDefault(attackHeroId, 0L)).build());
		if (!team.isWin() || team.getLessSoldier() <= 0) {
			player.getBroodWarInfo().cleanHero(attackHeroId);
		}
		builder.addAllFightNow(player.getBroodWarInfo().getFighNow());
		SynHelper.synMsgToPlayer(player, BroodWarPb.SynBroodWarRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarRq.ext, builder.build());
//		LogHelper.GAME_LOGGER.info("母巢之战 推送英雄恢复时间->[{}][{}][{}]", player.roleId, attackHeroId, info.getHeroInfo().getOrDefault(attackHeroId, 0L));
	}

	/**
	 * 推送多个战报信息
	 *
	 * @param curentTime
	 * @param team
	 * @param builder
	 */
	private void pushBroodWarReport(long curentTime, Team team, BroodWarPb.SynBroodWarRq.Builder builder) {
		if (team.getTeamType() == TeamType.NPC_CITY) {
			return;
		}
		if (builder.getWarInfoList().size() == 0) {
			return;
		}
		int attackHeroId = team.getAllEnities().get(0).getEntityId();
		Player player = playerManager.getPlayer(team.getLordId());
		builder.setTime(curentTime);
		builder.setHeroId(attackHeroId);
		builder.setLessSoldier(team.getLessSoldier());
		builder.setCurHonour(player.getBroodWarInfo().getDiedSolider() * limitMgr.getNum(SimpleId.BROOD_WAR_DIE_SOLDIERS));
		builder.setTotalKill(player.getBroodWarInfo().getTotalKill());
		builder.setMultiKill(player.getBroodWarInfo().getMulitKill());
		builder.setMultiRank(getScoreRanks(player));
		builder.addAllFightNow(player.getBroodWarInfo().getFighNow());
		builder.setReviveTime(CommonPb.PairIntLong.newBuilder().setV1(team.getAllEnities().get(0).getEntityId()).setV2(0).setV3(System.currentTimeMillis() + TimeHelper.MINUTE_MS).build());
		LogHelper.GAME_LOGGER.info("【母巢之战】 推送个人战报 playerId:{} heroId:{} cursoldier:{} kill:{}", team.getLordId(), attackHeroId, team.getLessSoldier(), player.getBroodWarInfo().getTotalKill());
		SynHelper.synMsgToPlayer(player, BroodWarPb.SynBroodWarRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarRq.ext, builder.build());
		team.getReports().clear();
	}

	/**
	 * 重新计算血量
	 *
	 * @param team
	 */
	private void afterBattle(Team team) {
		if (team.getTeamType() == TeamType.NPC_CITY) {
			return;
		}

		Player player = playerManager.getPlayer(team.getLordId());

		// 处理玩家扣血
		HashMap<Integer, Integer> attackRec = new HashMap<Integer, Integer>(4);

		// 计算攻击方的血量
		worldManager.caculatePlayer(team, player, attackRec);
		playerManager.synChange(player, Reason.BROOD_WAR);
	}

	/**
	 * 记录战报信息
	 *
	 * @param teamA 进攻方 防守方
	 * @param teamB
	 */
	private BroodWarReport recordBattle(BroodWar broodWar, Team teamA, Team teamB) {
		Report report = null;
		long defencerId = 0;
		if (teamB.getTeamType() == TeamType.NPC_CITY) {
			report = createBroodWarReport(teamA, teamB);
		} else {
			report = createBroodWarReport(teamA, teamB);
			defencerId = teamB.getLordId();
		}

		ReportMsg reportMsg = battleMailManager.createReportMsg(teamA, teamB);
		BroodWarReport broodWarReport = BroodWarReport.builder().attacker(teamA.getLordId()).defencer(defencerId).report(report).reportMsg(reportMsg).build();
		broodWar.getReports().add(broodWarReport);

		int heroA = teamA.getAllEnities().get(0).getEntityId();
		int heroB = teamB.getAllEnities().get(0).getEntityId();

		teamA.getReports().add(report);
		LogHelper.GAME_LOGGER.info("【母巢之战】 recordBattle keyId:{} attackId:{} defendId:{} [{} vs {}]", report.getKeyId(), teamA.getLordId(), teamB.getLordId(), heroA, heroB);

		Report reportB = createBroodWarReport(teamB, teamA);
		teamB.getReports().add(reportB);

		return broodWarReport;
	}

	/**
	 * 创建母巢战斗邮件
	 *
	 * @param teamA
	 * @param teamB
	 * @return
	 */
	public Report createBroodWarReport(Team teamA, Team teamB) {
		Report report = new Report();
		report.setKeyId(System.nanoTime());
		report.setResult(teamA.isWin());

		Player attacker = playerManager.getPlayer(teamA.getLordId());
		// 战报头信息: 成功
		ReportHead leftHead = battleMailManager.createPlayerReportHead(attacker, teamA, attacker.getPos());
		report.setLeftHead(leftHead);

		ReportHead rightHead = null;
		if (teamB.getTeamType() == TeamType.PLAYER) {
			Player p = playerManager.getPlayer(teamB.getLordId());
			rightHead = battleMailManager.createPlayerReportHead(p, teamB, p.getPos());
		} else {
			rightHead = battleMailManager.createCityReportHead(Long.valueOf(teamB.getCityId()).intValue(), teamB, null);
		}
		report.setRightHead(rightHead);

		List<Attender> attackers = battleMailManager.createRebelAttender(teamA);
		report.setLeftAttender(attackers);
		List<Attender> defenders = battleMailManager.createRebelAttender(teamB);
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
	 * 处理立即战斗
	 *
	 * @param broodWar
	 */
	private void attackNow(BroodWar broodWar, LinkedList<Team> attackQueue, LinkedList<Team> defenceQueue, long currentTime) {
		if (attackQueue.isEmpty() || defenceQueue.isEmpty()) {// 队伍为空
			return;
		}

		Team attackTeam = attackQueue.peekFirst();
		Team defenceTeam = defenceQueue.peekFirst();
		if (attackTeam.getCurSoldier() <= 0) {//容错处理
			attackQueue.poll();
			attackNow(broodWar, attackQueue, defenceQueue, currentTime);
			return;
		}

		if (defenceTeam.getCurSoldier() <= 0) {//容错处理
			defenceQueue.poll();
			attackNow(broodWar, attackQueue, defenceQueue, currentTime);
			return;
		}

		// 双方都没有立即出战人员
		if (attackTeam.getRank() != BroodWarRank.ATTACK_NOW && defenceTeam.getRank() != BroodWarRank.ATTACK_NOW) {
			return;
		}

		int heroIdA = attackTeam.getAllEnities().get(0).getEntityId();
		int heroIdB = defenceTeam.getAllEnities().get(0).getEntityId();

		//战斗
		battleMgr.doTeamBattle(attackTeam, defenceTeam, RandomUtil.random, false);

		// 都是立即出战 打完一场 放回队列
		if (attackTeam.getRank() == BroodWarRank.ATTACK_NOW && defenceTeam.getRank() == BroodWarRank.ATTACK_NOW) {

			// 玩家互打
			if (defenceTeam.getTeamType() != TeamType.NPC_CITY) {

				// 推送双方战报
				BroodWarPb.SynBroodWarRq.Builder builder = BroodWarPb.SynBroodWarRq.newBuilder();
				BroodWarReport report = recordBattle(broodWar, attackTeam, defenceTeam);
				addRankPvpHero(attackTeam);
				addRankPvpHero(defenceTeam);
				builder.addWarInfo(report.getReport().wrapPb());
				recordBroodBattleLog.put(broodWar.getId(), recordBroodBattleLog.getOrDefault(broodWar.getId(), 0) + 1);

				// 输赢都要进入普通队列,直接取出来
				attackTeam = attackQueue.poll();
				defenceTeam = defenceQueue.poll();

				afterBattle(attackTeam);
				afterBattle(defenceTeam);
				attackTeam.reset();
				defenceTeam.reset();

				Player attacker = playerManager.getPlayer(attackTeam.getLordId());
				Player defender = playerManager.getPlayer(defenceTeam.getLordId());

				if (attackTeam.getCurSoldier() > 0) {
					attackTeam.setRank(BroodWarRank.ATTACK_COMMON);
					attackQueue.add(attackTeam);// 打完返回队列视为普通出击
				} else {// 失败则有debuff时间
					attacker.getBroodWarInfo().getHeroInfo().put(heroIdA, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);
				}

				if (defenceTeam.getCurSoldier() > 0) {
					defenceTeam.setRank(BroodWarRank.ATTACK_COMMON);
					defenceQueue.add(defenceTeam);
				} else {// 失败则有debuff时间
					defender.getBroodWarInfo().getHeroInfo().put(heroIdB, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);
				}

				// 清除双方的立即攻击记录
				attacker.getBroodWarInfo().cleanHero(attackTeam.getAllEnities().get(0).getEntityId());
				defender.getBroodWarInfo().cleanHero(defenceTeam.getAllEnities().get(0).getEntityId());

				// 推送战斗结果给双方人员
				pushBroodWarReport(currentTime, attackTeam, builder);
				pushBroodWarReport(currentTime, defenceTeam, builder);
			} else {// 防守方为NPC，如果玩家赢了则接着打
				afterBattle(attackTeam);
				attackTeam.reset();
				defenceTeam.reset();

				// 玩家输了
				if (attackTeam.getCurSoldier() < 0) {
					Player attacker = playerManager.getPlayer(attackTeam.getLordId());
					attacker.getBroodWarInfo().getHeroInfo().put(heroIdA, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);

					attackTeam.setRank(BroodWarRank.ATTACK_COMMON);
					attackQueue.poll();
				}

				// NPC输了
				if (defenceTeam.getCurSoldier() < 0) {
					defenceQueue.poll();
				}
			}
		}
		// 进攻方是立马攻击,防守方不是[玩家 VS 玩家]
		else if (attackTeam.getRank() == BroodWarRank.ATTACK_NOW && defenceTeam.getRank() != BroodWarRank.ATTACK_NOW) {
			attackQueue.poll();
			attackTeam.setRank(BroodWarRank.ATTACK_COMMON);

			// 推送双方战报
			BroodWarPb.SynBroodWarRq.Builder builder = BroodWarPb.SynBroodWarRq.newBuilder();
			BroodWarReport report = recordBattle(broodWar, attackTeam, defenceTeam);
			addRankPvpHero(attackTeam);
			addRankPvpHero(defenceTeam);
			builder.addWarInfo(report.getReport().wrapPb());
			recordBroodBattleLog.put(broodWar.getId(), recordBroodBattleLog.getOrDefault(broodWar.getId(), 0) + 1);

			afterBattle(attackTeam);
			afterBattle(defenceTeam);
			attackTeam.reset();
			defenceTeam.reset();

			Player attacker = playerManager.getPlayer(attackTeam.getLordId());
			Player defender = playerManager.getPlayer(defenceTeam.getLordId());

			if (attackTeam.getCurSoldier() > 0) {
				attackQueue.add(attackTeam);// 打完返回队列视为普通出击
			} else {
				attacker.getBroodWarInfo().getHeroInfo().put(heroIdA, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);
			}

			// 防守方输了，则移除
			if (defenceTeam.getCurSoldier() <= 0) {
				defender.getBroodWarInfo().getHeroInfo().put(heroIdB, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);
				defenceQueue.poll();
			}

			// 清除进攻方的立即攻击记录
			attacker.getBroodWarInfo().cleanHero(attackTeam.getAllEnities().get(0).getEntityId());

			// 推送战斗结果给双方人员
			pushBroodWarReport(currentTime, attackTeam, builder);
			pushBroodWarReport(currentTime, defenceTeam, builder);
		}
		// 防守方是立马攻击
		else if (attackTeam.getRank() != BroodWarRank.ATTACK_NOW && defenceTeam.getRank() == BroodWarRank.ATTACK_NOW) {

			// 玩家 VS 玩家
			if (defenceTeam.getTeamType() != TeamType.NPC_CITY) {
				defenceQueue.poll();//移除掉第一个防御
				defenceTeam.setRank(BroodWarRank.ATTACK_COMMON);

				BroodWarPb.SynBroodWarRq.Builder builder = BroodWarPb.SynBroodWarRq.newBuilder();
				BroodWarReport report = recordBattle(broodWar, attackTeam, defenceTeam);
				addRankPvpHero(attackTeam);
				addRankPvpHero(defenceTeam);
				builder.addWarInfo(report.getReport().wrapPb());
				recordBroodBattleLog.put(broodWar.getId(), recordBroodBattleLog.getOrDefault(broodWar.getId(), 0) + 1);

				afterBattle(attackTeam);
				afterBattle(defenceTeam);
				attackTeam.reset();
				defenceTeam.reset();

				Player attacker = playerManager.getPlayer(attackTeam.getLordId());
				Player defencer = playerManager.getPlayer(defenceTeam.getLordId());

				if (attackTeam.getCurSoldier() <= 0) {
					synReportWhenLost(attackTeam, currentTime);
					attackQueue.poll();
				} else {
					attacker.getBroodWarInfo().getHeroInfo().put(heroIdA, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);
				}

				if (defenceTeam.getCurSoldier() > 0) {
					defenceQueue.add(attackTeam);// 打完返回队列视为普通出击
				} else {
					defencer.getBroodWarInfo().getHeroInfo().put(heroIdB, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);
				}

				defencer.getBroodWarInfo().cleanHero(defenceTeam.getAllEnities().get(0).getEntityId());

				pushBroodWarReport(currentTime, defenceTeam, builder);
			} else {//玩家 VS NPC
				afterBattle(attackTeam);
				afterBattle(defenceTeam);
				attackTeam.reset();
				defenceTeam.reset();

				if (attackTeam.getCurSoldier() <= 0) {
					Player attacker = playerManager.getPlayer(attackTeam.getLordId());
					attacker.getBroodWarInfo().getHeroInfo().put(heroIdA, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);
					attackQueue.poll();
				}

				if (defenceTeam.getCurSoldier() <= 0) {
					defenceQueue.poll();
				}
			}
		}

		LogHelper.GAME_LOGGER.info("【母巢之战】 立即攻击 攻击:{} marchId:{} 防守:{} marchId:{} 对阵英雄 [ {} vs {} ]", attackTeam.getLordId(), attackTeam.getMarchId(), defenceTeam.getLordId(), defenceTeam.getMarchId(), heroIdA, heroIdB);
		// 递归调用下一个有立即出击的,一直打到双方都没有立即出击的人为止
		attackNow(broodWar, attackQueue, defenceQueue, currentTime);
	}


	/**
	 * 优先出击和普通出击
	 *
	 * @param broodWar
	 * @param currentTime
	 */
	private int simpleAttack(BroodWar broodWar, LinkedList<Team> attackQueue, LinkedList<Team> defenceQueue, long currentTime) {
		if (attackQueue.isEmpty() && defenceQueue.isEmpty()) {
			return 0;
		}

		// 防守阵容为空,进攻阵容有人
		if (defenceQueue.isEmpty()) {
			return attackQueue.get(0).getCountry();
		}

		Iterator<Team> it = attackQueue.iterator();
		while (it.hasNext()) {

			Team attackTeam = it.next();
			if (attackTeam.getCurSoldier() <= 0) {// 血量没有了,直接删除
				it.remove();
				continue;
			}

			if (attackTeam.getRank() == BroodWarRank.ATTACK_NOW) {
				continue;
			}

			if (attackTeam.getTeamType() == TeamType.NPC_CITY) {// 进攻方没有NPC
				it.remove();
				continue;
			}

			// 防守队列为空，则进攻阵容直接成为防守阵容
			if (defenceQueue.isEmpty()) {// 防守方全部阵亡
				return attackTeam.getCountry();
			}

			// 打一场就行了
			Iterator<Team> df = defenceQueue.iterator();
			while (df.hasNext()) {
				Team defenceTeam = df.next();
				if (attackTeam.getCurSoldier() <= 0) {
					df.remove();
					continue;
				}

				int heroIdA = attackTeam.getAllEnities().get(0).getEntityId();
				int heroIdB = defenceTeam.getAllEnities().get(0).getEntityId();

				// 直接战斗
				battleMgr.doTeamBattle(attackTeam, defenceTeam, RandomUtil.random, false);

				if (defenceTeam.getTeamType() != TeamType.NPC_CITY) {
					recordBattle(broodWar, attackTeam, defenceTeam);
					addRankPvpHero(attackTeam);
					addRankPvpHero(defenceTeam);
					recordBroodBattleLog.put(broodWar.getId(), recordBroodBattleLog.getOrDefault(broodWar.getId(), 0) + 1);
				}

				afterBattle(attackTeam);
				afterBattle(defenceTeam);
				attackTeam.reset();
				defenceTeam.reset();

				// 防守方阵亡
				if (defenceTeam.getCurSoldier() <= 0) {
					synReportWhenLost(defenceTeam, currentTime);
					df.remove();
				}

				// 攻防阵亡
				if (attackTeam.getCurSoldier() <= 0) {
					synReportWhenLost(attackTeam, currentTime);
					it.remove();
					break;
				}
				LogHelper.GAME_LOGGER.info("【母巢之战】 普通攻击 攻击:{} marchId:{} 防守:{} marchId:{} 对阵英雄 [ {} vs {} ]", attackTeam.getLordId(), attackTeam.getMarchId(), defenceTeam.getLordId(), defenceTeam.getMarchId(), heroIdA, heroIdB);
			}

			// 防守方全部死亡
			if (defenceQueue.isEmpty()) {
				return attackTeam.getCountry();
			}
		}
		return 0;
	}

	/**
	 * 推送英雄排名的信息
	 *
	 * @param broodWar
	 */
	private void pushHeroRankChange(BroodWar broodWar) {
		if (broodWar instanceof Turret) {
			return;
		}
		Map<Long, Player> tmpOnline = new HashMap<>();
		playerManager.getOnlinePlayer().forEach(e -> {
			tmpOnline.put(e.roleId, e);
		});

		List<StaticWorldCity> citys = staticWorldMgr.getCityMap().values().stream().filter(e -> e.getType() == CityType.WORLD_FORTRESS || e.getType() == CityType.BROOD_WAR_TURRET).collect(Collectors.toList());
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		// lordId heroId list: index 恢复时间
		Map<Long, Map<Integer, List<Long>>> playerMap = new HashMap<>(16);
		for (StaticWorldCity city : citys) {
			int iIndexAttack = 0;
			int indexDefault = 0;
			BroodWar entity = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
			int defendCountry = entity.getDefenceCountry();
			for (Team team : entity.getAttackQueue()) {
				if (team.getCurSoldier() <= 0) {
					continue;
				}
				if (defendCountry == team.getCountry()) {
					indexDefault++;
					Map<Integer, List<Long>> heroMap = playerMap.computeIfAbsent(team.getLordId(), x -> new HashMap<>(16));
					ArrayList<Long> list = Lists.newArrayList();
					list.add((long) indexDefault);
					list.add(entity.getId());
					if (team.getAllEnities().size() != 0) {
						heroMap.put(team.getAllEnities().get(0).getEntityId(), list);
					}
				} else {
					iIndexAttack++;
					Map<Integer, List<Long>> heroMap = playerMap.computeIfAbsent(team.getLordId(), x -> new HashMap<>(16));
					ArrayList<Long> list = Lists.newArrayList();
					list.add((long) iIndexAttack);
					list.add(entity.getId());
					if (team.getAllEnities().size() != 0) {
						heroMap.put(team.getAllEnities().get(0).getEntityId(), list);
					}
				}
			}
		}
		tmpOnline.forEach((e, f) -> {
			if (!playerMap.containsKey(e)) {
				playerMap.put(e, new HashMap<>(16));
			}
		});

		BroodWarPb.SynHeroRankChangeRq.Builder builder = BroodWarPb.SynHeroRankChangeRq.newBuilder();
		playerMap.entrySet().forEach(e -> {
			Player p = playerManager.getPlayer(e.getKey());
			Map<Integer, List<Long>> heroMap = e.getValue();
			if (p != null && p.getBroodWarInfo() != null && p.isLogin && p.getChannelId() != -1) {
				builder.clear();
				p.getEmbattleList().forEach(heroId -> {
					Long f = p.getBroodWarInfo().getHeroInfo().getOrDefault(heroId, 0L);
					int v2 = heroMap.get(heroId) == null ? 0 : Math.toIntExact(heroMap.get(heroId).get(0));
					int v4 = heroMap.get(heroId) == null ? 0 : Math.toIntExact(heroMap.get(heroId).get(1));
					builder.addHeroInfo(CommonPb.HeroRankChange.newBuilder().setV1(heroId).setV2(v2).setV3(f).setV4(v4).build());
				});
				SynHelper.synMsgToPlayer(p, BroodWarPb.SynHeroRankChangeRq.EXT_FIELD_NUMBER, BroodWarPb.SynHeroRankChangeRq.ext, builder.build());
			}
		});
	}

	/**
	 * 炮塔的输出 每分钟输出一次
	 */
	private void turretFight(Turret turret) {
		// 每分钟计算一次伤害
		long nextFireTime = turret.getNextFireTime();
		long currentTime = TimeHelper.curentTime();
		if (currentTime >= nextFireTime) {
			turret.setNextFireTime(currentTime + TURN_FIGHT_TIME);
			// 同一阵营的不攻击
			if (turret.getDefenceCountry() == broodWar.getDefenceCountry()) {
				return;
			}

			LogHelper.GAME_LOGGER.info("炮塔进攻 母巢防守方", turret.getDefenceCountry(), broodWar.getDefenceCountry());
			// 对母巢所有守军造成伤害
			LinkedList<Team> defendQueue = getDefendQue(broodWar);
			int totalDamage = 0;
			Iterator<Team> iterator = defendQueue.iterator();
			while (iterator.hasNext()) {
				Team e = iterator.next();
				for (BattleEntity f : e.getAllEnities()) {
					f.cacSoldierNum();
					int lineNumber = f.getLineNumber();
					int before = f.getCurSoldierNum();
					// 每个守军5%的伤害
					int lost = Double.valueOf(Math.ceil(before * 0.05D)).intValue();
					LineEntity lineEntity = f.getLineEntity();
					do {
						int soldierNum = lineEntity.getSoldierNum();
						int result = soldierNum - lost;
						if (result >= 0) {
							lineEntity.setSoldierNum(result);
							break;
						} else if (result < 0) {
							lineNumber--;
							if (lineNumber == 1) {
								lineEntity.setSoldierNum(f.getLastLineSoldierNum());
								f.setLeftSoldier(0);
							} else if (lineNumber > 1) {
								lineEntity.setSoldierNum(f.getSoldierNum());
							}
							lost = Math.abs(result);
						}
					} while (lineNumber > 0);
					f.setLineNumber(lineNumber);
					f.cacSoldierNum();
					totalDamage += (before - f.getCurSoldierNum());
				}
				// 处理玩家扣血
				Player player = playerManager.getPlayer(e.getLordId());
				if (player != null) {
					HashMap<Integer, Integer> attackRec = new HashMap<Integer, Integer>(16);
					worldManager.caculatePlayer(e, player, attackRec);
					// 玩家兵力
					if (e.getCurSoldier() <= 0) {
						March march = player.getMarch(e.getMarchId());
						if (march == null) {
							LogHelper.GAME_LOGGER.info("【母巢之战】行军为空 playerId:{} marchId:{}", e.getLordId(), e.getMarchId());
						} else {
							marchManager.doMarchReturn(march, player, Reason.BROOD_WAR);
						}
						synReportWhenLost(e, currentTime);
						iterator.remove();
					}
				}
				e.reset();
			}
			// 推送下所有人知道 炮塔进攻了
			BroodWarPb.SynTurretAttackRq.Builder builder = BroodWarPb.SynTurretAttackRq.newBuilder();
			builder.setCityId(Long.valueOf(turret.getId()).intValue());
			builder.setDamage(totalDamage);
			BroodWarPb.SynTurretAttackRq msg = builder.build();
			playerManager.getOnlinePlayer().forEach(player -> {
				SynHelper.synMsgToPlayer(player, BroodWarPb.SynTurretAttackRq.EXT_FIELD_NUMBER, BroodWarPb.SynTurretAttackRq.ext, msg);
				playerManager.synChange(player, Reason.BROOD_WAR);

			});
			// 推送下跑马灯
			StaticWorldCity city = staticWorldMgr.getCity(Long.valueOf(turret.getId()).intValue());
			chatManager.sendWorldChat(ChatId.CHAT_165, city.getName(), Integer.toString(totalDamage));
		}
	}

	/**
	 * 开战
	 *
	 * @param broodWar
	 */
	private void changeToWarBegin(BroodWar broodWar) {
		// 非开启购买增益buff
		if (broodWar.getState() != BroodWarState.OPEN_BUY) {
			return;
		}

		if (broodWar.getState() == BroodWarState.BEGIN_WAR) {
			return;
		}

		// 可以购买了 活动开始了之后 推送城池变化信息
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);
		if (worldActPlan == null) {
			return;
		}

		// 活动还没有到开启时间
		if (TimeHelper.curentTime() < worldActPlan.getOpenTime()) {
			return;
		}

		LogHelper.GAME_LOGGER.info("【母巢之战】战斗阶段 预热时间:{} 开始时间:{} 结束时间:{}", TimeHelper.getFormatData(new Date(worldActPlan.getPreheatTime())), TimeHelper.getFormatData(new Date(worldActPlan.getOpenTime())), TimeHelper.getFormatData(new Date(worldActPlan.getEndTime())));
		broodWar.setEndTime(worldActPlan.getOpenTime() + TimeHelper.HOUR_MS);
		broodWar.beforeStart();
		broodWar.setState(BroodWarState.BEGIN_WAR);

		// 每次活动开启初始化守军
		initPveBoss(broodWar);

		if (broodWar.getCityType() == CityType.BROOD_WAR_TURRET) {
			Turret turret = (Turret) broodWar;
			turret.setNextFireTime(TimeHelper.curentTime() + TURN_FIGHT_TIME);
			return;
		}

		lastRecordLogTime = System.currentTimeMillis();
		recordBroodBattleLog.clear();
		// 重置任命信息
		recordAppoints(broodWar, worldData);
		// 清理玩家参与母巢的信息
		playerManager.getAllPlayer().values().forEach(e -> {
			BroodWarInfo info = e.getBroodWarInfo();
			if (info != null) {
				info.reset();
			}
			e.setBroodWarPosition(null);
		});
		// 清理排名信息
		ranks.clear();
		if (broodWar.getDefenceCountry() != 0) {
			BroodWarPb.SynBroodWarOccupyRq.Builder builder = BroodWarPb.SynBroodWarOccupyRq.newBuilder();
			builder.setOccupyPercentage(true);
			builder.setCurentCountry(broodWar.getDefenceCountry());
			BroodWarPb.SynBroodWarOccupyRq msg = builder.build();
			playerManager.getOnlinePlayer().forEach(e -> {
				SynHelper.synMsgToPlayer(e, BroodWarPb.SynBroodWarOccupyRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarOccupyRq.ext, msg);
			});
		}
	}

	/**
	 * 母巢之战开启购买
	 *
	 * @param broodWar
	 */
	private void checkIsOpen(BroodWar broodWar) {
		if (broodWar.getCityType() == CityType.WORLD_FORTRESS) {
			this.broodWar = broodWar;
		}

		// 停止定时线程
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);

		if (worldActPlan == null) {
			stopTimer(broodWar);
			return;
		}

//		if (worldActPlan.getState() == WorldActPlanConsts.OVER || worldActPlan.getState() == WorldActPlanConsts.NOE_OPEN) {
//			stopTimer(broodWar);
//			return;
//		}

		if (broodWar.getOpenBuyBuffTime() == 0) {
			broodWar.setOpenBuyBuffTime(worldActPlan.getOpenTime() - TimeHelper.MINUTE_MS * 15);
		}

		// 已经是购买状态
		if (broodWar.getState() == BroodWarState.OPEN_BUY) {
			return;
		}

		if (TimeHelper.curentTime() >= broodWar.getOpenBuyBuffTime()) {
			LogHelper.GAME_LOGGER.info("【母巢之战】开始购买buff 预热时间:{} 开始时间:{} 结束时间:{}", TimeHelper.getFormatData(new Date(worldActPlan.getPreheatTime())), TimeHelper.getFormatData(new Date(worldActPlan.getOpenTime())), TimeHelper.getFormatData(new Date(worldActPlan.getEndTime())));
			broodWar.setState(BroodWarState.OPEN_BUY);
			broodWar.setOpenBuyBuffTime(0);
			// 炮台不用推送
			if (broodWar instanceof Turret) {
				return;
			}

			BroodWarPb.SynBuyBuffRq msg = BroodWarPb.SynBuyBuffRq.newBuilder().build();
			playerManager.getOnlinePlayer().forEach(player -> {
				SynHelper.synMsgToPlayer(player, BroodWarPb.SynBuyBuffRq.EXT_FIELD_NUMBER, BroodWarPb.SynBuyBuffRq.ext, msg);
			});
		}
	}

	/**
	 * 通知全体玩家下一届母巢之战开启的时候
	 */
	private void resetAllPlayerInfo(long nextOpenTime) {
		playerManager.getAllPlayer().values().parallelStream().forEach(e -> {
			playerManager.sendNormalMail(e, MailId.BROOW_WAR_NEXT_TIME, DateHelper.getDate(nextOpenTime));
			BroodWarInfo info = e.getBroodWarInfo();
			if (info != null) {
				info.getBroodWarBuff().clear();
				info.getBroodWarBuffBuy().clear();
				info.setFirstAttack(0);
				info.setAttackNow(0);
				info.setRevive(0);
			}
		});
	}

	/**
	 * 交换攻守 这时候守方是肯定没人的
	 *
	 * @param country 新的守方阵营
	 */
	public void swapSides(BroodWar broodWar, int country) {
		int lastCountry = broodWar.getDefenceCountry();
		long currentTime = TimeHelper.curentTime();

		// 切换守方阵营
		broodWar.setDefenceCountry(country);

		// 计算下占领时长
		broodWar.setNextAttackTime(currentTime + TimeHelper.THREE_SECOND_MS);
		initPveBoss(broodWar);
		changeCityCountry(broodWar, country);
		// 切换了攻守方
		if (broodWar instanceof Turret) {
			// 炮塔换了阵营需要更新炮塔的攻击时间
			Turret turret = (Turret) broodWar;
			turret.setNextFireTime(currentTime + TURN_FIGHT_TIME);
			return;
		} else {
			BroodWarPb.SynBroodWarOccupyRq.Builder builder = BroodWarPb.SynBroodWarOccupyRq.newBuilder();
			builder.setOccupyPercentage(true);
			builder.setCurentCountry(country);
			BroodWarPb.SynBroodWarOccupyRq msg = builder.build();
			playerManager.getOnlinePlayer().forEach(e -> {
				SynHelper.synMsgToPlayer(e, BroodWarPb.SynBroodWarOccupyRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarOccupyRq.ext, msg);
			});
		}
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		List<StaticWorldCity> citys = staticWorldMgr.getCityMap().values().stream().filter(e -> e.getType() == CityType.BROOD_WAR_TURRET).collect(Collectors.toList());
		for (StaticWorldCity city : citys) {
			Entity entity = mapInfo.getEntity(new Pos(city.getX(), city.getY()));
			if (entity == null) {
				continue;
			}
			Turret turret = (Turret) entity;
			if (turret.getDefenceCountry() == lastCountry) {
				turret.setNextFireTime(currentTime + TURN_FIGHT_TIME);
			}
		}
		chatManager.sendWorldChat(ChatId.CHAT_167, Integer.toString(lastCountry), Integer.toString(country));
		if (lastCountry == 0) {
			return;
		}
		int percentage = broodWar.getOccupyPercentage().getOrDefault(lastCountry, 0);
		Float result = percentage * (1 - limitProcess);
		result = Math.max(0, result);
		percentage = result.intValue();
		broodWar.getOccupyPercentage().put(lastCountry, percentage);
		// 更新占领时间
		if (0 != broodWar.getOccupyPercentageTime().getOrDefault(lastCountry, 0l)) {
			Integer orDefault = broodWar.getOccupyTime().getOrDefault(lastCountry, 0);
			int after = orDefault + (int) ((currentTime - broodWar.getOccupyPercentageTime().get(lastCountry)) / TimeHelper.SECOND_MS);
			broodWar.getOccupyTime().put(lastCountry, after);
			broodWar.getOccupyPercentageTime().put(lastCountry, 0l);
		}

		// 更新母巢占领进度
		BroodWarPb.SynBroodWarOccupyRq.Builder builder = BroodWarPb.SynBroodWarOccupyRq.newBuilder();
		broodWar.getOccupyTime().forEach((x, time) -> {
			builder.addOccupy(CommonPb.ThreeInt.newBuilder().setV1(x).setV2(broodWar.getOccupyPercentage().getOrDefault(x, 0)).setV3(time).build());
		});
		builder.setCurentCountry(broodWar.getDefenceCountry());
		BroodWarPb.SynBroodWarOccupyRq msg = builder.build();
		playerManager.getOnlinePlayer().forEach(e -> {
			SynHelper.synMsgToPlayer(e, BroodWarPb.SynBroodWarOccupyRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarOccupyRq.ext, msg);
		});
	}

	/**
	 *
	 */
	public void changeCityCountry(BroodWar broodWar, int country) {
		// 获取城池
		City city = cityManager.getCity(Long.valueOf(broodWar.getId()).intValue());
		if (city == null) {
			LogHelper.CONFIG_LOGGER.info("city is null!");
			return;
		}
		city.setCountry(country);
		WorldPb.SynMapCityRq.Builder builder = WorldPb.SynMapCityRq.newBuilder();
		CommonPb.CityOwnerInfo.Builder cityOwnerInfo = worldManager.createCityOwner(city);
		cityOwnerInfo.setCanAttendElection(false);
		WorldPb.SynMapCityRq msg = builder.setInfo(cityOwnerInfo).build();
		playerManager.getOnlinePlayer().forEach(e -> {
			playerManager.synMapCityRq(e, msg);
		});
	}

	public Map<Integer, Integer> getRank(Player player, List<Integer> heros) {
		Map<Integer, Integer> map = new HashMap<>(16);
		StaticWorldCity city = staticWorldMgr.getCity(CityId.WORLD_CITY_ID);
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		BroodWar broodWar = (BroodWar) mapInfo.getEntity(new Pos(city.getX(), city.getY()));
		LinkedList<Team> linkedList = getDefendQue(broodWar);
		if (player.getCountry() == broodWar.getDefenceCountry()) {
			for (int i = 0; i < linkedList.size(); i++) {
				Team team = linkedList.get(i);
				if (team.getLordId() == player.roleId && heros.contains(team.getAllEnities().get(0).getEntityId())) {
					map.put(team.getAllEnities().get(0).getEntityId(), i + 1);
				}
			}
		}
		return map;
	}

	/**
	 * 初始化玩家buff
	 *
	 * @param broodWarInfo
	 */
	public BroodWarInfo initBuff(BroodWarInfo broodWarInfo) {
		if (broodWarInfo.getBroodWarBuff().size() == 0) {
			broodWarMgr.getBuffMap().values().stream().forEach(e -> {
				if (e.getLv() == 0) {
					broodWarInfo.getBroodWarBuff().put(e.getType(), e.getId());
				}
			});
		}
		return broodWarInfo;
	}

	public StaticBroodWarBuff getBuff(int buffId) {
		return broodWarMgr.getBuffMap().get(buffId);
	}

	/**
	 * 获取buff
	 *
	 * @param buffId
	 * @return
	 */
	public StaticBroodWarBuff getNextBuff(int buffId, int type) {
		for (StaticBroodWarBuff buff : broodWarMgr.getBuffMap().values()) {
			if (buff.getType() == type && buff.getId() == buffId) {
				return buff;
			}
		}
		return null;
	}

	/**
	 * 行军到达了
	 *
	 * @param march
	 */
	public void handleMarchArrive(MapInfo mapInfo, March march) {
		BroodWar entity = (BroodWar) mapInfo.getEntity(march.getEndPos());
		if (!isOpen || entity.getState() != BroodWarState.BEGIN_WAR) {
			worldManager.handleMiddleReturn(march, Reason.BROOD_WAR);
			worldManager.synMarch(mapInfo.getMapId(), march);
			return;
		}
		// 设置行军皇城血战中
		march.setEndTime(entity.getEndTime());
		march.setState(MarchState.Waiting);
		worldManager.synMarch(20, march);
		Player player = playerManager.getPlayer(march.getLordId());
		BroodWarInfo info = player.getBroodWarInfo();
		switch (march.getType()) {
			default:
				handleSimple(march, entity);
				break;
			case 1:
				handleQuick(march, entity);
				break;
		}
	}

	/**
	 * 普通出击到达后的逻辑
	 *
	 * @param march
	 */
	private void handleSimple(March march, BroodWar broodWar) {
		Player player = playerManager.getPlayer(march.getLordId());
		Team playerTeam = battleMgr.initPlayerTeam(player, march.getHeroIds(), BattleEntityType.HERO);
		playerTeam.setTeamType(TeamType.PLAYER);
		playerTeam.setLordId(march.getLordId());
		playerTeam.setRank(BroodWarRank.ATTACK_COMMON);
		playerTeam.setMarchId(march.getKeyId());

		broodWar.getAttackQueue().add(playerTeam);
		LogHelper.GAME_LOGGER.info("【母巢之战】 普通抵达 playerId:{} heroId:{} marchId:{} state:{} startPos:{} endPos:{} cityId:{} ", march.getLordId(), march.getHeroIds().get(0), march.getKeyId(), march.getState(), march.getStartPos(), march.getEndPos(), broodWar.getCityType());
	}

	/**
	 * 优先出击到达后的逻辑处理
	 *
	 * @param march
	 */
	private void handleQuick(March march, BroodWar broodWar) {
		Player player = playerManager.getPlayer(march.getLordId());
		Team playerTeam = battleMgr.initPlayerTeam(player, march.getHeroIds(), BattleEntityType.HERO);
		playerTeam.setLordId(march.getLordId());
		playerTeam.setRank(BroodWarRank.ATTACK_FIRST);
		playerTeam.setTeamType(TeamType.PLAYER);
		playerTeam.setMarchId(march.getKeyId());

		offerIndex(broodWar.getAttackQueue(), playerTeam, player);
		LogHelper.GAME_LOGGER.info("【母巢之战】 优先抵达 playerId:{} heroId:{} marchId:{} state:{} startPos:{} endPos:{} cityId:{} ", march.getLordId(), march.getHeroIds().get(0), march.getKeyId(), march.getState(), march.getStartPos(), march.getEndPos(), broodWar.getCityType());
	}

	/**
	 * 处理插队逻辑
	 *
	 * @param queue
	 * @param playerTeam
	 */
	private void offerIndex(LinkedList<Team> queue, Team playerTeam, Player player) {
		// 插队了要
		boolean isFind = false;
		for (int i = 0; i < queue.size(); i++) {
			Team t = queue.get(i);
			// 找到第一个非优先出击队列的人员 插到他前面
			if (t.getRank() == BroodWarRank.ATTACK_COMMON) {
				isFind = true;
				queue.add(i, playerTeam);
				break;
			}
		}
		if (!isFind) {
			queue.add(playerTeam);
		}
	}

//	/**
//	 * 蒋队列中的某个前置
//	 *
//	 * @param broodWar
//	 * @param player
//	 * @param heroId
//	 */
//	public GameError offerAttackNow(BroodWar broodWar, Player player, int heroId) {
//		LinkedList<Team> queue = broodWar.getAttackQueue();
//		if (queue.isEmpty()) {
//			return GameError.WAR_NOT_EXISTS;
//		}
//
//		Optional<Team> optional = queue.stream().filter(e -> !e.getGameEntities().isEmpty() && e.getGameEntities().get(0).getEntityId() == heroId && e.getRank() != BroodWarRank.ATTACK_NOW).findFirst();
//
//		if (optional.isPresent()) {// 设至为立即攻击
//			Team team = optional.get();
//			team.setRank(BroodWarRank.ATTACK_NOW);
//			team.setParam(System.currentTimeMillis());
//
//			March march = player.getMarch(team.getMarchId());
//			if (march != null) {
//				LogHelper.GAME_LOGGER.info("【母巢之战】 立即攻击 playerId:{} heroId:{} ma state:{} cityId:{}", player.getRoleId(), heroId, march.getState(), broodWar.getCityType());
//			} else {
//				LogHelper.GAME_LOGGER.info("【母巢之战】 立即攻击 march不在 playerId:{} heroId:{} marchId:{} state:{} cityId:{}", player.getRoleId(), heroId, team.getMarchId(), broodWar.getCityType());
//			}
//
//			return GameError.OK;
//		}
//
//		return GameError.WAR_NOT_EXISTS;
//	}

	/***
	 * 属性获取
	 *
	 * @param player
	 * @param hero
	 * @param property
	 * @return
	 */
	public Property getBroodWarProperty(Player player, Hero hero, Property property) {
		BroodWarInfo info = player.getBroodWarInfo();
		if (info == null) {
			return property;
		}
		for (int buffId : info.getBroodWarBuff().values()) {
			StaticBroodWarBuff buff = getBuff(buffId);
			if (buff != null && buff.getBuff() != null) {
				buff.getBuff().forEach(e -> {
					e.forEach(x -> {
						if (e.size() != 2) {
							LogHelper.CONFIG_LOGGER.info(" buff config error->[{}]", buffId);
							return;
						}
						property.addValue(e.get(0), e.get(1));
					});
				});
			}
		}
		return property;
	}

	/**
	 * 判定用户杀敌数
	 *
	 * @param team
	 */
	private void addRankPvpHero(Team team) {
		if (team.getTeamType() == TeamType.NPC_CITY) {
			return;
		}
		RankPvpHero rankPvpHero = new RankPvpHero();
		Player player = playerManager.getPlayer(team.getLordId());

		BroodWarInfo info = player.getBroodWarInfo();
		for (BattleEntity entity : team.getGameEntities()) {
			team.setMulitKill(team.getMulitKill() + entity.getKillNum());
			info.addTotalKill(entity.getKillNum());
			info.addTotalLost(entity.getLost());
			if (info.getMulitKill() < team.getMulitKill()) {
				info.setMulitKill(team.getMulitKill());
			}
		}
		rankPvpHero.setHeroId(team.getAllEnities().get(0).getEntityId());
		rankPvpHero.setLordId(team.getLordId());
		rankPvpHero.setMutilKill(info.getMulitKill());

		if (ranks.isEmpty()) {
			ranks.add(rankPvpHero);
			return;
		}
		// 先把用户移出来
		for (int i = 0; i < ranks.size(); i++) {
			RankPvpHero r = ranks.get(i);
			if (r.getLordId() == rankPvpHero.getLordId()) {
				if (r.getMutilKill() > rankPvpHero.getMutilKill()) {
					rankPvpHero = r;
				}
				ranks.remove(i);
				break;
			}
		}
		boolean isIn = false;
		for (int i = 0; i < ranks.size(); i++) {
			RankPvpHero r = ranks.get(i);
			if (rankPvpHero.getMutilKill() > r.getMutilKill()) {
				ranks.add(i, rankPvpHero);
				isIn = true;
				break;
			}
		}
		if (!isIn) {
			ranks.add(rankPvpHero);
		}
		LogHelper.GAME_LOGGER.info("【母巢之战】 pvp排名 playerId:{}", team.getLordId());
	}

	public int getScoreRanks(Player p) {
		List<RankPvpHero> list = ranks.stream().collect(Collectors.toList());
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getLordId() == p.roleId) {
				return i + 1;
			}
		}
		return 0;
	}

	private void initAllPveBoss() {
		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		staticWorldMgr.getCityMap().values().forEach(e -> {
			if (e.getType() == CityType.WORLD_FORTRESS || e.getType() == CityType.BROOD_WAR_TURRET) {
				BroodWar broodWar = (BroodWar) mapInfo.getEntity(new Pos(e.getX(), e.getY()));
				initPveBoss(broodWar);
			}
		});
	}

	/**
	 * 初始化母巢防守npc
	 */
	private void initPveBoss(BroodWar broodWar) {
		Team cityTeam = battleMgr.initBroodWarCityTeam(Long.valueOf(broodWar.getId()).intValue());
		cityTeam.setTeamType(TeamType.NPC_CITY);
		cityTeam.setRank(BroodWarRank.ATTACK_NOW);
		cityTeam.setCountry(broodWar.getDefenceCountry());
		cityTeam.setCityId(broodWar.getId());
		cityTeam.setParam(1);//永远排在最前面
		broodWar.getAttackQueue().add(0, cityTeam);
		LogHelper.GAME_LOGGER.info("【母巢之战】 初始化怪物 country:{} 第几届母巢之战:{}", broodWar.getDefenceCountry(), broodWar.getId());
	}

	/**
	 * 计算进度
	 */
	private boolean progress(BroodWar broodWar) {
		long currentTime = System.currentTimeMillis();
		// 炮台不参与进度计算
		if (broodWar instanceof Turret) {
			return false;
		}

		if (broodWar.getDefenceCountry() == 0) {
			return false;
		}

		Optional<Team> optional = broodWar.getAttackQueue().stream().filter(e -> e.getCurSoldier() > 0 && e.getCountry() == broodWar.getDefenceCountry()).findAny();
		if (!optional.isPresent()) {
			return false;
		}

		// 占领时间
		Long lastTime = broodWar.getOccupyPercentageTime().getOrDefault(broodWar.getDefenceCountry(), 0l);
		if (lastTime == 0) {
			lastTime = currentTime;
			pushProcessMsg(broodWar, 0);
			broodWar.getOccupyPercentageTime().put(broodWar.getDefenceCountry(), currentTime);
		}
		int change = (int) ((currentTime - lastTime) / TimeHelper.SECOND_MS);
		if (change >= process) {
			broodWar.getOccupyPercentageTime().put(broodWar.getDefenceCountry(), currentTime);
			int occupyTime = broodWar.getOccupyTime().getOrDefault(broodWar.getDefenceCountry(), 0);
			occupyTime += change;
			broodWar.getOccupyTime().put(broodWar.getDefenceCountry(), occupyTime);
			if (occupyTime != 0) {
				pushProcessMsg(broodWar, 1);
			}
		}
		// 阵营占领进度到了100%
		if (broodWar.getOccupyPercentage().getOrDefault(broodWar.getDefenceCountry(), 0) >= 100) {
			return true;
		}

		return false;
	}

	private void pushProcessMsg(BroodWar broodWar, int change) {
		// 占领进度
		int occupyPercentage = broodWar.getOccupyPercentage().getOrDefault(broodWar.getDefenceCountry(), 0);
		occupyPercentage += change;
		broodWar.getOccupyPercentage().put(broodWar.getDefenceCountry(), occupyPercentage);
		// 同步占领进度
		BroodWarPb.SynBroodWarOccupyRq.Builder builder = BroodWarPb.SynBroodWarOccupyRq.newBuilder();
		broodWar.getOccupyTime().forEach((country, time) -> {
			builder.addOccupy(CommonPb.ThreeInt.newBuilder().setV1(country).setV2(broodWar.getOccupyPercentage().getOrDefault(country, 0)).setV3(time).build());
		});
		builder.setCurentCountry(broodWar.getDefenceCountry());
		BroodWarPb.SynBroodWarOccupyRq msg = builder.build();
		playerManager.getOnlinePlayer().forEach(e -> {
			SynHelper.synMsgToPlayer(e, BroodWarPb.SynBroodWarOccupyRq.EXT_FIELD_NUMBER, BroodWarPb.SynBroodWarOccupyRq.ext, msg);
		});
	}

	/**
	 * 输掉的队伍处理
	 *
	 * @param team
	 * @param currentTime
	 */
	private void synReportWhenLost(Team team, long currentTime) {
		if (team.getTeamType() == TeamType.NPC_CITY) {
			return;
		}
		if (team.getCurSoldier() > 0) {
			return;
		}
		int heroId = team.getAllEnities().get(0).getEntityId();
		Player p = playerManager.getPlayer(team.getLordId());
		BroodWarInfo info = p.getBroodWarInfo();
		info.getHeroInfo().put(heroId, System.currentTimeMillis() + 2 * TimeHelper.MINUTE_MS);
		pushBroodWarReport(currentTime, info, p, team);
	}

	/**
	 * 增加母巢之战积分
	 *
	 * @param player
	 * @param score
	 */
	public void addScore(Player player, long score) {
		playerManager.addAward(player, AwardType.PVP_SCORE, 0, score, Reason.BROOD_WAR);
	}

	/**
	 * 募兵加速
	 *
	 * @param player
	 * @return
	 */
	public double getSoldierSpeed(Player player, int soldierType) {
		double ret = 0.0;
		if (soldierType == SoldierType.ROCKET_TYPE) {
			ret = getCommandBuff(player, TechEffectId.ADD_ROCKET_SOLDIER_NUM);
		} else if (soldierType == SoldierType.TANK_TYPE) {
			ret = getCommandBuff(player, TechEffectId.ADD_TANK_SOLDIER_NUM);
		} else if (soldierType == SoldierType.WAR_CAR) {
			ret = getCommandBuff(player, TechEffectId.ADD_WARCAR_SOLDIER_NUM);
		}
		return ret;
	}

	/**
	 * 指挥官科技检查
	 *
	 * @param player
	 * @param techType
	 * @return
	 */
	public double getCommandBuff(Player player, int techType) {
		double ret = 0D;
		if (player.getBroodWarPosition() != null) {
			int position = player.getBroodWarPosition().getPosition();
			StaticBroodWarCommand config = broodWarMgr.getCommandMap().get(position);
			List<List<Integer>> buffs = config.getBuff();
			for (List<Integer> list : buffs) {
				if (list.size() < 2) {
					continue;
				}
				if (list.get(0) == techType) {
					return list.get(1) / DevideFactor.PERCENT_NUM;
				}
			}
		}
		return ret;
	}

	public int getBroodWarRank() {
		if (broodWar == null) {
			return 0;
		}
		return broodWar.getRank();
	}

	/**
	 * 取消行军
	 *
	 * @param march
	 */
	public void cannelMarch(March march) {
		if (march == null) {
			return;
		}
		CannelMarchAction action = new CannelMarchAction(march);
		 logicServer.addCommand(action, DealType.MAIN);
	}

	public class CannelMarchAction implements ICommand {

		private March march;

		public CannelMarchAction(March march) {
			this.march = march;
		}

		@Override
		public void action() {
			int mapId = worldManager.getMapId(march.getEndPos());
			MapInfo mapInfo = worldManager.getMapInfo(mapId);

			Entity entity = mapInfo.getEntity(march.getEndPos());
			if (entity instanceof BroodWar) {

				BroodWar broodWar = (BroodWar) entity;

				if (broodWar.getState() != BroodWarState.BEGIN_WAR) {
					return;
				}

				Iterator<Team> iterator = broodWar.getAttackQueue().iterator();
				while (iterator.hasNext()) {
					Team t = iterator.next();
					if (t == null || t.getLordId() != march.getLordId()) {
						continue;
					}
					if (t.getAllEnities() == null || t.getAllEnities().isEmpty() || march.getHeroIds() == null || march.getHeroIds().isEmpty()) {
						continue;
					}
					if (t.getAllEnities().get(0).getEntityId() == march.getHeroIds().get(0)) {
						iterator.remove();
						LogHelper.GAME_LOGGER.info("【母巢之战】 取消部队进攻 playerId:{} heroId:{} state:{} cityId:{}", march.getLordId(), march.getHeroIds().get(0), march.getState(), broodWar.getCityType());
						break;
					}
				}
			}
		}
	}
}
