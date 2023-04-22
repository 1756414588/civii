package com.game.domain.s;

import java.util.List;

// 新手引导
public class StaticNewState {
    private int stateId;
    private List<Integer> openBuilding;
    private int nextStateId;
    private int autoBuild;

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public List<Integer> getOpenBuilding() {
        return openBuilding;
    }

    public void setOpenBuilding(List<Integer> openBuilding) {
        this.openBuilding = openBuilding;
    }

    public int getNextStateId() {
        return nextStateId;
    }

    public void setNextStateId(int nextStateId) {
        this.nextStateId = nextStateId;
    }

    public int getAutoBuild() {
        return autoBuild;
    }

    public void setAutoBuild(int autoBuild) {
        this.autoBuild = autoBuild;
    }
}
