package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StaticFishShop {

    private int id;
    private List<Integer> prop;
    private int cost;
    private int limit;
    private int baitId;

}
