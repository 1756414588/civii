package com.game.domain.p;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.game.pb.CommonPb;

//科技
public class Tech implements Cloneable {
    private BuildingBase base = new BuildingBase();
    // 科技等级信息
    private Map<Integer, TechInfo> techInfoMap = new HashMap<Integer, TechInfo>();
    // 科技研发进度
    private LinkedList<TechQue> techQues = new LinkedList<TechQue>();

    public BuildingBase getBase() {
        return base;
    }

    public void setBase(BuildingBase base) {
        this.base = base;
    }


    public int getLv() {
        return base.getLevel();
    }

    public int getBuildingId() {
        return base.getBuildingId();
    }

    public void incrementLevel() {
        base.incrementLevel();
    }

    public CommonPb.Building.Builder wrapBase() {
        return base.wrapPb();
    }

    public void initBase(int buildId, int buildingLevel) {
        base.setBuildingId(buildId);
        base.setLevel(buildingLevel);
    }

    public CommonPb.Tech.Builder wrapPb() {
        CommonPb.Tech.Builder builder = CommonPb.Tech.newBuilder();
        builder.setTech(wrapBase());

        return builder;
    }

    public void unwrapPb(CommonPb.Tech builder) {
        base.unwrapPb(builder.getTech());
    }

    public Map<Integer, TechInfo> getTechInfoMap() {
        return techInfoMap;
    }

    public void setTechInfoMap(Map<Integer, TechInfo> techInfoMap) {
        this.techInfoMap = techInfoMap;
    }

    public LinkedList<TechQue> getTechQues() {
        return techQues;
    }

    public void setTechQues(LinkedList<TechQue> techQues) {
        this.techQues = techQues;
    }

    public TechInfo getTechInfo(int keyId) {
        return techInfoMap.get(keyId);
    }

    public void addTechInfo(int keyId, TechInfo techInfo) {
        techInfoMap.put(keyId, techInfo);
    }

    @Override
    public Tech clone() {
        Tech tech = null;
        try {
            tech = (Tech) super.clone();
            tech.setBase(this.base.clone());

            Map<Integer, TechInfo> map1 = new HashMap<>();
            this.techInfoMap.forEach((integer, techInfo) -> {
                map1.put(integer, techInfo);
            });
            tech.setTechInfoMap(map1);

            LinkedList<TechQue> list1 = new LinkedList<>();
            this.techQues.forEach(techQue -> {
                list1.add(techQue);
            });
            tech.setTechQues(list1);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return tech;
    }

}
