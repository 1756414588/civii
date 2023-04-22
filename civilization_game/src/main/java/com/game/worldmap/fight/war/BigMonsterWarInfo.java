package com.game.worldmap.fight.war;

import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * 巨型虫族战斗
 */
@Getter
@Setter
public class BigMonsterWarInfo extends WarInfo {

	// 位置
	private Pos pos;
	// 阵营
	private int country;

	public Pos getPos() {
		return pos;
	}

	public void setPos(Pos pos) {
		this.pos = pos;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}
}
