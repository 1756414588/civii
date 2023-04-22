package com.game.worldmap.fight.zerg;

import com.game.domain.p.Team;
import com.game.worldmap.Pos;
import com.game.worldmap.fight.Fighter;

/**
 * 虫族主宰
 */
public class ZergFighter extends Fighter {

	private Team team;

	/**
	 * 进攻阶段防守者
	 *
	 * @param monsterId
	 * @param pos
	 * @param team
	 */
	public ZergFighter(long monsterId, Pos pos, Team team) {
		super(monsterId, 1, 0, pos);
		this.team = team;
	}

	@Override
	public int getSoldierNum() {
		return team.getCurSoldier();
	}

    public Team getTeam() {
        return team;
    }


}
