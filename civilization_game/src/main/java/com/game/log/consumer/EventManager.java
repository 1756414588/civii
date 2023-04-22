package com.game.log.consumer;

import com.game.constant.EventType;
import com.game.constant.Quality;
import com.game.constant.Reason;
import com.game.constant.SoldierName;
import com.game.domain.Award;
import com.game.domain.Player;
import com.game.log.consumer.domin.BaseProperties;
import com.game.log.consumer.domin.EventProperties;
import com.game.log.consumer.domin.UserProperties;
import com.game.manager.XinkuaiManager;
import com.game.server.exec.LogExecutor;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cpz
 * @date 2020/12/6 13:29
 * @description
 */
@Component
public class EventManager {

	@Autowired
	LoggerConsumer loggerConsumer;
	@Autowired
	private XinkuaiManager xinkuaiManager;
	@Autowired
	private LogExecutor logExecutor;

	public EventProperties build(Player player, EventName eventName) {
		return EventProperties.builder().eventName(eventName).player(player).build();
	}

	//    "创建账号
	public void create_account(Player player) {
		logExecutor.add(() -> {
			EventProperties build = build(player, EventName.create_account);
			loggerConsumer.track(build);
			xinkuaiManager.pushXinkuai(build, EventType.TRACK.getType());
			 
		});
	}

	//"	"登入游戏
	public void app_login(Player player) {
		logExecutor.add(() -> {
			EventProperties build = build(player, EventName.app_login);
			loggerConsumer.track(build);
			xinkuaiManager.pushXinkuai(build, EventType.TRACK.getType());
			 
		});
	}

	//"	"退出账号
	public void quit_account(Player player) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.quit_account);
			properties.register("offline_time", new Date());
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	//"	"设备新增(使用首次事件判断功能上传)
	public void new_device(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.new_device));
			 
		});
	}

	//"	"进入游戏（自动采集）
	public void ta_app_start(Player player) {
		logExecutor.add(() -> {
			EventProperties build = build(player, EventName.ta_app_start);
			loggerConsumer.track(build);
			xinkuaiManager.pushXinkuai(build, EventType.TRACK.getType());
			 
		});
	}

	//"	"离开游戏（自动采集）
	public void ta_app_end(Player player) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.ta_app_end);
			properties.register("#duration", player.getLord().getOlTime());
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"APP崩溃事件（自动采集）
	public void ta_app_crash(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.ta_app_crash));
			 
		});
	}

	//"		"创建角色
	public void create_role(Player player) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.create_role);
			properties.register("role_name", player.getLord().getNick());
			properties.register("role_create_time", new Date());
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"新手引导完成
	public void guide(Player player, int guidKey) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.guide);
			properties.register("guidkey", guidKey);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	//"	"角色等级提升
	public void level_up(Player player, int promotion_level) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.level_up);
			properties.register("promotion_level", promotion_level);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"军衔升级
	public void military_rank_level_up(Player player, long amout) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.military_rank_level_up);
			properties.register("title", player.getTitle());
			properties.register("cost_contribution_amount", amout);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"进入关卡
	public void enter_stage(Player player, int missionId, String missionName) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.enter_stage);
			properties.register("stage_id", missionId);
			properties.register("stage_name", missionName);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"关卡结束
	public void end_stage(Player player, int missionId, String missionName, Date startTime, int result, List<List<Integer>> rewards) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.end_stage);
			properties.register("stage_id", missionId);
			properties.register("stage_name", missionName);
			properties.register("mission_start_time", startTime);
			properties.register("stage_result", result == 0 ? "胜利" : "失败");
			properties.register("reward_list", rewards);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"科技解锁
	public void tech_unlock(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.tech_unlock));
			 
		});
	}

	//"	"科技升级
	public void tech_level_up(Player player, List<List<Long>> resourceCond, int tech_id, String tech_name, int level_now) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.tech_level_up);
			properties.register("tech_id", tech_id);
			properties.register("tech_name", tech_name);
			properties.register("cost_item_list", resourceCond);
			properties.register("level_now", level_now);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"建筑解锁
	public void build_unlock(Player player, int buildId, String buildName) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.build_unlock);
			properties.register("building_id", buildId);
			properties.register("building_name", buildName);
			properties.register("cost_item_list", Lists.newArrayList());
			properties.register("level_now", 0);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	//"	"建筑升级
	public void build_level_up(Player player, int buildId, String buildName, List<List<Long>> resourceCond, int lv) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.build_level_up);
			properties.register("building_id", buildId);
			properties.register("building_name", buildName);
			properties.register("cost_item_list", resourceCond);
			properties.register("level_now", lv);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	//"		"参与活动
	public void join_activity(Player player, int activityId, String activityName, Object activityType) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.join_activity));
			EventProperties properties = build(player, EventName.join_activity);
			properties.register("activity_id", activityId);
			properties.register("activity_name", activityName);
			properties.register("activity_type", activityType);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"完成活动
	public void complete_activity(Player player, int activityId, String activityName, int activityType, Date startTime, Object rewards) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.complete_activity);
			properties.register("activity_id", activityId);
			properties.register("activity_name", activityName);
			properties.register("activity_type", activityType);
			properties.register("mission_start_time", startTime);
			properties.register("reward_list", rewards);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
		xinkuaiManager.pushActivity(player, activityId, activityName, 1);
	}

	//"	"任务分配
	public void mission_distribute(Player player) {
//		logExecutor.add(() -> {
//			//        loggerConsumer.track(getInst(player, EventName.mission_distribute));
//			 
//		});

	}

	//"	"任务开始
	public void mission_start(Player player, int mission_id, int mission_type, String mission_name) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.mission_start);
			properties.register("mission_id", mission_id);
			properties.register("mission_type", mission_type);
			properties.register("mission_name", mission_name);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"任务完成
	public void mission_complete(Player player, int mission_id, int taskType, int mission_type, String mission_name, List<List<Long>> reward_list) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.mission_complete);
			properties.register("mission_id", mission_id);
			properties.register("mission_type", mission_type);
			properties.register("mission_name", mission_name);
			properties.register("mission_start_time", new Date());
			properties.register("reward_list", reward_list);
			properties.register("reward_type", 0);
			properties.register("task_type", taskType);
			loggerConsumer.track(properties);
			get_mission_reward(player, mission_id, mission_type, mission_name, reward_list);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"领取任务奖励
	public void get_mission_reward(Player player, int mission_id, int mission_type, String mission_name, List<List<Long>> reward_list) {
		EventProperties properties = build(player, EventName.get_mission_reward);
		properties.register("mission_id", mission_id);
		properties.register("mission_type", mission_type);
		properties.register("mission_name", mission_name);
		properties.register("mission_start_time", new Date());
		properties.register("reward_list", reward_list);
		properties.register("reward_type", 0);
		xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
		loggerConsumer.track(properties);
	}

	//"		"抽卡
	public void summon(Player player, int genre, int cost, List<Award> rewards, int type) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.summon);
			properties.register("summon_genre", genre);
			properties.register("summon_cost_num", cost);
			properties.register("item_list", rewards);
			properties.register("summon_type", type);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"商城购买
	public void shopping(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.shopping);
			properties.register("goods_id", params.get(0));
			properties.register("goods_name", params.get(1));
			properties.register("buy_num", params.get(2));
			properties.register("cost_currency_type", params.get(3));
			properties.register("left_currency_type", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"		"获得道具
	public void get_item(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_item);
			properties.register("item_id", params.get(0));
			properties.register("get_amount", params.get(1));
			properties.register("get_entrance", params.get(2));
			properties.register("item_name", params.get(3));
			properties.register("price", params.get(4));
			properties.register("total_num_now", params.get(5));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"消耗道具
	public void cost_item(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cost_item);
			properties.register("item_id", params.get(0));
			properties.register("cost_amount", params.get(1));
			properties.register("cost_entrance", params.get(2));
			properties.register("item_name", params.get(3));
			properties.register("total_num_now", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"角色获得经验
	public void role_get_exp(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.role_get_exp);
			properties.register("get_amount", params.get(0));
			properties.register("get_entrance", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"英雄获得经验
	public void hero_get_exp(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.hero_get_exp);
			properties.register("get_amount", params.get(0));
			properties.register("get_entrance", params.get(1));
			properties.register("hero_id", params.get(2));
			properties.register("hero_level", params.get(3));
			properties.register("total_num_now", params.get(4));
			properties.register("hero_type", params.get(5));
			properties.register("hero_quality", params.get(6));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"消耗晋升卡
	public void cost_promote_card(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cost_promote_card);
			properties.register("get_amount", params.get(0));
			properties.register("get_entrance", params.get(1));
			properties.register("hero_id", params.get(2));
			properties.register("hero_level", params.get(3));
			properties.register("total_num_now", params.get(4));
			properties.register("hero_type", params.get(5));
			properties.register("hero_quality", params.get(6));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"获得金币
	public void get_coins(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_coins);
			properties.register("get_amount", params.get(0));
			properties.register("get_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("price", params.get(3));
			properties.register("name", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"消耗金币
	public void cost_coins(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cost_coins);
			properties.register("cost_amount", params.get(0));
			properties.register("cost_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("name", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"获得钢铁
	public void get_iron(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_iron);
			properties.register("get_amount", params.get(0));
			properties.register("get_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("price", params.get(3));
			properties.register("name", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"消耗钢铁
	public void cost_iron(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cost_iron);
			properties.register("cost_amount", params.get(0));
			properties.register("cost_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("name", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"获得食物
	public void get_food(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_food);
			properties.register("get_amount", params.get(0));
			properties.register("get_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("price", params.get(3));
			properties.register("name", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"消耗食物
	public void cost_food(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cost_food);
			properties.register("cost_amount", params.get(0));
			properties.register("cost_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("name", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"获得晶体
	public void get_crystal(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_crystal);
			properties.register("get_amount", params.get(0));
			properties.register("get_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("price", params.get(3));
			properties.register("name", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"消耗晶体
	public void cost_crystal(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cost_crystal);
			properties.register("cost_amount", params.get(0));
			properties.register("cost_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("name", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	//"	"获得钻石
	public void get_diamond(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_diamond);
			properties.register("get_amount", params.get(0));
			properties.register("get_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("price", params.get(3));
			properties.register("get_entrance_name", Reason.ReasonName.getName((int) params.get(1)));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"消耗钻石
	public void cost_diamond(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cost_diamond);
			properties.register("cost_amount", params.get(0));
			properties.register("cost_entrance", params.get(1));
			properties.register("total_num_now", params.get(2));
			properties.register("cost_entrance_name", Reason.ReasonName.getName((int) params.get(1)));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"		"获得英雄
	public void get_hero(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_hero);
			properties.register("hero_id", params.get(0));
			properties.register("hero_keyid", params.get(0));
			properties.register("hero_type", params.get(1));
			properties.register("hero_quality", params.get(2));
			properties.register("atk_intelligence", params.get(3));
			properties.register("def_intelligence", params.get(4));
			properties.register("army_intelligence", params.get(5));
			properties.register("total_intelligence", params.get(6));
			properties.register("intelligence_limit", params.get(7));
			properties.register("intelligence_is_full", params.get(8));
			properties.register("cost_card_amount", params.get(9));
			properties.register("get_entrance", params.get(10));
			properties.register("hero_name", params.get(11));
			properties.register("quality_name", Quality.getName((int) params.get(2)));
			properties.register("solider_type", params.get(12));
			properties.register("solider_type_name", SoldierName.getName((int) params.get(12)));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"英雄升级
	public void hero_level_up(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.hero_level_up);
			properties.register("hero_id", params.get(0));
			properties.register("hero_keyid", params.get(0));
			properties.register("hero_type", params.get(1));
			properties.register("hero_quality", params.get(2));
			properties.register("atk_intelligence", params.get(3));
			properties.register("def_intelligence", params.get(4));
			properties.register("army_intelligence", params.get(5));
			properties.register("total_intelligence", params.get(6));
			properties.register("intelligence_limit", params.get(7));
			properties.register("intelligence_is_full", params.get(8));
			properties.register("cost_card_amount", params.get(9));
			properties.register("new_hero_level", params.get(10));
			properties.register("hero_name", params.get(11));
			properties.register("quality_name", Quality.getName((int) params.get(2)));
			properties.register("solider_type", params.get(12));
			properties.register("solider_type_name", SoldierName.getName((int) params.get(12)));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"英雄突破
	public void hero_breakthrough(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.hero_breakthrough);
			properties.register("hero_id", params.get(0));
			properties.register("hero_keyid", params.get(0));
			properties.register("hero_type", params.get(1));
			properties.register("hero_quality", params.get(2));
			properties.register("atk_intelligence", params.get(3));
			properties.register("def_intelligence", params.get(4));
			properties.register("army_intelligence", params.get(5));
			properties.register("total_intelligence", params.get(6));
			properties.register("intelligence_limit", params.get(7));
			properties.register("intelligence_is_full", params.get(8));
			properties.register("cost_card_amount", params.get(9));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"英雄特训
	public void hero_train(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.hero_train);
			properties.register("hero_id", params.get(0));
			properties.register("hero_keyid", params.get(0));
			properties.register("hero_type", params.get(1));
			properties.register("hero_quality", params.get(2));
			properties.register("atk_intelligence", params.get(3));
			properties.register("def_intelligence", params.get(4));
			properties.register("army_intelligence", params.get(5));
			properties.register("total_intelligence", params.get(6));
			properties.register("intelligence_limit", params.get(7));
			properties.register("intelligence_is_full", params.get(8));
			properties.register("cost_card_amount", params.get(9));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"英雄晋升传奇
	public void hero_become_legend(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.hero_become_legend);
			properties.register("hero_id", params.get(0));
			properties.register("hero_keyid", params.get(0));
			properties.register("hero_type", params.get(1));
			properties.register("hero_quality", params.get(2));
			properties.register("atk_intelligence", params.get(3));
			properties.register("def_intelligence", params.get(4));
			properties.register("army_intelligence", params.get(5));
			properties.register("total_intelligence", params.get(6));
			properties.register("intelligence_limit", params.get(7));
			properties.register("intelligence_is_full", params.get(8));
			properties.register("cost_card_amount", params.get(9));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"		"装备打造
	public void equip_build(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.equip_build);
			properties.register("equip_id", params.get(0));
			properties.register("equip_name", params.get(1));
			properties.register("equip_level", params.get(2));
			properties.register("equip_quality", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"装备精研
	public void equip_research(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.equip_research);
			properties.register("equip_id", params.get(0));
			properties.register("equip_name", params.get(1));
			properties.register("equip_level", params.get(2));
			properties.register("equip_quality", params.get(3));
			properties.register("equipWashFull", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"装备锻炼(秘技精研)
	public void equip_train(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.equip_train);
			properties.register("equip_id", params.get(0));
			properties.register("equip_name", params.get(1));
			properties.register("equip_level", params.get(2));
			properties.register("equip_quality", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"装备分解
	public void equip_break(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.equip_break);
			properties.register("equip_id", params.get(0));
			properties.register("equip_name", params.get(1));
			properties.register("equip_level", params.get(2));
			properties.register("equip_quality", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"	"神器升级
	public void magic_weapon_level_up(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.magic_weapon_level_up);
			properties.register("equip_id", params.get(0));
			properties.register("equip_name", params.get(1));
			properties.register("equip_level", params.get(2));
			properties.register("equip_quality", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	//"		"订单事件(可更新事件,订单状态变化时上报)
	public BaseProperties order_event(Player player, List<Object> params) {

		EventProperties properties = build(player, EventName.order_event);
		properties.setEvent_id(params.get(6));
		properties.register("order_id", params.get(0));
		properties.register("order_money_amount", params.get(1));
		properties.register("bundle_id", params.get(2));
		properties.register("item_id", params.get(4));
		properties.register("item_name", params.get(5));
//        properties.register("#event_id", params.get(6));
		properties.register("order_complete_time", params.get(7));
		properties.register("order_lose_efficacy", params.get(8));
		properties.register("is_first_pay", params.get(9));
		properties.register("pay_name", params.get(10));
		properties.register("productType", params.get(11));
		properties.register("productId", params.get(12));
		logExecutor.add(() -> {
			loggerConsumer.track_update(properties, params.get(6).toString());
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK_UPDATE.getType());
			 
		});
		return properties;
	}

	//"		"申请战队
	public void ask_for_guild(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.ask_for_guild));
			 
		});

	}

	//"	"创建战队
	public void create_guild(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.create_guild));
			 
		});

	}

	//"	"加入战队
	public void join_guild(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.join_guild));
			 
		});

	}

	//"	"离开战队
	public void quit_guild(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.quit_guild));
			 
		});

	}

	//"	"参与战队玩法
	public void play_guild_activity(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.play_guild_activity));
			 
		});

	}

	//"	"获得战队贡献度
	public void get_guild_points(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.get_guild_points));
			 
		});

	}

	//"		"申请添加好友
	public void ask_for_being_friends(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.ask_for_being_friends);
			properties.register("target_id", params.get(0));
			properties.register("target_level", params.get(1));
			properties.register("entrance", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	//"	"邀请好友成功
	public void invite_friends(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.invite_friends));
			 
		});

	}

	//"	"通过好友请求
	public void allow_friends_ask(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.allow_friends_ask);
			properties.register("target_id", params.get(0));
			properties.register("target_level", params.get(1));
			properties.register("entrance", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	//"	"好友对话
	public void talk_to_friends(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.talk_to_friends));
			 
		});

	}

	//"	"好友赠礼
	public void give_gift_to_friends(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.give_gift_to_friends));
			 
		});

	}

	//"	"赠礼领取
	public void get_gift_from_friends(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.get_gift_from_friends));
			 
		});

	}

	public void get_teacher(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_teacher);
			properties.register("target_id", params.get(0));
			properties.register("target_name", params.get(1));
			properties.register("target_level", params.get(2));
			properties.register("entrance", params.get(3));
			properties.register("type", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	public void get_student(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.get_student);
			properties.register("target_id", params.get(0));
			properties.register("target_name", params.get(1));
			properties.register("target_level", params.get(2));
			properties.register("entrance", params.get(3));
			properties.register("type", params.get(4));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	public void attack_rebel(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.attack_rebel);
			properties.register("rebel_level", params.get(0));
			properties.register("cost_soldier", params.get(1));
			properties.register("rewards", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	public void act_Hope(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.make_a_vow);
			properties.register("cost_amount", params.get(0));
			properties.register("get_amount", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	public void act_equip_wash(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.act_equip_wash);
			properties.register("level", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	public void spin_the_wheel(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.spin_the_wheel);
			properties.register("activity_id", params.get(0));
			properties.register("reward_list", params.get(1));
			properties.register("spin_type", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	public void equip_add(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.add_equips);
			properties.register("equip_id", params.get(0));
			properties.register("equip_name", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	public void equip_wear(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.sub_equip);
			properties.register("equip_id", params.get(0));
			properties.register("equip_name", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}


	//"		"每日统计事件(每日每人上传一次)
	public void every_day_snapshot(Player player) {
		logExecutor.add(() -> {
			loggerConsumer.track(build(player, EventName.every_day_snapshot));
			 
		});
	}


	//用户属性
	public void record_userInfo(Player player, EventName eventName) {
		logExecutor.add(() -> {
			UserProperties userProperties = new UserProperties(player, eventName);
			loggerConsumer.set(userProperties);
			xinkuaiManager.pushXinkuai(userProperties, EventType.USER_SET.getType());
			 
		});

	}

	//用户属性
	public void record_userInfo_once(Player player, EventName eventName) {
		logExecutor.add(() -> {
			UserProperties userProperties = new UserProperties(player, eventName);
			loggerConsumer.setOnce(userProperties);
			xinkuaiManager.pushXinkuai(userProperties, EventType.USER_SET_ONCE.getType());
			 
		});
	}

	/**
	 * 记录下在线时长
	 *
	 * @param player
	 * @param eventName
	 */
	public void record_onlineTime(Player player, EventName eventName) {
		logExecutor.add(() -> {
			UserProperties userProperties = new UserProperties(player, eventName);
			userProperties.register("online_time", System.currentTimeMillis() - player.getLoginTime());
			loggerConsumer.setOnce(userProperties);
			xinkuaiManager.pushXinkuai(userProperties, EventType.USER_SET_ONCE.getType());
			 
		});
	}

	public void online_user_amount(Map<String, Object> online, Object accountId, Object distinct_id) {
		logExecutor.add(() -> {
			BaseProperties baseProperties = new BaseProperties();
			baseProperties.setProperties(online);
			baseProperties.setAccount_id(accountId.toString());
			baseProperties.setDistinct_id(distinct_id.toString());
			baseProperties.setEventName(EventName.online_user_amount.name());
			if (loggerConsumer != null) {
				loggerConsumer.track(baseProperties);
			}
			xinkuaiManager.pushXinkuai(baseProperties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 伏击叛军
	 *
	 * @param player
	 * @param params
	 */
	public void worldActRebel(Player player, int state, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.world_act_rebel);
			properties.register("world_act_id", params.get(0));
			switch (state) {
				case 0: //召唤叛军
					properties.register("call_bebel", 1);
					properties.register("rebel_id", params.get(1));
					properties.register("rebel_level", params.get(2));
					break;
				case 1: //攻打叛军
					properties.register("attack_bebel", 1);
					properties.register("rebel_id", params.get(1));
					properties.register("rebel_level", params.get(2));
					break;
				case 2://叛军道具兑换
					properties.register("bebel_exchange", 1);
					properties.register("exchange_id", params.get(1));
					properties.register("exchange_num", params.get(2));
					break;
				default:
					break;
			}
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 世界活动虫族入侵
	 *
	 * @param player
	 * @param state
	 * @param params
	 */
	public void worldActRiot(Player player, int state, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.world_act_roit);
			properties.register("world_act_id", params.get(0));
			switch (state) {
				case 0: //攻打
					properties.register("attack_riot", 1);
					properties.register("rebel_id", params.get(2));
					properties.register("rebel_level", params.get(2));
					break;
				case 1: //道具兑换
					properties.register("exchange_bebel", 1);
					properties.register("exchange_riot_type", params.get(1));
					properties.register("exchange_riot_id", params.get(2));
					properties.register("exchange_riot_num", params.get(3));
					break;
				case 2://抵挡虫族波数
					properties.register("defence_max_round", params.get(1));
					break;
				default:
					break;
			}
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 世界活动 夜袭虫群
	 *
	 * @param player
	 * @param params
	 */
	public void worldActNightAttack(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.world_act_night);
			properties.register("world_act_id", params.get(0));
			properties.register("rebel_id", params.get(1));
			properties.register("rebel_level", params.get(2));
			properties.register("cost_soilder", params.get(3));
			properties.register("attack_heros", params.get(4));
			properties.register("rewards", params.get(5));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 体力购买
	 *
	 * @param player
	 * @param params
	 */
	public void buyEnergy(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.buy_energy);
			properties.register("buy_energy", params.get(0));
			properties.register("cost_gold", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 体力消耗
	 *
	 * @param player
	 * @param params
	 */
	public void costEnergy(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cost_energy);
			properties.register("exp", params.get(0));
			properties.register("cost_energy", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * @param player
	 * @param params
	 */
	public void getKillEquip(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.kill_equip);
			properties.register("resource", params.get(0));
			properties.register("isDrop", params.get(1));
			properties.register("cost_gold", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 雇佣内政管
	 *
	 * @param player
	 * @param params
	 */
	public void hireOfficer(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.hireOfficer);
			properties.register("free", params.get(0));
			properties.register("lv", params.get(1));
			properties.register("name", params.get(2));
			properties.register("employId", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 进攻
	 *
	 * @param player
	 * @param params
	 */
	public void attackCity(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.attackCity);
			properties.register("type", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}


	/**
	 * 行军加速
	 *
	 * @param player
	 * @param params
	 */
	public void marchSpeed(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.marchSpeed);
			//加速类型 中级 高级
			properties.register("type", params.get(0));
			//战斗类型 是什么
			properties.register("warType", params.get(1));
			//消耗钻石 0使用道具 1是购买并使用
			properties.register("costGold", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 高级迁城
	 *
	 * @param player
	 * @param params
	 */
	public void highMove(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.highMove);
			//当前地图
			properties.register("curMap", params.get(0));
			//迁城地图
			properties.register("moveMap", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 世界宝箱
	 *
	 * @param player
	 * @param params
	 */
	public void worldBox(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.worldBox);
			//宝箱品质
			properties.register("quality", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	public void worldBoxKey(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.worldBoxKey);
			//宝箱品质
			properties.register("keyNum", params.get(0));
			loggerConsumer.track(properties);
			 
		});
	}

	/**
	 * 跳过cg直升机坠落
	 *
	 * @param player
	 * @param params
	 */
	public void jumpCgPlane(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.jumpCgPlane);
			properties.register("oldGuidKey", params.get(0));
			properties.register("newGuidKey", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 跳过cgc虫潮来袭
	 *
	 * @param player
	 * @param params
	 */
	public void jumpCgMonster(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.jumpCgMonster);
			properties.register("oldGuidKey", params.get(0));
			properties.register("newGuidKey", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 远征通关
	 *
	 * @param player
	 * @param params
	 */
	public void journeryDone(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.jouneryDone);
			//远征关卡
			properties.register("journeyId", params.get(0));
			//最大记录
			properties.register("maxJourneyId", params.get(1));
			//次数
			properties.register("time", params.get(2));
			//奖励
			properties.register("reward", params.get(3));
			//输赢结果
			properties.register("journey_result", params.get(4));
			loggerConsumer.set(properties);
			//xinkuaiManager.pushXinkuai(properties, EventType.USER_SET.getType());
			 
		});
	}


	/**
	 * 远征次数购买
	 *
	 * @param player
	 * @param params
	 */
	public void journeryBuy(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.jouneryBuy);
			//次数
			properties.register("time", params.get(0));
			properties.register("cost_gold", params.get(1));
			loggerConsumer.set(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.USER_SET.getType());
			 
		});
	}

	/**
	 * 兵种变化
	 *
	 * @param player
	 * @param params
	 */
	public void soldierChange(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.soldierChange);
			//士兵类型
			properties.register("soldierType", params.get(0));
			//变化数量
			properties.register("maxJourneyId", params.get(1));
			properties.register("resource", params.get(2));
			loggerConsumer.set(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.USER_SET.getType());
			 
		});
	}

	/**
	 * vip礼包购买
	 *
	 * @param player
	 * @param params
	 */
	public void buyVipShop(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.vipShop);
			//礼包等级
			properties.register("vip", params.get(0));
			loggerConsumer.set(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.USER_SET.getType());
			 
		});
	}

	/**
	 * 装备打造
	 *
	 * @param player
	 * @param params
	 */
	public void equipDone(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.equipDone);
			properties.register("equip_id", params.get(0));
			properties.register("equip_name", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 塔防
	 *
	 * @param player
	 * @param params
	 */
	public void tdDone(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.TDDone);
			properties.register("TD_id", params.get(0));
			properties.register("TD_name", params.get(1));
			properties.register("TD_result", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 塔防奖励
	 *
	 * @param player
	 * @param params
	 */
	public void tDReward(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.TDReward);
			properties.register("reward", params.get(0));
			properties.register("tdName", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 日常任务积分
	 *
	 * @param player
	 * @param params
	 */
	public void dailyTask(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.dailyTask);
			properties.register("addScore", params.get(0));
			properties.register("totalScore", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 日常任务活跃度
	 *
	 * @param player
	 * @param params
	 */
	public void dailyActive(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.dailyTask);
			properties.register("score", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 勋章穿戴
	 *
	 * @param player
	 * @param params
	 */
	public void wearOmament(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.wearOmament);
			properties.register("omamentName", params.get(0));
			properties.register("lv", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 勋章升级
	 *
	 * @param player
	 * @param params
	 */
	public void upOmament(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.wearOmament);
			properties.register("omamentName", params.get(0));
			properties.register("lv", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	public void countryBuild(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.countryBuild);
			//阵营
			properties.register("camp", params.get(0));
			//消耗金币
			properties.register("costCoin", params.get(1));
			properties.register("num", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 扩建
	 *
	 * @param player
	 * @param params
	 */
	public void buyQue(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.buyQue);
			//阵营
			properties.register("costGold", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 国家任务
	 *
	 * @param player
	 * @param params
	 */
	public void countryTask(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.countryTask);
			//阵营
			properties.register("task", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 

		});
	}

	/**
	 * 阵营邮件
	 *
	 * @param player
	 * @param params
	 */
	public void countryMail(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.countryMail);
			properties.register("costGold", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}


	/**
	 * 1小时加速
	 *
	 * @param player
	 * @param params
	 */
	public void useSpeed(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.useSpeed);
			properties.register("desc", params.get(0));
			//分
			properties.register("speed", params.get(1));
			properties.register("costGold", params.get(2));
			properties.register("costProp", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 兵营扩容
	 *
	 * @param player
	 * @param params
	 */
	public void dilatation(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.dilatation);
			properties.register("barracks", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 商用建造队列
	 *
	 * @param player
	 * @param params
	 */
	public void buildTeam(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.buildTeam);
			properties.register("cost", params.get(0));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 城市保护
	 *
	 * @param player
	 * @param params
	 */
	public void cityProtect(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.cityProtect);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 加速
	 *
	 * @param player
	 * @param params
	 */
	public void quicken(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.quicken);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});

	}

	/**
	 * 练兵加速
	 *
	 * @param player
	 * @param params
	 */
	public void soldierQuicken(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.soldierQuicken);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 攻击提升
	 *
	 * @param player
	 * @param params
	 */
	public void attackUp(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.attackUp);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 防御提升
	 *
	 * @param player
	 * @param params
	 */
	public void defenceUp(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.defenceUp);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 部队重建
	 *
	 * @param player
	 * @param params
	 */
	public void reBuild(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.reBuild);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	public void smallQuicken(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.smallQuicken);
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}


	/**
	 * 勋章合成
	 *
	 * @param player
	 * @param params
	 */
	public void omamentCompound(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.omamentCompound);
			properties.register("omamentType", params.get(0));
			properties.register("omamentID", params.get(1));
			properties.register("omamentName", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 材料生产
	 *
	 * @param player
	 * @param params
	 */
	public void makeProp(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.makeProp);
			properties.register("costProp", params.get(0));
			properties.register("makeProp", params.get(1));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 美女玩牌
	 *
	 * @param player
	 * @param params
	 */
	public void beautyPlayGame(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.beautyPlayGame);
			properties.register("beautyId", params.get(0));
			properties.register("addIntimacyValue", params.get(1));
			properties.register("intimacyValue", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 赠送礼物
	 *
	 * @param player
	 * @param params
	 */
	public void beautyGift(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.beautyGift);
			properties.register("beautyId", params.get(0));
			properties.register("addIntimacyValue", params.get(1));
			properties.register("intimacyValue", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	/**
	 * 约会
	 *
	 * @param player
	 * @param params
	 */
	public void beautySeek(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.beautySeek);
			properties.register("beautyId", params.get(0));
			properties.register("addIntimacyValue", params.get(1));
			properties.register("intimacyValue", params.get(2));
			properties.register("award", params.get(3));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}

	public void beautyUp(Player player, List<Object> params) {
		logExecutor.add(() -> {
			EventProperties properties = build(player, EventName.beautyUp);
			properties.register("beautyId", params.get(0));
			properties.register("star", params.get(1));
			properties.register("cost", params.get(2));
			loggerConsumer.track(properties);
			xinkuaiManager.pushXinkuai(properties, EventType.TRACK.getType());
			 
		});
	}
}
