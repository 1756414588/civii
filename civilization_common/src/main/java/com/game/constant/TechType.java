package com.game.constant;

public interface TechType {
    int IRON    = 1;                // 增加生铁百分比
    int COPPER  = 2;                // 增加黄铜百分比
    int OIL     = 3;                // 增加石油百分比
    int STONE   = 4;                // 增加宝石百分比
    int ROCKET_SOLDIER_NUM = 5;     // 火箭兵数量
    int TANK_SOLDIER_NUM = 6;       // 坦克兵数量
    int WARCAR_SOLDIER_NUM = 7;     // 战车数量
    int ROCKET_ATTACK   = 8;        // 火箭兵增加攻击+耗粮(后期改成自由组合加成效果)
    int TANK_ATTACK   = 9;          // 坦克兵增加攻击+耗粮(后期改成自由组合加成效果)
    int WARCAR_ATTACK   = 10;       // 战车兵增加攻击+耗粮(后期改成自由组合加成效果)
    int PRIMARY_SOLDIER_LINE = 11;  // 初级点兵术+增加排数
    int PRIMARY_HERO_NUM = 12;      // 初级统率力
    int MARCH = 13;                 // 增加行军速度
    int BUILD_SPEED = 14;           // 减少建筑升级时间
    int WARE_CAPACITY = 15;         // 提高仓库保护量
    int WORK_SPEED = 16;            // 降低作坊生产耗时
    int SPECIAL_SKILL = 17;         // 开启第4技能
    int SPECIAL_EQUIP = 18;         // 打造有几率获得秘技的装备
    int REBUILD       = 19;         // 拆除指定建筑并重造
    int COLLECT_SPEED = 20;         // 采集加成
    int HERO_ADVANCE  = 21;         // 武将突破
    int HONOR         = 22;         // 提高获得威望百分比
    int COUNTRY_ITEM_CRITI = 23;    // 国器暴击
    int SCOUT = 24;                 // 侦察
    int AUTO_ADD_SOLDIER = 25;      // 自动补兵
    int MIDDLE_SOLDIER_LINE = 26;   // 中级点兵术+增加排数
    int MIDDLE_HERO_NUM = 27;       // 中级统率力

    int ROCKET_ATTACK_STAGE   = 28;        // 火箭兵增加攻击+耗粮(后期改成自由组合加成效果)
    int TANK_ATTACK_STAGE   = 29;          // 坦克兵增加攻击+耗粮(后期改成自由组合加成效果)
    int WARCAR_ATTACK_STAGE   = 30;       // 战车兵增加攻击+耗粮(后期改成自由组合加成效果)
}
