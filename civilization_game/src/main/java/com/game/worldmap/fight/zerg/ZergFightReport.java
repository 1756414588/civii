package com.game.worldmap.fight.zerg;

import com.game.constant.MailId;
import com.game.constant.TeamType;
import com.game.constant.WarType;
import com.game.dataMgr.StaticMonsterMgr;
import com.game.domain.Player;
import com.game.domain.p.AttackInfo;
import com.game.domain.p.Attender;
import com.game.domain.Award;
import com.game.domain.p.BattleEntity;
import com.game.domain.p.FightBefore;
import com.game.domain.p.FightIn;
import com.game.domain.p.Report;
import com.game.domain.p.ReportHead;
import com.game.domain.p.ReportMsg;
import com.game.domain.p.Team;
import com.game.domain.s.StaticMonster;
import com.game.manager.BattleMailManager;
import com.game.manager.PlayerManager;
import com.game.util.LogHelper;
import com.game.worldmap.March;
import com.game.worldmap.Monster;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import com.google.common.collect.HashBasedTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZergFightReport {

	@Autowired
	private BattleMailManager battleMailManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private StaticMonsterMgr staticMonsterMgr;

	public void sendZergDefendReport(WarInfo warInfo, Team teamA, Team teamB, HashMap<Integer, Integer> defenceRec, HashBasedTable<Long, Integer, Integer> allSoldierRec) {
		List<Award> tempList = new ArrayList<Award>();

		Report report = createReport(warInfo, teamA, teamB);
		ReportMsg reportMsg = createReportMsg(teamA, teamB);

		ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();

		StaticMonster staticMonster = staticMonsterMgr.getStaticMonster((int) warInfo.getAttackerId());

		Player target = playerManager.getPlayer(warInfo.getDefencerId());
		String[] param = new String[]{staticMonster.getName(), warInfo.getAttackerPos().toPosStr(), target.getNick(), target.getPosStr()};
		LogHelper.MESSAGE_LOGGER.info("defendWinWarReport name:{} pos:{},target:{} pos:{}", staticMonster.getName(), warInfo.getAttackerPos(), target.getNick(), target.getPosStr());

		// 防守成功
		playerManager.sendReportMail(target, report, reportMsg, MailId.CITY_DEFENCE_WIN, tempList, defenceRec, param);

		// 防守成功邮件
		HashSet<Long> defencers = new HashSet<Long>();
		for (March march : defencer) {
			long lordId = march.getLordId();
			Player p = playerManager.getPlayer(lordId);
			if (p == null || lordId == warInfo.getAttackerId()) {
				continue;
			}
			defencers.add(p.roleId);
		}

		for (Long lordId : defencers) {
			Player p = playerManager.getPlayer(lordId);
			if (p == null || lordId == warInfo.getDefencerId()) {
				continue;
			}
			HashMap<Integer, Integer> soldierRec = battleMailManager.getSoldierRecMap(allSoldierRec, lordId);
			playerManager.sendReportMail(p, report, reportMsg, MailId.CITY_DEFENCE_WIN, tempList, soldierRec, param);
		}
	}

	/**
	 * 主宰入侵玩家
	 *
	 * @param warInfo
	 * @param teamA
	 * @param teamB
	 * @return
	 */
	public Report createReport(WarInfo warInfo, Team teamA, Team teamB) {
		Report report = new Report();
		report.setKeyId(warInfo.getWarId());
		report.setResult(teamB.isWin());

		// 左侧战报信息-主宰
		ReportHead leftHead = createMonsterHead(teamA, warInfo.getAttackerPos());
		report.setLeftHead(leftHead);

		// 右侧战报信息-玩家
		long targetLordId = warInfo.getDefencerId();
		Player target = playerManager.getPlayer(targetLordId);
		ReportHead rightHead = battleMailManager.createPlayerReportHead(target, teamB, target.getPos());
		report.setRightHead(rightHead);

		List<Attender> attackers = battleMailManager.createAttender(teamA, WarType.DEFEND_ZERG);
		report.setLeftAttender(attackers);

		List<Attender> defenders = battleMailManager.createAttender(teamB, WarType.DEFEND_ZERG);
		report.setRightAttender(defenders);

		return report;
	}

	public ReportMsg createReportMsg(Team teamA, Team teamB) {
		ReportMsg reportMsg = new ReportMsg();
		// 战前信息
		FightBefore fightBefore = new FightBefore();
		ArrayList<BattleEntity> teamAEntities = teamA.getAllEnities();
		ArrayList<BattleEntity> teamBEntities = teamB.getAllEnities();
		int teamALen = teamAEntities.size();
		int teamBLen = teamBEntities.size();

		BattleEntity battleEntity;
		List<BattleEntity> leftEntities = fightBefore.getLeftEntities();
		List<BattleEntity> rightEntities = fightBefore.getRightEntities();
		for (int i = 0; i < teamALen; i++) {
			battleEntity = teamAEntities.get(i);
			if (battleEntity == null) {
				continue;
			}
			BattleEntity data = battleEntity.cloneData();
			leftEntities.add(data);
		}

		for (int i = 0; i < teamBLen; i++) {
			battleEntity = teamBEntities.get(i);
			if (battleEntity == null) {
				continue;
			}
			BattleEntity data = battleEntity.cloneData();
			rightEntities.add(data);
		}

		// 战中信息
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		ArrayList<AttackInfo> checkLeft = new ArrayList<AttackInfo>();
		for (int i = 0; i < leftAttackInfos.size() && i < 500; i++) {
			checkLeft.add(leftAttackInfos.get(i));
		}
		fightIn.setLeftInfo(checkLeft);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		ArrayList<AttackInfo> checkRight = new ArrayList<AttackInfo>();
		for (int i = 0; i < rightAttackInfos.size() && i < 500; i++) {
			checkRight.add(rightAttackInfos.get(i));
		}
		fightIn.setRightInfo(checkRight);

		reportMsg.setFightBefore(fightBefore);
		reportMsg.setFightIn(fightIn);

		return reportMsg;
	}


	public ReportHead createMonsterHead(Team team, Pos pos) {
		ReportHead reportHead = new ReportHead();
		for (BattleEntity battleEntity : team.getAllEnities()) {
			if (battleEntity == null) {
				continue;
			}
			StaticMonster staticMonster = staticMonsterMgr.getStaticMonster(battleEntity.getEntityId());
			if (staticMonster == null) {
				continue;
			}

			String name = staticMonster.getName();
			if (name != null) {// 显示最后出场的怪物名称
				reportHead.setName(name);
			}
			reportHead.setMonsterId(battleEntity.getEntityId());
			reportHead.setSoldierNum(reportHead.getSoldierNum() + battleEntity.getMaxSoldierNum());
		}

		reportHead.setLost(team.getLost());
		reportHead.setCountry(team.getCountry());
		reportHead.setType(TeamType.GUARD_MONSTER);
		reportHead.setPos(new Pos(pos.getX(), pos.getY()));

		return reportHead;
	}

	/**
	 * 进攻邮件
	 *
	 * @param teamA
	 * @param teamB
	 * @param player
	 * @param monster
	 * @return
	 */
	public CompletableFuture<Report> createKillMonsterReport(Team teamA, Team teamB, Player player, Monster monster, int totalSoldier, int lost) {
		Report report = new Report();
		report.setKeyId(System.currentTimeMillis());
		report.setResult(teamA.isWin());

		// 战报头信息: 左侧的玩家信息
		ReportHead leftHead = battleMailManager.createPlayerReportHead(player, teamA, player.getPos());
		report.setLeftHead(leftHead);

		// 战报头信息:右侧主宰信息
		ReportHead rightHead = createMonsterHead(teamB, monster.getPos());
		rightHead.setSoldierNum(totalSoldier);
		rightHead.setLost(lost);
		report.setRightHead(rightHead);

		List<Attender> attackers = battleMailManager.createRebelAttender(teamA);
		report.setLeftAttender(attackers);
		List<Attender> defenders = battleMailManager.createRebelAttender(teamB);
		report.setRightAttender(defenders);

		// 战斗回放
		FightIn fightIn = new FightIn();
		ArrayList<AttackInfo> leftAttackInfos = teamA.getAttackInfos();
		fightIn.setLeftInfo(leftAttackInfos);
		ArrayList<AttackInfo> rightAttackInfos = teamB.getAttackInfos();
		fightIn.setRightInfo(rightAttackInfos);

		return CompletableFuture.completedFuture(report);
	}


}
