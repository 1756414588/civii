package com.game.manager;

import com.game.constant.*;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticBeautyMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.Player;
import com.game.domain.p.ActRecord;
import com.game.domain.Award;
import com.game.domain.p.BeautyData;
import com.game.domain.p.BuildingBase;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.StaticActPayGift;
import com.game.domain.s.StaticBeautyBase;
import com.game.domain.s.StaticBeautyDate;
import com.game.domain.s.StaticBeautyDateSkills;
import com.game.domain.s.StaticMailAward;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.pb.BeautyPb;
import com.game.pb.BeautyPb.GetNewBeautySkillRs;
import com.game.pb.BeautyPb.NewGetBeautyListRs;
import com.game.pb.BeautyPb.NewGetBeautyListRs.Builder;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.NewBeautyBase;
import com.game.pb.CommonPb.NewBeautySkillValues;
import com.game.pb.CommonPb.NewBeautySkills;
import com.game.service.AchievementService;
import com.game.util.LogHelper;
import com.game.util.PbHelper;
import com.game.spring.SpringUtil;
import com.game.util.StringUtil;
import com.game.util.Triple;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 2020年5月29日
 *
 *    halo_game BeautyManager.java
 **/
@Component
public class BeautyManager {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private StaticBeautyMgr staticBeautyMgr;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private PlayerManager playerManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private StaticActivityMgr staticActivityMgr;

	/**
	 * @Description 获取美女列表
	 * @Date 2021/3/30 15:50
	 * @Param [player, i]
	 * @Return
	 **/
	public BeautyPb.NewGetBeautyListRs.Builder getBeautyList(Player player) {
		Builder builder = NewGetBeautyListRs.newBuilder();
		builder.setSGameTimes(player.getLord().getsGameTimes());
		List<StaticBeautyBase> allBeautyBaseList = staticBeautyMgr.getAllBeautyBaseList();
		int beautyId = staticLimitMgr.getAddtion(SimpleId.BEAUTY_GIFT).get(0);
		boolean have = false;
		List<Triple<Integer, Integer, Integer>> beautyMaps = new ArrayList<>();
		for (StaticBeautyBase beautyBase : allBeautyBaseList) {
			if (beautyBase == null) {
				continue;
			}
			NewBeautyBase.Builder newBeautyBaseBuilder = NewBeautyBase.newBuilder();
			newBeautyBaseBuilder.setBeautyId(beautyBase.keyId);
			BeautyData beautyInfo = getBeautyInfo(player, beautyBase.getKeyId());
			if (beautyInfo == null) {
				newBeautyBaseBuilder.setState(0);
				newBeautyBaseBuilder.setIntimacyValue(0);
				newBeautyBaseBuilder.setStar(0);
				newBeautyBaseBuilder.setSeekingTimes(0);
				newBeautyBaseBuilder.setFreeSeekingEndTime(0);
				newBeautyBaseBuilder.setIsUnlock(0);
				newBeautyBaseBuilder.setClickCount(0);
				builder.addBeautyBase(newBeautyBaseBuilder);

				beautyMaps.add(new Triple<>(beautyBase.getKeyId(), 0, 0));
				continue;
			}
			// 表示美女已经获得了
			if (beautyId == beautyBase.getKeyId()) {
				have = true;
				beautyInfo.setIsUnlock(1);
			}
			newBeautyBaseBuilder.setState(1);
			newBeautyBaseBuilder.setSeekingTimes(beautyInfo.getSeekingTimes());
			newBeautyBaseBuilder.setIntimacyValue(beautyInfo.getIntimacyValue());
			newBeautyBaseBuilder.setStar(beautyInfo.getStar());
			newBeautyBaseBuilder.setFreeSeekingEndTime(beautyInfo.getFreeSeekingEndTime());
			newBeautyBaseBuilder.setIsUnlock(beautyInfo.getIsUnlock());
			newBeautyBaseBuilder.setClickCount(beautyInfo.getClickCount());
			builder.addBeautyBase(newBeautyBaseBuilder);

			beautyMaps.add(new Triple<>(beautyBase.getKeyId(), 1, beautyInfo.getIsUnlock()));
		}
		if (!have) {
			int beautyGift = staticLimitMgr.getNum(SimpleId.BEAUTY_GIFT);
			StaticActPayGift payGift = staticActivityMgr.getPayGift(beautyGift);
			if (payGift != null) {
				CommonPb.ActivityCond.Builder activityPb = CommonPb.ActivityCond.newBuilder();
				activityPb.setKeyId(payGift.getPayGiftId());
				activityPb.setCond(1);
				if (player.getLord().getFirstPay() == 2) {
					activityPb.setIsAward(1);
				} else {
					activityPb.setIsAward(0);
				}
				List<List<Integer>> awardList = payGift.getSellList();
				for (List<Integer> e : awardList) {
					if (e.size() != 3) {
						continue;
					}
					int type = e.get(0);
					int id = e.get(1);
					int count = e.get(2);
					activityPb.addAward(PbHelper.createAward(type, id, count));
				}
				activityPb.setParam(payGift.getMoney() + "");
				activityPb.setParam2(beautyId + "");
				activityPb.setDesc(StringUtil.isNullOrEmpty(payGift.getDesc()) ? "" : payGift.getDesc());
				builder.setCond(activityPb.build());
			}
		}

//		LogHelper.MESSAGE_LOGGER.info("美女 getBeautyListRq playerId:{} beautys:{}", player.getRoleId(), beautyMaps);

		return builder;
	}

	/**
	 * 获取玩家所有美女数据结构体
	 *
	 * @param player
	 * @return
	 */
	public Map<Integer, BeautyData> getBeautys(Player player) {
		Map<Integer, BeautyData> beautys = player.getBeautys();
		if (null == beautys || beautys.isEmpty()) {
			return null;
		}
		return beautys;
	}

	/**
	 * 获取单个美女的数据结构体
	 *
	 * @param player
	 * @param beautyId
	 * @return
	 */
	public BeautyData getBeautyInfo(Player player, int beautyId) {
		return player.getBeautys().get(beautyId);
	}

	/**
	 * 获得美女模型
	 *
	 * @param player
	 * @param beautyId
	 * @param reason
	 */
	public void addBeautyInfo(Player player, int beautyId, int reason) {
		Map<Integer, BeautyData> beautys = player.getBeautys();
		StaticBeautyBase staticBeautyBase = staticBeautyMgr.getStaticBeautyBase(beautyId);
		if (null == staticBeautyBase) {
			logger.error("美女配置不存在beautyId = " + beautyId);
			return;
		}
		BeautyData beautyData = beautys.get(beautyId);
		if (beautyData != null) {
			return;
		}
		beautyData = new BeautyData(staticBeautyBase);
		beautyData.setSeekingTimes(1);
		beautyData.setClickCount(0);
		beautyData.setIsUnlock(1);
		beautys.put(beautyId, beautyData);
//		LogHelper.MESSAGE_LOGGER.info("美女 addBeautyInfo playerId:{} beautyId:{} isUnLock:{}", player.getRoleId(), beautyId, beautyData.getIsUnlock());
		if (beautys.size() == 1) {
			player.getLord().setsGameTimes(staticLimitMgr.getNum(SimpleId.EVERYDAY_BEAUTY_GAME_TIMES));
			player.getLord().setFreeSGameEndTime(System.currentTimeMillis());
		}

        Map<Integer, StaticMailAward> staticMailMap = staticLimitMgr.getStaticMailMap();
        if (staticMailMap == null) {
            logger.error("StaticMailAward not existence");
            return;
        }
        for (StaticMailAward staticMailAward : staticMailMap.values()) {
            if (beautyId == staticMailAward.getBeautyId()) {
                List<Integer> additions = staticMailAward.getAward();
                if (additions != null) {
                    List<Award> awards = new ArrayList<>();
                    awards.add(new Award(additions.get(0), additions.get(1), additions.get(2)));
                    playerManager.sendAttachMail(player, awards, staticMailAward.getMailId());
                }
            }
        }
        activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.ADD_SECRETARY + beautyData.getStar(), 0, 1);
        SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.add_beauty);
        // 如果得到了尼可就不显示
        if (beautyId == BeautyId.Sufei) {

			ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_BEAUTY_GIFT);
			if (activityBase == null) {
				return;
			}

            ActRecord activityInfo = activityManager.getActivityInfo(player, ActivityConst.ACT_BEAUTY_GIFT);
            if (activityInfo == null) {
                return;
            }
            ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_BEAUTY_GIFT);
            if (actRecord == null) {
                return;
            }
            actRecord.setShow(false);
            playerManager.synActivity(player, 0, 0);
        }
		if (beautyId == BeautyId.DiNa) {
			playerManager.synUnlockingBeautyRs(player, beautyId);
		}
		achievementService.addAndUpdate(player, AchiType.AT_52,1);
    }
    @Autowired
	AchievementService achievementService;
	/**
	 * 增加亲密度
	 *
	 * @param player
	 * @param beautyId
	 * @param count
	 * @param reason
	 */
	public void addIntimacyValue(Player player, int beautyId, int count, int reason) {
		BeautyData beautyData = getBeautyInfo(player, beautyId);
		if (beautyData == null) {
			return;
		}
		if (count <= 0) {
			LogHelper.CONFIG_LOGGER.trace("exp is less or equal zero, reason = " + reason);
			return;
		}
//
//        StaticBeautyBase staticBeautyBase = staticBeautyMgr.getStaticBeautyBase(beautyId);
//        if (null == staticBeautyBase) {
//            logger.error("美女配置不存在beautyId = " + beautyId);
//            return;
//        }
//
//        List<StaticBeautyDateSkills> staticBeautDateSkills = staticBeautyMgr.getStaticBeautDateSkills(beautyId);
//        if (null == staticBeautDateSkills || staticBeautDateSkills.size() == 0) {
//            logger.error("美女配置不存在beautyId = " + beautyId);
//            return;
//        }
//        StaticBeautyDateSkills staticBeautyDateSkills = staticBeautDateSkills.get(staticBeautDateSkills.size() - 1);
//        Integer max = staticBeautyDateSkills.getNeedNum();// 亲密度的上限值
//
//        int addCount = beautyData.getIntimacyValue() + count;
//        newKillDateActivation(beautyData, beautyData.getIntimacyValue(), addCount, player);
//        if (addCount > max) {
//            beautyData.setIntimacyValue(max);
//            return;
//        }
		beautyData.setIntimacyValue(beautyData.getIntimacyValue() + count);
	}

	/**
	 * 美女升星
	 *
	 * @param player
	 * @param beautyId
	 * @param count
	 * @param reason
	 */
	public void addStarValue(Player player, int beautyId, int count, int reason) {
		BeautyData beautyData = getBeautyInfo(player, beautyId);
		if (beautyData == null) {
			return;
		}
		if (count <= 0) {
			LogHelper.CONFIG_LOGGER.trace("exp is less or equal zero, reason = " + reason);
			return;
		}

		StaticBeautyBase staticBeautyBase = staticBeautyMgr.getStaticBeautyBase(beautyId);
		if (null == staticBeautyBase) {
			logger.error("美女配置不存在beautyId = " + beautyId);
			return;
		}

		List<StaticBeautyDateSkills> staticBeautStarSkills = staticBeautyMgr.getStaticBeautStarSkills(beautyId);
		if (null == staticBeautStarSkills || staticBeautStarSkills.isEmpty()) {
			logger.error("美女配置不存在beautyId = " + beautyId);
			return;
		}
		StaticBeautyDateSkills staticBeautyDateSkill = staticBeautStarSkills.get(staticBeautStarSkills.size() - 1);
		int max = staticBeautyDateSkill.getStar();

		int addCount = beautyData.getStar() + count;
		if (addCount > max) {
			beautyData.setStar(max);
			return;
		}
		beautyData.setStar(addCount);
		SpringUtil.getBean(HeroManager.class).synBattleScoreAndHeroList(player, player.getAllHeroList());

		activityManager.updActSeven(player, ActivityConst.TYPE_ADD, ActSevenConst.ADD_SECRETARY + beautyData.getStar(), 0, 1);
	}

	/**
	 * @Description 获取美女技能
	 * @Date 2021/3/23 15:39
	 * @Param [player, beautyId, skillType]
	 * @Return
	 **/
	public GetNewBeautySkillRs.Builder getBeautySkillList(Player player, int beautyId, int skillType) {
		GetNewBeautySkillRs.Builder builder = null;
		switch (skillType) {
			case 1:
				List<StaticBeautyDateSkills> staticBeautDateSkills = staticBeautyMgr.getStaticBeautDateSkills(beautyId);
				if (null == staticBeautDateSkills || staticBeautDateSkills.isEmpty()) {
					logger.error("美女配置不存在beautyId = " + beautyId);
					return null;
				}
				builder = getBeautySkillByType(player, staticBeautDateSkills);
				break;
			case 2:
				List<StaticBeautyDateSkills> staticStartBeautDateSkills = staticBeautyMgr.getStaticBeautStarSkills(beautyId);
				if (null == staticStartBeautDateSkills || staticStartBeautDateSkills.isEmpty()) {
					logger.error("美女配置不存在beautyId = " + beautyId);
					return null;
				}
				builder = getBeautySkillByType(player, staticStartBeautDateSkills);
				break;
			default:
				return null;
		}
		return builder;
	}

	/**
	 * @Description 获取技能通用方法
	 * @Date 2021/3/23 15:39
	 * @Param [player, staticBeautDateSkills]
	 * @Return
	 **/
	public GetNewBeautySkillRs.Builder getBeautySkillByType(Player player, List<StaticBeautyDateSkills> staticBeautDateSkills) {
		GetNewBeautySkillRs.Builder builder = GetNewBeautySkillRs.newBuilder();
		if (staticBeautDateSkills.size() > 0) {
			for (StaticBeautyDateSkills skill : staticBeautDateSkills) {
				NewBeautySkills.Builder newBeautySkillBuilder = NewBeautySkills.newBuilder();
				BeautyData beautyData = getBeautyInfo(player, skill.getBeautyId());
				int intimacyValue = beautyData.getIntimacyValue();
				if (intimacyValue >= skill.getNeedNum()) {
					newBeautySkillBuilder.setSkillState(1);
				} else {
					newBeautySkillBuilder.setSkillState(0);
				}
				newBeautySkillBuilder.setSkillId(skill.getId());
				newBeautySkillBuilder.setSkillCondition(skill.getNeedNum());
				List<List<Integer>> values = skill.getValue();
				for (List<Integer> list : values) {
					NewBeautySkillValues.Builder builder1 = NewBeautySkillValues.newBuilder();
					builder1.setValueId(list.get(0));
					builder1.setValueType(list.get(1));
					builder1.setValueNum(list.get(2));
					newBeautySkillBuilder.addBeautySkill(builder1.build());
				}
				builder.addSkills(newBeautySkillBuilder.build());
			}
		}
		return builder;
	}

	/**
	 * @Description 约会获取随机奖励
	 * @Date 2021/3/29 20:26
	 * @Param [type, records]
	 * @Return
	 **/
	public List<List<Integer>> getRandomAward(int star) {
		Map<Integer, StaticBeautyDate> allBeautyDateMap = staticBeautyMgr.getAllBeautyDateMap();
		StaticBeautyDate staticBeautyDate = allBeautyDateMap.get(star);
		if (staticBeautyDate == null) {
			return null;
		}
		List<List<Integer>> dateByStarList = staticBeautyDate.getChance();
		if (dateByStarList == null) {
			return null;
		}
		int seed = 0;
		for (List<Integer> list : dateByStarList) {
			if (list.size() == 2) {
				seed += list.get(1);
			}
		}
		List<List<Integer>> awardLists = new ArrayList<>();
		awardLists.add(staticBeautyDate.getAward());
		for (int i = 0; i < staticBeautyDate.getNum(); i++) {
			int total = 0;
			int random = RandomUtils.nextInt(1, seed + 1);
			for (List<Integer> e : dateByStarList) {
				total += e.get(1);
				if (total >= random) {
					awardLists.add(e);
					break;
				}
			}
		}
		return awardLists;
	}

	/**
	 * @Description 判断美女技能是否有新的激活
	 * @Date 2021/3/30 12:49
	 * @Param [intimacyValue]
	 * @Return
	 **/
	public int newKillDateActivation(BeautyData beautyData, int beforentimacyValue, int afterIntimacyValue, Player player) {
		List<StaticBeautyDateSkills> staticBeautDateSkills = staticBeautyMgr.getStaticBeautDateSkills(beautyData.getKeyId());
		List<StaticBeautyDateSkills> befirList = new ArrayList<>();
		List<StaticBeautyDateSkills> afterList = new ArrayList<>();
		if (staticBeautDateSkills.size() > 0) {
			for (StaticBeautyDateSkills staticBeautyDateSkill : staticBeautDateSkills) {
				if (beforentimacyValue >= staticBeautyDateSkill.getNeedNum()) {
					befirList.add(staticBeautyDateSkill);
				}
				if (afterIntimacyValue >= staticBeautyDateSkill.getNeedNum()) {
					afterList.add(staticBeautyDateSkill);
				}
			}
		}
		if (afterList.size() > 0) {
			StaticBeautyDateSkills staticBeautyDateSkill = afterList.get(afterList.size() - 1);
			if (staticBeautyDateSkill != null && beautyData.getKillId() != staticBeautyDateSkill.getId()) {
				beautyData.setKillId(staticBeautyDateSkill.getId());
				SpringUtil.getBean(HeroManager.class).synBattleScoreAndHeroList(player, player.getAllHeroList());
			}
		}
		return afterList.size() - befirList.size() == 0 ? 0 : 1;
	}

	/**
	 * @Description 获取美女技能加成
	 * @Date 2021/4/4 14:46
	 * @Param [player, type:技能type, id:加成类型]
	 * @Return
	 **/
	public int getBeautySkillEffect(Player player, int type, int id) {
		int buff = 0;
		Map<Integer, BeautyData> beautys = player.getBeautys();
		if (beautys == null || beautys.size() == 0) {
			return buff;
		}
		for (BeautyData beautyData : beautys.values()) {
			int killId = beautyData.getKillId();
			int star = beautyData.getStar();
			StaticBeautyDateSkills staticDateSkills = staticBeautyMgr.getStaticDateSkills(killId);
			StaticBeautyDateSkills staticStarSkills = staticBeautyMgr.getStaticStarSkills(beautyData.getKeyId(), star);
			if (staticDateSkills != null) {
				// 亲密度技能
				List<List<Integer>> staticDateSkillList = staticDateSkills.getValue();
				for (List<Integer> list : staticDateSkillList) {
					if (list.size() == 3 && list.get(0) == type && list.get(1) == id) {
						buff += list.get(2);
					}
				}
			}
			if (staticStarSkills != null) {
				// 星级技能
				List<List<Integer>> staticstarSkillList = staticStarSkills.getValue();
				for (List<Integer> list : staticstarSkillList) {
					if (list.size() == 3 && list.get(0) == type && list.get(1) == id) {
						buff += list.get(2);
					}
				}
			}
		}
		return buff;
	}

	/**
	 * @Description 增加所有美女约会次数
	 * @Date 2021/4/1 15:56
	 * @Param [player, count, reason]
	 * @Return
	 **/
	public boolean addSeekingTimes(Player player, int count, int reason) {
		if (count <= 0) {
			logger.debug("addSeekingTimes count  error  count={}", count);
			return false;
		}
		Map<Integer, BeautyData> beautys = player.getBeautys();
		if (beautys.isEmpty()) {
			return false;
		}
		for (BeautyData beautyData : beautys.values()) {
			beautyData.setSeekingTimes(count);
			beautyData.setFreeSeekingEndTime(System.currentTimeMillis());
		}
		return true;
	}

	/**
	 * @Description 任务获取美女
	 * @Date 2021/4/10 10:20
	 * @Param []
	 * @Return
	 **/// type 1:建筑 2:vip limit:限制条件
	public void levelUpBuildingGetBeauty(Player player, int limit, int type) {
		if (type == 1) {
			// 升级建筑获取美女
			BuildingBase building = player.getBuilding(limit);
			if (building == null) {
				return;
			}
			switch (limit) {
				case BuildingId.RESEARCH_COLLEGE:
					if (building.getLevel() >= 4 && getBeautyInfo(player, BeautyId.Maria) == null) {
						addBeautyInfo(player, BeautyId.Maria, 0);
					}
					break;
				case BuildingId.COMMAND:
					if (building.getLevel() >= 9 && getBeautyInfo(player, BeautyId.Huier) == null) {
						addBeautyInfo(player, BeautyId.Huier, 0);
					}
					if (building.getLevel() >= 13 && getBeautyInfo(player, BeautyId.Limei) == null) {
						addBeautyInfo(player, BeautyId.Limei, 0);
					}
					break;
			}
		} else if (type == 2 && limit >= 4 && getBeautyInfo(player, BeautyId.Lisa) == null) {
			// vip获取美女
			addBeautyInfo(player, BeautyId.Lisa, 0);
		}
	}

}
