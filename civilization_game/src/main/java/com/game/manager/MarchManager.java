package com.game.manager;

import com.game.Loading;
import com.game.constant.GameError;
import com.game.constant.MailId;
import com.game.constant.MapId;
import com.game.constant.MarchState;
import com.game.define.LoadData;
import com.game.domain.Player;
import com.game.domain.s.StaticWorldMap;
import com.game.flame.FlameWarManager;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;
import com.game.worldmap.MarchType;
import com.game.worldmap.Pos;
import com.game.worldmap.WarInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@LoadData(name = "行军管理", type = Loading.LOAD_USER_DB)
public class MarchManager extends BaseManager {

	private Queue<Integer> marchKey;
	private final static int MIN_MARCHKEY = 1000;
	private final static int MAX_MARCHKEY = 81000;

	@Autowired
	private WorldManager worldManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private WarBookManager warBookManager;
	@Autowired
	private FlameWarManager flameWarManager;

	@Override
	public void load() throws Exception {
		marchKey = new ConcurrentLinkedQueue<>();
		for (int i = MIN_MARCHKEY; i < MAX_MARCHKEY; i++) {
			marchKey.add(i);
		}
	}

	@Override
	public void init() throws Exception {
	}

	public int getMarchKey() {
		if (!marchKey.isEmpty()) {
			return marchKey.poll();
		}
		return 0;
	}

	public void backKey(int key) {
		marchKey.add(key);
	}

	public void removeUseMarch(ConcurrentLinkedDeque<March> playerMarchs) {
		if (playerMarchs.isEmpty()) {
			return;
		}
		List<Integer> useMarchKey = new ArrayList<>();
		playerMarchs.forEach(e -> {
			useMarchKey.add(e.getKeyId());
		});
		int finds = useMarchKey.size();
		Iterator<Integer> it = marchKey.iterator();
		while (it.hasNext() && finds > 0) {
			if (useMarchKey.contains(it.next())) {
				it.remove();
				finds--;
			}
		}
	}

	public GameError checkMarchState(March march) {
		if (march.getState() == MarchState.Waiting) {
			return GameError.OK;
		}
		if (march.getState() == MarchState.Back) {
			return GameError.MARCH_RETURNED;
		}

		if (march.getState() == MarchState.Fighting || march.getState() == MarchState.FightOver) {
			StaticWorldMap staticWorldMap = worldManager.getMap(march.getEndPos());
			if (staticWorldMap == null) {
				return GameError.MARCH_FIGHTING;
			}
			MapInfo mapInfo = worldManager.getMapInfo(staticWorldMap.getMapId());
			if (mapInfo == null) {
				return GameError.MARCH_FIGHTING;
			}
			long warId = march.getWarId();
			if (mapInfo.isContain(warId)) {
				return GameError.MARCH_FIGHTING;
			}
		}
		return GameError.OK;
	}

	/**
	 * 回城
	 *
	 * @param march
	 * @param reason
	 */
	public void handleMarchReturn(March march, int reason) {
		// 回城
		march.setState(MarchState.FightOver);
		// 开始掉头
		march.swapPos(reason);
		long lordId = march.getLordId();
		Player player = playerManager.getPlayer(lordId);

		//兵书对行军的影响值
		List<Integer> heroIds = march.getHeroIds();
		float bookEffectMarch = warBookManager.getBookEffectMarch(player, heroIds);

		long period = worldManager.getPeriod(player, march.getEndPos(), march.getStartPos(), bookEffectMarch);

		Pos startPos = march.getStartPos();
		Pos endPos = march.getEndPos();
		int mapId = worldManager.getMapId(endPos);
		if (mapId == MapId.FIRE_MAP) {
			period = flameWarManager.getPeriod(player, startPos, endPos, bookEffectMarch);
		}

		period = worldManager.checkPeriod(march, period);

		march.setPeriod(period);
		march.setEndTime(System.currentTimeMillis() + period);
	}


	public void doMarchReturn(March march, Player player, int reason) {
		int mapId = worldManager.getMapId(player);
		handleMarchReturn(march, reason);
		worldManager.synMarch(mapId, march);
	}

	public void doMarchReturn(int mapId, March march, int reason) {
		handleMarchReturn(march, reason);
		worldManager.synMarch(mapId, march);
	}


	/**
	 * 杀虫返回
	 *
	 * @param march
	 * @param reason
	 */
	public void handleAttackMonsterMarchReturn(March march, int reason) {
		// 回城
		march.setState(MarchState.FightOver);
		// 开始掉头
		march.swapPos(reason);
		march.setEndTime(System.currentTimeMillis() + march.getPeriod());
	}


	// 一个战斗中的所有人应该能够看到当前所有人的行军路线
	public void doWarMarchReturn(Player player, WarInfo warInfo, int reason) {
		// 通知所有部队遣返
		ConcurrentLinkedDeque<March> attacker = warInfo.getAttackMarches();

		for (March march : attacker) {
			long targetId = march.getLordId();
			Player target = playerManager.getPlayer(targetId);
			if (target == null) {
				continue;
			}
			if (march.getMarchType() == MarchType.ZERG_DEFEND_WAR) {// 虫族主宰进攻兵线不能清除
				continue;
			}

			worldManager.doMiddleReturn(march, reason);

			if (target.roleId.longValue() != player.roleId.longValue()) {
				if (worldManager.isEscape(reason)) {
					playerManager.sendNormalMail(target, MailId.ESCAPE_WAR, player.getNick());
				} else if (worldManager.isPlayerFly(reason)) {
					playerManager.sendNormalMail(target, MailId.LOST_TARGET);
				}
			}
		}

		ConcurrentLinkedDeque<March> defencer = warInfo.getDefenceMarches();
		Iterator<March> defencerIt = defencer.iterator();
		while (defencerIt.hasNext()) {
			March march = defencerIt.next();
			long targetId = march.getLordId();
			Player target = playerManager.getPlayer(targetId);
			if (target == null) {
				continue;
			}
			// 虫族入侵 玩家淘宝不能甩掉攻击
			if (targetId == player.roleId && march.getMarchType() == MarchType.RiotWar) {
				continue;
			}

			worldManager.doMiddleReturn(march, reason);

			if (worldManager.isEscape(reason)) {
				playerManager.sendNormalMail(target, MailId.ESCAPE_WAR, player.getNick());
			} else if (worldManager.isPlayerFly(reason)) {
				playerManager.sendNormalMail(target, MailId.LOST_TARGET);
			}
			defencerIt.remove();
		}
	}


}
