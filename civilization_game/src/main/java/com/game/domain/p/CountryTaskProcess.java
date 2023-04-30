package com.game.domain.p;

/**
 *
 * @date 2019/12/24 9:17
 * @description
 */
public class CountryTaskProcess{

    /**
     * 区域
     */
    private int area;

    /**
     * 国家id
     */
    private int countryId;
    /**
     * 进度
     */
    private int process;
    /**
     * 该任务攻打据点损失的兵力
     */
    private int lossSoldier;

    /**
     * 最后次刷新世界
     */
    private long lastRefreshTime = 0;


    /**
     * 积分(世界进程排行都通过这个来)
     */
    private int points;

    public CountryTaskProcess(){


    }

    public CountryTaskProcess(int area,int camp){
        this.area = area;
        this.countryId  =camp;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public int getLossSoldier() {
        return lossSoldier;
    }

    public void setLossSoldier(int lossSoldier) {
        this.lossSoldier = lossSoldier;
    }

    public long getLastRefreshTime() {
        return lastRefreshTime;
    }

    public void setLastRefreshTime(long lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int point){
        this.points+=point;
    }

    @Override
    public String toString() {
        return "CountryTaskProcess{" +
                "area=" + area +
                ", countryId=" + countryId +
                ", process=" + process +
                ", lossSoldier=" + lossSoldier +
                ", lastRefreshTime=" + lastRefreshTime +
                ", points=" + points +
                '}';
    }
}
