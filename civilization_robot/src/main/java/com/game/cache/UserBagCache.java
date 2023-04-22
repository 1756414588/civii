package com.game.cache;

import com.game.domain.BagEquip;
import com.game.domain.UserProp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户背包
 */
@Getter
@Setter
public class UserBagCache {

	// 装备信息
	private Map<Integer, BagEquip> bagEquipMap = new HashMap<>();

	private Map<Integer, UserProp> userPropMap = new HashMap<>();


	public void addProp(int id, int number) {
		if (userPropMap.containsKey(id)) {
			UserProp userProp = userPropMap.get(id);
			userProp.setNum(userProp.getNum() + number);
		} else {
			UserProp userProp = new UserProp();
			userProp.setPropId(id);
			userProp.setNum(number);
			userPropMap.put(id, userProp);
		}
	}

	public void addEquip(int key, int id, List<Integer> skills) {
		if (bagEquipMap.containsKey(key)) {
			return;
		}
		BagEquip bagEquip = new BagEquip(key, id, skills);
		bagEquipMap.put(key, bagEquip);
	}

	public BagEquip getEquipById(int equipId) {
		return bagEquipMap.values().stream().filter(e -> e.getEquipId() == equipId).findFirst().orElse(null);
	}


}
