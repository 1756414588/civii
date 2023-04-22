package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author cpz
 * @date 2020/12/21 9:23
 * @description
 */
@Getter
@Setter
public class StaticMailAward {
    private int mailId;
    private int beautyId;
    private List<Integer> award;
}
