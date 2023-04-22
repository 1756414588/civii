package com.game.enumerate;

import lombok.Getter;

/**
 *
 * @date 2021/1/26 17:29
 * @description
 */
public enum FrameUnlock {
    lock(0),   //锁定
    unlock(1);//解锁

    @Getter
    int val;

    FrameUnlock(int val) {
        this.val = val;
    }
}

