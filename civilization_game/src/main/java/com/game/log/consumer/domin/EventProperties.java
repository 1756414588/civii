package com.game.log.consumer.domin;

import com.game.constant.ResourceType;
import com.game.constant.SoldierType;
import com.game.domain.Player;
import com.game.domain.p.Account;
import com.game.domain.p.Item;
import com.game.domain.p.Soldier;
import com.game.log.consumer.EventName;
import lombok.Builder;

import java.util.Date;
import java.util.HashMap;

/**
 *
 * @date 2020/12/6 0:58
 * @description
 */
public class EventProperties extends BaseProperties {
    @Builder
    public EventProperties(Player player, EventName eventName) {
        Account account = player.account;
        if (account == null) {
            return;
        }
        this.eventName = eventName.name();
        this.distinct_id = account.getAccountKey() + "";
        this.account_id = player.roleId + "";
        properties = new HashMap<>();
        properties.put("#account_id", account.getAccountKey());
        properties.put("#distinct_id", account.getAccountKey());
        properties.put("#device_id", account.getDeviceNo());
        properties.put("#ip", account.getLastLoginIp());
        properties.put("#country_code", player.getCountry());
        properties.put("account", account.getAccountKey());
        properties.put("account_platform", account.getChannel());
        properties.put("role_amount", 1);
        properties.put("last_account_login_time", account.getLoginDate());
        properties.put("role_id", player.roleId);
        properties.put("role_name", player.getNick());
        properties.put("role_name_is_random", 1);
        properties.put("server_id", account.getServerId());
        properties.put("role_countryid", player.getCountry());
        properties.put("role_contribution", player.getHonor());
        properties.put("fighting_capacity", player.getMaxScore());
        properties.put("role_level", player.getLevel());
        properties.put("military_rank", player.getTitle());
        properties.put("role_exp", player.getExp());
        properties.put("role_power", player.getEnergy());
        properties.put("max_stage_level", player.getLord().getCurMainDupicate());
        properties.put("map_id", player.getPosStr());
        properties.put("coins_amount", player.getResource(ResourceType.IRON));
        properties.put("diamond_amount", player.getGold());
        properties.put("hero_amount", player.getHeros().size());
        properties.put("equip_amount", player.getEquips().size());
        properties.put("beauty_amount", player.getBeautys().size());
        Item item = player.getItem(82);
        if (item != null) {
            properties.put("promote_card_amount", item.getItemNum());
        } else {
            properties.put("promote_card_amount", 0);
        }
        properties.put("resource1_amount", player.getResource(ResourceType.IRON));
        properties.put("resource2_amount", player.getResource(ResourceType.COPPER));
        properties.put("resource3_amount", player.getResource(ResourceType.OIL));
        properties.put("resource4_amount", player.getResource(ResourceType.STONE));
        Soldier rocket = player.getSoldier(SoldierType.ROCKET_TYPE);
        Soldier tank = player.getSoldier(SoldierType.TANK_TYPE);
        Soldier war = player.getSoldier(SoldierType.WAR_CAR);
        properties.put("soldiers1_amount", rocket == null ? 0 : rocket.getNum());
        properties.put("soldiers2_amount", tank == null ? 0 : tank.getNum());
        properties.put("soldiers3_amount", war == null ? 0 : war.getNum());
//        properties.put("guild_id", account.getAccountKey());
//        properties.put("guild_name", account.getAccountKey());
//        properties.put("guild_level", account.getAccountKey());
        properties.put("vip_level", player.getVip());
        properties.put("IMEI", account.getDeviceNo());
        properties.put("imei", account.getDeviceNo());
//        properties.put("IDFA", account.getAccountKey());
//        properties.put("device_id_cus", account.getAccountKey());
        properties.put("guide_step", player.getLord().getGuideKey());
//        properties.put("bundle_id", account.getAccountKey());
//        properties.put("xx_id", account.getAccountKey());
        properties.put("is_banned", account.getForbid());
        properties.put("continuous_login_days", player.getLord().getLoginDays());
        properties.put("total_active_days", player.getLord().getLoginDays());
        properties.put("total_active_seconds", player.getLord().getOnTime() / 1000L);
        this.eventName = eventName.name();
    }
}
