package com.game.domain.s;

/**
 *
 * @date 2020/9/24 10:31
 * @description 虫族入侵，信物商店
 */
public class StaticActRoitItemShop {
    private int keyId;//INT	索引号
    private int itemNum;//INT	售卖价格，需要信物的数量
    private int gold;//INT	售卖价格，需要钻石的数量
    private int maxNum;//	INT	最大数量
    private String icon;//	VARCHAR	图标名称索引
    private String name;//	VARCHAR	道具名称
    private String detail;//	VARCHAR	道具描述
    private String effectList;//	VARCHAR	购买后的效果[效果类型，效果值]

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getEffectList() {
        return effectList;
    }

    public void setEffectList(String effectList) {
        this.effectList = effectList;
    }
}
