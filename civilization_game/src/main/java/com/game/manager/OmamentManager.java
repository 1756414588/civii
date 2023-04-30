package com.game.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.game.constant.GameError;
import com.game.dataMgr.StaticOmamentMgr;
import com.game.domain.Player;
import com.game.domain.p.Omament;
import com.game.domain.p.PlayerOmament;
import com.game.domain.p.Property;
import com.game.domain.s.StaticOmType;
import com.game.domain.s.StaticOmament;
import com.game.message.handler.ClientHandler;
import com.game.pb.BeautyPb.SynOpenSkillRs;
import com.game.pb.OmamentPb;
import com.game.pb.OmamentPb.SynOpenOmamentRs;
import com.game.util.LogHelper;
import com.game.util.SynHelper;

/**
 * 2020年8月5日
 *
 *    halo_game OmamentManager.java
 **/
@Component
public class OmamentManager {
    @Autowired
    private StaticOmamentMgr staticOmamentMgr;

    /**
     * 添加配饰到背包
     *
     * @param player
     * @param omamentId
     * @param count
     * @param reason
     * @return
     */
    public Omament addOmament(Player player, int omamentId, int count, int reason) {
        Map<Integer, Omament> omamentMap = player.getOmaments();
        if(omamentMap.containsKey(omamentId)){
            Omament omament = player.getOmaments().get(omamentId);
            omament.setCount(count + omament.getCount());
            return omament;
        }else{
            Omament omament = new Omament(omamentId, count);
            player.getOmaments().put(omamentId, omament);
            return omament;
        }
    }

    public GameError removeOmament(Player player, int omamentId) {
        /*
         * if (count < 0) { LogHelper.ERROR_LOGGER.error("subOmament count < 0"); return
         * GameError.OMAMENT_COUNT_LESS_ZERO; } Omament Omament =
         * player.getOmaments().get(omamentId); if (Omament == null) { return
         * GameError.OMAMENT_NOT_FOUND; } if (Omament.getCount() < count) {
         * LogHelper.ERROR_LOGGER.error("subOmament  less than count, reason = " +
         * reason); return GameError.NOT_ENOUGH_OMAMENT; }
         * Omament.setCount(Omament.getCount() - count);
		 */
		Map<Integer, Omament> omamentMap = player.getOmaments();
		if (!omamentMap.containsKey(omamentId)) {
			return GameError.OMAMENT_NOT_FOUND;
		}
		Omament omament = player.getOmaments().get(omamentId);

		if (omament.getCount() != 0) {
			LogHelper.CONFIG_LOGGER.error("removeOmament count > 0");
			return GameError.OMAMENT_COUNT_MORE_ZERO;
		}
		player.getOmaments().remove(omamentId);
		return GameError.OK;
	}

	public Omament subOmament(Player player, int omamentId, int count, int reason) {
		if (count < 0) {
			return null;
		}
		Map<Integer, Omament> omamentMap = player.getOmaments();
		if (omamentMap.containsKey(omamentId)) {
			Omament omament = player.getOmaments().get(omamentId);
			if (omament.getCount() < count) {
				return null;
			}
			omament.setCount(omament.getCount() - count);
			return omament;
		}
		return null;
	}

	// 获得物品的品质
	public int getQuality(int omamentId) {
		StaticOmament staticOmament = staticOmamentMgr.getStaticOmament(omamentId);
		if (staticOmament == null) {
			return 0;
		}

		return staticOmament.getQuality();
	}

	public boolean hasEnoughOmament(Player player, int omamentId, int omamentNum) {
		if (player == null) {
			LogHelper.CONFIG_LOGGER.error("hasEnoughOmament player is null");
			return false;
		}
		Map<Integer, Omament> omamentMap = player.getOmaments();
		if (omamentMap.containsKey(omamentId)) {
			Omament omament = player.getOmaments().get(omamentId);
			return omament.getCount() >= omamentNum;
		}
		return false;
	}

    public Omament addOmamentWithLimit(Player player, int omamentId, int count, int maxCount, int reason) {
        Omament omament = player.getOmaments().get(omamentId);
        if (omament != null) {
            int resCount = Math.min(maxCount, count + omament.getCount());
            omament.setCount(resCount);
        } else {
            int resCount = Math.min(maxCount, count);
            omament = new Omament(omamentId, resCount);
            player.getOmaments().put(omamentId, omament);
        }
        return omament;
    }

	/**
	 * 获取开启槽位的配置
	 *
	 * @param pos
	 * @return
	 */
	public StaticOmType findOmTypeConfig(int pos) {
		StaticOmType staticOmTypes = staticOmamentMgr.getStaticOmTypes(pos);
		if (staticOmTypes == null) {
			return null;
		}

		return staticOmTypes;
	}

	/**
	 * 获取配饰的配置
	 *
	 * @param omamentId
	 * @return
	 */
	public StaticOmament findOmamentConfig(int omamentId) {
		StaticOmament staticOmament = staticOmamentMgr.getStaticOmament(omamentId);
		if (staticOmament == null) {
			return null;
		}

		return staticOmament;
	}

	/**
	 * 查找背包对应的配饰
	 *
	 * @param player
	 * @param omamentId
	 * @return
	 */
	public Omament findOmamentBag(Player player, int omamentId) {
		Map<Integer, Omament> omaments = player.getOmaments();
		if (omaments.isEmpty()) {
			return null;
		}

		if (!omaments.containsKey(omamentId)) {
			return null;
		}

		return omaments.get(omamentId);
	}

	/**
	 * 查找槽位对应的配饰
	 *
	 * @param player
	 * @param pos
	 * @return
	 */
	public PlayerOmament findPlayerOmament(Player player, int pos) {
		Map<Integer, PlayerOmament> playerOmaments = player.getPlayerOmaments();
		if (null == playerOmaments) {
			return null;
		}
		if(!playerOmaments.containsKey(pos)){
		    return null;
        }
		return playerOmaments.get(pos);
	}

	public Property findOmamentProperty(int omamentId) {
		StaticOmament staticOmament = staticOmamentMgr.getStaticOmament(omamentId);
		if (null == staticOmament) {
			return null;
		}
		Property property = new Property();
		switch (staticOmament.getType()) {
			case 1:
				property.setAttack(staticOmament.getProperty().get(0));
				break;
			case 2:
				property.setDefence(staticOmament.getProperty().get(0));
				break;

			case 3:
				property.setSoldierNum(staticOmament.getProperty().get(0));
				break;
			default:
				break;
		}
		return property;
	}

	/**
	 * 开启槽位
	 *
	 * @param player
	 */
	public void openPosOmament(Player player) {
		Map<Integer, PlayerOmament> playerOmaments = player.getPlayerOmaments();
		if (null == playerOmaments) {
			playerOmaments = new HashMap<Integer, PlayerOmament>();
		}

		int level = player.getLevel();
		List<StaticOmType> allStaticOmType = staticOmamentMgr.getAllStaticOmType();
		for (StaticOmType staticOmType : allStaticOmType) {
			if (level >= staticOmType.getLevel() && !playerOmaments.containsKey(staticOmType.getId())) {

				PlayerOmament playerOmament = new PlayerOmament(staticOmType.getId(), 0);
				playerOmaments.put(staticOmType.getId(), playerOmament);

				OmamentPb.SynOpenOmamentRs.Builder builder = SynOpenOmamentRs.newBuilder();
				builder.setPlayerOmament(playerOmament.wrapPb().build());
				SynHelper.synMsgToPlayer(player, SynOpenOmamentRs.EXT_FIELD_NUMBER, SynOpenOmamentRs.ext, builder.build());
			}
		}
	}

	//查询背包最低可合成饰品id
	public int findBagLowestOmament(Player player, int omamentId, ClientHandler handler) {
		//
		int id = 0;
		StaticOmament staticOmament = staticOmamentMgr.getStaticOmament(omamentId);
		//获取配饰类型
		int omamentType = staticOmament.getType();
		//获取一类type的饰品
		List<StaticOmament> omamentByTypeList = new ArrayList<StaticOmament>();
		omamentByTypeList = staticOmamentMgr.getStaticOmamentByType(omamentType);
		//获取背包配饰
		Map<Integer, Omament> omaments = player.getOmaments();
		for (StaticOmament omamentByType : omamentByTypeList) {

			for (Map.Entry<Integer, Omament> omamentBag : omaments.entrySet()) {
				Omament omament = omamentBag.getValue();
				if (omamentByType.getComposeId() == 0 && omament.getOmamentId() == omamentByType.getId()) {
					handler.sendErrorMsgToPlayer(GameError.SUMMIT_OMAMENT);
					return 1;
				}
				if (omamentByType.getId() == omament.getOmamentId() && omament.getCount() > 2 && omamentByType.getComposeId() != 0) {
					return omament.getOmamentId();
				}
			}
		}
		return id;

	}

}
