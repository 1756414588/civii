package com.game.worldmap.fight;

import com.game.worldmap.March;
import com.game.worldmap.Pos;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 参战双方接口
 */
public interface IFighter {


	long getId();

	/**
	 * 名称
	 *
	 * @return
	 */
	String getName();

	/**
	 * 等级
	 *
	 * @return
	 */
	int getLevel();

	/**
	 * 战斗力
	 *
	 * @return
	 */
	long getPower();

	/**
	 * 坐标点
	 *
	 * @return
	 */
	Pos getPos();

	/**
	 * 0npc1-3玩家国家
	 *
	 * @return
	 */
	int getCountry();

	/**
	 * 类型0玩家1城池2怪兽
	 *
	 * @return
	 */
	int getType();

	/**
	 * 部队合集
	 *
	 * @return
	 */
	ConcurrentLinkedDeque<March> getMarchList();

	/**
	 * 添加行军
	 *
	 * @param march
	 */
	void addMarch(March march);

	/**
	 * 移除
	 *
	 * @param march
	 * @return
	 */
	boolean removeMarch(March march);

	/**
	 * 救援次数
	 *
	 * @return
	 */
	int getHelpTime();

	/**
	 * 当前总兵力
	 *
	 * @return
	 */
	int getSoldierNum();

}
