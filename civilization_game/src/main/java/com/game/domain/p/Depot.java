package com.game.domain.p;

import com.game.domain.Award;
import com.game.pb.CommonPb;

public class Depot implements Cloneable {

    private int grid;
    private int state;
    private int iron;
    private int gold;
    private int level;
    private Award award;

    @Override
    public Depot clone() {
        Depot depot = null;
        try {
            depot = (Depot) super.clone();
            depot.setAward(this.award.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return depot;
    }

    public int getGrid() {
        return grid;
    }

    public void setGrid(int grid) {
        this.grid = grid;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getIron() {
        return iron;
    }

    public void setIron(int iron) {
        this.iron = iron;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Award getAward() {
        return award;
    }

    public void setAward(Award award) {
        this.award = award;
    }

    public Depot() {

    }

    public Depot(int grid, int iron, int gold, int type, int id, int count) {
        this.grid = grid;
        this.iron = iron;
        this.gold = gold;
        this.award = new Award(type, id, count);
    }

    public Depot(CommonPb.Depot depot) {
        this.grid = depot.getGrid();
        this.state = depot.getState();
        this.iron = depot.getIron();
        this.gold = depot.getGold();
        this.level = depot.getLevel();
        this.award = new Award(depot.getAward());
    }

    public CommonPb.Depot ser() {
        CommonPb.Depot.Builder builder = CommonPb.Depot.newBuilder();
        builder.setGrid(grid);
        builder.setState(state);
        builder.setAward(award.ser());
        if (iron != 0) {
            builder.setIron(iron);
        }
        if (gold != 0) {
            builder.setGold(gold);
        }

        return builder.build();
    }

    public CommonPb.Depot serDb() {
        CommonPb.Depot.Builder builder = CommonPb.Depot.newBuilder();
        builder.setGrid(grid);
        builder.setState(state);
        builder.setIron(iron);
        builder.setGold(gold);
        builder.setLevel(level);
        builder.setAward(award.ser());
        return builder.build();
    }

}
