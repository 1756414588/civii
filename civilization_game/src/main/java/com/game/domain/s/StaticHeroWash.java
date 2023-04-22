package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author cpz
 * 英雄洗练
 */
@Getter
@Setter
public class StaticHeroWash {
    private int washId;
    private int washType;
    private int start;
    private int end;
    private List<List<Integer>> washRate;
    private int rate;
}
