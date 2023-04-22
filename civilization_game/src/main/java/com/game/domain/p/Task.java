package com.game.domain.p;

import com.game.pb.DataPb;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

// 任务
public class Task implements Cloneable {
    private int taskId; // 任务Id
    private int process; // 任务进度
    private int status; // 领奖状态 0未完成, 1已经达成 2.已经完成
    private Map<Integer, Integer> condMap = new HashMap<Integer, Integer>(); // 0不可领奖 1可以领奖 2.已经领完
    private int maxProcess;//最大任务进度

    @Override
    public Task clone() {
        Task task = null;
        try {
            task = (Task) super.clone();
            HashMap<Integer, Integer> map = new HashMap<>();
            this.condMap.forEach((key, value) -> {
                map.put(key, value);
            });
            task.setCondMap(map);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return task;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public int getStatus() {
        return status;
    }

    // 任务已经完成或者已经领取奖励
    public boolean isOk() {
        return status == 1 || status == 2;
    }

    // 任务已经完成或者已经领取奖励
    public boolean isFinished() {
        return status == 2;
    }


    public boolean isProcessing() {
        return status == 0 || status == 1;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DataPb.TaskData.Builder wrapPb() {
        DataPb.TaskData.Builder builder = DataPb.TaskData.newBuilder();
        builder.setTaskId(taskId);
        builder.setProcess(process);
        builder.setStatus(status);
        for (Map.Entry<Integer, Integer> elem : condMap.entrySet()) {
            if (elem == null) {
                continue;
            }
            Integer status = elem.getValue();
            if (status == null) {
                continue;
            }

            Integer condIndex = elem.getKey();
            if (condIndex == null) {
                continue;
            }
            DataPb.TaskCond.Builder taskCond = DataPb.TaskCond.newBuilder();
            taskCond.setStatus(status);
            taskCond.setCondIndex(condIndex);
            builder.addTaskCond(taskCond);
        }

        return builder;
    }

    public void unwrapPb(DataPb.TaskData builder) {
        taskId = builder.getTaskId();
        process = builder.getProcess();
        status = builder.getStatus();
        condMap.clear();
        for (DataPb.TaskCond taskCond : builder.getTaskCondList()) {
            condMap.put(taskCond.getCondIndex(), taskCond.getStatus());
        }
    }

    public Map<Integer, Integer> getCondMap() {
        return condMap;
    }

    public void setCondMap(Map<Integer, Integer> condMap) {
        this.condMap = condMap;
    }

    public void initCond(int condSize) {
        for (int i = 0; i < condSize; i++) {
            condMap.put(i, 0);
        }
    }

    public void updateCond(int condIndex) {
        condMap.put(condIndex, 1);
    }

    public int getCond(int condIndex) {
        Integer cond = condMap.get(condIndex);
        if (cond == null) {
            return 0;
        }

        return cond;
    }

    public boolean isCondOk(int condIndex) {
        return getCond(condIndex) != 0;
    }

    // 0 未完成 1 完成 2 领取完
    public boolean isCondOk() {
        for (Integer cond : condMap.values()) {
            if (cond == 0) {
                return false;
            }
        }

        return true;
    }

    public void setCond(int condIndex) {
        condMap.put(condIndex, 1);
    }

    public void unsetCond(int condIndex) {
        condMap.put(condIndex, 0);
    }

    public void doneCond() {
        int condSize = condMap.size();
        for (int i = 0; i < condSize; i++) {
            condMap.put(i, 1);
        }
    }

    public int getMaxProcess() {
        return maxProcess;
    }

    public void setMaxProcess(int maxProcess) {
        this.maxProcess = maxProcess;
    }

    @Override
    public String toString() {
        return "Task [taskId=" + taskId + ", process=" + process + ", status=" + status + ", condMap=" + condMap + "]";
    }
}
