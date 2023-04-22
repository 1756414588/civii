package com.game.domain.s;

// 玩家被击飞
public class StaticPlayerKick {
    private int mapType;
    private int currentMap;
    private int up1;
    private int up2;
    private int down1;
    private int down2;
    private int otherSameMap;

    public int getMapType() {
        return mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public int getCurrentMap() {
        return currentMap;
    }

    public void setCurrentMap(int currentMap) {
        this.currentMap = currentMap;
    }

    public int getUp1() {
        return up1;
    }

    public void setUp1(int up1) {
        this.up1 = up1;
    }

    public int getUp2() {
        return up2;
    }

    public void setUp2(int up2) {
        this.up2 = up2;
    }

    public int getDown1() {
        return down1;
    }

    public void setDown1(int down1) {
        this.down1 = down1;
    }

    public int getDown2() {
        return down2;
    }

    public void setDown2(int down2) {
        this.down2 = down2;
    }

    public int getOtherSameMap() {
        return otherSameMap;
    }

    public void setOtherSameMap(int otherSameMap) {
        this.otherSameMap = otherSameMap;
    }
}
