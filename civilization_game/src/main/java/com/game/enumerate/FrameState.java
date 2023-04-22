package com.game.enumerate;

import lombok.Getter;

/**
 * @author cpz
 * @date 2021/1/26 17:29
 * @description
 */
public enum FrameState {
    lock(0),   //锁定
    unlock(1);//解锁

    @Getter
    int val;

    FrameState(int val) {
        this.val = val;
    }
}

