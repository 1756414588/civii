package com.game.domain.p;

import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.util.LogHelper;
import io.netty.util.internal.StringUtil;

import java.util.ArrayList;
import java.util.List;
import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Map;

//战斗实体
public class BattleEntity {

	private int level;                                  //怪物等级
	private int lineNumber;                             //排数
	private int curSoldierNum;                          //兵力
	private int maxSoldierNum;                          //最大兵力: 向下取整之后 排数*每排兵数
	private LineEntity lineEntity = new LineEntity();   //兵排实体,属性实际可以共用
	private int killNum;                                //杀敌数
	private String name;                                //BattleEntity名字
	private int entityId;                               //实体Id(武将、野怪、世界boss Id)
	private int entityType;                             //实体类型: 1.玩家武将 2.pve怪物 3.叛军 4.世界BOSS 5.城防军 6.友军武将
	private int soldierType;                            //兵种类型
	private int maxLineNumber;                          //最大兵排数
	private long lordId;                                //玩家Id
	private int lostNum;                                //损兵值
	private long wallLordId;                            //城防军玩家Id
	private int wallDefencerkeyId;                      //城防军配置keyId
	private int leftSoldier;                            //对兵排求余剩余的兵力
	private int quality;                            //英雄品质
	private int techLv;                            //科技等级
	private Map<Integer, Map<Integer, RecordEntity>> recordEntityMap = new HashMap<Integer, Map<Integer, RecordEntity>>();
	private List<HeroBook> heroBooks = new ArrayList<>();// 武将的兵书

	public BattleEntity() {

	}

	public int getTechLv() {
		return techLv;
	}

	public void setTechLv(int techLv) {
		this.techLv = techLv;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public LineEntity getLineEntity() {
		return lineEntity;
	}

	public void setLineEntity(LineEntity lineEntity) {
		this.lineEntity = lineEntity;
	}

	public int getCurSoldierNum() {
		return curSoldierNum;
	}

	public void setCurSoldierNum(int curSoldierNum) {
		this.curSoldierNum = curSoldierNum;
	}

	public int getSoldierNum() {
		if (maxLineNumber == 0) {
			LogHelper.CONFIG_LOGGER.info("getSoldierNum maxLineNumber is zero");
			return 0;
		}
		//        return maxSoldierNum / maxLineNumber;
		return lineEntity.getMaxSoldierNum();
	}

	public boolean isAlive() {
		return curSoldierNum > 0;
	}

	public boolean isDead() {
		return curSoldierNum <= 0;
	}

	public int getMaxSoldierNum() {
		return maxSoldierNum;
	}

	public int getKillNum() {
		return killNum;
	}

	public void addKillNum(int num) {
		this.setKillNum(this.getKillNum() + num);
	}

	public void cacSoldierNum() {
		if (lineNumber <= 0) {
			curSoldierNum = 0;
			return;
		}

		// 检查当前兵排数量
		int leftLine = lineNumber - 1; // -1, 0; 0,0; 1,1; 2, 2
		leftLine = Math.max(0, leftLine);
		curSoldierNum = lineEntity.getMaxSoldierNum() * leftLine + lineEntity.getSoldierNum();
	}

	public int getLeftSoldier() {
		if (lineNumber <= 0) {
			curSoldierNum = 0;
			return 0;
		}

		// 检查当前并排数量
		int leftLine = lineNumber - 1; // -1, 0; 0,0; 1,1; 2, 2
		leftLine = Math.max(0, leftLine);
		//TODO 剩余兵力
		//int leftSoldierNum = lineEntity.getMaxSoldierNum() * leftLine + lineEntity.getSoldierNum();
		int leftSoldierNum = lineEntity.getMaxSoldierNum() * leftLine + lineEntity.getSoldierNum() + leftSoldier;
		return leftSoldierNum;
	}

	public void setMaxSoldierNum(int maxSoldierNum) {
		this.maxSoldierNum = maxSoldierNum;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setKillNum(int killNum) {
		this.killNum = killNum;
	}

	public String getName() {
		return name + ":" + entityId;
	}

	public void setName(String name) {
		this.name = name;
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

	public CommonPb.BattleEntity.Builder wrapPb() {
		CommonPb.BattleEntity.Builder builder = CommonPb.BattleEntity.newBuilder();
		builder.setEntityId(entityId);
		builder.setEntityType(entityType);
		builder.setEntityLv(level);
		builder.setMaxNum(maxSoldierNum);
		builder.setLineNum(maxLineNumber);
		builder.setSoldierType(soldierType);
		builder.setTechLv(techLv);
		return builder;

	}

	public DataPb.BattleEntityData.Builder wrapDataPb() {
		DataPb.BattleEntityData.Builder builder = DataPb.BattleEntityData.newBuilder();
		builder.setLevel(level);
		builder.setLineNumber(lineNumber);
		builder.setCurSoldierNum(curSoldierNum);
		builder.setMaxSoldierNum(maxSoldierNum);
		builder.setLineEntity(lineEntity.wrapDataPb());
		builder.setKillNum(killNum);
		builder.setName(StringUtil.isNullOrEmpty(name) ? "" : name);
		builder.setEntityId(entityId);
		builder.setEntityType(entityType);
		builder.setSoldierType(soldierType);
		builder.setMaxLineNumber(maxLineNumber);
		builder.setLordId(lordId);
		builder.setLostNum(lostNum);
		builder.setWallLordId(wallLordId);
		builder.setWallDefencerkeyId(wallDefencerkeyId);
		builder.setLeftSoldier(leftSoldier);
		builder.setQuality(quality);
		builder.setTechLv(techLv);
		return builder;
	}

	public void unWrapDataPb(DataPb.BattleEntityData data) {
		this.level = data.getLevel();
		this.lineNumber = data.getLineNumber();
		this.curSoldierNum = data.getCurSoldierNum();
		this.maxSoldierNum = data.getMaxSoldierNum();
		LineEntity lineEntity = new LineEntity();
		lineEntity.wrapDataPb(data.getLineEntity());
		this.lineEntity = lineEntity;
		this.killNum = data.getKillNum();
		this.name = data.getName();
		this.entityId = data.getEntityId();
		this.entityType = data.getEntityType();
		this.soldierType = data.getSoldierType();
		this.maxLineNumber = data.getMaxLineNumber();
		this.lordId = data.getLordId();
		this.lostNum = data.getLostNum();
		this.wallLordId = data.getWallLordId();
		this.wallDefencerkeyId = data.getWallDefencerkeyId();
		this.leftSoldier = data.getLeftSoldier();
		this.quality = data.getQuality();
		this.techLv = data.getTechLv();
	}


	public int getSoldierType() {
		return soldierType;
	}

	public void setSoldierType(int soldierType) {
		this.soldierType = soldierType;
	}

	// 损兵计算有问题
	public int getLost() {
		return lostNum;
	}

	public int getMaxLineNumber() {
		return maxLineNumber;
	}

	public void setMaxLineNumber(int maxLineNum) {
		this.maxLineNumber = maxLineNum;
	}

	public long getLordId() {
		return lordId;
	}

	public void setLordId(long lordId) {
		this.lordId = lordId;
	}

	public Map<Integer, Map<Integer, RecordEntity>> getRecordEntityMap() {
		return recordEntityMap;
	}

	public void setRecordEntityMap(Map<Integer, Map<Integer, RecordEntity>> recordEntityMap) {
		this.recordEntityMap = recordEntityMap;
	}

	public void unwrapPb(CommonPb.BattleEntity build) {
		entityId = build.getEntityId();
		entityType = build.getEntityType();
		level = build.getEntityLv();
		maxSoldierNum = build.getMaxNum();
		maxLineNumber = build.getLineNum();
		soldierType = build.getSoldierType();
		techLv = build.getTechLv();
	}

	public BattleEntity cloneData() {
		BattleEntity battleEntity = new BattleEntity();
		battleEntity.setEntityId(entityId);
		battleEntity.setEntityType(entityType);
		battleEntity.setLevel(level);
		battleEntity.setMaxSoldierNum(maxSoldierNum);
		battleEntity.setMaxLineNumber(maxLineNumber);
		battleEntity.setSoldierType(soldierType);
		battleEntity.setTechLv(techLv);
		return battleEntity;
	}

	public int getLostNum() {
		return lostNum;
	}

	public void setLostNum(int lostNum) {
		this.lostNum = lostNum;
	}

	public void addLostNum(int num) {
		this.setLostNum(this.getLostNum() + num);
	}

	public long getWallLordId() {
		return wallLordId;
	}

	public void setWallLordId(long wallLordId) {
		this.wallLordId = wallLordId;
	}

	public int getWallDefencerkeyId() {
		return wallDefencerkeyId;
	}

	public void setWallDefencerkeyId(int wallDefencerkeyId) {
		this.wallDefencerkeyId = wallDefencerkeyId;
	}

	public void setLeftSoldier(int leftSoldier) {
		this.leftSoldier = leftSoldier;
	}

	public int getLastCurSoldierNum() {
		if (curSoldierNum <= 0) {
			return leftSoldier;
		}
		return curSoldierNum + leftSoldier;
	}

	public int getRealCurSoldierNum() {
		if (curSoldierNum <= 0) {
			return 0;
		}
		return curSoldierNum;
	}

	public int getLastLineSoldierNum() {
		return getSoldierNum() + leftSoldier;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public List<HeroBook> getHeroBooks() {
		return heroBooks;
	}

	public void setHeroBooks(List<HeroBook> heroBooks) {
		this.heroBooks = heroBooks;
	}
}
