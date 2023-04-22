package com.game.dataMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.domain.p.ConfigException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.dao.s.StaticDataDao;
import com.game.domain.Award;
import com.game.domain.s.StaticAwards;
import com.game.util.RandomHelper;

@Component
public class StaticAwardsMgr extends BaseDataMgr {
    @Autowired
    private StaticDataDao staticDataDao;

    private Map<Integer, StaticAwards> awardsMap = new HashMap<Integer, StaticAwards>();


    @Override
    public void init() throws Exception {
        awardsMap = staticDataDao.selectAwardsMap();
        checkConfig();
        // testAward();
    }

    public void checkConfig() throws Exception {
        for (Map.Entry<Integer, StaticAwards> elem : awardsMap.entrySet()) {
            if (elem == null)
                continue;
            StaticAwards staticAwards = elem.getValue();
            if (staticAwards == null)
                continue;
            List<List<Integer>> awardList = staticAwards.getAwardList();
            for (List<Integer> award : awardList) {
                if (award != null && award.size() != 4) {
                    throw new ConfigException("awardId = " + staticAwards.getAwardId() + " size is not 4");
                }
            }
        }
    }

    public void testAward() {
        for (int i = 0; i < 10; i++) {
            List<List<Integer>> awardList = getAwards(4);
            for (List<Integer> elem : awardList) {
            }
        }
    }

    public StaticAwards getAwardById(int awardId) {
        return awardsMap.get(awardId);
    }

    /**
     * 描叙：该方法有可能会获取到空结果,取决于配置数据（掉落或者分解均可使用该方法）
     *
     * @param awardId
     * @return
     */
    public List<List<Integer>> getAwards(int awardId) {
        List<List<Integer>> rs = new ArrayList<List<Integer>>();
        StaticAwards staticAwards = awardsMap.get(awardId);
        int weight = staticAwards.getWeight(); // 0 概率掉落 1 权重掉落
        int repeat = staticAwards.getRepeat();
        int count = staticAwards.getCount();

        List<List<Integer>> awardList = staticAwards.getAwardList();
        List<List<Integer>> dropList = new ArrayList<List<Integer>>();
        dropList.addAll(awardList);
        int[][] award = new int[count][3];
        List<Award> awards = new ArrayList<Award>();
        if (weight == 0) {// 概率掉落
            for (int i = 0; i < count; i++) {
                dropList = regroupList(dropList, award, repeat);
                int index = RandomHelper.randomInSize(100) + 1; // 随机1~100
                for (List<Integer> e : dropList) {
                    if (e.get(3) == 0) {
                        continue;
                    }
                    if (index <= e.get(3)) {
                        award[i][0] = e.get(0);
                        award[i][1] = e.get(1);
                        award[i][2] = e.get(2);
                        Award lootItem = new Award(e.get(0), e.get(1), e.get(2));
                        awards.add(lootItem);
                        break;
                    }
                }
            }
        } else { // 权重掉落
            for (int i = 0; i < count; i++) {
                dropList = regroupList(dropList, award, repeat);
                int total = getTotal(dropList);
                int index = RandomHelper.randomInSize(total);
                total = 0;
                for (List<Integer> e : dropList) {
                    if (e.get(3) == 0) {
                        continue;
                    }
                    total += e.get(3);
                    if (index <= total) {
                        award[i][0] = e.get(0);
                        award[i][1] = e.get(1);
                        award[i][2] = e.get(2);
                        break;
                    }
                }
            }
        }

        // 去掉不合法的物品
        for (int i = 0; i < count; i++) {
            if (award[i][0] == 0) {
                continue;
            }
            List<Integer> ee = new ArrayList<Integer>();
            ee.add(award[i][0]);
            ee.add(award[i][1]);
            ee.add(award[i][2]);
            rs.add(ee);
        }
        return rs;
    }

    public static int getTotal(List<List<Integer>> list) {
        int total = 0;
        for (int i = 0; i < list.size(); i++) {
            total += list.get(i).get(3);
        }
        return total;
    }

    /**
     * 描叙：重组列表,去已掉落物件,或者重复同一物件 list : 掉落配置表， award 掉落结果 repeat 是否是独立事件
     */
    public static List<List<Integer>> regroupList(List<List<Integer>> list, int[][] award, int repeat) {
        List<List<Integer>> rs = new ArrayList<List<Integer>>();
        for (int i = 0; i < list.size(); i++) {
            List<Integer> e = list.get(i);
            int type = e.get(0);
            int id = e.get(1);
            int count = e.get(2);
            boolean flag = false;
            for (int c = 0; c < award.length; c++) {
                // type 不正确
                if (award[c][0] == 0) {
                    break;
                }
                // 不能重复
                if (repeat == 1 && type == award[c][0] && id == award[c][1]) {
                    flag = true;
                    break;
                } else if (repeat == 0 && type == award[c][0] && id == award[c][1] && count == award[c][2]) { // 可以重复
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                rs.add(e);
            }
        }
        return rs;
    }

    // 十连抽
    public List<List<Integer>> getAwards(int awardId, List<Integer> lootIdList) {
        List<List<Integer>> rs = new ArrayList<List<Integer>>();
        StaticAwards staticAwards = awardsMap.get(awardId);
        int weight = staticAwards.getWeight(); // 0 概率掉落 1 权重掉落
        int repeat = staticAwards.getRepeat();
        int count = staticAwards.getCount();

        List<List<Integer>> awardList = staticAwards.getAwardList();
        List<List<Integer>> dropList = new ArrayList<List<Integer>>();
        dropList.addAll(awardList);
        int[][] award = new int[count][3];
        List<Award> awards = new ArrayList<Award>();
        if (weight == 0) {// 概率掉落
            for (int i = 0; i < count; i++) {
                dropList = regroupList(dropList, award, repeat);
                int index = RandomHelper.randomInSize(100) + 1; // 随机1~100
                int dropIndex = -1;
                for (List<Integer> e : dropList) {
                    ++dropIndex;
                    if (e.get(3) == 0) {
                        continue;
                    }
                    if (index <= e.get(3)) {
                        award[i][0] = e.get(0);
                        award[i][1] = e.get(1);
                        award[i][2] = e.get(2);
                        Award lootItem = new Award(e.get(0), e.get(1), e.get(2));
                        lootIdList.add(dropIndex);
                        awards.add(lootItem);
                        break;
                    }
                }
            }
        } else { // 权重掉落
            for (int i = 0; i < count; i++) {
                dropList = regroupList(dropList, award, repeat);
                int total = getTotal(dropList);
                int index = RandomHelper.randomInSize(total) + 1;  // 1~total
                total = 0;
                int dropIndex = -1;
                for (List<Integer> e : dropList) {
                    ++dropIndex;
                    if (e.get(3) == 0) {
                        continue;
                    }
                    total += e.get(3);
                    if (index <= total) {
                        award[i][0] = e.get(0);
                        award[i][1] = e.get(1);
                        award[i][2] = e.get(2);
                        lootIdList.add(dropIndex);
                        break;
                    }

                }
            }
        }

        // 去掉不合法的物品
        for (int i = 0; i < count; i++) {
            if (award[i][0] == 0) {
                continue;
            }
            List<Integer> ee = new ArrayList<Integer>();
            ee.add(award[i][0]);
            ee.add(award[i][1]);
            ee.add(award[i][2]);
            rs.add(ee);
        }
        return rs;
    }

    /**
     * 掉落一个道具(权重)
     *
     * @param list
     * @return
     */
    public static List<Integer> randomList(List<List<Integer>> list) {
        int seed[] = {0, 0};
        for (List<Integer> e : list) {
            if (e.size() < 4) {
                continue;
            }
            seed[0] += e.get(3);
        }
        seed[0] = RandomHelper.randomInSize(seed[0]);

        for (List<Integer> e : list) {
            if (e.size() < 4) {
                continue;
            }
            seed[1] += e.get(3);
            if (seed[0] <= seed[1]) {
                return e;
            }
        }
        return null;
    }

}
