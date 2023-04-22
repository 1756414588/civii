package com.game.domain.p;

import java.util.HashMap;
import java.util.Map;

import com.game.pb.CommonPb;

//兵营
public class Camp implements Cloneable {
    //1.火箭 2.坦克 3.战车 4.民兵营
    //1.getBuildingId, 2.camp
    private Map<Integer, BuildingBase> camp = new HashMap<Integer, BuildingBase>();

    public int getBuildingLv(int buildingId) {
        BuildingBase buildingBase = getCamp().get(buildingId);
        if (buildingBase == null) {
            return Integer.MIN_VALUE;
        }
        return buildingBase.getLevel();
    }

    public BuildingBase getBuilding(int buildingId) {
        return camp.get(buildingId);
    }


    public int incrementLevel(int buildingId) {
        BuildingBase buildingBase = camp.get(buildingId);
        if (buildingBase != null) {
            buildingBase.incrementLevel();
            return buildingBase.getLevel();
        }
        return 1;
    }

    public void gmIncrementLevel(int buildingId, int level) {
        BuildingBase buildingBase = camp.get(buildingId);
        if (buildingBase != null) {
            buildingBase.setLevel(level);
        }
    }

    public CommonPb.Building.Builder wrapBase(int buildingId) {
        BuildingBase buildingBase = getCamp().get(buildingId);
        if (buildingBase != null) {
            return buildingBase.wrapPb();
        }

        return null;
    }

    public Map<Integer, BuildingBase> getCamp() {
        return camp;
    }

    public void setCamp(Map<Integer, BuildingBase> camp) {
        this.camp = camp;
    }

    public void addCamp(int buildingId, int buildingLv) {
        BuildingBase buildingBase = new BuildingBase(buildingId, buildingLv);
        camp.put(buildingId, buildingBase);
    }

    public CommonPb.Camp.Builder wrapPb() {
        CommonPb.Camp.Builder builder = CommonPb.Camp.newBuilder();
        for (Map.Entry<Integer, BuildingBase> item : camp.entrySet()) {
            BuildingBase buildingBase = item.getValue();
            if (buildingBase == null) {
                continue;
            }

            builder.addCamp(buildingBase.wrapPb());
        }
        return builder;
    }

    public void unwrapPb(CommonPb.Camp builder) {
        for (CommonPb.Building building : builder.getCampList()) {
            addCamp(building.getBuildingId(), building.getLv());
        }
    }

    public void setLevel(int buildingId, int level) {
        BuildingBase buildingBase = camp.get(buildingId);
        if (buildingBase != null) {
            buildingBase.setLevel(level);
        } else {
            BuildingBase base = new BuildingBase(buildingId, level);
            camp.put(buildingId, base);
        }
    }

    @Override
    public Camp clone() {
        Camp camp = null;
        try {
            camp = (Camp) super.clone();

            Map<Integer, BuildingBase> map1 = new HashMap<>();
            this.camp.forEach((integer, camp1) -> {
                map1.put(integer, camp1);
            });
            camp.setCamp(map1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return camp;
    }
}
