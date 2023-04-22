package com.game.domain.s;

/**
 * @author cpz
 * @date 2020/9/24 10:31
 * @description 虫族入侵活动配置
 */
public class StaticActRoitTime {
    private int keyId;
    private int last1;//第一阶段持续时间（小时）
    private int last2;//第二阶段持续时间（小时）
    private int interval;//活动开启时间间隔（天）
    private int open;//作用未知
    private int flushInterval;//第一阶段野外虫族刷新时间间隔（秒）
    private int waveInterval1;//第二阶段攻城虫族刷新时间间隔（秒）（初级）
    private int waveInterval2;//第二阶段攻城虫族刷新时间间隔（秒）（高级）
    private int campAward;//当本阵营有任意玩家完成第二阶段全部波次，全阵营奖励积分数量
    private int campAwardMail;//奖励发放的邮件编号
    private int commandLv;//攻城目标需要的最低指挥中心等级

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getLast1() {
        return last1;
    }

    public void setLast1(int last1) {
        this.last1 = last1;
    }

    public int getLast2() {
        return last2;
    }

    public void setLast2(int last2) {
        this.last2 = last2;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getFlushInterval() {
        return flushInterval;
    }

    public void setFlushInterval(int flushInterval) {
        this.flushInterval = flushInterval;
    }

    public int getWaveInterval1() {
        return waveInterval1;
    }

    public void setWaveInterval1(int waveInterval1) {
        this.waveInterval1 = waveInterval1;
    }

    public int getWaveInterval2() {
        return waveInterval2;
    }

    public void setWaveInterval2(int waveInterval2) {
        this.waveInterval2 = waveInterval2;
    }

    public int getCampAward() {
        return campAward;
    }

    public void setCampAward(int campAward) {
        this.campAward = campAward;
    }

    public int getCampAwardMail() {
        return campAwardMail;
    }

    public void setCampAwardMail(int campAwardMail) {
        this.campAwardMail = campAwardMail;
    }

    public int getCommandLv() {
        return commandLv;
    }

    public void setCommandLv(int commandLv) {
        this.commandLv = commandLv;
    }
}
