package com.game.domain.p;

import com.game.pb.CommonPb;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class Item implements Cloneable {
    private long lordId;
    private int itemId;
    private int itemNum;

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public Item() {

    }

    public Item(int itemId, int count) {
        this.itemId = itemId;
        this.itemNum = count;
    }

    // 构造
    public Item(CommonPb.Prop item) {
        this.itemId = item.getPropId();
        this.itemNum = item.getPropNum();
    }

    public CommonPb.Prop wrapPb() {
        CommonPb.Prop.Builder builder = CommonPb.Prop.newBuilder();
        builder.setPropId(itemId);
        builder.setPropNum(itemNum);
        return builder.build();
    }


    public static void main(String[] args) {
        List<List<Integer>> list = new ArrayList<>();
        list.add(Lists.newArrayList(1, 2, 3));
        list.add(Lists.newArrayList(4, 5, 6));
        list.add(Lists.newArrayList(7, 8, 9));
        List<List<Integer>> copyList = new ArrayList<>();
        list.forEach(e -> {
            copyList.add(new ArrayList<>(e));
        });
        copyList.forEach(e -> {
            int a = e.get(0);
            e.set(0, a + 10);
        });

        copyList.forEach(e -> {
            //System.out.println(e.get(0) + "," + e.get(1) + "," + e.get(2));
        });
        //System.out.println("-------------------");
        list.forEach(e -> {
            //System.out.println(e.get(0) + "," + e.get(1) + "," + e.get(2));
        });
    }

    @Override
    public Item clone() {
        Item item = null;
        try {
            item = (Item) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return item;
    }
}
