package com.game.domain.s;

import java.util.List;

/**
 * @author CaoBing
 * @date 2021/1/28 11:42
 * 主城皮肤的配置类
 */
public class StaticBaseSkin {
    private int keyId;
    private String name;//名称
    private int time;//持续时间
    private int type;//消耗的升星石类型
    private int star;//初始星级
    private List<List<Integer>> needNum;//每个等级需要升星石的数量
    private int maxStar;//最大星级
    private List<Integer> rand;//升星的概率

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public List<List<Integer>> getNeedNum() {
        return needNum;
    }

    public void setNeedNum(List<List<Integer>> needNum) {
        this.needNum = needNum;
    }

    public int getMaxStar() {
        return maxStar;
    }

    public void setMaxStar(int maxStar) {
        this.maxStar = maxStar;
    }

    public List<Integer> getRand() {
        return rand;
    }

    public void setRand(List<Integer> rand) {
        this.rand = rand;
    }

    @Override
    public String toString() {
        return "StaticBaseSkin{" +
               "keyId=" + keyId +
               ", name='" + name + '\'' +
               ", time=" + time +
               ", type=" + type +
               ", star=" + star +
               ", needNum=" + needNum +
               ", maxStar=" + maxStar +
               ", rand=" + rand +
               '}';
    }
}
