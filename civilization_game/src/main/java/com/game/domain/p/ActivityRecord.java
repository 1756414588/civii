package com.game.domain.p;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zcp
 * @date 2021/3/9 15:15
 * 诵我真名者,永不见bug
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
