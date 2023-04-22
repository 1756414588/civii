package com.game.dataMgr;

import com.game.dao.s.StaticDataDao;
import com.game.define.LoadData;
import com.game.domain.s.StaticNewState;
import com.game.domain.s.StaticTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@LoadData(name = "任务模块配置")
public class StaticTaskMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;
    private Map<Integer, StaticTask> taskMap = new HashMap<Integer, StaticTask>();
    // private Map<Integer, Set<Integer>> openTask = new HashMap<Integer, Set<Integer>>();
    private Map<Integer, StaticNewState> newStateMap = new HashMap<Integer, StaticNewState>();
    // private HashBasedTable<Integer, HashSet<Integer>, HashSet<Integer>> taskCondTable = HashBasedTable.create();

    private Map<Integer, List<StaticTask>> taskList = new HashMap<Integer, List<StaticTask>>();//主线完成引出得支线任务
    private Map<Integer, List<StaticTask>> allChapTaskList = new HashMap<Integer, List<StaticTask>>();//所有主线任务相关得支线
    private Map<Integer, StaticTask> mainNextTaskMap = new HashMap<Integer, StaticTask>();//主线
    private Map<Integer, List<StaticTask>> devNextTaskMap = new HashMap<Integer, List<StaticTask>>();//支线完成后引出得支线

    public static final int main = 1;
    public static final int dev = 2;

    @Override
    public void load() throws Exception {
        taskMap = staticDataDao.selectTaskMap();
        // openTask.clear();
        // taskCondTable.clear();
        // makeOpenTask();
        newStateMap = staticDataDao.selectStaticNewState();
        // makeTaskTable();

        taskList.clear();
        mainNextTaskMap.clear();
        devNextTaskMap.clear();
        allChapTaskList.clear();
        mainNextTaskMap = taskMap.values().stream().filter(x -> x.getType() == main && x.getNext()!=0).collect(Collectors.toMap(StaticTask::getNext, Function.identity()));
        allChapTaskList = taskMap.values().stream().filter(x -> x.getType() == dev).collect(Collectors.groupingBy(e -> e.getChapter_task()));
        taskList = taskMap.values().stream().filter(x -> x.getType() == dev && x.getNext() == 0).collect(Collectors.groupingBy(e -> e.getChapter_task()));
        devNextTaskMap = taskMap.values().stream().filter(x -> x.getType() == dev && x.getNext() != 0).collect(Collectors.groupingBy(e -> e.getNext()));
    }

    @Override
    public void init() throws Exception {

    }

    // public void makeOpenTask() {
    // for (Map.Entry<Integer, StaticTask> staticTaskEntry : getTaskMap().entrySet()) {
    // if (staticTaskEntry == null) {
    // continue;
    // }
    // StaticTask staticTask = staticTaskEntry.getValue();
    // if (staticTask == null) {
    // continue;
    // }
    //
    // int triggerId = staticTask.getTriggerId();
    // Set<Integer> nextTask = getOpenTask().get(triggerId);
    // if (nextTask == null) {
    // nextTask = new HashSet<Integer>();
    // nextTask.add(staticTask.getTaskId());
    // getOpenTask().put(triggerId, nextTask);
    // } else {
    // nextTask.add(staticTask.getTaskId());
    // }
    // }
    // }

    public StaticTask getStaticTask(int taskId) {
        return getTaskMap().get(taskId);
    }

    public StaticNewState getStaticNewState(int stateId) {
        return newStateMap.get(stateId);
    }

    // public Map<Integer, Set<Integer>> getOpenTask() {
    // return openTask;
    // }
    //
    // public void setOpenTask(Map<Integer, Set<Integer>> openTask) {
    // this.openTask = openTask;
    // }
    //
    // public Set<Integer> getOpenTask(int taskId) {
    // return openTask.get(taskId);
    // }

    public Map<Integer, StaticTask> getTaskMap() {
        return taskMap;
    }

    public void setTaskMap(Map<Integer, StaticTask> taskMap) {
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

    public List<StaticTask> getTaskList(int taskId) {
        return taskList.get(taskId);
    }

    public StaticTask getTaskByNext(int taskId) {
        return mainNextTaskMap.get(taskId);
    }

    public Map<Integer, StaticTask> getNextTaskMap() {
        return mainNextTaskMap;
    }

    public void setNextTaskMap(Map<Integer, StaticTask> nextTaskMap) {
        this.mainNextTaskMap = nextTaskMap;
    }

    public List<StaticTask> getDevNextTask(int taskId) {
        return devNextTaskMap.get(taskId);
    }

    public List<StaticTask> getAllChapTask(int taskId) {
        return allChapTaskList.get(taskId);
    }

}
