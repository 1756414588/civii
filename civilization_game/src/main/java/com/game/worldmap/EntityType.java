package com.game.worldmap;

// 世界实体类型
public interface EntityType {
    int Monster = 1; // 叛军
    int Resource = 2; // 资源
    int PleyerCity = 3; // 玩家
    int NpcCity = 4; // Npc城池
    int Land = 5; // 地形
    int PrimaryCollect = 6; // 初级采集点
    int REBEL_MONSTER = 7;
    int RIOT_MONSTER = 8;    //虫族入侵
    int BIG_MONSTER = 9;    //巨型虫族
    int BIG_RESOURCE = 10;    //大型资源点
}
