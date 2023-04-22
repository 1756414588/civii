package com.game.constant;

public interface TaskType {
	int NEW_STATE = 1; // 新手引导
	int BUILDING_LEVELUP = 2; // 指定Id建筑升级
	int START_TECH_UP = 3; // 科技升级
	int RES_BUILDING_LEVEL_UP = 4; // 多个资源建筑升级(非指定Id)
	int SINGLE_HERO_WEAR = 5; // 单个将领穿装备(param: 将领Id, 装备Id, 装备Id2)
	int GET_RESOURCE = 6; // 征收资源
	int START_HIRE_SOLDIER = 7; // 募兵一次
	int START_MAKE_EQUIP = 8; // 开始打造装备
	int DONE_MISSION = 9; // 完成副本
	int MISSION_HIRE_HERO = 10; // 副本武将招募
	int DONE_ANY_SUBTASK = 11; // 完成任意的支线任务
	int EMBATTLE_HERO = 12; // 武将上阵
	int AWARD_EQUIP = 13; // 收获打造
	int ARMMY_RETURN = 14; // 等待部队归来
	int HIRE_RESEARCHER = 15; // 雇佣研究员
	int FINISH_TECH = 16; // 研究完成
	int LORD_LEVEL_UP = 17; // 主公升级
	int HIRE_BLACKSMITH = 18; // 招募军需官
	int SPEED_MAKE_EQUIP = 19; // 军需官加速打造
	int HIRE_OFFIER = 20; // 招募内政官
	int WASH_EQUIP = 21; // 洗练装备
	int MAKE_KILL_EQUIP = 22; // 打造国器
	int LEVELUP_KILL_EQUIP = 23; // 升级国器
	int MAKE_PROP = 24; // 生产材料
	int ALL_HERO_WEARHAT = 25; // 全部武将穿戴精铁盔
	int ALL_HERO_WEAR_TWO = 26; // 全部武将穿戴守备印和千营符
	int HIRE_SOLDIER_NUM = 27; // 招募兵x个(param: 兵类型, 兵数)
	int HIRE_SOLDIER_TIMES = 28; // 招募兵x次(param: 兵类型)(最大进度表示次数)
	int CAPTURE_CITY = 29; // 攻克郡营|郡县|郡城|州郡|州府|州城(param:城池的类型)
	int KILL_REBEL = 30; // 击杀叛军-等级
	int KILL_MUTIL_REBEL = 31; // 击杀多队叛军
	int START_LEVELUP_BUILDING = 32; // 开始升级建筑到多少级, 点按钮完成(param:建筑Id, 建筑等级)
    int DEPOT = 33; // 点击聚宝盆一次
    int ANY_WEAR = 34; // 给任意将领装备防寒大衣
    int COMMON_HERO = 35; // 上将寻访
    int GOOD_HERO = 36; // 大将寻访
    int MISSION_HIRE_ANY = 37; // 关卡招募任意武将
    int HIRE_SOLDIER_TIMES_ANY = 38; // 募兵任意次数
    int SAY_A_WORD = 39; // 在国家频道发送一条信息
	int ADD_SOILIER_YUBEI =40; //预备兵营招募一次士兵
	int RECOVER_BUILDING =41; //收复建筑
	int REAUTY_SEEKING =42; //美女约会
	int HIRE_SCIENTIST =43; //利用雇佣的科学家进行一次研究加速
	int REAUTY_SGAME =44; //美女小游戏
	int LEARN_FROM_TEACHER = 45;	//拜师
	int RECRUIT_STUDENTS =46;	//收徒
	int DONE_JOURNEY =47;	//远征
	int OMAMENT_WEARER = 48;  //佩戴配饰
	int MAKE_SECOND_EQUIP = 49;  //打造第二件装备
	int COLLECT_EQUIP = 50;	//收集装备
	int COST_GOLD = 51;	//消费钻石
	//指定英雄穿戴某个位置的装备
	int POS_WEAR_EQUIP = 52;
	// 完成塔防关卡（param：塔防关卡id）
	int COMPLETE_TOWER_DEFENSE = 54;
	int AUTO_CPM =55;//自动完成该任务
	int AUTO_S_TASK=56;//完成所属的五个章节任务后完成该任务
	int GOVER =57;
	int QUICK_MONSTER =58;

}

