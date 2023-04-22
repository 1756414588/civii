package com.game.domain.s;

// 国家英雄逃跑概率
public class StaticCountryHeroEscape {
    private int keyId;
    private int escapeType;
    private int loyalty;
    private int rate;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getEscapeType() {
        return escapeType;
    }

    public void setEscapeType(int escapeType) {
        this.escapeType = escapeType;
    }

    public int getLoyalty() {
        return loyalty;
    }

    public void setLoyalty(int loyalty) {
        this.loyalty = loyalty;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
