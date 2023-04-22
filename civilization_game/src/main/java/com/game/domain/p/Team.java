package com.game.domain.p;

import com.game.pb.DataPb;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Getter
@Setter
public class Team {

	/**
	 * // 每个GameEntity无法共用
	 */
	private ArrayList<BattleEntity> gameEntities = new ArrayList<BattleEntity>();
	/**
	 * // 杀敌数
	 */
	private int killNum;
	private boolean isWin;
	/**
	 * // AI: 状态机, 0.正常 1.闪避 2.被暴击  3.当前排死亡 4.当前实体死亡
	 */
	private int status;
	/**
	 * // 玩家Id
	 */
	private long lordId;
	/**
	 * // 队伍类型: 1.玩家 2.叛军 3.NPC城池
	 */
	private int teamType;
	/**
	 * // 国家
	 */
	private int country;
	/**
	 * // 头像
	 */
	private int portrait;
	/**
	 * // 攻击信息
	 */
	private ArrayList<AttackInfo> attackInfos = new ArrayList<AttackInfo>();
	/**
	 * //母巢优先战斗级别 0无等待时间 1优先出击 2普通出击
	 */
	private int rank;
	/**
	 * //城池
	 */
	private long cityId;

	/**
	 * 行军ID
	 */
	private int marchId;

	/**
	 * 连续杀敌
	 */
	private int mulitKill = 0;

	/**
	 * 参数
	 */
	private long param;

	/**
	 * 圣域争霸战报
	 */
	private List<Report> reports = new ArrayList<>();

	private Map<Integer, Integer> column = new HashMap<>();
	private int line;
	private List<Integer> lineInfo = new ArrayList<>();

	public Team() {

	}

	public void reset() {
		for (BattleEntity entity : gameEntities) {
			int maxNum = Math.max(0, entity.getMaxSoldierNum() - entity.getLostNum());
			//最大兵力
			entity.setMaxSoldierNum(maxNum);
			//杀敌
			entity.setKillNum(0);
			//损兵
			entity.setLostNum(0);
		}
	}

	public ArrayList<BattleEntity> getAllEnities() {
		return gameEntities;
	}

	public int aliveNum() {
		int num = 0;
		for (BattleEntity battleEntity : gameEntities) {
			if (battleEntity.getLineNumber() > 0) {
				num += 1;
			}
		}

		return num;
	}

	public BattleEntity getEntity() {
		for (BattleEntity battleEntity : gameEntities) {
			if (battleEntity.getLineNumber() > 0) {
				return battleEntity;
			}
		}

		return null;
	}

	/**
	 * 当前剩余兵力
	 *
	 * @return
	 */
	public int getCurSoldier() {
		int soldiers = 0;
		for (BattleEntity battleEntity : gameEntities) {
			battleEntity.cacSoldierNum();
			soldiers += battleEntity.getCurSoldierNum();
		}
		return soldiers;
	}

	// 剩余兵力
	public int getLessSoldier() {
		int soldiers = 0;
		for (BattleEntity battleEntity : gameEntities) {
			soldiers += battleEntity.getLeftSoldier();
		}
		return soldiers;
	}

	public int getMaxSoldier() {
		int maxSoldier = 0;
		for (BattleEntity battleEntity : gameEntities) {
			maxSoldier += battleEntity.getMaxSoldierNum();
		}
		return maxSoldier;
	}


	public void addKillNum(int killNum) {
		this.killNum += killNum;
	}

	public boolean isAlive() {
		return aliveNum() > 0;
	}


	public void add(BattleEntity battleEntity) {
		gameEntities.add(battleEntity);
	}

	public void addTeam(Team addTeam) {
		if (addTeam == null) {
			return;
		}

		ArrayList<BattleEntity> addTeamAllEnities = addTeam.getAllEnities();

		for (BattleEntity battleEntity : addTeamAllEnities) {
			if (battleEntity == null) {
				continue;
			}
			gameEntities.add(battleEntity);
		}
	}

	/**
	 * 损兵数 = max - cur
	 *
	 * @return
	 */
	public int getLost() {
		int lost = 0;
		for (BattleEntity battleEntity : gameEntities) {
			lost += battleEntity.getLost();
		}
		return lost;
	}

	/**
	 * 需要lordId
	 *
	 * @param id
	 * @param entityType
	 * @param lordId
	 * @return
	 */
	public BattleEntity getEntity(int id, int entityType, long lordId) {
		for (BattleEntity battleEntity : gameEntities) {
			if (battleEntity.getEntityId() == id
				&& entityType == battleEntity.getEntityType()
				&& lordId == battleEntity.getLordId()) {
				return battleEntity;
			}
		}
		return null;
	}

	public void clear() {
		gameEntities.clear();
	}


	/**
	 * 查询每个玩家的损失兵力数
	 *
	 * @return
	 */
	public Map<Long, Integer> getLostSoldiersByPlayer() {
		Map<Long, Integer> lost = new ConcurrentHashMap<>();
		for (BattleEntity battleEntity : gameEntities) {
			if (lost.containsKey(battleEntity.getLordId())) {
				Integer num = lost.get(battleEntity.getLordId());
				lost.put(battleEntity.getLordId(), battleEntity.getLostNum() + num);
			} else {
				lost.put(battleEntity.getLordId(), battleEntity.getLostNum());
			}
		}
		return lost;
	}

	public Map<Long, Integer> getKillSoliderByPlayer() {
		Map<Long, Integer> killer = new ConcurrentHashMap<>();
		for (BattleEntity battleEntity : gameEntities) {
			long lordId = battleEntity.getLordId() == 0 ? battleEntity.getWallLordId() : battleEntity.getLordId();
			if (killer.containsKey(lordId)) {
				int num = killer.get(lordId);
				killer.put(lordId, battleEntity.getKillNum() + num);
			} else {
				killer.put(lordId, battleEntity.getKillNum());
			}
		}
		return killer;
	}


	public DataPb.Team.Builder wrapPb() {
		DataPb.Team.Builder builder = DataPb.Team.newBuilder();
		builder.setKillNum(killNum);
		builder.setIsWin(isWin);
		builder.setStatus(status);
		builder.setLordId(Long.valueOf(lordId).intValue());
		builder.setTeamType(teamType);
		builder.setCountry(country);
		builder.setPortrait(portrait);
		gameEntities.forEach(e -> {
			builder.addGameEntities(e.wrapDataPb());
		});
		attackInfos.forEach(e -> {
			builder.addAttackInfos(e.wrapPb());
		});
		builder.setCityId(cityId);
		builder.setMarchId(marchId);
		builder.setMulitKill(mulitKill);
		return builder;
	}

	public void unWrapPb(DataPb.Team data) {
		this.killNum = data.getKillNum();
		this.isWin = data.getIsWin();
		this.status = data.getStatus();
		this.lordId = data.getLordId();
		this.teamType = data.getTeamType();
		this.country = data.getCountry();
		this.portrait = data.getPortrait();
		data.getGameEntitiesList().forEach(e -> {
			BattleEntity battleEntity = new BattleEntity();
			battleEntity.unWrapDataPb(e);
			this.gameEntities.add(battleEntity);
		});
		data.getAttackInfosList().forEach(e -> {
			AttackInfo info = new AttackInfo();
			info.unwrapPb(e);
			this.attackInfos.add(info);
		});
		this.cityId = data.getCityId();
		this.marchId = data.getMarchId();
		this.mulitKill = data.getMulitKill();
	}

	public void addLine() {
		this.line += 1;
	}

	// 是否5-8排 收到1点攻击
	public boolean isEffectLine() {
		int i = line + 1;
		if (i == 5 || i == 6 || i == 7 || i == 8) {
			if (!lineInfo.contains(i)) {
				lineInfo.add(i);
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString() {
		return "Team{" +
			"gameEntities=" + gameEntities +
			", killNum=" + killNum +
			", isWin=" + isWin +
			", status=" + status +
			", lordId=" + lordId +
			", teamType=" + teamType +
			", country=" + country +
			", portrait=" + portrait +
			", rank=" + rank +
			", attackInfos=" + attackInfos +
			'}';
	}
}

