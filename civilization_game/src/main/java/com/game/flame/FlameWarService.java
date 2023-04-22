package com.game.flame;

import com.game.chat.domain.Chat;
import com.game.constant.Country;
import com.game.constant.*;
import com.game.dataMgr.StaticCountryMgr;
import com.game.dataMgr.StaticFlameWarMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticWorldActPlanMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.StaticWorldActPlan;
import com.game.flame.entity.*;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.FlameWarPb;
import com.game.pb.FlameWarPb.SynFlameBuildInfo;
import com.game.pb.FlameWarPb.SynFlameCountryInfo;
import com.game.pb.FlameWarPb.SynFlameEntityAddRq;
import com.game.pb.FlameWarPb.SynFlameEntityRq;
import com.game.pb.WorldPb;
import com.game.pb.WorldPb.SynEntityRq;
import com.game.service.ActivityService;
import com.game.service.WorldService;
import com.game.util.LogHelper;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

@Service
public class FlameWarService {

    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private BattleMgr battleMgr;
    @Autowired
    private WorldManager worldManager;
    @Autowired
    FlameWarManager flameWarManager;
    @Autowired
	StaticFlameWarMgr staticFlameWarMgr;
    @Autowired
    StaticLimitMgr staticLimitMgr;
    @Autowired
    CountryManager countryManager;
    @Autowired
    StaticCountryMgr staticCountryMgr;
    @Autowired
    ItemManager itemManager;
    @Autowired
    StaticPropMgr staticPropMgr;
    @Autowired
    WarManager warManager;
    @Autowired
    WorldLogic worldLogic;
    @Autowired
    ActivityManager activityManager;
    @Autowired
    ActivityService activityService;
    @Autowired
    WorldBoxManager worldBoxManager;
    @Autowired
    WorldService worldService;
    @Autowired
    WarBookManager warBookManager;
    @Autowired
    HeroManager heroManager;
    @Autowired
    ChatManager chatManager;
    @Autowired
    StaticWorldActPlanMgr staticWorldActPlanMgr;
    @Autowired
    BattleMailManager battleMailMgr;

    /**
     * 打开活动面板
     *
     * @param handler
     */
    public void loadFlameWarInit(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        FlameWarPb.FlameWarInitRs.Builder builder = FlameWarPb.FlameWarInitRs.newBuilder();

        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
        if (worldActPlan == null) {
            builder.setState(WorldActPlanConsts.NOE_OPEN);
            handler.sendMsgToPlayer(FlameWarPb.FlameWarInitRs.ext, builder.build());
            return;
        }

        builder.setState(worldActPlan.getState());
        long endTime = worldActPlan.getPreheatTime();
        switch (worldActPlan.getState()) {
            case WorldActPlanConsts.PREHEAT:
            case FlameWarManager.ENTER:
                endTime = worldActPlan.getOpenTime();
                break;
            case WorldActPlanConsts.OPEN:
                endTime = worldActPlan.getEndTime();
                break;
            case WorldActPlanConsts.DO_END:
            case FlameWarManager.EXHIBITION:
                endTime = worldActPlan.getExhibitionTime();
                break;
            default:
                break;
        }
        builder.setEndTime(endTime);

        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);

        if (flamePlayer != null) {
            Map<Integer, Buff> buff = flamePlayer.getBuff();
            buff.values().forEach(x -> {
                builder.addBuff(x.getBuffId());
            });
            Map<Integer, StaticFlameShop> flameShopMap = staticFlameWarMgr.getFlameShopMap();
            Map<Integer, Integer> awardCount = flamePlayer.getAwardCount();
            flameShopMap.values().forEach(x -> {
                CommonPb.TwoInt.Builder builder2 = CommonPb.TwoInt.newBuilder();
                builder2.setV1(x.getId());
                Integer integer = awardCount.get(x.getId());
                builder2.setV2(0);
                if (integer != null) {
                    builder2.setV2(integer);
                }
                builder.addShop(builder2);
            });

            builder.setNextEnterTime(flamePlayer.getNextEnterTime());
        }
        builder.setScore(player.getScore());
        handler.sendMsgToPlayer(FlameWarPb.FlameWarInitRs.ext, builder.build());
    }

    /**
     * 增益购买
     *
     * @param handler
     * @param rq
     */
    public void buyFlameWarBuff(ClientHandler handler, FlameWarPb.FlameBuyBuffRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
        if (worldActPlan == null || (worldActPlan.getState() != WorldActPlanConsts.OPEN && worldActPlan.getState() != flameWarManager.ENTER)) {
            return;
        }
        int buffId = rq.getBuffId();
        StaticFlameBuff buffById = staticFlameWarMgr.getBuffById(buffId);
        if (buffById == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        int cost = 0;
        StaticFlameBuff buffByNextId1 = staticFlameWarMgr.getBuffByNextId(buffById.getId());
        if (buffByNextId1 != null) {
            cost = buffByNextId1.getCost();
        }

        if (player.getGold() < cost) {
            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
            return;
        }

        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
        Map<Integer, Buff> buffMap = flamePlayer.getBuff();
        if (buffMap.isEmpty()) {
            Map<Integer, StaticFlameBuff> flameBuffMap = staticFlameWarMgr.getFlameBuffMap();
            flameBuffMap.values().stream().filter(x -> x.getLv() == 0).forEach(a -> {
                Buff buff = new Buff();
                buff.setBuffId(a.getId());
                buffMap.put(a.getId(), buff);
            });
        }
        StaticFlameBuff buffByNextId = staticFlameWarMgr.getBuffByNextId(buffId);
        if (buffByNextId != null) {
            if (!buffMap.containsKey(buffByNextId.getId())) {
                return;
            }
            buffMap.remove(buffByNextId.getId());
        }
        Buff buff = new Buff();
        buff.setBuffId(buffById.getId());
        buffMap.put(buffById.getId(), buff);
        FlameWarPb.FlameBuyBuffRs.Builder builder = FlameWarPb.FlameBuyBuffRs.newBuilder();
        builder.setState(0);
        List<Integer> collect = buffMap.keySet().stream().sorted(Integer::compareTo).collect(Collectors.toList());
        collect.forEach(x -> {
            builder.addBuff(x);
        });
        player.subGold(cost);
        builder.setGold(player.getGold());
        handler.sendMsgToPlayer(FlameWarPb.FlameBuyBuffRs.ext, builder.build());
        if (worldActPlan.getState() != flameWarManager.ENTER) {
            heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
        }
    }

    /**
     * 商店兑换
     *
     * @param handler
     * @param rq
     */
    public void exchangeShop(ClientHandler handler, FlameWarPb.ShopExchangeRq rq) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
        if (worldActPlan == null) {
            return;
        }
        int keyId = rq.getKeyId();
        StaticFlameShop staticFlameShop = staticFlameWarMgr.getStaticFlameShop(keyId);
        if (staticFlameShop == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);

        Map<Integer, Integer> awardCount = flamePlayer.getAwardCount();
        Integer integer = awardCount.getOrDefault(keyId, 0);
        if (integer >= staticFlameShop.getLimit()) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        if (player.getScore() < staticFlameShop.getNeedScore()) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }
        player.addScore(-staticFlameShop.getNeedScore());

        awardCount.merge(keyId, 1, (a, b) -> a + b);
        List<Integer> x = staticFlameShop.getProp();
        FlameWarPb.ShopExchangeRs.Builder builder = FlameWarPb.ShopExchangeRs.newBuilder();
        builder.setScore(player.getScore());
        List<Award> list = new ArrayList<>();
        list.add(new Award(0, x.get(0), x.get(1), x.get(2)));
        CommonPb.Award.Builder builder1 = CommonPb.Award.newBuilder();
        builder1.setType(x.get(0));
        builder1.setId(x.get(1));
        builder1.setCount(x.get(2));
        builder.addAward(builder1);

        playerManager.addAward(player, list, Reason.FLAME);

        CommonPb.TwoInt.Builder builder2 = CommonPb.TwoInt.newBuilder();
        builder2.setV1(keyId);
        builder2.setV2(awardCount.get(keyId));

        builder.setShop(builder2);
        handler.sendMsgToPlayer(FlameWarPb.ShopExchangeRs.ext, builder.build());

        if (staticFlameShop.getChat() == 1) {
            String p[] = {player.getNick(), String.valueOf(x.get(1))};
            chatManager.sendWorldChat(ChatId.FLAME_SHOP, p);
        }
    }

    /**
     * 进入活动
     *
     * @param handler
     */
    public void enterFlameMap(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        WorldData worldData = worldManager.getWolrdInfo();
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
        if (worldActPlan == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        StaticWorldActPlan staticWorldActPlan = staticWorldActPlanMgr.get(worldActPlan.getId());
        if (staticWorldActPlan == null || player.getLevel() < staticWorldActPlan.getLordLv()) {
            handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
            return;
        }

        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
        long time = System.currentTimeMillis();
        if (worldActPlan.getState() == WorldActPlanConsts.OPEN && time < flamePlayer.getNextEnterTime()) {
            handler.sendErrorMsgToPlayer(GameError.FIRE_OUT);
            return;
        }
        FlameMap flameMap = flameWarManager.getFlameMap();
        Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
        long count = playerCityMap.values().stream().filter(x -> x.getCountry() == player.getCountry()).count();
        int max_enter = staticLimitMgr.getNum(SimpleId.FIRE_MAX_ENTER);
        if (count >= max_enter) {
            handler.sendErrorMsgToPlayer(GameError.FIRE_MAX);
            return;
        }
        int mapId = player.getLord().getMapId();

        if (mapId != MapId.FIRE_MAP) {
            if (!player.getMarchList().isEmpty()) {
                handler.sendErrorMsgToPlayer(GameError.HERO_IN_MARCH);
                return;
            }

            MapInfo mapInfo = worldManager.getMapInfo(mapId);
            Pos oldPos = player.getPos();

            Pos safePos = flameMap.getSafePos(player.getCountry());

            PlayerCity playerCity = worldManager.changePlayerPos(player, safePos);


            //PlayerCity playerCity = worldManager.addPlayerCity(safePos, flameMap, player);
            //if (playerCity == null) {
            //	LogHelper.MESSAGE_LOGGER.info("flame pos:{} old:{}", safePos, oldPos);
            //	handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
            //	return;
            //}
            //
            //worldManager.removePlayerCity(oldPos, mapInfo);// 移除老坐标
            //playerManager.changePlayerPos(player, safePos);


            worldManager.handleWallFriendReturn(player);// 所有驻防全部回去


            // 通知老坐标 清空地块
            WorldPb.SynEntityRq.Builder old = WorldPb.SynEntityRq.newBuilder();
            old.setOldPos(CommonPb.Pos.newBuilder().setX(oldPos.getX()).setY(oldPos.getY()).build());
            playerManager.getOnlinePlayer().forEach(x -> {
                SynHelper.synMsgToPlayer(x, SynEntityRq.EXT_FIELD_NUMBER, SynEntityRq.ext, old.build());
            });
            if (playerCity != null) {
                synAdd(playerCity);
            }
        }
        FlameWarPb.OpenFlameMapRs.Builder builder = FlameWarPb.OpenFlameMapRs.newBuilder();
        ConcurrentLinkedDeque<March> marches = flameMap.getMarches();
        marches.forEach(x -> {
            builder.addMarch(worldManager.wrapMarchPb(x));
        });
        builder.setPos(CommonPb.Pos.newBuilder().setX(player.getLord().getPosX()).setY(player.getLord().getPosY()));
        handler.sendMsgToPlayer(FlameWarPb.OpenFlameMapRs.ext, builder.build());
        synCountryInfo();
        heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
    }

    public void synAdd(Entity node) {
        FlameMap flameMap = flameWarManager.getFlameMap();
        FlameWarPb.SynFlameEntityAddRq.Builder builder = FlameWarPb.SynFlameEntityAddRq.newBuilder();
        builder.addEntity(node.wrapPb());
        flameMap.getPlayerCityMap().values().forEach(x -> {
            Player player1 = x.getPlayer();
            SynHelper.synMsgToPlayer(player1, SynFlameEntityAddRq.EXT_FIELD_NUMBER, SynFlameEntityAddRq.ext, builder.build());
//			player1.sendMsgToPlayer(FlameWarPb.SynFlameEntityAddRq.ext, builder.build(), FlameWarPb.SynFlameEntityAddRq.EXT_FIELD_NUMBER);

        });
    }

    /**
     * 拉取地图实体
     *
     * @param rq
     * @param handler
     */
    public void flameMap(FlameWarPb.FlameMapRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        FlameMap flameMap = flameWarManager.getFlameMap();
        Map<Pos, Entity> node = flameMap.getNode();
        CommonPb.Pos pos = rq.getPos();
        int posX = pos.getX();
        int posY = pos.getY();
        int minX = Math.max(551, posX - 15);
        int maxX = Math.min(600, posX + 15);
        int minY = Math.max(551, posY - 15);
        int maxY = Math.min(600, posY + 15);
        FlameWarPb.FlameMapRs.Builder builder = FlameWarPb.FlameMapRs.newBuilder();
        for (int i = minX; i <= maxX; i++) {
            for (int j = minY; j <= maxY; j++) {
                Pos pos1 = new Pos(i, j);
                if (player.getPushPos().containsKey(pos1.toPosStr())) {
                    continue;
                }
                player.getPushPos().put(pos1.toPosStr(), true);
                Entity node1 = node.get(pos1);
                if (node1 != null) {
                    CommonPb.WorldEntity.Builder builder1 = node1.wrapPb();
                    if (node1.getNodeType() == NodeType.City) {
                        FlameWarCity city = (FlameWarCity) node1;
                        builder1.setReceive(2);
                        if (node1.getCountry() == player.getCountry() && city.getAward() != null && !city.getPlayerAwardList().contains(player.getRoleId())) {
                            builder1.setReceive(1);
                        }
                        builder1.setAllOwnTime(city.getOccupy());
                        builder1.setIsCanJoinAttack(0);
                        if (node1.getCountry() == player.getCountry()) {
                            if (!city.getAttackQueue().isEmpty() || city.getDefenceQueue().isEmpty()) {
                                builder1.setIsCanJoinAttack(1);
                            }
                        } else {
                            March march = city.getAttackQueue().stream().filter(z -> city.getCountry() == player.getCountry()).findAny().orElse(null);
                            if (march != null) {
                                builder1.setIsCanJoinAttack(2);
                            }
                        }
                    }
                    builder.addWorldEntity(builder1);
                }
            }
        }
        handler.sendMsgToPlayer(FlameWarPb.FlameMapRs.ext, builder.build());
    }

    public void synCountryInfo() {
        FlameWarPb.SynFlameCountryInfo.Builder builder = FlameWarPb.SynFlameCountryInfo.newBuilder();
        Country[] values = Country.values();
        FlameMap flameMap = flameWarManager.getFlameMap();
        for (Country value : values) {
            int country = value.getKey();
            FlameWarPb.FlameCountryInfo.Builder builder1 = FlameWarPb.FlameCountryInfo.newBuilder();
            builder1.setCountry(country);
            FlameCountry flameCountry = flameWarManager.getCountryFlameModelMap().computeIfAbsent(country, x -> new FlameCountry());
            flameCountry.setCountry(country);
            builder1.setTotal(flameCountry.getResource());
            long count = flameMap.getPlayerCityMap().values().stream().filter(x -> x.getCountry() == value.getKey()).count();
            builder1.setNum((int) count);
            Map<Long, FlameWarCity> cityNode = flameWarManager.getFlameMap().getCityNode();
            List<FlameWarCity> collect = cityNode.values().stream().filter(x -> x.getCountry() == country && x.getState() == NodeState.CAPTURE).collect(Collectors.toList());
            long res = 0;
            for (FlameWarCity flameWarCity : collect) {
                StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(flameWarCity.getId());
                if (staticFlameBuild != null) {
                    res += staticFlameBuild.getContinueCampAmount();
                }
            }
            builder1.setTimeResource(res);
            builder.addInfo(builder1);
        }
        flameWarManager.getFlameMap().getPlayerCityMap().values().forEach(x -> {
            Player player = x.getPlayer();
            SynHelper.synMsgToPlayer(player, SynFlameCountryInfo.EXT_FIELD_NUMBER, SynFlameCountryInfo.ext, builder.build());
//			player.sendMsgToPlayer(FlameWarPb.SynFlameCountryInfo.ext, builder.build(), FlameWarPb.SynFlameCountryInfo.EXT_FIELD_NUMBER);

        });
    }

    /**
     * 出击
     *
     * @param handler
     */
    public void attackFlame(FlameWarPb.AttackFlameRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null || player.getLord().getMapId() != MapId.FIRE_MAP) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        CommonPb.Pos pos = req.getPos();
        Pos pos1 = new Pos(pos.getX(), pos.getY());
        FlameMap flameMap = flameWarManager.getFlameMap();
        Entity node = flameMap.getNode(pos1);
        boolean flag = false;
        // 攻打的坐标在安全区 不让打
        Map<Integer, List<Pos>> safePos = flameMap.getSafePos();
        for (List<Pos> value : safePos.values()) {
            if (value.contains(pos1)) {
                flag = true;
                break;
            }
        }
        if (node == null || flag) {
            return;
        }
        List<Integer> heroIdList = req.getHeroIdList();
        switch (node.getNodeType()) {
            case City:
                attackBuild(player, heroIdList, handler, pos1, (FlameWarCity) node);
                break;
            case Mine:
                collectResRq(player, handler, heroIdList, (FlameWarResource) node);
                break;
            case Player:
                // 攻打玩家
                // attackPvp(req, player, (FlamePlayerCity) node, handler, pos1);
                break;
        }
    }

    public void attackBuild(Player player, List<Integer> heroIds, ClientHandler handler, Pos pos1, FlameWarCity flameWarCity) {
        if (flameWarCity.getNodeState() == NodeState.NOT_OPEN || heroIds.size() != 1) {
            handler.sendErrorMsgToPlayer(GameError.CITY_IS_PROTECTED);
            return;
        }
        if (flameWarCity.getCountry() != player.getCountry() && flameWarCity.getAttackQueue().size() >= 30) {
            handler.sendErrorMsgToPlayer(GameError.MAX_ARMY_COUNT);
            return;
        }
        if (flameWarCity.getCountry() == player.getCountry() && flameWarCity.getDefenceQueue().size() >= 10) {
            handler.sendErrorMsgToPlayer(GameError.MAX_ARMY_COUNT);
            return;
        }
        // 出兵消耗
        int oilCost = worldManager.getMarchOil(heroIds, player, pos1);
        if (player.getResource(ResourceType.OIL) < oilCost) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }

        // 行军英雄
        for (Integer heroId : heroIds) {
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
                return;
            }
        }
        FlameMap flameMap = flameWarManager.getFlameMap();
        playerManager.subAward(player, AwardType.RESOURCE, ResourceType.OIL, oilCost, Reason.FLAME);
        // 生成行军 // 创建一个行军
        March march = flameWarManager.createFlameWarMarch(player, heroIds, pos1);
        march.setFightTime(march.getEndTime() + 1000L, MarchReason.CountryAttender);
        march.setAttackerId(player.roleId);
        march.setDefencerId(flameWarCity.getId());
        march.setSide(1);
        march.setMarchType(MarchType.FLAME_WAR);
        march.setBuildId((int) flameWarCity.getId());
        // 添加行军到玩家身上
        player.addMarch(march);
        // 加到世界地图www中
        flameMap.addMarch(march);
        flameWarCity.attackPos(march);
        worldManager.synMarch(100, march);
        // 返回消息
        FlameWarPb.AttackFlameRs.Builder builder = FlameWarPb.AttackFlameRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        builder.setResource(player.wrapResourcePb());
        builder.setEnergy(player.getEnergy());
        builder.setEnergyCD(playerManager.getEnergyCD(player));
        handler.sendMsgToPlayer(FlameWarPb.AttackFlameRs.ext, builder.build());

        synFlameBuildInfo(flameWarCity, true);
        flameWarManager.synFlameBuild(flameWarCity);
    }

    public void collectResRq(Player player, ClientHandler handler, List<Integer> heroIds, FlameWarResource resouce) {
        if (player.getColectNum() || heroIds.size() != 1) {
            handler.sendErrorMsgToPlayer(GameError.COLLECT_NUN_MAX);
            return;
        }
        // 行军英雄
        Map<Integer, Hero> heroMap = player.getHeros();
        for (Integer heroId : heroIds) {
            // 检查英雄是否上阵
            if (!worldService.isEmbattle(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EMBATTLE);
                return;
            }
            // 检查英雄是否可以出征
            Hero hero = heroMap.get(heroId);
            if (hero == null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
                return;
            }
            // 检查武将带兵量
            if (hero.getCurrentSoliderNum() <= 0) {
                handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
                return;
            }
            if (!playerManager.isHeroFree(player, heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_STATE_ERROR);
                return;
            }
        }
        List<March> hasMarch = player.getMarch(resouce.getPos());
        if (hasMarch != null) {
            March march = hasMarch.stream().filter(x -> x.getMarchType() == MarchType.FLAME_COLLECT).findFirst().orElse(null);
            if (march != null) {
                handler.sendErrorMsgToPlayer(GameError.HERO_ALREADY_COLLECTED);
                return;
            }
        }
        // 出兵消耗
        int oilCost = worldManager.getMarchOil(heroIds, player, resouce.getPos());
        if (player.getResource(ResourceType.OIL) < oilCost) {
            handler.sendErrorMsgToPlayer(GameError.RESOURCE_NOT_ENOUGH);
            return;
        }
        // 处理普通采集
        March march = flameWarManager.createFlameWarMarch(player, heroIds, resouce.getPos());
        march.setMarchType(MarchType.FLAME_COLLECT);
        // 添加行军到玩家身上
        player.addMarch(march);
        // 加到世界地图中
        FlameMap flameMap = flameWarManager.getFlameMap();
        flameMap.addMarch(march);
        playerManager.subAward(player, AwardType.RESOURCE, ResourceType.OIL, oilCost, Reason.KILL_WORLD_MONSTER);
        FlameWarPb.AttackFlameRs.Builder builder = FlameWarPb.AttackFlameRs.newBuilder();
        builder.setMarch(worldManager.wrapMarchPb(march));
        builder.setResource(player.wrapResourcePb());
        handler.sendMsgToPlayer(FlameWarPb.AttackFlameRs.ext, builder.build());
        // 同步到世界
        worldManager.synMarch(flameMap.getMapId(), march);
    }

    public void flameMarchEnd(March march) {
        Player player = playerManager.getPlayer(march.getLordId());
        if (player == null) {
            return;
        }
        FlameMap flameMap = flameWarManager.getFlameMap();
        Entity node = flameMap.getNode(march.getEndPos());
        if (node == null) {
            worldManager.handleMiddleReturn(march, MarchReason.LostTarget);
            worldManager.synMarch(flameMap.getMapId(), march);
            return;
        }
        switch (node.getNodeType()) {
            case City:
                if (march.getMarchType() != MarchType.FLAME_WAR) {
                    worldManager.handleMiddleReturn(march, Reason.FLAME);
                    worldManager.synMarch(flameMap.getMapId(), march);
                    return;
                }
                march.setState(MarchState.Waiting);
                break;
            case Mine:
                if (march.getMarchType() != MarchType.FLAME_COLLECT) {
                    worldManager.handleMiddleReturn(march, Reason.FLAME);
                    worldManager.synMarch(flameMap.getMapId(), march);
                    return;
                }
                doQuickResource(player, march, (FlameWarResource) node);
                break;
        }
        worldManager.synMarch(flameMap.getMapId(), march);

    }

    public void doQuickResource(Player player, March march, FlameWarResource flameWarResource) {
        StaticFlameMine staticFlameMine = staticFlameWarMgr.getStaticFlameMine(flameWarResource.getResId());
        FlameMap flameMap = flameWarManager.getFlameMap();
        if (staticFlameMine == null) {
            worldManager.handleMiddleReturn(march, Reason.FLAME);
            worldManager.synMarch(flameMap.getMapId(), march);
            return;
        }
        long l = TimeHelper.curentTime();
        ConcurrentLinkedDeque<FlameGuard> collectArmy = flameWarResource.getCollectArmy();

        if (collectArmy.isEmpty()) {
            FlameGuard flameGuard = new FlameGuard();
            flameGuard.setMarch(march);
            flameGuard.setResouce(flameWarResource);
            flameGuard.setStartTime(l);
            flameGuard.setNextCalTime(l + staticFlameMine.getCollectTime());
            flameGuard.setPlayer(player);
            flameWarResource.getCollectArmy().add(flameGuard);
            march.setState(MarchState.Collect);// 采集
            long l1 = staticFlameMine.getResource() - flameWarResource.getConvertRes();
            long l2 = l1 / ((staticFlameMine.getCollectTime() / 60000) * 100);
            march.setEndTime(l + l2 * 60 * 1000);
            worldManager.synMarch(flameMap.getMapId(), march);
            synAdd(flameWarResource);
        } else {
            FlameGuard first = collectArmy.getFirst();
            if (first.getMarch().getCountry() == player.getCountry()) {
                handleMarchReturn(march, Reason.FLAME);
                worldManager.synMarch(flameMap.getMapId(), march);
                playerManager.sendNormalMail(player, MailId.FLAME_MAIL_156, flameWarResource.getPosStr());
                return;
            } else {
                March def = first.getMarch();
                Player player1 = playerManager.getPlayer(def.getLordId());
                String[] param = {player.getNick(), player1.getNick(), flameWarResource.getPosStr()};

                Team teamA = handleSimple(march);
                Team teamB = handleSimple(def);
                Random rand = new Random(l);
                battleMgr.doTeamBattle(teamA, teamB, rand, ActPassPortTaskType.IS_WORLD_WAR);

                HashMap<Integer, Integer> attackRec = new HashMap<Integer, Integer>(4);
                atertBattle(teamA, attackRec, null, march);
                HashMap<Integer, Integer> defRec = new HashMap<Integer, Integer>(4);
                atertBattle(teamB, defRec, null, def);
                int mailId = MailId.ATK_COLLECT_WIN;
                int collectId = MailId.COLLECT_BREAK;
                boolean isWin = true;
                if (teamA.isWin()) {
                    // 退回
                    Award award = new Award();
                    long totalRes = first.getTotalRes();
                    award.setCount((int) totalRes);
                    def.addAwards(award);
                    // 采集中断
                    collectArmy.clear();

                    flameWarResource.getCollectArmy().clear();
                    FlameGuard flameGuard = new FlameGuard();
                    flameGuard.setMarch(march);
                    flameGuard.setResouce(flameWarResource);
                    flameGuard.setStartTime(l);
                    flameGuard.setNextCalTime(l + staticFlameMine.getCollectTime());
                    flameGuard.setPlayer(player);
                    flameWarResource.getCollectArmy().add(flameGuard);
                    march.setState(MarchState.Collect);// 采集

                    long l1 = staticFlameMine.getResource() - flameWarResource.getConvertRes();
                    long l2 = l1 / ((staticFlameMine.getCollectTime() / 60000) * 100);
                    march.setEndTime(l + l2 * 60 * 1000);
                    worldManager.synMarch(flameMap.getMapId(), march);
                    synAdd(flameWarResource);
                    isWin = false;
                } else {
                    collectId = MailId.COLLECT_REPORT;
                    mailId = MailId.ATK_COLLECT_FAIL;
                }
                worldLogic.handleCollectWar(collectId, def, player1, player, isWin, flameWarResource, l - first.getStartTime());
                playerManager.sendReportMail(player, battleMailMgr.createCollectWarReport(teamA, teamB, player, player1), battleMailMgr.createReportMsg(teamA, teamB), mailId, new ArrayList<Award>(), attackRec, param);
            }
        }
    }

    public void playerFly(Player player) {
        Pos oldPos = player.getPos();
        FlameMap flameMap = flameWarManager.getFlameMap();
        Pos randPos = flameMap.getSafePos(player.getCountry());
        if (randPos.isError()) {
            return;
        }

        int currentMapId = worldManager.getMapId(player);
        MapInfo currentMapInfo = worldManager.getMapInfo(currentMapId);
        PlayerCity playerCity = worldManager.changePlayerPos(player, randPos);

        if (playerCity != null) {
            synFlameEntity(playerCity, oldPos);
        }

    }

    public void synFlameEntity(Entity entity, Pos oldPos) {
        FlameMap flameMap = flameWarManager.getFlameMap();
        FlameWarPb.SynFlameEntityRq.Builder builder = FlameWarPb.SynFlameEntityRq.newBuilder();
        if (entity != null) {
            builder.setEntity(entity.wrapPb());
        }
        builder.setOldPos(CommonPb.Pos.newBuilder().setX(oldPos.getX()).setY(oldPos.getY()).build());
        flameMap.getPlayerCityMap().values().forEach(x -> {
            Player player1 = x.getPlayer();
            builder.setMaxMonsterLv(player1.getMaxMonsterLv());
            SynHelper.synMsgToPlayer(player1, SynFlameEntityRq.EXT_FIELD_NUMBER, SynFlameEntityRq.ext, builder.build());
//			player1.sendMsgToPlayer(FlameWarPb.SynFlameEntityRq.ext, builder.build(), FlameWarPb.SynFlameEntityRq.EXT_FIELD_NUMBER);
        });
    }

    /**
     * 建筑抢占 1秒轮询一次战斗
     */
    public void fight(FlameWarCity node, long currentTime) {
        try {
            if (node.getNodeState() == NodeState.NOT_OPEN) {
                return;
            }
            LinkedList<March> attackQueue = node.getAttackQueue();
            LinkedList<March> defenceQueue = node.getDefenceQueue();
            March attack = attackQueue.stream().filter(x -> x.getState() == MarchState.Waiting).findFirst().orElse(null);
            March def = defenceQueue.stream().filter(x -> x.getState() == MarchState.Waiting).findFirst().orElse(null);
            StaticFlameBuild staticFlameBuild1 = staticFlameWarMgr.getStaticFlameBuild(node.getId());

            LogHelper.CONFIG_LOGGER.info("建筑名字={}，进攻人数={}，防守人数={}", staticFlameBuild1.getName(), attackQueue.size(), defenceQueue.size());

            if (attack == null) {
                return;
            }
            if (attack != null && def == null) {
                // 当有进攻者 并且 防守着是空的 转换归属和状态
                node.setState(NodeState.ATTACK);
                node.setCountry(attack.getCountry());
                // node.setPlayerAwardList(null);
                StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(node.getId());
                if (staticFlameBuild != null) {

                    // 处理buff
                    double buff = getBuff(BuffType.buff_4, node.getCountry(), null);
                    long ouTime = staticFlameBuild.getOccupyTime();
                    if (buff != 0) {
                        ouTime = (long) (ouTime * (1 - buff));
                    }
                    node.setCapTime(currentTime + ouTime);// 攻占变成占领所需时间
                }
                if (!defenceQueue.isEmpty()) {
                    attackQueue.addAll(defenceQueue);
                    defenceQueue.clear();
                }
                Iterator<March> iterator = attackQueue.iterator();
                while (iterator.hasNext()) {
                    March next = iterator.next();
                    if (next.getCountry() == node.getCountry()) {
                        defenceQueue.add(next);
                        iterator.remove();
                    }
                }
                node.setAward(null);
                node.setPlayerAwardList(null);
                node.setOccupy(node.getCapTime());
                flameWarManager.synFlameBuild(node);
                node.setFirstTime(node.getCapTime());// 该阵营完全占领时刻
                synFlameBuildInfo(node, true);
                return;
            }
            Team teamA = handleSimple(attack);
            Team teamB = handleSimple(def);
            Random rand = new Random(System.currentTimeMillis());
            battleMgr.doTeamBattle(teamA, teamB, rand, ActPassPortTaskType.IS_WORLD_WAR);
            HashMap<Integer, Integer> attackRec = new HashMap<Integer, Integer>(4);
            atertBattle(teamA, attackRec, attackQueue, attack);
            HashMap<Integer, Integer> defRec = new HashMap<Integer, Integer>(4);
            atertBattle(teamB, defRec, defenceQueue, def);
            // 战报
            addWarReport(ReportType.REPORT_3, teamA, node, attack, def, currentTime);
            addWarReport(ReportType.REPORT_2, teamB, node, def, attack, currentTime);
            fight(node, currentTime);
        } catch (Exception e) {
            LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
        }
    }

    // 战斗战况
    public void addWarReport(int type, Team team, FlameWarCity node, March attack, March def, long currentTime) {
        FlameWarPb.FlameRealOptInfo.Builder builder = FlameWarPb.FlameRealOptInfo.newBuilder();
        builder.setType(type);
        builder.setHeroId(attack.getHeroIds().get(0));
        builder.setCityId((int) node.getId());
        builder.setKillNum(team.getKillNum());
        builder.setLostNum(team.getLost());
        Player defPlayer = playerManager.getPlayer(def.getLordId());
        builder.setEnemyNick(defPlayer.getNick());
        builder.setEnemyPos(defPlayer.getPos().wrapPb());
        builder.setEnemyHeroId(def.getHeroIds().get(0));
        builder.setEndTime(currentTime);
        Player player = playerManager.getPlayer(attack.getLordId());
        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
        flamePlayer.addReport(builder.build());
    }

    public Team handleSimple(March march) {
        Player player = playerManager.getPlayer(march.getLordId());
        Team playerTeam = battleMgr.initPlayerTeam(player, march.getHeroIds(), BattleEntityType.HERO);
        playerTeam.setTeamType(TeamType.PLAYER);
        playerTeam.setLordId(march.getLordId());
        playerTeam.setMarchId(march.getKeyId());
        return playerTeam;
    }

    public void atertBattle(Team team, HashMap<Integer, Integer> attackRec, LinkedList<March> marches, March march) {
        Player player = playerManager.getPlayer(team.getLordId());
        // 处理玩家扣血
        // HashMap<Integer, Integer> attackRec = new HashMap<Integer, Integer>(4);
        // 计算攻击方的血量
        worldManager.caculatePlayer(team, player, attackRec);
        playerManager.synChange(player, Reason.FLAME);
        if (!team.isWin() || team.getLessSoldier() <= 0) {
            handleMarchReturn(march, Reason.FLAME);
            worldManager.synMarch(0, march);
            if (marches != null) {
                marches.remove(march);
            }
        }
        // 累计杀敌
        calKill(player, team);
    }

    public void handleMarchReturn(March march, int reason) {
        // 回城
        march.setState(MarchState.FightOver);
        // 开始掉头
        march.swapPos(reason);
        long lordId = march.getLordId();
        Player player = playerManager.getPlayer(lordId);

        // 兵书对行军的影响值
        List<Integer> heroIds = march.getHeroIds();
        float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);

        long period = flameWarManager.getPeriod(player, march.getEndPos(), march.getStartPos(), bookEffectMarch);
        period = worldManager.checkPeriod(march, period);

        march.setPeriod(period);
        march.setEndTime(System.currentTimeMillis() + period);
    }

    public void calKill(Player player, Team team) {
        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
        flamePlayer.addKill(team.getKillNum());
    }

    public void bagHandler(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
        Map<Integer, Item> prop = flamePlayer.getProp();
        FlameWarPb.FlameBagRs.Builder builder = FlameWarPb.FlameBagRs.newBuilder();
        prop.values().forEach(x -> {
            builder.addProp(x.wrapPb());
        });
        handler.sendMsgToPlayer(FlameWarPb.FlameBagRs.ext, builder.build());
    }

    public void receiveHandler(FlameWarPb.ReceiveBuildAwardRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
        if (worldActPlan == null || worldActPlan.getState() != WorldActPlanConsts.OPEN) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        int buildId = rq.getBuildId();
        StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(buildId);
        if (staticFlameBuild == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        FlameMap flameMap = flameWarManager.getFlameMap();
        FlameWarCity flameWarCity = flameMap.getCityNode().get((long) buildId);
        if (flameWarCity == null || flameWarCity.getState() != NodeState.CAPTURE || flameWarCity.getCountry() != player.getCountry()) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        CommonPb.Award award = flameWarCity.getAward();
        List<Long> playerAwardList = flameWarCity.getPlayerAwardList();
        if (award == null || playerAwardList == null || playerAwardList.contains(player.roleId)) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
        Map<Integer, Item> prop = flamePlayer.getProp();
        Item item = prop.get(award.getId());
        if (item != null) {
            item.setItemNum(award.getCount() + item.getItemNum());
        } else {
            item = new Item(award.getId(), award.getCount());
            prop.put(award.getId(), item);
        }
        playerAwardList.add(flamePlayer.getRoleId());
        FlameWarPb.ReceiveBuildAwardRs.Builder builder = FlameWarPb.ReceiveBuildAwardRs.newBuilder();
        builder.setAward(award);
        prop.values().forEach(x -> {
            builder.addProp(x.wrapPb());
        });
        handler.sendMsgToPlayer(FlameWarPb.ReceiveBuildAwardRs.ext, builder.build());
        flameWarManager.synFlameBuild(flameWarCity);
    }

    public void logOutMap(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (!player.getMarchList().isEmpty()) {
            handler.sendErrorMsgToPlayer(GameError.MARCH_STATE_ERROR);
            return;
        }
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData == null) {
            return;
        }
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
        if (worldActPlan != null && worldActPlan.getState() == WorldActPlanConsts.OPEN) {
            FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
            int num = staticLimitMgr.getNum(SimpleId.FIRE_OUT_TIME);
            flamePlayer.setNextEnterTime(System.currentTimeMillis() + num);// 如果是活动期间退出
        }
        MapInfo newMapInfo = worldManager.getMapInfo(MapId.CENTER_MAP_ID);
        Pos randPos = worldManager.givePlayerPos(newMapInfo);
        //if (randPos.isError() || !newMapInfo.isFreePos(randPos)) {
        //	handler.sendErrorMsgToPlayer(GameError.CANNOT_MAP_MOVE);
        //	return;
        //}
        // 通知退出活动
        FlameMap flameMap = flameWarManager.getFlameMap();
        Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
        PlayerCity playerCity1 = playerCityMap.get(player.getPos());
        if (playerCity1 != null) {
            synFlameEntity(null, player.getPos());
            worldManager.synRemoveAllPvpWar(player, flameMap);
			PlayerCity playerCity = worldManager.changePlayerPos(player, randPos);
			//worldManager.removePlayerCity(player.getPos(), flameMap);
            //playerManager.changePlayerPos(player, randPos);
            //PlayerCity playerCity = worldManager.addPlayerCity(randPos, newMapInfo, player);
            if (playerCity != null) {
                worldManager.synEntityRq(playerCity, newMapInfo.getMapId(), player.getOldPos()); // 同步城池
            }
        }
        // 显示在大地图块

        FlameWarPb.FlameLogOutRs.Builder builder = FlameWarPb.FlameLogOutRs.newBuilder();
        builder.setPos(player.getPos().wrapPb());
        handler.sendMsgToPlayer(FlameWarPb.FlameLogOutRs.ext, builder.build());
        player.getPushPos().clear();
        heroManager.synBattleScoreAndHeroList(player, player.getAllHeroList());
    }

    public void flameBuildInfo(FlameWarPb.FlameBuildInfoRq req, ClientHandler handler) {

        Player player1 = playerManager.getPlayer(handler.getRoleId());
        int buildId = req.getBuildId();
        FlameWarCity cityNode = flameWarManager.getFlameMap().getCityNode().get((long) buildId);
        if (cityNode == null) {
            return;
        }
        StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(buildId);
        if (staticFlameBuild == null) {
            return;
        }
        int type = req.getType();
        FlameWarPb.FlameBuildInfoRs.Builder builder = FlameWarPb.FlameBuildInfoRs.newBuilder();
        builder.setBuildId(buildId);
        builder.setType(type);
        if (type == 1) {
            FlameWarPb.FlameBuildInfo.Builder builder1 = FlameWarPb.FlameBuildInfo.newBuilder();
            builder1.setCountry(cityNode.getFirstCountry());
            builder1.setTime(cityNode.getFirstTime());
            HashSet<Long> first = cityNode.getFirst();
            first.forEach(x -> {
                Player player = playerManager.getPlayer(x);
                if (player != null) {
                    FlameWarPb.FlameFirstTeam.Builder builder2 = FlameWarPb.FlameFirstTeam.newBuilder();
                    builder2.setCountry(player.getCountry());
                    builder2.setNick(player.getNick());
                    builder2.setResource(staticFlameBuild.getFirstPerson());
                    builder1.addFlameFirstTeam(builder2);
                }
            });
            builder.setFlameBuildInfo(builder1);
        } else {
            FlameWarPb.FlameWarInfo.Builder builder1 = FlameWarPb.FlameWarInfo.newBuilder();
            builder1.setCountry(cityNode.getFirstCountry());
            builder1.setTime(cityNode.getFirstTime());
            builder1.setAttNum(cityNode.getAttackQueue().size());
            builder1.setDefNum(cityNode.getDefenceQueue().size());
            LinkedList<March> attackQueue = cityNode.getAttackQueue();
            attackQueue.forEach(x -> {
                FlameWarPb.FlameWarTeam.Builder builder2 = FlameWarPb.FlameWarTeam.newBuilder();
                builder2.setCountry(x.getCountry());
                Player player = playerManager.getPlayer(x.getLordId());
                builder2.setNick(player.getNick());
                Integer heroId = x.getHeroIds().get(0);
                builder2.setHeroId(heroId);
                Hero hero = player.getHero(heroId);
                if (hero != null) {
                    builder2.setDiviNum(hero.getDiviNum());
                }
                builder2.setEndTime(x.getEndTime());
                // int power = heroManager.calHeroBattleScore(player, x.getHeroIds());
                builder2.setFight(hero.getCurrentSoliderNum());
                builder1.addAttTeam(builder2);
            });

            LinkedList<March> defenceQueue = cityNode.getDefenceQueue();
            defenceQueue.forEach(x -> {
                FlameWarPb.FlameWarTeam.Builder builder2 = FlameWarPb.FlameWarTeam.newBuilder();
                builder2.setCountry(x.getCountry());
                Player player = playerManager.getPlayer(x.getLordId());
                builder2.setNick(player.getNick());
                Integer heroId = x.getHeroIds().get(0);
                builder2.setHeroId(heroId);
                Hero hero = player.getHero(heroId);
                if (hero != null) {
                    builder2.setDiviNum(hero.getDiviNum());
                }
                builder2.setEndTime(x.getEndTime());
                // int power = heroManager.calHeroBattleScore(player, x.getHeroIds());
                builder2.setFight(hero.getCurrentSoliderNum());
                builder1.addDefTeam(builder2);
            });
            builder.setFlameWarInfo(synFlameBuildInfo(cityNode, false));

        }
        handler.sendMsgToPlayer(FlameWarPb.FlameBuildInfoRs.ext, builder.build());
        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player1);
        flamePlayer.setNextSyn(System.currentTimeMillis() + 3 * 1000);
        flamePlayer.setStyId(buildId);
    }

    public FlameWarPb.FlameWarInfo synFlameBuildInfo(FlameWarCity cityNode, boolean flag) {
        FlameWarPb.FlameWarInfo.Builder builder1 = FlameWarPb.FlameWarInfo.newBuilder();
        builder1.setCountry(cityNode.getFirstCountry());
        builder1.setTime(cityNode.getFirstTime());
        builder1.setAttNum(cityNode.getAttackQueue().size());
        builder1.setDefNum(cityNode.getDefenceQueue().size());
        LinkedList<March> attackQueue = cityNode.getAttackQueue();
        attackQueue.forEach(x -> {
            FlameWarPb.FlameWarTeam.Builder builder2 = FlameWarPb.FlameWarTeam.newBuilder();
            builder2.setCountry(x.getCountry());
            Player player = playerManager.getPlayer(x.getLordId());
            builder2.setNick(player.getNick());
            Integer heroId = x.getHeroIds().get(0);
            builder2.setHeroId(heroId);
            Hero hero = player.getHero(heroId);
            if (hero != null) {
                builder2.setDiviNum(hero.getDiviNum());
            }
            builder2.setEndTime(x.getEndTime());
            // int power = heroManager.calHeroBattleScore(player, x.getHeroIds());
            builder2.setFight(hero.getCurrentSoliderNum());
            builder1.addAttTeam(builder2);
        });

        LinkedList<March> defenceQueue = cityNode.getDefenceQueue();
        defenceQueue.forEach(x -> {
            FlameWarPb.FlameWarTeam.Builder builder2 = FlameWarPb.FlameWarTeam.newBuilder();
            builder2.setCountry(x.getCountry());
            Player player = playerManager.getPlayer(x.getLordId());
            builder2.setNick(player.getNick());
            Integer heroId = x.getHeroIds().get(0);
            builder2.setHeroId(heroId);
            Hero hero = player.getHero(heroId);
            if (hero != null) {
                builder2.setDiviNum(hero.getDiviNum());
            }
            builder2.setEndTime(x.getEndTime());
            // int power = heroManager.calHeroBattleScore(player, x.getHeroIds());
            builder2.setFight(hero.getCurrentSoliderNum());
            builder1.addDefTeam(builder2);
        });
        if (flag) {
            FlameWarPb.SynFlameBuildInfo.Builder builder = FlameWarPb.SynFlameBuildInfo.newBuilder();
            builder.setFlameWarInfo(builder1);
            FlameMap flameMap = flameWarManager.getFlameMap();
            Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
            playerCityMap.values().forEach(x -> {
                Player player = x.getPlayer();
                FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
                long l = TimeHelper.curentTime();
                if (flamePlayer.getStyId() == cityNode.getId() && l > flamePlayer.getNextSyn()) {
                    SynHelper.synMsgToPlayer(player, SynFlameBuildInfo.EXT_FIELD_NUMBER, SynFlameBuildInfo.ext, builder.build());
//					player.sendMsgToPlayer(FlameWarPb.SynFlameBuildInfo.ext, builder.build(), FlameWarPb.SynFlameBuildInfo.EXT_FIELD_NUMBER);
                    flamePlayer.setNextSyn(l + 3 * 1000);
                }
            });
        }
        return builder1.build();
    }

    public void flameBuildInfo(FlameWarPb.FlameLoadAllBuildRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        FlameWarPb.FlameLoadAllBuildRs.Builder builder = FlameWarPb.FlameLoadAllBuildRs.newBuilder();
        FlameMap flameMap = flameWarManager.getFlameMap();
        switch (rq.getType()) {
            case 1:
                Map<Long, FlameWarCity> cityNode = flameMap.getCityNode();
                cityNode.values().forEach(x -> {
                    CommonPb.WorldEntity.Builder builder1 = x.wrapPb();
                    builder1.setAllOwnTime(x.getOccupy());
                    builder1.setIsCanJoinAttack(0);
                    if (x.getCountry() == player.getCountry()) {
                        if (!x.getAttackQueue().isEmpty() || !x.getDefenceQueue().isEmpty()) {
                            builder1.setIsCanJoinAttack(1);
                        }
                    } else {
                        March march = x.getAttackQueue().stream().filter(z -> z.getCountry() == player.getCountry()).findAny().orElse(null);
                        if (march != null) {
                            builder1.setIsCanJoinAttack(2);
                        }
                    }
                    builder1.setReceive(2);
                    if (x.getCountry() == player.getCountry() && x.getAward() != null && !x.getPlayerAwardList().contains(player.getRoleId())) {
                        builder1.setReceive(1);
                    }
                    builder.addWorldEntity(builder1);
                });
                break;
            case 2:
                Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
                playerCityMap.values().forEach(x -> {
                    builder.addWorldEntity(x.wrapPb());
                });
                break;
            case 3:
                Map<Pos, FlameWarResource> resourceNode = flameMap.getResourceNode();
                resourceNode.values().forEach(x -> {
                    builder.addWorldEntity(x.wrapPb());
                });
                break;
        }

        handler.sendMsgToPlayer(FlameWarPb.FlameLoadAllBuildRs.ext, builder.build());
    }

    public void getResInfo(FlameWarPb.FlameResInfoRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        CommonPb.Pos reqPos = req.getPos();
        // 检查是否有资源点
        Pos pos = new Pos(reqPos.getX(), reqPos.getY());
        FlameWarResource resource = flameWarManager.getFlameMap().getResourceNode().get(pos);
        if (resource == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_RESOURCE_ENTITY);
            return;
        }
        FlameWarPb.FlameResInfoRs.Builder builder = FlameWarPb.FlameResInfoRs.newBuilder();
        ConcurrentLinkedDeque<FlameGuard> collectArmy = resource.getCollectArmy();
        builder.setLeftRes((int) resource.leftRes());
        if (collectArmy.isEmpty()) {
            builder.setLordId(0);
        } else {
            FlameGuard first = collectArmy.getFirst();
            March march = first.getMarch();
            if (march != null) {
                Player target = playerManager.getPlayer(march.getLordId());
                if (target != null) {
                    builder.setCountry(target.getCountry());
                    builder.setLordLv(target.getLevel());
                    builder.setName(target.getNick());
                    List<Integer> heroIds = march.getHeroIds();
                    if (heroIds != null && heroIds.size() == 1) {
                        Integer heroId = heroIds.get(0);
                        Hero hero = target.getHero(heroId);
                        if (hero != null) {
                            builder.setHeroLv(hero.getHeroLv());
                            builder.setHeroId(heroId);
                            builder.setSoldier(hero.getCurrentSoliderNum());
                            builder.setDiviNum(hero.getDiviNum());
                        }
                    }
                    builder.setLordId(target.roleId);
                    builder.setCollectEndTime(march.getEndTime());
                    builder.setPeriod(march.getPeriod());
                    builder.setPortrait(target.getPortrait());
                    builder.setRes(first.getTotalRes());
                }
            }
        }
        handler.sendMsgToPlayer(FlameWarPb.FlameResInfoRs.ext, builder.build());
    }

    public void fightHelp(FlameWarPb.FlameFightHelpRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        FlameWarPb.FlameFightHelpRs.Builder builder = FlameWarPb.FlameFightHelpRs.newBuilder();
        if (player.getFlameChatTime() > TimeHelper.curentTime()) {
            builder.setNextChatTime(player.getFlameChatTime());
            handler.sendMsgToPlayer(FlameWarPb.FlameFightHelpRs.ext, builder.build());
            return;
        }
        FlameMap flameMap = flameWarManager.getFlameMap();
        FlameWarCity flameWarCity = flameMap.getCityNode().get(req.getBuildId());
        if (flameWarCity != null) {
            Pos pos = flameWarCity.getPos();
            String tarPos = String.format("%s,%s", pos.getX(), pos.getY());
            String p[] = {String.valueOf(flameWarCity.getId()), tarPos};
            int chatId = ChatId.FLAME_HELP_ATT;
            if (flameWarCity.getCountry() == player.getCountry()) {
                chatId = ChatId.FLAME_HELP_DEF;
            }
            Chat chat = chatManager.createManShare(player, chatId, p);
            chatManager.sendCountryShare(player.getCountry(), chat);
        }

        handler.sendMsgToPlayer(FlameWarPb.FlameFightHelpRs.ext, builder.build());
        player.setFlameChatTime(TimeHelper.curentTime() + TimeHelper.MINUTE_MS);
    }

    public void flameFlameRealWarInfo(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
        FlameWarPb.FlameRealWarInfoRs.Builder builder = FlameWarPb.FlameRealWarInfoRs.newBuilder();
        long killRes = staticFlameWarMgr.getStaticFlameKill(flamePlayer.getKill());
        builder.setCurPerRes(flamePlayer.getResource() + killRes);
        builder.setTotalRes(flamePlayer.getFirstResource());
        builder.setCurTotalKill(flamePlayer.getKill());
        builder.addAllFlameRealOptInfo(flamePlayer.getReports());

        FlameMap flameMap = flameWarManager.getFlameMap();
        Map<Long, FlameWarCity> cityNode = flameMap.getCityNode();
        long count = 0;
        List<FlameWarCity> collect = cityNode.values().stream().filter(x -> x.getCountry() == player.getCountry() && x.getState() == NodeState.CAPTURE).collect(Collectors.toList());
        for (FlameWarCity flameWarCity : collect) {
            LinkedList<March> defenceQueue = flameWarCity.getDefenceQueue();
            March march = defenceQueue.stream().filter(x -> x.getLordId() == player.getRoleId() && x.getState() == MarchState.Waiting).findAny().orElse(null);
            if (march != null) {
                StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(flameWarCity.getId());
                if (staticFlameBuild != null) {
                    count += staticFlameBuild.getContinuePersonAmount();
                }
            }
        }
        Map<Pos, FlameWarResource> resourceNode = flameMap.getResourceNode();
        List<FlameWarResource> collect1 = resourceNode.values().stream().filter(x -> x.getCollectArmy() != null && !x.getCollectArmy().isEmpty() && player.getMarchList().contains(x.getCollectArmy().getFirst().getMarch())).collect(Collectors.toList());
        for (FlameWarResource resource : collect1) {
            StaticFlameMine staticFlameMine = staticFlameWarMgr.getStaticFlameMine(resource.getResId());
            if (staticFlameMine != null) {
                count += staticFlameMine.getAmount();
            }
        }
        builder.setCurYield(count);
        handler.sendMsgToPlayer(FlameWarPb.FlameRealWarInfoRs.ext, builder.build());
    }

    public void getRank(FlameWarPb.FlameRankRq req, ClientHandler handler) {
        ReadWriteLock lock = FlameWarManager.lock;
        lock.readLock().lock();
        try {
            Player player = playerManager.getPlayer(handler.getRoleId());
            int type = req.getType();

            FlameWarPb.FlameRankRs.Builder builder = FlameWarPb.FlameRankRs.newBuilder();
            List<FlameCountry> rankCountry = flameWarManager.getRankCountry();
            for (int i = 0; i < rankCountry.size(); i++) {
                FlameCountry flameCountry = rankCountry.get(i);
                FlameWarPb.CountrySort.Builder builder1 = FlameWarPb.CountrySort.newBuilder();
                builder1.setCountry(flameCountry.getCountry());
                StaticFlameRankCamp staticFlameRankCamp = staticFlameWarMgr.getStaticFlameRankCamp(i + 1);
                if (staticFlameRankCamp != null) {
                    builder1.setAward(staticFlameRankCamp.getAward());
                }
                builder.addCountrySort(builder1);
            }
            List<FlamePlayer> rankFlamePlayer;
            if (type == 1) {
                rankFlamePlayer = flameWarManager.getRankFlamePlayer(player.getCountry());
            } else {
                rankFlamePlayer = flameWarManager.getAllRankPlayer();
            }
            if (rankFlamePlayer != null && !rankFlamePlayer.isEmpty()) {
                FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
                int index = rankFlamePlayer.indexOf(flamePlayer);
                FlameWarPb.FlameRankInfo.Builder my = FlameWarPb.FlameRankInfo.newBuilder();
                my.setCountry(player.getCountry());
                my.setRank(index);
                if (index >= 0) {
                    my.setRank(index + 1);
                }
                if (flamePlayer.getResource() <= 0) {
                    my.setRank(0);
                }
                my.setNick(player.getNick());
                my.setLordId(player.getRoleId());
                my.setRes(flamePlayer.getRankResource());
                StaticFlameRankGear flameRankGears = staticFlameWarMgr.getFlameRankGears(flamePlayer.getResource());
                if (flameRankGears != null) {
                    my.setCoin(flameRankGears.getAward());
                }
                builder.setMyRank(my);

                int size = 10;
                int page = req.getPage();// 当前页
                int begin = (page - 1) * size;
                int end = page * size;
                List<FlamePlayer> list = new ArrayList<>();
                if (rankFlamePlayer.size() >= end) {
                    list = rankFlamePlayer.subList(begin, end);
                }
                if (rankFlamePlayer.size() < end && rankFlamePlayer.size() > begin) {
                    list = rankFlamePlayer.subList(begin, rankFlamePlayer.size());
                }
                list = list.stream().filter(x -> x.getRankResource() > 0).collect(Collectors.toList());
                if (!list.isEmpty()) {
                    for (FlamePlayer flamePlayer1 : list) {
                        begin++;
                        Player player1 = playerManager.getPlayer(flamePlayer1.getRoleId());
                        FlameWarPb.FlameRankInfo.Builder builder1 = FlameWarPb.FlameRankInfo.newBuilder();
                        builder1.setCountry(player1.getCountry());
                        builder1.setRank(begin);
                        builder1.setNick(player1.getNick());
                        builder1.setLordId(player1.getRoleId());
                        builder1.setRes(flamePlayer1.getRankResource());
                        StaticFlameRankGear flameRankGears1 = staticFlameWarMgr.getFlameRankGears(flamePlayer1.getResource());
                        if (flameRankGears1 != null) {
                            builder1.setCoin(flameRankGears1.getAward());
                        }
                        builder.addFlameRankInfo(builder1);
                    }

                }
                builder.setTotalSize(rankFlamePlayer.size());
                builder.setPage(page);
            }
            handler.sendMsgToPlayer(FlameWarPb.FlameRankRs.ext, builder.build());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void flameRankRes(FlameWarPb.FlameResRankRq req, ClientHandler handler) {
        ReadWriteLock lock = FlameWarManager.lock;
        lock.readLock().lock();
        try {
            FlameWarPb.FlameResRankRs.Builder builder = FlameWarPb.FlameResRankRs.newBuilder();
            List<FlameCountry> rankCountry = flameWarManager.getRankCountry();
            // Map<Long, FlameWarCity> cityNode = flameWarManager.getFlameMap().getCityNode();
            for (int i = 0; i < rankCountry.size(); i++) {
                FlameCountry flameCountry = rankCountry.get(i);
                FlameWarPb.CountrySort.Builder builder1 = FlameWarPb.CountrySort.newBuilder();
                builder1.setCountry(flameCountry.getCountry());
                StaticFlameRankCamp staticFlameRankCamp = staticFlameWarMgr.getStaticFlameRankCamp(i + 1);
                if (staticFlameRankCamp != null) {
                    builder1.setAward(staticFlameRankCamp.getAward());
                }
                builder.addCountrySort(builder1);

                FlameWarPb.FlameCountryResInfo.Builder builder2 = FlameWarPb.FlameCountryResInfo.newBuilder();
                builder2.setCountry(flameCountry.getCountry());
                Map<Integer, List<FlameWarCity>> countryLevel = flameWarManager.getCountryLevel(flameCountry.getCountry());
                for (Map.Entry<Integer, List<FlameWarCity>> integerListEntry : countryLevel.entrySet()) {
                    FlameWarPb.FlameCityResInfo.Builder builder3 = FlameWarPb.FlameCityResInfo.newBuilder();
                    builder3.setCityLv(integerListEntry.getKey());
                    List<FlameWarCity> value = integerListEntry.getValue();
                    builder3.setCityNum(value.size());
                    long count = 0;
                    for (FlameWarCity flameWarCity : value) {
                        StaticFlameBuild staticFlameBuild = staticFlameWarMgr.getStaticFlameBuild(flameWarCity.getId());
                        if (staticFlameBuild != null) {
                            count += staticFlameBuild.getExtraCamp();
                        }
                    }
                    builder3.setCityResource(count);
                    builder2.addFlameCityResInfo(builder3);

                }
                builder.addFlameCountryResInfo(builder2);
            }
            handler.sendMsgToPlayer(FlameWarPb.FlameResRankRs.ext, builder.build());
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getBuff(int buffType, int country, Player player) {
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData == null) {
            return 0;
        }
        WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_15);
        if (worldActPlan == null || worldActPlan.getState() != WorldActPlanConsts.OPEN) {
            return 0;
        }
        if (player != null) {
            FlameMap flameMap = flameWarManager.getFlameMap();
            Map<Pos, PlayerCity> playerCityMap = flameMap.getPlayerCityMap();
            PlayerCity playerCity = playerCityMap.get(player.getPos());
            if (playerCity == null || playerCity.getPlayer() != player) {
                return 0;
            }
        }
        int add = 0;
        List<FlameWarCity> collect = flameWarManager.getCaptureBuild(country);
        for (FlameWarCity flameWarCity : collect) {
            StaticFlameBuild staticFlameBuild1 = staticFlameWarMgr.getStaticFlameBuild(flameWarCity.getId());
            if (staticFlameBuild1 != null) {
                List<List<Integer>> buff = staticFlameBuild1.getBuff();
                if (buff != null) {
                    for (List<Integer> list : buff) {
                        if (list.get(0) == buffType) {
                            add += list.get(1);
                        }
                    }
                }
            }
        }
        if (player != null) {
            FlamePlayer flamePlayer = flameWarManager.getFlamePlayer(player);
            Map<Integer, Buff> buff = flamePlayer.getBuff();
            for (Buff value : buff.values()) {
                StaticFlameBuff buffById = staticFlameWarMgr.getBuffById(value.getBuffId());
                if (buffById != null) {
                    List<List<Integer>> effect = buffById.getEffect();
                    if (effect != null) {
                        for (List<Integer> list : effect) {
                            if (list.get(0) == buffType) {
                                add += list.get(1);
                            }
                        }
                    }
                }
            }
        }
        return add / DevideFactor.PERCENT_NUM;
    }

}
