package com.game.domain.s;

/**
 * @author cpz
 * @date 2020/9/24 10:31
 * @description 虫族入侵 奖励配置
 */
public class StaticActRoitAward {
    private int keyId;//	INT	索引号
    private int killNum;//	INT	击杀数量
    private int gold;//	INT	金币奖励数量
    private int food;//	INT	食物奖励数量

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getKillNum() {
        return killNum;
    }

    public void setKillNum(int killNum) {
        this.killNum = killNum;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }
}
