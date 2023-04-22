package com.game.domain.s;


import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class StaticMission {
    private int missionId;
    private int mapId;
    private int missionType;
    private int roleExp;
    private int firstExp;
    private int heroExp;
    private List<Integer> monsterIds;
    private int star;
    private int winCost;
    private int failedCost;
    private List<Integer> resource;
    private List<Integer> countryEquip;
    private List<Integer> heroInfo;
    private List<Integer> nextMission;
    private List<List<Integer>> equipPaper;
    private HashMap<Integer, Integer> buyInfo;
    private int freeHeroId;
    private List<Integer> starCondition;
    private int lootIron;
    private int openBuildingId;
    private List<List<Integer>> resourceLandId;
    private List<List<Integer>> randHero;
    private List<List<Integer>> star1Award;
    private List<List<Integer>> star2Award;
    private List<List<Integer>> star3Award;
    private int failExp;
    private List<Integer> beautyAward;


    public int getBuyGold(int buyTimes) {
        if (buyInfo == null) {
            return Integer.MAX_VALUE;
        }

        Integer need = buyInfo.get(buyTimes);
        if (need == null) {
            return Integer.MAX_VALUE;
        }
        return need;
    }

    public List<List<Integer>> getAward(int starNum) {
        if (starNum == 1) {
            return star1Award;
        } else if (starNum == 2) {
            return star2Award;
        } else if (starNum == 3) {
            return star3Award;
        }
        return star1Award;
    }
}
