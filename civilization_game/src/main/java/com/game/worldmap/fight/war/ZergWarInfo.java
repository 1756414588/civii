package com.game.worldmap.fight.war;

import com.game.constant.WarType;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.domain.p.Team;
import com.game.domain.p.Zerg;
import com.game.domain.s.StaticMonster;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.WarAttender;
import com.game.spring.SpringUtil;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.game.worldmap.fight.IFighter;
import java.util.LinkedList;
import lombok.Getter;
import lombok.Setter;

/**
 * 虫族主宰战
 */
@Getter
@Setter
public class ZergWarInfo extends WarInfo {

	// 表信息
	private Zerg zerg;

	private int type;//1.进攻阶段，2.防守阶段

	private int cityId;        //国战城池ID
	private String cityName;//禁卫军出动的时候 需要四方要塞的名字

	// 进攻队列
	private LinkedList<Team> attackQueue = new LinkedList();

	// 防守队列
	private LinkedList<Team> defenceQueue = new LinkedList();

	public CommonPb.WarInfo.Builder wrapZergPb(boolean isJoin) {
		CommonPb.WarInfo.Builder builder = CommonPb.WarInfo.newBuilder();
		builder.setWarId(warId);
		builder.setEndTime(endTime);
		builder.setWarType(warType);
		builder.setCityWarType(warType);
		builder.setCityId(cityId);

		builder.setAttackerCountry(attacker.getCountry());
		builder.setDefenceCountry(defender.getCountry());
		builder.setAttackerSoldier(attacker.getSoldierNum());
		builder.setDefenceSoldier(defender.getSoldierNum());
		builder.setHelpTime(attacker.getHelpTime());
		builder.setDefencerHelpTime(defender.getHelpTime());
		builder.setPos(defender.getPos().wrapPb());
		builder.setAttackPos(attacker.getPos().wrapPb());
		refreshZergWarInfo(builder, isJoin);
		return builder;
	}

	public void refreshZergWarInfo(CommonPb.WarInfo.Builder wrapPb, boolean isJoin) {
		wrapPb.setIsIn(isJoin);
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		StaticMonsterMgr staticMonsterMgr = SpringUtil.getBean(StaticMonsterMgr.class);
		if (warType == WarType.ATTACK_ZERG) {// 玩家进攻
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster((int) defender.getId());
			wrapPb.setDefencer(createWarAttender(staticMonster, defender.getPos(), defender.getSoldierNum()));
		} else {// 怪物进攻
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster((int) attacker.getId());
			wrapPb.setAttacker(createWarAttender(staticMonster, attacker.getPos(), attacker.getSoldierNum()));
			wrapPb.setDefencer(getWarJoinInfo(playerManager.getPlayer(defender.getId())));
		}
	}

	private WarAttender createWarAttender(StaticMonster staticMonster, Pos pos, int soldierNum) {
		CommonPb.WarAttender.Builder builder = CommonPb.WarAttender.newBuilder();
		builder.setLordId(staticMonster.getMonsterId());
		builder.setPos(new Pos(pos.getX(), pos.getY()).wrapPb());
		builder.setCityLevel(staticMonster.getLevel());
		builder.setSoldierNum(soldierNum);
		return builder.build();
	}


}
