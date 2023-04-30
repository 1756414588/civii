package com.game.domain.p;

import java.util.ArrayList;
import java.util.HashMap;
/**
*2020年5月29日
*
*halo_game
*BeautyRecord.java
**/
import java.util.List;
import java.util.Map;

import com.game.domain.s.StaticBeautyBase;
import com.game.pb.DataPb;
import com.game.pb.DataPb.Skills;

public class BeautyRecord {
	private int keyId;
	private String beautyName;
	private int beautyExp;// 美女经验值
	private int charmValue;// 魅力值
	private int intimacyValue;// 亲密度
	private int nowDerss;// 当前的装扮
	private int ConVicSGames;// 小游戏连胜利次数
	private List<Integer> clothings =  new ArrayList<Integer>(); // 美女拥有服装信息;

	private Map<Integer, Integer> skills = new HashMap<Integer, Integer>(); // 已解锁的技能信息和状态信息;

	/**
	 * 启动初始化
	 * 
	 * @param
	 */
	public BeautyRecord(DataPb.BeautyRecord beautyRecord) {
		this.keyId = beautyRecord.getKeyId();
		this.beautyName = beautyRecord.getBeautyName();
		this.beautyExp = beautyRecord.getBeautyExp();
		this.charmValue = beautyRecord.getCharmValue();
		this.intimacyValue = beautyRecord.getIntimacyValue();
		this.nowDerss = beautyRecord.getNowDerss();
		this.ConVicSGames = beautyRecord.getConVicSGames();
		
		List<Integer> clothingsList = beautyRecord.getClothingsList();
		if(null != clothings) {
			for (Integer integer : clothingsList) {
				clothings.add(integer);
			}
		}

		/*List<Status> statusList = beautyRecord.getStatusList();
		if (statusList != null) {
			for (Status e : statusList) {
				status.put((int) e.getK(), (int) e.getV());
			}
		}*/

		List<Skills> skillsList = beautyRecord.getSkillsList();
		if (skillsList != null) {
			for (Skills e : skillsList) {
				skills.put(e.getK(), e.getV());
			}
		}
	}

	public BeautyRecord(StaticBeautyBase staticBeautyBase) {
		this.keyId = staticBeautyBase.getKeyId();
		this.beautyName = staticBeautyBase.getName();
//		this.clothings.add(staticBeautyBase.getClothesId());
//		this.nowDerss = staticBeautyBase.getClothesId();
//		this.charmValue = staticBeautyBase.getBaseCharm();
	}

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public String getBeautyName() {
		return beautyName;
	}

	public void setBeautyName(String beautyName) {
		this.beautyName = beautyName;
	}

	public int getBeautyExp() {
		return beautyExp;
	}

	public void setBeautyExp(int beautyExp) {
		this.beautyExp = beautyExp;
	}

	public int getCharmValue() {
		return charmValue;
	}

	public void setCharmValue(int charmValue) {
		this.charmValue = charmValue;
	}

	public int getIntimacyValue() {
		return intimacyValue;
	}

	public void setIntimacyValue(int intimacyValue) {
		this.intimacyValue = intimacyValue;
	}

	public int getNowDerss() {
		return nowDerss;
	}

	public void setNowDerss(int nowDerss) {
		this.nowDerss = nowDerss;
	}

	public List<Integer> getClothings() {
		return clothings;
	}

	public void setClothings(List<Integer> clothings) {
		this.clothings = clothings;
	}

	public Map<Integer, Integer> getSkills() {
		return skills;
	}

	public void setSkills(Map<Integer, Integer> skills) {
		this.skills = skills;
	}


	public int getConVicSGames() {
		return ConVicSGames;
	}


	public void setConVicSGames(int conVicSGames) {
		ConVicSGames = conVicSGames;
	}
}
