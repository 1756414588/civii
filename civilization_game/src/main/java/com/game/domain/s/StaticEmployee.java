package com.game.domain.s;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StaticEmployee {
    private int employId;
    private String name;
    private int level;
    /**
     * 单位秒
     */
    private int reduceTime;
    private long durationTime;
    private double baseResoureFactor;
    private int costIron;
    private int costGold;
    private int freeBuyTimes;
    private List<Integer> resAddFlag;
    private int commandLv;
    private int techLv;
    private int quality;
    private int freeDurationTime;
    private int nextId;
}
