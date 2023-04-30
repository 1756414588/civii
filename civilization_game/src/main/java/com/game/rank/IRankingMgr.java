/**
 *
 */
package com.game.rank;


import java.util.Collection;
import java.util.concurrent.locks.Lock;
import com.game.domain.Player;

/**
 *
 * @date 2020/5/6 17:09
 * @description
 */
public interface IRankingMgr<T extends RankInfo> {
    /**
     * 获取排行榜内所有排名信息
     * @return 排名信息集合
     */
    Collection<T> values();

    /**
     * 获取排行榜内排名最高的排名信息
     * @return 排名最高的排名信息
     */
    T getTop();

    /**
     * 获取排行榜内排名最低的排名信息
     * @return 排名最低的排名信息
     */
    T getLast();

    /**
     * 获取某一段排名信息集合
     * @param start 起始索引
     * @param length 数量/长度
     * @return 排名信息集合
     */
    Collection<T> getValues(int start,int length);

    /**
     * 发送排名信息
     * @param player 玩家实例
     * @param startIndex 起始索引
     * @param length 数量/长度
     */
    void send(Player player,int startIndex,int length);

    /**
     * 发送全部排名信息
     * @param player 玩家实例
     */
    void send(Player player);

    /**
     * 排行榜排名信息数量
     * @return 信息数量
     */
    int size();

    /**
     * 排行榜是否为空
     * @return 如果排行榜内没有任何排名信息返回true
     */
    boolean isEmpty();

    /**
     * 更新排名信息
     * @param info 排名信息
     * @return 如果排行榜产生更变返回true
     */
    boolean update(T info);

    /**
     * 更新排名信息
     * @param player 玩家实例
     * @param data 排名信息，未知对象类型根据不同榜单实现不一样
     * @return 如果排行榜产生更变返回true
     */
    boolean update(Player player, Object... data);

    /**
     * 刷新排行榜
     */
    void refresh();

    /**
     * 初始化排行榜在启动服务器或者首次加载排行榜时调用
     */
    void init();


    /**
     * 获取锁
     * @return 锁实例
     */
    Lock getLock();

    /**
     * 玩家上线
     * @param player 玩家实例
     */
    void online(Player player);

    /**
     * 通过玩家ID查询排名信息
     * @param key 玩家ID等标记性键值
     * @return 排名信息
     */
    T getByKey(int key);

    /**
     * 获取特定排行索引对应的排行信息对象实例
     * @param index 索引
     * @return 排行对象实例
     */
    T getByIndex(int index);

    /**
     * 获取排名
     * @param key 玩家ID等标记性键值
     * @return 排名
     */
    int indexOf(long key);

    /**
     * 获得特定目标的排位
     * @param info 目标实例
     * @return 排位信息，如果没有返回-1
     */
    int indexOf(T info);

    /**
     * 重新排名
     */
    void sort();

    /**
     * @param key
     * @return
     */
    T remove(Object key);

    /**
     * 清空
     */
    void clear();

    /**
     * 获得排行榜的最大容量
     * @return
     */
    int getCapacity();
}
