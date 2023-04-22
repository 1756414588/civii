package com.game.domain.s;

import java.util.List;

/**
 * 通信证活动任务的配置类
 * @author CaoBing
 * @date 2020/10/15 20:21
 */
public class StaticPassPortTask {
    /**
     * 任务id，决定排序
     */
    private int id;
    /**
     * 类型：1为每日任务，2为周任务，3为月任务
     */
    private int type;
    /**
     * 任务类型
     */
    private int taskType;
    /**
     * 周数，代表第几周的任务，月任务和周任务都默认是0
     */
    private int weekNum;
    /**
     * 任务条件
     */
    private int cond;
    /**
     * 任务内容
     */
    private String content;
    /**
     * 任务奖励
     */
    private List<Integer> award;
    /**
     * 积分
     */
    private int score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTaskType() {
        return taskType;
    }

    public void setTaskType(int taskType) {
        this.taskType = taskType;
    }

    public int getWeekNum() {
        return weekNum;
    }

    public void setWeekNum(int weekNum) {
        this.weekNum = weekNum;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "StaticPassPortTask{" +
               "id=" + id +
               ", type=" + type +
               ", taskType=" + taskType +
               ", weekNum=" + weekNum +
               ", cond=" + cond +
               ", content='" + content + '\'' +
               ", award=" + award +
               ", score=" + score +
               '}';
    }
}
