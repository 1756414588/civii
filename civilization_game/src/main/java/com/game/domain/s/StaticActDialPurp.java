package com.game.domain.s;


import com.game.pb.CommonPb.Award;

import java.util.List;

/**
 * 紫装转盘信息
 *
 */
public class StaticActDialPurp {

    private int awardId; //活动奖励ID
    private List<List<Integer>> buyAward; //活动奖励ID
    private int freeTimes; //每日免费抽奖次数
    private int onePrice; //单抽价格
    private int tenPrice; //十连抽价格



    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getTenPrice() {
        return tenPrice;
    }

    public void setTenPrice(int tenPrice) {
        this.tenPrice = tenPrice;
    }

    public List<List<Integer>> getBuyAward() {
        return buyAward;
    }

    public void setBuyAward(List<List<Integer>> buyAward) {
        this.buyAward = buyAward;
    }

    public int getFreeTimes() {
        return freeTimes;
    }

    public void setFreeTimes(int freeTimes) {
        this.freeTimes = freeTimes;
    }

    public int getOnePrice() {
        return onePrice;
    }

    public void setOnePrice(int onePrice) {
        this.onePrice = onePrice;
    }
}
