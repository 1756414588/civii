package com.game.service;

import com.game.chat.domain.Chat;
import com.game.constant.ChatId;
import com.game.constant.GameError;
import com.game.constant.Reason;
import com.game.dataMgr.StaticFishMgr;
import com.game.dataMgr.StaticHeroMgr;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.p.Hero;
import com.game.domain.s.*;
import com.game.manager.ChatManager;
import com.game.manager.HeroManager;
import com.game.manager.PlayerManager;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.*;
import com.game.pb.FishingPb.*;
import com.game.server.GameServer;
import com.game.util.*;
import com.game.util.random.WeightRandom;
import com.game.worldmap.FishPusher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;


/**
 * 渔场
 */
@Service
public class FishingService {

    @Autowired
    private StaticFishMgr staticFishMgr;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private ChatManager chatManager;

    @Autowired
    private StaticHeroMgr staticHeroDataMgr;

    @Autowired
    private HeroManager heroManager;

    private Map<Long, FishPusher> pushedMap = new ConcurrentHashMap<>();

    /**
     * HeroId  HeroType 互转
     */
    public List<Integer> heroIdToHeroType(List<Integer> raw, Player player, Integer type) {
        List<Integer> list = new ArrayList<>();
        if (type == 1) {
            // HeroId -> HeroType
            for (int heroId : raw) {
                list.add(staticHeroDataMgr.getHeroType(heroId));
            }
        } else if (type == 2) {
            // HeroType -> HeroId
            for (int heroType : raw) {
                list.add(heroManager.getHeroIdByType(heroType, player));
            }
        }

        return list;
    }

    /**
     * 鱼类图鉴配置
     */
    public void getReachFishAtlasRq(GetReachFishAtlasRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        PlayerFishingData fishingData = player.getPlayerFishingData();

        GetReachFishAtlasRs.Builder rsBuilder = GetReachFishAtlasRs.newBuilder();

        Map<Integer, ReachFishRecord> reachFishRecords = fishingData.getReachFishRecords();
        for (ReachFishRecord reachFishRecord : reachFishRecords.values()) {
            reachFishRecord.encode();
            rsBuilder.addReachFishRecord(reachFishRecord.encode().build());
        }

        handler.sendMsgToPlayer(GetReachFishAtlasRs.ext, rsBuilder.build());
    }

    /**
     * 鱼饵图鉴配置
     */
    public void getReachBaitAtlasRq(GetReachBaitAtlasRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        PlayerFishingData fishingData = player.getPlayerFishingData();

        GetReachBaitAtlasRs.Builder rsBuilder = GetReachBaitAtlasRs.newBuilder();

        Map<Integer, ReachBaitRecord> reachBaitRecords = fishingData.getReachBaitRecords();
        for (ReachBaitRecord reachBaitRecord : reachBaitRecords.values()) {
            reachBaitRecord.encode();
            rsBuilder.addReachBaitRecord(reachBaitRecord.encode().build());
        }

        handler.sendMsgToPlayer(GetReachBaitAtlasRs.ext, rsBuilder.build());
    }

    /**
     * 英雄组合配置
     */
    public void getHeroGroupConfigRq(GetHeroGroupConfigRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        GetHeroGroupConfigRs.Builder rsBuilder = GetHeroGroupConfigRs.newBuilder();
        Map<Integer, StaticFishHeroGroup> heroGroupMap = staticFishMgr.getFishHeroGroupMap();

        for (StaticFishHeroGroup heroGroup : heroGroupMap.values()) {
            HeroGroupPB.Builder heroGroupBuilder = HeroGroupPB.newBuilder();
            heroGroupBuilder.setId(heroGroup.getId());
            heroGroupBuilder.setName(heroGroup.getName());
            // 此处发的是前端需要的结构
            heroGroupBuilder.addAllHeroIds(heroGroup.getCombination());
//            heroGroupBuilder.addAllHeroIds(heroIdToHeroType(heroGroup.getHeroIds(),player,2));

            for (StaticGroupToBaitProbability probability : heroGroup.getGroupToBaitProbability().values()) {
                GroupToBaitProbabilityPB.Builder builder = GroupToBaitProbabilityPB.newBuilder();
                builder.setBaitId(probability.getBaitId());
                builder.setProbability(probability.getProbability());
                heroGroupBuilder.addGroupToBaitProbability(builder.build());
            }

            rsBuilder.addHeroGroups(heroGroupBuilder.build());
        }

        handler.sendMsgToPlayer(GetHeroGroupConfigRs.ext, rsBuilder.build());
    }

    /**
     * 钓鱼等级配置
     */
    public void getFishingLevelConfigRq(GetFishingLevelConfigRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        GetFishingLevelConfigRs.Builder rsBuilder = GetFishingLevelConfigRs.newBuilder();
        Map<Integer, StaticFishLv> fishingLv = staticFishMgr.getFishLvMap();

        for (StaticFishLv fishLv : fishingLv.values()) {
            FishingLevelPB.Builder fishLvBuilder = FishingLevelPB.newBuilder();
            fishLvBuilder.setId(fishLv.getId());
            fishLvBuilder.setLv(fishLv.getLv());
            fishLvBuilder.setName(fishLv.getName());
            fishLvBuilder.setExp(fishLv.getExp());
            fishLvBuilder.setDesc(fishLv.getDesc());
            fishLvBuilder.setSpeed(fishLv.getSpeed());

            for (StaticFishLevelCritBuff critBuff : fishLv.getHit().values()) {
                LevelCritBuffPB.Builder critBuilder = LevelCritBuffPB.newBuilder();
                critBuilder.setMultiple(critBuff.getMultiple());
                critBuilder.setProbability(critBuff.getProbability());
                fishLvBuilder.addLevelCritBuff(critBuilder.build());
            }

            StaticFishLevelSizeBuff sizeBuff = fishLv.getSize();
            LevelSizeBuffPB.Builder sizeBuilder = LevelSizeBuffPB.newBuilder();
            sizeBuilder.setMin(sizeBuff.getMin());
            sizeBuilder.setMax(sizeBuff.getMax());

            fishLvBuilder.setLevelSizeBuff(sizeBuilder.build());

            rsBuilder.addFishLevel(fishLvBuilder.build());
        }
        handler.sendMsgToPlayer(GetFishingLevelConfigRs.ext, rsBuilder.build());
    }

    /**
     * 积分商店配置
     */
    public void getPointsShopConfigRq(GetPointsShopConfigRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        PlayerFishingData data = player.getPlayerFishingData();
        Map<Integer, FishCostRecord> costRecords = data.getFishCostRecords();

        GetPointsShopConfigRs.Builder rsBuilder = GetPointsShopConfigRs.newBuilder();

        Map<Integer, StaticFishShop> fishShopMap = staticFishMgr.getFishShopMap();
        for (StaticFishShop fishShop : fishShopMap.values()) {
            PointsShopItemPB.Builder itemBuilder = PointsShopItemPB.newBuilder();
            itemBuilder.setId(fishShop.getId());
            itemBuilder.setCost(fishShop.getCost());
            itemBuilder.setLimit(fishShop.getLimit());
            if (costRecords != null && costRecords.containsKey(fishShop.getId())) {
                itemBuilder.setTimes(costRecords.get(fishShop.getId()).getCount());
            } else {
                itemBuilder.setTimes(0);
            }
            List<Integer> award = fishShop.getProp();
            itemBuilder.addAward(PbHelper.createAward(award.get(0), award.get(1), award.get(2)));
            rsBuilder.addShopItem(itemBuilder.build());
        }
        rsBuilder.setPoints(data.getPoints());
        handler.sendMsgToPlayer(GetPointsShopConfigRs.ext, rsBuilder.build());
    }

    /**
     * 玩家钓鱼相关数据
     */
    public void getPlayerFishingDataRq(GetPlayerFishingDataRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        GetPlayerFishingDataRs.Builder rsBuilder = GetPlayerFishingDataRs.newBuilder();
        PlayerFishingData data = player.getPlayerFishingData();

        if (data.getTeamQueue().size() == 0) {
            // LogHelper.MESSAGE_LOGGER.info("初始化钓鱼数据");
            initData(player);
        }
        playerManager.refFishing(player);
        rsBuilder.setLevel(data.getLevel());
        rsBuilder.setExp(data.getExp());
        rsBuilder.setPoints(data.getPoints());

        // LogHelper.MESSAGE_LOGGER.info("玩家钓鱼基本数据 level: {} exp :{} points: {} baits: {}", data.getLevel(), data.getExp(), data.getPoints(), data.getBaits());

        Map<Integer, PlayerBaits> baitsMap = data.getBaits();
        for (PlayerBaits bait : baitsMap.values()) {
            // LogHelper.MESSAGE_LOGGER.info("玩家包里的鱼饵 baitId: {}  count: {}", bait.getBaitId(), bait.getCount());
            rsBuilder.addBaits(bait.encode().build());
        }
        data.setBaits(baitsMap);

        handler.sendMsgToPlayer(GetPlayerFishingDataRs.ext, rsBuilder.build());
    }

    /**
     * 渔场采集队列数据
     */
    public void GetFishingTeamQueueRq(GetFishingTeamQueueRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        GetFishingTeamQueueRs.Builder rsBuilder = GetFishingTeamQueueRs.newBuilder();
        PlayerFishingData data = player.getPlayerFishingData();
        // 队列状态 0=未上阵 1=未派遣 2=派遣中 3=已派遣
        Map<Integer, TeamQueue> teamQueues = data.getTeamQueue();
        for (TeamQueue team : teamQueues.values()) {
            List<Integer> heroList = heroManager.checkHeroChange(player, team.getUsedHeroId());
            rsBuilder.addTeamQueue(team.encode(heroList).build());
        }
        handler.sendMsgToPlayer(GetFishingTeamQueueRs.ext, rsBuilder.build());
    }

    /**
     * 采集鱼饵界面--英雄上阵下阵
     */
    public void pickHeroRq(PickHeroRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        List<Integer> heroIds = req.getHeroIdList();
        int teamId = req.getTeamId();
        int action = req.getAction();
        // 打印参数
        // LogHelper.MESSAGE_LOGGER.info("pickHeroRq, roleId={}, teamId={}, action={}, heroIds={}", handler.getRoleId(), teamId, action, heroIds);
        // 判断heroIds是否合法
        if (heroIds.size() != 4 || teamId < 1 || teamId > 5) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        // 检查英雄是否存在
        Map<Integer, Hero> heros = player.getHeros();
        // 判断玩家是否拥有这些英雄
        for (int heroId : heroIds) {
            if (heroId == 0) {
                continue;
            }
            if (!heros.containsKey(heroId)) {
                handler.sendErrorMsgToPlayer(GameError.HERO_NOT_EXISTS);
                return;
            }
        }
        Map<Integer, StaticFishHeroGroup> heroGroupMap = staticFishMgr.getFishHeroGroupMap();
        PlayerFishingData data = player.getPlayerFishingData();
        Map<Integer, TeamQueue> teamQueueMap = data.getTeamQueue();
        TeamQueue teamQueue = teamQueueMap.get(teamId);
        if (teamQueue == null) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_DATA_ERROR);
            return;
        }
        // 如果队列当前状态为2,则不能进行上下阵操作
        if (teamQueue.getStatus() >= 2) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_TEAM_STATUS_ERROR);
            return;
        }

        // heroId 转 heroType  前端无法发送heroType
        List<Integer> heroTypes = heroIdToHeroType(heroIds, player, 1);
        boolean isAllZero = false;
        // 声明一个长度为4,值都为0的list
        List<Integer> emptyList = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            emptyList.add(0);
        }
        // 判断heroIds是否都为0
        if (emptyList.containsAll(heroIds)) {
            isAllZero = true;
        }

        // 遍历其他队伍
        for (TeamQueue team : teamQueueMap.values()) {
            if (team.getTeamId() != teamId && team.getStatus() < 2) {
                // 判断该队伍中是否包含heroIds中的英雄
                for (Integer heroId : heroIds) {
                    if (heroId == 0) {
                        continue;
                    }
                    if (team.getUsedHeroId().contains(heroId)) {
                        switch (team.getStatus()) {
                            case 0:
                                handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                                break;
                            case 1:
                                // 取消该队伍中的此英雄
                                // 获取索引
                                int index = team.getUsedHeroId().indexOf(heroId);
                                List<Integer> usedHeroId = new ArrayList<>(team.getUsedHeroId());
                                // 将该位置的值设为0
                                usedHeroId.set(index, 0);
                                team.setUsedHeroId(usedHeroId);

                                // team.UsedHeroId转换为heroType
                                List<Integer> heroTypes2 = heroIdToHeroType(team.getUsedHeroId(), player, 1);
                                // 判断是否还匹配heroGroup
                                StaticFishHeroGroup group = heroGroupMap.get(team.getHeroGroup());
                                if (group == null || !heroTypes2.containsAll(group.getHeroIds())) {
                                    team.setHeroGroup(0);
                                }
                                boolean isOtherZero = false;
                                // 判断heroIds是否都为0
                                if (emptyList.containsAll(heroIds)) {
                                    isOtherZero = true;
                                }
                                if (isOtherZero) {
                                    team.setHeroGroup(0);
                                }
                                break;
                            case 2:
                                handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }

        if (isAllZero) {
            teamQueue.setUsedHeroId(heroIds);
            teamQueue.setStatus(0);
        } else {
            teamQueue.setUsedHeroId(heroIds);
            teamQueue.setStatus(1);
        }
        teamQueue.setHeroGroup(0);
        // 从配置表中匹配英雄组合
        for (StaticFishHeroGroup group : heroGroupMap.values()) {
            // 判断heroids是否包含group.getHeroIds()中元素
            if (heroTypes.containsAll(group.getHeroIds())) {
                // 如果匹配到了,则设置队列的英雄组合
                teamQueue.setHeroGroup(group.getId());
            }
        }

        teamQueueMap.put(teamId, teamQueue);
        // 更新玩家数据
        data.setTeamQueue(teamQueueMap);

        PickHeroRs.Builder rsBuilder = PickHeroRs.newBuilder();

        for (TeamQueue team : teamQueueMap.values()) {
            List<Integer> heroList = heroManager.checkHeroChange(player, team.getUsedHeroId());
            rsBuilder.addTeamQueue(team.encode(heroList).build());
        }

        handler.sendMsgToPlayer(PickHeroRs.ext, rsBuilder.build());
    }

    /**
     * 采集鱼饵界面--队伍派遣
     */
    public void dispatchTeamRq(DispatchTeamRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int teamId = req.getTeamId();
        if (teamId == 0) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        PlayerFishingData data = player.getPlayerFishingData();
        Map<Integer, TeamQueue> teamQueues = data.getTeamQueue();
        TeamQueue teamQueue = teamQueues.get(teamId);
        if (teamQueue == null) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_DATA_ERROR);
            return;
        } else {
            // 判断队伍状态是否为1
            if (teamQueue.getStatus() != 1) {
                handler.sendErrorMsgToPlayer(GameError.FISHING_TEAM_STATUS_ERROR);
                return;
            }
            // 遍历队列,是否都不为0
            for (Integer heroId : teamQueue.getUsedHeroId()) {
                if (heroId == 0) {
                    handler.sendErrorMsgToPlayer(GameError.FISHING_TEAM_MEMBER_ERROR);
                    return;
                }
            }

            List<Integer> baitIds = new ArrayList<>();

            // 如果没有匹配到,则给予默认的鱼饵
            if (teamQueue.getHeroGroup() == 0) {
                // 取得鱼饵配置表中第一个鱼饵
                baitIds.add(staticFishMgr.getFishBaitMap().values().iterator().next().getBaitId());
                teamQueue.setBaits(baitIds);
            } else {
                Map<Integer, StaticFishHeroGroup> heroGroupMap = staticFishMgr.getFishHeroGroupMap();
                StaticFishHeroGroup group = heroGroupMap.get(teamQueue.getHeroGroup());
//                for (StaticFishHeroGroup group : heroGroupMap.values()) {
//                    if (pickHeroIds.equals(group.getHeroIds())) {
//                        teamQueue.setHeroGroup(group.getId());
                Map<Integer, StaticGroupToBaitProbability> groupToBaitProbabilityMap = group.getGroupToBaitProbability();

                // 产生一个1-100的随机数
                int random = RandomUtil.randomBetween(1, 100);
                // 遍历GroupToBaitProbabilityMap
                for (StaticGroupToBaitProbability probability : groupToBaitProbabilityMap.values()) {
                    // 根据random设置baitId且考虑概率相同的情况
                    if (random <= probability.getProbability() && probability.getBaitId() != 0) {
                        baitIds.add(probability.getBaitId());
                    }
                }
                teamQueue.setBaits(baitIds);
//                    }
//                }
            }

            // 派遣时间
            teamQueue.setBeginTime(System.currentTimeMillis());
            // 获取队列中的鱼饵
            // 获取最后一个鱼饵所需时间
            int baitTime = staticFishMgr.getFishBaitMap().get(baitIds.get(baitIds.size() - 1)).getTime();
            // 预计结束时间 = 开始时间 + 鱼饵所需时间
            teamQueue.setEndTime(teamQueue.getBeginTime() + baitTime * 1000L);
            // 设置派遣状态
            teamQueue.setStatus(2);
            // 更新玩家数据
            data.setTeamQueue(teamQueues);

            // build
            DispatchTeamRs.Builder rsBuilder = DispatchTeamRs.newBuilder();
            for (TeamQueue team : teamQueues.values()) {
                List<Integer> heroList = heroManager.checkHeroChange(player, team.getUsedHeroId());
                rsBuilder.addTeamQueue(team.encode(heroList).build());
            }
            handler.sendMsgToPlayer(DispatchTeamRs.ext, rsBuilder.build());
        }
    }

    /**
     * 采集鱼饵界面--领取鱼饵
     */
    public void getBaitsRq(GetBaitsRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int teamId = req.getTeamId();
        if (teamId == 0) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        PlayerFishingData data = player.getPlayerFishingData();
        Map<Integer, TeamQueue> teamQueues = data.getTeamQueue();
        TeamQueue teamQueue = teamQueues.get(teamId);
        if (teamQueue == null) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_DATA_ERROR);
            return;
        }

        if (teamQueue.getStatus() != 2 || System.currentTimeMillis() < teamQueue.getEndTime()) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_TEAM_TIME_ERROR);
            return;
        }

        // 判断队列是否到达结束时间
        // 获取鱼饵
        List<Integer> baitIds = teamQueue.getBaits();
        Map<Integer, PlayerBaits> baitsData = data.getBaits();
        // 获取鱼饵列表
        Map<Integer, StaticFishBait> baitMap = staticFishMgr.getFishBaitMap();
        GetBaitsRs.Builder rsBuilder = GetBaitsRs.newBuilder();
        // 遍历baitIds
        for (Integer baitId : baitIds) {
            // 获取鱼饵
            StaticFishBait bait = baitMap.get(baitId);
            if (bait == null) {
                handler.sendErrorMsgToPlayer(GameError.FISHING_DATA_ERROR);
                return;
            }

            // 更新玩家鱼饵数据
            PlayerBaits playerBaits = baitsData.get(baitId);
            if (playerBaits != null) {
                playerBaits.setCount(playerBaits.getCount() + 1);
                baitsData.put(baitId, playerBaits);
            } else {
                playerBaits = new PlayerBaits();
                playerBaits.setBaitId(baitId);
                playerBaits.setCount(playerBaits.getCount() + 1);
                baitsData.put(baitId, playerBaits);
            }
            // 更新鱼饵图鉴点亮
            // 是否已经存在
            if (!data.getReachBaitRecords().containsKey(baitId)) {
                ReachBaitRecord reachBaitRecord = new ReachBaitRecord();
                // 鱼饵id
                reachBaitRecord.setBaitId(baitId);
                // 获得时间
                reachBaitRecord.setFirstGainTime(System.currentTimeMillis());
                data.getReachBaitRecords().put(baitId, reachBaitRecord);
            }
            // build
            rsBuilder.addBaits(bait.encode().build());
        }
        teamQueue.setStatus(3);
        // 更新玩家数据
        data.setBaits(baitsData);
        data.setTeamQueue(teamQueues);
        // 设置领取刷新时间为当天
        data.setResetTime(GameServer.getInstance().currentDay);
        handler.sendMsgToPlayer(GetBaitsRs.ext, rsBuilder.build());

    }

    /**
     * 钓鱼界面--抛竿
     */
    public void throwPoleRq(ThrowPoleRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int baitId = req.getBaitId();
        if (baitId == 0) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        // 获取鱼饵
        StaticFishBait bait = staticFishMgr.getFishBaitMap().get(baitId);
        if (bait == null) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }

        PlayerFishingData data = player.getPlayerFishingData();

        // 扣除鱼饵
        if (data.getBaits().get(baitId) != null && data.getBaits().get(baitId).getCount() > 0) {
            data.getBaits().get(baitId).setCount(data.getBaits().get(baitId).getCount() - 1);
            if (data.getBaits().get(baitId).getCount() == 0) {
                data.getBaits().remove(baitId);
            }
        } else {
            handler.sendErrorMsgToPlayer(GameError.FISHING_BAIT_NOT_ENOUGH);
        }

        //更新钓鱼数据中的钓鱼记录
        Map<Integer, FishRecord> fishingRecords = data.getFishRecords();

        // 判断fishingRecords长度是否等于10
        if (fishingRecords.size() == 10) {
            // 根据map的key进行排序
            List<Integer> keyList = new ArrayList<>(fishingRecords.keySet());
            Collections.sort(keyList);
            // 删除最早的一条记录
            fishingRecords.remove(keyList.get(0));
        }

        FishRecord record = new FishRecord();
        // 设置记录id为日期,精确到毫秒
        int nowTime = Integer.parseInt(new SimpleDateFormat("MMddHHmmss").format(new Date()).trim());
        // LogHelper.MESSAGE_LOGGER.info("throwPoleRq {}", nowTime);
        record.setRecordId(nowTime);
        record.setBaitId(baitId);

        // 定义概率数组
        List<Integer> orignalRates = new ArrayList<>(bait.getBaitToFishProbability().size());
        // 定义鱼类数组
        List<Integer> fishIds = new ArrayList<>(bait.getBaitToFishProbability().size());
        //遍历baitToFishProbability
        for (Map.Entry<Integer, StaticBaitToFishProbability> entry : bait.getBaitToFishProbability().entrySet()) {
            int probability = entry.getValue().getProbability();
            orignalRates.add(probability);
            fishIds.add(entry.getValue().getFishId());
        }
        // 抽奖
        int index = WeightRandom.initData(orignalRates);

        // 获取鱼种
        StaticFish fish = staticFishMgr.getFishMap().get(fishIds.get(index));

        record.setFishId(fish.getId());
        record.setFishTime(System.currentTimeMillis());
        record.setCount(1);
        record.setSize(fish.getBaseSize());
        record.setExp(fish.getExp());
        record.setPoints(fish.getPoints());
        record.setStage(3);

        fishingRecords.put(record.getRecordId(), record);

        data.setFishRecords(fishingRecords);

        // build
        ThrowPoleRs.Builder rsBuilder = ThrowPoleRs.newBuilder();
        rsBuilder.setRecordId(record.getRecordId());

        // 发送消息
        handler.sendMsgToPlayer(ThrowPoleRs.ext, rsBuilder.build());

    }

    /**
     * 钓鱼界面--收竿
     */
    public void takeBackPoleRq(TakeBackPoleRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int result = req.getResult();
        int critStatus = req.getCritStatus();
        int recordId = req.getRecordId();

        PlayerFishingData data = player.getPlayerFishingData();

        //更新钓鱼数据中的钓鱼记录
        Map<Integer, FishRecord> fishingRecords = data.getFishRecords();
        int level = data.getLevel();

        FishRecord record = fishingRecords.get(recordId);

        if (record.getStage() != 3) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_DATA_ERROR);
            return;
        }

        if (result == 0) {
            //收竿失败
            // 增加默认熟练度
            data.setExp(data.getExp() + 10);
            // 判断是否升级
            // 遍历钓鱼等级配置
            for (Map.Entry<Integer, StaticFishLv> entry : staticFishMgr.getFishLvMap().entrySet()) {
                // 判断是否达到等级
                if (data.getExp() >= entry.getValue().getExp()) {
                    // 设置等级
                    data.setLevel(entry.getKey());
                }
            }
            // 删除钓鱼记录
            fishingRecords.remove(recordId);
            // 更新数据
            data.setFishRecords(fishingRecords);

            // 发送消息
            TakeBackPoleRs.Builder rsBuilder = TakeBackPoleRs.newBuilder();
            rsBuilder.setExp(10);
            rsBuilder.setLevel(data.getLevel());
            Map<Integer, PlayerBaits> baitsMap = data.getBaits();
            for (PlayerBaits bait : baitsMap.values()) {
                rsBuilder.addBaits(bait.encode().build());
            }
            handler.sendMsgToPlayer(TakeBackPoleRs.ext, rsBuilder.build());
        } else if (result == 1) {
            //收竿成功
            // 读取钓鱼等级配置
            StaticFishLv fishingLevel = staticFishMgr.getFishLvMap().get(level);

            // 等级加成-尺寸加成
            StaticFishLevelSizeBuff sizeBuff = fishingLevel.getSize();
            // 获取随机数
            int addition = RandomUtil.randomBetween(sizeBuff.getMin(), sizeBuff.getMax());
            float add = record.getSize() * (addition / 100.0f);
            // 设置尺寸
            record.setSize((int) (record.getSize() + add));

            // 判断是否暴击
            if (critStatus == 1) {
                // 定义概率数组
                List<Integer> originalRates = new ArrayList<>(fishingLevel.getHit().size());
                // 定义暴击数组
                List<Integer> multiple = new ArrayList<>(fishingLevel.getHit().size());
                // 遍历hit
                for (Map.Entry<Integer, StaticFishLevelCritBuff> entry : fishingLevel.getHit().entrySet()) {
                    int probability = entry.getValue().getProbability();
                    int buff = entry.getValue().getMultiple();
                    originalRates.add(probability);
//                    if(buff > fishingLevel.getMaxMultiple()){

//                        buff = fishingLevel.getMaxMultiple();
//                    }
                    multiple.add(buff);
                }
                // 抽奖
                int index = WeightRandom.initData(originalRates);
                Integer integer = multiple.get(index);
                LogHelper.MESSAGE_LOGGER.info("钓鱼等级配置参数 level: {}, probability: {}, curMultiple: {}, maxMultiple: {}", level, originalRates.get(index), integer, fishingLevel.getMaxMultiple());
                if (integer > fishingLevel.getMaxMultiple()) {
                    integer = fishingLevel.getMaxMultiple();
                }
                LogHelper.MESSAGE_LOGGER.info("钓鱼玩家信息 roleId: {}, fishId: {}, 原始count: {}, 原始points: {}", handler.getRoleId(), record.getFishId(), record.getCount(), record.getPoints());
                // 增加获得的鱼的数量  基础数值*暴击倍数
                record.setCount(integer);
                // 增加获得的积分  基础数值*暴击倍数
                record.setPoints(record.getPoints() * integer);
            }

            record.setFishTime(System.currentTimeMillis());
            record.setStage(4);
            // 更新点亮的鱼类图鉴
            // 判断之前是否点亮过
            Map<Integer, ReachFishRecord> reachFishRecords = data.getReachFishRecords();
            ReachFishRecord reachFishRecord = reachFishRecords.get(record.getFishId());
            if (reachFishRecord == null) {
                // 创建一个新的记录
                ReachFishRecord newRecord = new ReachFishRecord();
                newRecord.setFishId(record.getFishId());
                newRecord.setFirstGainTime(System.currentTimeMillis());
                newRecord.setMaxSize(record.getSize());
                newRecord.setAwardStatus(0);
                newRecord.setCount(0);

                reachFishRecords.put(record.getFishId(), newRecord);
            } else {
                // 判断是否有更新
                if (record.getSize() > reachFishRecord.getMaxSize()) {
                    reachFishRecord.setMaxSize(record.getSize());
                    reachFishRecord.setCount(reachFishRecord.getCount() + 1);
                }
            }

            // 更新玩家钓鱼数据
            data.getFishRecords().put(record.getRecordId(), record);
            data.setFishRecords(fishingRecords);
            data.setReachFishRecords(reachFishRecords);
            // 更新熟练度
            data.setPoints(data.getPoints() + record.getPoints());
            // 更新经验
            data.setExp(data.getExp() + record.getExp());
            // 更新等级
            // 遍历钓鱼等级配置
            for (Map.Entry<Integer, StaticFishLv> entry : staticFishMgr.getFishLvMap().entrySet()) {
                // 判断是否达到等级
                if (data.getExp() >= entry.getValue().getExp()) {
                    // 设置等级
                    data.setLevel(entry.getKey());
                }
            }

            // build
            TakeBackPoleRs.Builder rsBuilder = TakeBackPoleRs.newBuilder();
            rsBuilder.setRecordId(recordId);
            rsBuilder.setFishId(record.getFishId());
            rsBuilder.setBaitId(record.getBaitId());
            rsBuilder.setGainTime(record.getFishTime());
            rsBuilder.setCount(record.getCount());
            rsBuilder.setSize(record.getSize());
            rsBuilder.setExp(record.getExp());
            rsBuilder.setPoints(record.getPoints());
            rsBuilder.setLevel(data.getLevel());
            rsBuilder.setSizeBuff(addition);

            Map<Integer, PlayerBaits> baitsMap = data.getBaits();
            for (PlayerBaits bait : baitsMap.values()) {
                rsBuilder.addBaits(bait.encode().build());
            }

            // 品质3鱼类时,发送全服跑马灯进行通告
            // 遍历鱼类配置表
            for (Map.Entry<Integer, StaticFish> entry : staticFishMgr.getFishMap().entrySet()) {
                // 判断鱼类id是否相等
                if (entry.getValue().getId() == record.getFishId()) {
                    // 判断品质是否为6
                    if (entry.getValue().getColor() == 6) {
                        // 发送全服跑马灯
                        String[] params = new String[4];
                        params[0] = String.valueOf(entry.getValue().getId());
                        params[1] = String.valueOf(record.getSize());
                        params[2] = String.valueOf(player.getCountry());
                        params[3] = player.getNick();
                        chatManager.sendWorldChat(ChatId.FISHING_NOTICE, params);
                    }
                }
            }

            // 发送消息
            handler.sendMsgToPlayer(TakeBackPoleRs.ext, rsBuilder.build());
        }
    }

    /**
     * 获取钓鱼记录
     */
    public void getFishRecordRq(GetFishRecordRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        PlayerFishingData data = player.getPlayerFishingData();
        Map<Integer, FishRecord> fishRecords = data.getFishRecords();

        // build
        GetFishRecordRs.Builder rsBuilder = GetFishRecordRs.newBuilder();
        // 遍历钓鱼记录
        for (FishRecord record : fishRecords.values()) {
            if (record.getStage() < 4) {
                continue;
            }
            rsBuilder.addFishRecord(record.encode().build());
        }
        rsBuilder.setShareCount(data.getShareCount());
        // 发送消息
        handler.sendMsgToPlayer(GetFishRecordRs.ext, rsBuilder.build());
    }

    /**
     * 分享钓鱼记录
     */
    public void shareFishRecordRq(ShareFishRecordRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int now = TimeHelper.getCurrentSecond();
        if (now - player.chatTime < 5) {
            handler.sendErrorMsgToPlayer(GameError.CHAT_CD);
            return;
        }

        // 判断是否超过分享次数
        if (player.getPlayerFishingData().getShareCount() >= 30) {
            // 超过次数限制
            handler.sendErrorMsgToPlayer(GameError.FISHING_SHARE_COUNT_LIMIT);
            return;
        }

        int recordId = req.getRecordId();

        PlayerFishingData data = player.getPlayerFishingData();
        FishRecord record = data.getFishRecords().get(recordId);
        if (record == null) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_RECORD_NOT_EXIST);
            return;
        }

        // 分享到阵营聊天
        String[] params = new String[2];
        params[0] = String.valueOf(record.getFishId());
        params[1] = String.valueOf(record.getSize());
        Chat chat = chatManager.createManShare(player, ChatId.FISH_RECORD_SHARE, params);
        chatManager.sendCountryShare(player.getCountry(), chat);
        player.chatTime = now;
        //更新分享次数
        data.setShareCount(data.getShareCount() + 1);

        // build
        ShareFishRecordRs.Builder rsBuilder = ShareFishRecordRs.newBuilder();
        // 返回消息
        handler.sendMsgToPlayer(ShareFishRecordRs.ext, rsBuilder.build());

    }


    /**
     * 查看分享的钓鱼记录
     */
    public void lookFishRecordRq(LookFishRecordRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        long sharerId = req.getSharerId();
        int recordId = req.getRecordId();

        Player sharer = playerManager.getPlayer(sharerId);
        if (sharer == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        PlayerFishingData data = sharer.getPlayerFishingData();
        // 获取钓鱼记录
        FishRecord record = data.getFishRecords().get(recordId);
        if (record == null) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_RECORD_NOT_EXIST);
            return;
        }
        // build
        LookFishRecordRs.Builder rsBuilder = LookFishRecordRs.newBuilder();
        rsBuilder.setFishId(record.getFishId());
        rsBuilder.setSize(record.getSize());
        // 返回消息
        handler.sendMsgToPlayer(LookFishRecordRs.ext, rsBuilder.build());

    }

    /**
     * 图鉴奖励
     */
    public void GetFishAtlasAwardRq(GetFishAtlasAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int fishId = req.getFishId();
        PlayerFishingData data = player.getPlayerFishingData();
        ReachFishRecord record = data.getReachFishRecords().get(fishId);
        // 判断是否已经领取
        if (record.getAwardStatus() == 1) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_ATLAS_AWARD_ALREADY_GET);
            return;
        } else {
            // 获取奖励
            List<Integer> award = staticFishMgr.getFishMap().get(fishId).getAward();
            // 发放奖励
            playerManager.addAward(player, award.get(0), award.get(1), award.get(2), Reason.GET_FISH_ATLAS_AWARD);
            // 更新状态
            record.setAwardStatus(1);
            // 更新玩家钓鱼数据
            data.getReachFishRecords().put(fishId, record);
            // build
            GetFishAtlasAwardRs.Builder rsBuilder = GetFishAtlasAwardRs.newBuilder();
            rsBuilder.setAward(PbHelper.createAward(award.get(0), award.get(1), award.get(2)));
            // 遍历ReachFishRecords
            for (Map.Entry<Integer, ReachFishRecord> entry : data.getReachFishRecords().entrySet()) {
                rsBuilder.addReachBaitRecord(entry.getValue().encode());
            }
            // 发送消息
            handler.sendMsgToPlayer(GetFishAtlasAwardRs.ext, rsBuilder.build());
        }
    }

    /**
     * 积分商店--兑换物品
     */
    public void pointsExchangeRq(PointsExchangeRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());

        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int keyId = req.getKeyId();
        PlayerFishingData data = player.getPlayerFishingData();
        // 判断是否有该物品
        if (!staticFishMgr.getFishShopMap().containsKey(keyId)) {
            handler.sendErrorMsgToPlayer(GameError.FISHING_POINTS_EXCHANGE_NOT_EXIST);
            return;
        } else {
            // 获取物品信息
            StaticFishShop item = staticFishMgr.getFishShopMap().get(keyId);
            // 判断积分是否足够
            if (data.getPoints() < item.getCost()) {
                handler.sendErrorMsgToPlayer(GameError.FISHING_POINTS_EXCHANGE_NOT_ENOUGH);
                return;
            } else {

                if (data.getFishCostRecords().get(keyId) == null) {
                    FishCostRecord record = new FishCostRecord();
                    record.setPropId(keyId);
                    record.setCount(1);
                    // 更新数据
                    data.getFishCostRecords().put(keyId, record);
                    data.setPoints(data.getPoints() - item.getCost());
                } else {
                    FishCostRecord record = data.getFishCostRecords().get(keyId);
//                    LogHelper.MESSAGE_LOGGER.info("兑换前 keyId: {} PropId :{} Count: {}", keyId, record.getPropId(), record.getCount());
                    // 判断是否已经超过兑换次数
                    if (record.getCount() >= item.getLimit()) {
                        handler.sendErrorMsgToPlayer(GameError.FISHING_POINTS_EXCHANGE_LIMIT);
                        return;
                    } else {
                        //更新数据
                        record.setCount(record.getCount() + 1);
                        data.getFishCostRecords().put(keyId, record);
                        data.setPoints(data.getPoints() - item.getCost());
                    }
//                    LogHelper.MESSAGE_LOGGER.info("兑换后 keyId: {} PropId :{} Count: {}", keyId, record.getPropId(), record.getCount());
                }

                // 发放物品
                // 因为有鱼饵 ,特殊处理
                if (item.getBaitId() == 0) {
                    // 兑换道具
                    List<Integer> award = item.getProp();
                    playerManager.addAward(player, award.get(0), award.get(1), award.get(2), Reason.GET_FISH_SHOP_AWARD);
                } else {
                    // 兑换鱼饵
                    if (data.getBaits().get(item.getBaitId()) == null) {
                        PlayerBaits bait = new PlayerBaits();
                        bait.setBaitId(item.getBaitId());
                        bait.setCount(1);
                        data.getBaits().put(item.getBaitId(), bait);
                        ;
                        // 更新鱼饵图鉴点亮
                        // 是否已经存在
                        if (!data.getReachBaitRecords().containsKey(item.getBaitId())) {
                            ReachBaitRecord reachBaitRecord = new ReachBaitRecord();
                            // 鱼饵id
                            reachBaitRecord.setBaitId(item.getBaitId());
                            // 获得时间
                            reachBaitRecord.setFirstGainTime(System.currentTimeMillis());
                            data.getReachBaitRecords().put(item.getBaitId(), reachBaitRecord);
                        }
                    } else {
                        PlayerBaits bait = data.getBaits().get(item.getBaitId());
                        bait.setCount(bait.getCount() + 1);
                        data.getBaits().put(item.getBaitId(), bait);
                    }
                }

            }
            // build
            CommonPb.Award.Builder awardBuilder = CommonPb.Award.newBuilder();
            awardBuilder.setType(item.getProp().get(0));
            awardBuilder.setId(item.getProp().get(1));
            awardBuilder.setCount(item.getProp().get(2));

            PointsExchangeRs.Builder rsBuilder = PointsExchangeRs.newBuilder();
            rsBuilder.setAward(awardBuilder.build());
            // 发送消息
            handler.sendMsgToPlayer(PointsExchangeRs.ext, rsBuilder.build());
        }
    }


    public void initData(Player player) {
        PlayerFishingData data = player.getPlayerFishingData();

        data.setLevel(1);
        data.setExp(0);
        data.setPoints(0);
        data.setShareCount(0);
        data.setResetTime(0);
//        data.setBaits(new ArrayList<>());
        data.setBaits(new ConcurrentHashMap<>());

        Map<Integer, TeamQueue> teamQueue = new HashMap<>();
        //循环5次
        for (int i = 1; i <= 5; i++) {
            //初始化派遣队列
            TeamQueue team = new TeamQueue();
            team.setTeamId(i);
            team.initData();
            teamQueue.put(i, team);
        }
        data.setTeamQueue(teamQueue);
        data.setReachBaitRecords(new ConcurrentHashMap<>());
        data.setReachFishRecords(new ConcurrentHashMap<>());
        data.setFishRecords(new ConcurrentHashMap<>());
        data.setFishCostRecords(new ConcurrentHashMap<>());
    }

    /**
     * GM命令
     *
     * @param player
     */
    public void getALlFishBaits(Player player) {
        Map<Integer, PlayerBaits> playerBaitsMap = player.getPlayerFishingData().getBaits();
        playerBaitsMap.clear();
        Map<Integer, StaticFishBait> baitsMap = staticFishMgr.getFishBaitMap();
        for (Map.Entry<Integer, StaticFishBait> entry : baitsMap.entrySet()) {
            StaticFishBait bait = entry.getValue();
            PlayerBaits playerBaits = new PlayerBaits();
            playerBaits.setBaitId(bait.getBaitId());
            playerBaits.setCount(10);
            playerBaitsMap.put(bait.getBaitId(), playerBaits);
        }
        // 更新数据
        player.getPlayerFishingData().setBaits(playerBaitsMap);
    }

    /**
     * 检查派遣队列状态
     */
    public void checkDispatchQueue() {
        Iterator<Player> iterator = playerManager.getOnlinePlayer().iterator();
        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player == null || player.getChannelId() == -1) {
                continue;
            }
            checkPlayerDispatchQueue(player, now);
        }
    }

    public void checkPlayerDispatchQueue(Player player, long now) {
        // 获取当前玩家所有队列
        Map<Integer, TeamQueue> teamQueue = player.getPlayerFishingData().getTeamQueue();

        FishPusher fishPusher = pushedMap.computeIfAbsent(player.getLord().getLordId(), k -> new FishPusher());

        int today = GameServer.getInstance().currentDay;
        boolean flag = false;
        // 遍历当前玩家所有队列
        for (Map.Entry<Integer, TeamQueue> entry : teamQueue.entrySet()) {
            TeamQueue team = entry.getValue();
            // 判断当前队列状态是否为已派遣
            if (team.getStatus() != 2) {
                continue;
            }
            // 派遣时间未结束
            if (team.getEndTime() > now) {
                continue;
            }
            //今天已经推送过
            if (fishPusher.isPushToday(team.getTeamId(), today)) {
                continue;
            }
            flag = true;
            //记录推送
            fishPusher.pushToday(team.getTeamId(), today);
        }

        // 通知客户端
        if (flag) {
            DispatchQueueEndRs msg = DispatchQueueEndRs.newBuilder().build();
            SynHelper.synMsgToPlayer(player, DispatchQueueEndRs.EXT_FIELD_NUMBER, DispatchQueueEndRs.ext, msg);
        }
    }

}
