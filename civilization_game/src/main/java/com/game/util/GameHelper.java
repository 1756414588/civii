package com.game.util;
import com.game.constant.*;

public class GameHelper {
    public static boolean isValidResType(int resType) {
        return resType >= ResourceType.IRON && resType <= ResourceType.STONE;
    }



    public static boolean isCamp(int buildingType) {
        return buildingType == BuildingType.ROCKET_CAMP ||
                buildingType == BuildingType.TANK_CAMP ||
                buildingType == BuildingType.WAR_CAR_CAMP ||
                buildingType == BuildingType.MILITIA_CAMP;
    }

    public static boolean isResourceBuilding(int buildingType) {
        return buildingType == BuildingType.IRON ||
                buildingType == BuildingType.COPPER ||
                buildingType == BuildingType.OIL ||
                buildingType == BuildingType.STONE;
    }

    public static int getSoldierType(int itemId) {
        if (itemId == ItemId.ROCKET_ID) {
            return SoldierType.ROCKET_TYPE;
        } else if (itemId == ItemId.TANK_ID) {
            return SoldierType.TANK_TYPE;
        } else if (itemId == ItemId.WAR_CAR) {
            return SoldierType.WAR_CAR;
        }
        return -1;
    }

    public static int getSoldierIndexByBuildingId(int buildingId) {
        if (buildingId == BuildingId.ROCKET_CAMP) {
            return SoldierType.ROCKET_TYPE;
        } else if (buildingId == BuildingId.TANK_CAMP) {
            return SoldierType.TANK_TYPE;
        } else if (buildingId == BuildingId.WAR_CAR_CAMP) {
            return SoldierType.WAR_CAR;
        } else if (buildingId == BuildingId.MILITIA_CAMP) {
            return SoldierType.MILITIA;
        }

        return -1;
    }

    public static int getSoldierBuildingId(int soldierType) {
        if (soldierType == SoldierType.ROCKET_TYPE) {
            return BuildingId.ROCKET_CAMP;
        } else if (soldierType == SoldierType.TANK_TYPE) {
            return BuildingId.TANK_CAMP;
        } else if (soldierType == SoldierType.WAR_CAR) {
            return BuildingId.WAR_CAR_CAMP;
        }

        return -1;
    }

    public static int getPercent(int curValue, int maxValue) {
        double percentValue = (double)curValue / (double)maxValue * 100;
        percentValue = Math.ceil(percentValue);
        int resPercent = (int)percentValue;
        resPercent = resPercent>= 100 ? 100 : resPercent;
        return resPercent;
    }

}
