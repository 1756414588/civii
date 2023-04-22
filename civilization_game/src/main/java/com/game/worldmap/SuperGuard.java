package com.game.worldmap;

import com.game.domain.s.StaticSuperRes;
import com.game.util.TimeHelper;


public class SuperGuard {
    private long armyArriveTime;// 部队达到的时刻
    private March march; // 部队信息
    private long startTime;// 开始的采集的时间
    private long maxCollectTime;// 最大采集时间配置的数据
    private long collectTime;// 已采集时间 用于状态切换时使用
    private long canMaxCollectTime;// 当前情况下可采集的最大时间
    private SuperResource superMine;// 所在的矿点

    public SuperGuard() {

    }

    /**
     * 创建一个新的超级矿点驻军
     */
    public SuperGuard(March march, SuperResource superMine, long now, long maxTime) {
        // 获取可采集最大时间
        this.march = march;
        this.maxCollectTime = maxTime;
        this.startTime = now;
        this.superMine = superMine;
        this.armyArriveTime = now;
    }

    /**
     * 停产状态
     *
     * @param now
     */
    public void stopState(int now) {
        long cTime = now - startTime;
        this.collectTime += cTime;// 已采集时间
        march.setEndTime(-1);// 部队时间设置为-1标志
    }

    /**
     * 停产 恢复到 生产
     *
     * @param now
     */
    public void reProducedState(long now) {
        this.startTime = now; // 重新开始采集
    }

    /**
     * 计算获取已经采集时间
     *
     * @return
     */
    public long calcCollectedTime(long now) {
        long time = superMine.getState() == SuperResource.STATE_PRODUCED ? now - startTime : 0;// 只有生产状态才能使用 now-startTime
        long collectedTime = time + collectTime;
        return Math.min(canMaxCollectTime, collectedTime);
    }

    /**
     * 计算已采集到的资源
     *
     * @param now
     * @return
     */
    public int calcCollectedResCnt(int now, StaticSuperRes res) {
        return (int) Math.floor((calcCollectedTime(now) * 1.0 / TimeHelper.HOUR_S) * res.getSpeed());
    }

    /**
     * 资源充足情况下还可以采集多长时间
     *
     * @param now
     * @return
     */
    public long furtherCollectTime(long now) {
        return canMaxCollectTime - calcCollectedTime(now);
    }

    /**
     * 设置部队时间
     *
     * @param now
     * @param durationTime
     */
    public void setArmyTime(long now, long durationTime) {
        if (superMine.getState() == SuperResource.STATE_PRODUCED) {
            march.setEndTime(now + durationTime);
        }
    }

    /**
     * 当矿点足够时并且是生产状态的采集时间计算
     *
     * @param now
     */
    public void setArmyTimeInEnoughRes(long now) {
        if (superMine.getState() == SuperResource.STATE_PRODUCED) {
            long endTime = canMaxCollectTime - calcCollectedTime(now);
            march.setEndTime(now + endTime);
            System.err.println(march.getLordId() + "==============" + march.getEndTime());
        }
    }

    /**
     * 矿点充足情况设置最大可采集量
     */
    public void setCanMaxCollectTimeEnoughRes() {
        this.canMaxCollectTime = maxCollectTime;
    }

    /**
     * 矿点资源不足情况设置最大可采集量
     *
     * @param now
     * @param durationTime
     */
    public void setCanMaxCollectTime(long now, long durationTime) {
        this.canMaxCollectTime = durationTime + calcCollectedTime(now);
    }

    /**
     * 是否是相同的army
     *
     * @param am
     * @return
     */
    public boolean isSameArmy(March am) {
        return am.getLordId() == this.march.getLordId() && am.getKeyId() == this.march.getKeyId();
    }

    public long getArmyArriveTime() {
        return armyArriveTime;
    }

    public void setArmyArriveTime(long armyArriveTime) {
        this.armyArriveTime = armyArriveTime;
    }

    public March getMarch() {
        return march;
    }

    public void setMarch(March march) {
        this.march = march;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getMaxCollectTime() {
        return maxCollectTime;
    }

    public void setMaxCollectTime(long maxCollectTime) {
        this.maxCollectTime = maxCollectTime;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public long getCanMaxCollectTime() {
        return canMaxCollectTime;
    }

    public void setCanMaxCollectTime(long canMaxCollectTime) {
        this.canMaxCollectTime = canMaxCollectTime;
    }

    public SuperResource getSuperMine() {
        return superMine;
    }

    public void setSuperMine(SuperResource superMine) {
        this.superMine = superMine;
    }
}
