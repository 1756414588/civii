package com.game.domain.p;

import com.game.domain.Award;

/**
 * @author CaoBing
 * @date 2020/12/23 16:23
 */
public class WarBookShopItem {
    private int pos; //商品的栏位
    private Award award; //物品
    private int isbuy; //是否已经购买
    private Award price; //物品购买的价格
    private int isFreeBuy;  //是否免费购买
    private int place;

    public WarBookShopItem(Award award, int isbuy, Award price,int place) {
        this.award = award;
        this.isbuy = isbuy;
        this.price = price;
        this.isFreeBuy = isFreeBuy;
        this.place = place;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public Award getAward() {
        return award;
    }

    public void setAward(Award award) {
        this.award = award;
    }

    public int getIsbuy() {
        return isbuy;
    }

    public void setIsbuy(int isbuy) {
        this.isbuy = isbuy;
    }

    public Award getPrice() {
        return price;
    }

    public void setPrice(Award price) {
        this.price = price;
    }

    public int getIsFreeBuy() {
        return isFreeBuy;
    }

    public void setIsFreeBuy(int isFreeBuy) {
        this.isFreeBuy = isFreeBuy;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return "WarBookShopItem{" +
               "pos=" + pos +
               ", award=" + award +
               ", isbuy=" + isbuy +
               ", price=" + price +
               ", isFreeBuy=" + isFreeBuy +
               ", place=" + place +
               '}';
    }
}
