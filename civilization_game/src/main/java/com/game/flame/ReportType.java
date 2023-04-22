package com.game.flame;

/**
 * --不同情况下需要的字段如下： --1、首次占领： 据点id，首次个人资源
 * --2、驻守：我的英雄id, 据点id，敌对玩家名，敌对玩家位置，敌对英雄id, 杀敌数，损兵数
 * --3、进攻：我的英雄id, 据点id，敌对玩家名，敌对玩家位置，敌对英雄id, 杀敌数，损兵数
 * --4、持续占领： 我的英雄id, 据点id，每分钟资源产出
 */
public interface ReportType {
	int REPORT_1 = 1;
	int REPORT_2 = 2;
	int REPORT_3 = 3;
	int REPORT_4 = 4;
}
