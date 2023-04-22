package com.game.domain.p;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.game.pb.CommonPb;
import com.game.worldmap.Entity;

// 所有资源建筑, 需要存盘
public class ResBuildings implements Cloneable {
    //id, resBuilding
    private Map<Integer, BuildingBase> res = new HashMap<Integer, BuildingBase>();

    @Override
    public ResBuildings clone() {
        ResBuildings resBuildings = null;
        try {
            resBuildings = (ResBuildings) super.clone();
            HashMap<Integer, BuildingBase> map = new HashMap<>();
            this.res.forEach((key, value) -> {
                map.put(key, value.clone());
            });
            resBuildings.setRes(map);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return resBuildings;
    }

    public int getBuildingLv(int buildingId) {
        BuildingBase buildingBase = getRes().get(buildingId);
        if (buildingBase == null) {
            return Integer.MIN_VALUE;
        }
        return buildingBase.getLevel();
    }

    public void incrementLevel(int buildingId) {
        BuildingBase buildingBase = getRes().get(buildingId);
        if (buildingBase != null) {
            buildingBase.incrementLevel();
        }
    }

    public void gmIncrementLevel(int buildingId, int level) {
        BuildingBase buildingBase = getRes().get(buildingId);
        if (buildingBase != null) {
            buildingBase.setLevel(level);
        }
    }

    public CommonPb.Building.Builder wrapBase(int buildingId) {
        BuildingBase buildingBase = getRes().get(buildingId);
        if (buildingBase != null) {
            return buildingBase.wrapPb();
        }

        return null;
    }


    public Map<Integer, BuildingBase> getRes() {
        return res;
    }

    public void setRes(Map<Integer, BuildingBase> res) {
        this.res = res;
    }

    public void addResourceBuilding(int buildingId, int buildingLv) {
        BuildingBase buildingBase = new BuildingBase(buildingId, buildingLv);
        res.put(buildingId, buildingBase);
    }


    public CommonPb.ResBuildings.Builder wrapPb() {
        CommonPb.ResBuildings.Builder builder = CommonPb.ResBuildings.newBuilder();
        for (BuildingBase buildingBase : res.values()) {
            if (buildingBase == null) {
                continue;
            }

            builder.addResBuilding(buildingBase.wrapPb());
        }

        return builder;
    }

    public void unwrapPb(CommonPb.ResBuildings builder) {
        for (CommonPb.Building building : builder.getResBuildingList()) {
            addResourceBuilding(building.getBuildingId(), building.getLv());
        }
    }

    public BuildingBase openResBuilding(int buildingId) {
        BuildingBase buildingBase = res.get(buildingId);
        if (buildingBase != null) {
            return buildingBase;
        }

        buildingBase = new BuildingBase();
        buildingBase.setBuildingId(buildingId);
        buildingBase.setLevel(1);
        res.put(buildingId, buildingBase);

        return buildingBase;
    }

    public BuildingBase getBuilding(int buildingId) {
        return res.get(buildingId);
    }


    public void setLevel(int buildingId, int level) {
        BuildingBase buildingBase = getRes().get(buildingId);
        if (buildingBase != null) {
            buildingBase.setLevel(level);
        }
    }

    public void removeBuilding(int buildingId) {
        Iterator<Map.Entry<Integer, BuildingBase>> it = res.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, BuildingBase> val = it.next();
            if (val.getKey().intValue() == buildingId) {
                it.remove();
                break;
            }
        }
//        res.remove(buildingId);
    }

}
