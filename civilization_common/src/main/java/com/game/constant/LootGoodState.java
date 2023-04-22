package com.game.constant;

// 抽奖掉落状态机
public interface LootGoodState {
    int Special_Three_Loot = 1;  // 特殊3抽状态[首次开启神将掉落设置]
    int Four_Loot_Hero = 2;      // 特殊4抽神将[前3次没有掉落神将]
    int Four_Loot_Hero_Chip = 3; // 特殊4抽神将碎片[前3次有掉落神将]
    int Five_Times_Speical = 4;  // 5次必出[第一个五次, 再次开启时重置]
    int Ten_Times_Special = 5;   // 10次必出[到10次必出]
    int Common_Loot = 6;         // 正常掉落[5、6~9次], 再次开启, 需要清除前3次有没有掉落神将状态


}
