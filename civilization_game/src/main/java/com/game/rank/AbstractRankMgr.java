package com.game.rank;

import com.game.domain.Player;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author jyb
 * @date 2020/5/6 17:16
 * @description
 */
public abstract class AbstractRankMgr<T extends RankInfo> implements IRankingMgr<T> {
    /**
     * 排行榜单
     */
    protected List<T> rankList;
    /**
     * 锁，因为一个榜单可能会因为多人同时修改因此需要上锁保证数据的一致性
     */
    protected ReentrantLock lock = new ReentrantLock(false);


    public AbstractRankMgr() {
        rankList = new ArrayList<>(getCapacity());
    }

    @Override
    public Collection<T> values() {
        return rankList;
    }

    @Override
    public T getTop() {
        if (rankList.isEmpty()) {
            return null;
        }
        return rankList.get(0);
    }

    @Override
    public T getLast() {
        int size = rankList.size();
        if (size < 1) {
            return null;
        }
        return rankList.get(size - 1);
    }

    @Override
    public Collection<T> getValues(int startIndex, int length) {
        List<T> list = new ArrayList<T>(length);
        int endIndex = startIndex + length;
        endIndex = Math.min(rankList.size(), endIndex);
        for (int i = startIndex; i < endIndex; i++) {
            list.add(rankList.get(i));
        }
        return list;
    }

    @Override
    public int size() {
        return rankList.size();
    }

    @Override
    public boolean isEmpty() {
        return rankList.isEmpty();
    }

    @Override
    public boolean update(T info) {
        lock.lock();
        try {
            int index = indexOf(info.getKey());
            if (index == -1) {
                int size = rankList.size();
                if (size < getCapacity()) {
                    rankList.add(info);
                    sort();
                    return true;
                } else {
                    T bottom = rankList.get(size - 1);
                    if (compare(bottom, info)) {
                        return false;
                    }
                    rankList.set(size - 1, info);
                    sort();
                    return true;
                }
            } else {
                rankList.set(index, info);
                sort();
                return true;
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void sort() {
        if (rankList == null) {
            return;
        }
        lock.lock();
        try {
            int size = rankList.size();
            if (size == 1) {
                rankList.get(0).setRanking(0);
                return;
            }
            // 排序
            Collections.sort(rankList);
            // 刷新排名
            int i = 0;
            for (T tmp : rankList) {
                tmp.setRanking(i++);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 验证是否其他排名信息实例更佳靠前
     *
     * @param info  参战信息
     * @param other 其他排名信息实例
     * @return 如果其他排名信息实例更佳靠前返回true
     */
    protected boolean compare(T info, T other) {
        return info.getValue() > other.getValue() ? true : false;
    }


    @Override
    public Lock getLock() {
        return lock;
    }


    @Override
    public T getByKey(int key) {
        for (T tmp : rankList) {
            if (tmp.getKey() == key) {
                return tmp;
            }
        }
        return null;
    }

    @Override
    public int indexOf(long key) {
        int index = 0;
        for (T tmp : rankList) {
            if (tmp.getKey() == key) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    @Override
    public T getByIndex(int index) {
        if (rankList.size() < 1 || index > rankList.size() - 1) {
            return null;
        }
        if (index >= getCapacity()) {
            return null;
        }
        return rankList.get(index);
    }

    @Override
    public int indexOf(T info) {
        int index = 0;
        for (T tmp : rankList) {
            if (tmp.equals(info)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    @Override
    public T remove(Object key) {
        lock.lock();
        try {
            int keyValue = ((Long) key).intValue();
            Iterator<T> it = rankList.iterator();
            while (it.hasNext()) {
                T tmp = it.next();
                if (tmp.getKey() == keyValue) {
                    it.remove();
                    return tmp;
                }
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    @Override
    public void clear() {
        rankList.clear();
    }

    @Override
    public void send(Player player) {
        send(player, 0, size());
    }

    @Override
    public void refresh() {
        sort();
    }

    @Override
    public int getCapacity() {
        return 1000;
    }
}
