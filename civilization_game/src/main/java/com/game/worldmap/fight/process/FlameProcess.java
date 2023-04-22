package com.game.worldmap.fight.process;

import com.game.constant.ActPassPortTaskType;
import com.game.constant.MailId;
import com.game.constant.MarchReason;
import com.game.constant.MarchState;
import com.game.constant.Reason;
import com.game.define.Fight;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.Team;
import com.game.domain.p.WorldMap;
import com.game.flame.FlameGuard;
import com.game.flame.FlameMap;
import com.game.flame.FlameWarManager;
import com.game.flame.FlameWarResource;
import com.game.flame.FlameWarService;
import com.game.flame.StaticFlameWarMgr;
import com.game.flame.entity.StaticFlameMine;
import com.game.pb.FlameWarPb;
import com.game.pb.FlameWarPb.SynFlameEntityAddRq;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.game.worldmap.Entity;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Fight(warName = "战火燎原", warType = {}, marthes = {MarchType.FLAME_COLLECT, MarchType.FLAME_WAR})
@Component
public class FlameProcess extends FightProcess {

	@Autowired
	private FlameWarManager flameWarManager;

	@Autowired
	private FlameWarService flameWarService;

	@Autowired
	private StaticFlameWarMgr staticFlameWarMgr;


	@Override
	public void init(int[] warTypes, int[] marches) {
		registerMarch(MarchType.FLAME_COLLECT, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.FLAME_COLLECT, MarchState.Back, this::doFinishedMarch);

		registerMarch(MarchType.FLAME_WAR, MarchState.Begin, this::marchArrive);
		registerMarch(MarchType.FLAME_WAR, MarchState.Back, this::doFinishedMarch);
	}

	private void marchArrive(MapInfo mapInfo, March march) {
		Player player = playerManager.getPlayer(march.getLordId());
		if (player == null) {
			return;
		}
		FlameMap flameMap = flameWarManager.getFlameMap();
		Entity node = flameMap.getNode(march.getEndPos());
		if (node == null) {
			worldManager.handleMiddleReturn(march, MarchReason.LostTarget);
			worldManager.synMarch(flameMap.getMapId(), march);
			return;
		}
		switch (node.getNodeType()) {
			case City:
				if (march.getMarchType() != MarchType.FLAME_WAR) {
					worldManager.handleMiddleReturn(march, Reason.FLAME);
					worldManager.synMarch(flameMap.getMapId(), march);
					return;
				}
				march.setState(MarchState.Waiting);
				break;
			case Mine:
				if (march.getMarchType() != MarchType.FLAME_COLLECT) {
					worldManager.handleMiddleReturn(march, Reason.FLAME);
					worldManager.synMarch(flameMap.getMapId(), march);
					return;
				}
				doQuickResource(player, march, (FlameWarResource) node);
				break;
		}
		worldManager.synMarch(flameMap.getMapId(), march);
	}

	@Override
	public void loadWar(WorldMap worldMap, MapInfo mapInfo) {
	}


	public void doQuickResource(Player player, March march, FlameWarResource flameWarResource) {
		StaticFlameMine staticFlameMine = staticFlameWarMgr.getStaticFlameMine(flameWarResource.getResId());
		FlameMap flameMap = flameWarManager.getFlameMap();
		if (staticFlameMine == null) {
			worldManager.handleMiddleReturn(march, Reason.FLAME);
			worldManager.synMarch(flameMap.getMapId(), march);
			return;
		}
		long l = TimeHelper.curentTime();
		ConcurrentLinkedDeque<FlameGuard> collectArmy = flameWarResource.getCollectArmy();

		if (collectArmy.isEmpty()) {
			FlameGuard flameGuard = new FlameGuard();
			flameGuard.setMarch(march);
			flameGuard.setResouce(flameWarResource);
			flameGuard.setStartTime(l);
			flameGuard.setNextCalTime(l + staticFlameMine.getCollectTime());
			flameGuard.setPlayer(player);
			flameWarResource.getCollectArmy().add(flameGuard);
			march.setState(MarchState.Collect);// 采集
			long l1 = staticFlameMine.getResource() - flameWarResource.getConvertRes();
			long l2 = l1 / ((staticFlameMine.getCollectTime() / 60000) * 100);
			march.setEndTime(l + l2 * 60 * 1000);
			worldManager.synMarch(flameMap.getMapId(), march);
			synAdd(flameWarResource);
		} else {
			FlameGuard first = collectArmy.getFirst();
			if (first.getMarch().getCountry() == player.getCountry()) {
				marchManager.handleMarchReturn(march, Reason.FLAME);
				worldManager.synMarch(flameMap.getMapId(), march);
				playerManager.sendNormalMail(player, MailId.FLAME_MAIL_156, flameWarResource.getPosStr());
				return;
			} else {
				March def = first.getMarch();
				Player player1 = playerManager.getPlayer(def.getLordId());
				String[] param = {player.getNick(), player1.getNick(), flameWarResource.getPosStr()};

				Team teamA = flameWarService.handleSimple(march);
				Team teamB = flameWarService.handleSimple(def);
				Random rand = new Random(l);
				battleMgr.doTeamBattle(teamA, teamB, rand, ActPassPortTaskType.IS_WORLD_WAR);

				HashMap<Integer, Integer> attackRec = new HashMap<Integer, Integer>(4);
				flameWarService.atertBattle(teamA, attackRec, null, march);
				HashMap<Integer, Integer> defRec = new HashMap<Integer, Integer>(4);
				flameWarService.atertBattle(teamB, defRec, null, def);
				int mailId = MailId.ATK_COLLECT_WIN;
				int collectId = MailId.COLLECT_BREAK;
				boolean isWin = true;
				if (teamA.isWin()) {
					// 退回
					Award award = new Award();
					long totalRes = first.getTotalRes();
					award.setCount((int) totalRes);
					def.addAwards(award);
					// 采集中断
					collectArmy.clear();

					flameWarResource.getCollectArmy().clear();
					FlameGuard flameGuard = new FlameGuard();
					flameGuard.setMarch(march);
					flameGuard.setResouce(flameWarResource);
					flameGuard.setStartTime(l);
					flameGuard.setNextCalTime(l + staticFlameMine.getCollectTime());
					flameGuard.setPlayer(player);
					flameWarResource.getCollectArmy().add(flameGuard);
					march.setState(MarchState.Collect);// 采集

					long l1 = staticFlameMine.getResource() - flameWarResource.getConvertRes();
					long l2 = l1 / ((staticFlameMine.getCollectTime() / 60000) * 100);
					march.setEndTime(l + l2 * 60 * 1000);
					worldManager.synMarch(flameMap.getMapId(), march);
					synAdd(flameWarResource);
					isWin = false;
				} else {
					collectId = MailId.COLLECT_REPORT;
					mailId = MailId.ATK_COLLECT_FAIL;
				}
				worldLogic.handleCollectWar(collectId, def, player1, player, isWin, flameWarResource, l - first.getStartTime());
				playerManager.sendReportMail(player, battleMailManager.createCollectWarReport(teamA, teamB, player, player1), battleMailManager.createReportMsg(teamA, teamB), mailId, new ArrayList<Award>(), attackRec, param);
			}
		}
	}

	private void synAdd(Entity node) {
		FlameMap flameMap = flameWarManager.getFlameMap();
		FlameWarPb.SynFlameEntityAddRq.Builder builder = FlameWarPb.SynFlameEntityAddRq.newBuilder();
		builder.addEntity(node.wrapPb());
		flameMap.getPlayerCityMap().values().forEach(x -> {
			Player player1 = x.getPlayer();
			SynHelper.synMsgToPlayer(player1, SynFlameEntityAddRq.EXT_FIELD_NUMBER, SynFlameEntityAddRq.ext, builder.build());
		});
	}
}
