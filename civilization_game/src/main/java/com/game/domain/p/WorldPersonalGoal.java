package com.game.domain.p;

import com.game.pb.CommonPb;

/**
 * @author jyb
 * @date 2019/12/24 15:13
 * @description 世界目标 个人目标进度
 */
public class WorldPersonalGoal implements Cloneable {
    private int taskId;
    private int process;
    private int state;  //0 未完成   1 已完成 未领取  2 已领取

    private boolean worldState; //0 未完成   1 已完成 未领取  2 已领取

    private int challengeNumber;//挑战的次数

    private long lastAttackBossTime;//上一次杀boss的时间

    public WorldPersonalGoal() {

    }

    public WorldPersonalGoal(int taskId) {
        this.taskId = taskId;
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


    public CommonPb.WorldPersonalGoal.Builder writeData() {
        CommonPb.WorldPersonalGoal.Builder builder = CommonPb.WorldPersonalGoal.newBuilder();
        builder.setTaskId(taskId);
        builder.setProcess(process);
        builder.setWorldState(worldState);
        builder.setState(state);
        builder.setChallengeNumber(challengeNumber);
        builder.setLastAttackBossTime(lastAttackBossTime);
        return builder;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean isWorldState() {
        return worldState;
    }

    public void setWorldState(boolean worldState) {
        this.worldState = worldState;
    }

    public int getChallengeNumber() {
        return challengeNumber;
    }

    public void setChallengeNumber(int challengeNumber) {
        this.challengeNumber = challengeNumber;
    }

    public long getLastAttackBossTime() {
        return lastAttackBossTime;
    }

    public void setLastAttackBossTime(long lastAttackBossTime) {
        this.lastAttackBossTime = lastAttackBossTime;
    }

    @Override
    public WorldPersonalGoal clone() {
        WorldPersonalGoal worldPersonalGoal = null;
        try {
            worldPersonalGoal = (WorldPersonalGoal) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return worldPersonalGoal;
    }
}
