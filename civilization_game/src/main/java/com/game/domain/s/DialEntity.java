package com.game.domain.s;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.game.constant.AwardType;
import com.game.util.RandomHelper;

public class DialEntity {
	// 活动奖励ID
	private int awardId;

	// 通用类型转盘
	private Map<Integer, List<StaticActDial>> commonDials = new HashMap<Integer, List<StaticActDial>>();
	private Map<Integer, Integer> commons = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> dialsCount = new HashMap<Integer, Integer>();
	// 限制获取
	private Map<Integer, Integer> limits = new HashMap<Integer, Integer>();
	
	public DialEntity(int awardId) {
		this.awardId = awardId;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public int getDailCount(int type) {
		if (!dialsCount.containsKey(type)) {
			return 0;
		}
		return dialsCount.get(type);
	}

	public Map<Integer, List<StaticActDial>> getActDails() {
		return commonDials;
	}

	public List<StaticActDial> getActDialList(int type) {
		return commonDials.get(type);
	}

	/**
	 * 初始化添加转盘
	 * 
	 * @param e
	 */
	public void addStaticActDial(StaticActDial e) {
		List<StaticActDial> t = this.commonDials.get(e.getType());
		if (t == null) {
			t = new ArrayList<StaticActDial>();
			this.commonDials.put(e.getType(), t);
		}
		int count = getDailCount(e.getType());
		dialsCount.put(e.getType(), count + 1);

		Integer seed = commons.get(e.getType());
		if (seed == null) {
			commons.put(e.getType(), e.getWeight());
		} else {
			commons.put(e.getType(), e.getWeight() + seed);
		}

		if (e.getLimit() > 0) {// 有限制数量的相同道具用同一个标识
			int keyId = e.getItemType() * 1000000 + e.getItemId();
			e.setKeyId(keyId);
			limits.put(e.getKeyId(), e.getLimit());
		}
		t.add(e);
	}

	/**
	 * 获取随机转盘
	 * 
	 * @param type
	 * @param records
	 * @return
	 */
	public StaticActDial getRandomDail(int type, Map<Integer, Integer> records) {
		List<StaticActDial> list = commonDials.get(type);
		if (list == null) {
			return null;
		}
		int seed = 0;
		if (records != null && !records.isEmpty()) {
			List<StaticActDial> tempList = new ArrayList<StaticActDial>();
			for (StaticActDial e : list) {
				if (e.getKeyId() != 0 && records.containsKey(e.getKeyId()) && records.get(e.getKeyId()) >= e.getLimit()) {
                    continue;
                }
				seed += e.getWeight();
				tempList.add(e);
			}
			return random(tempList, seed);
		}
		seed = commons.get(type);
		return random(list, seed);
	}

	private StaticActDial random(List<StaticActDial> list, int seed) {
		if (list == null || list.isEmpty() || seed == 0) {
			return null;
		}
		int total = 0;
		int random = RandomHelper.randomInSize(seed);
		for (StaticActDial e : list) {
			total += e.getWeight();
			if (total >= random) {
				return e;
			}
		}
		return list.get(0);
	}

    public StaticActDial getLuckRandomDail(int type, Map<Integer, Integer> records,int randomEquip) {
        List<StaticActDial> list = commonDials.get(type);
        if (list == null) {
            return null;
        }
        int seed = 0;
        if (records != null && !records.isEmpty()) {
            List<StaticActDial> tempList = new ArrayList<StaticActDial>();
            for (StaticActDial e : list) {
                // 过滤军刀
				if (e.getLimit() > 0) {
					if (null != records.get(e.getEquipId()) && records.get(e.getEquipId()) >0) {
						continue;
					}
					
					Integer number = records.get(e.getItemId());
					if (number == null) {
						number = 0;
					}
					if (number >= e.getLimit()) {
						continue;
					}
					if(randomEquip > 0){
						return e;
					}
				}
                seed += e.getWeight();
                tempList.add(e);
            }
            
            return random(tempList, seed);
        }
        seed = commons.get(type);
        return random(list, seed);
    }

    public StaticActDial getStaticActDial(int itemType,int itemId){
		List<StaticActDial> actDialList = getActDialList(1);
		return actDialList.stream().filter(x->x.getItemType()==itemType && x.getItemId()==itemId).findAny().orElse(null);
	}
}
