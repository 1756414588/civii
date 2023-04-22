package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StaticFish {

    private int id;
    private String name;
    private int color;
    private String colorDesc;
    private int baseSize;
    private String atlasDesc;
    private int points;
    private int exp;
    private String icon;
    private String idle;
    private List<Integer> award;
}
