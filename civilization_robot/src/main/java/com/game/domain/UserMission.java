package com.game.domain;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @Description
 * @Date 2022/10/21 17:59
 **/

@Getter
@Setter
public class UserMission {

	private int id; //关卡Id
	private int star; //星级
	private long resourceTime; //资源副本剩余时间，单位毫秒秒
	private int fightTimes; //资源本攻打次数： 资源副本、资源田图纸
	private int buyTimes; //资源攻打次数的购买次数
	private int countryPropNum; //已经获得的国器碎片数
	private boolean isHeroBuy; //当前副本英雄是否已经招募
	private int resourceLandNum; //资源田碎片数
	private int buyEquipPaperTimes; //购买图纸次数
	private int state; //关卡状态 0 lock ,1 open, 2 complet

}
