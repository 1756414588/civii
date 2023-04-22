package com.game.manager;

import com.game.Loading;
import com.game.constant.ActivityConst;
import com.game.constant.SimpleId;
import com.game.constant.WorldBoxState;
import com.game.constant.WorldBoxTask;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticCountryMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticWorldBoxDataMgr;
import com.game.define.LoadData;
import com.game.domain.CountryData;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.WorldBox;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticCountryTitle;
import com.game.domain.s.StaticWorldBox;
import com.game.domain.s.StaticWorldBoxCollect;
import com.game.log.LogUser;
import com.game.log.domain.WorldBoxLog;
import com.game.pb.WorldBoxPb;
import com.game.util.RandomHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cpz
 * @date 2021/1/10 0:06
 * @description
 */
@Component
@LoadData(name = "世界宝箱", type = Loading.LOAD_USER_DB)
public class WorldBoxManager extends BaseManager {

	@Autowired
	private StaticWorldBoxDataMgr worldBoxDataMgr;
	@Autowired
	private StaticCountryMgr staticCountryMgr;
	@Autowired
	private StaticLimitMgr limitMgr;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private CountryManager countryManager;
	@Autowired
	private StaticActivityMgr staticActivityMgr;

	public Map<WorldBoxTask, WorldBoxTaskAction> taskMap = new HashMap<>();

	@Override
	public void load() throws Exception {
		taskMap.put(WorldBoxTask.KILL_MONSTER, this::killMonster);
		taskMap.put(WorldBoxTask.BUILD_COUNTRY, this::buildCountry);
		taskMap.put(WorldBoxTask.CITY_FIGHT, this::cityFight);
		taskMap.put(WorldBoxTask.COUNTRY_FIGHT, this::countryFight);
		taskMap.put(WorldBoxTask.DO_RECHARGE, this::doRecharge);
		taskMap.put(WorldBoxTask.CAMP_SYNERGY, this::campSynergy);
		taskMap.put(WorldBoxTask.WORLD_TASK, this::worldTask);
	}

	@Override
	public void init() throws Exception {
	}

	/**
	 * 根据玩家军衔随机箱子
	 *
	 * @param player
	 * @return
	 */
	public WorldBox randomBox(Player player) {
		int num = player.getLord().getWordBoxNum();
		List<Integer> firstTimes = limitMgr.getAddtion(SimpleId.WORLD_BOX_THREE_TIMES);
		if (num < firstTimes.size()) {
			int boxId = firstTimes.get(num);
			StaticWorldBox staticWorldBox = worldBoxDataMgr.getStaticWorldBox(boxId);
			if (staticWorldBox == null) {
				LoggerFactory.getLogger(getClass()).error("randowm box error ->[{}]", boxId);
			} else {
				return WorldBox.builder()
					.state(WorldBoxState.WAIT)
					.boxId(staticWorldBox.getBoxId())
					.openTime(TimeHelper.getCurrentSecond() + staticWorldBox.getOpenTime()).build();
			}
		}

		StaticCountryTitle staticCountryTitle = staticCountryMgr.getCountryTitle(player.getTitle());
		if (staticCountryTitle == null) {
			LoggerFactory.getLogger(getClass()).error("random box title error->[{}]", player.getTitle());
			return null;
		}
		List<List<Integer>> result = new ArrayList<>();
		List<List<Integer>> randoms = staticCountryTitle.getWorldBoxDrop();
		int totalWeight = 0;
		for (List<Integer> items : randoms) {
			result.add(items);
			totalWeight += items.get(3);
		}
		int random = RandomHelper.randomInSize(totalWeight);
		int total = 0;
		for (List<Integer> items : randoms) {
			total += items.get(3);
			if (total >= random) {
				StaticWorldBox staticWorldBox = worldBoxDataMgr.getStaticWorldBox(items.get(1));
				if (staticWorldBox == null) {
					LoggerFactory.getLogger(getClass()).error("randowm box error ->[{}]", items.get(1));
				} else {
					return WorldBox.builder()
						.state(WorldBoxState.WAIT)
						.boxId(staticWorldBox.getBoxId())
						.openTime(TimeHelper.getCurrentSecond() + staticWorldBox.getOpenTime()).build();
				}
			}
		}
		return null;
	}

	@Getter
	public static class WorldBoxItem {

		int weight;
		int index;
		List<List<Integer>> award;

		public WorldBoxItem(int weight, int index, List<List<Integer>> award) {
			this.weight = weight;
			this.index = index;
			this.award = award;
		}
	}

	/**
	 * 世界宝箱开启
	 *
	 * @param worldBox
	 * @return
	 */
	public List<Award> openWorldBox(WorldBox worldBox) {
		StaticWorldBox staticWorldBox = worldBoxDataMgr.getStaticWorldBox(worldBox.getBoxId());
		List<Award> list = new ArrayList<>();
		//基础奖励
		staticWorldBox.getStapleAward().forEach(e -> {
			list.add(new Award(e.get(0), e.get(1), e.get(2)));
		});
		//随机奖励数量
		int num = randomAwardNum(staticWorldBox.getRandomAwardNum());
		List<List<List<Integer>>> awards = staticWorldBox.getRandomAward();
		int total = 0;
		List<WorldBoxItem> worldBoxItems = new ArrayList<>();
		for (int index = 0; index < awards.size(); index++) {
			List<List<Integer>> items = awards.get(index);
			int weight = items.stream().mapToInt(e -> e.get(3)).sum();
			total += weight;
			worldBoxItems.add(new WorldBoxItem(weight, index, items));
		}
		for (int i = 0; i < num; i++) {
			WorldBoxItem item = randomWorldBoxItem(worldBoxItems, total);
			if (item != null) {
				total -= item.getWeight();
				//随机奖励
				Award award = randomAward(item.getAward(), item.getWeight());
				list.add(award);
			}
		}
		return list;
	}

	private WorldBoxItem randomWorldBoxItem(List<WorldBoxItem> worldBoxItems, int total) {
		int ran = RandomHelper.randomInSize(total);
		int t = 0;
		for (WorldBoxItem item : worldBoxItems) {
			t += item.getWeight();
			if (t >= ran) {
				worldBoxItems.remove(item);
				return item;
			}
		}
		return null;
	}

	private Award randomAward(List<List<Integer>> list, int total) {
		int ran = RandomHelper.randomInSize(total);
		int t = 0;
		for (List<Integer> l : list) {
			t += l.get(3);
			if (t >= ran) {
				return new Award(l.get(0), l.get(1), l.get(2));
			}
		}
		return null;
	}

	private int randomAwardNum(List<List<Integer>> numlist) {
		int total = 0;
		for (List<Integer> l : numlist) {
			total += l.get(1);
		}
		int ran = RandomHelper.randomInSize(total);
		total = 0;
		for (List<Integer> l : numlist) {
			total += l.get(1);
			if (total >= ran) {
				return l.get(0);
			}
		}
		return 1;
	}

	/**
	 * 杀敌计算贡献值
	 *
	 * @param task
	 * @param player
	 * @param val
	 */
	public void calcuPoints(WorldBoxTask task, Player player, int val) {
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_WORLD_BOX);
		if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			return;
		}
		taskMap.get(task).action(player, val);
	}

	/**
	 * 添加积分
	 *
	 * @param player
	 * @param points
	 */
	private void addPoints(Player player, int points, WorldBoxTask task) {
		int exchangePoint = limitMgr.getNum(SimpleId.WORLD_BOX_DAY_POINT);
		int num = limitMgr.getNum(SimpleId.WORLD_BOX_TOTAL_POINT);
		if (player.getPWorldBox().getTodayPoints() >= exchangePoint || player.getPWorldBox().getPoints() >= num) {
			return;
		}
		int afterPoint = player.getPWorldBox().getTodayPoints() + points;
		if (afterPoint > exchangePoint) {
			points = exchangePoint - player.getPWorldBox().getTodayPoints();
		}
		afterPoint = player.getPWorldBox().getPoints() + points;
		if (afterPoint > num) {
			points = num - player.getPWorldBox().getPoints();
		}

		player.getPWorldBox().addPoints(points);
		//推送下积分变更
		WorldBoxPb.SynWorldBoxPointRs.Builder builder = WorldBoxPb.SynWorldBoxPointRs.newBuilder();
		builder.setPoint(player.getPWorldBox().getPoints());
		builder.setTodayPoints(player.getPWorldBox().getTodayPoints());
		SynHelper.synMsgToPlayer(player, WorldBoxPb.SynWorldBoxPointRs.EXT_FIELD_NUMBER, WorldBoxPb.SynWorldBoxPointRs.ext, builder.build());
		SpringUtil.getBean(LogUser.class).world_box_log(WorldBoxLog.builder()
			.lordId(player.roleId)
			.nick(player.getNick())
			.level(player.getLevel())
			.vip(player.getVip())
			.count(points)
			.reason(task.getVal())
			.cur(player.getPWorldBox().getPoints())
			.num(0)
			.build());
	}

	private interface WorldBoxTaskAction {

		void action(Player player, int val);
	}

	private void killMonster(Player player, int val) {
		StaticWorldBoxCollect staticWorldBoxCollect = worldBoxDataMgr.getStaticWorldBoxCollect(WorldBoxTask.KILL_MONSTER.getVal());
		int point = staticWorldBoxCollect.getMark().get(0).get(0);
		addPoints(player, point, WorldBoxTask.KILL_MONSTER);
	}

	private void buildCountry(Player player, int val) {
		StaticWorldBoxCollect staticWorldBoxCollect = worldBoxDataMgr.getStaticWorldBoxCollect(WorldBoxTask.BUILD_COUNTRY.getVal());
		int point = staticWorldBoxCollect.getMark().get(0).get(0);
		addPoints(player, point, WorldBoxTask.BUILD_COUNTRY);
	}

	private void cityFight(Player player, int val) {
		StaticWorldBoxCollect staticWorldBoxCollect = worldBoxDataMgr.getStaticWorldBoxCollect(WorldBoxTask.CITY_FIGHT.getVal());
		int point = staticWorldBoxCollect.getMark().get(0).get(0);
		addPoints(player, point, WorldBoxTask.CITY_FIGHT);
	}

	private void countryFight(Player player, int val) {
		StaticWorldBoxCollect staticWorldBoxCollect = worldBoxDataMgr.getStaticWorldBoxCollect(WorldBoxTask.COUNTRY_FIGHT.getVal());
		int point = staticWorldBoxCollect.getMark().get(0).get(0);
		addPoints(player, point, WorldBoxTask.COUNTRY_FIGHT);
	}

	/**
	 * 需要处理下当日首充
	 *
	 * @param player
	 * @param val
	 */
	private void doRecharge(Player player, int val) {
		StaticWorldBoxCollect staticWorldBoxCollect = worldBoxDataMgr.getStaticWorldBoxCollect(WorldBoxTask.DO_RECHARGE.getVal());
		int point = val * staticWorldBoxCollect.getMark().get(1).get(1);
		if (player.getLord().getDayRecharge() == 0) {
			int firstPoint = staticWorldBoxCollect.getMark().get(0).get(1);
			point += firstPoint;
		}
		addPoints(player, point, WorldBoxTask.DO_RECHARGE);
	}

	private void campSynergy(Player player, int val) {
		int country = player.getCountry();
		CountryData countryData = countryManager.getCountry(player.getCountry());
		int countryLv = countryData.getLevel();
		StaticWorldBoxCollect staticWorldBoxCollect = worldBoxDataMgr.getStaticWorldBoxCollect(WorldBoxTask.CAMP_SYNERGY.getVal());
		int point = 0;
		for (List<Integer> l : staticWorldBoxCollect.getMark()) {
			int lv = l.get(0);
			if (lv == countryLv) {
				point = l.get(1);
				break;
			}
		}
		final int points = point;
		playerManager.getAllPlayer().values().parallelStream().filter(e -> e.getCountry() == country).forEach(e -> {
			if (e.roleId != player.roleId) {
				addPoints(e, points, WorldBoxTask.CAMP_SYNERGY);
			}
		});
	}

	private void worldTask(Player player, int val) {
		StaticWorldBoxCollect staticWorldBoxCollect = worldBoxDataMgr.getStaticWorldBoxCollect(WorldBoxTask.WORLD_TASK.getVal());
		int point = staticWorldBoxCollect.getMark().get(0).get(0);
		playerManager.getAllPlayer().values().forEach(e -> {
			addPoints(e, point, WorldBoxTask.WORLD_TASK);
		});
	}

	public long getOpenTime(WorldBox worldBox) {
		switch (worldBox.getState()) {
			default:
				return worldBox.getOpenTime();
			case WAIT:
				StaticWorldBox staticWorldBox = worldBoxDataMgr.getStaticWorldBox(worldBox.getBoxId());
				return TimeHelper.getCurrentSecond() + staticWorldBox.getOpenTime() - worldBox.getReadyTime();
		}
	}
}
