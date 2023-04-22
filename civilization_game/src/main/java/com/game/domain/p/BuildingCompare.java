package com.game.domain.p;

public class BuildingCompare implements Comparable<BuildingCompare>{
    private int buildingId;
    private int buildingLv;

    public BuildingCompare() {
    }

    public BuildingCompare(int buildingId, int buildingLv) {
        this.setBuildingId(buildingId);
        this.setBuildingLv(buildingLv);
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildingLv() {
        return buildingLv;
    }

    public void setBuildingLv(int buildingLv) {
        this.buildingLv = buildingLv;
    }

    public int compareTo(BuildingCompare compareBc) {
        int buildingLv = compareBc.getBuildingLv();
        if (this.buildingLv < buildingLv) {
            return -1;
        }

        if (this.buildingLv > buildingLv) {
            return 1;
        }

        if (this.getBuildingId() < compareBc.getBuildingId()) {
            return -1;
        }

        if (this.getBuildingId() > compareBc.getBuildingId()) {
            return 1;
        }

        return 0;
    }

}
