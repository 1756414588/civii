package com.game.domain.p;

/**
 *
 * @date 2019/12/12 19:05
 * @description
 */
public class MeetingTask implements Cloneable {
    /**
     * 任务id
     */
    private int id;

    /**
     * 任务进度
     */
    private int process;

    /**
     * 任务状态  0 未开启任务 1 开启任务 ，任务中  2 已完成 未激活 ，3 已完成 激活
     */
    private int state;
    /**
    * 任务开始时间
    **/
    private long startTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "MeetingTask{" +
                "id=" + id +
                ", process=" + process +
                ", state=" + state +
                '}';
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public MeetingTask clone() {
        MeetingTask meetingTask = null;
        try {
            meetingTask = (MeetingTask) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return meetingTask;
    }
}
