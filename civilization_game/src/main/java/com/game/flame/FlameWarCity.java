package com.game.flame;

import com.game.pb.CommonPb;
import com.game.pb.FlameWarPb;
import com.game.worldmap.Entity;
import com.game.worldmap.March;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class FlameWarCity extends Entity {

	private LinkedList<March> attackQueue = new LinkedList();
	private LinkedList<March> defenceQueue = new LinkedList();
	private int state;// 当前状态 1.不能被攻击状态（时间到了变成中立状态） 2.中立状态 3.攻占状态 4.占领状态
	private int firstCountry;
	private HashSet<Long> first = new HashSet<>();// 首次占领的人
	private long firstTime;// 首次占领的时间
	private long occupy;// 该阵营完成占领的时刻
	private long capTime;// 占领时长
	private int level;// 据点等级
	private long updateStateTime;// 这个时间读取数据库，开战多长时间状态发生变化 状态改变的时间
	private List<Long> playerAwardList;// 占领时间刷新的物质 领取过的玩家
	private CommonPb.Award award;
	private long nextCalCountryTime;// 下一次结算时间

	@Override
	public NodeType getNodeType() {
		return NodeType.City;
	}

	@Override
	public int getNodeState() {
		return this.state;
	}

	public void attackPos(March march) {
		if (this.country == march.getCountry()) {
			this.defenceQueue.add(march);
		} else {
			this.attackQueue.add(march);
		}
	}

	@Override
	public CommonPb.WorldEntity.Builder wrapPb() {
		CommonPb.WorldEntity.Builder builder = super.wrapPb();
		builder.setState(this.state);
		builder.setEntityType(getNodeType().getType());
		builder.setProtectedTime(this.updateStateTime);
		builder.setAllOwnTime(this.occupy);
		return builder;
	}

	public FlameWarPb.FlameWarCity encode() {
		FlameWarPb.FlameWarCity.Builder builder = FlameWarPb.FlameWarCity.newBuilder();
		attackQueue.forEach(x -> {
			builder.addAttMarchKey(x.getKeyId());
		});
		defenceQueue.forEach(x -> {
			builder.addDefMarchKey(x.getKeyId());
		});
		builder.setState(this.state);
		builder.setFirstCountry(this.firstCountry);
		builder.addAllFirst(this.first);
		builder.setCapTime(this.capTime);
		builder.setLevel(this.level);
		builder.setUpdateStateTime(this.updateStateTime);
		if (playerAwardList != null) {
			builder.addAllPlayerAwardList(this.playerAwardList);
		}
		if (award != null) {
			builder.setAward(this.award);
		}
		builder.setNextCalCountryTime(this.nextCalCountryTime);
		builder.setId(this.id);
		builder.setPos(this.pos.wrapPb());
		return builder.build();
	}

	public void decode(FlameWarPb.FlameWarCity builder, Map<Integer, March> marches) {
		List<Integer> attMarchKeyList = builder.getAttMarchKeyList();
		attMarchKeyList.forEach(x -> {
			March march = marches.get(x);
			if (march != null) {
				this.attackQueue.add(march);
			}
		});
		List<Integer> defMarchKeyList = builder.getDefMarchKeyList();
		defMarchKeyList.forEach(x -> {
			March march = marches.get(x);
			if (march != null) {
				this.defenceQueue.add(march);
			}
		});
		this.state = builder.getState();
		this.firstCountry = builder.getFirstCountry();
		List<Long> firstList = builder.getFirstList();
		firstList.forEach(x -> {
			first.add(x);
		});
		this.capTime = builder.getCapTime();
		this.level = builder.getLevel();
		this.updateStateTime = builder.getUpdateStateTime();
		List<Long> playerAwardListList = builder.getPlayerAwardListList();
		playerAwardListList.forEach(x -> {
			this.playerAwardList.add(x);
		});
		this.award = builder.getAward();

		this.nextCalCountryTime = builder.getNextCalCountryTime();
		// this.id = builder.getId();
	}

	public void clear() {
		this.attackQueue.clear();
		this.defenceQueue.clear();
	}
}
