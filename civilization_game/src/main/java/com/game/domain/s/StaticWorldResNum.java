package com.game.domain.s;

// 世界地图资源分布
public class StaticWorldResNum {
    private int id;
    private int mapType;
    private long count;
    private int level;
    private int type;


    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public int getMapType () {
        return mapType;
    }

    public void setMapType (int mapType) {
        this.mapType = mapType;
    }

    public long getCount () {
        return count;
    }

    public void setCount (long count) {
        this.count = count;
    }

    public int getLevel () {
        return level;
    }

    public void setLevel (int level) {
        this.level = level;
    }

    public int getType () {
        return type;
    }

    public void setType (int type) {
        this.type = type;
    }
}
