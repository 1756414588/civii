package com.game.domain.s;

public class StaticBuildingType {
	private int buildingType;
	private int canUp;
	private int resourceType;
	private int initLv;
    private int maxLv;

    private int pros;

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

	public int getCanUp() {
		return canUp;
	}

	public void setCanUp(int canUp) {
		this.canUp = canUp;
	}

	public int getInitLv() {
		return initLv;
	}

	public void setInitLv(int initLv) {
		this.initLv = initLv;
	}

	public int getPros() {
		return pros;
	}

	public void setPros(int pros) {
		this.pros = pros;
	}

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public void setMaxLv(int maxLv) {
        this.maxLv = maxLv;
    }
}
