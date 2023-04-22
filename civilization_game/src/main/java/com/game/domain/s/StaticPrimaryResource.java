package com.game.domain.s;

import java.util.List;

// 初级资源采集
public class StaticPrimaryResource {
    private int id;
    private int copper;
    private int cdTime;
    private List<List<Integer>> effect;

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public int getCopper () {
        return copper;
    }

    public void setCopper (int copper) {
        this.copper = copper;
    }

    public int getCdTime () {
        return cdTime;
    }

    public void setCdTime (int cdTime) {
        this.cdTime = cdTime;
    }

    public List<List<Integer>> getEffect () {
        return effect;
    }

    public void setEffect (List<List<Integer>> effect) {
        this.effect = effect;
    }
}
