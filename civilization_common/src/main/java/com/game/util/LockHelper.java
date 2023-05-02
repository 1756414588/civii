package com.game.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @date 2021/7/26 14:02
 */
public class LockHelper {
    public static Lock lock = new ReentrantLock();
}
