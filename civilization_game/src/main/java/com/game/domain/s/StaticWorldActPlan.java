package com.game.domain.s;

import java.util.List;

/**
 *
 * @date 2020/1/17 15:45
 * @description
 */
public class StaticWorldActPlan {

    private int id ;// id;

    private String name;//名字

    private int targetId;//世界进程id

    private int openWeek;//解锁后第几周开启

    private int roundOpen;//是否循环

    private int roundType;//世界活动循环周数 (从开始时间从几周一循环

    private int weekTime;  // 开起当日的时间

    private int time;//开启当日时间(几点)

    private int preheat;//预热持续时间

    private List<Integer> continues; //世界活动时长持续参数

    private int lordLv ;//活动图标显示等级限制（指挥官等级）客户端用

    private int endTime;// 结束时间，分钟；为0的时候表示结束时间不确定

    private int nomal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getOpenWeek() {
        return openWeek;
    }

    public void setOpenWeek(int openWeek) {
        this.openWeek = openWeek;
    }

    public int getRoundOpen() {
        return roundOpen;
    }

    public void setRoundOpen(int roundOpen) {
        this.roundOpen = roundOpen;
    }

    public int getRoundType() {
        return roundType;
    }

    public void setRoundType(int roundType) {
        this.roundType = roundType;
    }

    public int getWeekTime() {
        return weekTime;
    }

    public void setWeekTime(int weekTime) {
        this.weekTime = weekTime;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getPreheat() {
        return preheat;
    }

    public void setPreheat(int preheat) {
        this.preheat = preheat;
    }

    public List<Integer> getContinues() {
        return continues;
    }

    public void setContinues(List<Integer> continues) {
        this.continues = continues;
    }

    public int getLordLv() {
        return lordLv;
    }

    public void setLordLv(int lordLv) {
        this.lordLv = lordLv;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getNomal() {
        return nomal;
    }

    public void setNomal(int nomal) {
        this.nomal = nomal;
    }
}
