package com.game.domain.p;

import com.game.constant.StaffTaskState;
import com.game.pb.CommonPb;

public class StaffTask {
    private int taskId; // 任务Id
    private int status; // 任务状态 0 未开启 1未完成  2.已经达成 3.已激活

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public CommonPb.StaffTask.Builder wrap() {
        CommonPb.StaffTask.Builder task = CommonPb.StaffTask.newBuilder();
        task.setTaskId(taskId);
        task.setStatus(status);
        return task;
    }

    public void unwrap(CommonPb.StaffTask data) {
        taskId = data.getTaskId();
        status = data.getStatus();
    }

    public boolean isActivated() {
        return status == StaffTaskState.ACTIVATED;
    }

    public boolean isNotActivated() {
        return status != StaffTaskState.ACTIVATED;
    }

    public void activated() {
        status = StaffTaskState.ACTIVATED;
    }
}
