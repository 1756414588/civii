package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2020/12/23 10:57
 */
public class StaticWarBookBuy {
    private int type;//商品类型：读取s_warbook_shop_buy表的type字段
    private int last;//是否是最后被剩下的类型，1为是，0为不是
    private List<List<Integer>> rand;//该类型商品出现的数量：[[数量，概率],[...]]

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLast() {
        return last;
    }

    public void setLast(int last) {
        this.last = last;
    }

    public List<List<Integer>> getRand() {
        return rand;
    }

    public void setRand(List<List<Integer>> rand) {
        this.rand = rand;
    }

    @Override
    public String toString() {
        return "StaticWarBookBuy{" +
               ", type=" + type +
               ", last=" + last +
               ", rand=" + rand +
               '}';
    }
}
