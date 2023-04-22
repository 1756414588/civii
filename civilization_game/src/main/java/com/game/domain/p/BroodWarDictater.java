package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

/**
 * 独裁者信息
 *
 * @author zcp
 * @date 2021/7/19 15:23
 */
@Getter
@Setter
public class BroodWarDictater {
    /**
     * 第几届
     */
    private int rank;
    /**
     * 时间
     */
    private long time;
    /**
     * 角色ID
     */
    private long lordId;

    /**
     * 加载数据
     *
     * @param data
     */
    public void loadData(BroodWarHofData data) {
        this.rank = data.getRank();
        this.lordId = data.getLordId();
        this.time = data.getTime();
    }
}
