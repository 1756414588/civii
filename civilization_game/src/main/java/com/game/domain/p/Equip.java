package com.game.domain.p;

import java.util.ArrayList;
import java.util.List;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.util.LogHelper;

public class Equip implements Cloneable {

	private int keyId;                    //装备唯一Id(因为装备有技能，所以需要区分)
	private int equipId;                  //装备配置ID
	private ArrayList<Integer> skills;    //装备技能Ids
	private int freeWashTimes;            //免费洗练次数
	private int goldWashTimes;            //金币洗练次数

	public Equip() {
		skills = new ArrayList<Integer>();
	}

	public Equip(int keyId, int equipId, ArrayList<Integer> skills) {
		this.keyId = keyId;
		this.equipId = equipId;
		this.copyEquipSkill(skills);
	}

	private void copyEquipSkill(ArrayList<Integer> skills) {
		if (this.skills == null) {
			LogHelper.CONFIG_LOGGER.info("Equip: skills is null");
			return;
		}
		this.skills = new ArrayList<Integer>();
		this.skills.addAll(skills);
	}


	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getEquipId() {
		return equipId;
	}

	public void setEquipId(int equipId) {
		this.equipId = equipId;
	}


	public ArrayList<Integer> getSkills() {
		return skills;
	}

	public void setSkills(ArrayList<Integer> skills) {
		this.skills = skills;
	}

	public Equip cloneInfo() {
		Equip equip = new Equip();
		equip.keyId = keyId;
		equip.equipId = equipId;
		equip.copyEquipSkill(skills);
		equip.setFreeWashTimes(freeWashTimes);
		equip.setGoldWashTimes(goldWashTimes);
		return equip;
	}

	public CommonPb.Equip.Builder wrapPb() {
		CommonPb.Equip.Builder builder = CommonPb.Equip.newBuilder();
		builder.setKeyId(keyId);
		builder.setEquipId(equipId);
		//装备技能
		for (int i = 0; i < skills.size() && i < 4; i++) {
			Integer skillId = skills.get(i);
			if (skillId == null) {
				continue;
			}
			builder.addSkillId(skillId);
		}

		return builder;
	}

	public void unwrapPb(CommonPb.Equip build) {
		keyId = build.getKeyId();
		equipId = build.getEquipId();

		//技能
		skills.clear();
		List<Integer> skillDatas = build.getSkillIdList();
		for (int i = 0; i < skillDatas.size() && i < 4; i++) {
			Integer skillId = skillDatas.get(i);
			if (skillId == null) {
				continue;
			}
			skills.add(skillId);
		}
	}


	public void copyData(Equip equip) {
		keyId = equip.getKeyId();
		equipId = equip.getEquipId();
		copyEquipSkill(equip.getSkills());
		freeWashTimes = equip.getFreeWashTimes();
		goldWashTimes = equip.getGoldWashTimes();
	}


	public DataPb.EquipData.Builder writeData() {
		DataPb.EquipData.Builder builder = DataPb.EquipData.newBuilder();
		builder.setKeyId(keyId);
		builder.setEquipId(equipId);
		//装备技能
		for (int i = 0; i < skills.size() && i < 4; i++) {
			Integer skillId = skills.get(i);
			if (skillId == null) {
				continue;
			}
			builder.addSkillId(skillId);
		}
		builder.setFreeWashTimes(freeWashTimes);
		builder.setGoldWashTimes(goldWashTimes);

		return builder;
	}

	public void readData(DataPb.EquipData build) {
		keyId = build.getKeyId();
		equipId = build.getEquipId();

		//技能
		skills.clear();
		for (Integer skillId : build.getSkillIdList()) {
			if (skillId == null) {
				continue;
			}

			skills.add(skillId);
		}
		freeWashTimes = build.getFreeWashTimes();
		goldWashTimes = build.getGoldWashTimes();
	}


	public int getFreeWashTimes() {
		return freeWashTimes;
	}

	public void setFreeWashTimes(int freeWashTimes) {
		this.freeWashTimes = freeWashTimes;
	}

	public int getGoldWashTimes() {
		return goldWashTimes;
	}

	public void setGoldWashTimes(int goldWashTimes) {
		this.goldWashTimes = goldWashTimes;
	}

	@Override
	public Equip clone() {
		Equip equip = null;
		try {
			equip = (Equip) super.clone();
			ArrayList<Integer> list1 = new ArrayList<>();
			this.skills.forEach(integer -> {
				list1.add(integer);
			});
			equip.setSkills(list1);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return equip;
	}

}
