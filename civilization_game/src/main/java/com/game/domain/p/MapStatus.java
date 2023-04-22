package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;

// 玩家地图开放的状态
public class MapStatus implements Cloneable {
    private int mapId;   // 地图Id
    private int status;  // 开放的状态  0 未开放 1 开放但是不能迁城 2.开放可以迁城

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public DataPb.MapStatusData.Builder writeData() {
        DataPb.MapStatusData.Builder builder = DataPb.MapStatusData.newBuilder();
        builder.setMapId(mapId);
        builder.setStatus(status);
        return builder;
    }

    public void readData(DataPb.MapStatusData data) {
        mapId = data.getMapId();
        status = data.getStatus();
    }


    public CommonPb.MapStatus.Builder wrapPb() {
        CommonPb.MapStatus.Builder builder = CommonPb.MapStatus.newBuilder();
        builder.setMapId(mapId);
        builder.setStatus(status);
        return builder;
    }

    public MapStatus(int mapId, int status) {
        this.mapId = mapId;
        this.status = status;
    }

    public MapStatus() {
    }

    @Override
    public MapStatus clone() {
        MapStatus mapStatus = null;
        try {
            mapStatus = (MapStatus) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return mapStatus;
    }
}
