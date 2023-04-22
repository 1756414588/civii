package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author zcp
 * @date 2021/3/15 14:30
 * 诵我真名者,永不见bug
 * 巨型 虫族
 */
@Getter
@Setter
public class StaticGiantZerg {
    private long id;
    private String name;
    private int type;
    /**
     * 指向s_pve_monster
     */
    private List<Integer> monsterIds;
    private int level;
    private List<List<Integer>> award;
    private List<List<Integer>> firstAward;
    private int soldierType;
    private int num;
    private int needPlayers;
    private List<Integer> refreshArea;
}
