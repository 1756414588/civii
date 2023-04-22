package com.game.worldmap;

import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.Hero;
import java.util.ArrayList;
import java.util.List;

public class Army {
	private int armyId;   // 行军Id
	private int fightId;   // 战报Id
	private Player player;  // 玩家
	private int mapId;  // 地图Id
	private March march;   // 行军信息
	private int soilder;   // 当前兵力
	private List<Hero> heros = new ArrayList<Hero>();  // 英雄
	private List<Award> awardList = new ArrayList<Award>();  // 奖励

	public int getArmyId() {
		return armyId;
	}

	public void setArmyId(int armyId) {
		this.armyId = armyId;
	}

	public int getFightId() {
		return fightId;
	}

	public void setFightId(int fightId) {
		this.fightId = fightId;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getSoilder() {
		return soilder;
	}

	public void setSoilder(int soilder) {
		this.soilder = soilder;
	}

	public List<Hero> getHeros() {
		return heros;
	}

	public void setHeros(List<Hero> heros) {
		this.heros = heros;
	}

	public List<Award> getAwardList() {
		return awardList;
	}

	public void setAwardList(List<Award> awardList) {
		this.awardList = awardList;
	}

    public int getMapId () {
        return mapId;
    }

    public void setMapId (int mapId) {
        this.mapId = mapId;
    }

    public March getMarch () {
        return march;
    }

    public void setMarch (March march) {
        this.march = march;
    }
}
