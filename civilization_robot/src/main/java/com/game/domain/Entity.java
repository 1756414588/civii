package com.game.domain;

import com.game.pb.CommonPb.WorldEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Entity {

	private int entityType; // 实体类型: 1.叛军 2.资源 3.玩家 4.npc城池 5.地形 6.初级采集点,7.伏击叛军类型，可被多人攻打
	private long id; // 实体Id: 玩家Id, 怪物Id,资源Id
	private int level; // 实体等级
	private WorldPos pos; // 坐标
	private String name; // 玩家姓名
	private int country; // 国家
	private long protectedTime; // 保护时间
	private int callCount; // 可召唤总人数
	private int callReply; // 召唤响应人数
	private long callEndTime; // 召唤结束时间
	private long createTime; //创建时间
	private boolean isBreak; //虫族入侵 城池是否被攻破
	private boolean isAttack; //虫族入侵 城池是否被攻击
	private int skin; //玩家皮肤
	private int lessHp; //巨型虫族血量
	private int totalHp; //巨型虫族总血量
	private int state; //矿点状态
	private int flush; //1.更新  不等于1增加

	public Entity(WorldEntity worldEntity) {
		this.entityType = worldEntity.getEntityType();
		this.id = worldEntity.getId();
		this.level = worldEntity.getLevel();
		this.pos = new WorldPos(worldEntity.getPos());
		this.name = worldEntity.getName();
		this.country = worldEntity.getCountry();
		this.callCount = worldEntity.getCallCount();
		this.callReply = worldEntity.getCallReply();
		this.callEndTime = worldEntity.getCallEndTime();
		this.createTime = worldEntity.getCreateTime();
		this.isBreak = worldEntity.getIsBreak();
		this.isAttack = worldEntity.getIsAttack();
		this.skin = worldEntity.getSkin();
		this.lessHp = worldEntity.getLessHp();
		this.totalHp = worldEntity.getTotalHp();
		this.state = worldEntity.getState();
		this.flush = worldEntity.getFlush();
	}

}
