package com.game.domain.s;

/**
 * @author cpz
 * @date 2020/9/24 10:31
 * @description 虫族入侵 普通虫兵配置
 */
public class StaticActRoitMonster {
    private int keyId;//	INT	索引号
    private int type;//	INT	难度类型，根据世界目标的完成情况决定使用那一个难度
    private String dropLists;//	VARCHAR	掉落，用于配置掉落的信物道具
    private int monsterId;//	INT	怪物编号，对应s_world_monster中的索引号
    private int monsterNum;//	INT	单次刷新怪物的数量
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

    public String getDropLists() {
        return dropLists;
    }

    public void setDropLists(String dropLists) {
        this.dropLists = dropLists;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public int getMonsterNum() {
        return monsterNum;
    }

    public void setMonsterNum(int monsterNum) {
        this.monsterNum = monsterNum;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
