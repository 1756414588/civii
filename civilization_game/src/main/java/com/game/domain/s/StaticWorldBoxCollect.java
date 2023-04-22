package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author cpz
 * @date 2021/1/19 2:41
 * @description
 */
@Getter
@Setter
public class StaticWorldBoxCollect {
    private int eventId;
    private List<List<Integer>> mark;
}
