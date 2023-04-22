package com.game.domain.p;

/**
 * @author jyb
 * @date 2020/3/30 10:29
 * @description 活动类
 */
public class WorldActPlan {
    /**
     * 活动id
     */
    private int id;

    /**
     * 世界目标成功的时间 null 的时候 说明没有解锁
     */
    private long targetSuccessTime;
    /**
     * 开启的时间
     */
    private long openTime;

    /**
     * 预热开始时间
     */
    private long preheatTime;
    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 0 未开启   1 预热   3 开始 4 结束
     */
    private int state;

    private long refushTime;

	private long enterTime;// 可进入时间

	private long exhibitionTime;// 展示结束时间

    public long getRefushTime() {
        return refushTime;
    }

    public void setRefushTime(long refushTime) {
        this.refushTime = refushTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTargetSuccessTime() {
        return targetSuccessTime;
    }

    public void setTargetSuccessTime(long targetSuccessTime) {
        this.targetSuccessTime = targetSuccessTime;
    }

    public long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(long openTime) {
        this.openTime = openTime;
    }

    public long getPreheatTime() {
        return preheatTime;
    }

    public void setPreheatTime(long preheatTime) {
        this.preheatTime = preheatTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

	public long getEnterTime() {
		return enterTime;
	}

	public void setEnterTime(long enterTime) {
		this.enterTime = enterTime;
	}

	public long getExhibitionTime() {
		return exhibitionTime;
	}

	public void setExhibitionTime(long exhibitionTime) {
		this.exhibitionTime = exhibitionTime;
	}

    @Override
    public String toString() {
		return "WorldActPlan{" + "id=" + id + ", state=" + state + '}';
    }
}
