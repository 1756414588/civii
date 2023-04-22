package com.game.domain.s;

/**
 * @author cpz
 * @date 2020/9/24 10:31
 * @description 虫族入侵 攻城虫兵配置
 */
public class StaticActRoitWaveMonster {
    private int keyId;//INT	索引号
    private int type;//INT	难度类型，根据世界目标的完成情况决定使用那一个难度
    private int wave;//INT	波次，决定怪物出现的顺序
    private int monsterList;//INT	怪物编号，对应s_world_monster中的索引号
    private String dropLists;//	VARCHAR	掉落，用于配置掉落的积分数量
    private String desc;//	VARCHAR	描述

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getWave() {
        return wave;
    }

    public void setWave(int wave) {
        this.wave = wave;
    }

    public int getMonsterList() {
        return monsterList;
    }

    public void setMonsterList(int monsterList) {
        this.monsterList = monsterList;
    }

    public String getDropLists() {
        return dropLists;
    }

    public void setDropLists(String dropLists) {
        this.dropLists = dropLists;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
