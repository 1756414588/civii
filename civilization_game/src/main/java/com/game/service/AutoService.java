package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.Buff;
import com.game.domain.p.SimpleData;
import com.game.domain.s.StaticWorldMonster;
import com.game.log.consumer.EventManager;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.WorldPb;
import com.game.server.thread.ServerThread;
import com.game.spring.SpringUtil;
import com.game.timer.AutoTimer;
import com.game.util.*;
import com.game.worldmap.WorldLogic;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 自动模块 清剿虫族
 *
 *
 * @date 2021/4/1 13:58
 *
 */
@Service
public class AutoService {
    private static final int START = 0;
    private static final int STOP = 1;

    private static final int STOP_KILL = 0;
    private static final int KILL_ING = 1;
    private static final int REWARD = 2;


    @Autowired
    private PlayerManager playerManager;
    @Autowired
    private StaticWorldMgr staticWorldMgr;
    @Autowired
    private WorldManager worldManager;
    @Autowired
    private WorldLogic worldLogic;
    @Autowired
    private SoldierManager soldierManager;
    @Autowired
    private StaticLimitMgr staticLimitMgr;
    @Autowired
    private ActivityManager activityManager;
    @Autowired
    private WorldTargetTaskService worldTargetTaskService;
    @Autowired
    private WorldBoxManager worldBoxManager;
    @Autowired
    private DailyTaskManager dailyTaskManager;
    @Autowired
    ActivityEventManager activityEventManager;
    /**
     * 自动清剿队列
     * 玩家ID 放入时间
     */
    private Map<Long, Long> autoMap = new ConcurrentHashMap<>();


    public void autoKill() {
        long curentTime = TimeHelper.curentTime();
        long check = staticLimitMgr.getNum(SimpleId.AUTO_REWARD_TIME);
        List<Player> kill = Lists.newArrayList();
        Iterator<Map.Entry<Long, Long>> it = autoMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Long> entry = it.next();
            Player player = playerManager.getPlayer(entry.getKey());
            // 特权到期了
            long endTime = playerManager.getAutoKillEndTime(player);
            if (endTime == 0) {
                it.remove();
                if (player.getSimpleData().getAutoRewards().size() > 0) {
                    player.getSimpleData().setAutoState(REWARD);
                } else {
                    player.getSimpleData().setAutoState(STOP_KILL);
                }
                pushAutoKillMsg(player);
                continue;
            }
            SimpleData simpleData = player.getSimpleData();
            if (simpleData.getAutoOnline() == 2) {
                int num = staticLimitMgr.getNum(372);
                if (simpleData.getAutoNum() >= num) {
                    it.remove();
                    if (player.getSimpleData().getAutoRewards().size() > 0) {
                        player.getSimpleData().setAutoState(REWARD);
                    } else {
                        player.getSimpleData().setAutoState(STOP_KILL);
                    }
                    continue;
                }
            } else {
                if (!player.isLogin) {
                    if ((curentTime - player.getLord().getOffTime()) / TimeHelper.SECOND_MS >= staticLimitMgr.getNum(SimpleId.AUTO_LEAVE_TIME)) {
                        it.remove();
                        if (player.getSimpleData().getAutoRewards().size() > 0) {
                            player.getSimpleData().setAutoState(REWARD);
                        } else {
                            player.getSimpleData().setAutoState(STOP_KILL);
                        }
                        continue;
                    }
                }
            }

            int lessTime = Long.valueOf((curentTime - entry.getValue()) / TimeHelper.SECOND_MS).intValue();
            if (lessTime >= check) {
                it.remove();
                int lv = player.getSimpleData().getAutoKillLevel();
                // 减少下兵力
                boolean hasReward = autoLossSoldiers(player);
                if (hasReward) {
                    // 给玩家发下奖励
                    StaticWorldMonster staticMonster = staticWorldMgr.getWorldMonsterMap().values().stream().filter(e -> e.getType() == 1 && e.getLevel() == lv).findFirst().orElse(null);
                    //StaticWorldMonster staticMonster = op.get();
                    if (staticMonster != null) {
                        List<Award> list = autoKillReward(player, staticMonster);
                        addReward(player, list);
                        // 记录杀虫次数
                        player.getSimpleData().setKillRebelTimes(player.getSimpleData().getKillRebelTimes() + 1);
                        if (simpleData.getAutoOnline() == 2) {
                            simpleData.addAutoNum();
                        }
                        doTask(player, staticMonster);
                        // 超过了杀虫上限
                        if (player.getSimpleData().getKillRebelTimes() >= staticLimitMgr.getNum(11)) {
                            if (player.getSimpleData().getAutoRewards().size() > 0) {
                                player.getSimpleData().setAutoState(REWARD);
                            } else {
                                player.getSimpleData().setAutoState(STOP_KILL);
                            }
                            pushAutoKillMsg(player);
                            continue;
                        }
                    }
                } else {
                    if (player.getSimpleData().getAutoRewards().size() > 0) {
                        player.getSimpleData().setAutoState(REWARD);
                    } else {
                        player.getSimpleData().setAutoState(STOP_KILL);
                    }
                    pushAutoKillMsg(player);
                    continue;
                }
                kill.add(player);
            }
        }
        kill.forEach(e -> {
            autoMap.put(e.roleId, curentTime);
            pushAutoKillMsg(e);
        });
    }

    private void doTask(Player player, StaticWorldMonster monster) {
        // 触发任务[只有叛军触发]
        worldLogic.doKillMonster(player, monster.getLevel());
        //TODO jyb世界目标击杀叛军
        worldTargetTaskService.doKillMosnster(player);
        //TODO 击杀虫子事件影响的活动
        activityEventManager.activityTip(EventEnum.KILL_MONSTER, player, 1, monster.getLevel());
        //TODO 通行证
//        activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.DONE_FREE_MONSTER, 1);
        //虫族加速活动
        worldLogic.zergAccelerate(player);
        //TODO 世界宝箱
        worldBoxManager.calcuPoints(WorldBoxTask.KILL_MONSTER, player, 1);
        //TODO 日常任务
        dailyTaskManager.record(DailyTaskId.KILL_MONSTER, player, 1);
        SpringUtil.getBean(EventManager.class).attack_rebel(player, Lists.newArrayList(
                monster.getLevel(),
                "",
                ""
        ));
    }

    //java.lang.IllegalStateException: null
    //at java.util.ArrayList$Itr.remove(ArrayList.java:872) ~[?:1.8.0_211]
    //at com.game.service.AutoService.addReward(AutoService.java:204) ~[civilization_game-1.0-SNAPSHOT.jar:?]
    //at com.game.service.AutoService.autoKill(AutoService.java:132) ~[civilization_game-1.0-SNAPSHOT.jar:?]
    //at com.game.timer.AutoTimer.action(AutoTimer.java:15) ~[civilization_game-1.0-SNAPSHOT.jar:?]
    //at com.game.server.thread.ServerThread.run(ServerThread.java:78) [civilization_common-1.0-SNAPSHOT.jar:?]


    /**
     * 奖励合并
     *
     * @param player
     * @param list
     */
    //private void addReward(Player player, List<Award> list) {
    //    if (player.getSimpleData().getAutoRewards().size() > 0) {
    //        Iterator<Award> it = list.iterator();
    //        while (it.hasNext()) {
    //            Award tmpAward = it.next();
    //            for (Award award : player.getSimpleData().getAutoRewards()) {
    //                if (award.getType() == tmpAward.getType() && award.getId() == tmpAward.getId()) {
    //                    award.setCount(award.getCount() + tmpAward.getCount());
    //                    it.remove();
    //                }
    //            }
    //        }
    //    }
    //    if (list.size() > 0) {
    //        player.getSimpleData().getAutoRewards().addAll(list);
    //    }
    //}
    private void addReward(Player player, List<Award> list) {
        List<Award> awardList = new ArrayList<>();
        if (player.getSimpleData().getAutoRewards().size() > 0) {
            Iterator<Award> it = list.iterator();
            while (it.hasNext()) {
                boolean flag = false;
                Award tmpAward = it.next();
                for (Award award : player.getSimpleData().getAutoRewards()) {
                    if (award.getType() == tmpAward.getType() && award.getId() == tmpAward.getId()) {
                        award.setCount(award.getCount() + tmpAward.getCount());
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    awardList.add(tmpAward);
                }
            }
        } else {
            awardList.addAll(list);
        }
        if (awardList.size() > 0) {
            player.getSimpleData().getAutoRewards().addAll(awardList);
        }
    }

    /**
     * 自动杀虫
     *
     * @param rq
     * @param handler
     */
    public void autoKillMonster(WorldPb.AutoKillMonsterRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        long endTime = playerManager.getAutoKillEndTime(player);

        WorldPb.AutoKillMonsterRs.Builder builder = WorldPb.AutoKillMonsterRs.newBuilder();
        int maxSoldier = staticLimitMgr.getNum(SimpleId.AUTO_MAX_SOLDIER);
        if (player.getSimpleData().getSoliderPool().size() == 0) {
            player.getSimpleData().getSoliderPool().put(1, 0);
            player.getSimpleData().getSoliderPool().put(2, 0);
            player.getSimpleData().getSoliderPool().put(3, 0);
            builder.addSoliders(CommonPb.ThreeInt.newBuilder().setV1(1).setV2(0).setV3(maxSoldier).build());
            builder.addSoliders(CommonPb.ThreeInt.newBuilder().setV1(2).setV2(0).setV3(maxSoldier).build());
            builder.addSoliders(CommonPb.ThreeInt.newBuilder().setV1(3).setV2(0).setV3(maxSoldier).build());
        } else {
            player.getSimpleData().getSoliderPool().forEach((e, f) -> {
                builder.addSoliders(CommonPb.ThreeInt.newBuilder().setV1(e).setV2(f).setV3(maxSoldier).build());
            });
        }
        builder.setEndTime(endTime);
        builder.setKillNum(player.getSimpleData().getKillRebelTimes());
        builder.setMaxLevel(player.getSimpleData().getAutoMaxKillLevel());
        builder.setKillLevel(player.getSimpleData().getAutoKillLevel());
        if (player.getSimpleData().getAutoRewards().size() > 0) {
            player.getSimpleData().getAutoRewards().forEach(e -> {
                builder.addAwards(PbHelper.createAward(e.getType(), e.getId(), e.getCount()));
            });
        }
        Long time = autoMap.get(player.roleId);
        if (time != null) {
            builder.setNextKillTime(time + staticLimitMgr.getNum(SimpleId.AUTO_REWARD_TIME) * TimeHelper.SECOND_MS);
        }
        builder.setState(player.getSimpleData().getAutoState());
        builder.setOffType(player.getSimpleData().getAutoOnline());
        handler.sendMsgToPlayer(WorldPb.AutoKillMonsterRs.ext, builder.build());
    }

    /**
     * 启动自动打怪
     *
     * @param rq
     * @param handler
     */
    public void autoKillMonsterStart(WorldPb.AutoStartKillMonsterRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        switch (rq.getType()) {
            case START:
                long endTime = playerManager.getAutoKillEndTime(player);
                if (endTime == 0) {
                    handler.sendErrorMsgToPlayer(GameError.AUTO_KILL_EXPIRE);
                    return;
                }
                //清剿中 勿重复点击
                if (player.getSimpleData().getAutoState() == KILL_ING) {
                    handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                    return;
                }
                //先领取再清剿吧
                if (player.getSimpleData().getAutoState() == REWARD) {
                    handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                    return;
                }
                //判断下等级是否满足
                if (player.getSimpleData().getAutoKillLevel() > player.getSimpleData().getAutoMaxKillLevel()) {
                    handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                    return;
                }
                player.getSimpleData().setAutoState(KILL_ING);
                player.getSimpleData().setAutoKillLevel(rq.getLevel());
                //放到剿匪队列中了吧要
                autoMap.put(player.roleId, System.currentTimeMillis());
                break;
            case STOP:
                //停止了 勿重复点击
                if (player.getSimpleData().getAutoState() == STOP_KILL) {
                    handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
                    return;
                }
                //先领取再清剿吧
                if (player.getSimpleData().getAutoState() == REWARD) {
                    handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
                    return;
                }
                if (player.getSimpleData().getAutoRewards().size() > 0) {
                    player.getSimpleData().setAutoState(REWARD);
                } else {
                    player.getSimpleData().setAutoState(STOP_KILL);
                }
                autoMap.remove(player.roleId);
                break;
            default:
                handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
                break;
        }
        int offType = rq.getOffType();
        if (offType != 0) {
            if (player.getSimpleData() != null) {
                player.getSimpleData().setAutoNum(0);
                player.getSimpleData().setAutoOnline(rq.getOffType());
            }
        }
        WorldPb.AutoStartKillMonsterRs.Builder builder = WorldPb.AutoStartKillMonsterRs.newBuilder();
        handler.sendMsgToPlayer(WorldPb.AutoStartKillMonsterRs.ext, builder.build());
    }

    /**
     * 领奖
     *
     * @param rq
     * @param handler
     */
    public void autoKillMonsterReward(WorldPb.AutoKillMonsterRewardRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        if (player.getSimpleData().getAutoState() != REWARD) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        if (player.getSimpleData().getAutoRewards().size() == 0) {
            handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
            return;
        }
        WorldPb.AutoKillMonsterRewardRs.Builder builder = WorldPb.AutoKillMonsterRewardRs.newBuilder();
        player.getSimpleData().getAutoRewards().forEach(e -> {
            builder.addAwards(PbHelper.createAward(e.getType(), e.getId(), e.getCount()));
            playerManager.addAward(player, e, Reason.AUTO_KILL_REWARD);
        });
        player.getSimpleData().getAutoRewards().clear();
        player.getSimpleData().setAutoState(STOP_KILL);
        handler.sendMsgToPlayer(WorldPb.AutoKillMonsterRewardRs.ext, builder.build());
    }


    /**
     * 损兵
     *
     * @param player
     * @return
     */
    private boolean autoLossSoldiers(Player player) {
        List<Integer> cost = staticLimitMgr.getAddtion(SimpleId.AUTO_SOLDIER_COST);
        List<Map.Entry<Integer, Integer>> list = player.getSimpleData().getSoliderPool().entrySet().stream().filter(e -> e.getValue() > cost.get(1)).collect(Collectors.toList());
        if (list.size() == 0) {
            return false;
        }
        Map.Entry<Integer, Integer> entry = RandomUtil.getOneRandomElement(list);
        int lossSoldiers = RandomUtil.randomBetween(cost.get(0), cost.get(1));
        int lost = Math.min(entry.getValue(), lossSoldiers);
        int result = entry.getValue() - lost;
        player.getSimpleData().getSoliderPool().put(entry.getKey(), result);
        return true;
    }

    /**
     * 自动发放奖励
     *
     * @param player
     * @param staticMonster
     * @return
     */
    public List<Award> autoKillReward(Player player, StaticWorldMonster staticMonster) {
        List<Award> awards = worldLogic.getMonsterAwards(player, staticMonster);
        for (Award award : awards) {
            if (award.getType() == AwardType.RESOURCE && award.getId() == ResourceType.IRON) {
                int iron = award.getCount();
                //金币增益道具加成
                SimpleData simpleData = player.getSimpleData();
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
                int copper = award.getCount();
                //钢铁增益道具加成
                SimpleData simpleData = player.getSimpleData();
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
        return awards;
    }

    public void pushAutoKillMsg(Player player) {
        WorldPb.SynAutoKillMonsterRq.Builder builder = WorldPb.SynAutoKillMonsterRq.newBuilder();
        int maxSoldier = staticLimitMgr.getNum(SimpleId.AUTO_MAX_SOLDIER);
        player.getSimpleData().getSoliderPool().forEach((e, f) -> {
            builder.addSoliders(CommonPb.ThreeInt.newBuilder().setV1(e).setV2(f).setV3(maxSoldier).build());
        });
        long endTime = playerManager.getAutoKillEndTime(player);
        builder.setEndTime(endTime);
        builder.setKillNum(player.getSimpleData().getKillRebelTimes());
        builder.setMaxLevel(player.getSimpleData().getAutoMaxKillLevel());
        builder.setKillLevel(player.getSimpleData().getAutoKillLevel());
        if (player.getSimpleData().getAutoRewards().size() > 0) {
            player.getSimpleData().getAutoRewards().forEach(e -> {
                builder.addAwards(PbHelper.createAward(e.getType(), e.getId(), e.getCount()));
            });
        }

        Long time = autoMap.get(player.roleId);
        SimpleData simpleData = player.getSimpleData();
        if (time != null) {
            int num = staticLimitMgr.getNum(372);
            if (simpleData.getAutoOnline() == 2 && simpleData.getAutoNum() >= num) {
                simpleData.setAutoState(2);
                simpleData.setAutoOnline(1);
                simpleData.setAutoNum(0);
            }
            builder.setNextKillTime(time + staticLimitMgr.getNum(SimpleId.AUTO_REWARD_TIME) * TimeHelper.SECOND_MS);
        }
        builder.setState(simpleData.getAutoState());
        builder.setOffType(simpleData.getAutoOnline());
        SynHelper.synMsgToPlayer(player, WorldPb.SynAutoKillMonsterRq.EXT_FIELD_NUMBER, WorldPb.SynAutoKillMonsterRq.ext, builder.build());
    }

    /**
     * 领奖
     *
     * @param rq
     * @param handler
     */
    public void addKillMonsterSoldierRq(WorldPb.AddKillMonsterSoldierRq rq, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            LogHelper.CONFIG_LOGGER.info("player is null.");
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        long endTime = playerManager.getAutoKillEndTime(player);
        if (endTime == 0) {
            handler.sendErrorMsgToPlayer(GameError.AUTO_KILL_EXPIRE);
            return;
        }
        int soldierType = rq.getSoldierType();
        //当前兵力
        int curentSoliderNum = player.getSimpleData().getSoliderPool().get(soldierType);
        //兵营剩余兵力
        int soldierNum = soldierManager.getSoldierNum(player, soldierType);
        //补兵数量 3W-当前兵力
        int maxSoldier = staticLimitMgr.getNum(SimpleId.AUTO_MAX_SOLDIER);
        int costSoldierNum = maxSoldier - curentSoliderNum;
        if (costSoldierNum <= 0) {
            handler.sendErrorMsgToPlayer(GameError.AUTO_SOLDIER_FULL);
            return;
        }
        costSoldierNum = Math.min(costSoldierNum, soldierNum);
        if (costSoldierNum <= 0) {
            handler.sendErrorMsgToPlayer(GameError.NO_SOLDIER_COUNT);
            return;
        }

        soldierManager.subSoldierNum(player, soldierType, costSoldierNum, Reason.AUTO_KILL);
        curentSoliderNum += costSoldierNum;
        player.getSimpleData().getSoliderPool().put(soldierType, curentSoliderNum);
        WorldPb.AddKillMonsterSoldierRs.Builder builder = WorldPb.AddKillMonsterSoldierRs.newBuilder();
        builder.setSoliders(CommonPb.ThreeInt.newBuilder()
                .setV1(soldierType)
                .setV2(curentSoliderNum)
                .setV3(maxSoldier)
                .build());
        handler.sendMsgToPlayer(WorldPb.AddKillMonsterSoldierRs.ext, builder.build());
        playerManager.synChange(player, Reason.AUTO_KILL);
    }
}
