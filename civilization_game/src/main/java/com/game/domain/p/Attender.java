package com.game.domain.p;

import com.game.pb.CommonPb;

// 战报参与
public class Attender {
	private int entityId;// 实体Id(武将、野怪、世界boss Id)
	private int entityType;// 实体类型 1.玩家武将 2 世界野怪
	private int entityLv;// 实体等级
	private int soldierType;// 带兵类型 1.弓兵 2.骑兵 3.步兵
	private int killNum;// 击杀数目
	private int honor;// 获得的威望
	private String attenderName = "unkown";// 玩家姓名
	private int quality; // 英雄品质
	private long lordId;
	private int divNum;// 如果是英雄此字段有用
	private int lost;// 损兵数

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityLv() {
        return entityLv;
    }

    public void setEntityLv(int entityLv) {
        this.entityLv = entityLv;
    }

    public int getSoldierType() {
        return soldierType;
    }

    public void setSoldierType(int soldierType) {
        this.soldierType = soldierType;
    }

    public int getKillNum() {
        return killNum;
    }

    public void setKillNum(int killNum) {
        this.killNum = killNum;
    }

    public int getHonor() {
        return honor;
    }

    public void setHonor(int honor) {
        this.honor = honor;
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getDivNum() {
        return divNum;
    }

    public void setDivNum(int divNum) {
        this.divNum = divNum;
    }

	public CommonPb.Attender.Builder wrapPb() {
		CommonPb.Attender.Builder builder = CommonPb.Attender.newBuilder();
		builder.setEntityId(entityId);
		builder.setEntityType(entityType);
		builder.setEntityLv(entityLv);
		builder.setSoldierType(soldierType);
		builder.setKillNum(killNum);
		builder.setHonor(honor);
		builder.setAttenderName(attenderName);
		builder.setQuality(quality);
		builder.setDiviNum(divNum);
		builder.setLost(lost);
		return builder;
	}

	public void unwrapPb(CommonPb.Attender builder) {
		entityId = builder.getEntityId();
		entityType = builder.getEntityType();
		entityLv = builder.getEntityLv();
		soldierType = builder.getSoldierType();
		killNum = builder.getKillNum();
		honor = builder.getHonor();
		attenderName = builder.getAttenderName();
		quality = builder.getQuality();
		divNum = builder.getDiviNum();
		lost = builder.getLost();
	}

	public String getAttenderName() {
		return attenderName;
	}

	public void setAttenderName(String attenderName) {
		this.attenderName = attenderName;
	}

	public int getLost() {
		return lost;
	}

	public void setLost(int lost) {
		this.lost = lost;
	}

	@Override
	public String toString() {
		return new StringBuilder(".heroId:").append(entityId).append(".杀敌:").append(killNum).append(".军功:").append(honor).toString();
	}
}
