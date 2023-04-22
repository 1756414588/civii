package com.game.manager;

import com.game.domain.Player;
import com.game.domain.p.Lord;
import com.game.domain.s.StaticWorldMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.stereotype.Component;

@Component
public class RankManager {
    private List<Lord> rankList = new ArrayList<Lord>();
    private ConcurrentLinkedQueue<Lord> addList = new ConcurrentLinkedQueue<Lord>();
    private Set<Long> rankSet = new HashSet<Long>();
    private long lastSort = 0L;
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
	private ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    // 排行榜规则: 如果rankList为空,则插入,否则找到可以插入的位置加入
    // 如果排行榜人数 >= 3000, 先和最后一名比较,如果最后一名没有这个人分数高
    // 则删除最后一名, 把当前人加进去,然后重新排序
    // 如果当前玩家在排行榜内，则进行排行榜内排名
    // 爵位、战力、等级发生变化的时候
	public void checkRankList(Lord lord) {
		try {
			writeLock.lock();
        if (rankList.isEmpty()) {
            rankList.add(lord);
            rankSet.add(lord.getLordId());
        } else {
            if (rankList.size() >= 3000) {
                int lastIndex = rankList.size() - 1;
                if (lastIndex < 0) {
                    return;
                }

                Lord last = rankList.get(lastIndex);
                if (!rankSet.contains(lord.getLordId())) {
                    ComparatorLord comparatorLord = new ComparatorLord();
                    if (comparatorLord.compare(last, lord) == 1) {
                        rankList.add(lord);
                        rankSet.add(lord.getLordId());
                        rankList.remove(lastIndex);
                        rankSet.remove(last.getLordId());
                        // 新增变化
                        Collections.sort(rankList, new ComparatorLord());
                    } else {
                        // 没有进入排行榜
                        return;
                    }
                } else {
                    // 排行行内变化
                    Collections.sort(rankList, new ComparatorLord());
                }

            } else {
                if (!rankSet.contains(lord.getLordId())) {
                    rankList.add(lord);
                    rankSet.add(lord.getLordId());
                } else {
                    // in rankList
                }

                // 都需要变化
                Collections.sort(rankList, new ComparatorLord());
            }
        }
		} finally {
			writeLock.unlock();
		}
    }

    //https://stackoverflow.com/questions/11441666/java-error-comparison-method-violates-its-general-contract
    class ComparatorLord implements Comparator<Lord> {
        @Override
        public int compare(Lord o1, Lord o2) {
            // by title
            if (o1.getTitle() < o2.getTitle()) {
                return 1;
            }

            if (o1.getTitle() > o2.getTitle()) {
                return -1;
            }

            // by score
            if (o1.getAllScore() < o2.getAllScore()) {
                return 1;
            }

            if (o1.getAllScore() > o2.getAllScore()) {
                return -1;
            }

            // by level
            if (o1.getLevel() < o2.getLevel()) {
                return 1;
            }

            if (o1.getLevel() > o2.getLevel()) {
                return -1;
            }

            // by lordId
            if (o1.getLordId() < o2.getLordId()) {
                return 1;
            }

            if (o1.getLordId() > o2.getLordId()) {
                return -1;
            }

            return 0;
        }


    }

    public List<Lord> getRankList() {
		try {
			readLock.lock();
        return rankList;
		} finally {
			readLock.unlock();
		}
    }

    public List<Lord> getAreaRankList(StaticWorldMap staticWorldMap) {
        List<Lord> retList = new ArrayList<Lord>();
		List<Lord> rankList = getRankList();
        Iterator<Lord> it = rankList.iterator();
        while (it.hasNext()) {
            Lord next = it.next();
            if (next.getPosX() >= staticWorldMap.getX1() && next.getPosX() <= staticWorldMap.getX2() && next.getPosY() >= staticWorldMap.getY1()
                    && next.getPosY() <= staticWorldMap.getY2()) {
                retList.add(next);
            }
        }
        return retList;
    }

    public void setRankList(List<Lord> rankList) {
		try {
			writeLock.lock();
        this.rankList = rankList;
		} finally {
			writeLock.unlock();
		}
    }

    /**
     * 获取国家排行
     *
     * @param page
     * @param size
     * @return
     */
    public List<Lord> getCountryRankList(int country, int page, int size) {
        List<Lord> ret = new ArrayList<Lord>();
        int index = 0;
        int begin = page * size;
        int end = (page + 1) * size;
		List<Lord> rankList = getRankList();
        Iterator<Lord> it = rankList.iterator();
        while (it.hasNext()) {
            Lord next = it.next();
            if (next.getCountry() == country) {
                if (index >= begin && index < end) {
                    ret.add(next);
                }
                index++;
                if (index >= end) {
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 获取国家排行
     *
     * @return
     */
    public List<Lord> getCountryRankList(int country) {
        List<Lord> ret = new ArrayList<Lord>();
		List<Lord> rankList = getRankList();
        Iterator<Lord> it = rankList.iterator();
        while (it.hasNext()) {
            Lord next = it.next();
            if (next.getCountry() == country) {
                ret.add(next);
            }
        }
        return ret;
    }

    /**
     * 获取自己的排行
     *
     * @param lords
     * @param player
     * @return
     */
    public int getMyRank(List<Lord> lords, Player player) {
        int index = 1;
        for (Lord lord : lords) {
            if (lord.getLordId() == player.getLord().getLordId()) {
                return index;
            }
            ++index;
        }
        return -1;

    }

    /**
     * 获取区域排行榜
     *
     * @param page
     * @param size
     * @return
     */
    public List<Lord> getAreaRankList(int x1, int x2, int y1, int y2, int page, int size) {
        List<Lord> ret = new ArrayList<Lord>();
        int index = 0;
        int begin = page * size;
        int end = (page + 1) * size;
		List<Lord> rankList = getRankList();
        Iterator<Lord> it = rankList.iterator();
        while (it.hasNext()) {
            Lord next = it.next();
            int x = next.getPosX();
            int y = next.getPosY();

            if (x >= x1 && x <= x2 && y >= y1 && y <= y2) {
                if (index >= begin && index < end) {
                    ret.add(next);
                }
                index++;
                if (index >= end) {
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 获取个人排名
     *
     * @param country
     * @param player
     * @return
     */
    public int getPersonRank(int country, Player player) {
        Lord lord = player.getLord();
		List<Lord> rankList = getRankList();
        if (!rankList.contains(lord)) {
            return 0;
        }
        int rank = 0;
        try {
            Iterator<Lord> it = rankList.iterator();
            while (it.hasNext()) {
                Lord next = it.next();
                if (country == 0) {
                    rank++;
                    if (next.getLordId() == lord.getLordId()) {
                        return rank;
                    }
                } else if (next.getCountry() == lord.getCountry()) {
                    rank++;
                    if (next.getLordId() == lord.getLordId()) {
                        return rank;
                    }
                }
            }
        } catch (Exception e) {

        }
        return rank + 1;
    }

    public ConcurrentLinkedQueue<Lord> getAddList() {
        return addList;
    }

    public Set<Long> getRankSet() {
        return rankSet;
    }

    public int getMyRank(long lordId) {
        if (!rankSet.contains(lordId)) {
            return 0;
        }
        int rank = 0;
		List<Lord> rankList = getRankList();
        Iterator<Lord> it = rankList.iterator();
        while (it.hasNext()) {
            rank++;
            Lord next = it.next();
            if (next.getLordId() == lordId) {
                return rank;
            }
        }
        return 0;
    }

    public long getLastSort() {
        return lastSort;
    }

    public void setLastSort(long lastSort) {
        this.lastSort = lastSort;
    }

}
