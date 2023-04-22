package com.game.domain.s;

import java.util.List;

public class StaticRoitWaveMonster {
    private int keyId;
    private int type;
    private List<Integer> monsters;

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

    public List<Integer> getMonsters() {
        return monsters;
    }

    public void setMonsters(List<Integer> monsters) {
        this.monsters = monsters;
    }
}
