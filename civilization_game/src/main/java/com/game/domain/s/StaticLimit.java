package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StaticLimit {
    private int maxHeroLevel;
    private int maxEquipSlot;
    private int initHeroLevel;
    private int initEquipSlot;
    private int equipSlotNum;
    private int washSkillPrice;
    private int washHeroPrice;
    private int washPortion;
    private int soliderLines;
    private int collectInterval;
    private int maxCollectTimes;
    private long maxIron;
    private long maxCopper;
    private long maxOil;
    private long maxStone;
    private int maxBuildTeams;
    private int minBuildTeams;
    private int maxEnegy;
    private int energyInterval;
    private long maxHonor;
    private int maxUseEnergy;
    private int buildTimePrice;
    private int oneDayBuildTeamCost;
    private int sevenDayBuildTeamCost;
    private int soldierSpeed;
    private int recruitMinTime;
    private int recruitMaxTime;
    private int recruitOilCost;
    private int equipCdPrice;
    private int maxWashHeroTimes;
    private int maxWashSkillTimes;

    private int washSkillInterval;
    private int washHeroInterval;
    private int lootHeroNeedLevel;
    private int commonHeroPeriod;
    private int goodHeroPeriod;
    private int heroAdvanceLordLv;
    private int goodHeroLordLv;
    private int advanceCost;
    private int goodHeroProcessAdd;
    private int advanceItemId;
    private int killTechCdPrice;
    private int recoverPeople;
    private int escapePeople;
    private int capturePeople;
    private long recoverPeopleInterval;
    private int maxAutoBuildTimes;
    private int maxWallTimes;
    private List<List<Integer>> cleanAccount;
    /**
     * 兵书商城物品配置 例如:([[105,3,9],[160,4,12]])
     * 玩家105级,有3种类型商品,物品数量是9个;
     * 玩家160级,有4种类型商品,物品数量是12个
     */
    private List<List<Integer>> warbookShop;
    /**
     * 昵称前缀
     */
    private String nickPrefix;
}
