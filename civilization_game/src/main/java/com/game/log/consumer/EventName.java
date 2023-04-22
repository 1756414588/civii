package com.game.log.consumer;

/**
 * @author cpz
 * @date 2020/12/6 13:19
 * @description
 */
public enum EventName {
    //    事件名称	"创建账号
    create_account,
    //    "	"登入游戏
    app_login,
    //    "	"退出账号
    quit_account,
    //    "	"设备新增(使用首次事件判断功能上传)
    new_device,
    //    "	"进入游戏（自动采集）
    ta_app_start,
    //    "	"离开游戏（自动采集）
    ta_app_end,
    //    "	"APP崩溃事件（自动采集）
    ta_app_crash,
    //    "		"创建角色
    create_role,
    //    "	"新手引导完成
    guide,
    //    "	"角色等级提升
    level_up,
    //    "	"军衔升级
    military_rank_level_up,
    //    "	"进入关卡
    enter_stage,
    //    "	"关卡结束
    end_stage,
    //    "	"科技解锁
    tech_unlock,
    //    "	"科技升级
    tech_level_up,
    //    "	"建筑解锁
    build_unlock,
    //    "	"建筑升级
    build_level_up,
    //    "		"参与活动
    join_activity,
    //    "	"完成活动
    complete_activity,
    //    "	"任务分配
    mission_distribute,
    //    "	"任务开始
    mission_start,
    //    "	"任务完成
    mission_complete,
    //    "	"领取任务奖励
    get_mission_reward,
    //    "		"抽卡
    summon,
    //    "	"商城购买
    shopping,
    //    "		"获得道具
    get_item,
    //    "	"消耗道具
    cost_item,
    //    "	"角色获得经验
    role_get_exp,
    //    "	"英雄获得经验
    hero_get_exp,
    //    "	"消耗晋升卡
    cost_promote_card,
    //    "	"获得金币
    get_coins,
    //    "	"消耗金币
    cost_coins,
    //    "	"获得钢铁
    get_iron,
    //    "	"消耗钢铁
    cost_iron,
    //    "	"获得食物
    get_food,
    //    "	"消耗食物
    cost_food,
    //    "	"获得晶体
    get_crystal,
    //    "	"消耗晶体
    cost_crystal,
    //    "	"获得钻石
    get_diamond,
    //    "	"消耗钻石
    cost_diamond,
    //    "		"获得英雄
    get_hero,
    //    "	"英雄升级
    hero_level_up,
    //    "	"英雄突破
    hero_breakthrough,
    //    "	"英雄特训
    hero_train,
    //    "	"英雄晋升传奇
    hero_become_legend,
    //    "		"装备打造
    equip_build,
    //    "	"装备精研
    equip_research,
    //    "	"装备锻炼(秘技精研)
    equip_train,
    //    "	"装备分解
    equip_break,
    //    "	"神器升级
    magic_weapon_level_up,
    //    "		"订单事件(可更新事件,订单状态变化时上报)
    order_event,
    //    "		"申请战队
    ask_for_guild,
    //    "	"创建战队
    create_guild,
    //    "	"加入战队
    join_guild,
    //    "	"离开战队
    quit_guild,
    //    "	"参与战队玩法
    play_guild_activity,
    //    "	"获得战队贡献度
    get_guild_points,
    //    "		"申请添加好友
    ask_for_being_friends,
    //    "	"邀请好友成功
    invite_friends,
    //    "	"通过好友请求
    allow_friends_ask,
    //    "	"好友对话
    talk_to_friends,
    //    "	"好友赠礼
    give_gift_to_friends,
    //    "	"赠礼领取
    get_gift_from_friends,
    //    "		"每日统计事件(每日每人上传一次)
    every_day_snapshot,
    get_teacher,
    get_student,
    //攻打虫族
    attack_rebel,
    make_a_vow,
    act_equip_wash,
    //转盘活动
    spin_the_wheel,
    //在线玩家
    online_user_amount,
    //世界活动
    //伏击叛军
    world_act_rebel,
    //虫族入侵
    world_act_roit,
    //母巢之战
    world_act_hatchery,
    //突破虫穴
    world_act_insect,
    //夜袭虫群
    world_act_night,
    //抢夺名城
    world_act_city,

    //用户事件
    modify_nick,
    add_honner,
    add_capacity,
    add_stage,
    player_move,
    add_hero,
    add_equip,
    add_equips,
    sub_equip,
    add_beauty,
    add_card,
    add_vip,
    guide_step,
    first_vip_up,
    first_pay,
    first_leave,
    buy_energy,
    cost_energy,
    kill_equip,
    hireOfficer,
    attackCity,
    //行军加速
    marchSpeed,
    highMove,
    worldBox,
    worldBoxKey,
    jumpCgPlane,
    jumpCgMonster,
    jouneryDone,
    jouneryBuy,
    soldierChange,
    vipShop,
    equipDone,
    TDDone,
    TDReward,
    dailyTask,
    wearOmament,
    upOmament,
    countryBuild,
    buyQue,
    countryTask,
    countryMail,
    useSpeed,
    dilatation,
    buildTeam,
    cityProtect,
    quicken,
    reBuild,
    smallQuicken,
    soldierQuicken,
    attackUp,
    defenceUp,
    omamentCompound,
    makeProp,
    beautyPlayGame,
    beautyGift,
    beautySeek,
    beautyUp,
    //在线时长
    onlin_time,
    ;
}
