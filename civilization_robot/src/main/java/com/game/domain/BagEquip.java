package com.game.domain;

import com.game.pb.CommonPb.Equip;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BagEquip {

	private int keyId;
	private int equipId;
	private List<Integer> skillId = new ArrayList<>();

	public BagEquip(int keyId, int equipId, List<Integer> list) {
		this.keyId = keyId;
		this.equipId = equipId;
		if (list != null) {
			this.skillId = list;
		}
	}

	public BagEquip(Equip equip) {
		this.keyId = equip.getKeyId();
		this.equipId = equip.getEquipId();
		if (equip.getSkillIdCount() > 0) {
			equip.getSkillIdList().forEach(e -> {
				skillId.add(e);
			});
		}
	}
}
