package com.game.worldmap.fight;

import com.game.domain.Player;
import com.game.domain.p.SquareMonster;
import com.game.pb.CommonPb;
import com.game.pb.DataPb;
import java.util.Map;

/**
 * 战斗接口
 */
public interface IWar {

	/**
	 * 战斗id
	 *
	 * @return
	 */
	long getWarId();

	/**
	 * 开始时间
	 *
	 * @return
	 */
	long getStartTime();

	/**
	 * 战斗结束时间
	 *
	 * @return
	 */
	long getEndTime();

	/**
	 * 战斗类型  1.远征 2.奔袭 3.国战 4.闪电
	 *
	 * @return
	 */
	int getWarType();

	/**
	 * 地图编号
	 *
	 * @return
	 */
	int getMapId();

	/*
	 state = 1, 等待 state = 2, 战斗中, state = 3.开始战斗 state = 4, 战斗取消
	 */
	int getState();

	/**
	 * 攻击方
	 *
	 * @return
	 */
	IFighter getAttacker();

	/**
	 * 防守方
	 *
	 * @return
	 */
	IFighter getDefencer();


	/**
	 * 是否结束
	 *
	 * @return
	 */
	boolean isEnd();

	/**
	 * 更新状态
	 *
	 * @param state
	 */
	void updateState(int state);

	/**
	 * 近卫军放进来
	 *
	 * @return
	 */
	Map<Integer, SquareMonster> getMonsters();

	boolean isJoin(Player player);

	/**
	 * 序列号
	 *
	 * @param join
	 * @return
	 */
	CommonPb.WarInfo.Builder wrapPb(boolean join);

	/**
	 * 战斗数据
	 *
	 * @return
	 */
	DataPb.WarData.Builder writeData();

}
