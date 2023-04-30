package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.manager.*;
import com.game.message.handler.cs.AwardWorldTargetHandler;
import com.game.message.handler.cs.GetWorldBossInfoHandler;
import com.game.message.handler.cs.GetWorldTargetTaskHandler;
import com.game.pb.BasePb;
import com.game.pb.CommonPb;
import com.game.pb.RolePb;
import com.game.pb.WorldPb;
import com.game.server.GameServer;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.WorldBoss;
import com.google.common.collect.HashBasedTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @date 2019/12/24 14:22
 * @description
 */
@Service
public class WorldTargetTaskService {

    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private WorldManager worldManager;

    @Autowired
    private StaticWorldNewTargetMgr staticWorldNewTargetMgr;

    @Autowired
    private StaticActWorldBossMgr staticActWorldBossMgr;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private LordManager lordManager;

    @Autowired
    private StaticWorldMgr staticWorldMgr;

    @Autowired
    private WorldActPlanService worldActPlanService;
    @Autowired
    private StaticMonsterMgr staticMonsterMgr;
    @Autowired
    private WorldBoxManager worldBoxManager;
    @Autowired
    private CityManager cityManager;
    @Autowired
    private WorldTargetManager worldTargetManager;


    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 拿到世界目标任务列表
     *
     * @param handler
     */
    public void GetWorldTargetTask(GetWorldTargetTaskHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldData worldData = worldManager.getWolrdInfo();
        WorldPb.GetWorldTargetTaskRs.Builder builder = WorldPb.GetWorldTargetTaskRs.newBuilder();
        worldData.getTasks().forEach((key, task) -> {
            builder.addWorldTargetTaskInfo(worldTargetTaskInfo(task, player));
        });

        handler.sendMsgToPlayer(GameError.OK, WorldPb.GetWorldTargetTaskRs.ext, builder.build());
    }


    private WorldPb.WorldTargetTaskInfo.Builder worldTargetTaskInfo(WorldTargetTask task, Player player) {
        //calculationPoints(task);
        WorldPb.WorldTargetTaskInfo.Builder worldTargetTaskInfo = WorldPb.WorldTargetTaskInfo.newBuilder();
        task.getCountryTaskProcess().forEach(country -> worldTargetTaskInfo.addCountyInfo(worldTargetCountyInfo(country)));
        WorldPersonalGoal worldPersonalGoal = player.getPersonalGoals().computeIfAbsent(task.getTaskId(), x -> new WorldPersonalGoal(task.getTaskId()));
        worldTargetTaskInfo.setWorldPersonalGoal(worldPersonalGoal(worldPersonalGoal, task));
        worldTargetTaskInfo.setNum(task.getNum());
        worldTargetTaskInfo.setCurHp(task.getCurHp());
        worldTargetTaskInfo.setTaskId(task.getTaskId());
        worldTargetTaskInfo.setCount(task.getCount());
        worldTargetTaskInfo.setComplete(task.getComplete());
        worldTargetTaskInfo.setTime(0L);
        StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(task.getTaskId());
        if (staticWorldNewTarget != null && staticWorldNewTarget.getLastTime() != 0) {
            worldTargetTaskInfo.setTime(task.getOpenTime() + staticWorldNewTarget.getLastTime() * 1000);
        }
        return worldTargetTaskInfo;

    }

    /**
     * 同步世界任务目标
     *
     * @param player
     */
    public void synUpdateWorldTargetTask(Player player) {
        WorldData worldData = worldManager.getWolrdInfo();
        WorldPb.SynUpddateWorldTargetTaskRq.Builder builder = WorldPb.SynUpddateWorldTargetTaskRq.newBuilder();
        worldData.getTasks().forEach((key, task) -> {
            synchronized (task) {
                builder.addWorldTargetTaskInfo(worldTargetTaskInfo(task, player));
            }
        });
        SynHelper.synMsgToPlayer(player, WorldPb.SynUpddateWorldTargetTaskRq.EXT_FIELD_NUMBER, WorldPb.SynUpddateWorldTargetTaskRq.ext, builder.build());
    }

    public WorldPb.WorldTargetCountyInfo.Builder worldTargetCountyInfo(CountryTaskProcess process) {
        WorldPb.WorldTargetCountyInfo.Builder worldTargetCountyInfo = WorldPb.WorldTargetCountyInfo.newBuilder();
        worldTargetCountyInfo.setCountryId(process.getCountryId());
        worldTargetCountyInfo.setPoints(process.getPoints());
        worldTargetCountyInfo.setArea(process.getArea());
        return worldTargetCountyInfo;
    }

    public CommonPb.WorldPersonalGoal.Builder worldPersonalGoal(WorldPersonalGoal worldPersonalGoal, WorldTargetTask task) {
        CommonPb.WorldPersonalGoal.Builder builder = CommonPb.WorldPersonalGoal.newBuilder();
        builder.setTaskId(worldPersonalGoal.getTaskId());
        builder.setWorldState(worldPersonalGoal.isWorldState());
        builder.setProcess(worldPersonalGoal.getProcess());
        StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(task.getTaskId());
        if (staticWorldNewTarget != null) {
            if (task.getCount() >= staticWorldNewTarget.getWorldGoal2() && worldPersonalGoal.getState() == TaskState.DOING && task.getTaskId() == WorldActivityConsts.ACTIVITY_1) {
                worldPersonalGoal.setState(TaskState.SUCCESS);
            }
        }
        builder.setState(worldPersonalGoal.getState());
        return builder;
    }

    /**
     * 杀怪
     *
     * @param player
     */
    public void doKillMosnster(Player player) {
        try {
            WorldData worldData = worldManager.getWolrdInfo();
            WorldTargetTask task = worldData.getTasks().get(worldData.getTarget());
            StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(task.getTaskId());
            if (staticWorldNewTarget.getTargetType() == WorldTaskTargetType.KILL_MONSTER) {
                boolean worldTargetSuccess = false;
                if (task.getNum() < staticWorldNewTarget.getWorldGoal()) {
                    task.setNum(task.getNum() + 1);
                    //updateCountryProcess(player, task,1);
                    //世界任务完成激活下一个世界任务
                    if (task.getNum() >= staticWorldNewTarget.getWorldGoal() && task.getCount() >= staticWorldNewTarget.getWorldGoal2()) {
                        sendRankAward(task, staticWorldNewTarget);
                        //世界任务完成 激活下一个 世界任务
                        worldTargetSuccess = worldTargetSuccess(worldData, staticWorldNewTarget);
                    }
                }
//                if (worldTargetSuccess) {
//                    synUpdateWorldTargetTask(player);
//                }
            }
        } catch (Exception e) {
            logger.error("WorldTargetTaskService  doKillMosnster  e", e);
        }
    }


    public void attackCity(Team attacker, Team defencer, int cityId) {
        StaticWorldCity staticWorldCity = staticWorldMgr.getCity(cityId);
        if (staticWorldCity == null) {
            LogHelper.CONFIG_LOGGER.info("city config is null, cityId = " + cityId);
            return;
        }
        try {
            WorldData worldData = worldManager.getWolrdInfo();
            WorldTargetTask task = worldData.getTasks().get(worldData.getTarget());
            StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(task.getTaskId());
            if (staticWorldNewTarget.getTargetType() == WorldTaskTargetType.ATTACK_CITY && staticWorldNewTarget.getSubObject() == staticWorldCity.getType()) {
                boolean worldTargetSuccess = false;
                if (attacker.isWin()) {
                    //这里是更新世界任务进度
                    if (task.getNum() < staticWorldNewTarget.getWorldGoal()) {
                        task.setNum(task.getNum() + 1);
                        if (task.getNum() >= staticWorldNewTarget.getWorldGoal()) {
                            //TODO 发放世界活动奖励
                            sendRankAward(task, staticWorldNewTarget);
                            //世界任务完成 激活下一个 世界任务
                            worldTargetSuccess = worldTargetSuccess(worldData, staticWorldNewTarget);
                        }
                    }
                }
//                if (worldTargetSuccess) {
//                    Player player = playerManager.getPlayer((long) attacker.getLordId());
//                    if (player != null) {
//                        synUpdateWorldTargetTask(player);
//                    }
//                }
            }
        } catch (Exception e) {
            logger.error("WorldTargetTaskService  attackCity  e", e);
        }
    }


    public void sendRankAward(WorldTargetTask task, StaticWorldNewTarget staticWorldNewTarget) {
        logger.error("sendRankAward", task.getTaskId());
        task.setComplete(1);//设置成完成任务
        updateWorldRank();
        List<CountryTaskProcess> list = task.getCountryTaskProcess();
        if (list.isEmpty()) {
            return;
        }
//        CountryTaskProcess countryTaskProcess = list.stream().filter(x -> x.getPoints() > 0).findAny().orElse(null);
//        if(countryTaskProcess==null){
//
//        }
        List<Integer> campRanking = staticWorldNewTarget.getCampRanking().stream().sorted(Comparator.comparingInt(Integer::intValue).reversed()).collect(Collectors.toList());
        //calculationPoints(task);

        //234阶段方法排行奖励 必须玩家在该地图
        if (task.getTaskId() >= WorldActivityConsts.ACTIVITY_2 && task.getTaskId() <= WorldActivityConsts.ACTIVITY_4) {
            HashBasedTable<Integer, Integer, Integer> map = HashBasedTable.create();
            for (int i = 0; i < list.size(); i++) {
                CountryTaskProcess taskProcess = list.get(i);
                map.put(taskProcess.getArea(), taskProcess.getCountryId(), i);
                logger.error("world sendRankAward country->[{}],rank->[{}],process->[{}],points->[{}]", taskProcess.getCountryId(), i, taskProcess.getPoints(), taskProcess.getPoints());
            }
            logger.error("world sendRankAward--end--");
            for (Player player : playerManager.getPlayers().values()) {
                if (player.getLevel() < staticWorldNewTarget.getLimitLevel()) {
                    continue;
                }
                int mapId = worldManager.getMapId(player.getPos());
                Integer rank = map.get(mapId, player.getCountry());//task.getRank(player.getCountry());
                if (rank != null) {
                    int gold = campRanking.get(rank);
                    Award award = new Award(AwardType.GOLD, 0, gold);
                    playerManager.sendAttachMail(player, Arrays.asList(award), MailId.WORLD_TARGET_MAIL_AWARD, staticWorldNewTarget.getTitle());
                }
            }
        } else {
            //阵营 排名0,1,2对应campRanking size
            Map<Integer, Integer> rankMap = new ConcurrentHashMap<>();
            logger.error("world sendRankAward--start--");
            for (int i = 0; i < list.size(); i++) {
                CountryTaskProcess taskProcess = list.get(i);
                rankMap.put(taskProcess.getCountryId(), i);
                logger.error("world sendRankAward country->[{}],rank->[{}],process->[{}],points->[{}]", taskProcess.getCountryId(), i, taskProcess.getPoints(), taskProcess.getPoints());
            }
            logger.error("world sendRankAward--end--");
            if (campRanking != null && !campRanking.isEmpty()) {
                for (Player player : playerManager.getPlayers().values()) {
                    if (player.getLevel() < staticWorldNewTarget.getLimitLevel()) {
                        continue;
                    }
                    int rank = rankMap.get(player.getCountry());
                    int gold = campRanking.get(rank);
                    Award award = new Award(AwardType.GOLD, 0, gold);
                    playerManager.sendAttachMail(player, Arrays.asList(award), MailId.WORLD_TARGET_MAIL_AWARD, staticWorldNewTarget.getTitle());
                }
            }
        }
        //此处发放伤害排名奖励
        List<StaticLairRank> lairRankList = staticActWorldBossMgr.getLairRankList(task.getTaskId());
        if (lairRankList != null) {
            List<WorldHitRank> hitRanks = task.getHitRanks();
            for (int i = 0; i < hitRanks.size(); i++) {
                WorldHitRank rank = hitRanks.get(i);
                int index = i + 1;
                StaticLairRank staticLairRank = lairRankList.stream().filter(a -> index >= a.getRankRand().get(0) && index <= a.getRankRand().get(1)).findFirst().orElse(null);
                if (staticLairRank != null) {
                    List<Award> awardList = new ArrayList<>();
                    staticLairRank.getAward().forEach(x -> {
                        awardList.add(new Award(x.get(0), x.get(1), x.get(2)));
                    });
                    if (!list.isEmpty()) {
                        playerManager.sendAttachMail(rank.getPlayer(), awardList, MailId.WORLD_TARGET_HIT_MAIL_AWARD, staticWorldNewTarget.getTitle());
                    }
                }
            }
        }
    }

    /**
     * 一个世界目标完成激活下一个
     *
     * @param
     * @param staticWorldNewTarget
     */
    private boolean worldTargetSuccess(WorldData worldData, StaticWorldNewTarget staticWorldNewTarget) {
        //世界任务完成 激活下一个 世界任务
        int nextId = staticWorldNewTarget.getNextId();
        WorldTargetTask worldTargetTask = worldData.getTasks().get(nextId);
        if (worldTargetTask == null) {
            openWorldTarget(nextId);
            return true;
        }
        return false;
    }

    /**
     * 激活某个世界目标 初始化
     *
     * @param taskId
     */
    public WorldTargetTask openWorldTarget(int taskId) {
        WorldData worldData = worldManager.getWolrdInfo();
        WorldTargetTask worldTargetTask = new WorldTargetTask();
        worldTargetTask.setTaskId(taskId);
        if (taskId > WorldActivityConsts.ACTIVITY_1) {
            if (taskId >= WorldActivityConsts.ACTIVITY_2 && taskId <= WorldActivityConsts.ACTIVITY_4) {
                List<Integer> primaryMapId = staticWorldMgr.getPrimaryMapId();
                primaryMapId.forEach(x -> {
                    Stream.of(1, 2, 3).forEach(a -> worldTargetTask.getPross().put(x, a, new CountryTaskProcess(x, a)));
                });
            } else {
                Stream.of(1, 2, 3).forEach(a -> worldTargetTask.getProcess().put(a, new CountryTaskProcess(0, a)));
            }
        }
        worldData.getTasks().put(taskId, worldTargetTask);
        //初始化世界boss
        initWorldBoss(worldTargetTask, worldData);
        //清楚上一次的进度
        for (Player player : playerManager.getPlayers().values()) {
            WorldPersonalGoal worldPersonalGoal = player.getPersonalGoals().get(taskId);
            if (worldPersonalGoal != null) {
                player.getPersonalGoals().remove(taskId);
            }
        }
        // 开启世界活动

        worldData.setTarget(taskId);
        // 开启季节加成 (开启世界进程的7的时候开启季节加成)
        if (taskId == 7) {
            //worldManager.openSeason();
            worldManager.openNewSeason();
        }

        //刷新所有玩家的地图状态
        playerManager.openTargetToMap(taskId);
        worldBoxManager.calcuPoints(WorldBoxTask.WORLD_TASK, null, 0);
        //同步所有人
        for (Player player : playerManager.getOnlinePlayer()) {
            synUpdateWorldTargetTask(player);
        }
        worldActPlanService.openWorldTarget(taskId);
        return worldTargetTask;
    }


    /**
     * 初始化世界boss
     *
     * @param worldTargetTask
     * @param worldData
     */
    public void initWorldBoss(WorldTargetTask worldTargetTask, WorldData worldData) {
        StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(worldTargetTask.getTaskId());
        if (staticWorldNewTarget.getTargetType() != WorldTaskTargetType.KILL_BOSS) {
            return;
        }
        StaticActWorldBoss staticActWorldBoss = staticActWorldBossMgr.getStaticActWorldBoss(staticWorldNewTarget.getSubObject());
        StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(staticActWorldBoss.getBossModel());
        WorldBoss share = new WorldBoss();
        share.setMonsterId(staticMonster.getMonsterId());
        share.setSoldier(staticMonster.getSoldierCount());
        share.setMaxSoldier(staticMonster.getSoldierCount());
        share.setCountry(0);
        worldData.setShareBoss(share);
        WorldPb.SynWorldBossOpen.Builder builder = WorldPb.SynWorldBossOpen.newBuilder();
        builder.setWorldBossInfo(worldBossInfo(worldTargetTask, null));
        for (Player player : playerManager.getPlayers().values()) {
            if (player == null) {
                continue;
            }
            SynHelper.synMsgToPlayer(player, WorldPb.SynWorldBossOpen.EXT_FIELD_NUMBER, WorldPb.SynWorldBossOpen.ext, builder.build());
        }
    }

    public void awardWorldTarget(WorldPb.AwardWorldTargetRq rq, AwardWorldTargetHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldData worldData = worldManager.getWolrdInfo();
        WorldTargetTask worldTargetTask = worldData.getTasks().get(rq.getTaskId());
        if (worldTargetTask == null) {
            handler.sendErrorMsgToPlayer(GameError.WORLD_TARGET_TASK_NOT_EXIST);
            logger.error("awardWorldTarget GameError {}", GameError.WORLD_TARGET_TASK_NOT_EXIST.toString());
            return;
        }
        StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(worldTargetTask.getTaskId());
        WorldPersonalGoal worldPersonalGoal = player.getPersonalGoals().get(worldTargetTask.getTaskId());
        if (rq.getAwardType() == AwardTargetType.PERSONTARGET) {
            if (worldPersonalGoal.getState() == TaskState.AWARD) {
                handler.sendErrorMsgToPlayer(GameError.TARGET_AWARD_IS_AWARD);
                logger.error("awardWorldTarget GameError {}", GameError.TARGET_AWARD_IS_AWARD.toString());
                return;
            }
            if (worldTargetTask.getCount() < staticWorldNewTarget.getWorldGoal2()) {
                handler.sendErrorMsgToPlayer(GameError.TARGET_NOT_SUCCESS);
                logger.error("awardWorldTarget GameError {}", GameError.TARGET_NOT_SUCCESS.toString());
                return;
            }
            worldPersonalGoal.setState(TaskState.AWARD);
            playerManager.addAward(player, AwardType.GOLD, 0, staticWorldNewTarget.getWorldGoalAward2(), Reason.AWARD_WORLD_PERSON_TARGET);
        } else {
            if (worldPersonalGoal.isWorldState()) {
                handler.sendErrorMsgToPlayer(GameError.TARGET_AWARD_IS_AWARD);
                logger.error("awardWorldTarget GameError {}", GameError.TARGET_AWARD_IS_AWARD.toString());
                return;
            }
            if (worldTargetTask.getNum() < staticWorldNewTarget.getWorldGoal()) {
                handler.sendErrorMsgToPlayer(GameError.TARGET_NOT_SUCCESS);
                logger.error("awardWorldTarget GameError {}", GameError.TARGET_NOT_SUCCESS.toString());
                return;
            }
            worldPersonalGoal.setWorldState(true);
            playerManager.addAward(player, AwardType.GOLD, 0, staticWorldNewTarget.getWorldGoalAward(), Reason.AWARD_WORLD_TARGET);
        }
        WorldPb.AwardWorldTargetRs.Builder builder = WorldPb.AwardWorldTargetRs.newBuilder();
        builder.setGolda(player.getGold());
        handler.sendMsgToPlayer(GameError.OK, WorldPb.AwardWorldTargetRs.ext, builder.build());
    }

    public void getWorldBossInfo(GetWorldBossInfoHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        WorldTargetTask worldTargetTask = getWorldBossTarget();
        WorldPb.GetWorldBossInfoRs.Builder builder = WorldPb.GetWorldBossInfoRs.newBuilder();
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldTargetTask != null && worldData.getShareBoss() != null) {
            WorldPersonalGoal worldPersonaGoal = player.getPersonalGoals().get(worldTargetTask.getTaskId());
            if (worldPersonaGoal == null) {
                worldPersonaGoal = new WorldPersonalGoal(worldTargetTask.getTaskId());
                player.getPersonalGoals().put(worldTargetTask.getTaskId(), worldPersonaGoal);
            }
            //看是否是同一天
            if (!TimeHelper.isSameDayOfMillis(worldPersonaGoal.getLastAttackBossTime(), System.currentTimeMillis())) {
                worldPersonaGoal.setChallengeNumber(0);
                worldPersonaGoal.setLastAttackBossTime(System.currentTimeMillis());
            }
            builder.setWorldBossInfo(worldBossInfo(worldTargetTask, worldPersonaGoal));
            builder.setRankInfo(worldTargetTask.getWorldHitRankInfo(player));
        }
        handler.sendMsgToPlayer(GameError.OK, WorldPb.GetWorldBossInfoRs.ext, builder.build());
    }


    /**
     * @param worldTargetTask
     * @param
     * @return
     */
    public WorldPb.WorldBossInfo.Builder worldBossInfo(WorldTargetTask worldTargetTask, WorldPersonalGoal worldPersonaGoal) {
        WorldData worldData = worldManager.getWolrdInfo();
        WorldPb.WorldBossInfo.Builder builder = WorldPb.WorldBossInfo.newBuilder();
        builder.setCurHP(worldData.getShareBoss().getSoldier());
        builder.setTaskId(worldTargetTask.getTaskId());
        if (worldPersonaGoal != null) {
            builder.setChallengeNumber(worldPersonaGoal.getChallengeNumber());
        } else {
            builder.setChallengeNumber(0);
        }
        return builder;
    }

    /**
     * 查找是否有世界boss正在进行中
     *
     * @return
     */
    public WorldTargetTask getWorldBossTarget() {
        WorldData worldData = worldManager.getWolrdInfo();
        StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(worldData.getTarget());
        if (staticWorldNewTarget == null) {
            return null;
        }
        if (staticWorldNewTarget.getTargetType() != WorldTaskTargetType.KILL_BOSS) {
            return null;
        }
        WorldTargetTask worldTargetTask1 = worldData.getTasks().get(worldData.getTarget());
        if (worldTargetTask1 != null && worldTargetTask1.getComplete() == 2) {
            return worldTargetTask1;
        }
        return null;
//
//
//        for (Map.Entry<Integer, WorldTargetTask> entry : worldData.getTasks().entrySet()) {
//            WorldTargetTask worldTargetTask = entry.getValue();
//            StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(entry.getKey());
//            if (staticWorldNewTarget.getTargetType() != WorldTaskTargetType.KILL_BOSS) {
//                continue;
//            }
//            if (worldTargetTask.getNum() < staticWorldNewTarget.getWorldGoal()) {
//                return entry.getValue();
//            }
//        }
//        return null;
    }

    //阶段1 新增任务 指挥部达到7级 500个
    public void updateWorldTaskTarget() {
        WorldData worldData = worldManager.getWolrdInfo();
        if (worldData == null) {
            return;
        }
        if (worldData.getTarget() != WorldActivityConsts.ACTIVITY_1) {
            return;
        }
        StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(worldData.getTarget());
        if (staticWorldNewTarget == null) {
            return;
        }
        WorldTargetTask worldTargetTask = worldData.getTasks().get(worldData.getTarget());
        if (worldTargetTask != null) {
            if (worldTargetTask.getCount() < staticWorldNewTarget.getWorldGoal2()) {
                worldTargetTask.addCount();
                if (worldTargetTask.getNum() >= staticWorldNewTarget.getWorldGoal()) {
                    //TODO 发放世界活动奖励
                    if (worldTargetTask.getCount() >= staticWorldNewTarget.getWorldGoal2()) {
                        sendRankAward(worldTargetTask, staticWorldNewTarget);
                        //世界任务完成 激活下一个 世界任务
                        worldTargetSuccess(worldData, staticWorldNewTarget);
                    }

                }
            }
        }
    }

    /**
     * 处理世界势力值排行的问题
     */
    public void updateWorldRank() {
        WorldData worldData = worldManager.getWolrdInfo();
        logger.info("统计势力值,target={}", worldData);
        if (worldData == null) {
            return;
        }
        int target = worldData.getTarget();
        WorldTargetTask worldTargetTask = worldData.getTasks().computeIfAbsent(target, x -> new WorldTargetTask());
        /**
         * 世界进程第2-4阶段  平原8区域排名攻击24组
         *  拆分为8个区域3个阵营的24组进行竞争排名
         */
        logger.info("统计势力值,target={}", target);
        if (target >= WorldActivityConsts.ACTIVITY_2 && target <= WorldActivityConsts.ACTIVITY_4) {
            List<Integer> primaryMapId = staticWorldMgr.getPrimaryMapId();//获取低级区域的mapId；
            primaryMapId.forEach(mapId -> {
                List<StaticWorldCity> staticWorldCityByMapId = staticWorldMgr.getStaticWorldCityByMapId(mapId);
                if (staticWorldCityByMapId != null) {
                    staticWorldCityByMapId.forEach(x -> {
                        City city = cityManager.getCity(x.getCityId());
                        if (city.getCountry() > 0) {
                            logger.info("统计势力值,city={}", city.toString());
                            worldTargetTask.updatePross(mapId, city.getCountry(), x);
                        }
                    });
                }
            });
            worldTargetTask.flushRank();
        }
        /**
         * 6-8阶段 高原4区整体阵营排行
         */
        if (target >= WorldActivityConsts.ACTIVITY_6 && target <= WorldActivityConsts.ACTIVITY_8) {
            List<Integer> midMapId = staticWorldMgr.getMiddleMapId();//获取低级区域的mapId；
            midMapId.forEach(mapId -> {
                List<StaticWorldCity> staticWorldCityByMapId = staticWorldMgr.getStaticWorldCityByMapId(mapId);
                if (staticWorldCityByMapId != null) {
                    staticWorldCityByMapId.forEach(x -> {
                        City city = cityManager.getCity(x.getCityId());
                        if (city.getCountry() > 0) {
                            logger.info("统计势力值,city={}", city.toString());
                            worldTargetTask.updatePross(city.getCountry(), x);
                        }
                    });
                }
            });
            worldTargetTask.flushRank();
        }
    }

    /**
     * 定时 进程超时进入下一进程
     */
    public void timeWorldTarget() {
        WorldData worldData = worldManager.getWolrdInfo();
        WorldTargetTask task = worldData.getTasks().get(worldData.getTarget());
        if (task == null) {
            return;
        }

        StaticWorldNewTarget staticWorldNewTarget = staticWorldNewTargetMgr.get(task.getTaskId());
        long lastTime = staticWorldNewTarget == null ? 0 : staticWorldNewTarget.getLastTime();
        long l = (System.currentTimeMillis() - task.getOpenTime()) / 1000;
        boolean isOverTime = lastTime != 0 && l >= lastTime;

        boolean isComplateTarget = worldTargetManager.isComplateTarget(task, staticWorldNewTarget);

        if (isOverTime || isComplateTarget) {
            LogHelper.GAME_LOGGER.info("世界目标taskId:{}完成", task.getTaskId());
            sendRankAward(task, staticWorldNewTarget);
            boolean b = worldTargetSuccess(worldData, staticWorldNewTarget);
            if (b) {
                logger.error("timeWorldTarget is open {}", staticWorldNewTarget.getNextId());
            }
        }
    }

    //整体推送 前端要求这样做
    public void synAllPlayerTime() {
        RolePb.SynTimeRq.Builder builder = RolePb.SynTimeRq.newBuilder();
        builder.setTime(LocalDateTime.now().getHour());
        BasePb.Base.Builder synBase = PbHelper.createSynBase(RolePb.SynTimeRq.EXT_FIELD_NUMBER, RolePb.SynTimeRq.ext, builder.build());
        playerManager.getOnlinePlayer().forEach(x -> {
            GameServer.getInstance().sendMsgToPlayer(x, synBase);
        });
    }
}
