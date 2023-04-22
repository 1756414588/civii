package com.game.domain.s;

import java.util.List;


public class StaticWorldCity {
	private int cityId;
	private int mapId;
	private int type;
	private String name;
	private int level;
	private int canLv;
	private int x;
	private int y;
	private int length;
	private int period;
	private List<Integer> monsters;
	private List<Integer> preMonsters;
	private List<List<Integer>> output;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int rangex1;
    private int rangex2;
    private int rangey1;
    private int rangey2;
    private int ownPeriod;
    private int warPeriod;
    private int recoverSoldier;
    private int rebuildIron;
    private int rebuildCopper;
    private int rebuildSoldier;
    private int recoverTime;
    private long honor;
    private int cityScore;
    private List<List<Integer>> buff;



	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public int getMapId () {
		return mapId;
	}

	public void setMapId (int mapId) {
		this.mapId = mapId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getCanLv() {
		return canLv;
	}

	public void setCanLv(int canLv) {
		this.canLv = canLv;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

    public int getX1 () {
        return x1;
    }

    public void setX1 (int x1) {
        this.x1 = x1;
    }

    public int getX2 () {
        return x2;
    }

    public void setX2 (int x2) {
        this.x2 = x2;
    }

    public int getY1 () {
        return y1;
    }

    public void setY1 (int y1) {
        this.y1 = y1;
    }

    public int getY2 () {
        return y2;
    }

    public void setY2 (int y2) {
        this.y2 = y2;
    }

    public int getRangex1() {
        return rangex1;
    }

    public void setRangex1(int rangex1) {
        this.rangex1 = rangex1;
    }

    public int getRangex2() {
        return rangex2;
    }

    public void setRangex2(int rangex2) {
        this.rangex2 = rangex2;
    }

    public int getRangey1() {
        return rangey1;
    }

    public void setRangey1(int rangey1) {
        this.rangey1 = rangey1;
    }

    public int getRangey2() {
        return rangey2;
    }

    public void setRangey2(int rangey2) {
        this.rangey2 = rangey2;
    }

    public int getOwnPeriod() {
        return ownPeriod;
    }

    public void setOwnPeriod(int ownPeriod) {
        this.ownPeriod = ownPeriod;
    }

    public int getWarPeriod() {
        return warPeriod;
    }

    public void setWarPeriod(int warPeriod) {
        this.warPeriod = warPeriod;
    }

    public int getRecoverSoldier() {
        return recoverSoldier;
    }

    public void setRecoverSoldier(int recoverSoldier) {
        this.recoverSoldier = recoverSoldier;
    }

    public int getRebuildIron() {
        return rebuildIron;
    }

    public void setRebuildIron(int rebuildIron) {
        this.rebuildIron = rebuildIron;
    }

    public int getRebuildCopper() {
        return rebuildCopper;
    }

    public void setRebuildCopper(int rebuildCopper) {
        this.rebuildCopper = rebuildCopper;
    }

    public int getRebuildSoldier() {
        return rebuildSoldier;
    }

    public void setRebuildSoldier(int rebuildSoldier) {
        this.rebuildSoldier = rebuildSoldier;
    }

    public List<List<Integer>> getOutput() {
        return output;
    }

    public void setOutput(List<List<Integer>> output) {
        this.output = output;
    }

    public List<Integer> getMonsters() {
        return monsters;
    }

    public void setMonsters(List<Integer> monsters) {
        this.monsters = monsters;
    }

    public int getRecoverTime() {
        return recoverTime;
    }

    public void setRecoverTime(int recoverTime) {
        this.recoverTime = recoverTime;
    }

    public long getHonor() {
        return honor;
    }

    public void setHonor(long honor) {
        this.honor = honor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getPreMonsters() {
        return preMonsters;
    }

    public void setPreMonsters(List<Integer> preMonsters) {
        this.preMonsters = preMonsters;
    }

    public int getCityScore() {
        return cityScore;
    }

    public void setCityScore(int cityScore) {
        this.cityScore = cityScore;
    }


    public List<List<Integer>> getBuff() {
        return buff;
    }

    public void setBuff(List<List<Integer>> buff) {
        this.buff = buff;
    }
}
