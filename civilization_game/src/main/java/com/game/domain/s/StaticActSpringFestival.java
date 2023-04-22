package com.game.domain.s;

import com.game.domain.Award;
import com.game.pb.CommonPb.ActSpringFestival;
import java.util.List;

/**
 * @Description TODO
 * @ProjectName halo_server
 * @Date 2022/1/6 9:52
 **/
public class StaticActSpringFestival {
	private int keyId;
	private int type;
	private int awardId;
	private int sortId;
	private int cond;
	private List<List<Integer>> awardList;
	private String desc;
	private int probability;
	private int stage;
	private String icon;
	private int place;

	public int getKeyId() {
		return keyId;
	}

	public void setKeyId(int keyId) {
		this.keyId = keyId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getAwardId() {
		return awardId;
	}

	public void setAwardId(int awardId) {
		this.awardId = awardId;
	}

	public int getSortId() {
		return sortId;
	}

	public void setSortId(int sortId) {
		this.sortId = sortId;
	}

	public int getCond() {
		return cond;
	}

	public void setCond(int cond) {
		this.cond = cond;
	}

	public List<List<Integer>> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<List<Integer>> awardList) {
		this.awardList = awardList;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getPlace() {
		return place;
	}

	public void setPlace(int place) {
		this.place = place;
	}

	public ActSpringFestival.Builder wrapBp() {
		ActSpringFestival.Builder builder = ActSpringFestival.newBuilder();
		builder.setKeyId(keyId);
		builder.setType(type);
		builder.setSortId(sortId);
		builder.setCond(cond);
		if (awardList != null && !awardList.isEmpty()) {
			awardList.forEach(e -> {
				builder.addAward(new Award(e.get(0), e.get(1), e.get(2)).wrapPb());
			});
		}
		builder.setDesc(desc == null ? "" : desc);
		builder.setStage(stage);
		builder.setIcon(icon == null ? "" : icon);
		return builder;
	}
}
