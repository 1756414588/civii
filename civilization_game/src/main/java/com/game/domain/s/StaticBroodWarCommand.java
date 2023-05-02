package com.game.domain.s;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 独裁者职位
 *
 *
 * @date 2021/7/6 10:13
 */
@Getter
@Setter
public class StaticBroodWarCommand {
    private String name;
    private int position;
    private List<List<Integer>> buff;
}
