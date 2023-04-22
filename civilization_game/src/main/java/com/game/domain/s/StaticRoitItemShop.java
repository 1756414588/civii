package com.game.domain.s;

import java.util.List;

public class StaticRoitItemShop {
    private int keyId;
    private int itemNum;
    private int gold;
    private int maxNum;
    private List<Integer> effect;
    private int propNum;
    private int propId;

    public int getPropNum() {
        return propNum;
    }

    public void setPropNum(int propNum) {
        this.propNum = propNum;
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getItemNum() {
        return itemNum;
    }

    public void setItemNum(int itemNum) {
        this.itemNum = itemNum;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public List<Integer> getEffect() {
        return effect;
    }

    public void setEffect(List<Integer> effect) {
        this.effect = effect;
    }
}
