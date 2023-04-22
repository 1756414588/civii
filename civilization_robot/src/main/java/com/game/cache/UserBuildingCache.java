package com.game.cache;

import com.game.util.Pair;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author 陈奎
 * @Description
 * @Date 2022/10/25 15:42
 **/

@Getter
@Setter
public class UserBuildingCache {

	// 司令部等级
	private int command;

	// 科技馆等级
	private int tech;

	//坦克
	private int tankCamp;

	//火箭
	private int rockCamp;

	//步兵
	private int footCamp;

	// 建筑队列信息
	private int workQue;

}
