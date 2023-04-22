package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

/**
 * 职位信息
 *
 * @author zcp
 * @date 2021/7/19 15:23
 */
@Getter
@Setter
public class BroodWarPosition {
    /**
     * 第几届
     */
    private int rank;
    /**
     * 角色ID
     */
    private long lordId;
    /**
     * 职位
     */
    private int position;
}
