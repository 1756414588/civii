package com.game.domain.s;

import com.google.common.collect.HashBasedTable;

// 叛军暴乱
public class StaticRiotMonster {
    private int keyId;
    private HashBasedTable<Integer, Integer, Integer> monster;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public HashBasedTable<Integer, Integer, Integer> getMonster() {
        return monster;
    }

    public void setMonster(HashBasedTable<Integer, Integer, Integer> monster) {
        this.monster = monster;
    }
}
