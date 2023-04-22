package com.game.domain.s;

import java.util.List;

public class StaticKillEquip {
    private int equipId;
    private List<List<Integer>> compound;
    private int openLevel;

    public int getEquipId () {
        return equipId;
    }

    public void setEquipId (int equipId) {
        this.equipId = equipId;
    }

    public int getOpenLevel () {
        return openLevel;
    }

    public void setOpenLevel (int openLevel) {
        this.openLevel = openLevel;
    }


    public List<List<Integer>> getCompound () {
        return compound;
    }

    public void setCompound (List<List<Integer>> compound) {
        this.compound = compound;
    }
}
