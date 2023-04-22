package com.game.manager;

import com.game.dataMgr.StaticKillEquipMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.domain.Player;
import com.game.domain.p.KillEquip;
import com.game.domain.p.Property;
import com.game.domain.s.StaticKillEquip;
import com.game.domain.s.StaticKillEquipLevel;
import com.game.domain.s.StaticKillEquipRate;
import com.game.domain.s.StaticProp;
import com.game.util.LogHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

// 国器管理器
@Component
public class KillEquipManager {

	@Autowired
	private StaticKillEquipMgr staticKillEquipMgr;

	@Autowired
	private StaticPropMgr staticPropMgr;

	public StaticKillEquip getStaticKillEquip(int euqipId) {
		return staticKillEquipMgr.getStaticKillEquip(euqipId);
	}

	public boolean isEuqipIdOk(int equipId) {
		Map<Integer, StaticKillEquip> killEquipMap = staticKillEquipMgr.getKillEquipMap();
		return killEquipMap.containsKey(equipId);
	}

	// 检查玩家是否有国器
	public boolean hasEquip(Player player, int equipId) {
		Map<Integer, KillEquip> killEquipMap = player.getKillEquipMap();
		return killEquipMap.containsKey(equipId);
	}


	// 增加杀器
	public KillEquip addEquip(Player player, int equipId) {
		if (hasEquip(player, equipId)) {
			return null;
		}

		// 检查国器的Id范围, id wrong
		if (!isEuqipIdOk(equipId)) {
			return null;
		}
		KillEquip killEquip = new KillEquip();
		killEquip.setLevel(1);
		killEquip.setEquipId(equipId);
		killEquip.setCriti(1);

		Map<Integer, KillEquip> killEquipMap = player.getKillEquipMap();
		killEquipMap.put(equipId, killEquip);

		return killEquip;
	}

	public KillEquip getKillEquip(Player player, int equipId) {
		Map<Integer, KillEquip> killEquipMap = player.getKillEquipMap();
		return killEquipMap.get(equipId);
	}

	public StaticProp getChipConfig(int equipId) {
		StaticKillEquip staticKillEquip = getStaticKillEquip(equipId);
		if (staticKillEquip == null) {
			LogHelper.CONFIG_LOGGER.info("staticKillEquip is null, equipId = " + equipId);
			return null;
		}

		List<List<Integer>> compund = staticKillEquip.getCompound();
		if (compund == null || compund.size() < 1) {
			LogHelper.CONFIG_LOGGER.info("compund is error!");
			return null;
		}

		List<Integer> item = compund.get(0);
		if (item == null || item.size() != 3) {
			LogHelper.CONFIG_LOGGER.info("item config is error in getChipConfig!");
			return null;
		}

		int itemId = item.get(1);
		StaticProp staticProp = staticPropMgr.getStaticProp(itemId);
		if (staticProp == null) {
			LogHelper.CONFIG_LOGGER.info("itemId = " + itemId + " is not exist.");
			return null;
		}

		return staticProp;

	}

	public boolean reachMaxChip(Player player, int chipId) {
		int count = staticKillEquipMgr.getKillNeedCount(chipId);
		int own = player.getItemNum(chipId);

		return own >= count;
	}


	public int getMaxChip(Player player, int chipId) {
		return staticKillEquipMgr.getKillNeedCount(chipId);
	}


	// 下一次暴击的概率
	public int nextCriti(int criti, int count) {
		// 获取国器暴击概率
		StaticKillEquipRate staticKillEquipRate = staticKillEquipMgr.getCritiConfig(criti);
		List<Integer> rates = staticKillEquipRate.getRate();
		int index = criti - 1;
		if (index < 0) {
			LogHelper.CONFIG_LOGGER.info("index < 0)");
			return 1;
		}

		int num = 0;
		Random random = new Random(System.nanoTime());
		int randNum = random.nextInt(1000) + 1;
//        for (int i = 0; i < rate.size(); i++) {
//            num += rate.get(i);
//            if (randNum < num) {
//                return (i+1);
//            }
//
//            // 不能超过当前玩家的爆率[1, criti]
//            if (i+1 == criti) {
//                break;
//            }
//        }

		for (int i = rates.size() - 1; i >= 0; i--) {
			int rate = rates.get(i);
            if (count == 2) {
                rate *= 2;
            }
			num += rate;
			num = (i == 0 && count == 2) ? 1000 : num;
			if (randNum < num) {
				return (i + 1);
			}

			// 不能超过当前玩家的爆率[1, criti]
			if (i >= criti) {
				break;
			}
		}

		return 1;
	}

	public int getStoneCost(int euqipId, int level) {
		return staticKillEquipMgr.getStoneCost(euqipId, level);
	}

	public Map<Integer, StaticKillEquipLevel> getLevelKey(int equipId) {
		Map<Integer, Map<Integer, StaticKillEquipLevel>> killEquipLevelKeymap = staticKillEquipMgr.getKillEquipLevelKeymap();
		Map<Integer, StaticKillEquipLevel> levelKey = killEquipLevelKeymap.get(equipId);
		if (levelKey == null) {
			LogHelper.CONFIG_LOGGER.info("levelKey is null!");
			return null;
		}
		return levelKey;
	}


	// 获取杀器配置
	public Property getKillProperty(int equipId, int equipLv) {
		return staticKillEquipMgr.getKillProperty(equipId, equipLv);
	}

	// 计算杀器的属性
	public Property getAllProperty(Player player, Property total) {
		Map<Integer, KillEquip> killEquipMap = player.getKillEquipMap();
		for (KillEquip killEquip : killEquipMap.values()) {
			if (killEquip == null) {
				continue;
			}

			Property property = getKillProperty(killEquip.getEquipId(), killEquip.getLevel());
			total.add(property);
		}

		return total;

	}

	// 计算杀器的属性
	public Property getAllProperty(Player player) {
		Property total = new Property();
		Map<Integer, KillEquip> killEquipMap = player.getKillEquipMap();
		for (KillEquip killEquip : killEquipMap.values()) {
			if (killEquip == null) {
				continue;
			}

			Property property = getKillProperty(killEquip.getEquipId(), killEquip.getLevel());
			total.add(property);
		}

		return total;

	}

	public int getLevelNum(Player player, int level) {
		int count = 0;
		Map<Integer, KillEquip> killEquipMap = player.getKillEquipMap();
		for (KillEquip killEquip : killEquipMap.values()) {
			if (killEquip == null) {
				continue;
			}
			if (killEquip.getLevel() < level) {
				continue;
			}
			count++;
		}
		return count;
	}

}
