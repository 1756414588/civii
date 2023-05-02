package com.game.domain.p;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2021/7/29 15:38
 */
@Getter
@Setter
public class BroodWarHofData {
    private int id;
    private int rank;
    private long lordId;
    private long time;

    public BroodWarHofData() {

    }

    public BroodWarHofData(int rank, long lordId) {
        this.rank = rank;
        this.lordId = lordId;
        this.time = System.currentTimeMillis();
    }
}
