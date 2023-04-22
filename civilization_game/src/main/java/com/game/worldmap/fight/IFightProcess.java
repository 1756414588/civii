package com.game.worldmap.fight;


import com.game.domain.p.WorldActPlan;
import com.game.domain.p.WorldMap;
import com.game.worldmap.MapInfo;
import com.game.worldmap.March;

public interface IFightProcess {

	/**
	 * 初始化
	 *
	 * @param warTypes
	 * @param marches
	 */
	void init(int[] warTypes, int[] marches);


	/**
	 * 加载战斗
	 *
	 * @param worldMap
	 * @param mapInfo
	 * @return
	 */
	void loadWar(WorldMap worldMap, MapInfo mapInfo);

	/**
	 * 行军处理接口
	 *
	 * @param mapInfo
	 * @param march
	 */
	void doMarch(MapInfo mapInfo, March march);

	/**
	 * 战斗处理接口
	 *
	 * @param mapInfo
	 * @param war
	 */
	void process(MapInfo mapInfo, IWar war);

	/**
	 * 世界活动
	 */
	void doWorldActPlan(WorldActPlan worldActPlan);

	/**
	 * 检测行军
	 *
	 * @param march
	 */
	void checkMarch(MapInfo mapInfo, March march);


}
