package com.game.service;

import com.game.chat.domain.Chat;
import com.game.constant.AwardType;
import com.game.constant.ChatId;
import com.game.constant.GameError;
import com.game.constant.MapId;
import com.game.constant.MarchReason;
import com.game.constant.SimpleId;
import com.game.constant.WarType;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.dataMgr.StaticZergShopMgr;
import com.game.dataMgr.StaticZerglMgr;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.City;
import com.game.domain.p.Hero;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.StaticMonster;
import com.game.domain.s.StaticWorldCity;
import com.game.domain.s.StaticZergShop;
import com.game.manager.ChatManager;
import com.game.manager.CityManager;
import com.game.manager.PlayerManager;
import com.game.manager.WarBookManager;
import com.game.manager.WorldManager;
import com.game.manager.ZergManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.ZergPb.AttackZergRq;
import com.game.pb.ZergPb.AttackZergRs;
import com.game.pb.ZergPb.AttendZergCityRq;
import com.game.pb.ZergPb.AttendZergCityRs;
import com.game.pb.ZergPb.DefencedInfo;
import com.game.pb.ZergPb.GetZergRs;
import com.game.pb.ZergPb.GetZergShopRs;
import com.game.pb.ZergPb.ZergBuyShopRq;
import com.game.pb.ZergPb.ZergBuyShopRs;
import com.game.pb.ZergPb.ZergCity;
import com.game.pb.ZergPb.ZergWarHelpRs;
import com.game.util.LogHelper;
import com.game.util.Pair;
import com.game.util.TimeHelper;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.PlayerCity;
import com.game.worldmap.Pos;
import com.game.worldmap.fight.IWar;
import com.game.worldmap.fight.war.ZergWarInfo;
import com.game.worldmap.fight.zerg.ZergConst;
import com.game.worldmap.fight.zerg.ZergData;
import com.game.worldmap.fight.process.ZergProcess;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZergService {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticWorldMgr staticWorldMgr;

	@Autowired
	private WorldManager worldManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private ZergManager zergManager;

	@Autowired
	private StaticZerglMgr staticZerglMgr;

	@Autowired
	private ZergProcess zergFight;

	@Autowired
	private WorldService worldService;
	@Autowired
	private WarBookManager warBookManager;
	@Autowired
	private ChatManager chatManager;
	@Autowired
	private StaticMonsterMgr staticMonsterMgr;
	@Autowired
	private StaticZergShopMgr staticZergShopMgr;
	@Autowired
	private CityManager cityManager;

	/**
	 * 虫族主宰信息
	 *
	 * @param handler
	 */
	public void getZerg(ClientHandler handler) {
		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData == null) {
//			LogHelper.MESSAGE_LOGGER.info("worldData:{}", worldData);
			handler.sendErrorMsgToPlayer(GameError.COUNT_ERROR);
			return;
		}

		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_13);
		if (worldActPlan == null) {
//			LogHelper.MESSAGE_LOGGER.info("worldActPlan:{}", worldActPlan);
			handler.sendErrorMsgToPlayer(GameError.COUNT_ERROR);
			return;
		}

		GetZergRs.Builder builder = GetZergRs.newBuilder();
        ZergData zergData = zergManager.getZergData();
		if (zergData == null) {
			builder.setState(3);
			handler.sendMsgToPlayer(GetZergRs.ext, builder.build());
			return;
		}
		int step = zergData.getStep() % 2;
		step = step == 0 ? 2 : step;// 1进攻阶段 2.防守阶段

		Player player = playerManager.getPlayer(handler.getRoleId());

		if (worldActPlan.getState() == WorldActPlanConsts.PREHEAT) {
			builder.setState(0);// 0预热
			builder.setEndTime(worldActPlan.getOpenTime());
		} else if (worldActPlan.getState() == WorldActPlanConsts.OPEN) {
			builder.setState(step);
			builder.setEndTime(zergData.getStepEndTime());
		} else {// 活动已结束
			builder.setState(3);
			handler.sendMsgToPlayer(GetZergRs.ext, builder.build());
			return;
		}

		int cityId = zergData.getCityId();
		StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);

		Pos pos = new Pos(staticWorldCity.getX(), staticWorldCity.getY());

		// 防守阶段,循环创建PB则异步执行
		List<Long> defence = zergData.getStepParam();
		this.getDefenceFuture(player, step, defence).thenAcceptAsync(e -> {

			MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);

			for (Player target : e) {
				DefencedInfo.Builder b = DefencedInfo.newBuilder();
				b.setLordId(target.getRoleId());
				b.setNick(target.getNick());
				b.setPos(target.getPos().wrapPb());
				b.setSkin(target.getLord().getSkin());
				PlayerCity playerCity = mapInfo.getPlayerCity(target.getPos());
				b.setCityLv(playerCity == null ? 1 : playerCity.getLevel());
				builder.addDefenceInfo(b.build());
			}

			ZergCity.Builder zergCity = ZergCity.newBuilder();
			City city = cityManager.getCity(cityId);
			zergCity.setCityName(city.getCityName() == null ? "" : city.getCityName());
			zergCity.setCityId(cityId);
			zergCity.setPos(pos.wrapPb());
			builder.setZergCity(zergCity);

//			LogHelper.MESSAGE_LOGGER.info("GetZergRs:{}", builder.build());

			handler.sendMsgToPlayer(GetZergRs.ext, builder.build());
		});
	}

	private CompletableFuture<List<Player>> getDefenceFuture(Player player, final int state, final List<Long> targets) {
		List<Player> resultList = new ArrayList<>();
		if (state != ZergConst.STEP_DEFEND) {
			return CompletableFuture.completedFuture(resultList);
		}

		for (Long lordId : targets) {
			Player target = playerManager.getPlayer(lordId.longValue());
			if (target.getCountry() != player.getCountry()) {
				continue;
			}
			resultList.add(target);
		}

		return CompletableFuture.completedFuture(resultList);
	}

	/**
	 * 参加主宰战斗
	 *
	 * @param req
	 * @param handler
	 */
	public void attendZergWar(AttackZergRq req, ClientHandler handler) {
		// 需要45级才能宣战
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 查看当前pos存放的实体
		int mapId = MapId.CENTER_MAP_ID;
		MapInfo mapInfo = worldManager.getMapInfo(mapId);
		if (mapInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		// 需要的等级
		int needLevel = staticLimitMgr.getNum(35);
		if (player.getLevel() < needLevel) {
			handler.sendErrorMsgToPlayer(GameError.CANNOT_ATTEND_COUNTRY);
			return;
		}

		long now = System.currentTimeMillis();
		List<IWar> warInfoList = mapInfo.getWarMap().values().stream().filter(e -> e.getWarType() == WarType.ATTACK_ZERG && e.getEndTime() > now).collect(Collectors.toList());
		if (warInfoList == null || warInfoList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.WAR_NOT_EXISTS);
			return;
		}

		ZergWarInfo warInfo = (ZergWarInfo) warInfoList.get(0);
		long warId = warInfo.getWarId();

		// 检查国战是否结束
		if (warInfo.getEndTime() <= now) {
			handler.sendErrorMsgToPlayer(GameError.WAR_IS_OVER);
			return;
		}

		if (req.getHeroIdList().isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.NO_MARCH_HEROS);
			return;
		}

		// 行军英雄
		List<Integer> heroIds = req.getHeroIdList();

		Map<Integer, Hero> heroMap = player.getHeros();
		// 检测英雄是否重复
		HashSet<Integer> checkHero = new HashSet<Integer>();
		// 检查英雄是否上阵
		for (Integer heroId : heroIds) {
			checkHero.add(heroId);
		}

		// 有相同的英雄出征
		if (checkHero.size() != heroIds.size()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
			return;
		}

		// 检查英雄是否可以出征
		for (Integer heroId : heroIds) {
			Hero hero = heroMap.get(heroId);
			if (hero == null) {
				handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
				return;
			}

			if (!isEmbattle(player, heroId)) {
				handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
				return;
			}

			if (player.isHeroInMarch(heroId)) {
				handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
				return;
			}

			// 检查武将带兵量
			if (hero.getCurrentSoliderNum() <= 0) {
				handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
				return;
			}
		}

		// 行军消耗
		// 出兵消耗
		int side = 1;

		// 生成行军
		Pos targetPos = new Pos(warInfo.getDefencerPos().getX(), warInfo.getDefencerPos().getY());
		March march = worldManager.createMarch(player, heroIds, targetPos);
		if (march.getEndTime() >= warInfo.getEndTime() - 5000) {// 开战
			handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
			return;
		}

		march.setFightTime(warInfo.getEndTime() + 1000L, MarchReason.AttendZergWar);
		march.setDefencerId(warInfo.getDefencerId());
		march.setAttackerId(player.getRoleId());

		march.setSide(side);
		march.setMarchType(MarchType.ZERG_WAR); // 需要放到战役里面去
		march.setWarId(warId);

		// 添加行军到玩家身上
		player.addMarch(march);
		warInfo.addAttackMarch(march);

		// 加到世界地图中
		worldManager.addMarch(mapId, march);

		// 返回消息
		AttackZergRs.Builder builder = AttackZergRs.newBuilder();
		worldManager.handleZergWarSoldier(warInfo);
		builder.setWarInfo(warInfo.wrapPb(warInfo.isJoin(player)));
		builder.setMarch(worldManager.wrapMarchPb(march));
		builder.setResource(player.wrapResourcePb());
		handler.sendMsgToPlayer(AttackZergRs.ext, builder.build());

		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	public boolean isEmbattle(Player player, int heroId) {
		List<Integer> embattleList = player.getEmbattleList();
		List<Integer> miningList = player.getMiningList();
		return embattleList.contains(heroId) || miningList.contains(heroId);
	}

	/**
	 * 虫族入侵参加防守
	 *
	 * @param handler
	 */
	public void attendZergCityRq(AttendZergCityRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Player target = playerManager.getPlayer(req.getRoleId());
		if (target == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		if (player.roleId == target.roleId) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 增加等级限制
		int playerLevel = player.getLevel();
		if (playerLevel < staticLimitMgr.getNum(SimpleId.REBAL_ATTACK_LV)) {
			handler.sendErrorMsgToPlayer(GameError.CITY_WAR_LEVEL_NOT_ENOUGH);
			return;
		}
		int mapId = worldManager.getMapId(player);

		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		if (mapInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		int warMapId = worldManager.getMapId(target.getPos());

		Optional<IWar> optional = mapInfo.getWarMap().values().stream().filter(e -> e.getDefencer().getId() == target.getRoleId()).findFirst();
		if (!optional.isPresent()) {
			handler.sendErrorMsgToPlayer(GameError.WAR_IS_OVER);
			return;
		}
		ZergWarInfo warInfo = (ZergWarInfo) optional.get();

		// 相同国家不能发生城战
		int defencerCountry = target.getCountry();
		int myCountry = player.getCountry();
		if (defencerCountry != myCountry) {
			handler.sendErrorMsgToPlayer(GameError.NOT_SAME_MAP);
			return;
		}
		int side = 2;
		// 是否在一个区域
		if (mapId != warMapId) { // 不在同一个地图
			handler.sendErrorMsgToPlayer(GameError.NOT_SAME_MAP);
			return;
		}

		// 行军英雄
		List<Integer> heroIds = req.getHeroIdList();
		if (heroIds.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.NO_HERO_MARCH);
			return;
		}

		Map<Integer, Hero> heroMap = player.getHeros();
		// 检测英雄是否重复
		HashSet<Integer> checkHero = new HashSet<Integer>();
		// 检查英雄是否上阵
		for (Integer heroId : heroIds) {
			if (!worldService.isEmbattle(player, heroId)) {
				handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
				return;
			}
			checkHero.add(heroId);
		}

		// 有相同的英雄出征
		if (checkHero.size() != heroIds.size()) {
			handler.sendErrorMsgToPlayer(GameError.HAS_SAME_HERO_ID);
			return;
		}

		// 检查英雄是否可以出征
		for (Integer heroId : heroIds) {
			Hero hero = heroMap.get(heroId);
			if (hero == null) {
				handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
				return;
			}

			if (!playerManager.isHeroFree(player, heroId)) {
				handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
				return;
			}

			// 检查武将带兵量
			if (hero.getCurrentSoliderNum() <= 0) {
				handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
				return;
			}
		}

		// 行军消耗
		// 出兵消耗
		Pos targetPos = target.getPos();

		// 检查行军时间
		Pos playerPos = player.getPos();

		// 兵书对行军的影响值
		float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);
		long period = worldManager.getPeriod(player, playerPos, targetPos, bookEffectMarch);
		long attackPeriod = staticLimitMgr.getNum(23) * TimeHelper.SECOND_MS;

		if (attackPeriod <= 0L) {
			handler.sendErrorMsgToPlayer(GameError.WAR_IS_STARTED);
			return;
		}

		if (period > attackPeriod) {
			handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
			return;
		}

		// 检查战斗是否已经结束了
		if (worldManager.isPvpWarOver(warInfo)) {
			handler.sendErrorMsgToPlayer(GameError.WAR_IS_STARTED);
			return;
		}

		// 生成行军
		March march = worldManager.createMarch(player, heroIds, targetPos);
		// 检查行军是否超过战斗时间
		if (!worldService.isMarchWarOk(march.getPeriod(), warInfo)) {
			handler.sendErrorMsgToPlayer(GameError.TO_LONG_MARCH);
			return;
		}

		march.setDefencerId(warInfo.getDefencerId());
		march.setAttackerId(warInfo.getAttackerId());
		march.setSide(side);
		march.setMarchType(MarchType.ZERG_DEFEND_WAR);

		march.setWarId(warInfo.getWarId());
		march.setFightTime(warInfo.getEndTime(), MarchReason.AttendPvpWar);
		// add march to player
		player.addMarch(march);

		// attack or defence
		worldManager.synAddCityWar(target, warInfo);
		// add world map
		worldManager.addMarch(mapId, march);

		// return msg
		AttendZergCityRs.Builder builder = AttendZergCityRs.newBuilder();
		builder.setMarch(worldManager.wrapMarchPb(march));
		handler.sendMsgToPlayer(AttendZergCityRs.ext, builder.build());
		worldManager.synMarch(mapInfo.getMapId(), march);
	}

	// 请求支援
	public void zergWarHelpRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		MapInfo mapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
		if (mapInfo == null) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		Optional<IWar> optional = mapInfo.getWarMap().values().stream().filter(e -> e.getDefencer().getId() == player.getRoleId()).findFirst();
		if (!optional.isPresent()) {
			handler.sendErrorMsgToPlayer(GameError.WAR_IS_OVER);
			return;
		}
		ZergWarInfo warInfo = (ZergWarInfo) optional.get();
		warInfo.setDefencerHelpTime(warInfo.getDefencerHelpTime() + 1);

		StaticMonster staticMonster = staticMonsterMgr.getStaticMonster((int) warInfo.getAttackerId());
		Chat chat = chatManager.createManShare(player, ChatId.ZERG_HELP_DEFENCE, String.valueOf(staticMonster.getMonsterId()), player.getPosStr());
		CommonPb.Chat chat1 = chatManager.sendCountryShare(player, chat);

		ZergWarHelpRs.Builder builder = ZergWarHelpRs.newBuilder();
		// CommonPb.Chat b = chatManager.addCountryChat(player.getCountry(), 0, chat);
		builder.setChat(chat1);
		builder.setKeyId(warInfo.getWarId());
		handler.sendMsgToPlayer(ZergWarHelpRs.ext, builder.build());
	}

	public void getZergShopRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		WorldData worldData = worldManager.getWolrdInfo();
		if (worldData == null) {
			handler.sendErrorMsgToPlayer(GameError.WORLD_TARGET_NOT_OPEN);
			return;
		}
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_13);
		if (worldActPlan == null) {
			handler.sendErrorMsgToPlayer(GameError.WORLD_TARGET_NOT_OPEN);
			return;
		}

        ZergData zergData = zergManager.getZergData();
		if (zergData == null) {
			handler.sendErrorMsgToPlayer(GameError.WORLD_TARGET_NOT_OPEN);
			return;
		}

		int recordDate = zergData.getRecordDate();
		List<StaticZergShop> shops = staticZergShopMgr.getShops();
		Map<Integer, Pair<Integer, Long>> records = player.getSimpleData().getZergShop();

		GetZergShopRs.Builder builder = GetZergShopRs.newBuilder();
		for (StaticZergShop e : shops) {
			CommonPb.Shop.Builder shopPb = CommonPb.Shop.newBuilder();
			shopPb.setPropId(e.getPropId());// 道具ID
			shopPb.setPrice(e.getScore());// 购买需要积分
			shopPb.setCount(e.getCount());// 一次购买可得数量
			if (records.containsKey(e.getPropId())) {
				Pair<Integer, Long> history = records.get(e.getPropId());
				if (history.getRight().intValue() == recordDate) {
					shopPb.setBuyCount(e.getLimit() - history.getLeft());// 剩余购买次数
				} else {
					shopPb.setBuyCount(e.getLimit());// 剩余购买次数
				}
			} else {
				shopPb.setBuyCount(e.getLimit());// 剩余购买次数
			}
			builder.addShop(shopPb.build());
		}

		builder.setScore(player.getSimpleData().getZergScore());
		handler.sendMsgToPlayer(GetZergShopRs.ext, builder.build());
	}

	public void ZergBuyShopRq(ZergBuyShopRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_13);
		if (worldActPlan == null) {
			handler.sendErrorMsgToPlayer(GameError.WORLD_TARGET_NOT_OPEN);
			return;
		}
        ZergData zergData = zergManager.getZergData();
		if (zergData == null) {
			handler.sendErrorMsgToPlayer(GameError.WORLD_TARGET_NOT_OPEN);
			return;
		}
		int shopId = req.getShopId();
		StaticZergShop staticZergShop = staticZergShopMgr.getShopMap().get(shopId);
		if (staticZergShop == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int zergScore = player.getSimpleData().getZergScore();
		if (zergScore < staticZergShop.getScore()) {
			handler.sendErrorMsgToPlayer(GameError.ZERG_SCORE_NOT_ENOUGH);
		}

		// 活动结束
		int recordDate = zergData.getRecordDate();

		Map<Integer, Pair<Integer, Long>> records = player.getSimpleData().getZergShop();
		Pair<Integer, Long> record = records.get(shopId);
		if (record == null) {
			record = new Pair<>(0, Long.valueOf(recordDate));
			records.put(shopId, record);
		}
		if (record.getRight() != recordDate) {
			record.setLeft(0);
			record.setRight(Long.valueOf(recordDate));
		}

		if (record.getLeft() >= staticZergShop.getLimit()) {
			handler.sendErrorMsgToPlayer(GameError.CANT_BUY);
			return;
		}

		player.getSimpleData().subZergScore(staticZergShop.getScore());
		record.setLeft(record.getLeft() + 1);

		ZergBuyShopRs.Builder builder = ZergBuyShopRs.newBuilder();

		List<StaticZergShop> shops = staticZergShopMgr.getShops();
		for (StaticZergShop e : shops) {
			CommonPb.Shop.Builder shopPb = CommonPb.Shop.newBuilder();
			shopPb.setPropId(e.getPropId());// 道具ID
			shopPb.setPrice(e.getScore());// 购买需要积分
			shopPb.setCount(e.getCount());// 一次购买可得数量
			if (records.containsKey(e.getPropId())) {
				Pair<Integer, Long> history = records.get(e.getPropId());
				if (history.getRight().intValue() == recordDate) {
					shopPb.setBuyCount(e.getLimit() - history.getLeft());// 剩余购买次数
				} else {
					shopPb.setBuyCount(e.getLimit());// 剩余购买次数
				}
			} else {
				shopPb.setBuyCount(e.getLimit());// 剩余购买次数
			}
			builder.addShop(shopPb.build());
		}

		playerManager.addAward(player, AwardType.PROP, staticZergShop.getPropId(), staticZergShop.getCount(), 0);
		CommonPb.Award.Builder award = CommonPb.Award.newBuilder();
		award.setType(AwardType.PROP);
		award.setId(staticZergShop.getPropId());
		award.setCount(staticZergShop.getCount());
		builder.setScore(player.getSimpleData().getZergScore());
		builder.addAward(award.build());
		handler.sendMsgToPlayer(ZergBuyShopRs.ext, builder.build());
	}
}
