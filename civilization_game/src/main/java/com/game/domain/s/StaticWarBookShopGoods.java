package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/23 11:00
 */
public class StaticWarBookShopGoods {
    private int id;//主键ID
    private int type;//商品类型
    private List<Integer> award;    //商品具体奖励：[类型,id,数量]（必须是单奖励）
    private List<Integer> price;    //商品购买所需要的物品：[类型,id,数量]（必须是单物品）
    private int place;    //商品排序位置，1为最前面

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public List<Integer> getPrice() {
        return price;
    }

    public void setPrice(List<Integer> price) {
        this.price = price;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return "StaticWarBookShopGoods{" +
               "id=" + id +
               ", type=" + type +
               ", award=" + award +
               ", price=" + price +
               ", place=" + place +
               '}';
    }
}
