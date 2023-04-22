package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author 陈奎
 * @version 1.0
 * @filename
 * @time 2017-3-13 下午6:38:44
 * @describe
 */
@Getter
@Setter
public class StaticCountryTitle {

    private int titleId;
    private int level;
    private int attack;
    private int defence;
    private int soldierCount;
    private long prestige;
    private long iron;
    private List<List<Integer>> propList;
    private List<List<Integer>> worldBoxDrop;
	private List<List<Integer>> upgradeAward;
    private List<List<Integer>> dailyAward;
    private List<List<Integer>> promotionAward;
}
