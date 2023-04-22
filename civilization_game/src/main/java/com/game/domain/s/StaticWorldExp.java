package com.game.domain.s;

public class StaticWorldExp {
    private int keyId;
    private int entityLv;
    private int lostExp;
    private int killExp;
    private int entityType;



    public int getLostExp() {
        return lostExp;
    }

    public void setLostExp(int lostExp) {
        this.lostExp = lostExp;
    }

    public int getKillExp() {
        return killExp;
    }

    public void setKillExp(int killExp) {
        this.killExp = killExp;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityLv() {
        return entityLv;
    }

    public void setEntityLv(int entityLv) {
        this.entityLv = entityLv;
    }
}
