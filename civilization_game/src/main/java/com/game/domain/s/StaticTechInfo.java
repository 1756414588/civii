package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 科技模板 s_tech_lv
 * @author other
 */
@Getter
@Setter
public class StaticTechInfo {
    private int keyId;
    private int techType;
    private int techLv;
    private int process;
    private int buildingLv;
    private int triggerTechId;
    private List<List<Long>> resourceCond;
    private List<List<Integer>> effectValue;
    private int upTime;
    private List<Integer> openCond;
    private String upgradeLevelDesc;
}
