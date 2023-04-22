package com.game.domain;

import com.game.worldmap.Entity;
import com.game.worldmap.Pos;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/11/14 19:10
 **/

@Getter
@Setter
public class MapDistance {

	private Pos originPos;
	private int distance;
	private Pos target;
	private Entity entity;

	public MapDistance(Pos pos) {
		this.originPos = pos;
	}

	public void setDistance(Pos target) {
		this.target = target;
		this.distance = this.calcDistance(target);
	}

	public int calcDistance(Pos pos) {
		return Math.abs(originPos.getX() - pos.getX()) + Math.abs(originPos.getY() - pos.getY());
	}

	public int getDistance() {
		return this.distance;
	}

	public void findNearestEntity(Entity entity) {
		if (this.entity == null) {
			this.entity = entity;
			this.distance = calcDistance(entity.getPos());
			return;
		}

		int tempDistance = calcDistance(entity.getPos());
		if (tempDistance <= this.distance) {
			this.distance = tempDistance;
			this.entity = entity;
		}
	}

	public void findNearestPos(Pos pos) {
		if (this.target == null) {
			this.target = pos;
			this.distance = calcDistance(pos);
			return;
		}

		int tempDistance = calcDistance(pos);
		if (tempDistance <= this.distance) {
			this.distance = tempDistance;
			this.target = pos;
		}
	}

	public Entity getNearest() {
		return entity;
	}

	public Pos getNearestPos() {
		return this.target;
	}


}
