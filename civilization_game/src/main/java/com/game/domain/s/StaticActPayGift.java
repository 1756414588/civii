package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 购买礼包的计费点
 *
 *
 * @version 1.0
 * @filename
 * @time 2017-6-5 下午5:26:58
 * @describe
 */
@Getter
@Setter
public class StaticActPayGift {
    private int payGiftId;
    private String name;
    private int display;
    private int awardId;
    private int money;
    private int count;
    private List<List<Integer>> sellList;
    private int vipExp;
    private String asset;
    private int sort;
    private int nextJump;
    private int position;
    private int diamond;
    private String desc;
}
