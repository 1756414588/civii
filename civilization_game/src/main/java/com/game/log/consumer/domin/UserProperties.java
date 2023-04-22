package com.game.log.consumer.domin;

import com.game.constant.BuildingId;
import com.game.constant.ResourceType;
import com.game.constant.SoldierType;
import com.game.domain.Player;
import com.game.domain.p.Account;
import com.game.domain.p.BuildingBase;
import com.game.domain.p.Item;
import com.game.domain.p.Market;
import com.game.domain.p.Soldier;
import com.game.log.consumer.EventName;

import java.util.Date;
import java.util.HashMap;

/**
 * @author cpz
 * @date 2020/12/17 15:45
 * @description
 */
public class UserProperties extends BaseProperties {

    public UserProperties(Player player, EventName eventName) {
        Account account = player.account;
        if (account == null) {
            return;
        }
        this.distinct_id = account.getAccountKey() + "";
        this.account_id = player.roleId + "";
        properties = new HashMap<>();
        switch (eventName) {
            case app_login://登陆
            case quit_account://登出
                properties.put("device_id", account.getDeviceNo());
                properties.put("role_id", player.roleId);
                properties.put("account", account.getAccountKey());
                properties.put("account_platform", account.getChannel());
                properties.put("role_amount", 1);
                properties.put("last_account_login_time", account.getLoginDate());
                if(eventName == EventName.quit_account){
                    properties.put("last_account_logout_time", new Date());
                }
                properties.put("role_countryid", player.getCountry());
                if (player.getLord().getOlTime() == 0) {    //首次登陆上报
                    properties.put("role_level", player.getLevel());
                }
                properties.put("role_power", player.getEnergy());
                properties.put("diamond_amount", player.getGold());
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
                if (eventName == EventName.app_login) {
                    properties.put("IMEI", "IMEI");
                    properties.put("IDFA", account.getDeviceNo());
                    properties.put("imei", "IMEI");
                    properties.put("idfa", account.getDeviceNo());
                    properties.put("device_id_cus", "uuid");
                    properties.put("last_login_time", account.getLoginDate());
                }
                properties.put("bundle_id", "xinkuai");
                properties.put("xx_id", account.getChannel());
                properties.put("is_banned", account.getForbid());
                properties.put("continuous_login_days", player.getLord().getLoginDays());
                properties.put("total_active_days", player.getLord().getLoginDays());
                properties.put("total_active_seconds", player.getLord().getOnTime() / 1000L);
                properties.put("total_charge_amount", player.getLord().getTopup());
                register("commandLv", player.getCommandLv());
                register("techLv", player.getTechLv());
                BuildingBase camp = player.getBuilding(BuildingId.ROCKET_CAMP);
                BuildingBase tankBuild = player.getBuilding(BuildingId.TANK_CAMP);
                BuildingBase car = player.getBuilding(BuildingId.WAR_CAR_CAMP);
                register("ROCKET_CAMP", camp != null ? camp.getLevel() : 0);
                register("TANK_CAMP", tankBuild != null ? tankBuild.getLevel() : 0);
                register("WAR_CAR_CAMP", car != null ? car.getLevel() : 0);
                register("rocket_camp", camp != null ? camp.getLevel() : 0);
                register("tank_camp", tankBuild != null ? tankBuild.getLevel() : 0);
                register("war_car_camp", car != null ? car.getLevel() : 0);
                register("ware", player.getWare().getLv());
                register("wall", player.getWall().getLv());
                Market market = player.buildings.getMarket();
                register("market", market != null ? market.getLv() : 0);

                register("first_online_time", player.getLord().getOlTime());
                break;
            case modify_nick:
                register("role_name", player.getLord().getNick());
                register("role_name_is_random", false);
                break;
            case add_honner:
                register("role_contribution", player.getHonor());
                break;
            case add_capacity:
                register("fighting_capacity", player.getMaxScore());
                break;
            case military_rank_level_up:
                register("military_rank", player.getTitle());
                break;
            case add_stage:
                register("max_stage_level", player.getLord().getCurMainDupicate());
                break;
            case player_move:
                register("map_id", player.getPosStr());
                break;
            case add_hero:
                register("hero_amount", player.getHeros().size());
                break;
            case add_equip:
                register("equip_amount", player.getEquips().size());
                break;
            case add_beauty:
                register("beauty_amount", player.getBeautys().size());
                break;
            case add_card:
                Item item = player.getItem(82);
                if (item != null) {
                    register("promote_card_amount", item.getItemNum());
                } else {
                    register("promote_card_amount", 0);
                }
                break;
            case add_vip:
                register("vip_level", player.getVip());
                break;
            case guide_step:
                register("guide_step", player.getLord().getGuideKey());
                break;
            case create_role:
                register("role_create_time", new Date());
                register("account_create_time", account.getCreateDate());
                register("account_create_time", account.getCreateDate());
                register("platform_id", account.getChannel());
                register("channel_id", account.getChannel());
                register("server_id", account.getServerId());
                register("role_countryid", player.getCountry());
                break;
            case first_vip_up:
                register("first_vip_level_up", new Date());
                break;
            case first_pay:
                register("first_charge_amount", player.getLord().getTopup());
                register("first_charge_time", new Date());
                break;
            case first_leave:
                //离线时间
                register("first_off_time", new Date());
                //首次在线时长
                register("first_ol_time", player.getLord().getOlTime());
                break;
        }
    }
}
