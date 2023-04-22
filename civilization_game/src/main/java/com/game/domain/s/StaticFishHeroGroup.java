package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.sql.ClientInfoStatus;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class StaticFishHeroGroup {

    private int id;
    private String name;
    private List<Integer> heroIds;
    private List<Integer> combination;
    private Map<Integer, StaticGroupToBaitProbability> groupToBaitProbability;


    public void setGroupToBaitProbability(List<List<Integer>> bait) {
        Map<Integer, StaticGroupToBaitProbability> groupToBaitProbabilityMap = new HashMap<>();
        for (List<Integer> cell : bait) {
            StaticGroupToBaitProbability unit = new StaticGroupToBaitProbability();
            unit.setBaitId(cell.get(0));
            unit.setProbability(cell.get(1));
            groupToBaitProbabilityMap.put(unit.getBaitId(), unit);
        }
        this.groupToBaitProbability = groupToBaitProbabilityMap;
    }

}
