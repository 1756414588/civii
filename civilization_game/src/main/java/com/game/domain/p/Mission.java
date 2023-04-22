package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.util.TimeHelper;

public class Mission implements Cloneable {
    private int missionId;  // ok
    private int mapId;
    private int state;       // ok   //是否通关 0:未开启, 1:新开启, 2: 已通关(已通关的普通关卡不能打)
    private int star;       // ok
    private int type;
    private long resourceEndTime;
    private int countryItemNum;
    private boolean isHeroBought;
    private int resourceLandNum;
    private int buyTimes;
    private int fightTimes;
    private int buyEquipPaperTimes;

    public int getMissionId() {
        return missionId;
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getResourceEndTime() {
        return resourceEndTime;
    }

    public void setResourceEndTime(long resourceEndTime) {
        this.resourceEndTime = resourceEndTime;
    }

    public int getCountryItemNum() {
        return countryItemNum;
    }

    public void setCountryItemNum(int countryItemNum) {
        this.countryItemNum = countryItemNum;
    }

    public boolean isHeroBought() {
        return isHeroBought;
    }

    public void setHeroBought(boolean heroBought) {
        isHeroBought = heroBought;
    }

    public int isResourceLandNum() {
        return resourceLandNum;
    }

    public void setResourceLandNum(int resourceLandNum) {
        this.resourceLandNum = resourceLandNum;
    }

    public CommonPb.Mission wrapPb() {
        CommonPb.Mission.Builder builder = CommonPb.Mission.newBuilder();
        builder.setId(missionId);
        builder.setStar(star);
        builder.setResourceTime(resourceEndTime);
        builder.setCountryPropNum(countryItemNum);
        builder.setIsHeroBuy(isHeroBought);
        builder.setResourceLandNum(resourceLandNum);
        builder.setFightTimes(fightTimes);
        builder.setBuyTimes(buyTimes);
        builder.setBuyEquipPaperTimes(buyEquipPaperTimes);
        builder.setState(state);

        return builder.build();
    }

    //数据库：反序列化
    public void unwrapPb(CommonPb.Mission mission) {
        missionId = mission.getId();
        star = mission.getStar();
        resourceEndTime = mission.getResourceTime();
        countryItemNum = mission.getCountryPropNum();
        isHeroBought = mission.getIsHeroBuy();
        resourceLandNum = mission.getResourceLandNum();
        fightTimes = mission.getFightTimes();
        buyTimes = mission.getBuyTimes();
        buyEquipPaperTimes = mission.getBuyEquipPaperTimes();
        state = mission.getState();

    }

    public int getBuyTimes() {
        return buyTimes;
    }

    public void setBuyTimes(int buyTimes) {
        this.buyTimes = buyTimes;
    }

    public int getFightTimes() {
        return fightTimes;
    }

    public void setFightTimes(int fightTimes) {
        this.fightTimes = fightTimes;
    }

    public int getLeftTime() {
        return (int) TimeHelper.getLeftTime(resourceEndTime);
    }


    public int getBuyEquipPaperTimes() {
        return buyEquipPaperTimes;
    }

    public void setBuyEquipPaperTimes(int buyEquipPaperTimes) {
        this.buyEquipPaperTimes = buyEquipPaperTimes;
    }

    public void increBuyEquipTimes() {
        buyEquipPaperTimes += 1;
    }

    public DataPb.MissionData.Builder writeData() {
        DataPb.MissionData.Builder builder = DataPb.MissionData.newBuilder();
        builder.setId(missionId);
        builder.setStar(star);
        builder.setResourceTime(resourceEndTime);
        builder.setCountryPropNum(countryItemNum);
        builder.setIsHeroBuy(isHeroBought);
        builder.setResourceLandNum(resourceLandNum);
        builder.setFightTimes(fightTimes);
        builder.setBuyTimes(buyTimes);
        builder.setBuyEquipPaperTimes(buyEquipPaperTimes);
        builder.setState(state);
        builder.setMapId(mapId);
        return builder;
    }

    public void readData(DataPb.MissionData mission) {
        missionId = mission.getId();
        star = mission.getStar();
        resourceEndTime = mission.getResourceTime();
        countryItemNum = mission.getCountryPropNum();
        isHeroBought = mission.getIsHeroBuy();
        resourceLandNum = mission.getResourceLandNum();
        fightTimes = mission.getFightTimes();
        buyTimes = mission.getBuyTimes();
        buyEquipPaperTimes = mission.getBuyEquipPaperTimes();
        state = mission.getState();
        mapId = mission.getMapId();
    }

    @Override
    public Mission clone() {
        Mission mission = null;
        try {
            mission = (Mission) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return mission;
    }
}
