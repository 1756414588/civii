package com.game.domain.s;

import java.util.List;

// world pvp battle cost
public class StaticPvpCost {
    private int actionId;
    private List<Integer> cost;

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public List<Integer> getCost() {
        return cost;
    }

    public void setCost(List<Integer> cost) {
        this.cost = cost;
    }
}
