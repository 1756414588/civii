package com.game.domain;

import com.game.pb.CommonPb.Pos;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorldPos {

	private int mapId;
	private int type;
	private int x;
	private int y;
	private int level;
	private boolean attack;

	public WorldPos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public WorldPos(int x, int y, int level) {
		this.x = x;
		this.y = y;
		this.level = level;
	}

	public WorldPos(Pos pos) {
		this.x = pos.getX();
		this.y = pos.getY();
	}

	@Override
	public int hashCode() {
		return getX() ^ getY();
	}

	public int getPosValue() {
		return this.x * 1000 + y;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Pos)) {
			return false;
		}
		WorldPos o = (WorldPos) object;
		return o.getX() == x && o.getY() == y;
	}

	public String toString() {
		return new StringBuffer().append("[").append(x).append(",").append(y).append(":").append(level).append("]").toString();
	}
}
