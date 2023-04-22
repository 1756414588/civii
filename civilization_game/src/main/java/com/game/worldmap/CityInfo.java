package com.game.worldmap;

import com.game.flame.NodeType;

public class CityInfo extends Entity{
    private int mapId;      // 地图Id
    private long endTime;   // 征收的结束时间
    private int cityType;   // 城池类型

    public long getEndTime () {
        return endTime;
    }

    public void setEndTime (long endTime) {
        this.endTime = endTime;
    }

    public int getMapId () {
        return mapId;
    }

    public void setMapId (int mapId) {
        this.mapId = mapId;
    }

    public int getCityType () {
        return cityType;
    }

    public void setCityType (int cityType) {
        this.cityType = cityType;
    }

    @Override
    public NodeType getNodeType() {
        return null;
    }

    @Override
    public int getNodeState() {
        return 0;
    }
}
