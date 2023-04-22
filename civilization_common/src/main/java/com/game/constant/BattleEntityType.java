package com.game.constant;

// 1.玩家武将 2.pve怪物 3.叛军 4.世界BOSS 5.玩家城防军 6.友军武将 7.城池的城防军
public interface BattleEntityType {
    int HERO = 1;      // 英雄
    int MONSTER = 2;      // pve怪物
    int REBEL = 3;      // 叛军
    int WORLD_BOSS = 4;      // 世界BOSS
    int WALL_DEFENCER = 5;     // 城防军
    int FRIEND_HERO = 6;      // 友军武将(参与进攻或者防守)
    int CITY_MONSTER = 7;     // NPC城池的城防军
    int WALL_FRIEND_HERO = 8; // 友军驻防城墙武将
    int GUARD_MONSTER = 9;   // 禁卫军
    int DEFENSE_ARMY_HERO = 10;      // 参谋部城防军
    int REBEL_MONSTER = 11;      // 伏击叛军
    int ROIT_MONSTER = 12;    //虫族入侵
    int BIG_MONSTER = 13;   //巨型虫族/精英叛军
}
