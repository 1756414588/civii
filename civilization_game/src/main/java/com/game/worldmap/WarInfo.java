package com.game.worldmap;

import com.game.constant.BuildingType;
import com.game.constant.WarType;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.CtyGovern;
import com.game.domain.p.SquareMonster;
import com.game.domain.p.WorldMap;
import com.game.manager.CountryManager;
import com.game.manager.PlayerManager;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import com.game.pb.DataPb.CompanionMap;
import com.game.server.GameServer;
import com.game.spring.SpringUtil;
import com.game.worldmap.fight.Fighter;
import com.game.worldmap.fight.IFighter;
import com.game.worldmap.fight.IWar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 奔袭，远征，闪电战
 */
public class WarInfo implements IWar {

	protected long warId; // 战争Id
	protected long endTime; // 城战、国战开始时间(结束时间)
	protected int state;   // state = 1, 等待 state = 2, 战斗中, state = 3.开始战斗 state = 4, 战斗取消
	protected int warType; // 战争类型, 1.远征 2.奔袭 3.国战 4.闪电
	protected int cityWarType; //1 闪电战 2.奔袭  3 远征  不要问我为啥这么写  为了兼容以前的sb代码  兼容客户端
	protected int mapId;
	protected int mapType;

	// 双方参战人员
	public IFighter attacker;
	public IFighter defender;
	// 已经结束
	public boolean end = false;

	// 发起者 战友邀请 邀请过的玩家
	private Map<Long, Player> companionMap = new ConcurrentHashMap<>();


	private Map<Integer, SquareMonster> monsters = new HashMap<Integer, SquareMonster>(); // 只可能是进攻方


	public Map<Long, Player> getCompanionMap() {
		return companionMap;
	}

	public void setCompanionMap(Map<Long, Player> companionMap) {
		companionMap = companionMap;
	}

	public ConcurrentLinkedDeque<March> getAttackMarches() {
		return attacker.getMarchList();
	}

	public ConcurrentLinkedDeque<March> getDefenceMarches() {
		return defender.getMarchList();
	}

	public void addAttackMarch(March march) {
		for (March item : getAttackMarches()) {
			if (item.getKeyId() == march.getKeyId()) {
				return;
			}
		}
		getAttackMarches().add(march);
	}

	public void addDefenceMarch(March march) {
		for (March item : getDefenceMarches()) {
			if (item.getKeyId() == march.getKeyId()) {
				return;
			}
		}

		getDefenceMarches().add(march);
	}

	public long getWarId() {
		return warId;
	}

	public void setWarId(long warId) {
		this.warId = warId;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getAttackerId() {
		return attacker.getId();
	}

	public long getDefencerId() {
		return defender.getId();
	}

	public Pos getAttackerPos() {
		return attacker.getPos();
	}

	public Pos getDefencerPos() {
		return defender.getPos();
	}

	public int getAttackerCountry() {
		return attacker.getCountry();
	}

	public int getDefencerCountry() {
		return defender.getCountry();
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public int getWarType() {
		return warType;
	}

	public void setWarType(int warType) {
		this.warType = warType;
	}

	public int getCityWarType() {
		return cityWarType;
	}

	public void setCityWarType(int cityWarType) {
		this.cityWarType = cityWarType;
	}

	@Override
	public CommonPb.WarInfo.Builder wrapPb(boolean join) {
		CommonPb.WarInfo.Builder builder = CommonPb.WarInfo.newBuilder();
		builder.setWarId(warId);
		builder.setWarType(warType);
		builder.setEndTime(endTime);
//		builder.setCityWarType(cityWarType);
		builder.setCityWarType(warType);

		builder.setAttackerCountry(attacker.getCountry());
		builder.setDefenceCountry(defender.getCountry());
		builder.setAttackerSoldier(getAttackSoldierNum());
		builder.setDefenceSoldier(getDefenceSoldierNum());

		builder.setPos(defender.getPos().wrapPb());
		for (SquareMonster monster : monsters.values()) {
			builder.addMonsterData(monster.wrapPb());
		}
		builder.setAttackId(attacker.getId());
		builder.setHelpTime(attacker.getHelpTime());
		builder.setDefencerHelpTime(defender.getHelpTime());
		builder.setAttackPos(attacker.getPos().wrapPb());
		refreshWarInfo(builder, join);
		builder.setCityId((int) defender.getId());
		return builder;
	}

	public CommonPb.WarInfo.Builder wrapCountryPb(boolean isJoin) {

		CommonPb.WarInfo.Builder builder = CommonPb.WarInfo.newBuilder();
		builder.setWarId(warId);
		builder.setEndTime(endTime);
		builder.setWarType(warType);
		builder.setCityWarType(cityWarType);
//		builder.setCityWarType(warType);

		builder.setAttackerCountry(attacker.getCountry());
		builder.setDefenceCountry(defender.getCountry());
		builder.setPos(defender.getPos().wrapPb());
		builder.setAttackerSoldier(getAttackSoldierNum());
		builder.setDefenceSoldier(getDefenceSoldierNum());
		builder.setHelpTime(attacker.getHelpTime());
		builder.setDefencerHelpTime(defender.getHelpTime());
		builder.setAttackPos(attacker.getPos().wrapPb());
		refreshWarInfo(builder, isJoin);
		return builder;
	}


	public DataPb.WarData.Builder writeData() {
		DataPb.WarData.Builder builder = DataPb.WarData.newBuilder();
		builder.setWarId(warId);
		builder.setEndTime(endTime);
		builder.setWarType(warType);
		builder.setState(state);
		builder.setCityWarType(cityWarType);

		builder.setAttackerId(attacker.getId());
		builder.setAttackerPos(attacker.getPos().writeData());
		builder.setAttackerCountry(attacker.getCountry());
		builder.setHelpTime(attacker.getHelpTime());
		builder.setAttackerType(attacker.getType());

		builder.setDefencerId(defender.getId());
		builder.setDefencerPos(defender.getPos().writeData());
		builder.setDefencerCountry(defender.getCountry());
		builder.setDefencerHelpTime(defender.getHelpTime());
		builder.setDefencerType(defender.getType());

		// repeated SquareMonsterData data  = 15;   // 近卫军信息(它的国家是进攻方国家)
		for (SquareMonster monster : monsters.values()) {
			builder.addMonsterData(monster.write());
		}

		if (!getAttackMarches().isEmpty()) {
			getAttackMarches().forEach(x -> {
				builder.addAttacker(x.writeMarch());
			});
		}
		if (!getDefenceMarches().isEmpty()) {
			getDefenceMarches().forEach(x -> {
				builder.addDefencer(x.writeMarch());
			});
		}
		DataPb.CompanionMap.Builder builder1 = DataPb.CompanionMap.newBuilder();
		companionMap.keySet().forEach(e -> {
			builder1.addLordId(e);
		});
		builder.setCompanionMap(builder1);
		return builder;
	}


	@Override
	public IFighter getAttacker() {
		return attacker;
	}

	@Override
	public IFighter getDefencer() {
		return defender;
	}

	@Override
	public void updateState(int state) {
		this.state = state;
	}

	public boolean isOK() {
		Pos pos = new Pos(0, 0);

		if (attacker.getPos().isEqual(pos) && defender.getPos().isEqual(pos)) {
			return false;
		}

		return true;
	}

	public boolean canAttend() {
		return warType == WarType.Attack_WARFARE
			|| warType == WarType.ATTACK_FAR
			|| warType == WarType.ATTACK_QUICK;
	}

	public int getState() {
		return state;
	}


	public void setState(int state) {
		this.state = state;
	}


	public int getAttackerType() {
		return attacker.getType();
	}

	public Map<Integer, SquareMonster> getMonsters() {
		return monsters;
	}

	public void setMonsters(Map<Integer, SquareMonster> monsters) {
		this.monsters = monsters;
	}

	public void updateMonster(int monsterId, SquareMonster monster) {
		this.monsters.put(monsterId, monster);
	}

	public int getMonsterId() {
		int monsterId = 0;
		for (SquareMonster monster : monsters.values()) {
			if (monster != null && monster.getMonsterId() > monsterId) {
				monsterId = monster.getMonsterId();
			}
		}

		return monsterId;
	}

	public int getAttackSoldierNum() {
		return attacker.getSoldierNum();
	}

	public void setAttackSoldierNum(int attackSoldierNum) {
		Fighter fighter = (Fighter) attacker;
		fighter.setSoilder(attackSoldierNum);
	}


	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public int getMapType() {
		return mapType;
	}

	public void setMapType(int mapType) {
		this.mapType = mapType;
	}


	public int getDefenceSoldierNum() {
		return defender.getSoldierNum();
	}

	public void setDefenceSoldierNum(int defenceSoldierNum) {
		Fighter fighter = (Fighter) defender;
		fighter.setSoilder(defenceSoldierNum);
	}

	public boolean hasPlayerMarch(long lordId) {
		for (March march : getAttackMarches()) {
			if (march.getLordId() == lordId) {
				return true;
			}
		}
		return false;
	}

	public March getPlayMarch(long lordId) {
		for (March march : getAttackMarches()) {
			if (march.getLordId() == lordId) {
				return march;
			}
		}
		return null;
	}

	public HashSet<Long> getPlayers() {
		HashSet<Long> players = new HashSet<Long>();
		for (March march : getAttackMarches()) {
			if (march == null) {
				continue;
			}
			players.add(march.getLordId());
		}

		for (March march : getDefenceMarches()) {
			if (march == null) {
				continue;
			}
			players.add(march.getLordId());
		}
		players.add(defender.getId());
		return players;
	}

	public int getAttackerHelpTime() {
		return attacker.getHelpTime();
	}

	public void setAttackerHelpTime(int attackerHelpTime) {
		Fighter fighter = (Fighter) attacker;
		fighter.setHelpTime(attackerHelpTime);
	}

	public int getDefencerHelpTime() {
		return defender.getHelpTime();
	}

	public void setDefencerHelpTime(int defencerHelpTime) {
		Fighter fighter = (Fighter) defender;
		fighter.setHelpTime(defencerHelpTime);
	}

	/**
	 * 拿到进攻方，去重 有的玩家派出去几次兵
	 *
	 * @return
	 */
	public List<Long> getAttackerPlayers() {
		List<Long> players = new ArrayList<>();
		for (March march : getAttackMarches()) {
			if (!players.contains(march.getLordId())) {
				players.add(march.getLordId());
			}

		}
		return players;
	}


	//刷新战斗信息
	public void refreshWarInfo(CommonPb.WarInfo.Builder wrapPb, boolean isJoin) {
		wrapPb.setIsIn(isJoin);
		//判定近卫军
		if (attacker.getType() == 1) {
			wrapPb.setAttacker(getWarJoinInfo(null));
			wrapPb.setDefencer(getWarJoinInfo(null));
			return;
		}
		PlayerManager playerManager = SpringUtil.getBean(PlayerManager.class);
		wrapPb.setAttacker(getWarJoinInfo(playerManager.getPlayer(attacker.getId())));
		wrapPb.setDefencer(getWarJoinInfo(playerManager.getPlayer(defender.getId())));
	}

	public CommonPb.WarAttender.Builder getWarJoinInfo(Player player) {
		CommonPb.WarAttender.Builder builder = CommonPb.WarAttender.newBuilder();
		if (player != null) {
			builder.setLordId(player.getLord().getLordId());
			builder.setName(player.getNick());
			builder.setPos(new Pos(player.getPosX(), player.getPosY()).wrapPb());
			builder.setCityLevel(player.getBuildingLv(BuildingType.COMMAND));
			builder.setPortrait(player.getLord().getPortrait());
			builder.setHeadImg(player.getLord().getHeadIndex());
			builder.setTitle(player.getTitle());
			CtyGovern govern = SpringUtil.getBean(CountryManager.class).getGovern(player);
			if (govern != null) {
				builder.setPost(govern.getGovernId());
			}
		}
		return builder;
	}

	@Override
	public boolean isJoin(Player self) {
		if (self == null) {
			return false;
		}
		HashMap<Integer, March> marchMap = self.getMarchList(this.getDefencerPos());
		if (marchMap.isEmpty()) {
			return false;
		}
		for (March attackerMarch : this.getAttackMarches()) {
			if (marchMap.containsKey(attackerMarch.getKeyId())) {
				return true;
			}
		}
		for (March defencerMarch : this.getDefenceMarches()) {
			if (marchMap.containsKey(defencerMarch.getKeyId())) {
				return true;
			}
		}
		return false;
	}


	@Override
	public long getStartTime() {
		return 0;
	}


}
