package com.game.service;

import com.game.constant.*;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.dataMgr.StaticTaskMgr;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticTask;
import com.game.log.LogUser;
import com.game.log.constant.*;
import com.game.log.consumer.EventManager;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.*;
import com.game.message.handler.ClientHandler;
import com.game.pb.CommonPb;
import com.game.pb.TaskPb;
import com.game.pb.TaskPb.GetTaskRs;
import com.game.spring.SpringUtil;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskService {
    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private HeroManager heroManager;
    @Autowired
    private StaticLimitMgr staticLimitMgr;
    @Autowired
    private BuildingManager buildingManager;
    @Autowired
    private ServerManager serverManager;
    @Autowired
    private StaticPropMgr staticPropMgr;
    @Autowired
    private LordManager lordManager;
    @Autowired
    private WorldManager worldManager;
    @Autowired
    LogUser logUser;
    @Autowired
    EventManager eventManager;

    // 获取所有的任务信息
    public void getTaskRq(ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }
        Map<Integer, Task> taskMap = player.getTaskMap();
        int curMainTask = player.getLord().getCurMainTask();
        List<StaticTask> taskList = staticTaskMgr.getTaskList(curMainTask);
        if (taskList != null) {
            taskList.forEach(x -> {
                if (!taskMap.containsKey(x.getTaskId())) {
                    taskManager.addTask(x.getTaskId(), taskMap);
                }
            });
        }

        GetTaskRs.Builder builder = GetTaskRs.newBuilder();
        Iterator<Task> iterator = taskMap.values().iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            StaticTask staticTask = taskManager.getStaticTask(task.getTaskId());
            if (staticTask == null) {
                continue;
            }
            if (task.getStatus() != 2) {
                task.setMaxProcess(taskManager.getMaxProcess(task.getTaskId()));
                taskManager.checkTask(task, player);
                if (task.isCondOk()) {
                    List<Integer> triggers = new ArrayList<Integer>();
                    triggers.add(task.getTaskId());
                    //taskManager.checkSubTask(TaskType.DONE_ANY_SUBTASK, player, triggers);
                    taskManager.recordFinished(task, player);
                }
                taskManager.checkTaskConfig(task);
            }
            builder.addTask(taskManager.wrapTask(task));
        }

        List<StaticTask> lineTasks = taskManager.getLineTask();
        for (StaticTask e : lineTasks) {
            if (!taskMap.containsKey(e.getTaskId())) {
                if (player.getFinishedTask().contains(e.getTaskId())) { // 已完成
                    Task task = new Task();
                    task.setTaskId(e.getTaskId());
                    task.setProcess(1);
                    task.setStatus(2);
                    builder.addTask(taskManager.wrapTask(task));
                }
            }
        }
        builder.setComplate(taskManager.getComplateTask(player));

        handler.sendMsgToPlayer(GetTaskRs.ext, builder.build());

    }


    @Autowired
    StaticTaskMgr staticTaskMgr;

    // 领取奖励, 并触发后续的任务
    public void taskAwardRq(TaskPb.TaskAwardRq req, ClientHandler handler) {
        Player player = playerManager.getPlayer(handler.getRoleId());
        if (player == null) {
            handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
            return;
        }

        int taskId = req.getTaskId();
        StaticTask staticTask = taskManager.getStaticTask(taskId);
        if (staticTask == null) {
            handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
            return;
        }


        Map<Integer, Task> taskMap = player.getTaskMap();
        Task task = taskMap.get(taskId);
        if (task == null) {
//			handler.sendErrorMsgToPlayer(GameError.NO_TASK);
            TaskPb.TaskAwardRs.Builder taskAward = TaskPb.TaskAwardRs.newBuilder();
            handler.sendMsgToPlayer(TaskPb.TaskAwardRs.ext, taskAward.build());
            return;
        }


        int maxProcess = taskManager.getMaxProcess(taskId);
        if (task.getProcess() < maxProcess) {
            handler.sendErrorMsgToPlayer(GameError.TASK_PROCESS_NOT_OK);
            return;
        }

        if (task.getStatus() != 1) {
            handler.sendErrorMsgToPlayer(GameError.TASK_AWARD_CAN_NOT_TAKE);
            return;
        }

        if (staticTask.getType() == 1) {
            List<StaticTask> allChapTask = staticTaskMgr.getAllChapTask(staticTask.getTaskId());
            for (StaticTask staticTask1 : allChapTask) {
                Task task1 = taskMap.get(staticTask1.getTaskId());
                if (task1 == null) {
                    handler.sendErrorMsgToPlayer(GameError.TASK_AWARD_CAN_NOT_TAKE);
                    return;
                }
                if (task1.getStatus() != 2) {
                    handler.sendErrorMsgToPlayer(GameError.TASK_AWARD_CAN_NOT_TAKE);
                    return;
                }

            }
        }


        List<List<Long>> awardList = taskManager.getAwardList(taskId);
        if (playerManager.isEquipError(awardList, player)) {
            handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
            return;
        }


        TaskPb.TaskAwardRs.Builder taskAward = TaskPb.TaskAwardRs.newBuilder();
        for (List<Long> award : awardList) {
            if (award == null || award.size() != 3) {
                continue;
            }

            int type = award.get(0).intValue();
            int id = award.get(1).intValue();
            int count = award.get(2).intValue();

            if (type == AwardType.HERO) {
                if (heroManager.hasHero(player, id) || heroManager.hasHeroType(player, id)) {
                    continue;
                }
            }

            Award awardPb = new Award();
            awardPb.setType(type);
            awardPb.setId(id);
            awardPb.setCount(count);

            int res = playerManager.addAward(player, awardPb, Reason.TASK_AWARD);
            if (type == AwardType.EQUIP) {
                awardPb.setKeyId(res);
            }


            taskAward.addAward(awardPb.wrapPb());
            /**
             * 任务建立获得的资源产出日志
             */
            if (type == AwardType.RESOURCE) {
                logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(),
                        player.account.getCreateDate(),
                        player.getLevel(),
                        player.getNick(),
                        player.getVip(),
                        player.getCountry(),
                        player.getTitle(),
                        player.getHonor(),
                        player.getResource(id),
                        RoleResourceLog.OPERATE_IN, id, ResOperateType.TASK_AWARD_IN.getInfoType(), count, player.account.getChannel()));
                int t = 0;
                int resType = id;
                switch (resType) {
                    case ResourceType.IRON:
                        t = IronOperateType.TASK_AWARD_IN.getInfoType();
                        break;
                    case ResourceType.COPPER:
                        t = CopperOperateType.TASK_AWARD_IN.getInfoType();
                        break;
                    case ResourceType.OIL:
                        t = OilOperateType.TASK_AWARD_IN.getInfoType();
                        break;
                    case ResourceType.STONE:
                        t = StoneOperateType.TASK_AWARD_IN.getInfoType();
                        break;
                    default:
                        break;
                }
                if (t != 0) {
                    logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId,
                            player.getNick(),
                            player.getLevel(),
                            player.getTitle(),
                            player.getHonor(),
                            player.getCountry(),
                            player.getVip(),
                            player.account.getChannel(),
                            0, count, t), resType);
                }

            }
        }


        int type = AwardType.LORD_PROPERTY;
        int id = LordPropertyType.EXP;
        int count = staticTask.getExp();
        Award award = new Award(0, type, id, count);
        if (count > 0) {
            taskAward.addAward(award.wrapPb());
            LevelAward levelAward = lordManager.addExp(player, staticTask.getExp(), Reason.TASK_AWARD);
            if (levelAward != null) {
                taskAward.setLevelAward(levelAward.wrapPb());
            }
        }
        task.setStatus(2);
        TreeSet<Integer> finishedTask = player.getFinishedTask();
        if (!finishedTask.contains(taskId)) {
            finishedTask.add(taskId);
        }

        Set<Integer> taskSet = new HashSet<>();
        if (staticTask.getType() == 1) {
            int nextTask = staticTask.getNext();
            StaticTask staticTask1 = taskManager.getStaticTask(nextTask);
            if (staticTask1 != null) {
                taskSet.add(staticTask1.getTaskId());
                player.getLord().setCurMainTask(staticTask1.getTaskId());
                List<StaticTask> taskList = staticTaskMgr.getTaskList(staticTask1.getTaskId());
                if (taskList != null) {
                    for (StaticTask staticTask2 : taskList) {
                        taskSet.add(staticTask2.getTaskId());
                    }
                }
            }
        } else if (staticTask.getType() == 2) {
            List<StaticTask> devNextTask = staticTaskMgr.getDevNextTask(staticTask.getTaskId());
            if (devNextTask != null) {
                for (StaticTask staticTask1 : devNextTask) {
                    taskSet.add(staticTask1.getTaskId());
                }
            }
        }

        // 检查下一个新手引导
        Lord lord = player.getLord();
        for (Integer openTask : taskSet) {
            Task addTask = taskManager.addTask(openTask, taskMap);
            eventManager.mission_start(player, taskId, staticTask.getTypeChild(), "");
            addTask.setMaxProcess(taskManager.getMaxProcess(addTask.getTaskId()));
            taskManager.checkTask(addTask, player);  // 检查任务是否完成
            if (addTask != null) {
                taskAward.addTask(taskManager.wrapTask(addTask));
            }
            if (addTask != null && addTask.isCondOk()) {
                // 检查完成任意两项支线任务的任务
                List<Integer> triggers = new ArrayList<Integer>();
                triggers.add(addTask.getTaskId());
                //taskManager.checkSubTask(TaskType.DONE_ANY_SUBTASK, player, triggers);  // 检查支线任务是否完成
                taskManager.recordFinished(addTask, player);
            }

            StaticTask openConfig = taskManager.getStaticTask(openTask);
            if (openConfig != null) {
                int collectTimes = openConfig.getCollectTimes();
                if (collectTimes > 0 && lord != null) {
                    int currentCollectTimes = lord.getCollectTimes();
                    currentCollectTimes += collectTimes;

                    lord.setCollectTimes(currentCollectTimes);
                    taskAward.setCollectTimes(lord.getCollectTimes());
                    int collectInterval = staticLimitMgr.getCollectInterval();
                    taskAward.setCollectEndTime(lord.getCollectEndTime() + collectInterval);
                }
            }
            worldManager.flushTaskMonster1(player, addTask.getTaskId());
        }
        player.setAutoBuildTimes(player.getAutoBuildTimes() + staticTask.getAutoBuildTimes());
        if (lord != null) {
            int nextState = staticTask.getNewState();
            if (nextState != 0) {
                lord.setNewState(nextState);
            }
            taskAward.setNextState(lord.getNewState());
            if (taskId == 10) {
                lord.setOnBuild(0);
            }
            taskAward.setOnBuild(lord.getOnBuild());
        }
        taskAward.setAutoBuildTimes(player.getAutoBuildTimes());
        taskAward.setComplate(taskManager.getComplateTask(player));
        handler.sendMsgToPlayer(TaskPb.TaskAwardRs.ext, taskAward.build());
        eventManager.mission_complete(player, taskId, staticTask.getType(), staticTask.getTypeChild(), staticTask.getTaskName(), awardList);
        buildingManager.synBuildingsByTask(player, req.getTaskId());
        // 删除任务
        if (staticTask.getType() == 1) {
            player.removeTask(task);

            List<StaticTask> allChapTask = staticTaskMgr.getAllChapTask(staticTask.getTaskId());
            for (StaticTask staticTask1 : allChapTask) {
                Task task1 = taskMap.get(staticTask1.getTaskId());
                player.removeTask(task1);
            }
        } else {
            taskManager.updateMainTask(player, task);
        }
        try {
            // 特殊处理类型为8和13, 49的任务
            for (Integer openTaskId : taskSet) {
                int typeChild = taskManager.getTypeChild(openTaskId);
                if (typeChild == TaskType.AWARD_EQUIP || typeChild == TaskType.START_MAKE_EQUIP || typeChild == TaskType.MAKE_SECOND_EQUIP) {
                    //Task openTask = taskManager.createTask(openTaskId);
                    List<List<Integer>> config = taskManager.getParam(openTaskId);
                    if (config == null || config.size() != 1) {
                        continue;
                    }

                    int num = 0;

                    //判断背包是否有物品
                    Integer eqInteger = config.get(0).get(0);
                    Collection<Equip> equips = player.getEquips().values();
                    for (Equip equip : equips) {
                        if (equip.getEquipId() == eqInteger.intValue()) {
                            num++;
                        }
                    }

                    Collection<Hero> heros = player.getHeros().values();
                    for (Hero hero : heros) {
                        ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
                        for (HeroEquip heroEquip : heroEquips) {
                            if (null != heroEquip && null != heroEquip.getEquip() && heroEquip.getEquip().getEquipId() == eqInteger.intValue()) {
                                num++;
                            }
                        }
                    }

                    if (typeChild == TaskType.START_MAKE_EQUIP) {
                        //判断打造队列是否有对应的物品
                        Building buildings = player.buildings;
                        if (buildings != null && buildings.getEquipWorkQue() != null) {
                            LinkedList<WorkQue> equipWorkQue = buildings.getEquipWorkQue();
                            for (WorkQue workQue : equipWorkQue) {
                                Award workQueAward = workQue.getAward();
                                if (null != workQueAward && workQueAward.getId() == eqInteger) {
                                    num++;
                                }
                            }
                        } else {
                            num = 0;
                        }
                    }

                    if (typeChild != TaskType.MAKE_SECOND_EQUIP && num > 0) {
                        taskManager.doTask(typeChild, player, config.get(0));
                    } else if (num > 1) {
                        taskManager.doTask(typeChild, player, config.get(0));
                    }
                } else if (typeChild == TaskType.REAUTY_SGAME) {
                    // 特殊处理美女小游戏提前完成,直接生效
                    long firstPlaySGameTime = player.getLord().getFirstPlaySGameTime();
                    if (firstPlaySGameTime != 0) {
                        taskManager.doTask(TaskType.REAUTY_SGAME, player);
                    }
                } else if (typeChild == TaskType.MAKE_PROP) {
                    // 判断材料工厂打造队列是否已经生产对应的物品
                    //Task openTask = taskManager.createTask(openTaskId);
                    List<List<Integer>> config = taskManager.getParam(openTaskId);
                    if (config == null || config.size() != 1) {
                        continue;
                    }
                    Integer color = config.get(0).get(0);
                    int num = 0;
                    Building building = player.buildings;
                    if (building == null || building.getWorkShop() == null || building.getWorkShop().getWorkQues() == null) {
                        continue;
                    }
                    Map<Integer, WsWorkQue> wsWorkQue = building.getWorkShop().getWorkQues();
                    Set<Map.Entry<Integer, WsWorkQue>> entries = wsWorkQue.entrySet();
                    for (Map.Entry<Integer, WsWorkQue> entry : entries) {
                        WsWorkQue value = entry.getValue();
                        if (value == null || value.getAward() == null) {
                            continue;
                        }
                        Award valueAward = value.getAward();
                        int awardId = valueAward.getId();
                        StaticProp staticProp = staticPropMgr.getStaticProp(awardId);
                        if (null != staticProp && staticProp.getColor() == color) {
                            num++;
                        }
                    }
                    if (num > 0) {
                        taskManager.doTask(typeChild, player, config.get(0));
                    }
                } else if (typeChild == TaskType.DONE_JOURNEY) {
                    CommonPb.Award stableAwards = SpringUtil.getBean(JourneyManager.class).getStableAwards(player.getLord().getLastJourney());
                    if (stableAwards != null) {
                        List<Integer> list = Lists.newArrayList(player.getLord().getLastJourney());
                        taskManager.doTask(TaskType.DONE_JOURNEY, player, list);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
