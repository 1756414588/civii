package com.game.domain.s;


import java.util.List;

// 建筑信息
public class StaticBuilding {
    private int buildingId;
    private String name;
    private int buildingType;
    private int initLevel;
    private int isOpen;
    private int rebuild;
    private List<List<Integer>> rebuildingId;

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

    public int getInitLevel() {
        return initLevel;
    }

    public void setInitLevel(int initLevel) {
        this.initLevel = initLevel;
    }

    public int getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }

    public int getRebuild() {
        return rebuild;
    }

    public void setRebuild(int rebuild) {
        this.rebuild = rebuild;
    }

    public List<List<Integer>> getRebuildingId() {
        return rebuildingId;
    }

    public void setRebuildingId(List<List<Integer>> rebuildingId) {
        this.rebuildingId = rebuildingId;
    }

    public List<Integer> getRebuildInfo(int rebuildId) {
        if(this.rebuildingId==null){
            return  null;
        }
        for (List<Integer> re : rebuildingId) {
            if (re.get(1).intValue() == rebuildId) {
                return re;
            }

        }
        return null;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
