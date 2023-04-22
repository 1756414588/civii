package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *
 * @date 2020/12/7 12:01
 * @description
 */
@Getter
@Setter
public class StaticActivityChrismas {
    private int keyId;  //id
    private int awardId;
    private int sort;
    /**
     * 奖励
     */
    private List<List<Integer>> award;
    /**
     * 花费
     */
    private int cost;
    /**
     * 可购买次数
     */
    private int canBuy;
    private String desc;
}
