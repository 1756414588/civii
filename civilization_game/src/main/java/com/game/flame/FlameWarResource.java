package com.game.flame;

import com.game.domain.Player;
import com.game.pb.CommonPb;
import com.game.pb.FlameWarPb;
import com.game.worldmap.Entity;
import com.game.worldmap.March;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FlameWarResource extends Entity {

	private int resId; // 对应配置的id
	private long total;
	private long convertRes;// 已结算矿点
	private ConcurrentLinkedDeque<FlameGuard> collectArmy = new ConcurrentLinkedDeque<>(); // 采集部队

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		super.id = resId;
		this.resId = resId;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getConvertRes() {
		return convertRes;
	}

	public void setConvertRes(long convertRes) {
		this.convertRes = convertRes;
	}

	public void addConvertRes(long convertRes) {
		this.convertRes += convertRes;
	}

	public ConcurrentLinkedDeque<FlameGuard> getCollectArmy() {
		return collectArmy;
	}

	public void setCollectArmy(ConcurrentLinkedDeque<FlameGuard> collectArmy) {
		this.collectArmy = collectArmy;
	}

	@Override
	public NodeType getNodeType() {
		return NodeType.Mine;
	}

	@Override
	public int getNodeState() {
		return 0;
	}

//	@Override
//	public void flush(long curentTime) {
//
//	}

	@Override
	public CommonPb.WorldEntity.Builder wrapPb() {
		CommonPb.WorldEntity.Builder builder = super.wrapPb();
		builder.setEntityType(getNodeType().getType());
		if (!collectArmy.isEmpty()) {
			FlameGuard first = collectArmy.getFirst();
			Player player = first.getPlayer();
			if (player != null) {
				builder.setCountry(player.getCountry());
				builder.setName(player.getNick());
			}
		}
		return builder;
	}

	public FlameWarPb.FlameWarResource encode() {
		FlameWarPb.FlameWarResource.Builder builder = FlameWarPb.FlameWarResource.newBuilder();
		builder.setId(this.id);
		builder.setTotal(this.total);
		builder.setConvertRes(this.convertRes);
		this.collectArmy.forEach(x -> {
			FlameWarPb.FlameGuard.Builder builder1 = FlameWarPb.FlameGuard.newBuilder();
			builder1.setMarchKey(x.getMarch().getKeyId());
			builder1.setStartTime(x.getStartTime());
			builder.addFlameGuard(builder1);
		});
		return builder.build();
	}

	public FlameWarResource() {

	}

	public FlameWarResource(FlameWarPb.FlameWarResource flameWarResource, Map<Integer, March> marchMap) {
		this.id = flameWarResource.getId();
		this.total = flameWarResource.getTotal();
		this.convertRes = flameWarResource.getConvertRes();
		List<FlameWarPb.FlameGuard> flameGuardList = flameWarResource.getFlameGuardList();
		flameGuardList.forEach(x -> {
			March march = marchMap.get(x.getMarchKey());
			if (march != null) {
				FlameGuard guard = new FlameGuard();
				guard.setMarch(march);
				guard.setStartTime(x.getStartTime());
				guard.setResouce(this);
				collectArmy.add(guard);
			}
		});
	}

	public long leftRes() {
		return this.total - this.convertRes;

	}

}
