package com.game.manager;

import com.game.activity.ActivityEventManager;
import com.game.activity.actor.CommonTipActor;
import com.game.activity.define.EventEnum;
import com.game.constant.ActivityConst;
import com.game.constant.SuripriseId;
import com.game.dataMgr.StaticActivityMgr;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.p.ActivityRecord;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticLimitGift;
import com.game.pb.ActivityPb;
import com.game.pb.CommonPb;
import com.game.util.LogHelper;
import com.game.util.BasePbHelper;
import com.game.util.PbHelper;
import com.game.util.RandomUtil;
import com.game.util.SynHelper;
import com.game.util.TimeHelper;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zcp
 * @date 2021/3/9 14:02 诵我真名者,永不见bug 惊喜礼包
 */
@Component
public class SurpriseGiftManager {

	@Autowired
	private StaticActivityMgr activityMgr;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private PlayerManager playerManager;
	@Autowired
	ActivityEventManager activityEventManager;
	/**
	 * @param player
	 * @param suripriseId
	 * @param cond
	 */
	public void doSurpriseGift(Player player, SuripriseId suripriseId, int cond, boolean flag) {
		ActivityBase activityBase = activityMgr.getActivityById(ActivityConst.ACT_SURIPRISE_GIFT);
		if (activityBase == null) {
			LogHelper.CONFIG_LOGGER.info("no  activity 88 config");
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		StaticLimitGift staticLimitGift = marketFlop(player, suripriseId, cond, actRecord.getAwardId());
		if (staticLimitGift != null) {
			ActivityRecord record = ActivityRecord.builder()
				.key(staticLimitGift.getKeyId())
				.expireTime(System.currentTimeMillis() + staticLimitGift.getTime() * TimeHelper.SECOND_MS)
				.build();
			actRecord.getActivityRecords().add(record);
			pushSuripriseGift(player, staticLimitGift, record, actRecord, flag);
		}
	}

	private void pushSuripriseGift(Player player, StaticLimitGift staticLimitGift, ActivityRecord record, ActRecord actRecord, boolean flag) {
		ActivityBase activityBase = activityManager.getActivityBase(actRecord.getActivityId());
		ActivityPb.SynSuripriseGiftRq.Builder rs = ActivityPb.SynSuripriseGiftRq.newBuilder();
		CommonPb.SuripriseGift.Builder builder = CommonPb.SuripriseGift.newBuilder();
		builder.setKeyId(staticLimitGift.getKeyId());
		builder.setName(staticLimitGift.getName());
		builder.setGold(staticLimitGift.getDisplay());
		builder.setMoney(staticLimitGift.getMoney());
		staticLimitGift.getAwardList().forEach(e -> {
			builder.addAward(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
		});
		builder.setCount(staticLimitGift.getCount());
		builder.setBuyCount(0);
		builder.setExpireTime(record.getExpireTime());
		builder.setAsset(staticLimitGift.getAsset());
		builder.setIcon(staticLimitGift.getIcon());
		rs.setGifts(builder);
		rs.setTips(1);
		actRecord.getReceived().remove(ActivityConst.ACT_SURIPRISE_GIFT);
		SynHelper.synMsgToPlayer(player, ActivityPb.SynSuripriseGiftRq.EXT_FIELD_NUMBER, ActivityPb.SynSuripriseGiftRq.ext, rs.build());
		activityEventManager.activityTip(EventEnum.SYN_ACTIVITY_AND_DISAPPERAR, new CommonTipActor(player, actRecord, activityBase));
	}

	/**
	 * 查找指定礼包种类
	 *
	 * @param type
	 * @param cond
	 * @return
	 */
	public List<StaticLimitGift> findStaticLimitGift(int type, int cond, int awardId) {
		Map<Integer, StaticLimitGift> map = activityMgr.getLimitGiftByAward(awardId);
		if (map != null) {
			List<StaticLimitGift> list = Lists.newArrayList(map.values());
			return list.stream().filter(e -> {
				if (e.getLimit().get(0) == type && e.getLimit().get(1) == cond) {
					return true;
				}
				return false;
			}).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	/**
	 * 随机奖励
	 */
	public StaticLimitGift marketFlop(Player player, SuripriseId suripriseId, int cond, int awardId) {
		StaticLimitGift staticLimitGift = randomLimitGift(player, suripriseId.get(), cond, awardId);
		return staticLimitGift;
	}

	/**
	 * 随机一个奖励出来
	 *
	 * @param player
	 * @param type
	 * @param cond
	 * @return
	 */
	private StaticLimitGift randomLimitGift(Player player, int type, int cond, int awardId) {
		List<StaticLimitGift> list = findStaticLimitGift(type, cond, awardId);
		//先查VIP符合的 再查登录时间符合的
		int vip = player.getVip();
		int login = TimeHelper.equation(player.account.getCreateDate().getTime(), System.currentTimeMillis()) + 1;
		list = getStaticLimitGifts(list, vip, login);
		ActivityBase activityBase = activityMgr.getActivityById(ActivityConst.ACT_SURIPRISE_GIFT);
		if (activityBase == null) {
			LogHelper.CONFIG_LOGGER.info("no  activity 88 config");
			return null;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		List<ActivityRecord> list2 = actRecord.getActivityRecords();
		List<StaticLimitGift> result = Lists.newArrayList();
		for (StaticLimitGift gift : list) {
			//互斥
			boolean random = checkMutu(actRecord, list2, gift);
			if (random) {
				result.add(gift);
			}
		}
		if (result.size() == 0) {
			return null;
		}
		StaticLimitGift staticLimitGift = RandomUtil.getOneRandomElement(result);
		return staticLimitGift;
	}

	/**
	 * 查找符合条件的礼包
	 *
	 * @param list
	 * @param vip
	 * @param login
	 * @return
	 */
	private List<StaticLimitGift> getStaticLimitGifts(List<StaticLimitGift> list, int vip, int login) {
		list = list.stream().filter(e -> {
				int minVip = e.getTypeid().get(0);
				int maxVip = e.getTypeid().get(1);
				boolean result = vip >= minVip && vip <= maxVip;
				if (!result) {
					return false;
				}
				int minLogin = e.getTimelimit().get(0);
				int maxLogin = e.getTimelimit().get(1);
				if (maxLogin != 999) {
					boolean loginResult = login >= minLogin && login <= maxLogin;
					return loginResult;
				}
				return true;
			}
		).collect(Collectors.toList());
		return list;
	}

	/**
	 * 检测是否互斥
	 *
	 * @param list2
	 * @param gift
	 * @return
	 */
	private boolean checkMutu(ActRecord actRecord, List<ActivityRecord> list2, StaticLimitGift gift) {
		boolean random = true;
		for (ActivityRecord record : list2) {
			StaticLimitGift staticRecord = activityMgr.getLimitGiftByKeyId(record.getKey());
			//判定是否又互斥的
			if (staticRecord.getMutex() == 1 && gift.getMutex() == 1) {
				random = false;
				break;
			}
			if (gift.getKeyId() == record.getKey()) {
				random = false;
				break;
			}
			if (actRecord.getReceived().containsKey(gift.getKeyId())) {
				random = false;
				break;
			}
		}
		return random;
	}
}
