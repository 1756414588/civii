package com.game.worldmap;

public interface MarchType {
    int AttackMonster = 1; // 击杀流寇
    int CollectResource = 2; // 资源采集
    int AttackCityQuick = 3; // 玩家闪电战
    int AttackCityFar = 4; // 玩家发起远征或者奔袭战
    int CityFriendAssist = 5; // 城池驻防
    int CountryWar = 6; // 国战
    int MonsterAttack = 7; // 黄巾军/西凉军暴乱[玩家属于防守方/防守方可以申请支援]
    int RiotWar = 8; // 暴乱
    int ReBelWar = 9; //伏击叛军
    int BigWar = 10;    //巨型虫族
    int BROOD_WAR = 11;    //母巢
    int SUPER_ATTACK = 12; //攻击大型资源
    int SUPER_COLLECT = 13; //采集大型资源点
    int SUPER_ASSIST = 14; //驻防大型资源点
    int QUICK_ASSIST = 15; //闪电战参与防守
    int ZERG_WAR = 16; //虫族主宰行军
    int ZERG_DEFEND_WAR = 17; //虫族主宰防守阶段行军

    int FLAME_WAR = 18; //战火燎原攻击建筑行军
    int FLAME_COLLECT = 19; //战火燎原攻击建筑行军
    int FLAME_ATTACK = 20; //战火燎原攻击玩家行军
}
