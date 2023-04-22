package com.game.constant;

import com.game.log.domain.JourneyLog;

/**
 * @author cpz
 * @date 2020/11/20 11:12
 * @description
 */
public enum LogTable {
    role_login("role_login"),
    role_create("role_create"),
    role_exp("role_exp"),
    role_title("role_title"),
    role_resource("role_resource"),
    hero_exp("hero_exp"),
    hero_advance("hero_advance"),
    hero_wash("hero_wash"),
    hero_divine("hero_divine"),
    equip_add("equip_add"),
    equip_wash("equip_wash"),
    equip_decompound_log("equip_decompound_log"),
    kill_equip_log("kill_equip_log"),
    vip_exp("vip_exp"),
    role_guide("role_guide"),
    role_misson("role_misson"),
    role_task("role_task"),
    role_iron_log("role_iron_log"),
    role_copper_log("role_copper_log"),
    role_oil_log("role_oil_log"),
    role_stone_log("role_stone_log"),
    role_gold_log("role_gold_log"),
    role_item_log("role_item_log"),
    chat_log("chat_log"),
    td_log("td_log"),
    beauty_item_log("beauty_item_log"),
    login_log("login_log"),
    energy_log("energy_log"),
    battle_log("battle_log"),
    hatchery_log("hatchery_log"),//母巢
    draw_card_log("draw_card_log"),//抽卡
    world_box_log("world_box_log"),//世界宝箱
    activity_log("activity_log"),//活动log
    act_hope_log("act_hope_log"),//许愿池
    personal_signature_log("personal_signature_log"),//个性签名
    mail_log("mail_log"),//邮件
    seek_log("seek_log"),//搜寻
    war_book_log("war_book_log"),//兵书
    wear_book_log("wear_book_log"),//兵书穿戴
    record_reissue_awards_log("record_reissue_awards_log"),//合服前活动记录
    //配饰活动日志
    omament_log("omament_log"),
    journey_log("journey_log"),
    glover_log("glover_log"),
    broodWar_buyBuff_log("broodWar_buyBuff_log"),
    broodWar_entity_battle_count_log("broodWar_entity_battle_count_log"),
    get_act_power_rq_log("get_act_power_rq_log"),
    endless_td_log("endless_td_log"),
    endless_td_error_log("endless_td_error_log"),
    material_substitution_log("material_substitution_log"),
    manoeuvre_log("manoeuvre_log"),
    ;


    String table;

    LogTable(String table) {
        this.table = table;
    }

    public String table() {
        return table;
    }
}
