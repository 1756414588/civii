package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author cpz
 * @date 2021/3/4 14:51
 * @description
 */
@Getter
@Setter
public class StaticHeroTask {
    private int id;
    private int position;
    private String name;
    private List<List<Integer>> awardlist;
    private int jumpType;
    private int cond;
    private List<Integer> param;
}
