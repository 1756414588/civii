package com.game.domain.p;

import com.game.domain.s.StaticCountryTask;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;

/**
 * 国家任务记录
 */
public class CtyTask implements Cloneable {

    private int taskId;
    private int type;
    private int cond;
    private int state;
    private String taskName;

    public CtyTask() {

    }

    @Override
    public CtyTask clone() {
        CtyTask ctyTask = null;
        try {
            ctyTask = (CtyTask) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return ctyTask;
    }

    public CtyTask(StaticCountryTask staticCountryTask) {
        this.taskId = staticCountryTask.getTaskId();
        this.type = staticCountryTask.getType();
        this.cond = 0;
        this.state = 0;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public CommonPb.CountryTask.Builder wrapPb() {
        CommonPb.CountryTask.Builder builder = CommonPb.CountryTask.newBuilder();
        builder.setTaskId(taskId);
        builder.setCond(cond);
        builder.setState(state);
        return builder;
    }

    public DataPb.CountryTaskData.Builder writeData() {
        DataPb.CountryTaskData.Builder builder = DataPb.CountryTaskData.newBuilder();
        builder.setTaskId(taskId);
        builder.setType(type);
        builder.setCond(cond);
        builder.setState(state);
        return builder;
    }

    public void readData(DataPb.CountryTaskData data) {
        taskId = data.getTaskId();
        type = data.getType();
        cond = data.getCond();
        state = data.getState();
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
