package com.game.domain.p;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @date 2021/3/9 15:15
 *
 * 活动记录
 */
@Getter
@Setter
@Builder
public class ActivityRecord implements Cloneable {
    private int key;
    private long expireTime;
    private int buyCount;

    @Override
    public ActivityRecord clone() {
        ActivityRecord activityRecord = null;
        try {
            activityRecord = (ActivityRecord) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return activityRecord;
    }
}
