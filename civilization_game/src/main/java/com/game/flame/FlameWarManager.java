package com.game.flame;

import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.StaticBroodWarBuff;
import com.game.domain.s.StaticWorldActPlan;
import com.game.domain.s.StaticWorldMap;
import com.game.flame.entity.*;
import com.game.log.domain.MailLog;
import com.game.manager.*;
import com.game.message.handler.DealType;
import com.game.pb.CommonPb;
import com.game.pb.FlameWarPb;
import com.game.pb.FlameWarPb.SynFlameBuildStateRq;
import com.game.pb.FlameWarPb.SynFlameEntityAddRq;
import com.game.pb.FlameWarPb.SynFlameEntityRq;
import com.game.pb.FlameWarPb.SynFlameWarStateRq;
import com.game.pb.FriendPb.SynApplyMsgRs;
import com.game.pb.WorldPb;
import com.game.season.SeasonManager;
import com.game.season.talent.entity.EffectType;
import com.game.server.GameServer;
import com.game.server.LogicServer;
import com.game.spring.SpringUtil;
import com.game.util.DateHelper;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.util.random.WeightRandom;
import com.game.worldmap.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Component
public class FlameWarManager {

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private WorldManager worldManager;
	@Autowired
	private StaticFlameWarMgr staticFlameWarMgr;
	@Autowired
	FlameWarService flameWarService;
	@Autowired
	StaticWorldActPlanMgr staticWorldActPlanMgr;
	@Autowired
	WarManager warManager;
	@Autowired
	StaticWorldMgr staticWorldMgr;
	@Autowired
	MarchManager marchManager;
	@Autowired
	WarBookManager warBookManager;
	@Autowired
	StaticLimitMgr staticLimitMgr;
	@Autowired
	TechManager techManager;
	@Autowired
	BigMonsterManager bigMonsterManager;
	@Autowired
	BroodWarManager broodWarManager;
	@Autowired
	ChatManager chatManager;
	@Autowired
	BattleMailManager battleMailMgr;
	@Autowired
	HeroManager heroManager;
	@Autowired
	MailManager mailManager;
	@Autowired
	SeasonManager seasonManager;
	final static int ENTER = 6;// 可进入状态
	final static int EXHIBITION = 7;// 展示状态
	private Map<Integer, BiConsumer<WorldActPlan, Long>> actions = new HashMap<>();
	private Map<Integer, BiConsumer<FlameWarCity, Long>> stateMap = new HashMap<>();
	@Getter
	private Map<Integer, Map<Long, FlamePlayer>> flameModelMap = new ConcurrentHashMap<>();
	@Getter
	private Map<Integer, FlameCountry> countryFlameModelMap = new ConcurrentHashMap<>();
	private Map<Integer, Integer> safeMap = new HashMap<>();
	// 排名相关
	private Map<Integer, List<FlamePlayer>> rankPlayer = new HashMap<>();
	private List<FlamePlayer> allRankPlayer = new ArrayList<>();
	private List<FlameCountry> rankCountry = new ArrayList<>();
	public static final ReadWriteLock lock = new ReentrantReadWriteLock();
	private long openTime;
	private long nextRankTime;

	public void initFlameWar(StaticWorldActPlan staticWorldActPlan) {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = new WorldActPlan();
		worldActPlan.setId(staticWorldActPlan.getId());
		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
		worldData.getWorldActPlans().put(worldActPlan.getId(), worldActPlan);
		flushAcPlan(worldActPlan);

	}

	public void checkActPlan(WorldActPlan worldActPlan) {
		if (actions.isEmpty()) {
			initActions();
			initMap();
		}
		if (actions.containsKey(worldActPlan.getState())) {
			actions.get(worldActPlan.getState()).accept(worldActPlan, TimeHelper.curentTime());
		}
		if (worldActPlan.getEnterTime() == 0) {
			StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(WorldActivityConsts.ACTIVITY_15);
			initFlameWar(staticWorldActPlan);
		}
		// LogHelper.GAME_LOGGER.info("世界活动{},预热时间{},可进入时间{},开始时间{},结束时间{},展示结束时间{},当前活动状态{}", "战火燎原", DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getEnterTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()), DateHelper.getDate(worldActPlan.getExhibitionTime()), worldActPlan.getState());
	}

	// 活动进程状态
	private void initActions() {
		actions.put(WorldActPlanConsts.NOE_OPEN, this::handlerNotOpen);
		actions.put(WorldActPlanConsts.PREHEAT, this::handlerPrepare);
		actions.put(ENTER, this::handlerEnter);
		actions.put(WorldActPlanConsts.OPEN, this::handlerOpen);
		actions.put(WorldActPlanConsts.DO_END, this::handlerDoEnd);
		actions.put(EXHIBITION, this::handlerExhibition);
	}

	// 建筑状态
	private void initStateMap() {
		stateMap.put(NodeState.NOT_OPEN, this::noOpenWar);
		stateMap.put(NodeState.ATTACK, this::attackWar);
		stateMap.put(NodeState.CAPTURE, this::captureWar);
	}

	private void handlerNotOpen(WorldActPlan worldActPlan, long currentTime) {
		if (worldActPlan.getPreheatTime() > currentTime) {
			return;
		}
		worldActPlan.setState(WorldActPlanConsts.PREHEAT);
		synFlameActivity(worldActPlan, worldActPlan.getOpenTime());
		LogHelper.GAME_LOGGER.info("进入预热阶段 state is{}", worldActPlan.getState());
	}

	private void handlerPrepare(WorldActPlan worldActPlan, long currentTime) {

		if (worldActPlan.getEnterTime() > currentTime) {
			return;
		}
		worldActPlan.setState(ENTER);
		// 初始化地图数据
		initMap();
		synFlameActivity(worldActPlan, worldActPlan.getOpenTime());
		LogHelper.GAME_LOGGER.info("进入可进入阶段 state is{}", worldActPlan.getState());
	}

	private void handlerEnter(WorldActPlan worldActPlan, long currentTime) {
		if (worldActPlan.getOpenTime() > currentTime) {
			return;
		}
		worldActPlan.setState(WorldActPlanConsts.OPEN);

		synFlameActivity(worldActPlan, worldActPlan.getEndTime());

		flush();

		sendBuffChange();
		LogHelper.GAME_LOGGER.info("进入开始阶段 state is{}", worldActPlan.getState());
		// 通知开始活动

	}

	private void handlerOpen(WorldActPlan worldActPlan, long currentTime) {
		if (worldActPlan.getEndTime() < currentTime) {
			worldActPlan.setState(WorldActPlanConsts.DO_END);
			handleActEnd();
			synFlameActivity(worldActPlan, worldActPlan.getEndTime());
			LogHelper.GAME_LOGGER.info("进入结算阶段 state is{}", worldActPlan.getState());
			return;
		}
		flameWarCheck(currentTime);
	}

	private void handlerDoEnd(WorldActPlan worldActPlan, long currentTime) {
		// 处理结算的事情
		worldActPlan.setState(EXHIBITION);
		synFlameActivity(worldActPlan, worldActPlan.getExhibitionTime());
		LogHelper.GAME_LOGGER.info("进入展示阶段 state is{}", worldActPlan.getState());
	}

	private void handlerExhibition(WorldActPlan worldActPlan, long currentTime) {
		// 结束展示
		if (currentTime < worldActPlan.getExhibitionTime()) {
			return;
		}
		worldActPlan.setState(WorldActPlanConsts.NOE_OPEN);
		synFlameActivity(worldActPlan, worldActPlan.getExhibitionTime());
		flushAcPlan(worldActPlan);
	}

	/**
	 * 刷矿
	 */
	public void flush() {
		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData == null) {
			return;
		}
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
		if (worldActPlan == null || worldActPlan.getState() != WorldActPlanConsts.OPEN) {
			return;
		}
		List<StaticFlameFlushMine> flameFlushMineMap = staticFlameWarMgr.getFlameFlushMineMap();
		LogicServer mainLogicServer = GameServer.getInstance().mainLogicServer;
		// 定时刷矿
		flameFlushMineMap.forEach(x -> {
			long l = x.getRefreshTime() - TimeHelper.curentTime() + worldActPlan.getOpenTime();
			if (l > 0) {
				mainLogicServer.scheduledExecutorService.schedule(() -> {
					flushMine(x);
				}, l, TimeUnit.MILLISECONDS);
			}
		});
	}

	/**
	 * 活动时间
	 *
	 * @param worldActPlan
	 */
	private void flushAcPlan(WorldActPlan worldActPlan) {
		StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(WorldActivityConsts.ACTIVITY_15);
		int openWeek = 1;
		LocalDateTime now = LocalDateTime.now();
		int day = now.getDayOfWeek().getValue();
		int hour = now.getHour();
		// 如果当前时间在星期2得8点之前
		if (day < 2 || (day == 2 && hour <= 20)) {
			openWeek = 0;
		}
		worldActPlan.setTargetSuccessTime(System.currentTimeMillis());
		long openTime = TimeHelper.getTime(worldActPlan.getTargetSuccessTime(), openWeek, staticWorldActPlan.getWeekTime(), staticWorldActPlan.getTime());
		long preheat = TimeHelper.getTime(new Date(openTime), staticWorldActPlan.getPreheat());
		long endTime = openTime + staticWorldActPlan.getEndTime() * 60 * 1000L;
		long enterTime = TimeHelper.getTimeMinute(new Date(openTime), -5);
		long exhibitionTime = TimeHelper.getTimeMinute(new Date(endTime), 180);
		worldActPlan.setOpenTime(openTime);
		worldActPlan.setPreheatTime(preheat);
		worldActPlan.setEndTime(endTime);
		worldActPlan.setEnterTime(enterTime);// 可进入时间 5分钟
		worldActPlan.setExhibitionTime(exhibitionTime);// 展示阶段
		this.openTime = openTime;
		LogHelper.GAME_LOGGER.info("世界活动{},预热时间{},可进入时间{},开始时间{},结束时间{},展示结束时间{}", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getEnterTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()), DateHelper.getDate(worldActPlan.getExhibitionTime()));
	}

	public void initMap() {
		StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(WorldActivityConsts.ACTIVITY_15);
		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData == null) {
			return;
		}
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
		if (worldActPlan == null) {
			return;
		}
		LogHelper.GAME_LOGGER.info("世界活动{},预热时间{},可进入时间{},开始时间{},结束时间{},展示结束时间{}", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getEnterTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()), DateHelper.getDate(worldActPlan.getExhibitionTime()));
		LogHelper.GAME_LOGGER.info("初始化地图坐标");
		this.openTime = worldActPlan.getOpenTime();
		FlameMap flameMap = (FlameMap) worldManager.getMapInfo(MapId.FIRE_MAP);
		flameMap.clear();
		// 随机初始化安全区域
		List<StaticFlameSafe> flameSafe = staticFlameWarMgr.getFlameSafe();
		Collections.shuffle(flameSafe);
		for (int i = 0; i < flameSafe.size(); i++) {
			StaticFlameSafe staticFlameSafe = flameSafe.get(i);
			int x_1 = staticFlameSafe.getX1();
			int x_2 = staticFlameSafe.getX2();
			int y_1 = staticFlameSafe.getY1();
			int y_2 = staticFlameSafe.getY2();
			List<Pos> pos1 = flameMap.getSafePos().computeIfAbsent(i + 1, z -> new ArrayList<>());
			for (int a = x_1; a <= x_2; a++) {
				for (int b = y_1; b <= y_2; b++) {
					Pos pos = new Pos(a, b);
					pos1.add(pos);
				}
			}
			safeMap.put(staticFlameSafe.getId(), i + 1);// id->country//记录这期活动 阵营安全区对应的坐标区域
		}
		// 初始化地图坐标
		StaticWorldMap staticFlameMap = staticWorldMgr.getStaticWorldMap(MapId.FIRE_MAP);

		int x1 = staticFlameMap.getX1();
		int x2 = staticFlameMap.getX2();
		int y1 = staticFlameMap.getY1();
		int y2 = staticFlameMap.getY2();
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				Pos pos = new Pos(x, y);
				boolean flag = true;
				for (List<Pos> posList : flameMap.getSafePos().values()) {
					if (posList.contains(pos)) {
						flag = false;
						break;
					}
				}
				if (flag) {
					flameMap.getAllPos().add(pos);
				}
			}
		}
		// 初始化建筑
		Map<Integer, StaticFlameBuild> flameBuildMap = staticFlameWarMgr.getFlameBuildMap();
		flameBuildMap.values().forEach(x -> {
			FlameWarCity flameWarCity = new FlameWarCity();
			flameWarCity.setId(x.getId());
			Pos pos = new Pos(x.getX(), x.getY());
			flameWarCity.setPos(pos);
			flameWarCity.setState(NodeState.NOT_OPEN);
			flameWarCity.setUpdateStateTime(x.getProtectTime() + openTime);
			flameWarCity.setLevel(x.getLevel());
			flameMap.addNode(flameWarCity);
			if (x.getType() == 5) {
				flameWarCity.setCountry(safeMap.get(x.getSafeId()));
			}
			// 去掉大地块中的 建筑坐标
			int x11 = x.getX1();
			int x21 = x.getX2();
			int y11 = x.getY1();
			int y21 = x.getY2();
			for (int a = x11; a <= x21; a++) {
				for (int z = y11; z <= y21; z++) {
					Pos pos1 = new Pos(a, z);
					flameMap.getAllPos().remove(pos1);
				}
			}
		});
		rankPlayer.clear();
		allRankPlayer.clear();
		rankCountry.clear();
		countryFlameModelMap.clear();
		flameModelMap.clear();
		// TODO:测试容错处理
		Map<Long, Player> allPlayer = playerManager.getAllPlayer();
		for (Player player : allPlayer.values()) {
			Pos pos = player.getPos();
			if (pos.isError()) {
				continue;
			}
			int mapId = worldManager.getMapId(pos);
			if (mapId == MapId.FIRE_MAP) {
				ConcurrentLinkedDeque<March> marchList = player.getMarchList();
				marchList.forEach(x -> {
					handleMarchReturn(x, Reason.FLAME);
					worldManager.synMarch(flameMap.getMapId(), x);
				});
				MapInfo newMapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
				Pos randPos = worldManager.givePlayerPos(newMapInfo);
				playerManager.changePlayerPos(player, randPos);
				PlayerCity playerCity = worldManager.addPlayerCity(randPos, newMapInfo, player);

			}
		}
		flameMap.getMarches().clear();

	}

	public void flameWarCheck(long currentTime) {
		FlameMap flameMap = getFlameMap();
		Map<Long, FlameWarCity> cityNode = flameMap.getCityNode();
		cityNode.values().forEach(x -> {
			StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(x.getId());
			if (staticFlameBuild != null && staticFlameBuild.getType() != 5) {
				flameWarService.fight(x, currentTime);
				flameWarState(x, currentTime);
			}
		});
		Map<Pos, FlameWarResource> resourceNode = flameMap.getResourceNode();
		Iterator<FlameWarResource> iterator = resourceNode.values().iterator();
		while (iterator.hasNext()) {
			FlameWarResource next = iterator.next();
			boolean b = calResource(next, currentTime);
			if (!b) {
				iterator.remove();
				flameMap.getNode().remove(next.getPos());
			}
		}
		GameServer.getInstance().mainLogicServer.addCommand(() -> {
			flameWarService.synCountryInfo();
			if (currentTime > nextRankTime) {
				nextRankTime = currentTime + 60 * 1000;
				flushRank();
			}
		});
	}

	public void flameWarState(FlameWarCity node, long currentTime) {
		if (stateMap.isEmpty()) {
			initStateMap();
		}
		BiConsumer<FlameWarCity, Long> command = stateMap.get(node.getState());
		if (command == null) {
			return;
		}
		command.accept(node, currentTime);
	}

	public void noOpenWar(FlameWarCity node, long currentTime) {
		if (currentTime > node.getUpdateStateTime()) {
			node.setState(NodeState.CENTER);
			// 推送建筑状态
			sendChat(node, 1);
			synFlameBuild(node);
		}
	}

	public void sendChat(FlameWarCity flameWarCity, int type) {
		StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(flameWarCity.getId());
		if (staticFlameBuild != null) {
			Pos pos = flameWarCity.getPos();
			String tarPos = String.format("%s,%s", pos.getX(), pos.getY());
			String s = String.valueOf(flameWarCity.getId());
			if (type == 1) {
				if (staticFlameBuild.getProtectChat() != 0) {
					String p[] = {s, tarPos};
					chatManager.sendWorldChat(staticFlameBuild.getProtectChat(), p);
				}
			} else {
				if (staticFlameBuild.getOccupyChat() != 0) {
					String p[] = {s, tarPos, String.valueOf(flameWarCity.getCountry())};
					chatManager.sendWorldChat(ChatId.FLAME_OCCUPY, p);
				}
			}
		}
	}

	// 攻占状态
	public void attackWar(FlameWarCity node, long currentTime) {
		if (currentTime >= node.getCapTime()) {
			node.setState(NodeState.CAPTURE);

			StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(node.getId());
			if (staticFlameBuild != null && node.getCountry() != 0) {
				// 结算个人 资源时间
				LinkedList<March> defenceQueue = node.getDefenceQueue();
				defenceQueue.forEach(x -> {
					if (x.getState() == MarchState.Waiting) {
						x.setNextCalTime(currentTime + staticFlameBuild.getContinuePersonTime());
					}
				});
				// 结算国家资源时间
				node.setNextCalCountryTime(currentTime + staticFlameBuild.getContinueCampTime());
				// 处理首次占领得问题
				if (node.getFirstCountry() == 0 && node.getCountry() != 0) {
					node.setFirstCountry(node.getCountry());

					HashSet<Long> first = node.getFirst();
					defenceQueue.forEach(x -> {
						if (x.getState() == MarchState.Waiting) {
							x.setNextCalTime(currentTime + staticFlameBuild.getContinuePersonTime());
						}
						first.add(x.getLordId());
					});
					// 给阵营 首占奖励
					FlameCountry flameCountry = countryFlameModelMap.computeIfAbsent(node.getCountry(), x -> new FlameCountry());
					flameCountry.addFirstResource(staticFlameBuild.getFirstCamp());
					flameCountry.addResource(staticFlameBuild.getFirstCamp());
					// 给个人
					first.forEach(x -> {
						Player player = playerManager.getPlayer(x);
						if (player != null) {

							FlamePlayer flamePlayer = getFlamePlayer(player);
							flamePlayer.addFirstResource(staticFlameBuild.getFirstPerson());
							flamePlayer.addResource(staticFlameBuild.getFirstPerson());
							// 战报1
							FlameWarPb.FlameRealOptInfo.Builder builder = FlameWarPb.FlameRealOptInfo.newBuilder();
							builder.setType(ReportType.REPORT_1);
							builder.setCityId((int) node.getId());
							builder.setFirstRes(staticFlameBuild.getFirstPerson());
							builder.setEndTime(currentTime);
							for (March march : defenceQueue) {
								if (march.getState() == MarchState.Waiting && march.getLordId() == flamePlayer.getRoleId()) {
									builder.setHeroId(march.getHeroIds().get(0));
									break;
								}
							}
							flamePlayer.addReport(builder.build());
						}
					});
				}
				// 占领后多长时间产出物质
				node.setCapTime(currentTime + staticFlameBuild.getProductionTime());
				node.setOccupy(currentTime);// 该阵营 完全占领的时刻
			}
			synFlameBuild(node);
			sendBuffChange();
			sendChat(node, 2);
		}
	}

	// 占领状态
	public void captureWar(FlameWarCity node, long currentTime) {
		StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(node.getId());
		if (staticFlameBuild == null) {
			return;
		}
		if (node.getCountry() != 0) {
			if (currentTime >= node.getCapTime()) {
				// 推送该阵营可领取奖励
				List<List<Integer>> award = staticFlameBuild.getAward();
				if (award != null && !award.isEmpty()) {
					List<Integer> temp = new ArrayList<>();
					for (List<Integer> weight : award) {
						temp.add(weight.get(3));
					}
					int index = WeightRandom.initData(temp);
					List<Integer> list = award.get(index);
					node.setAward(PbHelper.createAward(list.get(0), list.get(1), list.get(2)).build());
					node.setPlayerAwardList(new ArrayList<>());
					synFlameBuild(node);
					node.setCapTime(currentTime + staticFlameBuild.getProductionTime());// 此次产出后，持续占领 下次产生物质得时间
				}
			}
			// 这里给 占领得 个人结算
			HashSet<Long> set = new HashSet<>();
			LinkedList<March> defenceQueue = node.getDefenceQueue();
			defenceQueue.forEach(x -> {
				if (x.getState() == MarchState.Waiting) {
					if (x.getNextCalTime() != 0 && !set.contains(x.getLordId())) {
						if (currentTime >= x.getNextCalTime()) {
							Player player = playerManager.getPlayer(x.getLordId());

							FlamePlayer flamePlayer = getFlamePlayer(player);
							flamePlayer.addResource(staticFlameBuild.getContinuePersonAmount());
							set.add(x.getLordId());
							x.setNextCalTime(currentTime + staticFlameBuild.getContinuePersonTime());

							// 战报4
							FlameWarPb.FlameRealOptInfo.Builder builder = FlameWarPb.FlameRealOptInfo.newBuilder();
							builder.setType(ReportType.REPORT_4);
							builder.setCityId((int) node.getId());
							builder.setPerRes(staticFlameBuild.getContinuePersonAmount());
							builder.setEndTime(currentTime);
							builder.setHeroId(x.getHeroIds().get(0));
							flamePlayer.addReport(builder.build());
						}
					} else {
						x.setNextCalTime(currentTime + staticFlameBuild.getContinuePersonTime());
					}
				}
			});
			// 这里给 占领得 国家结算
			if (node.getNextCalCountryTime() <= currentTime) {
				FlameCountry flameCountry = countryFlameModelMap.computeIfAbsent(node.getCountry(), x -> new FlameCountry());
				flameCountry.setCountry(node.getCountry());
				flameCountry.addResource(staticFlameBuild.getContinueCampAmount());
				node.setNextCalCountryTime(currentTime + staticFlameBuild.getContinueCampTime());
			}
		}
	}

	public void synFlameBuild(FlameWarCity node) {
		FlameWarPb.SynFlameBuildStateRq.Builder builder = FlameWarPb.SynFlameBuildStateRq.newBuilder();
		CommonPb.WorldEntity.Builder builder1 = node.wrapPb();
		builder1.setAllOwnTime(node.getOccupy());
		builder1.setProtectedTime(node.getUpdateStateTime());
		FlameMap flameMap = getFlameMap();
		Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
		playerCityMap.values().forEach(x -> {
			Player player = x.getPlayer();
			builder1.setIsCanJoinAttack(0);
			if (node.getCountry() == player.getCountry()) {
				if (!node.getAttackQueue().isEmpty() || !node.getDefenceQueue().isEmpty()) {
					builder1.setIsCanJoinAttack(1);
				}
			} else {
				March march = node.getAttackQueue().stream().filter(z -> z.getCountry() == player.getCountry()).findAny().orElse(null);
				if (march != null) {
					builder1.setIsCanJoinAttack(2);
				}
			}
			builder1.setReceive(2);
			if (node.getCountry() == player.getCountry() && node.getAward() != null && !node.getPlayerAwardList().contains(player.getRoleId())) {
				builder1.setReceive(1);
			}
			builder.setEntity(builder1);

			SynHelper.synMsgToPlayer(player, SynFlameBuildStateRq.EXT_FIELD_NUMBER, SynFlameBuildStateRq.ext, builder.build());
		});
	}

	public void synFlameActivity(WorldActPlan worldActPlan, long endTime) {

		GameServer.getInstance().mainLogicServer.addCommand(() -> {
			FlameWarPb.SynFlameWarStateRq.Builder builder = FlameWarPb.SynFlameWarStateRq.newBuilder();
			builder.setStatus(worldActPlan.getState());
			builder.setEndTime(endTime);
			playerManager.getOnlinePlayer().forEach(player -> {
				builder.setPos(player.getPos().wrapPb());
				SynHelper.synMsgToPlayer(player, SynFlameWarStateRq.EXT_FIELD_NUMBER, SynFlameWarStateRq.ext, builder.build());
			});
		});
	}

	/**
	 * 活动结束处理
	 */
	private void handleActEnd() {
		FlameMap flameMap = getFlameMap();
		// 所有兵线全部返回
		ConcurrentLinkedDeque<March> marches = flameMap.getMarches();
		Iterator<March> iterator = marches.iterator();
		while (iterator.hasNext()) {
			March next = iterator.next();
			handleMarchReturn(next, Reason.FLAME);
			worldManager.synMarch(flameMap.getMapId(), next);
			if (next.getState() == MarchState.Collect) {
			}
		}
		Map<Long, FlameWarCity> cityNode = flameMap.getCityNode();
		cityNode.values().forEach(node -> {
			if (node.getCountry() > 0) {
				StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(node.getId());
				FlameCountry flameCountry = countryFlameModelMap.computeIfAbsent(node.getCountry(), x -> new FlameCountry());
				flameCountry.addExtra(staticFlameBuild.getExtraCamp());// 增加额外奖励
				flameCountry.addResource(staticFlameBuild.getExtraCamp());
				flameCountry.getCount().merge(staticFlameBuild.getLevel(), 1, (a, b) -> a + b);// 统计等级数量
				node.clear();
			}
		});
		// 加上额外之后重新排序
		flushRank();

		MapInfo newMapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		Iterator<Map.Entry<Integer, Map<Long, FlamePlayer>>> iterator2 = flameModelMap.entrySet().iterator();
		while (iterator2.hasNext()) {
			Map.Entry<Integer, Map<Long, FlamePlayer>> integerMapEntry = iterator2.next();
			Integer country = integerMapEntry.getKey();
			Map<Long, FlamePlayer> next = integerMapEntry.getValue();
			FlameCountry flameCountry = countryFlameModelMap.computeIfAbsent(country, x -> new FlameCountry());
			flameCountry.setCountry(country);
			int b1 = flameCountry.getCount().getOrDefault(1, 0);// 据点等级数量
			int b2 = flameCountry.getCount().getOrDefault(2, 0);
			int b3 = flameCountry.getCount().getOrDefault(3, 0);
			for (FlamePlayer value : next.values()) {
				Player player = playerManager.getPlayer(value.getRoleId());
				PlayerCity playerCity = flameMap.getPlayerCity(player.getPos());
				// 这里做迁城
				if (playerCity != null) {
					flameWarService.synFlameEntity(null, player.getPos());
					Pos randPos = worldManager.givePlayerPos(newMapInfo);
					if (randPos.isError() || !newMapInfo.isFreePos(randPos)) {
						return;
					}
					// 显示在大地图块
					playerManager.changePlayerPos(player, randPos);
					PlayerCity playerCity1 = worldManager.addPlayerCity(randPos, newMapInfo, player);
					if (playerCity1 != null) {
						worldManager.synEntityRq(playerCity1, newMapInfo.getMapId(), player.getOldPos()); // 同步城池
						heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
					}
				}
				long pRes = value.getResource();// 个人资源
				long kill = value.getKill();// 累计杀敌
				long pEx = staticFlameWarMgr.getStaticFlameKill(kill);// 个人额外
				long totalP = pRes + pEx;
				value.addResource(pEx);// 个人加上额外得
				long pCp = 0;// 个人钞票
				StaticFlameRankGear flameRankGears = staticFlameWarMgr.getFlameRankGears(totalP);
				if (flameRankGears != null) {
					pCp = flameRankGears.getAward();
				}
				int minRes = staticLimitMgr.getNum(SimpleId.FIRE_MAX_RESOURCE);
				int rank = rankCountry.indexOf(flameCountry) + 1;// 排名
				int cScore = 0;// 钞票
				StaticFlameRankCamp staticFlameRankCamp = staticFlameWarMgr.getStaticFlameRankCamp(rank);
				if (staticFlameRankCamp != null) {
					cScore = staticFlameRankCamp.getAward();
				}
				int mailId = MailId.FLAME_MAIL_155;
				List<Award> awards = new ArrayList<>();
				long l;
				long resource = flameCountry.getResource() - flameCountry.getExtra();
				long exRes = flameCountry.getExtra();
				long totalRes = flameCountry.getResource();
				if (totalP > minRes) {
					mailId = MailId.FLAME_MAIL_154;
					l = pCp + cScore;
					Award award = new Award();
					award.setType(AwardType.FLAME_SCORE);
					double seasonBuf = seasonManager.getSeasonBuf(player, EffectType.EFFECT_TYPE21);
					long v = (long) (1 + seasonBuf) * l;
					award.setCount((int) v);
					awards.add(award);
					playerManager.sendAttachMail(player, awards, mailId, minRes + "", l + "", value.getCountry() + "", resource + "", b1 + "", b2 + "", b3 + "", exRes + "", totalRes + "", rank + "", cScore + "", pRes + "", kill + "", pEx + "", totalP + "", pCp + "");
				} else {
					playerManager.sendAttachMail(player, awards, mailId, minRes + "", value.getCountry() + "", resource + "", b1 + "", b2 + "", b3 + "", exRes + "", totalRes + "", rank + "", pRes + "", kill + "", pEx + "", totalP + "");
				}
			}
		}
		Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
		playerCityMap.clear();
	}

	public void handleMarchReturn(March march, int reason) {
		// 回城
		march.setState(MarchState.FightOver);
		// 开始掉头
		march.swapPos(reason);
		march.setPeriod(1000);
		march.setEndTime(System.currentTimeMillis() + 1000);
	}

	/**
	 * 矿点结算
	 *
	 * @param next
	 * @param currentTime
	 */
	private boolean calResource(FlameWarResource next, long currentTime) {
		ConcurrentLinkedDeque<FlameGuard> collectArmy = next.getCollectArmy();
		StaticFlameMine staticFlameMine = staticFlameWarMgr.getStaticFlameMine(next.getResId());
		if (staticFlameMine == null) {
			return false;
		}
		FlameMap flameMap = getFlameMap();
		collectArmy.forEach(x -> {
			if (currentTime > x.getNextCalTime()) {
				March march = x.getMarch();
				Player player = playerManager.getPlayer(march.getLordId());

				FlamePlayer flame = getFlamePlayer(player);

				flame.addResource(staticFlameMine.getAmount());
				x.setNextCalTime(currentTime + staticFlameMine.getCollectTime());
				x.addTotalRes(staticFlameMine.getAmount());
				next.addConvertRes(staticFlameMine.getAmount());
			}
		});
		if (next.getConvertRes() >= next.getTotal()) {
			ConcurrentLinkedDeque<FlameGuard> collectArmy1 = next.getCollectArmy();
			collectArmy1.forEach(x -> {
				marchManager.handleMarchReturn(x.getMarch(), MarchReason.CollectDone);
				worldManager.synMarch(flameMap.getMapId(), x.getMarch());
				March march = x.getMarch();
				List<Integer> heroIds = march.getHeroIds();
				Player player = x.getPlayer();
				Hero hero = player.getHero(heroIds.get(0));
				// sendCollectDone(MailId.COLLECT_WIN, next, currentTime - x.getStartTime(), x.getTotalRes(), hero.getHeroId(), hero.getHeroLv(), player, false, null);
				battleMailMgr.sendCollectDone(MailId.COLLECT_WIN, next, currentTime - x.getStartTime(), x.getTotalRes(), hero.getHeroId(), hero.getHeroLv(), player, false, null);
			});
			MapInfo mapInfo = worldManager.getMapInfo(MapId.FIRE_MAP);
			mapInfo.getPlayerCityMap().values().forEach(x -> {
				Player player = x.getPlayer();
				FlameWarPb.SynFlameEntityRq.Builder builder = FlameWarPb.SynFlameEntityRq.newBuilder();
				builder.setOldPos(next.getPos().wrapPb());
				SynHelper.synMsgToPlayer(player, SynFlameEntityRq.EXT_FIELD_NUMBER, SynFlameEntityRq.ext, builder.build());
			});
			return false;
		}
		return true;
	}

	public void flushMine(StaticFlameFlushMine mine) {
		StaticFlameMine staticFlameMine = staticFlameWarMgr.getStaticFlameMine(mine.getTypeId());
		if (staticFlameMine == null) {
			return;
		}
		FlameMap flameMap = getFlameMap();
		int flushNum = mine.getLimit();
		for (int i = 0; i < flushNum; i++) {
			Pos pos = flameMap.getPos(0);
			if (pos != null) {
				FlameWarResource node = new FlameWarResource();
				node.setResId(staticFlameMine.getId());
				node.setPos(pos);
				node.setTotal(staticFlameMine.getResource());
				node.setLevel(staticFlameMine.getLv());
				flameMap.addNode(node);
				FlameWarPb.SynFlameEntityAddRq.Builder builder = FlameWarPb.SynFlameEntityAddRq.newBuilder();
				builder.addEntity(node.wrapPb());
				flameMap.getPlayerCityMap().values().forEach(x -> {
					Player player = x.getPlayer();
					SynHelper.synMsgToPlayer(player, SynFlameEntityAddRq.EXT_FIELD_NUMBER, SynFlameEntityAddRq.ext, builder.build());
//					//player.sendMsgToPlayer(FlameWarPb.SynFlameEntityAddRq.ext, builder.build(), FlameWarPb.SynFlameEntityAddRq.EXT_FIELD_NUMBER);
				});
			}
		}
	}

	public byte[] flushDb() {
		FlameWarPb.FlameWarDb.Builder builder = FlameWarPb.FlameWarDb.newBuilder();
		// WorldData worldData = worldManager.getWolrdInfo();
		// if (worldData == null) {
		// return builder.build().toByteArray();
		// }
		// WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
		// if (worldActPlan == null || worldActPlan.getState() == WorldActPlanConsts.NOE_OPEN) {
		// return builder.build().toByteArray();
		// }
		// Map<Long, FlameWarCity> cityNode = flameMap.getCityNode();
		// cityNode.values().forEach(x -> {
		// builder.addFlameWarCity(x.encode());
		// });
		// Map<Long, FlamePlayerCity> playerCityMap = flameMap.getPlayerCityMap();
		// playerCityMap.values().forEach(x -> {
		// builder.addFlamePlayerCity(x.getId());
		// });
		// countryFlameModelMap.values().forEach(x -> {
		// builder.addCountryFlame(x.encode());
		// });
		// flameModelMap.values().forEach(x -> {
		// x.values().forEach(y -> {
		// builder.addPlayerFlame(y.encode());
		// });
		// });
		// Map<Integer, March> marches = flameMap.getMarches();
		// marches.values().forEach(x -> {
		// builder.addMarch(x.writeMarch());
		// });
		// Map<Pos, FlameWarResource> resourceNode = flameMap.getResourceNode();
		// resourceNode.values().forEach(x -> {
		// builder.addFlameWarResource(x.encode());
		// });
		// Map<Long, WarInfo> war = flameMap.getWar();
		// war.values().forEach(x -> {
		// builder.addWarInfo(x.writeData());
		// });
		return builder.build().toByteArray();
	}

	public void decode(byte[] bytes) {
		// try {
		// initMap();
		// FlameWarPb.FlameWarDb flameWarDb = FlameWarPb.FlameWarDb.parseFrom(bytes);
		// // 初始化兵线
		// List<DataPb.MarchData> marchList = flameWarDb.getMarchList();
		// marchList.forEach(x -> {
		// March march = new March();
		// march.readMarch(x);
		// flameMap.addMarch(march);
		// });
		//
		// // 初始化pvp战斗
		// List<DataPb.WarData> warInfoList = flameWarDb.getWarInfoList();
		// Map<Integer, March> marches = flameMap.getMarches();
		// warInfoList.forEach(x -> {
		// WarInfo warInfo = new WarInfo(x, marches);
		// flameMap.addWar(warInfo);
		// });
		// // 初始化建筑数据
		// List<FlameWarPb.FlameWarCity> flameWarCityList = flameWarDb.getFlameWarCityList();
		// flameWarCityList.forEach(x -> {
		// FlameWarCity flameWarCity = flameMap.getCityNode().get(x.getId());
		// if (flameWarCity != null) {
		// flameWarCity.decode(x, marches);
		// }
		// });
		// // 初始化玩家城池
		// List<Long> flamePlayerCityList = flameWarDb.getFlamePlayerCityList();
		// flamePlayerCityList.forEach(x -> {
		// Player player = playerManager.getPlayer(x);
		// if (player != null) {
		// player.getLord().setMapId(100);
		// FlamePlayerCity flamePlayerCity = new FlamePlayerCity(player);
		// flameMap.addNode(flamePlayerCity);
		// }
		// });
		// // 初始化资源
		// List<FlameWarPb.FlameWarResource> flameWarResourceList = flameWarDb.getFlameWarResourceList();
		// flameWarResourceList.forEach(x -> {
		// FlameWarResource resource = new FlameWarResource(x, marches);
		// flameMap.addNode(resource);
		// });
		// // 初始化国家
		// List<FlameWarPb.CountryFlame> countryFlameList = flameWarDb.getCountryFlameList();
		// countryFlameList.forEach(x -> {
		// this.countryFlameModelMap.put(x.getCountry(), new FlameCountry(x));
		// });
		// // 初始化个人信息
		// List<FlameWarPb.PlayerFlame> playerFlameList = flameWarDb.getPlayerFlameList();
		// playerFlameList.forEach(x -> {
		// FlamePlayer flame = new FlamePlayer(x);
		// Map<Long, FlamePlayer> longPlayerFlameMap = this.flameModelMap.computeIfAbsent(flame.getCountry(), y -> new ConcurrentHashMap<>());
		// longPlayerFlameMap.put(flame.getRoleId(), flame);
		// });
		//
		// WorldData worldData = worldManager.getWolrdInfo();
		// if (worldData == null) {
		// return;
		// }
		// WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
		// StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(WorldActivityConsts.ACTIVITY_15);
		// LogHelper.GAME_LOGGER.info("世界活动{},预热时间{},可进入时间{},开始时间{},结束时间{},展示结束时间{}", staticWorldActPlan.getName(), DateHelper.getDate(worldActPlan.getPreheatTime()), DateHelper.getDate(worldActPlan.getEnterTime()), DateHelper.getDate(worldActPlan.getOpenTime()), DateHelper.getDate(worldActPlan.getEndTime()), DateHelper.getDate(worldActPlan.getExhibitionTime()));
		//
		// } catch (Exception e) {
		//
		// }
	}

	public FlameMap getFlameMap() {
		return (FlameMap) worldManager.getMapInfo(MapId.FIRE_MAP);
	}

	// 计算战力时
	public float addFlameProperty(Player player, int buffType) {
		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData == null) {
			return 0;
		}
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
		if (worldActPlan == null || (worldActPlan.getState() != WorldActPlanConsts.OPEN && worldActPlan.getState() != FlameWarManager.ENTER)) {
			return 0;
		}
		int add = 0;
		FlamePlayer flame = getFlamePlayer(player);
		if (flame != null) {
			// 个人购买的buff
			FlameMap flameMap = getFlameMap();
			Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
			PlayerCity playerCity = playerCityMap.get(player.getPos());
			if (playerCity == null || playerCity.getPlayer() != player) {
				return 0;
			}
			Map<Integer, Buff> buff = flame.getBuff();
			Collection<Buff> values = buff.values();
			for (Buff x : values) {
				StaticFlameBuff buffById = staticFlameWarMgr.getBuffById(x.getBuffId());
				if (buffById != null) {
					List<List<Integer>> effect = buffById.getEffect();
					if (effect != null) {
						for (List<Integer> list : effect) {
							if (list.get(0) == buffType) {
								add += list.get(1);
								// property.addValue(list.get(0),);
							}
						}
					}
				}
			}

			List<FlameWarCity> collect = getCaptureBuild(flame.getCountry());
			// 建筑加成的buff
			for (FlameWarCity flameWarCity : collect) {
				StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(flameWarCity.getId());
				List<List<Integer>> buff1 = staticFlameBuild.getBuff();
				if (buff1 != null) {
					for (List<Integer> list : buff1) {
						if (list.get(0) == buffType) {
							add += list.get(1);
						}
					}
				}
			}
		}
		if (buffType == BuffType.buff_1) {
			double seasonBuf = seasonManager.getSeasonBuf(player, EffectType.EFFECT_TYPE17);
			//float v = (float) buf / 100;
			double v1 = 1 + seasonBuf;
			add *= v1;
		}
		return (float) add / 100;
	}

	public List<FlameWarCity> getCaptureBuild(int country) {
		FlameMap flameMap = getFlameMap();
		Map<Long, FlameWarCity> cityNode = flameMap.getCityNode();
		List<FlameWarCity> collect = cityNode.values().stream().filter(x -> x.getCountry() == country && x.getState() == NodeState.CAPTURE).collect(Collectors.toList());
		return collect;
	}

	public March createFlameWarMarch(Player player, List<Integer> heroId, Pos targetPos) {
		March march = new March();
		march.setKeyId(marchManager.getMarchKey());
		march.setLordId(player.roleId);
		march.setHeroIds(heroId);
		// List<Integer> marchHero = march.getHeroIds();
		// marchHero.add(heroId);
		march.setState(MarchState.Begin);
		march.setEndPos(new Pos(targetPos.getX(), targetPos.getY()));
		Lord lord = player.getLord();
		Pos playerPos = new Pos(lord.getPosX(), lord.getPosY());
		march.setStartPos(playerPos);
		// 兵书对行军的影响
		float bookEffectMarch = warBookManager.getBookEffectMarch(player, Lists.newArrayList(heroId));
		long period = worldManager.getPeriod(player, playerPos, targetPos, bookEffectMarch);

		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
		// march.setEndTime(System.currentTimeMillis() + 1000);
		march.setCountry(player.getCountry());
		march.setMarchType(MarchType.FLAME_WAR);
		return march;
	}

	public long getPeriod(Player player, Pos pos1, Pos pos2, float bookEffectMarch) {
		int distance = worldManager.distance(pos1, pos2);
		float configNum = staticLimitMgr.getNum(10) * 1.0f / 1000;
		double spAdd = techManager.getArmySp(player);
		float vipFactor = playerManager.getMarchBuff(player);
		float propAdd = playerManager.getBuffAdd(player, BuffId.MARCH_SPEED);

		// 巨型虫族buff
		int mapId = worldManager.getMapId(player.getPos());
		float bigMonsterSpeed = bigMonsterManager.getMonsterBuff(mapId, player) / 100F;
		// 母巢行军BUFF
		//BroodWarInfo broodWarInfo = player.getBroodWarInfo();
		float broodSpeed = broodWarManager.getSpeed(player);
		double commandBuff = broodWarManager.getCommandBuff(player, TechEffectId.ADD_MARCH_SPEED);
		double cityBuf = worldManager.getCityBuf(player, CityBuffType.MARCH);
		double buff = flameWarService.getBuff(BuffType.buff_5, player.getCountry(), player);
		long period = (long) Math.ceil(distance / (configNum * 1.0f * ((float) 1 / 3 + spAdd)) * (1 - vipFactor) * (1 - propAdd) * (1 - bookEffectMarch) / (1 + bigMonsterSpeed) * (1 - broodSpeed) * (1 - commandBuff) * (1 - cityBuf) * (1 - buff));
		return period * TimeHelper.SECOND_MS;
	}

	public FlamePlayer getFlamePlayer(Player player) {
		Map<Long, FlamePlayer> longPlayerFlameMap = flameModelMap.computeIfAbsent(player.getCountry(), y -> new HashMap<>());
		FlamePlayer flamePlayer = longPlayerFlameMap.computeIfAbsent(player.getRoleId(), z -> new FlamePlayer(player));
		return flamePlayer;
	}

	public List<FlamePlayer> getRankFlamePlayer(int country) {
		return rankPlayer.get(country);
	}

	public List<FlameCountry> getRankCountry() {
		return rankCountry;
	}

	public void setRankCountry(List<FlameCountry> rankCountry) {
		this.rankCountry = rankCountry;
	}

	public List<FlamePlayer> getAllRankPlayer() {
		return allRankPlayer;
	}

	public void setAllRankPlayer(List<FlamePlayer> allRankPlayer) {
		this.allRankPlayer = allRankPlayer;
	}

	public Map<Integer, List<FlameWarCity>> getCountryLevel(int country) {
		FlameMap flameMap = getFlameMap();
		Map<Long, FlameWarCity> cityNode = flameMap.getCityNode();
		Map<Integer, List<FlameWarCity>> collect = cityNode.values().stream().filter(x -> x.getCountry() == country && staticFlameWarMgr.getStaticFlameBuild(x.getId()) != null && staticFlameWarMgr.getStaticFlameBuild(x.getId()).getType() != 5).collect(Collectors.groupingBy(FlameWarCity::getLevel));
		// levelRank.put(country, collect);
		return collect;
	}

	// 排行
	public void flushRank() {
		if (lock.writeLock().tryLock()) {
			try {
				Iterator<Map.Entry<Integer, Map<Long, FlamePlayer>>> iterator = flameModelMap.entrySet().iterator();
				List<FlamePlayer> allRankList = new ArrayList<>();
				while (iterator.hasNext()) {
					Map.Entry<Integer, Map<Long, FlamePlayer>> next = iterator.next();
					Integer key = next.getKey();
					Map<Long, FlamePlayer> value = next.getValue();
					List<FlamePlayer> list = new ArrayList<>(value.values());
					List<FlamePlayer> collect = list.stream().sorted(Comparator.comparing(FlamePlayer::rankResource).reversed()).collect(Collectors.toList());
					rankPlayer.put(key, collect);
					allRankList.addAll(value.values());
				}
				this.allRankPlayer = allRankList.stream().sorted(Comparator.comparing(FlamePlayer::rankResource).reversed()).collect(Collectors.toList());

				List<FlameCountry> countries = new ArrayList<>();
				countries.addAll(countryFlameModelMap.values());
				this.rankCountry = countries.stream().sorted(Comparator.comparing(FlameCountry::getResource).reversed()).collect(Collectors.toList());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	public void sendCollectDone(int mailId, Entity resource, long period, long count, int heroId, int heroLv, Player player, boolean isWin, Player target) {
		if (resource == null) {
			return;
		}
		CommonPb.MailCollectRes.Builder collectRes = CommonPb.MailCollectRes.newBuilder();
		if (resource.getPos() != null) {
			collectRes.setPos(resource.getPos().wrapPb());
		}
		int minutes = (int) (period / TimeHelper.MINUTE_MS);
		collectRes.setPeriod(minutes);
		int resType = 6;
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
		Mail mail;
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
			LogHelper.CONFIG_LOGGER.info("sendReport mail failed, mail = null, mailId:{} ", mailId);
			return;
		}
		mail.setMailCollectRes(collectRes.build());
		mail.setAwardGot(2); // 如果为空则邮件got为2
		// 处理采集方邮件
		mail.setPortrait(target != null ? target.getPortrait() : player.getPortrait());
		playerManager.synMailToPlayer(player, mail);
		// 武将经验值
		playerManager.synHeroChange(player, heroId, Reason.WORLD_RESOURCE_COLLECT);

		SpringUtil.getBean(com.game.log.LogUser.class).mail_log(MailLog.builder().lordId(mail.getLordId()).mailId(mail.getMailId()).nick(player.getNick()).vip(player.getVip()).level(player.getLevel()).msg(mailManager.mailToString(mail)).build());
	}

	/**
	 * 这里处理战火兵线
	 *
	 * @param req
	 * @param player
	 */
	public void cancelFlameMarch(WorldPb.MarchCancelRq req, Player player) {
		GameServer.getInstance().mainLogicServer.addCommand(() -> {
			WorldManager worldManager = SpringUtil.getBean(WorldManager.class);
			WorldData worldData = worldManager.getWolrdInfo();
			if (worldData == null) {
				return;
			}
			WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
			if (worldActPlan != null && worldActPlan.getState() != WorldActPlanConsts.OPEN) {
				return;
			}
			int keyId = req.getKeyId();
			March march = player.getMarch(keyId);
			if (march != null) {
				FlameMap flameMap = getFlameMap();
				if (march.getMarchType() == MarchType.FLAME_WAR) {
					long buildId = march.getBuildId();
					FlameWarCity flameWarCity = flameMap.getCityNode().get(buildId);
					if (flameWarCity != null) {
						flameWarCity.getAttackQueue().remove(march);
						flameWarCity.getDefenceQueue().remove(march);
						flameWarService.synFlameBuildInfo(flameWarCity, true);
						synFlameBuild(flameWarCity);
					}

				} else if (march.getMarchType() == MarchType.FLAME_COLLECT) {
					Pos startPos = march.getStartPos();
					FlameWarResource resource = flameMap.getResourceNode().get(startPos);
					if (resource != null) {
						ConcurrentLinkedDeque<FlameGuard> collectArmy = resource.getCollectArmy();
						if (collectArmy != null && collectArmy.getFirst().getMarch() == march) {
							FlameGuard first = collectArmy.getFirst();
							List<Integer> heroIds = march.getHeroIds();
							Hero hero = player.getHero(heroIds.get(0));
							// sendCollectDone(MailId.COLLECT_WIN, resource, System.currentTimeMillis() - first.getStartTime(), first.getTotalRes(), hero.getHeroId(), hero.getHeroLv(), player, false, null);
							battleMailMgr.sendCollectDone(MailId.COLLECT_CANCEL, resource, System.currentTimeMillis() - first.getStartTime(), first.getTotalRes(), hero.getHeroId(), hero.getHeroLv(), player, false, null);

							collectArmy.clear();
							flameWarService.synAdd(resource);
						}
					}
				}
			}
		}, DealType.MAIN);
	}

	public boolean subProp(FlamePlayer player, int itemId, int num) {
		Map<Integer, Item> prop = player.getProp();
		Item item = prop.get(itemId);
		if (item == null || item.getItemNum() < num) {
			return false;
		}
		item.setItemNum(item.getItemNum() - num);
		if (item.getItemNum() == 0) {
			prop.remove(itemId);
		}
		return true;
	}

	// 给在这个地图的人 推送战力变化
	public void sendBuffChange() {
		FlameMap flameMap = getFlameMap();
		Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
		playerCityMap.values().forEach(x -> {
			Player player = x.getPlayer();
			heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
		});
	}

	public boolean isSafePos(Pos pos) {
		Map<Integer, List<Pos>> safePos = getFlameMap().getSafePos();
		for (List<Pos> value : safePos.values()) {
			if (value.contains(pos)) {
				return true;
			}
		}
		return false;
	}
}
