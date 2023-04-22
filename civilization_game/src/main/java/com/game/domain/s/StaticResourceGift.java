package com.game.domain.s;

import java.util.List;

/**
 * @Description s_resource_gift
 * @ProjectName halo_server
 * @Date 2021/10/29 10:36
 **/
public class StaticResourceGift {
	private int keyId;
	private List<List<Integer>> awardList;
	private String name;
	private int money;
	private int limit;
	private int count;
	private int payid;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPayid() {
        return payid;
    }

    public void setPayid(int payid) {
        this.payid = payid;
    }

    @Override
    public String toString() {
        return "StaticResourceGift{" +
            "keyId=" + keyId +
            ", awardList=" + awardList +
            ", name='" + name + '\'' +
            ", money=" + money +
            ", limit=" + limit +
            ", count=" + count +
            ", payid=" + payid +
            '}';
    }
}
