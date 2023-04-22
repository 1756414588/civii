package com.game.manager;

import com.game.activity.ActivityEventManager;
import com.game.activity.define.EventEnum;
import com.game.constant.ActivityConst;
import com.game.constant.ChatShowType;
import com.game.constant.FriendType;
import com.game.constant.OpenConsts;
import com.game.constant.Reason;
import com.game.constant.SuripriseId;
import com.game.constant.TaskType;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticLordDataMgr;
import com.game.dataMgr.StaticOpenManger;
import com.game.dataMgr.StaticVipMgr;
import com.game.domain.Player;
import com.game.domain.Award;
import com.game.domain.p.Friend;
import com.game.domain.p.LevelAward;
import com.game.domain.p.LogicException;
import com.game.domain.p.Lord;
import com.game.domain.s.StaticLordLv;
import com.game.domain.s.StaticOpen;
import com.game.domain.s.StaticVip;
import com.game.log.consumer.EventManager;
import com.game.log.consumer.EventName;
import com.game.log.domain.EnergyLog;
import com.game.log.domain.RoleExpLog;
import com.game.log.domain.VipExpLog;
import com.game.pb.CommonPb;
import com.game.pb.DailyTaskPb;
import com.game.pb.RolePb;
import com.game.server.exec.LoginExecutor;
import com.game.service.ActivityService;
import com.game.util.LogHelper;
import com.game.spring.SpringUtil;
import com.game.util.SynHelper;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LordManager {

	@Autowired
	private StaticLordDataMgr staticLordDataMgr;

	@Autowired
	private RankManager rankManager;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private StaticLimitMgr staticLimitMgr;

	@Autowired
	private StaticVipMgr staticVipMgr;

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private StaticActivityMgr staticActivityMgr;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private BuildingManager buildingManager;

	@Autowired
	private OmamentManager omamentManager;
	@Autowired
	private SurpriseGiftManager surpriseGiftManager;
	@Autowired
	private PersonalityManager personalityManager;
	@Autowired
	private StaticOpenManger staticOpenManger;

	/**
	 * Function:主角增加经验
	 */
	public LevelAward addExp(Player player, int exp, int reason) {
		Lord lord = player.getLord();
		if (exp <= 0) {
			LogHelper.CONFIG_LOGGER.trace("exp is less or equal zero, reason = " + reason);
			return null;
		}

		int maxLevel = staticLordDataMgr.maxLevel();
		int evertLvExp;
		int beforeLevel = lord.getLevel();
		int levelIndex = lord.getLevel() + 1;
		int totalExp = lord.getExp() + exp;
		for (; levelIndex <= maxLevel; levelIndex++) {
			evertLvExp = staticLordDataMgr.getExp(levelIndex);
			if (totalExp < evertLvExp) {
				break;
			}

			totalExp -= evertLvExp;
			lord.setLevel(levelIndex);
			//刷新成长基金红点
			SpringUtil.getBean(ActivityManager.class).updateActInvestRq(player);
			lord.setLvUpTime(System.currentTimeMillis());
			//TODO  jyb玩家升级解锁建筑
			buildingManager.synBuildingsByLv(player);
			/**
			 * 每日活跃开启推送
			 **/
			SpringUtil.getBean(LoginExecutor.class).add(() -> {
				StaticOpen open = staticOpenManger.getOpen(OpenConsts.OPEN_69);
				if (open != null && player.getLevel() == open.getCondition()) {
					DailyTaskPb.SynDailyTaskRq builder = DailyTaskPb.SynDailyTaskRq.newBuilder().setComplate(taskManager.getComplateTask(player)).build();
					SynHelper.synMsgToPlayer(player, DailyTaskPb.SynDailyTaskRq.EXT_FIELD_NUMBER, DailyTaskPb.SynDailyTaskRq.ext, builder);
				}
			});
		}

		if (totalExp > 0) {
			lord.setExp(totalExp);
		}
		SpringUtil.getBean(EventManager.class).role_get_exp(player, Lists.newArrayList(exp, reason
		));
//		if (lord.getLevel() >= maxLevel && lord.getExp() > 0) {
//			lord.setExp(0);
//		}
		LevelAward levelAward = null;
		// 等级变化触发任务
		if (beforeLevel != lord.getLevel()) {
			doLordLevelUp(player, lord.getLevel());
			levelAward = doLevelAwards(player, beforeLevel, reason);
			//LogHelper.logLevelUp(player);
			rankManager.checkRankList(player.getLord()); // 检查排行榜
			activityManager.updActPersonRank(player, ActivityConst.ACT_LEVEL_RANK, 50, lord.getLevel(), 0);
			//TODO  jyb玩家升级解锁建筑
			buildingManager.synBuildingsByLv(player);

			Map<Long, Friend> friendMap = player.getFriends().get(FriendType.MASTER);
			if (friendMap != null) {
				Iterator<Long> it = friendMap.keySet().iterator();
				if (friendMap.size() == 1 && it.hasNext()) {
					Player mentor = playerManager.getPlayer(it.next().longValue());
					if (mentor != null) {
						//导师排行榜记录值更新
						activityManager.updActMentorScore(mentor);
					}
				}
			}
			//等级触发配饰槽位解锁
			omamentManager.openPosOmament(player);
			SpringUtil.getBean(EventManager.class).level_up(player, lord.getLevel() - beforeLevel);
			surpriseGiftManager.doSurpriseGift(player, SuripriseId.Level, player.getLevel(), true);

			ActivityEventManager.getInst().activityTip(EventEnum.UP_LEVEL, player, 0, 0);
		}

		// LogHelper.GAME_DEBUG.error("玩家=" + player.getNick() + ", 获得经验值=" +
		// exp + ",原因=" + reason);
		/**
		 * 玩家通过任务奖励获得的经验值日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.roleExpLog(RoleExpLog.builder()
			.channel(player.account.getChannel())
			.commandLevel(player.getCommandLv())
			.increaseExp(exp)
			.country(player.getCountry())
			.energy(player.getEnergy())
			.exp(player.getExp())
			.reason(reason)
			.roleCreateTime(player.account.getCreateDate())
			.roleId(player.roleId)
			.rolelv(player.getLevel())
			.roleName(player.getNick())
			.techLevel(player.getTechLv())
			.title(player.getTitle())
			.vip(player.getVip())
			.build());

		return levelAward;
	}

	// do level awards
	public LevelAward doLevelAwards(Player player, int beforeLevel, int reason) {
		int level = player.getLevel();
		Map<Integer, LevelAward> levelAwards = player.getLevelAwardsMap();
		int index = 0;
		for (int i = beforeLevel; i <= level; i++) {
			StaticLordLv staticLordLv = staticLordDataMgr.getStaticLordLv(i);
			if (staticLordLv == null) {
				LogHelper.CONFIG_LOGGER.error("staticLordLv config is null, level =" + i);
				continue;
			}
			List<List<Integer>> awards = staticLordLv.getAwards();
			if (awards == null || awards.isEmpty() || levelAwards.containsKey(i)) {
				continue;
			}
			List<Award> awardList = getLevelAward(awards);
			LevelAward levelAward = new LevelAward();
			levelAward.setLevel(i);
			levelAward.setAwards(awardList);
			levelAward.setStatus(0);
			levelAwards.put(i, levelAward);
			if (index == 0 || i < index) {
				index = i;
			}
		}
		//如果是完成任务升级的
		if (reason == Reason.TASK_AWARD) {
			LevelAward levelAward = levelAwards.get(index);
			if (levelAward != null) {
				return levelAward;
			}
			levelAward = new LevelAward();
			levelAward.setLevel(level);
			return levelAward;
		}
		if (index > 0) {
			return this.synLevelupAwards(player, index);
		}
		return null;
	}

	// 同步升级礼包
	public LevelAward synLevelupAwards(Player player, int index) {
		LevelAward levelAward = player.getLevelAwardsMap().get(index);
		if (levelAward.getStatus() != 0) {
			return null;
		}
		RolePb.SynLevelupAwardsRq.Builder builder = RolePb.SynLevelupAwardsRq.newBuilder();
		List<CommonPb.LevelupAwards> rt = new ArrayList<CommonPb.LevelupAwards>();
		rt.add(levelAward.wrapPb());
		builder.addAllAwards(rt);
		StaticLordLv staticLordLv = staticLordDataMgr.getStaticLordLv(index);
		if (staticLordLv != null) {
			int nextLv = staticLordLv.getNextLv();
			LevelAward levelAward1 = player.getLevelAwardsMap().get(nextLv);
			if (levelAward1 != null) {
				builder.setLevel(levelAward1.getLevel());
			}
		}
		SynHelper.synMsgToPlayer(player, RolePb.SynLevelupAwardsRq.EXT_FIELD_NUMBER, RolePb.SynLevelupAwardsRq.ext, builder.build());
		return levelAward;
	}

	public List<Award> getLevelAward(List<List<Integer>> awards) {
		List<Award> res = new ArrayList<Award>();
		for (List<Integer> award : awards) {
			if (award == null || award.size() != 3) {
				continue;
			}
			Award elem = new Award(award);
			res.add(elem);
		}
		return res;
	}

	/**
	 * Function:主角扣除体力
	 */
	public boolean subEnergy(Lord lord, int energy, int reason) {
		if (energy <= 0) {
			LogHelper.CONFIG_LOGGER.trace("energy is less or equal zero.");
			return false;
		}

		lord.setEnergy(lord.getEnergy() - energy);
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.energyLog(EnergyLog.builder()
			.lordId(lord.getLordId())
			.cost(-energy)
			.lv(lord.getLevel())
			.nick(lord.getNick())
			.reason(reason).build());
		return true;
	}

	/**
	 * Function:主角扣除体力
	 */
	public boolean addEnergy(Lord lord, int energy, int reason) {
		if (energy <= 0) {
			LogHelper.CONFIG_LOGGER.trace("energy is less or equal zero.");
			return false;
		}

		lord.setEnergy(lord.getEnergy() + energy);
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.energyLog(EnergyLog.builder()
			.lordId(lord.getLordId())
			.cost(energy)
			.lv(lord.getLevel())
			.nick(lord.getNick())
			.reason(reason).build());
		return true;
	}

	/**
	 * Function:主角增加威望
	 */
	public boolean addHonor(Lord lord, long honor, int reason) {
		if (honor <= 0) {
			return false;
		}
		lord.setHonor(lord.getHonor() + honor);

		return true;
	}

	/**
	 * Function:减少增加威望
	 */
	public boolean subHonor(Lord lord, long honor, int reason) {
		if (honor <= 0) {
			return false;
		}

		long res = lord.getHonor() - honor;
		res = Math.max(0, res);

		lord.setHonor(res);
		return true;
	}

	/**
	 * Function:主角增加英雄洗练次数
	 */
	public boolean addHeroWashTimes(Lord lord, int heroWashTimes, int reason) {
		if (heroWashTimes <= 0) {
			return false;
		}

		// 增加上限
		int washTimes = lord.getWashHeroTimes() + heroWashTimes;
		washTimes = Math.min(staticLimitMgr.getMaxWashHero(), washTimes);
		lord.setWashHeroTimes(washTimes);
		return true;
	}

	/**
	 * Function:主角增加装备高级技能洗练次数
	 */
	public boolean addExpertSkillWashTimes(Lord lord, int expertSkillWashTimes, int reason) {
		if (expertSkillWashTimes <= 0) {
			return false;
		}

		// 增加上限
		int times = lord.getExpertWashSkillTimes() + expertSkillWashTimes;
		lord.setExpertWashSkillTimes(times);
		return true;
	}

	/**
	 * Function:主角增加装备技能洗练次数
	 */
	public boolean addSkillWashTimes(Lord lord, int skillWashTimes, int reason) {
		if (skillWashTimes <= 0) {
			return false;
		}

		// 增加上限
		int washTimes = lord.getWashSkillTimes() + skillWashTimes;
		washTimes = Math.min(staticLimitMgr.getMaxWashEquip(), washTimes);

		lord.setWashSkillTimes(washTimes);
		return true;
	}

	/**
	 * Function:触发升级任务
	 */
	public void doLordLevelUp(Player player, int lordLv) {
		List<Integer> triggers = new ArrayList<Integer>();
		triggers.add(lordLv);
		taskManager.doTask(TaskType.LORD_LEVEL_UP, player, triggers);
	}

	/**
	 * Function:主角增加英雄洗练次数
	 */
	public boolean subHeroWashTimes(Lord lord, int heroWashTimes, int reason) {
		if (heroWashTimes <= 0) {
			return false;
		}

		// 增加上限
		int washTimes = lord.getWashHeroTimes() - heroWashTimes;
		if (washTimes < 0) {
			try {
				throw new LogicException("washTimes < 0");
			} catch (LogicException e) {
				e.printStackTrace();
			}
		}
		washTimes = Math.max(0, washTimes);
		lord.setWashHeroTimes(washTimes);
		return true;
	}

	/**
	 * Function:主角增加装备技能洗练次数
	 */
	public boolean subSkillWashTimes(Lord lord, int skillWashTimes, int reason) {
		if (skillWashTimes <= 0) {
			return false;
		}

		// 增加上限
		int washTimes = lord.getWashSkillTimes() - skillWashTimes;
		if (washTimes < 0) {
			try {
				throw new LogicException("washTimes < 0");
			} catch (LogicException e) {
				e.printStackTrace();
			}
		}
		washTimes = Math.max(0, washTimes);

		lord.setWashSkillTimes(washTimes);
		return true;
	}

	public void addVipLevel(Lord lord, int vipLevel, int reason) {
		if (vipLevel <= 0) {
			return;
		}

		int vip = lord.getVip();
		vip += vipLevel;
		vip = Math.min(12, vip);
		vip = Math.max(0, vip);
		lord.setVip(vip);

		int minVip = staticLimitMgr.getNum(242);// 开启聊天的最低VIP等级
		if (lord.getOpenSpeak() != 1) {
			if (lord.getVip() >= minVip) {
				lord.setOpenSpeak(1);
			}
		}
		personalityManager.isPush(lord.getLordId(), vip);
	}

	public void setVipLevel(Lord lord, int vipLevel, int reason) {
		if (vipLevel <= 0) {
			return;
		}

		vipLevel = Math.min(12, vipLevel);
		vipLevel = Math.max(0, vipLevel);
		lord.setVip(vipLevel);

		int minVip = staticLimitMgr.getNum(242);// 开启聊天的最低VIP等级
		if (lord.getOpenSpeak() != 1) {
			if (lord.getVip() >= minVip) {
				lord.setOpenSpeak(1);
			}
		}
		personalityManager.isPush(lord.getLordId(), vipLevel);
	}
	@Autowired
	ChatManager chatManager;
	public boolean addVipExp(Player player, int exp, int reason, int amount, int topUp, int serverId) {
		if (exp <= 0) {
			LogHelper.CONFIG_LOGGER.trace("exp is less or equal zero.");
			return false;
		}
		Lord lord = player.getLord();
		Map<Integer, StaticVip> vipMap = staticVipMgr.getVipMap();
		int beforeVip = lord.getVip();
		int vip = lord.getVip();
		int vipExp = lord.getVipExp() + exp;

		Iterator<StaticVip> it = vipMap.values().iterator();
		while (it.hasNext()) {
			StaticVip staticVip = it.next();
			if (vipExp >= staticVip.getTopup() && vip < staticVip.getVip()) {

//                StaticVip beforeStaticVip = vipMap.get(vip);
//                lord.setsGameTimes(lord.getsGameTimes() + (staticVip.getGameTime() - beforeStaticVip.getGameTime()));
				vip = staticVip.getVip();
			}
		}

		lord.setVip(vip);
		//vip发生改变
		if (vip != beforeVip) {
			activityManager.actHighVipPush();
			for (int i = beforeVip+1; i <= vip; i++) {
				chatManager.updateChatShow(ChatShowType.VIP_LEVEL, i, player);
			}
		}
		personalityManager.isPush(lord.getLordId(), vip);
		//获取美女
		BeautyManager beautyManager = SpringUtil.getBean(BeautyManager.class);
		beautyManager.levelUpBuildingGetBeauty(player, lord.getVip(), 2);
		lord.setVipExp(vipExp);
		//非充值的全部算赠送vip
		if (reason != Reason.PAY) {
			lord.addFreeVipExp(exp);
		}
		SpringUtil.getBean(EventManager.class).record_userInfo(player, EventName.add_vip);
		SpringUtil.getBean(EventManager.class).record_userInfo_once(player, EventName.first_vip_up);
		int minVip = staticLimitMgr.getNum(242);// 开启聊天的最低VIP等级
		if (lord.getOpenSpeak() != 1) {
			if (lord.getVip() >= minVip) {
				lord.setOpenSpeak(1);
			}
		}

		/**
		 * vip升级日志埋点
		 */
		com.game.log.LogUser logUser = SpringUtil.getBean(com.game.log.LogUser.class);
		logUser.vipExpLog(new VipExpLog(
			lord.getLordId(),
			lord.getNick(),
			player.account.getChannel(),
			serverId,
			lord.getLevel(),
			lord.getVip(),
			lord.getVipExp(),
			exp,
			amount,
			topUp,
			lord.getGold(),
			player.account.getLoginDate(),
			reason == Reason.PAY));

		return true;
	}


	public boolean addSeekingTimes(Lord lord, int seekingTime, int reason) {
		if (seekingTime <= 0) {
			LogHelper.CONFIG_LOGGER.trace("seekingTime is less or equal zero.");
			return false;
		}

		lord.setSeekingTimes(lord.getSeekingTimes() + seekingTime);
		return true;
	}

	public boolean subSeekingTimes(Lord lord, int seekingTime, int reason) {
		if (seekingTime <= 0) {
			LogHelper.CONFIG_LOGGER.trace("exp is less or equal zero.");
			return false;
		}

		lord.setSeekingTimes(lord.getSeekingTimes() - seekingTime);
		return true;
	}

	public boolean addSafety(Lord lord, int safety, int reason) {
		if (safety <= 0) {
			LogHelper.CONFIG_LOGGER.trace("safety is less or equal zero.");
			return false;
		}

		lord.setSafety(lord.getSafety() + safety);
		return true;
	}

	public boolean subSafety(Lord lord, int safety, int reason) {
		if (safety <= 0) {
			LogHelper.CONFIG_LOGGER.trace("safety is less or equal zero.");
			return false;
		}

		lord.setSafety(lord.getSafety() - safety);
		return true;
	}

	public boolean addSGameTimes(Lord lord, int sGameTimes, int reason) {
		if (sGameTimes <= 0) {
			LogHelper.CONFIG_LOGGER.trace("sGameTimes is less or equal zero.");
			return false;
		}

		lord.setsGameTimes(lord.getsGameTimes() + sGameTimes);
		return true;
	}

	public boolean subSGameTimes(Lord lord, int sGameTimes, int reason) {
		if (sGameTimes <= 0) {
			LogHelper.CONFIG_LOGGER.trace("sGameTimes is less or equal zero.");
			return false;
		}

		lord.setsGameTimes(lord.getsGameTimes() - sGameTimes);
		return true;
	}

	public boolean addJourneyTime(Lord lord, int journeyTime, int reason) {
		if (journeyTime <= 0) {
			LogHelper.CONFIG_LOGGER.trace("journeyTime is less or equal zero.");
			return false;
		}

		lord.setJourneyTimes(lord.getJourneyTimes() + journeyTime);
		return true;
	}

	public boolean subJourneyTime(Lord lord, int journeyTime, int reason) {
		if (journeyTime <= 0) {
			LogHelper.CONFIG_LOGGER.trace("journeyTime is less or equal zero.");
			return false;
		}

		lord.setJourneyTimes(lord.getJourneyTimes() - journeyTime);
		return true;
	}

	public void subExpertSkillWashTimes(Lord lord, int count, int reason) {
		if (count <= 0) {
			LogHelper.CONFIG_LOGGER.trace("expertSkillWashTimes is less or equal zero.");
			return;
		}
		if (lord.getExpertWashSkillTimes() <= 0) {
			LogHelper.CONFIG_LOGGER.trace("expertSkillWashTimes is less or equal zero.");
			return;
		}

		lord.setExpertWashSkillTimes(lord.getExpertWashSkillTimes() - count);
	}
}
