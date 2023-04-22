package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/28 13:58
 */
public class StaticWarBookExchange {
    private int id;    // keyId，代表商品位置
    private List<Integer> award;//物品
    private int price;//购买价格，单位为钻石
    private int isDiscount;//是否是折扣商品，1为是，0为不是

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getIsDiscount() {
        return isDiscount;
    }

    public void setIsDiscount(int isDiscount) {
        this.isDiscount = isDiscount;
    }

    @Override
    public String toString() {
        return "StaticWarBookExchange{" +
               "id=" + id +
               ", award=" + award +
               ", price=" + price +
               ", isDiscount=" + isDiscount +
               '}';
    }
}
