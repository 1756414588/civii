package com.game.uc;

/**
 * @author jyb
 * @date 2020/6/3 18:58
 * @description 游戏服实时统计
 */
public class ServerStatistics {
    /**
     * 服务器id
     */
    private int serverId;

    /**
     * 当前在线
     */
    private int onLineNum;

    /**
     * 今日最高在线
     */
    private int todayMaxOnLineNum;

    /**
     * 总最高在线
     */
    private int totalMaxOnLineNum;

    /**
     * 总注册人数
     */
    private int  registerNum;


    public ServerStatistics(int serverId, int onLineNum, int todayMaxOnLineNum, int totalMaxOnLineNum, int registerNum) {
        this.serverId = serverId;
        this.onLineNum = onLineNum;
        this.todayMaxOnLineNum = todayMaxOnLineNum;
        this.totalMaxOnLineNum = totalMaxOnLineNum;
        this.registerNum = registerNum;
    }

    public ServerStatistics() {
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getOnLineNum() {
        return onLineNum;
    }

    public void setOnLineNum(int onLineNum) {
        this.onLineNum = onLineNum;
    }

    public int getTodayMaxOnLineNum() {
        return todayMaxOnLineNum;
    }

    public void setTodayMaxOnLineNum(int todayMaxOnLineNum) {
        this.todayMaxOnLineNum = todayMaxOnLineNum;
    }

    public int getTotalMaxOnLineNum() {
        return totalMaxOnLineNum;
    }

    public void setTotalMaxOnLineNum(int totalMaxOnLineNum) {
        this.totalMaxOnLineNum = totalMaxOnLineNum;
    }

    public int getRegisterNum() {
        return registerNum;
    }

    public void setRegisterNum(int registerNum) {
        this.registerNum = registerNum;
    }
}
