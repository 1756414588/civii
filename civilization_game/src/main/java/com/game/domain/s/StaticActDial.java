package com.game.domain.s;

/**
 *
 * @version 1.0
 * @filename
 * @time 2017-3-9 下午8:17:43
 * @describe
 */
public class StaticActDial {

    private int keyId;
    private int dialId;
    private int awardId;
    private int type;
    private int place;
    private int itemType;
    private int itemId;
    private int itemCount;
    private int limit;
    private int equipId;
    private int weight;
    private String asset;
    private int minGuarantee;//是否保底，大于等于1为需要保底
    private int beRecorded;
    private String desc;
    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getDialId() {
        return dialId;
    }

    public void setDialId(int dialId) {
        this.dialId = dialId;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public int getMinGuarantee() {
        return minGuarantee;
    }
    public void setMinGuarantee(int minGuarantee) {
        this.minGuarantee = minGuarantee;
    }

    public int getBeRecorded() {
        return beRecorded;
    }

    public void setBeRecorded(int beRecorded) {
        this.beRecorded = beRecorded;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "StaticActDial{" +
               "keyId=" + keyId +
               ", dialId=" + dialId +
               ", awardId=" + awardId +
               ", type=" + type +
               ", place=" + place +
               ", itemType=" + itemType +
               ", itemId=" + itemId +
               ", itemCount=" + itemCount +
               ", limit=" + limit +
               ", equipId=" + equipId +
               ", weight=" + weight +
               ", asset='" + asset + '\'' +
               ", minGuarantee=" + minGuarantee +
               '}';
    }
}
