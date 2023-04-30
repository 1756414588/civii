package com.game.rank;

import java.io.Serializable;

/**
 *
 * @date 2020/5/6 17:09
 * @description
 */
public class RankInfo implements Serializable,Comparable<RankInfo> {
    /**
     *
     */
    private static final long serialVersionUID = 7747348276467429864L;
    /**
     * 玩家ID ,或者其他主键
     */
    protected long key;
    /**
     * 排名数值，以此数值进行排名
     */
    protected int value;
    /**
     * 排名
     */
    protected int ranking = -1;

    /**
     * @param key
     */
    public RankInfo(int key) {
        super();
        this.key = key;
    }

    public RankInfo(long key, int value) {
        this.key = key;
        this.value = value;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    @Override
    public int compareTo(RankInfo o) {
        return o.value-value;
    }

	@Override
	public String toString() {
		return "RankInfo [key=" + key + ", value=" + value + ", ranking=" + ranking + "]";
	}

}
