package com.game.domain.s;

import java.util.List;

/**
 * @filename
 * @author 陈奎
 * @version 1.0
 * @time 2017-2-18 下午3:11:33
 * @describe
 */
public class StaticWorldMap {
	private int mapId;
	private int centerCityId;
	private String name;
	private int register;
	private int areaType;
	private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int registerOrder;
	private int registerNum;
	private List<Integer> city;
	private int priority;
	private int bossId;
	private int belong;
	private int born;


	public int getMapId () {
		return mapId;
	}

	public void setMapId (int mapId) {
		this.mapId = mapId;
	}

	public int getRegister() {
		return register;
	}

	public void setRegister(int register) {
		this.register = register;
	}

	public int getRegisterOrder() {
		return registerOrder;
	}

	public void setRegisterOrder(int registerOrder) {
		this.registerOrder = registerOrder;
	}

	public int getRegisterNum() {
		return registerNum;
	}

	public void setRegisterNum(int registerNum) {
		this.registerNum = registerNum;
	}

	public List<Integer> getCity() {
		return city;
	}

	public void setCity(List<Integer> city) {
		this.city = city;
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

    public int getPriority () {
        return priority;
    }

    public void setPriority (int priority) {
        this.priority = priority;
    }

    public int getBossId () {
        return bossId;
    }

    public void setBossId (int bossId) {
        this.bossId = bossId;
    }


    public int getAreaType () {
        return areaType;
    }

    public void setAreaType (int areaType) {
        this.areaType = areaType;
    }

    public int getBelong() {
        return belong;
    }

    public void setBelong(int belong) {
        this.belong = belong;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCenterCityId() {
        return centerCityId;
    }

    public void setCenterCityId(int centerCityId) {
        this.centerCityId = centerCityId;
    }

    public int getBorn() {
        return born;
    }

    public void setBorn(int born) {
        this.born = born;
    }
}
