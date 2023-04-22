package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.domain.s.StaticNewState;
import com.game.domain.s.StaticTask;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StaticTaskMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;
    private Map<Integer, StaticTask> taskMap = new HashMap<Integer, StaticTask>();
    private Map<Integer, Set<Integer>> openTask = new HashMap<Integer, Set<Integer>>();
    private Map<Integer, StaticNewState> newStateMap = new HashMap<Integer, StaticNewState>();
    private HashBasedTable<Integer, HashSet<Integer>, HashSet<Integer>> taskCondTable = HashBasedTable.create();

    @Override
    public void init() throws Exception{
        setTaskMap(staticDataDao.selectTaskMap());
        openTask.clear();
        taskCondTable.clear();
        makeOpenTask();
        newStateMap = staticDataDao.selectStaticNewState();
        makeTaskTable();

    }


    public void makeOpenTask() {
        for (Map.Entry<Integer, StaticTask> staticTaskEntry : getTaskMap().entrySet()) {
            if (staticTaskEntry == null) {
                continue;
            }
            StaticTask staticTask = staticTaskEntry.getValue();
            if (staticTask == null) {
                continue;
            }

            int triggerId = staticTask.getTriggerId();
            Set<Integer> nextTask = getOpenTask().get(triggerId);
            if (nextTask == null) {
                nextTask = new HashSet<Integer>();
                nextTask.add(staticTask.getTaskId());
                getOpenTask().put(triggerId, nextTask);
            } else {
                nextTask.add(staticTask.getTaskId());
            }
        }
    }

    public StaticTask getStaticTask(int taskId) {
        return getTaskMap().get(taskId);
    }

    public StaticNewState getStaticNewState(int stateId) {
        return newStateMap.get(stateId);
    }

    public Map<Integer, Set<Integer>> getOpenTask () {
        return openTask;
    }

    public void setOpenTask (Map<Integer, Set<Integer>> openTask) {
        this.openTask = openTask;
    }

    public Set<Integer> getOpenTask(int taskId) {
        return openTask.get(taskId);
    }

    public Map<Integer, StaticTask> getTaskMap () {
        return taskMap;
    }

    public void setTaskMap (Map<Integer, StaticTask> taskMap) {
        this.taskMap = taskMap;
    }

    public Map<Integer, StaticNewState> getNewStateMap() {
        return newStateMap;
    }

    public void setNewStateMap(Map<Integer, StaticNewState> newStateMap) {
        this.newStateMap = newStateMap;
    }

    public int newStateNextStateId(int stateId) {
        StaticNewState staticNewState = newStateMap.get(stateId);
        if (staticNewState != null) {
            return staticNewState.getNextStateId();
        }

        return 0;
    }

    public void makeTaskTable() {
//        HashBasedTable<Integer, HashSet<Integer>, HashSet<Integer>> taskCondTable = HashBasedTable.create();
//        // HashMap<HashSet<Integer>, HashSet<Integer>> colMap =  openTask.columnMap();
//        // 创建任务的时候检查任务是否已经创建，如果已经创建，就不要再创建了，如果没有创建，则进行创建
//        Map<Integer, Map<HashSet<Integer>, HashSet<Integer>>> colMap =  taskCondTable.rowMap();
//        HashSet<Integer> finishedTask = new HashSet<Integer>();
//        finishedTask.add(10);
//        finishedTask.add(1);
//        // 检查任务是否完成，如果完成，则开启后续的任务
//        for (Map<HashSet<Integer>, HashSet<Integer>> value : colMap.values()) {
//            for (Map.Entry<HashSet<Integer>, HashSet<Integer>> entry : value.entrySet()) {
//                HashSet<Integer> innerKey = entry.getKey();
//                HashSet<Integer> innerValue = entry.getValue();
//                boolean isOk = true;
//                for (Integer checkTaskId : innerKey) {
//                    if (checkTaskId == 0) {
//                        continue;
//                    }
//
//                    if (!finishedTask.contains(checkTaskId)) {
//                        isOk = false;
//                        break;
//                    }
//                }
//
//                if (!isOk) {
//                    continue;
//                }
//
//                // 开启任务
//                for (Integer openTaskId : innerValue) {
//                    System.out.println("开启任务Id="+openTaskId);
//                }
//
//            }
//        }

        for (StaticTask config : taskMap.values()) {
            if (config == null) {
                continue;
            }
            List<Integer> mulTriggerId = config.getMulTriggerId();
            HashSet<Integer> cond = new HashSet<Integer>();
            for (Integer taskId : mulTriggerId) {
                cond.add(taskId);
            }

            for (Integer taskId : mulTriggerId) {
                HashSet<Integer> openTask = getTaskCondTable().get(taskId, cond);
                if (openTask == null) {
                    openTask = new HashSet<Integer>();
                    getTaskCondTable().put(taskId, cond, openTask);
                }
                openTask.add(config.getTaskId());
            }
        }

    }


    public HashBasedTable<Integer, HashSet<Integer>, HashSet<Integer>> getTaskCondTable() {
        return taskCondTable;
    }

    public void setTaskCondTable(HashBasedTable<Integer, HashSet<Integer>, HashSet<Integer>> taskCondTable) {
        this.taskCondTable = taskCondTable;
    }
}
