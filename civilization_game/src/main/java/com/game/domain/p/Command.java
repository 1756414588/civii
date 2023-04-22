package com.game.domain.p;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.game.constant.CityActionType;
import com.game.domain.Player;
import com.game.pb.BuildingPb;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.CommandInfo;
import com.game.util.LogHelper;
import com.game.util.SynHelper;

/**
 * @author 司令部
 */
public class Command implements Cloneable {
    /**
     * 建筑信息
     */
    private BuildingBase base = new BuildingBase();
    /**
     * 征收剩余时间, [to delete]
     */
    private int collectTimeLeft;
    /**
     * 当前可征收次数
     */
    private int collectTimes;
    /**
     * 军事信息:邮件Id,军事信息
     */
    private Map<Integer, String> militaryInfo = new HashMap<Integer, String>();

    public Command() {

    }

    public BuildingBase getBase() {
        return base;
    }

    public void setBase(BuildingBase base) {
        this.base = base;
    }

    public int getCollectTimeLeft() {
        return collectTimeLeft;
    }

    public void setCollectTimeLeft(int collectTimeLeft) {
        this.collectTimeLeft = collectTimeLeft;
    }

    public int getCollectTimes() {
        return collectTimes;
    }

    public void setCollectTimes(int collectTimes) {
        this.collectTimes = collectTimes;
    }


    public Map<Integer, String> getMilitaryInfo() {
        return militaryInfo;
    }

    public void setMilitaryInfo(Map<Integer, String> militaryInfo) {
        this.militaryInfo = militaryInfo;
    }

    public CommandInfo.Builder wrapPb() {
        CommandInfo.Builder commandInfo = CommandInfo.newBuilder();
        commandInfo.setBuilding(base.wrapPb());
        commandInfo.setCollectTimeLeft(collectTimeLeft);
        commandInfo.setCollectTimes(collectTimes);
        return commandInfo;
    }

    public CommonPb.Building.Builder wrapBase() {
        return base.wrapPb();
    }

    public void unwrapPb(CommandInfo commandInfo) {
        base.unwrapPb(commandInfo.getBuilding());
        setBase(base);
        setCollectTimeLeft(commandInfo.getCollectTimeLeft());
        setCollectTimes(commandInfo.getCollectTimes());
    }

    public boolean isCityInfoOk(CityInfo info) {
        if (info == null) {
            return false;
        }

        if (info.getActionType() == CityActionType.HIRE_SOLDIER && info.getSoldierType() == 0) {
            return false;
        } else if (info.getActionType() == CityActionType.BUILDING_LEVEL_UP &&
                (info.getBuildingId() == 0 || info.getBuildingLevel() == 0)) {
            return false;
        }

        return true;

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

    public void initBase(int buildId, int buildingLevel) {
        base.setBuildingId(buildId);
        base.setLevel(buildingLevel);
    }

    @Override
    public Command clone() {
        Command command = null;
        try {
            command = (Command) super.clone();
            command.setBase(this.base.clone());

            HashMap<Integer, String> map1 = new HashMap<>();
            this.militaryInfo.forEach((integer, string) -> {
                map1.put(integer, string);
            });
            command.setMilitaryInfo(map1);

            Queue<CityInfo> list1 = new LinkedList<>();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return command;
    }
}
