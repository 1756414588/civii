package com.game.domain.p;

import com.game.domain.s.ActShopItem;
import com.game.pb.DataPb;

/**
 * @author 陈奎
 * @version 1.0
 * @filename
 * @time 2017-3-8 上午10:40:16
 * @describe
 */
public class ActShopProp implements Cloneable {

    private int grid;
    private int propId;
    private int propNum;
    private int price;
    private int isBuy;

    @Override
    public ActShopProp clone() {
        ActShopProp actShopProp = null;
        try {
            actShopProp = (ActShopProp) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return actShopProp;
    }

    public ActShopProp(int grid, int propId, int propNum, int price) {
        this.grid = grid;
        this.propId = propId;
        this.propNum = propNum;
        this.price = price;
        this.isBuy = 1;
    }

    public ActShopProp(int grid, ActShopItem actShopItem) {
        this.grid = grid;
        this.propId = actShopItem.getId();
        this.propNum = actShopItem.getCount();
        this.price = actShopItem.getPrice();
        this.isBuy = 1;
    }

    public ActShopProp(DataPb.ActShopProp pb) {
        this.grid = pb.getGrid();
        this.propId = pb.getPropId();
        this.propNum = pb.getPropNum();
        this.price = pb.getPrice();
        this.isBuy = pb.getIsBuy();
    }

    public int getGrid() {
        return grid;
    }

    public void setGrid(int grid) {
        this.grid = grid;
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public int getPropNum() {
        return propNum;
    }

    public void setPropNum(int propNum) {
        this.propNum = propNum;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getIsBuy() {
        return isBuy;
    }

    public void setIsBuy(int isBuy) {
        this.isBuy = isBuy;
    }

}
