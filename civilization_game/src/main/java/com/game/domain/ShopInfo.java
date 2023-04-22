package com.game.domain;

import java.util.ArrayList;
import java.util.List;

import com.game.domain.s.StaticProp;

public class ShopInfo implements Cloneable {

    // 时间
    private int time;

    // 折扣
    private List<Integer> shops = new ArrayList<Integer>();

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<Integer> getShops() {
        return shops;
    }

    public void setShops(List<Integer> shops) {
        this.shops = shops;
    }

    public boolean isDiscount(StaticProp staticProp) {
        if (shops.contains(staticProp.getPropId())) {
            return true;
        }
        return false;
    }

    @Override
    public ShopInfo clone() {
        ShopInfo shopInfo = null;
        try {
            shopInfo = (ShopInfo) super.clone();
            ArrayList<Integer> list = new ArrayList<>();
            list.addAll(this.shops);
            shopInfo.setShops(list);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return shopInfo;
    }
}
