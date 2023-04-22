package com.game.domain.p;

public class Effect implements Cloneable {

    private int effectId;
    private int effect;
    private long beginTime;
    private long endTime;

    public int getEffectId() {
        return effectId;
    }

    public void setEffectId(int effectId) {
        this.effectId = effectId;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public Effect clone() {
        Effect effect = null;
        try {
            effect = (Effect) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return effect;
    }
}
