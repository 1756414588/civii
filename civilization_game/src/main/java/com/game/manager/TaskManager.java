package com.game.manager;

import com.game.spring.SpringUtil;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.*;
import com.game.dataMgr.*;
import com.game.domain.Player;
import com.game.domain.p.*;
import com.game.domain.s.*;
import com.game.log.consumer.EventManager;
import com.game.pb.BasePb;
import com.game.pb.BuildingPb;
import com.game.pb.CommonPb;
import com.game.pb.TaskPb.SynTaskRq;
import com.game.server.GameServer;
import com.game.util.GameHelper;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.util.SynHelper;
import com.google.common.collect.HashBasedTable;

// service->manager->config+db
// 后面任务有个优化,已经完成的任务不需要加载，但是要存盘
@Component
public class TaskManager {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private StaticTaskMgr staticTaskMgr;

    @Autowired
    private TechManager techManager;

    @Autowired
    private BuildingManager buildingManager;

    @Autowired
    private StaticMissionMgr staticMissionMgr;

    @Autowired
    private MissionManager missionManager;

    @Autowired
    private HeroManager heroManager;

    @Autowired
    private WorldManager worldManager;

    @Autowired
    private PlayerManager playerManager;

    @Autowired
    private StaticBuildingMgr staticBuildingMgr;

    @Autowired
    private KillEquipManager killEquipMgr;
    @Autowired
    private StaticOpenManger staticOpenManger;
    @Autowired
    private StaticLimitMgr staticLimitMgr;

    @Autowired
    private StaticEquipDataMgr equipDataMgr;

    @Autowired
    private ActivityManager manager;

    @Autowired
    private DailyTaskMgr dailyTaskMgr;

    // 主线和支线
    public int getTaskType(int taskId) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
        if (staticTask == null) {
            return 0;
        }
        return staticTask.getType();
    }

    public int getTypeChild(int taskId) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
        if (staticTask == null) {
            return 0;
        }
        return staticTask.getTypeChild();
    }

    public CommonPb.Task.Builder wrapTask(Task task) {
        CommonPb.Task.Builder builder = CommonPb.Task.newBuilder();
        builder.setTaskId(task.getTaskId());
        builder.setProcess(task.getProcess());
        builder.setStatus(task.getStatus());
        builder.setTaskType(getTaskType(task.getTaskId()));
        Map<Integer, Integer> condMap = task.getCondMap();
        for (Map.Entry<Integer, Integer> elem : condMap.entrySet()) {
            if (elem == null) {
                continue;
            }
            builder.addCond(elem.getValue());
        }

        return builder;
    }

    public List<List<Long>> getAwardList(int taskId) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
        if (staticTask == null) {
            return new ArrayList<List<Long>>();
        }
        return staticTask.getAwardList();
    }

    public boolean isConfigOK(int taskId) {
        return staticTaskMgr.getStaticTask(taskId) != null;
    }

    public int getMaxProcess(int taskId) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
        return staticTask.getProcess();
    }

    public Task createTask(int taskId) {
        Task task = new Task();
        task.setTaskId(taskId);
        task.setProcess(0);
        task.setStatus(0);
        StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
        if (staticTask != null) {
            if (staticTask.getTypeChild() == TaskType.SINGLE_HERO_WEAR) {
                checkSingleWear(task);
            } else if (staticTask.getTypeChild() == TaskType.START_MAKE_EQUIP || staticTask.getTypeChild() == TaskType.MAKE_SECOND_EQUIP) {
                checkMakeEquip(task);
            } else if (staticTask.getTypeChild() == TaskType.AWARD_EQUIP) {
                checkAwardEquip(task);
            } else if (staticTask.getTypeChild() == TaskType.ANY_WEAR) {
                checkAnyWear(task);
            } else if (staticTask.getTypeChild() == TaskType.MISSION_HIRE_ANY) {
                checkAnyMissionHire(task);
            } else {
                commonInit(task);
            }

        } else {
            LogHelper.CONFIG_LOGGER.info("taskId = " + taskId + " config is null.");
        }

        return task;
    }

    public void checkSingleWear(Task task) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
        List<List<Integer>> param = staticTask.getParam();
        if (param != null) {
            List<Integer> config = param.get(0);
            if (config != null && config.size() > 1) {
                task.initCond(config.size() - 1);
            } else {
                LogHelper.CONFIG_LOGGER.info("single hero wear config error!");
            }
        } else {
            LogHelper.CONFIG_LOGGER.info("param is null, taskId = " + task.getTaskId());
        }
    }

    public void checkMakeEquip(Task task) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
        List<List<Integer>> param = staticTask.getParam();
        List<Integer> config = param.get(0);
        if (config != null && config.size() > 0) {
            task.initCond(config.size());
        } else {
            LogHelper.CONFIG_LOGGER.info("START_MAKE_EQUIP config error!");
        }
    }

    public void checkTaskConfig(Task task) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
        if (staticTask.getTypeChild() == TaskType.AWARD_EQUIP) {
            checkAwardEquip(task);
        }
    }

    public void checkAwardEquip(Task task) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
        if (staticTask == null) {
            return;
        }
        List<List<Integer>> param = staticTask.getParam();
        if (param != null && param.size() == 1) {
            List<Integer> config = param.get(0);
            if (config != null && config.size() >= 1) {
                task.initCond(config.size());
            } else {
                LogHelper.CONFIG_LOGGER.info("award equip config error!");
            }
        } else {
            LogHelper.CONFIG_LOGGER.info("award equip param is null, taskId = " + task.getTaskId());
        }
    }

    public void checkAnyWear(Task task) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
        if (staticTask == null) {
            return;
        }
        List<List<Integer>> param = staticTask.getParam();
        if (param != null && param.size() == 1) {
            List<Integer> config = param.get(0);
            if (config != null && config.size() >= 1) {
                task.initCond(config.size());
            } else {
                LogHelper.CONFIG_LOGGER.info("checkAnyEquipconfig error!");
            }
        } else {
            LogHelper.CONFIG_LOGGER.info("checkAnyWear param is null, taskId = " + task.getTaskId());
        }
    }

    public void checkAnyMissionHire(Task task) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
        if (staticTask == null) {
            return;
        }

        List<List<Integer>> param = staticTask.getParam();
        if (param != null && param.size() == 1) {
            List<Integer> config = param.get(0);
            if (config != null && config.size() >= 1) {
                task.initCond(config.size());
            } else {
                LogHelper.CONFIG_LOGGER.info("checkAnyEquipconfig error!");
            }
        } else {
            LogHelper.CONFIG_LOGGER.info("checkAnyWear param is null, taskId = " + task.getTaskId());
        }
    }

    public void commonInit(Task task) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
        if (staticTask == null) {
            return;
        }
        List<List<Integer>> param = staticTask.getParam();
        if (param != null) {
            task.initCond(param.size());
        } else {
            LogHelper.CONFIG_LOGGER.info("param is null, taskId = " + task.getTaskId());
        }
    }

    public void checkLineTask(Player player) {
        staticTaskMgr.getTaskMap().values().forEach(e -> {
            if (e.getType() == 3) { //成长线任务
                if (!player.getFinishedTask().contains(e.getTaskId())) {
                    addTask(e.getTaskId(), player.getTaskMap());
                    SpringUtil.getBean(EventManager.class).mission_start(player, e.getTaskId(), e.getTypeChild(), "");
                }
            }
        });
    }

    public Task addTask(int taskId, Map<Integer, Task> taskMap) {
        Task task = createTask(taskId);
        if (taskMap.containsKey(task.getTaskId())) {
            return taskMap.get(taskId);
        } else {
            taskMap.put(task.getTaskId(), task);
        }

        return task;
    }

    public List<List<Integer>> getParam(int taskId) {
        StaticTask staticTask = staticTaskMgr.getStaticTask(taskId);
        return staticTask.getParam();
    }

    // 选择任务执行
    public boolean selectTaskDo(Task task, Player player, List<Integer> triggers) {
        // 已经完成的不需要同步
        if (task.isCondOk() && task.getStatus() >= 1) {
            return false;
        }

        int taskType = getTypeChild(task.getTaskId());
        if (taskType == TaskType.NEW_STATE) {
            return doNewStateTask(task, getParam(task.getTaskId()), player);
        } else if (taskType == TaskType.BUILDING_LEVELUP) {
            return doUpBuilding(task, player, triggers);
        } else if (taskType == TaskType.START_TECH_UP || taskType == TaskType.FINISH_TECH) {
            return doUpTech(task, triggers);
        } else if (taskType == TaskType.RES_BUILDING_LEVEL_UP) {
            return doUpResBuilding(task, player);
        } else if (taskType == TaskType.SINGLE_HERO_WEAR) {
            return doWearEquip(task, triggers);
        } else if (taskType == TaskType.POS_WEAR_EQUIP) {
            return doWearEquip(task, triggers);
        } else if (taskType == TaskType.GET_RESOURCE) {
            return doGetResource(task);
        } else if (taskType == TaskType.START_HIRE_SOLDIER) {
            return doStartHireSoldier(task, triggers);
        } else if (taskType == TaskType.START_MAKE_EQUIP || taskType == TaskType.MAKE_SECOND_EQUIP) {
            return doStartMakeEquip(task, triggers);
        } else if (taskType == TaskType.DONE_MISSION) {
            return doneMission(task, triggers);
        } else if (taskType == TaskType.MISSION_HIRE_HERO) {
            return missionHireHero(task, triggers);
        } else if (taskType == TaskType.DONE_ANY_SUBTASK) {
            return doSubTask(task, triggers);
        } else if (taskType == TaskType.EMBATTLE_HERO) {
            return doEmbattle(task, triggers);
        } else if (taskType == TaskType.AWARD_EQUIP) {
            return doAwardEquip(task, triggers);
        } else if (taskType == TaskType.ARMMY_RETURN) {
            return doArmmyReturn(task);
        } else if (taskType == TaskType.HIRE_RESEARCHER) {
            return doHireReseacher(task, triggers);
        } else if (taskType == TaskType.LORD_LEVEL_UP) {
            return doLordLevelUp(task, triggers);
        } else if (taskType == TaskType.HIRE_BLACKSMITH) {
            return doHireEquiper(task, triggers);
        } else if (taskType == TaskType.SPEED_MAKE_EQUIP) {
            return doSpeedMakeEquip(task);
        } else if (taskType == TaskType.HIRE_OFFIER) {
            return doHireOfficer(task, triggers);
        } else if (taskType == TaskType.WASH_EQUIP) {
            return doWashEquip(task, triggers);
        } else if (taskType == TaskType.MAKE_KILL_EQUIP) {
            return doMakeKillEquip(task, triggers);
        } else if (taskType == TaskType.LEVELUP_KILL_EQUIP) {
            return doLevelupKillEquip(task, triggers);
        } else if (taskType == TaskType.MAKE_PROP) {
            return makeProp(task, triggers);
        } else if (taskType == TaskType.ALL_HERO_WEARHAT) {
            return doAllHeroWearHat(task, player, triggers);
        } else if (taskType == TaskType.ALL_HERO_WEAR_TWO) {
            return doAllHeroWearTwo(task, getParam(task.getTaskId()), player);
        } else if (taskType == TaskType.HIRE_SOLDIER_NUM) {
            return doHireSoldierNum(task, triggers);
        } else if (taskType == TaskType.HIRE_SOLDIER_TIMES) {
            return doHireSoldierTimes(task, triggers);
        } else if (taskType == TaskType.CAPTURE_CITY) {
            return doCaptureCity(task, triggers);
        } else if (taskType == TaskType.KILL_REBEL) {
            return doKillRebel(task, triggers);
        } else if (taskType == TaskType.KILL_MUTIL_REBEL) {
            return doKillMutilRebel(task);
        } else if (taskType == TaskType.START_LEVELUP_BUILDING) {
            return doStartLevelBuilding(task, triggers);
        } else if (taskType == TaskType.DEPOT) {
            return doDepot(task);
        } else if (taskType == TaskType.ANY_WEAR) {
            return doAnyWear(task, triggers);
        } else if (taskType == TaskType.MISSION_HIRE_ANY) {
            return doAnyMisionHire(task, getParam(task.getTaskId()), player);
        } else if (taskType == TaskType.HIRE_SOLDIER_TIMES_ANY) {
            return doAnySoldierTimes(task);
        } else if (taskType == TaskType.COMMON_HERO) {
            return doCommonHeroTask(task, triggers);
        } else if (taskType == TaskType.GOOD_HERO) {
            return doGoodHeroTask(task, triggers);
        } else if (taskType == TaskType.SAY_A_WORD) {
            return doDoSayWord(task);
        } else if (taskType == TaskType.ADD_SOILIER_YUBEI) {
            return doAddSoilier(task);
        } else if (taskType == TaskType.RECOVER_BUILDING) {
            if (player.buildings.getRecoverBuilds().contains(getParam(task.getTaskId()).get(0).get(0))) {
                return doRecoverBuild(task);
            }
        } else if (taskType == TaskType.REAUTY_SEEKING) {
            return doReautySeeking(task);
        } else if (taskType == TaskType.HIRE_SCIENTIST) {
            return doHireScientist(task);
        } else if (taskType == TaskType.REAUTY_SGAME) {
            return doReautysGame(task);
        } else if (taskType == TaskType.LEARN_FROM_TEACHER) {
            return doReautysGame(task);
        } else if (taskType == TaskType.RECRUIT_STUDENTS) {
            return doReautysGame(task);
        } else if (taskType == TaskType.OMAMENT_WEARER) {
            return doWearOmamen(task, player);
        } else if (taskType == TaskType.DONE_JOURNEY) {
            return doneJourney(task, triggers);
        }else if (taskType == TaskType.COMPLETE_TOWER_DEFENSE) {
            return doTD(task,player);
        }
        return false;
    }
    private boolean doTD(Task task, Player player){
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() <= 0) {
            LogHelper.CONFIG_LOGGER.info("upbuilding param error");
            return false;
        }
        int targetLv = param.get(0).get(0);
        TD td = player.getTdMap().values().stream().filter(e -> e.getLevelId() ==targetLv ).findFirst().orElse(null);
        if (td!=null&&td.getStar()>0){
            updateTask(task, 1, targetLv);
            return  true;
        }
        return false;
    }

    // 利用雇佣的科学家进行一次研究加速
    private boolean doHireScientist(Task task) {
        // TODO Auto-generated method stub
        updateTask(task);
        return true;
    }

    private boolean doReautySeeking(Task task) {
        updateTask(task);
        // logger.error(task.toString());
        return true;
    }

    private boolean doRecoverBuild(Task task) {
        updateTask(task);
        return true;
    }

    public boolean doDoSayWord(Task task) {
        updateTask(task);
        return true;
    }

    public boolean doAddSoilier(Task task) {
        updateTask(task);
        return true;
    }

    public boolean doReautysGame(Task task) {
        updateTask(task);
        return true;
    }

    public boolean doWearOmamen(Task task, Player player) {
        Map<Integer, PlayerOmament> playerOmaments = player.getPlayerOmaments();
        if (null == playerOmaments) {
            return false;
        }


        Iterator<PlayerOmament> iterator = playerOmaments.values().iterator();
        while (iterator.hasNext()) {
            PlayerOmament playerOmament = (PlayerOmament) iterator.next();
            int omamentId = playerOmament.getOmamentId();
            if (omamentId != 0) {
                updateTask(task);
                return true;
            }
        }
        //updateTask(task);
        return false;
    }

    public void synTask(Task task, Player player) {
        if (task != null) {
            SynTaskRq.Builder builder = SynTaskRq.newBuilder();
            builder.setTask(wrapTask(task));
            checkAutoBuild(task, player, builder);
            builder.setComplate(getComplateTask(player));
            SynHelper.synMsgToPlayer(player, SynTaskRq.EXT_FIELD_NUMBER, SynTaskRq.ext, builder.build());
        }
    }

    public void checkAutoBuild(Task task, Player player, SynTaskRq.Builder builder) {
        if (task.getTaskId() == 5 && task.getStatus() == 1) {
            player.setOnBuild(0);
            builder.setOnBuild(0);
        } else if (task.getTaskId() == 11 && task.getStatus() == 1) {
            player.setOnBuild(0);
            builder.setOnBuild(0);
        }
    }

    public boolean isSubTaskFinished(Task task) {
        int taskType = getTaskType(task.getTaskId());
        if (taskType != 2) {
            return false;
        }

        int taskId = task.getTaskId();
        int maxProcess = getMaxProcess(taskId);
        if (task.getProcess() >= maxProcess) {
            return true;
        }

        return false;
    }

    public boolean isMainTask(Task task) {
        int taskType = getTaskType(task.getTaskId());
        return taskType == 1;
    }

    public void doTask(int taskType, Player player) {
        doTask(taskType, player, null);
    }

    // 完成任务
    public void doTask(int taskType, Player player, List<Integer> triggers) {
        Map<Integer, Task> taskMap = player.getTaskMap();
        if (taskMap == null) {
            return;
        }

        ArrayList<Integer> finishedTask = new ArrayList<Integer>();
        for (Map.Entry<Integer, Task> elem : taskMap.entrySet()) {
            if (elem == null) {
                continue;
            }

            Task task = elem.getValue();
            if (task == null) {
                continue;
            }

            if (getTypeChild(task.getTaskId()) != taskType) {
                continue;
            }

            int taskId = task.getTaskId();
            if (!isConfigOK(taskId)) {
                continue;
            }

            task.setMaxProcess(getMaxProcess(taskId));
            boolean isProcess = selectTaskDo(task, player, triggers);
            // syn task
            if (isProcess) {
                synTask(task, player);
                // 任务完成的时候触发
                if (task.isCondOk()) {
                    worldManager.flushTaskMonster1(player, task.getTaskId());
                    recordFinished(task, player);
                }
            }

            // 检查完成的任务
            if (isSubTaskFinished(task)) {
                finishedTask.add(task.getTaskId());
            }
        }

        // 检查完成任意两项支线任务的任务
        checkSubTask(TaskType.DONE_ANY_SUBTASK, player, finishedTask);
    }

    // 当前只有1个进度的任务
    public void updateTask(Task task) {
        updateTask(task, 1);
    }

    // task: 当前任务 processAdd: 增加的进度 condIndex:完成的条件
    public void updateTask(Task task, int processAdd, int condIndex) {
        if (processAdd <= 0) {
            LogHelper.CONFIG_LOGGER.info("processAdd <= 0, taskId = " + task.getTaskId());
            return;
        }

        int maxProcess = task.getMaxProcess();
        int currentProcess = task.getProcess() + processAdd;
        currentProcess = Math.max(1, currentProcess);
        currentProcess = Math.min(maxProcess, currentProcess);
        task.setProcess(currentProcess);
        task.updateCond(condIndex);
        if (task.getProcess() >= maxProcess) {
            task.setStatus(1);
        }
    }

    public void updateTask(Task task, int processAdd) {
        if (processAdd <= 0) {
            LogHelper.CONFIG_LOGGER.info("processAdd <= 0, taskId = " + task.getTaskId());
            return;
        }

        int maxProcess = task.getMaxProcess();
        int currentProcess = task.getProcess() + processAdd;
        currentProcess = Math.max(1, currentProcess);
        currentProcess = Math.min(maxProcess, currentProcess);
        task.setProcess(currentProcess);
        if (task.getProcess() >= maxProcess) {
            task.setStatus(1);
            task.doneCond();
        }
    }

    // 完成新手引导
    public boolean doNewStateTask(Task task, List<List<Integer>> param, Player player) {
        Lord lord = player.getLord();
        if (param == null || param.size() != 1 || param.get(0).size() != 1) {
            LogHelper.CONFIG_LOGGER.info("new state param error");
            return false;
        }
        List<Integer> stateParam = param.get(0);
        int state = stateParam.get(0);
        int curState = (int) lord.getNewState();
        if (curState < state) {
            return false;
        }
        updateTask(task);
        return true;
    }

    // 指定建筑升级
    public boolean doUpBuilding(Task task, Player player, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("doUpBuilding triggers == null || triggers.size() != 2");
            return false;
        }

        int buildingId = triggers.get(0);
        int buildingLv = triggers.get(1);

        Building building = player.buildings;
        if (building == null) {
            return false;
        }

        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() <= 0) {
            LogHelper.CONFIG_LOGGER.info("upbuilding param error");
            return false;
        }

        // 建筑id1, 建筑Id2
        boolean needSync = false;
        for (int i = 0; i < param.size(); i++) {
            List<Integer> elem = param.get(i);
            if (elem == null) {
                LogHelper.CONFIG_LOGGER.info("doUpBuilding elem is null");
                continue;
            }

            if (elem.size() != 2) {
                LogHelper.CONFIG_LOGGER.info("doUpBuilding elem size is not 2.");
                continue;
            }

            int configId = elem.get(0);
            int configLv = elem.get(1);
            if (configId != buildingId) {
                continue;
            }

            if (buildingLv < configLv) {
                continue;
            }

            if (task.isCondOk(i)) {
                continue;
            }
            updateTask(task, 1, i);
            needSync = true;
        }

        return needSync;
    }

    // 研究科技
    public boolean doUpTech(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 3) {
            LogHelper.CONFIG_LOGGER.info("doUpTech triggers == null || triggers.size() != 2");
            return false;
        }

        // 当前科技
        int techType = triggers.get(0);
        int techLv = triggers.get(1);
        int techProcess = triggers.get(2);

        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doUpTech param error");
            return false;
        }

        List<Integer> techConfig = param.get(0);
        if (techConfig == null || techConfig.size() != 3) {
            LogHelper.CONFIG_LOGGER.info("doUpTech techConfig error");
            return false;
        }

        int type = techConfig.get(0);
        if (techType != type) {
            return false;
        }

        int level = techConfig.get(1);
        if (techLv > level) {
            updateTask(task);
            return true;
        }

        if (techLv < level) {
            return false;
        }

        int process = techConfig.get(2);
        if (techProcess < process) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 多个资源建筑升级: 资源类型, 资源等级
    public boolean doUpResBuilding(Task task, Player player) {
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doUpResBuilding param error");
            return false;
        }

        List<Integer> resource = param.get(0);
        if (resource == null || resource.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("doUpResBuilding resource error");
            return false;
        }

        int resType = resource.get(0);
        int resLevel = resource.get(1);

        // 当前个数
        int curNum = buildingManager.getResouceNum(player, resType, resLevel);
        int process = curNum - task.getProcess();
        process = Math.max(0, process);

        // 类型+等级
        if (process > 0) {
            updateTask(task, process);
            return true;
        }

        return false;
    }

    //  单个将领穿装备(param: 将领Id, 装备Id, 装备Id2)
    public boolean doWearEquip(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        // 英雄Id
        int heroId = triggers.get(0);
        int equipId = triggers.get(1);

        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("killRebel param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() <= 1) {
            LogHelper.CONFIG_LOGGER.info("killRebel config error");
            return false;
        }

        if (heroId != config.get(0)) {
            return false;
        }

        boolean needSync = false;
        for (int i = 1; i < config.size(); i++) {
            // 当前条件已经完成
            if (task.isCondOk(i - 1)) {
                continue;
            }

            if (equipId == config.get(i)) {
                updateTask(task, 1, i - 1);
                needSync = true;
            } else {
                // 增加逻辑判断英雄穿戴的装备是否比配置的装备的品质高,高也算完成任务
                StaticEquip staticEquip = equipDataMgr.getStaticEquip(equipId);
                int equipType = staticEquip.getEquipType();
                int quality = staticEquip.getQuality();

                StaticEquip tarStaticEquip = equipDataMgr.getStaticEquip(config.get(i));
                int tarEquipType = tarStaticEquip.getEquipType();
                int tarQuality = tarStaticEquip.getQuality();
                if (equipType == tarEquipType && quality > tarQuality) {
                    updateTask(task, 1, i - 1);
                    needSync = true;
                }
            }
        }

        return needSync;
    }

    // 征收资源[1], 填最大进度就可以了
    public boolean doGetResource(Task task) {
        updateTask(task);

        return true;
    }

    // 募兵一次，点按钮完成
    public boolean doStartHireSoldier(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int soldierType = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() <= 0) {
            updateTask(task);
            return true;
        } else {

            if (param.size() != 1) {
                LogHelper.CONFIG_LOGGER.info("doStartMakeEquip param error");
                return false;
            }

            List<Integer> config = param.get(0);
            if (config == null || config.size() != 1) {
                LogHelper.CONFIG_LOGGER.info("doStartMakeEquip wearConfig error");
                return false;
            }

            if (soldierType != config.get(0)) {
                return false;
            }
            updateTask(task);
            return true;
        }
    }

    // 开始打造装备, 支持多件
    public boolean doStartMakeEquip(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int equipId = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doStartMakeEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.info("doStartMakeEquip wearConfig error");
            return false;
        }

        boolean needSyn = false;
        for (int i = 0; i < config.size(); i++) {
            if (task.isCondOk(i)) {
                continue;
            }

            if (equipId == config.get(i)) {
                updateTask(task, 1, i);
                needSyn = true;
            }
        }

        return needSyn;
    }

    // 完成副本
    public boolean doneMission(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int missionId = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doneMission param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doneMission wearConfig error");
            return false;
        }

        if (missionId != config.get(0)) {
            LogHelper.CONFIG_LOGGER.info("doneMission compare error");
            return false;
        }

        updateTask(task);

        return true;
    }

    // 完成副本
    public boolean doneJourney(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

    /*int journeyId = triggers.get(0);
    List<List<Integer>> param = getParam(task.getTaskId());
    if (param == null || param.size() != 1) {
      LogHelper.CONFIG_LOGGER.info("doneMission param error");
      return false;
    }

    List<Integer> config = param.get(0);
    if (config == null || config.size() != 1) {
      LogHelper.CONFIG_LOGGER.info("doneMission wearConfig error");
      return false;
    }

    if (journeyId != config.get(0)) {
      return false;
    }*/

        updateTask(task);

        return true;
    }

    // 武将招募
    public boolean missionHireHero(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int heroId = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("missionHireHero param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("missionHireHero config error");
            return false;
        }

        if (heroId != config.get(0)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 主线任务: 完成支线任务触发任务
    public boolean checkSubTask(int taskType, Player player, List<Integer> triggers) {
        Map<Integer, Task> taskMap = player.getTaskMap();
        if (taskMap == null) {
            return false;
        }

        for (Map.Entry<Integer, Task> elem : taskMap.entrySet()) {
            if (elem == null) {
                continue;
            }

            Task task = elem.getValue();
            if (task == null) {
                continue;
            }

            if (!isMainTask(task)) {
                continue;
            }

            if (getTypeChild(task.getTaskId()) != taskType) {
                continue;
            }

            int taskId = task.getTaskId();
            if (!isConfigOK(taskId)) {
                continue;
            }
            task.setMaxProcess(getMaxProcess(taskId));
            boolean isProcess = selectTaskDo(task, player, triggers);
            // syn task
            if (isProcess) {
                synTask(task, player);
                if (task.isCondOk()) {
                    recordFinished(task, player);
                }
            }
        }

        return false;
    }

    // 检查主线任务的支线任务
    public boolean doSubTask(Task task, List<Integer> triggers) {
        int subTaskNum = 0;
        if (triggers != null) {
            subTaskNum = triggers.size();
        }

        if (subTaskNum <= 0) {
            return false;
        }

        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doWearEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doWearEquip wearConfig error");
            return false;
        }

        updateTask(task, subTaskNum);

        return true;
    }

    // 武将上阵
    public boolean doEmbattle(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int heroId = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doEmbattle param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() < 1) {
            LogHelper.CONFIG_LOGGER.info("doEmbattle config error");
            return false;
        }

        boolean found = false;
        for (Integer configHeroId : config) {
            if (configHeroId == heroId) {
                found = true;
                break;
            }
        }

        if (found) {
            updateTask(task);
            task.doneCond();
            return true;
        }

        return false;
    }

    // 有上阵武将A或者B的任务
    public boolean checkEmbattle(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doEmbattle param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() < 1) {
            LogHelper.CONFIG_LOGGER.info("doEmbattle config error");
            return false;
        }

        boolean found = false;
        List<Integer> embattleList = player.getEmbattleList();
        for (Integer configId : config) {
            if (embattleList.contains(configId)) {
                found = true;
                break;
            }
        }

        if (found) {
            updateTask(task);
            task.doneCond();
            return true;
        }

        return false;
    }

    // 收获打造
    public boolean doAwardEquip(Task task, List<Integer> triggers) {

        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        // 装备Id
        int equipId = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() <= 0) {
            updateTask(task);
            return true;
        } else {
            if (param.size() != 1) {
                LogHelper.CONFIG_LOGGER.info("doAwardEquip param error");
                return false;
            }

            List<Integer> config = param.get(0);
            if (config == null || config.size() < 1) {
                LogHelper.CONFIG_LOGGER.info("doAwardEquip config error");
                return false;
            }

            boolean needSyn = false;
            for (int i = 0; i < config.size(); i++) {
                // 当前条件已经完成
                if (task.isCondOk(i)) {
                    continue;
                }

                if (equipId == config.get(i)) {
                    updateTask(task, 1, i);
                    needSyn = true;
                }
            }
            return needSyn;
        }
    }

    // 部队回城
    public boolean doArmmyReturn(Task task) {
        updateTask(task);

        return true;
    }

    // 如果没有部队也算完成
    public boolean checkArmyReturn(Player player, Task task) {
        // 没有部队也算完成
        if (!player.hasMarch()) {
            updateTask(task);
            return true;
        }
        return false;
    }

    // 雇佣官员
    public boolean doHireEmployee(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int employeeLv = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doHireEmployee param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doHireEmployee config error");
            return false;
        }

        if (employeeLv < config.get(0)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 雇佣研究员: param 研究员等级
    public boolean doHireReseacher(Task task, List<Integer> triggers) {
        return doHireEmployee(task, triggers);
    }

    // 主公升级
    public boolean doLordLevelUp(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int lordLv = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doLordLevelUp param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doLordLevelUp config error");
            return false;
        }

        if (lordLv < config.get(0)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 军需官(武器)招募
    public boolean doHireEquiper(Task task, List<Integer> triggers) {
        return doHireEmployee(task, triggers);
    }

    // 内政官招募
    public boolean doHireOfficer(Task task, List<Integer> triggers) {
        return doHireEmployee(task, triggers);
    }

    // 军需官加速打造
    public boolean doSpeedMakeEquip(Task task) {
        updateTask(task);

        return true;
    }

    // 装备洗练
    public boolean doWashEquip(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int washTimes = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doWashEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doWashEquip config error");
            return false;
        }

        if (washTimes < config.get(0)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 打造杀器
    public boolean doMakeKillEquip(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int euqipId = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doMakeKillEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doMakeKillEquip config error");
            return false;
        }

        if (euqipId != config.get(0)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    public boolean docheckKillEquip(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doMakeKillEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doMakeKillEquip config error");
            return false;
        }

        int configId = config.get(0);
        // 检测玩家身上是否有国器
        if (killEquipMgr.hasEquip(player, configId)) {
            updateTask(task);
            return true;
        }

        return false;
    }

    // 升级杀器
    public boolean doLevelupKillEquip(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int euqipId = triggers.get(0);
        int level = triggers.get(1);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doLevelKillEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("doLevelKillEquip config error");
            return false;
        }

        if (euqipId != config.get(0)) {
            return false;
        }

        if (level < config.get(1)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    public boolean doCheckKillEquip(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doLevelKillEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("doLevelKillEquip config error");
            return false;
        }

        int equipId = config.get(0);
        KillEquip killEquip = killEquipMgr.getKillEquip(player, equipId);
        if (killEquip == null) {
            return false;
        }

        if (killEquip.getLevel() < config.get(1)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 全部武将穿戴精铁盔(param:装备Id)
    public boolean doCheckAllHeroWearHat(Task task, List<List<Integer>> param, Player player) {
        boolean needSync = false;
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doAllHeroWearHat param error");
            return needSync;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doAllHeroWearHat config error");
            return needSync;
        }

        int equipId = config.get(0);
        List<Integer> embattles = player.getEmbattleList();
        for (Integer heroId : embattles) {
            Hero hero = player.getHero(heroId);
            if (hero == null) {
                continue;
            }

            ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
            for (HeroEquip heroEquip : heroEquips) {
                if (heroEquip == null) {
                    continue;
                }

                Equip equip = heroEquip.getEquip();
                if (equip == null) {
                    continue;
                }

                // 增加逻辑判断英雄穿戴的装备是否比配置的装备的品质高,高也算完成任务
                StaticEquip staticEquip = equipDataMgr.getStaticEquip(equip.getEquipId());
                int equipType = staticEquip.getEquipType();
                int quality = staticEquip.getQuality();

                StaticEquip tarStaticEquip = equipDataMgr.getStaticEquip(equipId);
                int tarEquipType = tarStaticEquip.getEquipType();
                int tarQuality = tarStaticEquip.getQuality();

                if (equipType == tarEquipType && quality >= tarQuality) {
                    needSync = true;
                }
            }

            if (needSync) {
                updateTask(task, getMaxProcess(task.getTaskId()));
            }
        }

        return needSync;
    }

    // 生产材料
    public boolean makeProp(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int quality = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("makeProp param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("makeProp config error");
            return false;
        }

        if (quality != config.get(0)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 全部武将穿戴精铁盔(param:装备Id)
    public boolean doAllHeroWearHat(Task task, Player player, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int equipId = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doAllHeroWearHat param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doAllHeroWearHat config error");
            return false;
        }

        if (equipId != config.get(0)) {
            return false;
        }

        List<Integer> embattles = player.getEmbattleList();
        for (Integer heroId : embattles) {
            Hero hero = player.getHero(heroId);
            if (hero == null) {
                continue;
            }

            if (!hero.hasEquip(equipId)) {
                return false;
            }
        }

        updateTask(task, getMaxProcess(task.getTaskId()));
        task.doneCond();
        return true;
    }

    // 全部武将穿戴守备印和千营符
    public boolean doAllHeroWearTwo(Task task, List<List<Integer>> param, Player player) {
        boolean flag = true;
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doAllHeroWearTwo param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("doAllHeroWearTwo config error");
            return false;
        }

        int equipIdA = config.get(0);
        int equipIdB = config.get(1);

        List<Integer> embattles = player.getEmbattleList();
        for (Integer heroId : embattles) {
            Hero hero = player.getHero(heroId);
            if (hero == null) {
                continue;
            }

            boolean needSync1 = false;
            boolean needSync2 = false;
            ArrayList<HeroEquip> heroEquips = hero.getHeroEquips();
            for (HeroEquip heroEquip : heroEquips) {
                if (heroEquip == null) {
                    continue;
                }

                Equip equip = heroEquip.getEquip();
                if (equip == null) {
                    continue;
                }

                // 增加逻辑判断英雄穿戴的装备是否比配置的装备的品质高,高也算完成任务
                StaticEquip staticEquip = equipDataMgr.getStaticEquip(equip.getEquipId());
                int equipType = staticEquip.getEquipType();
                int quality = staticEquip.getQuality();

                StaticEquip tarStaticEquip1 = equipDataMgr.getStaticEquip(equipIdA);
                int tarEquipType1 = tarStaticEquip1.getEquipType();
                int tarQuality1 = tarStaticEquip1.getQuality();

                StaticEquip tarStaticEquip2 = equipDataMgr.getStaticEquip(equipIdB);
                int tarEquipType2 = tarStaticEquip2.getEquipType();
                int tarQuality2 = tarStaticEquip2.getQuality();

                if (equipType == tarEquipType1 && quality >= tarQuality1) {
                    needSync1 = true;
                }

                if (equipType == tarEquipType2 && quality >= tarQuality2) {
                    needSync2 = true;
                }
            }
            if (!needSync1 || !needSync2) {
                flag = false;
            }
        }

        if (flag) {
            updateTask(task, getMaxProcess(task.getTaskId()));
        }
        task.doneCond();
        return true;
    }

    // 招募兵x个(param: 兵类型)
    public boolean doHireSoldierNum(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        // 士兵的类型
        int soldierType = triggers.get(0);
        // 当前兵营的兵力
        int soldierNum = triggers.get(1);

        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doLevelKillEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doLevelKillEquip config error");
            return false;
        }

        int configSoldierType = config.get(0);
        if (soldierType != configSoldierType) {
            return false;
        }

        updateTask(task, soldierNum);
        return true;
    }

    // 招募兵x次(param: 兵类型, 次数)
    public boolean doHireSoldierTimes(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        // 士兵的类型
        int soldierType = triggers.get(0);

        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doHireSoldierTimes param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doHireSoldierTimes config error");
            return false;
        }

        int configSoldierType = config.get(0);
        if (soldierType != configSoldierType) {
            return false;
        }

        updateTask(task);
        return true;
    }

    // 攻克郡营|郡县|郡城|州郡|州府|州城(param:城池的类型)
    public boolean doCaptureCity(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        // 城池的类型
        int cityType = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doCaptureCity param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doCaptureCity config error");
            return false;
        }

        int configCityType = config.get(0);
        if (cityType != configCityType) {
            return false;
        }

        updateTask(task);
        return true;
    }

    // 击杀叛军(param: 叛军等级) 次数在进度中
    public boolean doKillRebel(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        // 叛军的等级
        int rebelLv = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("killRebel param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("killRebel config error");
            return false;
        }

        int configRebelLv = config.get(0);
        if (rebelLv < configRebelLv) {
            return false;
        }

        updateTask(task);
        return true;
    }

    // 击杀多队叛军(param:无)
    public boolean doKillMutilRebel(Task task) {
        updateTask(task);
        return true;
    }

    // 开始升级建筑
    public boolean doStartLevelBuilding(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        int buildingId = triggers.get(0);
        int buildingLv = triggers.get(1);

        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("killRebel param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("killRebel config error");
            return false;
        }

        int configId = config.get(0);
        int configLv = config.get(1);
        if (configId != buildingId) {
            return false;
        }

        if (buildingLv < configLv) {
            return false;
        }

        updateTask(task);

        return true;
    }

    public boolean doDepot(Task task) {
        updateTask(task);

        return true;
    }

    public boolean doAnyWear(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        // 装备Id
        int equipId = triggers.get(0);
        List<List<Integer>> param = getParam(task.getTaskId());
        if (param == null || param.size() <= 0) {
            updateTask(task);
            return true;
        } else {
            if (param.size() != 1) {
                LogHelper.CONFIG_LOGGER.info("doAwardEquip param error");
                return false;
            }

            List<Integer> config = param.get(0);
            if (config == null || config.size() < 1) {
                LogHelper.CONFIG_LOGGER.info("doAwardEquip config error");
                return false;
            }

            boolean needSyn = false;
            for (int i = 0; i < config.size(); i++) {
                // 当前条件已经完成
                if (task.isCondOk(i)) {
                    continue;
                }

                if (equipId == config.get(i)) {
                    updateTask(task, 1, i);
                    needSyn = true;
                }
            }
            return needSyn;
        }
    }

    public boolean doAnyMisionHire(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() <= 0) {
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() < 1) {
            LogHelper.CONFIG_LOGGER.info("doAwardEquip config error");
            return false;
        }

        for (Integer heroId : config) {
            if (heroManager.hasHero(player, heroId)) {
                updateTask(task);
                task.doneCond();
                return true;
            }
        }

        return false;
    }

    public boolean doAnySoldierTimes(Task task) {
        updateTask(task);
        return true;
    }

    public boolean doCommonHeroTask(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        updateTask(task, triggers.get(0));
        return true;
    }

    public boolean doGoodHeroTask(Task task, List<Integer> triggers) {
        if (triggers == null || triggers.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("trigger param size error");
            return false;
        }

        updateTask(task, triggers.get(0));
        return true;
    }

    public boolean doCheckAnyWear(Task task, List<List<Integer>> param, Player player) {
        // 装备Id
        if (param == null || param.size() <= 0) {
            updateTask(task);
            return true;
        } else {
            if (param.size() != 1) {
                LogHelper.CONFIG_LOGGER.info("doAwardEquip param error");
                return false;
            }

            List<Integer> config = param.get(0);
            if (config == null || config.size() < 1) {
                LogHelper.CONFIG_LOGGER.info("doAwardEquip config error");
                return false;
            }

            boolean needSyn = false;
            for (int i = 0; i < config.size(); i++) {
                // 当前条件已经完成
                if (task.isCondOk(i)) {
                    continue;
                }

                int equipId = config.get(i);
                if (heroManager.allHeroWearEquip(player, equipId)) {
                    updateTask(task, 1, i);
                    needSyn = true;
                }
            }
            return needSyn;
        }
    }

    // 检查下一个任务是否完成, again ?
    public boolean checkTask(Task task, Player player) {
        if (task.isCondOk()) {
            return false;
        }

        int taskType = getTypeChild(task.getTaskId());
        return checkTaskType(task, getParam(task.getTaskId()), player, taskType);
    }

    public boolean checkTaskType(Task task, List<List<Integer>> param, Player player, int taskType) {
        if (taskType == TaskType.NEW_STATE) {
            return doNewStateTask(task, param, player);
        } else if (taskType == TaskType.BUILDING_LEVELUP) {
            return checkUpBuilding(task, param, player);
        } else if (taskType == TaskType.START_TECH_UP || taskType == TaskType.FINISH_TECH) {
            return checkTechUp(task, param, player);
        } else if (taskType == TaskType.RES_BUILDING_LEVEL_UP) {
            return checkBuildingLevelUp(task, param, player);
        } else if (taskType == TaskType.SINGLE_HERO_WEAR) {
            return checkWearEquip(task, param, player);
        } else if (taskType == TaskType.POS_WEAR_EQUIP) {
            return checkPosWearEquip(task, param, player);
        } else if (taskType == TaskType.DONE_MISSION) {
            return checkMission(task, param, player);
        } else if (taskType == TaskType.MISSION_HIRE_HERO) {
            return checkHireHero(task, param, player);
        } else if (taskType == TaskType.LORD_LEVEL_UP) {
            return checkLordLevelUp(task, param, player);
        } else if (taskType == TaskType.HIRE_RESEARCHER) {
            return checkHireEmployee(task, param, player, EmplyeeType.RESEACHER);
        } else if (taskType == TaskType.HIRE_BLACKSMITH) {
            return checkHireEmployee(task, param, player, EmplyeeType.BLACK_SMITH);
        } else if (taskType == TaskType.HIRE_OFFIER) {
            return checkHireEmployee(task, param, player, EmplyeeType.OFFICER);
        } else if (taskType == TaskType.MAKE_KILL_EQUIP) {
            return docheckKillEquip(task, param, player);
        } else if (taskType == TaskType.LEVELUP_KILL_EQUIP) {
            return doCheckKillEquip(task, param, player);
        } else if (taskType == TaskType.ALL_HERO_WEARHAT) {
            return doCheckAllHeroWearHat(task, param, player);
        } else if (taskType == TaskType.ALL_HERO_WEAR_TWO) {
            return doAllHeroWearTwo(task, param, player);
        } else if (taskType == TaskType.ANY_WEAR) {
            return doCheckAnyWear(task, param, player);
        } else if (taskType == TaskType.MISSION_HIRE_ANY) {
            return doAnyMisionHire(task, param, player);
        } else if (taskType == TaskType.EMBATTLE_HERO) {
            return checkEmbattle(task, param, player);
        } else if (taskType == TaskType.START_LEVELUP_BUILDING) {
            return checkStartLevelBuilding(task, param, player);
        } else if (taskType == TaskType.KILL_REBEL) {
            return checkKillRebel(task, param, player);
        } else if (taskType == TaskType.ARMMY_RETURN) {
            return checkArmyReturn(player, task);
        } else if (taskType == TaskType.LEARN_FROM_TEACHER) {
            if (playerManager.getMaster(player, FriendType.MASTER).size() > 0) {
                updateTask(task);
                return true;
            }
            return false;
        } else if (taskType == TaskType.RECRUIT_STUDENTS) {
            if (playerManager.getMaster(player, FriendType.APPRENTICE).size() > 0) {
                updateTask(task);
                return true;
            }
            return false;
        } else if (taskType == TaskType.OMAMENT_WEARER) {
            return doWearOmamen(task, player);
        } else if (taskType == TaskType.HIRE_SCIENTIST) {
            return checkHireScientist(task, player);
        } else if (taskType == TaskType.COST_GOLD) {
            //消耗钻石
            return checkCostGold(task, player);
        } else if (taskType == TaskType.START_HIRE_SOLDIER) {
            return startHireSoldier(task, param, player);

        }else if (taskType == TaskType.COMPLETE_TOWER_DEFENSE) {
                return doTD(task,player);
        }
        return false;
    }

    /**
     * 判断玩家是否完成加速秒科技
     *
     * @param task
     * @param player
     * @return
     */
    public boolean checkHireScientist(Task task, Player player) {
        Tech tech = player.getTech();
        LinkedList<TechQue> techQues = tech.getTechQues();
        int queCount = 0;
        for (TechInfo e : tech.getTechInfoMap().values()) {
            if (e.getSpeed() > 0) {
                queCount += e.getSpeed();
            }
        }
        for (TechQue que : techQues) {
            if (que.getSpeed() == 1) {  //秒过
                queCount++;
            }
        }
        updateTask(task, queCount);
        return task.getStatus() == 1;
    }

    public boolean checkCostGold(Task task, Player player) {
        updateTask(task, player.getLord().getGoldCost());
        return task.getStatus() == 1;
    }

    public boolean checkKillRebel(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("killRebel param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("killRebel config error");
            return false;
        }

        int configRebelLv = config.get(0);
        int monsterLv = player.getMaxMonsterLv();
        if (monsterLv < configRebelLv) {
            return false;
        }

        updateTask(task);
        return true;
    }

    // 开始升级建筑
    public boolean checkStartLevelBuilding(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("killRebel param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("killRebel config error");
            return false;
        }

        int configId = config.get(0);
        int configLv = config.get(1);
        BuildingBase buildingBase = player.getBuilding(configId);
        if (buildingBase == null) {
            return false;
        }

        if (buildingBase.getLevel() + 1 == configLv) {
            Building buildings = player.buildings;
            if (buildings != null && buildings.isBuildUping(configId)) { // 当前建筑正在升级
                updateTask(task);
                return true;
            }
        }

        if (buildingBase.getLevel() < configLv) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 指定建筑升级
    public boolean checkUpBuilding(Task task, List<List<Integer>> param, Player player) {
        Building building = player.buildings;
        if (building == null) {
            return false;
        }
        if (param == null) {
            LogHelper.CONFIG_LOGGER.info("upbuilding param error");
            return false;
        }

        boolean needSyn = false;
        for (int i = 0; i < param.size(); i++) {
            List<Integer> elem = param.get(i);
            if (elem == null) {
                LogHelper.CONFIG_LOGGER.info("doUpBuilding elem is null");
                continue;
            }

            if (elem.size() != 2) {
                LogHelper.CONFIG_LOGGER.info("doUpBuilding elem size is nOot 2.");
                continue;
            }

            int configId = elem.get(0);
            int configLv = elem.get(1);
            int buildingLv = player.getBuildingLv(configId);
            if (buildingLv < configLv) {
                continue;
            }

            if (task.isCondOk(i)) {
                continue;
            }

            updateTask(task, 1, i);
            needSyn = true;
        }

        return needSyn;
    }

    // 检查科技升级,研究科技(开始研究:param: 科技类型, 等级, 进度)
    public boolean checkTechUp(Task task, List<List<Integer>> param, Player player) {
        Tech tech = player.getTech();
        if (tech == null) {
            return false;
        }
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doUpTech param error");
            return false;
        }

        List<Integer> techConfig = param.get(0);
        if (techConfig == null || techConfig.size() != 3) {
            LogHelper.CONFIG_LOGGER.info("doUpTech techConfig error");
            return false;
        }

        int type = techConfig.get(0);
        TechInfo techInfo = techManager.getTechInfo(player, type);
        if (techInfo == null) {
            return false;
        }

        int level = techConfig.get(1);
        // 已经大于当前等级了，直接完成
        if (techInfo.getLevel() > level) {
            updateTask(task);
            return true;
        }

        if (techInfo.getLevel() < level) {
            return false;
        }

        int process = techConfig.get(2);
        if (techInfo.getProcess() < process) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 多个资源建筑升级(非指定Id)
    public boolean checkBuildingLevelUp(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doUpResBuilding param error");
            return false;
        }

        List<Integer> resource = param.get(0);
        if (resource == null || resource.size() != 2) {
            LogHelper.CONFIG_LOGGER.info("doUpResBuilding resource error");
            return false;
        }

        int resType = resource.get(0);
        int resLevel = resource.get(1);

        // 当前个数
        int curNum = buildingManager.getResouceNum(player, resType, resLevel);
        int process = curNum - task.getProcess();
        process = Math.max(0, process);

        // 类型+等级
        if (process > 0) {
            updateTask(task, process);
            return true;
        }

        return false;
    }

    // 单个将领穿装备(param: 将领Id, 装备Id, 装备Id2)
    //  单个将领穿装备(param: 将领Id, 装备Id, 装备Id2)
    public boolean checkWearEquip(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("checkWearEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() <= 1) {
            LogHelper.CONFIG_LOGGER.info("checkWearEquip config.size() < 2");
            return false;
        }

        int heroId = config.get(0);
        Hero hero = player.getHero(heroId);
        if (hero == null) {
            return false;
        }

        // 0 默认完成
        boolean needSync = false;
        for (int i = 1; i < config.size(); i++) {
            // 当前条件已经完成
            if (task.isCondOk(i - 1)) { // 条件状态
                continue;
            }

            int equipId = config.get(i);
            if (hero.hasEquip(equipId)) {
                updateTask(task, 1, i - 1);
                needSync = true;
            }
        }

        return needSync;
    }

    public boolean startHireSoldier(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("checkWearEquip param error");
            return false;
        }
        List<Integer> config = param.get(0);
        if (config == null) {
            LogHelper.CONFIG_LOGGER.info("checkWearEquip config.size() < 2");
            return false;
        }
        // 0 默认完成
        boolean needSync = false;
        for (int i = 0; i < config.size(); i++) {
            // 当前条件已经完成
            if (task.isCondOk(i)) { // 条件状态
                continue;
            }
            int solderType = config.get(i);
            Soldier soldier = player.getSoldiers().get(solderType);
            if (!soldier.getWorkQues().isEmpty()) {
                updateTask(task, 1, i);
                needSync = true;
            }
        }
        return needSync;
    }

    public boolean checkPosWearEquip(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("checkWearEquip param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() <= 1) {
            LogHelper.CONFIG_LOGGER.info("checkWearEquip config.size() < 2");
            return false;
        }

        int heroId = config.get(0);
        Hero hero = player.getHero(heroId);
        if (hero == null) {
            return false;
        }

        // 0 默认完成
        boolean needSync = false;
        for (int i = 1; i < config.size(); i++) {
            // 当前条件已经完成
            if (task.isCondOk(i - 1)) { // 条件状态
                continue;
            }

            int pos = config.get(i);
            if (posIsWearEquip(hero, pos)) {
                updateTask(task, 1, i - 1);
                needSync = true;
            }
        }

        return needSync;
    }

    // 完成副本
    public boolean checkMission(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doneMission param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doneMission wearConfig error");
            return false;
        }

        int missionId = config.get(0);
        StaticMission staticMission = staticMissionMgr.getStaticMission(missionId);
        if (staticMission == null) {
            return false;
        }

        int mapId = staticMission.getMapId();
        Mission mission = missionManager.getMission(player, missionId, mapId);
        if (mission == null) {
            return false;
        }

        if (mission.getState() != MissionStateType.Complete) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 检测武将招募
    public boolean checkHireHero(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("missionHireHero param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("missionHireHero config error");
            return false;
        }

        int heroId = config.get(0);
        if (!heroManager.hasHero(player, heroId)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    // 主公升级
    public boolean checkLordLevelUp(Task task, List<List<Integer>> param, Player player) {
        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doLordLevelUp param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doLordLevelUp config error");
            return false;
        }

        int lordLv = player.getLevel();
        if (lordLv < config.get(0)) {
            return false;
        }

        updateTask(task);

        return true;
    }

    public boolean checkHireEmployee(Task task, List<List<Integer>> param, Player player, int emplyeeType) {
        EmployInfo employInfo = player.getEmployInfo();
        if (employInfo == null) {
            return false;
        }

        int employeeId = 0;
        if (emplyeeType == EmplyeeType.OFFICER) {
            employeeId = employInfo.getOfficerId();
        } else if (emplyeeType == EmplyeeType.RESEACHER) {
            employeeId = employInfo.getResearcherId();
        } else if (emplyeeType == EmplyeeType.BLACK_SMITH) {
            employeeId = employInfo.getBlackSmithId();
        }

        if (param == null || param.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doHireEmployee param error");
            return false;
        }

        List<Integer> config = param.get(0);
        if (config == null || config.size() != 1) {
            LogHelper.CONFIG_LOGGER.info("doHireEmployee config error");
            return false;
        }

        Employee employee = employInfo.getEmployee(employeeId);
        if (employee == null) {
            return false;
        }

        StaticEmployee staticEmployee = staticBuildingMgr.getEmployee(employeeId);
        if (staticEmployee == null) {
            return false;
        }

        if (staticEmployee.getLevel() < config.get(0)) {
            return false;
        }

        if (employee.getEndTime() <= System.currentTimeMillis()) {
            return false;
        }

        updateTask(task);

        return true;
    }

    public StaticTask getStaticTask(int taskId) {
        return staticTaskMgr.getStaticTask(taskId);
    }

    public StaticNewState getStaticNewState(int stateId) {
        return staticTaskMgr.getStaticNewState(stateId);
    }

    // 找到当前有升级建筑任务，返回所有建筑Id
    public List<Integer> getTaskBuilding(Player player) {
        Map<Integer, Task> taskMap = player.getTaskMap();
        TreeSet<Integer> mainBuildings = new TreeSet<Integer>();
        TreeSet<Integer> subBuildings = new TreeSet<Integer>();
        for (Task task : taskMap.values()) {
            if (task.isOk()) {
                continue;
            }
            // 指定Id建筑
            int typeChild = getTypeChild(task.getTaskId());
            if (typeChild == 0) {
                // LogHelper.CONFIG_LOGGER.info("getTaskBuilding task type is 0, taskId = " +
                // task.getTaskId());
                continue;
            }

            StaticTask staticTask = getStaticTask(task.getTaskId());
            if (staticTask == null) {
                // LogHelper.CONFIG_LOGGER.info("getTaskBuilding staticTask is null, taskId = " +
                // task.getTaskId());
                continue;
            }

            // 过滤某些任务
            if (staticTask.getTaskId() == 4 || staticTask.getTaskId() == 10) {
                if (!task.isCondOk()) {
                    continue;
                }
            }

            // 指定建筑升级
            if (typeChild == TaskType.BUILDING_LEVELUP || typeChild == TaskType.START_LEVELUP_BUILDING) {
                List<List<Integer>> param = staticTask.getParam();
                if (param == null) {
                    LogHelper.CONFIG_LOGGER.info("getTaskBuilding param is null");
                    continue;
                }

                if (param.size() <= 0) {
                    LogHelper.CONFIG_LOGGER.info("getTaskBuilding param.size() <= 0");
                    continue;
                }

                for (List<Integer> config : param) {
                    if (config.size() < 1) {
                        LogHelper.CONFIG_LOGGER.info("getTaskBuilding config.size() < 1");
                        continue;
                    }

                    int buildingId = config.get(0);
                    if (staticTask.getType() == 1) {
                        mainBuildings.add(buildingId);
                    } else if (staticTask.getType() == 2) {
                        subBuildings.add(buildingId);
                    }
                }

            } else if (typeChild == TaskType.RES_BUILDING_LEVEL_UP) {
                // 建筑status = 1的建筑
                List<List<Integer>> param = staticTask.getParam();
                if (param == null) {
                    LogHelper.CONFIG_LOGGER.info("getTaskBuilding param is null");
                    continue;
                }

                if (param.size() != 1) {
                    LogHelper.CONFIG_LOGGER.info("getTaskBuilding param.size() != 1");
                    continue;
                }

                List<Integer> config = param.get(0);
                if (config.size() < 2) {
                    LogHelper.CONFIG_LOGGER.info("getTaskBuilding config.size() < 1");
                    continue;
                }

                int resType = config.get(0);
                List<Integer> ids = playerManager.getBuildingRes(player, resType);
                for (Integer id : ids) {
                    if (staticTask.getType() == 1) {
                        mainBuildings.add(id);
                    } else if (staticTask.getType() == 2) {
                        subBuildings.add(id);
                    }
                }
            }
        }

        List<BuildingCompare> mainBcList = new ArrayList<BuildingCompare>();
        List<BuildingCompare> subBcList = new ArrayList<BuildingCompare>();

        List<Integer> lastResult = new ArrayList<Integer>();
        for (Integer buildingId : mainBuildings) {
            BuildingBase buildingBase = player.getBuilding(buildingId);
            if (buildingBase == null) {
                continue;
            }
            mainBcList.add(new BuildingCompare(buildingBase.getBuildingId(), buildingBase.getLevel()));
        }

        for (Integer buildingId : subBuildings) {
            BuildingBase buildingBase = player.getBuilding(buildingId);
            if (buildingBase == null) {
                continue;
            }
            subBcList.add(new BuildingCompare(buildingBase.getBuildingId(), buildingBase.getLevel()));
        }

        if (!mainBcList.isEmpty()) {
            Collections.sort(mainBcList);
        }

        if (!subBcList.isEmpty()) {
            Collections.sort(subBcList);
        }

        for (int i = 0; i < mainBcList.size(); i++) {
            BuildingCompare bc = mainBcList.get(i);
            if (bc == null) {
                continue;
            }
            lastResult.add(bc.getBuildingId());
        }

        for (int i = 0; i < subBcList.size(); i++) {
            BuildingCompare bc = subBcList.get(i);
            if (bc == null) {
                continue;
            }
            lastResult.add(bc.getBuildingId());
        }

        return lastResult;
    }

    // 新手引导开启建筑
    @Deprecated
    public void synBuildings(Player player, List<Integer> openBuildingId) {
        if (openBuildingId == null || openBuildingId.isEmpty()) {
            return;
        }

        Building buildings = player.buildings;
        int buildingLv = 1;
        List<BuildingBase> buildingBases = new ArrayList<BuildingBase>();
        for (Integer buildingId : openBuildingId) {
            StaticBuilding staticBuilding = staticBuildingMgr.getStaticBuilding(buildingId);
            if (staticBuilding == null) {
                continue;
            }
            if (staticBuilding.getBuildingType() == BuildingType.COMMAND) {
                Command command = buildings.getCommand();
                if (command.getLv() >= 1) {
                    continue;
                }

                command.initBase(buildingId, buildingLv);
                buildingBases.add(command.getBase());
            } else if (GameHelper.isCamp(staticBuilding.getBuildingType())) {
                Camp camp = buildings.getCamp();
                BuildingBase buildingBase = camp.getBuilding(buildingId);
                if (buildingBase != null && buildingBase.getLevel() >= 1) {
                    continue;
                }
                camp.addCamp(buildingId, buildingLv);
                buildingBases.add(camp.getBuilding(buildingId));
            } else if (staticBuilding.getBuildingType() == BuildingType.TECH) {
                Tech tech = buildings.getTech();
                if (tech.getLv() >= 1) {
                    continue;
                }
                tech.initBase(buildingId, buildingLv);
                buildingBases.add(tech.getBase());
            } else if (staticBuilding.getBuildingType() == BuildingType.WALL) {
                Wall wall = buildings.getWall();
                if (wall.getLv() >= 1) {
                    continue;
                }
                wall.initBase(buildingId, buildingLv);
                buildingBases.add(wall.getBase());
            } else if (staticBuilding.getBuildingType() == BuildingType.WORK_SHOP) {
                WorkShop workShop = buildings.getWorkShop();
                if (workShop.getLv() >= 1) {
                    continue;
                }
                workShop.initBase(buildingId, buildingLv);
                buildingBases.add(workShop.getBase());
            } else if (staticBuilding.getBuildingType() == BuildingType.WARE) {
                Ware ware = buildings.getWare();
                if (ware.getLv() >= 1) {
                    continue;
                }

                ware.initBase(buildingId, buildingLv);
                buildingBases.add(ware.getBase());
            } else if (GameHelper.isResourceBuilding(staticBuilding.getBuildingType())) {
                ResBuildings resBuildings = buildings.getResBuildings();
                BuildingBase buildingBase = resBuildings.getBuilding(buildingId);
                if (buildingBase != null && buildingBase.getLevel() >= 1) {
                    continue;
                }
                resBuildings.addResourceBuilding(buildingId, buildingLv);
                buildingBases.add(resBuildings.getBuilding(buildingId));
            } else if (staticBuilding.getBuildingType() == BuildingType.STAFF) {
                Staff staff = buildings.getStaff();
                if (staff.getLv() >= 1) {
                    continue;
                }
                staff.initBase(buildingId, buildingLv);
                buildingBases.add(staff.getBase());
            }

            // LogHelper.logBuilding(player, buildingId, buildingLv);
        }

        BuildingPb.SynBuildingRq.Builder builder = BuildingPb.SynBuildingRq.newBuilder();
        for (BuildingBase buildingBase : buildingBases) {
            builder.addBuilding(buildingBase.wrapPb());
        }

        if (player.isLogin && player.getChannelId() != -1) {
            BasePb.Base.Builder msg =
                PbHelper.createSynBase(
                    BuildingPb.SynBuildingRq.EXT_FIELD_NUMBER,
                    BuildingPb.SynBuildingRq.ext,
                    builder.build());
            checkOpenAutoBuild(openBuildingId, player, builder);
            GameServer.getInstance().sendMsgToPlayer(player, msg);
        }

        // 建筑开启计算战斗力
        buildingManager.caculateBattleScore(player);
    }

    public void checkOpenAutoBuild(
        List<Integer> openBuildingId, Player player, BuildingPb.SynBuildingRq.Builder builder) {
        if (!openBuildingId.contains(36)) {
            return;
        }

        player.setOnBuild(1);
        builder.setOnBuild(1);
    }

    // 登录时检查
    public void checkTask(Player player) {
        // checkFinishedTask(player);
        Map<Integer, Task> taskMap = player.getTaskMap();
        for (Task task : taskMap.values()) {
            StaticTask staticTask = staticTaskMgr.getStaticTask(task.getTaskId());
            if (staticTask == null) {
                continue;
            }
            task.setMaxProcess(staticTask.getProcess());
            int taskType = getTypeChild(task.getTaskId());
            if (taskType == TaskType.MISSION_HIRE_ANY) {
                doAnyMisionHire(task, getParam(task.getTaskId()), player);
            } else if (taskType == TaskType.ALL_HERO_WEAR_TWO) {
                doAllHeroWearTwo(task, getParam(task.getTaskId()), player);
            } else if (taskType == TaskType.FINISH_TECH) {
                checkTask(task, player);
            } else if (taskType == TaskType.ALL_HERO_WEARHAT) {
                doCheckAllHeroWearHat(task, getParam(task.getTaskId()), player);
            } else if (taskType == TaskType.LORD_LEVEL_UP) {
                checkLordLevelUp(task, getParam(task.getTaskId()), player);
            } else if (taskType == TaskType.MISSION_HIRE_HERO) {
                checkHireHero(task, getParam(task.getTaskId()), player);
            } else if (taskType == TaskType.LEARN_FROM_TEACHER) {
                if (playerManager.getMaster(player, FriendType.MASTER).size() > 0) {
                    updateTask(task);
                }
            } else if (taskType == TaskType.RECRUIT_STUDENTS) {
                if (playerManager.getMaster(player, FriendType.APPRENTICE).size() > 0) {
                    updateTask(task);
                }
            } else if (taskType == TaskType.RECOVER_BUILDING) {
                if (player.buildings.getRecoverBuilds().contains(getParam(task.getTaskId()).get(0).get(0))) {
                    doRecoverBuild(task);
                }
            }

            if (task.isCondOk()) {
                List<Integer> triggers = new ArrayList<Integer>();
                triggers.add(task.getTaskId());
                checkSubTask(TaskType.DONE_ANY_SUBTASK, player, triggers);
                recordFinished(task, player);
            }
        }

        // checkMainTask(player);

    }

    // 检测当前条件,开启任务,已经完成和开启的任务就不要开启了
    public Set<Integer> getMutiTriggerTask(int taskId, Player player) {
        HashSet<Integer> openTask = new HashSet<Integer>();
        HashBasedTable<Integer, HashSet<Integer>, HashSet<Integer>> taskCondTable =
            staticTaskMgr.getTaskCondTable();
        Map<Integer, Map<HashSet<Integer>, HashSet<Integer>>> colMap = taskCondTable.rowMap();
        Map<HashSet<Integer>, HashSet<Integer>> checkMap = colMap.get(taskId);
        if (checkMap == null) {
            // LogHelper.CONFIG_LOGGER.info("check Map is null.");
            return openTask;
        }

        // 当前已经完成的任务
        TreeSet<Integer> finishedTask = player.getFinishedTask();
        Map<Integer, Task> taskMap = player.getTaskMap();
        if (checkMap.isEmpty()) {
            // LogHelper.CONFIG_LOGGER.info("checkMap is isEmpty.");
            return openTask;
        }

        // 检查任务是否完成，如果完成，则开启后续的任务
        for (Map.Entry<HashSet<Integer>, HashSet<Integer>> entry : checkMap.entrySet()) {
            HashSet<Integer> condKey = entry.getKey();
            HashSet<Integer> openValue = entry.getValue();
            boolean isOk = true;
            // check cond
            for (Integer checkTaskId : condKey) {
                if (checkTaskId == 0) {
                    continue;
                }

                // 只要有一个任务没有完成就不要开启任务
                if (!finishedTask.contains(checkTaskId)) {
                    isOk = false;
                    break;
                }
            }

            if (!isOk) {
                continue;
            }

            // open task
            for (Integer openTaskId : openValue) {
                if (finishedTask.contains(openTaskId) || taskMap.containsKey(openTaskId)) {
                    continue;
                }
                openTask.add(openTaskId);
            }
        }

        return openTask;
    }

    public void recordFinished(Task task, Player player) {
        if (task.isCondOk()) {
            player.getFinishedTask().add(task.getTaskId());
        }
    }

    // 遍历配置, 查找MutiTrigger, 如果MutiTigger里面有这个任务,
    // 则检查是否所有的除了这个任务意外的任务有没有
    // 完成，如果有完成，则开启当前的任务
    // 如果开启的任务已经完成则return
    public Set<Integer> getOpenTask(int curTaskId, Player player) {
        TreeSet<Integer> finishedTask = player.getFinishedTask();
        Map<Integer, StaticTask> taskConfig = staticTaskMgr.getTaskMap();
        Set<Integer> openTask = new HashSet<Integer>();
        // 1.遍历配置, 查找MutiTrigger
        for (StaticTask staticTask : taskConfig.values()) {
            if (staticTask == null) {
                continue;
            }
            List<Integer> mulTriggerId = staticTask.getMulTriggerId();
            // 2.如果MutiTigger里面没有这个任务
            if (!mulTriggerId.contains(curTaskId)) {
                continue;
            }

            // 3.则检查是否所有的除了这个任务意外的任务有没有完成
            boolean isAllOk = true;
            for (Integer taskId : mulTriggerId) {
                if (curTaskId == taskId) {
                    continue;
                }

                // 4.只要有一个任务没有完成，则不开启任务
                if (!finishedTask.contains(taskId)) {
                    isAllOk = false;
                    break;
                }
            }

            if (isAllOk) {
                openTask.add(staticTask.getTaskId());
            }
        }

        return openTask;
    }

    public List<StaticTask> getLineTask() {
        return staticTaskMgr.getTaskMap().values().stream().filter(e -> e.getType() == 3).collect(Collectors.toList());
    }


    public int getComplateTask(Player player) {
        if (!staticOpenManger.isOpen(OpenConsts.OPEN_69, player)) {
            return 0;
        }
        int count = 0;
        PlayerDailyTask playerDailyTask = player.getPlayerDailyTask();
        for (StaticTaskDaily dailyTask : dailyTaskMgr.getDailyMap().values()) {
            if (playerDailyTask.getTaskState(dailyTask.getId()) == 1) {
                continue;
            }
            if (playerDailyTask.getTaskCount(dailyTask.getId()) >= dailyTask.getNeedTime()) {
                count++;
            }
        }

        //未领取的活跃宝箱数量
        for (StaticTaskDailyAward award : dailyTaskMgr.getDailyAwardMap().values()) {
            if (playerDailyTask.getActiveState(award.getId()) == 1) {
                continue;
            }
            if (playerDailyTask.getActive() >= award.getNeedNum()) {
                count++;
            }
        }

        return count;
    }

    private boolean posIsWearEquip(Hero hero, int pos) {
        for (HeroEquip heroEquip : hero.getHeroEquips()) {
            if (heroEquip == null) {
                continue;
            }
            Equip equip = heroEquip.getEquip();
            if (equip == null) {
                continue;
            }
            StaticEquip staticEquip = equipDataMgr.getStaticEquip(equip.getEquipId());
            if (staticEquip == null) {
                continue;
            }
            if (staticEquip.getEquipType() == pos) {
                return true;
            }
        }
        return false;
    }
}
