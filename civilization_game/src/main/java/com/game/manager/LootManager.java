package com.game.manager;

import com.game.dataMgr.StaticAwardsMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.util.LogHelper;
import com.game.util.RandomHelper;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

@Component
public class LootManager {

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticAwardsMgr staticAwardsMgr;

	@Autowired
	private HeroManager heroManager;

	// 分解掉落: List<List<Long>> lootConfig : awardType, id, minCount, maxCount,
	// configRandNum
	public void lootItem(List<List<Long>> lootConfig, HashBasedTable<Integer, Integer, Award> hashBasedTable) {
		Random rand = new Random(System.currentTimeMillis());
		// ArrayList<Award> lootList = new ArrayList<Award>();
		for (List<Long> award : lootConfig) {
			if (award.size() != 5) {
				LogHelper.CONFIG_LOGGER.info("award size is not 5, check config!");
				continue;
			}

			int randNum = rand.nextInt(1000) + 1;
			int awardType = award.get(0).intValue();
			int awardId = award.get(1).intValue();
			int minCount = award.get(2).intValue();
			int maxCount = award.get(3).intValue();
			int configRandNum = award.get(4).intValue();
			// 是否掉落物品
			if (randNum <= configRandNum) {
				// 掉落成功
				int lootNum = rand.nextInt(maxCount - minCount + 1) + minCount;
				Award award1 = hashBasedTable.get(awardType, awardId);
				if (award1 != null) {
					award1.setCount(award1.getCount() + lootNum);
				} else {
					Award lootAward = new Award();
					lootAward.setType(awardType);
					lootAward.setId(awardId);
					lootAward.setCount(lootNum);
					hashBasedTable.put(awardType, awardId, lootAward);
				}

			}
		}
	}

	// 完成掉落, 符合所有的掉落规则
	public ArrayList<Award> doLootItem(Player player, int awardId, int reason, List<Integer> lootAwardIndex, List<Integer> heroIds) {
		List<List<Integer>> lootRes = staticAwardsMgr.getAwards(awardId, lootAwardIndex);
		ArrayList<Award> lootAward = new ArrayList<Award>();
		for (List<Integer> lootItem : lootRes) {
			if (lootItem.size() != 3)
				continue;
			lootAward.add(new Award(lootItem.get(0), lootItem.get(1), lootItem.get(2)));
		}

		// 检查award里面有没有相同的英雄，如果有重新返回将令
		ArrayList<Award> lastLootList = heroManager.checkAward(player, lootAward, heroIds);

		for (Award award : lastLootList) {
			playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), reason);
		}

		return lastLootList;
	}

	// 掉落重构
	// 权重或者概率掉落 awardType, id, count ,rate, 只掉落一个
    // List<List<Integer>> outPut = staticWorldCity.getOutput();
    //  Award award = lootManager.lootAwardByRate(outPut);
    //  if (award.isOk()) {
    //     awards.add(award);
    // }
    // 不适合掉落装备
    public Award lootAwardByRate(List<List<Integer>> config) {
	    Award award = new Award();
	    if (config == null || config.size() <= 0) {
	        return award;
        }

        int totalRate = 0;
	    Map<Integer,Integer> addRate = new TreeMap<Integer, Integer>();
        for (int i = 0; i < config.size(); i++) {
            List<Integer> elem = config.get(i);
            if (elem == null) {
                LogHelper.CONFIG_LOGGER.info("elem is null");
                continue;
            }

            if (elem.size() != 4) {
                LogHelper.CONFIG_LOGGER.info("elem.size() != 4");
                continue;
            }
            totalRate += elem.get(3);
            addRate.put(i, totalRate);
        }

        if (totalRate == 0) {
            return award;
        }

        int randNum = RandomHelper.threadSafeRand(1, totalRate);
        int index = -1;
        for (Map.Entry<Integer, Integer> elem : addRate.entrySet()) {
            if (randNum <= elem.getValue()) {
                index = elem.getKey();
                break;
            }
        }

        if (index == -1) {
            return award;
        }

        List<Integer> loot = config.get(index);

        award.setKeyId(0);
        award.setType(loot.get(0));
        award.setId(loot.get(1));
        award.setCount(loot.get(2));
        return award;
    }


}
