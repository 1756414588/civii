package com.game.service;

import com.game.activity.ActivityEventManager;
import com.game.activity.actor.CommonTipActor;
import com.game.activity.actor.RankActor;
import com.game.activity.actor.TdActor;
import com.game.activity.define.EventEnum;
import com.game.constant.ActMonthlyCard;
import com.game.constant.ActWorldBattleConst;
import com.game.constant.ActivityConst;
import com.game.constant.AwardType;
import com.game.constant.BeautyId;
import com.game.constant.ChatId;
import com.game.constant.DailyTaskId;
import com.game.constant.GameError;
import com.game.constant.ItemId;
import com.game.constant.MailId;
import com.game.constant.Reason;
import com.game.constant.ResourceType;
import com.game.constant.SimpleId;
import com.game.constant.SpringType;
import com.game.constant.SuripriseId;
import com.game.constant.TaskType;
import com.game.constant.WorldActPlanConsts;
import com.game.constant.WorldActivityConsts;
import com.game.dataMgr.StaticActivityMgr;
import com.game.dataMgr.StaticEquipDataMgr;
import com.game.dataMgr.StaticLimitMgr;
import com.game.dataMgr.StaticPropMgr;
import com.game.domain.ActivityData;
import com.game.domain.CountryData;
import com.game.domain.Player;
import com.game.domain.WorldData;
import com.game.domain.p.ActPassPortTask;
import com.game.domain.p.ActPlayerRank;
import com.game.domain.p.ActRecord;
import com.game.domain.p.ActShopProp;
import com.game.domain.p.ActTDSevenType;
import com.game.domain.p.ActivityRecord;
import com.game.domain.Award;
import com.game.domain.p.CampMembersRank;
import com.game.domain.p.Equip;
import com.game.domain.p.Item;
import com.game.domain.p.Lord;
import com.game.domain.p.LuckPoolRewardRecord;
import com.game.domain.p.SimpleData;
import com.game.domain.p.WorldActPlan;
import com.game.domain.s.ActivityBase;
import com.game.domain.s.DialEntity;
import com.game.domain.s.StaticActAward;
import com.game.domain.s.StaticActCommand;
import com.game.domain.s.StaticActDial;
import com.game.domain.s.StaticActDialLuck;
import com.game.domain.s.StaticActDialPurp;
import com.game.domain.s.StaticActDialStone;
import com.game.domain.s.StaticActEquipUpdate;
import com.game.domain.s.StaticActExchange;
import com.game.domain.s.StaticActFirstPay;
import com.game.domain.s.StaticActFoot;
import com.game.domain.s.StaticActFreeBuy;
import com.game.domain.s.StaticActHope;
import com.game.domain.s.StaticActPayArms;
import com.game.domain.s.StaticActPayCard;
import com.game.domain.s.StaticActPayGift;
import com.game.domain.s.StaticActPayMoney;
import com.game.domain.s.StaticActQuota;
import com.game.domain.s.StaticActRankDisplay;
import com.game.domain.s.StaticActRedDial;
import com.game.domain.s.StaticActSeven;
import com.game.domain.s.StaticActShop;
import com.game.domain.s.StaticActSpringFestival;
import com.game.domain.s.StaticActTask;
import com.game.domain.s.StaticActivity;
import com.game.domain.s.StaticActivityChrismas;
import com.game.domain.s.StaticActivityChrismasAward;
import com.game.domain.s.StaticDailyCheckin;
import com.game.domain.s.StaticDialAwards;
import com.game.domain.s.StaticDialCost;
import com.game.domain.s.StaticEquip;
import com.game.domain.s.StaticExchangeHero;
import com.game.domain.s.StaticExchangeItem;
import com.game.domain.s.StaticHeroTask;
import com.game.domain.s.StaticLimitGift;
import com.game.domain.s.StaticMaterialSubstituteVip;
import com.game.domain.s.StaticMyExchange;
import com.game.domain.s.StaticPassPortAward;
import com.game.domain.s.StaticPassPortTask;
import com.game.domain.s.StaticPayPassPort;
import com.game.domain.s.StaticProp;
import com.game.domain.s.StaticTDSevenBoxAward;
import com.game.domain.s.StaticTDSevenTask;
import com.game.log.LogUser;
import com.game.log.constant.CopperOperateType;
import com.game.log.constant.IronOperateType;
import com.game.log.constant.OilOperateType;
import com.game.log.constant.ResOperateType;
import com.game.log.constant.StoneOperateType;
import com.game.log.consumer.EventManager;
import com.game.log.domain.ActHopeLog;
import com.game.log.domain.ActMaterialSubstitutionLog;
import com.game.log.domain.ActivityLog;
import com.game.log.domain.CampMembersRankLog;
import com.game.log.domain.GetActPowerLog;
import com.game.log.domain.RoleResourceChangeLog;
import com.game.log.domain.RoleResourceLog;
import com.game.manager.ActivityManager;
import com.game.manager.BigMonsterManager;
import com.game.manager.ChatManager;
import com.game.manager.CountryManager;
import com.game.manager.DailyTaskManager;
import com.game.manager.EquipManager;
import com.game.manager.HeroManager;
import com.game.manager.KillEquipManager;
import com.game.manager.MissionManager;
import com.game.manager.PlayerManager;
import com.game.manager.ServerManager;
import com.game.manager.SurpriseGiftManager;
import com.game.manager.TDTaskManager;
import com.game.manager.WarBookManager;
import com.game.manager.WorldManager;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.cs.FixSevenLoginSignHandler;
import com.game.pb.ActivityPb;
import com.game.pb.ActivityPb.ActBuildRankRs;
import com.game.pb.ActivityPb.ActBuyGiftRs;
import com.game.pb.ActivityPb.ActCampMembersRankRs;
import com.game.pb.ActivityPb.ActChrismasBuyRq;
import com.game.pb.ActivityPb.ActChrismasBuyRs;
import com.game.pb.ActivityPb.ActChrismasRewardRq;
import com.game.pb.ActivityPb.ActChrismasRewardRs;
import com.game.pb.ActivityPb.ActChrismasRq;
import com.game.pb.ActivityPb.ActChrismasRs;
import com.game.pb.ActivityPb.ActCityRs;
import com.game.pb.ActivityPb.ActCostPersonRs;
import com.game.pb.ActivityPb.ActCountryRankRs;
import com.game.pb.ActivityPb.ActDailyCheckInRs;
import com.game.pb.ActivityPb.ActDailyExpeditionRs;
import com.game.pb.ActivityPb.ActDailyMissionRs;
import com.game.pb.ActivityPb.ActDailyRechargeRs;
import com.game.pb.ActivityPb.ActDailyTrainRs;
import com.game.pb.ActivityPb.ActDayPayRs;
import com.game.pb.ActivityPb.ActDoubleEggChangeRq;
import com.game.pb.ActivityPb.ActDoubleEggChangeRs;
import com.game.pb.ActivityPb.ActDoubleEggRq;
import com.game.pb.ActivityPb.ActDoubleEggRs;
import com.game.pb.ActivityPb.ActFlashGiftRs;
import com.game.pb.ActivityPb.ActForgeRankRs;
import com.game.pb.ActivityPb.ActGoldRankRs;
import com.game.pb.ActivityPb.ActGrandRecharegRq;
import com.game.pb.ActivityPb.ActGrandRecharegRs;
import com.game.pb.ActivityPb.ActGrowFootRs;
import com.game.pb.ActivityPb.ActHeroDialRq;
import com.game.pb.ActivityPb.ActHeroDialRs;
import com.game.pb.ActivityPb.ActHeroKowtowRs;
import com.game.pb.ActivityPb.ActHighVipRs;
import com.game.pb.ActivityPb.ActHopeRs;
import com.game.pb.ActivityPb.ActInvestRs;
import com.game.pb.ActivityPb.ActKillAllRs;
import com.game.pb.ActivityPb.ActLevelRankRs;
import com.game.pb.ActivityPb.ActLevelRs;
import com.game.pb.ActivityPb.ActLoginVipRs;
import com.game.pb.ActivityPb.ActLowCountryRs;
import com.game.pb.ActivityPb.ActLuckDialRs;
import com.game.pb.ActivityPb.ActLucklyDialRs;
import com.game.pb.ActivityPb.ActLuxuryGiftRs;
import com.game.pb.ActivityPb.ActMasterDialRq;
import com.game.pb.ActivityPb.ActMasterDialRs;
import com.game.pb.ActivityPb.ActMasterRankRq;
import com.game.pb.ActivityPb.ActMasterRankRs;
import com.game.pb.ActivityPb.ActMaterAwardRq;
import com.game.pb.ActivityPb.ActMaterAwardRs;
import com.game.pb.ActivityPb.ActMaterInfoRs;
import com.game.pb.ActivityPb.ActMonsterRs;
import com.game.pb.ActivityPb.ActMonthCardRs;
import com.game.pb.ActivityPb.ActMonthGiftRs;
import com.game.pb.ActivityPb.ActOilRankRs;
import com.game.pb.ActivityPb.ActOnlineAwardRq;
import com.game.pb.ActivityPb.ActOnlineAwardRs;
import com.game.pb.ActivityPb.ActOnlineTimeRs;
import com.game.pb.ActivityPb.ActOpenBuildGiftRs;
import com.game.pb.ActivityPb.ActOrderRs;
import com.game.pb.ActivityPb.ActPassPortPorAwardRs;
import com.game.pb.ActivityPb.ActPayArmsRs;
import com.game.pb.ActivityPb.ActPayEveryDayRs;
import com.game.pb.ActivityPb.ActPayFirstRs;
import com.game.pb.ActivityPb.ActPayGiftRs;
import com.game.pb.ActivityPb.ActPowerRs;
import com.game.pb.ActivityPb.ActPurpDialRq;
import com.game.pb.ActivityPb.ActPurpDialRs;
import com.game.pb.ActivityPb.ActSceneCityRs;
import com.game.pb.ActivityPb.ActSerPayRs;
import com.game.pb.ActivityPb.ActSevenLoginRs;
import com.game.pb.ActivityPb.ActSevenRechargeRs;
import com.game.pb.ActivityPb.ActSevenRq;
import com.game.pb.ActivityPb.ActSevenRs;
import com.game.pb.ActivityPb.ActSoilderRankRs;
import com.game.pb.ActivityPb.ActSpecialGiftRq;
import com.game.pb.ActivityPb.ActSpecialGiftRs;
import com.game.pb.ActivityPb.ActSpringAwardRs;
import com.game.pb.ActivityPb.ActSpringGiftRs;
import com.game.pb.ActivityPb.ActSpringRechargeRs;
import com.game.pb.ActivityPb.ActSpringTurntableRq;
import com.game.pb.ActivityPb.ActSpringTurntableRs;
import com.game.pb.ActivityPb.ActStoneDialRs;
import com.game.pb.ActivityPb.ActStoneRankRs;
import com.game.pb.ActivityPb.ActSuripriseGiftRq;
import com.game.pb.ActivityPb.ActSuripriseGiftRs;
import com.game.pb.ActivityPb.ActTaskHeroRq;
import com.game.pb.ActivityPb.ActTaskHeroRs;
import com.game.pb.ActivityPb.ActTopupPersonRs;
import com.game.pb.ActivityPb.ActTopupRankRs;
import com.game.pb.ActivityPb.ActTopupServerRs;
import com.game.pb.ActivityPb.ActWashEquiptRs;
import com.game.pb.ActivityPb.ActWashRankRs;
import com.game.pb.ActivityPb.ActWellCrownThreeArmyRq;
import com.game.pb.ActivityPb.ActWellCrownThreeArmyRs;
import com.game.pb.ActivityPb.ActWorldBattleRs;
import com.game.pb.ActivityPb.ActZeroGiftRs;
import com.game.pb.ActivityPb.ActzhenjiIconRs;
import com.game.pb.ActivityPb.BuyActPassPortLvRq;
import com.game.pb.ActivityPb.BuyActPassPortLvRs;
import com.game.pb.ActivityPb.BuyHeroKowtowRq;
import com.game.pb.ActivityPb.BuyHeroKowtowRs;
import com.game.pb.ActivityPb.BuyLanternRq;
import com.game.pb.ActivityPb.BuyLanternRs;
import com.game.pb.ActivityPb.BuyPayArmsRq;
import com.game.pb.ActivityPb.BuyPayArmsRs;
import com.game.pb.ActivityPb.BuyStoneDialRs;
import com.game.pb.ActivityPb.DoActPayFirstRs;
import com.game.pb.ActivityPb.DoActTaskHeroRewardRq;
import com.game.pb.ActivityPb.DoActTaskHeroRewardRs;
import com.game.pb.ActivityPb.DoActZeroGiftRq;
import com.game.pb.ActivityPb.DoActZeroGiftRs;
import com.game.pb.ActivityPb.DoDailyCheckInRq;
import com.game.pb.ActivityPb.DoDailyCheckInRs;
import com.game.pb.ActivityPb.DoGrowFootRq;
import com.game.pb.ActivityPb.DoGrowFootRs;
import com.game.pb.ActivityPb.DoHeroDialRq;
import com.game.pb.ActivityPb.DoHeroDialRs;
import com.game.pb.ActivityPb.DoHeroExchangelRq;
import com.game.pb.ActivityPb.DoHeroExchangelRs;
import com.game.pb.ActivityPb.DoHopeRq;
import com.game.pb.ActivityPb.DoHopeRs;
import com.game.pb.ActivityPb.DoInvestRs;
import com.game.pb.ActivityPb.DoLuckDialRq;
import com.game.pb.ActivityPb.DoLuckDialRs;
import com.game.pb.ActivityPb.DoLucklyDialRq;
import com.game.pb.ActivityPb.DoLucklyDialRs;
import com.game.pb.ActivityPb.DoMasterDialRq;
import com.game.pb.ActivityPb.DoMasterDialRs;
import com.game.pb.ActivityPb.DoPayArmsAwardRq;
import com.game.pb.ActivityPb.DoPayArmsAwardRs;
import com.game.pb.ActivityPb.DoPurpDialRq;
import com.game.pb.ActivityPb.DoPurpDialRs;
import com.game.pb.ActivityPb.DoQuotaRq;
import com.game.pb.ActivityPb.DoQuotaRs;
import com.game.pb.ActivityPb.DoRaidersRq;
import com.game.pb.ActivityPb.DoRaidersRs;
import com.game.pb.ActivityPb.DoRecharDialRq;
import com.game.pb.ActivityPb.DoRecharDialRs;
import com.game.pb.ActivityPb.DoSevenAwardRq;
import com.game.pb.ActivityPb.DoSevenAwardRs;
import com.game.pb.ActivityPb.DoSpringTurntableRq;
import com.game.pb.ActivityPb.DoSpringTurntableRs;
import com.game.pb.ActivityPb.DoStoneDialRs;
import com.game.pb.ActivityPb.DoWashEquiptRq;
import com.game.pb.ActivityPb.DoWashEquiptRs;
import com.game.pb.ActivityPb.ExchangeHeroRs;
import com.game.pb.ActivityPb.ExchangeItemRs;
import com.game.pb.ActivityPb.GetActPowerRq;
import com.game.pb.ActivityPb.GetActPowerRs;
import com.game.pb.ActivityPb.GetActivityAwardRq;
import com.game.pb.ActivityPb.GetActivityAwardRs;
import com.game.pb.ActivityPb.GetActivityListRs;
import com.game.pb.ActivityPb.GetMaterialSubstitutionRs;
import com.game.pb.ActivityPb.GetMonthCardAwardRq;
import com.game.pb.ActivityPb.GetMonthCardAwardRs;
import com.game.pb.ActivityPb.GetTDTaskRs;
import com.game.pb.ActivityPb.GetWonderfulListRs;
import com.game.pb.ActivityPb.MaterialSubstitutionRq;
import com.game.pb.ActivityPb.MaterialSubstitutionRs;
import com.game.pb.ActivityPb.RaidersRq;
import com.game.pb.ActivityPb.RaidersRs;
import com.game.pb.ActivityPb.ReceiveSpringFestivalRq;
import com.game.pb.ActivityPb.ReceiveSpringFestivalRs;
import com.game.pb.ActivityPb.RecharDialRs;
import com.game.pb.ActivityPb.RefreshHeroKowtowRs;
import com.game.pb.ActivityPb.TDTaskAwardRq;
import com.game.pb.ActivityPb.TDTaskAwardRs;
import com.game.pb.ActivityPb.actCollectionResourceRs;
import com.game.pb.ActivityPb.getBuildGiftRs;
import com.game.pb.BasePb;
import com.game.pb.CommonPb;
import com.game.pb.CommonPb.ActSpringFestival;
import com.game.pb.CommonPb.ActWashEquipt;
import com.game.pb.CommonPb.ActivityCondState;
import com.game.pb.CommonPb.BattleAward;
import com.game.pb.CommonPb.MonthCard;
import com.game.pb.CommonPb.PayItem;
import com.game.pb.CommonPb.Prop;
import com.game.pb.CommonPb.WorldBattleAward;
import com.game.pb.PropPb;
import com.game.pb.WarBookPb;
import com.game.server.GameServer;
import com.game.util.DateHelper;
import com.game.util.PbHelper;
import com.game.util.RandomHelper;
import com.game.util.RandomUtil;
import com.game.spring.SpringUtil;
import com.game.util.TimeHelper;
import com.game.util.random.WeightRandom;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import io.netty.util.internal.StringUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ChenKui
 * @version 创建时间：2015-10-24 下午15:16:23
 * @declare 活动处理模块
 */
@Service
public class ActivityService {

	@Autowired
	private PlayerManager playerManager;
	@Autowired
	private ActivityManager activityManager;
	@Autowired
	private StaticActivityMgr staticActivityMgr;
	@Autowired
	private HeroManager heroManager;
	@Autowired
	private EquipManager equipManager;
	@Autowired
	private KillEquipManager killEquipManager;
	@Autowired
	private MissionManager missionManager;
	@Autowired
	private WorldManager worldManager;
	@Autowired
	private StaticLimitMgr staticLimitMgr;
	@Autowired
	private StaticPropMgr staticPropMgr;
	@Autowired
	private StaticEquipDataMgr equipDataMgr;
	@Autowired
	private ChatManager chatManager;
	@Autowired
	private WorldActPlanService worldActPlanService;
	@Autowired
	private DailyTaskManager dailyTaskManager;
	@Autowired
	private SurpriseGiftManager surpriseGiftManager;
	@Autowired
	private BigMonsterManager bigMonsterManager;
	@Autowired
	private ServerManager serverManager;
	@Autowired
	private EventManager eventManager;

	private Map<Integer, AfterActivity> afterActivityMap;

	private Map<Integer, List<StaticActPayMoney>> actPayMap = new HashMap<>();

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 活动结束处理
	 */
	public interface AfterActivity {

		/**
		 * 各项活动结束实际处理方式
		 *
		 * @param activityBase
		 * @param activityData
		 */
		void sendMail(ActivityBase activityBase, ActivityData activityData);
	}

	@PostConstruct
	public void init() {
		afterActivityMap = new HashMap<>();
		afterActivityMap.put(ActivityConst.RANK_1, this::sendRankMail);
		afterActivityMap.put(ActivityConst.RANK_2, this::sendRankOffAward);
		afterActivityMap.put(ActivityConst.ACT_GROW_FOOT, this::sendFootMail);
		afterActivityMap.put(ActivityConst.ACT_DIAL_LUCK, this::sendLuckOffAward);
		afterActivityMap.put(ActivityConst.ACT_SER_PAY, this::sendSerPayOffAward);
		afterActivityMap.put(ActivityConst.ACT_SEVEN, this::sendSevenOffAward);
		afterActivityMap.put(ActivityConst.ACT_ARMS_PAY, this::sendArmsPayOffAward);
		afterActivityMap.put(ActivityConst.ACT_WASH_EQUIP, this::sendActWashEquiptOffAward);
		afterActivityMap.put(ActivityConst.ACT_PASS_PORT, this::sendActPassPortAwardAward);
		afterActivityMap.put(ActivityConst.ACT_MENTOR_SCORE, this::sendActMentorScore);
		afterActivityMap.put(ActivityConst.ACT_DOUBLE_EGG, this::sendActDoubleEggs);
		afterActivityMap.put(ActivityConst.ACT_NEW_YEAR_EGG, this::sendActDoubleEggs);
		afterActivityMap.put(ActivityConst.ACT_DRAGON_BOAT, this::sendActDoubleEggs);
		afterActivityMap.put(ActivityConst.ACT_DOUBLE_EGG_GIFT, this::sendActChrismasReward);
		afterActivityMap.put(ActivityConst.ACT_NEW_YEAR_GIFT, this::sendActChrismasReward);
		afterActivityMap.put(ActivityConst.ACT_DRAGON_BOAT_GIFT, this::sendActChrismasReward);
		afterActivityMap.put(ActivityConst.ACT_MEDAL_EXCHANGE, this::sendActDoubleEggs);
		afterActivityMap.put(ActivityConst.ACT_WORLD_BOX, this::sendActWorldBoxReward);
		afterActivityMap.put(ActivityConst.ACT_CAMP_MEMBERS, this::sendActCampMembersRankRq);
		afterActivityMap.put(ActivityConst.ACT_WELL_CROWN_THREE_ARMY, this::endWellCrownThreeArmy);
		afterActivityMap.put(ActivityConst.ACT_GRAND_TOTAL, this::sendActGrandTotalReward);
		afterActivityMap.put(ActivityConst.ACT_SEARCH, this::sendActMaterTotalReward);
		afterActivityMap.put(ActivityConst.ACT_COLLECTION_RESOURCE, this::sendActCollectionResource);
		afterActivityMap.put(ActivityConst.ACT_TOPUP_RANK, this::sendActRecharScore);
		afterActivityMap.put(ActivityConst.ACT_COST_GOLD, this::sendActUseScore);
		afterActivityMap.put(ActivityConst.ACT_SPRING_FESTIVAL, this::sendSpringFestival);
	}

	// 物质搜寻在活动结束的时候 发放奖励
	private void sendActMaterTotalReward(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (actRecord == null) {
				continue;
			}
			// 奖励的
			List<CommonPb.Award> tawards = new ArrayList<>();
			List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
			condList.forEach((statiAward) -> {
				if (actRecord.getStatus(statiAward.getSortId()) >= statiAward.getCond() && !actRecord.getReceived().containsKey(statiAward.getKeyId())) {
					actRecord.getReceived().put(statiAward.getKeyId(), 1);
					tawards.addAll(statiAward.getAwardPbList());
				}
			});
			// 已达成条件,未领取奖励,补发奖励邮件
			if (tawards.size() > 0) {
				playerManager.sendAttachMail(next, PbHelper.finilAward(tawards), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}
		}
	}

	private void sendActRecharScore(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (actRecord == null) {
				continue;
			}
			List<CommonPb.Award> awardList = new ArrayList<>();
			ActPlayerRank actRank = activityData.getLordRank(next.roleId);
			if (actRank == null) {
				continue;
			}
			// 获取当前排名的奖励
			StaticActAward rankAward = staticActivityMgr.getActRankAward(activityBase.getAwardId(), actRank.getRank());
			if (rankAward != null && !actRecord.getReceived().containsKey(rankAward.getKeyId())) {
				actRecord.getReceived().put(rankAward.getKeyId(), 1);
				awardList.addAll(rankAward.getAwardPbList());

			}
			if (awardList.size() > 0) {
				// 已达成条件,未领取奖励,补发奖励邮件
				playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
			}
			playerManager.synActivity(next, activityBase.getActivityId());
		}
	}

	private void sendActUseScore(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (actRecord == null) {
				continue;
			}
			List<CommonPb.Award> awardList = new ArrayList<>();
			ActPlayerRank actRank = activityData.getLordRank(next.roleId);
			if (actRank == null) {
				continue;
			}
			// 获取当前排名的奖励
			StaticActAward rankAward = staticActivityMgr.getActRankAward(activityBase.getAwardId(), actRank.getRank());
			if (rankAward != null && !actRecord.getReceived().containsKey(rankAward.getKeyId())) {
				actRecord.getReceived().put(rankAward.getKeyId(), 1);
				awardList.addAll(rankAward.getAwardPbList());
			}
			if (awardList.size() > 0) {
				// 已达成条件,未领取奖励,补发奖励邮件
				playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());

			}

		}
	}

	/**
	 * 奖励定时器 针对特价尊享中的单个礼包倒计时 在起服和每天转点拉取一次
	 */
	public void activityRewardLogic() {
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SPECIAL_GIFT);
		if (activityBase == null) {
			return;
		}
		List<StaticActPayMoney> payMoneyList = staticActivityMgr.getPayMoneyList(activityBase.getAwardId());
		if (payMoneyList != null) {
			actPayMap.clear();
			Date now = new Date();
			Date openTime = serverManager.getServer().getOpenTime();
			for (StaticActPayMoney staticActPayMoney : payMoneyList) {
				List<StaticActPayMoney> list = actPayMap.get(staticActPayMoney.getAwardId());
				if (list == null) {
					list = new ArrayList<>();
					actPayMap.put(staticActPayMoney.getAwardId(), list);
				}
				boolean isOpen = staticActPayMoney.isOpen(openTime, now);
				if (isOpen && !list.contains(staticActPayMoney)) {
					list.add(staticActPayMoney);
				}
				// 活动开启中并且不在list中
//                if (isOpen && !isIn(list, staticActPayMoney)) {
//                    list.add(staticActPayMoney);
//                } else if (!isOpen) {
//                    if (list.size() > 0) {
//                        Iterator<StaticActPayMoney> it = list.iterator();
//                        while (it.hasNext()) {
//                            StaticActPayMoney nextIt = it.next();
//                            if (nextIt.getPayMoneyId() == staticActPayMoney.getPayMoneyId()) {
//                                it.remove();
//                                break;
//                            }
//                        }
//                    }
//                }
			}
		}
	}

	private boolean isIn(List<StaticActPayMoney> list, StaticActPayMoney actPayMoney) {
		boolean result = false;
		for (StaticActPayMoney act : list) {
			if (act.getPayMoneyId() == actPayMoney.getPayMoneyId()) {
				result = true;
				break;
			}
		}
		return result;
//        Iterator<StaticActPayMoney> iterator = list.iterator();
//
//        while (iterator.hasNext()) {
//            StaticActPayMoney act = iterator.next();
//            if (act.getPayMoneyId() == actPayMoney.getPayMoneyId()) {
//                iterator.remove();
//                list.add(actPayMoney);
//                result = true;
//                break;
//            }
//        }
//        return result;
	}

	/**
	 * 活动的定时器
	 */
	public void activityTimerLogic() {

		Date now = new Date();
		int hour = TimeHelper.getCurrentHour();
		int today = TimeHelper.getDay(now);

		/**
		 * 更新夜袭虫群活动的状态
		 */
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_5);
		if (worldActPlan != null) {
			// 先做检查
			if (worldActPlan.getState() == WorldActPlanConsts.NOE_OPEN) {
				if (worldActPlan.getPreheatTime() != 0 && System.currentTimeMillis() > worldActPlan.getPreheatTime()) {
					worldActPlan.setState(WorldActPlanConsts.PREHEAT);
					worldActPlanService.syncWorldActivityPlan();
				}
			} else if (worldActPlan.getState() == WorldActPlanConsts.OPEN) {
				// 执行活动结束逻辑
				if (System.currentTimeMillis() > worldActPlan.getEndTime() && worldActPlan.getEndTime() != 0) {
					worldActPlan.setState(WorldActPlanConsts.END);
				}
			}
			worldActPlanService.refreshWorldActPlan(worldActPlan);
		}
		for (Player player : playerManager.getOnlinePlayer()) {
			ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DAILY_CHECKIN);
			if (actRecord == null) {
				continue;
			}
			long timeKey = 0;
			if (actRecord.getStatus().containsKey(timeKey)) {
				Date pre = new Date();
				pre.setTime(actRecord.getStatus(timeKey));
				int clean = staticLimitMgr.getNum(237);
				if (TimeHelper.isNextDay(clean, now, pre)) {
					actRecord.putRecord(0, actRecord.getRecord(0) + 1);
					actRecord.putState(timeKey, now.getTime());
				}
			} else {
				actRecord.putState(timeKey, now.getTime());
			}
		}
		checkNewOnlinePlayer();

		List<ActivityBase> list = staticActivityMgr.getActivityList();
		for (ActivityBase activityBase : list) {
			int step = activityBase.getStep();

			// 排名活动的排名历史最高记录在19点开启
			if (activityBase.isRankAct() && step == ActivityConst.ACTIVITY_BEGIN) {
				ActivityData activityData = activityManager.getActivity(activityBase);
				int history = activityData.getHistory();
				int beginDay = TimeHelper.getDay(activityBase.getBeginTime());

				if (history == 0 && (beginDay != today || hour >= ActivityConst.RANK_HOUR)) {
					activityData.recordHistory();
					activityData.setHistory(1);
					synActivity(activityBase, activityData, EventEnum.SEVEN_PM_REWARD);
				}
			}
			// 排名活动的排名历史最高记录在19点开启
			if (activityBase.isRankThree() && step == ActivityConst.ACTIVITY_BEGIN) {
				ActivityData activityData = activityManager.getActivity(activityBase);
				int history = activityData.getHistory();
				Date date = TimeHelper.getRewardTime(activityBase.getEndTime());
				if (history == 0 && now.after(date)) { // 到了7点钟之后
					activityData.recordHistory();
					activityData.setHistory(1);
					synActivity(activityBase, activityData, EventEnum.SEVEN_PM_REWARD);
				}
			}

			// 阵营骨干结束当天19刷新定榜 刷新任务红点
			refreshCampMembersRank(now, activityBase, step);

			// 刷新通行证活动的任务的时间周期
			if (step == ActivityConst.ACTIVITY_BEGIN && activityBase.getActivityId() == ActivityConst.ACT_PASS_PORT) {
				activityManager.refreshPassPortTaskTime();
			}
		}

		// 对未领奖的玩家发放邮件
		List<ActivityBase> sendList = staticActivityMgr.getSendMailList();
		if (!sendList.isEmpty()) {
			Iterator<ActivityBase> iterator = sendList.iterator();
			while (iterator.hasNext()) {
				ActivityBase activityBase = iterator.next();
				if (!activityBase.isSendMail()) {
					continue;
				}
				if (activityBase.getStaticActivity().getActivityId() == ActivityConst.ACT_ZERO_GIFT) {
					sendActZeroMail(activityBase);
					if (now.after(activityBase.getSendTime())) { // 活动结束了
						// 重制下
						// ActivityData activityData = activityManager.getActivity(activityBase);
						activityBase.setSendMail(false);
						iterator.remove();
					}
					continue;
				}
				if (now.after(activityBase.getSendTime())) {
					// 排行活动处理
					if (activityBase.getActivityId() == ActivityConst.ACT_HERO_DIAL) {
						this.sendActMyReward();
					}
					AfterActivity activity = afterActivityMap.get(activityBase.getStaticActivity().getRank());
					if (activity != null) {
						ActivityData activityData = activityManager.getActivity(activityBase);
						activity.sendMail(activityBase, activityData);
						activityBase.setSendMail(false);
						iterator.remove();
						continue;
					}
					// 其他活动处理
					activity = afterActivityMap.get(activityBase.getStaticActivity().getActivityId());
					if (activity != null) {
						ActivityData activityData = activityManager.getActivity(activityBase);
						activity.sendMail(activityBase, activityData);
						activityBase.setSendMail(false);
						iterator.remove();
						continue;
					}
					// 发送奖励
					ActivityData activityData = activityManager.getActivity(activityBase);
					sendActAward(activityBase, activityData);
					activityBase.setSendMail(false);
					iterator.remove();
				}
			}
		}
		// 未领取的月卡奖励补发邮件
		this.sendActMonthlyCard();
	}

	public void synActivity(ActivityBase activityBase, ActivityData activityData, EventEnum eventEnum) {
		int actId = activityBase.getActivityId();
		for (Player player : playerManager.getOnlinePlayer()) {
			ActRecord actRecord = activityManager.getActivityInfo(player, actId);
			if (actRecord == null) {
				continue;
			}
			ActivityEventManager.getInst().addTipsSyn(eventEnum, new RankActor(player, actRecord, activityBase, activityData));
		}
	}

	@Autowired
	WarBookManager warBookManager;

	private void checkNewOnlinePlayer() {
		long now = System.currentTimeMillis();
		WarBookPb.ReFlushBookShopRq build = WarBookPb.ReFlushBookShopRq.newBuilder().build();
		BasePb.Base.Builder msg = PbHelper.createSynBase(WarBookPb.ReFlushBookShopRq.EXT_FIELD_NUMBER, WarBookPb.ReFlushBookShopRq.ext, build);
		for (Player player : playerManager.getOnlinePlayer()) {
			if (player.getLord().getWarBookShopRefreshTime() < now) {
				warBookManager.refreshWarbookShop(player);
				warBookManager.updateRefeshWarShopTime(player);
				GameServer.getInstance().sendMsgToPlayer(player, msg);
				player.setBookFlush(1);
			}
			ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ONLINE_TIME);
			if (actRecord == null) {
				continue;
			}
			List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
			if (null == condList || condList.size() == 0) {
				continue;
			}
			actRecord.addCount();
		}
	}

	/**
	 * 通信证活动结束补发奖励
	 *
	 * @param activityBase
	 * @param activityData
	 */
	private void sendActPassPortAwardAward(ActivityBase activityBase, ActivityData activityData) {
		// 奖励列表
		List<StaticPassPortAward> passPortList = staticActivityMgr.getPassPortList(activityData.getAwardId());
		if (passPortList == null || passPortList.size() == 0) {
			return;
		}

		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			Map<Integer, Integer> record = actRecord.getRecord();
			if (record.get(0) == null) {
				record.put(0, 0);
			}
			if (record.get(1) == null) {
				record.put(1, 0);
			}

			Integer score = record.get(0);
			Integer isBuy = record.get(1);
			int lv = staticActivityMgr.getPassPortLv(score);

			List<CommonPb.Award> awardList = new ArrayList<>();
			for (StaticPassPortAward staticPassPortAward : passPortList) {
				if (!actRecord.getReceived().containsKey(staticPassPortAward.getId()) && lv >= staticPassPortAward.getLv()) {
					if (staticPassPortAward.getType() == 2 && isBuy == 0) {
						continue;
					}
					List<List<Integer>> award = staticPassPortAward.getAward();
					if (award.size() > 0) {
						for (List<Integer> awd : award) {
							playerManager.addAward(next, awd.get(0), awd.get(1), awd.get(2), Reason.PASSPORT_SCORE);
							awardList.add(PbHelper.createAward(awd.get(0), awd.get(1), awd.get(2)).build());
						}
					}
					actRecord.getReceived().put(staticPassPortAward.getId(), 1);
				}
			}
			if (awardList.size() > 0) {
				// 已达成条件,未领取奖励,补发奖励邮件
				playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}
		}
	}

	/**
	 * @param activityBase
	 * @param activityData
	 */
	private void sendArmsPayOffAward(ActivityBase activityBase, ActivityData activityData) {
		// 奖励列表
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (condList == null) {
			return;
		}
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			boolean getAward = true;
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);

			CommonPb.ArmsPayAward.Builder armsPayAward = getArmsPayAward(next, activityData, actRecord);
			int count = armsPayAward.getCount();
			if (count > 0) {
				List<CommonPb.Award> awardList = new ArrayList<>();
				for (int i = 0; i < count; i++) {
					for (StaticActAward e : condList) {
						Integer received = actRecord.getReceived().get(e.getKeyId());
						if (received != null) {
							actRecord.getReceived().put(e.getKeyId(), received + 1);
						} else {
							actRecord.getReceived().put(e.getKeyId(), 1);
						}
						awardList.addAll(e.getAwardPbList());
					}
				}
				// 已达成条件,未领取奖励,补发奖励邮件
				playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}
		}
	}

	/**
	 * 活动奖励未领取邮件
	 *
	 * @param activityBase
	 * @param activityData
	 */
	public void sendActAward(ActivityBase activityBase, ActivityData activityData) {
		// 奖励列表
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (condList == null) {
			return;
		}
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			sendPlayerActAward(actRecord, activityBase, activityData, condList, next);
		}
	}

	public void sendActTopUpPerson(Player player, ActivityBase activityBase, ActRecord actRecord) {
		// 奖励列表
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (condList == null) {
			return;
		}
		// 发送奖励
		ActivityData activityData = activityManager.getActivity(activityBase);
		sendPlayerActAward(actRecord, activityBase, activityData, condList, player);
	}

	private void sendPlayerActAward(ActRecord actRecord, ActivityBase activityBase, ActivityData activityData, List<StaticActAward> condList, Player next) {
		List<CommonPb.Award> awardList = new ArrayList<>();
		// 已达成条件,未领取奖励,补发奖励邮件
		for (StaticActAward e : condList) {
			int cond = 0;
			// 全服累积的
			if (activityBase.getStaticActivity().getAddUp() == 1) {
				cond = currentActivity(next, activityBase, activityData, e);
			} else {
				cond = currentActivity(next, actRecord, e.getSortId());
			}
			if (!actRecord.getReceived().containsKey(e.getKeyId()) && cond != 0 && cond >= e.getCond()) {
				actRecord.getReceived().put(e.getKeyId(), 1);
				awardList.addAll(e.getAwardPbList());

				SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(activityBase.getActivityId()).isAward(true).awardId(e.getAwardId()).giftName(e.getDesc()).roleId(next.roleId).vip(next.getVip()).costGold(e.getCond()).channel(next.account.getChannel()).build());
			}
		}
		if (!awardList.isEmpty()) {
			playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
		}
	}

	/**
	 * 排行奖励未领取邮件
	 *
	 * @param activityBase
	 * @param activityData
	 */
	public void sendRankOffAward(ActivityBase activityBase, ActivityData activityData) {
		// 奖励列表
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (condList == null) {
			return;
		}
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			boolean getAward = true;
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			ActPlayerRank personRank = activityData.getLordRank(next.getLord().getLordId());
			if (personRank == null) {
				continue;
			}
			// 已达成条件,未领取奖励,补发奖励邮件
			for (StaticActAward e : condList) {

				if (personRank.getRank() != 0 && personRank.getRank() <= e.getCond() && !actRecord.getReceived().containsKey(e.getKeyId()) && getAward) {
					actRecord.getReceived().put(e.getKeyId(), 1);
					playerManager.sendAttachMail(next, PbHelper.finilAward(e.getAwardPbList()), MailId.ACT_RANK_AWARD, activityBase.getStaticActivity().getName(), String.valueOf(personRank.getRank()));
					getAward = false;
				}

			}

		}
	}

	public void sendRankMail(ActivityBase activityBase, ActivityData activityData) {
		Map<Long, Long> status = activityData.getStatus();
		// 奖励列表
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (condList == null) {
			return;
		}

		// 排行榜中有排名
		Iterator<Entry<Long, Long>> it = status.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Long, Long> next = it.next();
			long lordId = next.getKey();
			long rank = next.getValue();
			if (rank == 0) {
				continue;
			}

			Player target = playerManager.getPlayer(lordId);
			if (target == null) {
				continue;
			}

			List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();

			ActRecord actRecord = activityManager.getActivityInfo(target, activityBase);

			// 已达成条件,未领取奖励,补发奖励邮件
			for (StaticActAward e : condList) {
				if (!actRecord.getReceived().containsKey(e.getKeyId()) && rank <= e.getCond()) {
					actRecord.getReceived().put(e.getKeyId(), 1);
					awardList.addAll(e.getAwardPbList());
				}
			}

			if (!awardList.isEmpty()) {
				playerManager.sendAttachMail(target, PbHelper.finilAward(awardList), MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
			}
		}
	}

	/**
	 * Function:活动开启列表
	 *
	 * @param handler
	 */
	public void getActivityList(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		GetActivityListRs.Builder builder = GetActivityListRs.newBuilder();

		Date createDate = player.account.getCreateDate();
		Date now = new Date();
		Date pre = player.account.getLoginDate();
		if (!DateHelper.isSameDate(now, pre)) {
			player.account.setLoginDays(player.account.getLoginDays() + 1);
			player.account.setLoginDate(now);
			activityManager.reflushSeven(player.roleId, ActivityConst.ACT_LOGIN_SEVEN);
			activityManager.reflushSeven(player.roleId, ActivityConst.ACT_SEARCH);
		}
		List<ActivityBase> list = staticActivityMgr.getActivityList();

		// 1.活动结束,则不展示给客户端
		// 2.活动预备开放前一天,仅展示一个活动名称给客户端
		// 3.活动开放阶段,tip提示,可否领奖
		for (ActivityBase activityBase : list) {

			if (activityBase.isWonderfulActivity()) {
				continue;
			}

			int step = activityBase.getStep();
			if (step == ActivityConst.ACTIVITY_CLOSE) {// 活动未开启
				continue;
			} else if (step == ActivityConst.ACTIVITY_TO_BEGIN) {// 准备开放
				if (activityBase.getActivityId() != ActivityConst.ACT_PASS_PORT) {
					builder.addActivity(PbHelper.createActivityPb(activityBase, null, false, false));
				}
				continue;
			}
			// 首充活动奖励领完,则关闭
			if (activityBase.getActivityId() == ActivityConst.ACT_PAY_FIRST && player.getLord().getFirstPay() == 2) {
				continue;
			}

			ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
			if (activityBase.getActivityId() == ActivityConst.ACT_BEAUTY_GIFT) {
				if (player.getRecordList().contains(284) && player.getBeautys().get(BeautyId.Sufei) == null) {
					actRecord.setShow(true);
				}
				if (!actRecord.isShow()) {
					continue;
				}
			}
			// 不在周五就不发。
			if (activityBase.getActivityId() == ActivityConst.BLOOD_ACTIVITY) {
				WorldData worldData = worldManager.getWolrdInfo();
				WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);
				LocalDate now1 = LocalDate.now();
				if (worldActPlan == null || worldActPlan.getPreheatTime() == 0 || now1.getDayOfWeek().getValue() != 5 || TimeHelper.isThisWeek(worldActPlan.getTargetSuccessTime())) {
					continue;
				}
			}
			// 可领奖，不可领奖
			boolean cangetAward = activityBase.canAward();

			// 活动tips
			boolean tip = activityTips(player, cangetAward, actRecord, activityBase);

			// less大于零,活动开启,关闭时间和玩家创建账号时间相关[结束时间:创建角色时间+less天数]
			int less = activityBase.getStaticActivity().getLess();
			if (less != 0) {
				Date endTime = DateHelper.addDate(createDate, less);
				if (now.before(endTime)) {
					// 超值礼包 领取奖励后关闭
					if (activityBase.getActivityId() == ActivityConst.ACT_LUXURY_GIFT) {
						if (actRecord == null) {
							continue;
						}
						List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
						if (condList == null || condList.size() == 0) {
							continue;
						}
						boolean flag = false;
						for (StaticActAward actAward : condList) {
							if (actRecord.getReceived().containsKey(actAward.getKeyId())) {
								flag = true;
							}
						}
						if (!flag) {
							builder.addActivity(PbHelper.createActivityPb(activityBase, endTime, cangetAward, tip));
						}
						continue;
					}
					builder.addActivity(PbHelper.createActivityPb(activityBase, endTime, cangetAward, tip));
				}
				continue;
			}

			// 限时礼包购买后,则关闭
			if (activityBase.getActivityId() == ActivityConst.ACT_FLASH_GIFT) {
				actFlashGift(player, builder, activityBase, cangetAward, tip);
				continue;
			}
			// 月卡大礼包购买后,则关闭
			if (activityBase.getActivityId() == ActivityConst.ACT_MONTH_GIFT) {
				actMonthGift(player, builder, activityBase, cangetAward, tip);
				continue;
			}
			// 许愿池领完关闭
			if (activityBase.getActivityId() == ActivityConst.ACT_HOPE) {
				Map<Integer, StaticActHope> staticActHopeMap = staticActivityMgr.getStaticActHopeMap();
				boolean gotAll = true;
				for (StaticActHope e : staticActHopeMap.values()) {
					int keyId = e.getLevel();
					if (!actRecord.getReceived().containsKey(keyId)) {// 未领取奖励
						gotAll = false;
					}
				}
				if (gotAll) {
					continue;
				}
			}
			// 主城升级领完关闭
			if (activityBase.getActivityId() == ActivityConst.ACT_LEVEL) {
				List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
				boolean gotAll = true;
				for (StaticActAward e : condList) {
					int keyId = e.getKeyId();
					if (!actRecord.getReceived().containsKey(keyId)) {// 未领取奖励
						gotAll = false;
					}
				}
				if (gotAll) {
					continue;
				}
			}
			// 建造队列大礼包
			if (activityBase.getActivityId() == ActivityConst.ACT_BUILD_QUE) {
				actBuildQue(player, builder, activityBase, cangetAward, tip);
				continue;
			}
			// 超值礼包 领取奖励后关闭
			if (activityBase.getActivityId() == ActivityConst.ACT_LUXURY_GIFT) {
				if (actRecord == null) {
					continue;
				}
				List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
				if (condList == null || condList.size() == 0) {
					continue;
				}
				boolean flag = false;
				for (StaticActAward actAward : condList) {
					if (actRecord.getReceived().containsKey(actAward.getKeyId())) {
						flag = true;
					}
				}
				if (!flag) {
					builder.addActivity(PbHelper.createActivityPb(activityBase, null, cangetAward, tip));
				}
				continue;
			}
			if (activityBase.getActivityId() == ActivityConst.ACT_SURIPRISE_GIFT) {
				suripriserGift(builder, activityBase, actRecord, cangetAward, tip);
				continue;
			}
			// 春节特惠礼包全部买完 活动消失
			if (activityBase.getActivityId() == ActivityConst.ACT_SPRING_FESTIVAL_GIFT && springGiftDisappear(actRecord)) {
				continue;
			}
			// 七日豪礼如果全部领取则不再显示,老用户也不显示
			if (activityBase.getActivityId() == ActivityConst.ACT_LOGIN_SEVEN) {
				if (player.getLord().getIsSeven() == 1) {
					continue;
				}
				List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
				if (condList == null || condList.isEmpty()) {
					continue;
				}
				Integer integer = actRecord.getReceived().values().stream().filter(x -> x == 0).findAny().orElse(null);
				if (actRecord.getReceived().size() >= condList.size() && integer == null) {
					player.getLord().setIsSeven(1);
					continue;
				}
			}
			builder.addActivity(PbHelper.createActivityPb(activityBase, null, cangetAward, tip));
		}
		handler.sendMsgToPlayer(GetActivityListRs.ext, builder.build());

		// 活动红点
	}

	private void suripriserGift(GetActivityListRs.Builder builder, ActivityBase activityBase, ActRecord actRecord, boolean cangetAward, boolean tip) {
		actRecord.checkExprie();
		if (actRecord.hasNoExprie()) {
			int num = 0;
			for (ActivityRecord record : actRecord.getActivityRecords()) {
				if (actRecord.getReceived().containsKey(record.getKey())) {
					continue;
				}
				num++;
			}
			builder.addActivity(PbHelper.createActivityPb(activityBase, new Date(actRecord.getExpireTime()), cangetAward, tip, num));
		}
	}

	// 春节特惠是否消失
	public boolean springGiftDisappear(ActRecord actRecord) {
		if (actRecord == null) {
			return true;
		}
		Map<Integer, StaticLimitGift> springGiftMap = staticActivityMgr.getSpringGiftMap(actRecord.getAwardId());
		if (springGiftMap == null || springGiftMap.isEmpty()) {
			return true;
		}
		// 可以购买的礼包数,礼包买完了消失
		long count = springGiftMap.values().stream().filter(e -> actRecord.getRecordNum(e.getKeyId()) < e.getCount()).count();
		return count <= 0;
	}

	private void actBuildQue(Player player, GetActivityListRs.Builder builder, ActivityBase activityBase, boolean cangetAward, boolean tip) {
		ActRecord actFlash = activityManager.getActivityInfo(player, ActivityConst.ACT_BUILD_QUE);
		if (actFlash == null) {
			return;
		}
		if (!actFlash.isShow()) {
			return;
		}
		int openDays = staticLimitMgr.getAddtion(254).get(2);
		Date openTime = new Date(actFlash.getBeginTime() * TimeHelper.SECOND_MS);
		Date cleanTime = new Date(openTime.getTime() + openDays * TimeHelper.DAY_MS);
		// 时间到期 结束了
		if (cleanTime.before(new Date(System.currentTimeMillis()))) {
			return;
		}
		// 限时礼包过期
		// 奖励领完了
		boolean flag = false;
		List<StaticActPayGift> payGiftList = staticActivityMgr.getPayGiftList(activityBase.getAwardId());
		if (null == payGiftList || payGiftList.size() == 0) {
			return;
		}
		for (StaticActPayGift e : payGiftList) {
			long keyId = e.getPayGiftId();
			if (actFlash.getStatus() != null && actFlash.getStatus().containsKey(keyId)) {// 已领取奖励
				flag = true;
				break;
			}
		}
		if (!flag) {
			builder.addActivity(PbHelper.createActivityPb(activityBase, openTime, cleanTime, cangetAward, tip, 0));
		}
	}

	private void actMonthGift(Player player, GetActivityListRs.Builder builder, ActivityBase activityBase, boolean cangetAward, boolean tip) {
		ActRecord actMonth = activityManager.getActivityInfo(player, ActivityConst.ACT_MONTH_GIFT);
		if (actMonth == null) {
			return;
		}

		// 限时礼包过期
		int configNum = staticLimitMgr.getNum(253);
		long timeKey = 0;
		long endTime = 0;
		if (actMonth.getStatus().containsKey(timeKey)) {
			endTime = actMonth.getStatus(timeKey);
			if (endTime <= System.currentTimeMillis()) {
				return;
			}
		}

		boolean flag = false;
		List<StaticActPayGift> payGiftList = staticActivityMgr.getPayGiftList(activityBase.getAwardId());
		if (null == payGiftList || payGiftList.size() == 0) {
			return;
		}
		for (StaticActPayGift e : payGiftList) {
			long keyId = e.getPayGiftId();
			if (actMonth.getStatus() != null && actMonth.getStatus().containsKey(keyId)) {// 已领取奖励
				flag = true;
				break;
			}
		}
		if (!flag) {
			builder.addActivity(PbHelper.createActivityPb(activityBase, new Date(endTime), cangetAward, tip));
		}
	}

	private void actFlashGift(Player player, GetActivityListRs.Builder builder, ActivityBase activityBase, boolean cangetAward, boolean tip) {
		ActRecord actFlash = activityManager.getActivityInfo(player, ActivityConst.ACT_FLASH_GIFT);
		if (actFlash == null || player.getLord().getFirstPay() != 2) {
			return;
		}

		// 限时礼包过期
		int configNum = staticLimitMgr.getNum(224);
		long endTime = actFlash.getStatus(0) + (configNum * TimeHelper.MINUTE_MS);
		if (endTime <= System.currentTimeMillis()) {
			return;
		}

		boolean flag = false;
		List<StaticActPayGift> payGiftList = staticActivityMgr.getPayGiftList(activityBase.getAwardId());
		if (null == payGiftList || payGiftList.size() == 0) {
			return;
		}
		for (StaticActPayGift e : payGiftList) {
			long keyId = e.getPayGiftId();
			if (actFlash.getStatus() != null && actFlash.getStatus().containsKey(keyId)) {// 已领取奖励
				flag = true;
				break;
			}
		}
		if (!flag) {
			builder.addActivity(PbHelper.createActivityPb(activityBase, new Date(endTime), cangetAward, tip));
		}
	}

	/**
	 * 精彩活动列表
	 *
	 * @param handler
	 */
	public void getWonderfulListRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		GetWonderfulListRs.Builder builder = GetWonderfulListRs.newBuilder();

		Date createDate = player.account.getCreateDate();
		Date now = new Date();
		List<ActivityBase> list = staticActivityMgr.getActivityList();

		// 1.活动结束,则不展示给客户端
		// 2.活动开放阶段,tip提示,可否领奖
		for (ActivityBase activityBase : list) {
			if (!activityBase.isWonderfulActivity()) {
				continue;
			}
			int step = activityBase.getStep();
			if (step != ActivityConst.ACTIVITY_BEGIN && step != ActivityConst.ACTIVITY_DISPLAY) {// 活动未开启
				continue;
			}

			ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);

			// 可领奖，不可领奖
			boolean cangetAward = activityBase.canAward();

			// 活动tips
			boolean tip = activityTips(player, cangetAward, actRecord, activityBase);

			if (activityBase.getActivityId() == ActivityConst.ACT_GROW_FOOT && step == ActivityConst.ACTIVITY_DISPLAY) {
				List<StaticActFoot> footList = staticActivityMgr.getActFoots(activityBase.getAwardId());
				if (footList == null) {
					continue;
				}
				boolean flag = true;
				for (StaticActFoot foot : footList) {
					if (null == foot) {
						continue;
					}
					int sortId = foot.getSortId();

					// 0.未购买 1-N购买后的第几天
					int state = currentActivity(player, activityManager.getActivityInfo(player, activityBase), sortId);

					if (state != 0) {
						flag = false;
					}
				}
				if (flag) {
					continue;
				}
			}
			if (activityBase.getActivityId() == ActivityConst.ACT_ZERO_GIFT && step == ActivityConst.ACTIVITY_DISPLAY) {
				List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
				boolean next = true;
				for (StaticActAward award : condList) {
					if (!actRecord.getReceived().containsKey(award.getKeyId())) {
						next = false;
					}
				}
				// 所有奖励都领完了
				if (next) {
					continue;
				}
				List<StaticActFreeBuy> footList = staticActivityMgr.getActFreeBuy(activityBase.getAwardId());
				if (footList == null) {
					continue;
				}
				boolean flag = true;
				for (StaticActFreeBuy foot : footList) {
					if (null == foot) {
						continue;
					}
					int sortId = foot.getSortId();

					// 0.未购买 1-N购买后的第几天
					int state = currentActivity(player, activityManager.getActivityInfo(player, activityBase), sortId);

					if (state != 0) {
						flag = false;
					}
				}
				if (flag) {
					continue;
				}
			}
			if (activityBase.getActivityId() == ActivityConst.ACT_SURIPRISE_GIFT) {
				actRecord.checkExprie();
				if (actRecord.hasNoExprie()) {
					int num = 0;
					for (ActivityRecord record : actRecord.getActivityRecords()) {
						if (actRecord.getReceived().containsKey(record.getKey())) {
							continue;
						}
						num++;
					}
					builder.addActivity(PbHelper.createActivityPb(activityBase, new Date(actRecord.getExpireTime()), cangetAward, tip, num));
				}
				continue;
			}
			// less大于零,活动开启,关闭时间和玩家创建账号时间相关[结束时间:创建角色时间+less天数]
			int less = activityBase.getStaticActivity().getLess();
			if (less != 0) {
				Date endTime = DateHelper.addDate(createDate, less);
				if (now.before(endTime)) {
					builder.addActivity(PbHelper.createActivityPb(activityBase, endTime, cangetAward, tip));
				}
				continue;
			}
			builder.addActivity(PbHelper.createActivityPb(activityBase, null, cangetAward, tip));
		}
		handler.sendMsgToPlayer(GetWonderfulListRs.ext, builder.build());
	}

	/**
	 * 领取奖励
	 *
	 * @param req
	 * @param handler
	 */
	public void getActivityAward(GetActivityAwardRq req, ClientHandler handler) {
		int activityId = req.getActivityId();
		int keyId = req.getKeyId();
		StaticActAward actAward = staticActivityMgr.getActAward(keyId);
		if (actAward == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		if (!activityBase.canAward()) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 活动奖励和配置对不上
		if (actRecord.getAwardId() != actAward.getAwardId()) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 奖励已领取
		if (actRecord.getReceived().containsKey(keyId) && activityId != ActivityConst.ACT_ARMS_PAY) {
			handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}

		StaticActivity staticActivity = activityBase.getStaticActivity();
		if (staticActivity.getActivityId() == ActivityConst.ACT_HIGH_VIP) {
			if (player.getLevel() < staticLimitMgr.getNum(SimpleId.ACT_HIGHT_VIP)) {
				handler.sendErrorMsgToPlayer(GameError.LORD_LV_NOT_ENOUGH);
				return;
			}
		}

		// 领取排行榜奖励
		if (staticActivity.getRank() == ActivityConst.RANK_1) {// 领取排名
			ActivityData activityData = activityManager.getActivity(activityBase);
			if (activityData == null) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
				return;
			}
			int historyRank = (int) activityData.getStatus(handler.getRoleId());
			if (historyRank <= 0 || historyRank > actAward.getCond()) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
				return;
			}
		} else if (staticActivity.getRank() == ActivityConst.RANK_2) {// 竞争排行榜奖励
			ActivityData activityData = activityManager.getActivity(activityBase);
			if (activityData == null) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
				return;
			}
			ActPlayerRank actRank = activityData.getLordRank(handler.getRoleId());
			if (actRank == null || actRank.getRank() > actAward.getCond() || actRank.getRank() <= 0) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
				return;
			}
			// 获取当前排名的奖励
			StaticActAward rankAward = staticActivityMgr.getActRankAward(actAward.getAwardId(), actRank.getRank());
			if (rankAward.getKeyId() != actAward.getKeyId()) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
				return;
			}
		} else if (staticActivity.getRank() == ActivityConst.RANK_3) {// 最后一天19:00才能领奖
			Date rewardTime = TimeHelper.getRewardTime(activityBase.getEndTime());
			Date now = new Date();
			if (now.before(rewardTime)) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
				return;
			}
			ActivityData activityData = activityManager.getActivity(activityBase);
			if (activityData == null) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
				return;
			}
			int rank = 0;
			if (activityId != ActivityConst.ACT_CAMP_MEMBERS) {
				ActPlayerRank persionRank = activityData.getLordRank(handler.getRoleId());
				if (persionRank == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
					return;
				}
				rank = persionRank.getRank();
			} else {
				CampMembersRank campMembersRank = activityData.getCampMembersRank(player);
				if (campMembersRank == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
					return;
				}
				List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(actRecord.getAwardId());
				if (displayList == null) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}
				rank = campMembersRank.getRank();
				int tempRank = activityManager.obtainAwardGear(rank, displayList);
				if (tempRank != actAward.getCond()) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
					return;
				}
			}

			if (rank > actAward.getCond() || rank == 0) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
				return;
			}
			if (staticActivity.getActivityId() == ActivityConst.ACT_MENTOR_SCORE || staticActivity.getActivityId() == ActivityConst.ACT_TOPUP_RANK || staticActivity.getActivityId() == ActivityConst.ACT_COST_GOLD) {
				StaticActAward preReward = staticActivityMgr.getActAward(keyId - 1);
				if (actAward.getCond() > 1 && preReward != null) {
					if (rank <= preReward.getCond()) {
						handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
						return;
					}
				}
			}

		} else if (staticActivity.getAddUp() == 0) {// 领取个人奖励
			if (actRecord.getActivityId() == ActivityConst.ACT_LEVEL) {
				StaticActCommand actCommand = staticActivityMgr.getActCommand(keyId);
				if (actCommand == null) {
					handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
					return;
				}
				if (player.getCommandLv() < actCommand.getLevel() || (StaticActCommand.TEC_INSTITUTE == actCommand.getLimit().get(0) && player.getTechLv() < actCommand.getLimit().get(1)) || (StaticActCommand.PLAYER_LEVEL == actCommand.getLimit().get(0) && player.getLevel() < actCommand.getLimit().get(1))) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
					return;
				}
			} else {
				int status = currentActivity(player, activityBase, actRecord, actAward);
				if (status < actAward.getCond()) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
					return;
				}
			}

		} else if (staticActivity.getAddUp() == 1) {// 领取全服累计
			ActivityData activityData = activityManager.getActivity(activityBase);
			if (activityData == null) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
				return;
			}
			int status = currentActivity(player, activityBase, activityData, actAward);
			if (status < actAward.getCond()) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
				return;
			}
		}

		GetActivityAwardRs.Builder builder = GetActivityAwardRs.newBuilder();
		List<List<Integer>> awardList = actAward.getAwardList();
		if (awardList == null || awardList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int size = awardList.size();
		if (playerManager.isEquipFull(awardList, player)) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
			return;
		}

		// 记录领取奖励记录
		if (actRecord.getReceived().containsKey(keyId)) {
			Integer recevied = actRecord.getReceived().get(keyId);
			actRecord.getReceived().put(keyId, 1 + recevied.intValue());
		} else {
			actRecord.getReceived().put(keyId, 1);
		}

		// 基金活动提前发奖励
		if (activityBase.getStaticActivity().getActivityId() == ActivityConst.ACT_GROW_FOOT && activityBase.isSendMail()) {
			sendFootMailGold(activityBase, player);
		}

		for (int i = 0; i < size; i++) {
			List<Integer> e = awardList.get(i);
			int type = e.get(0);
			int itemId = e.get(1);
			int count = e.get(2);
			if (type == AwardType.EQUIP && count > 1) {
				for (int c = 0; c < count; c++) {
					int itemkey = playerManager.addAward(player, type, itemId, 1, Reason.ACT_AWARD);
					builder.addAward(PbHelper.createAward(player, type, itemId, 1, itemkey));
				}
			} else {
				int itemkey = playerManager.addAward(player, type, itemId, count, Reason.ACT_AWARD);

				/**
				 * 活动资源产出日志埋点
				 */
				LogUser logUser = SpringUtil.getBean(LogUser.class);
				if (type == AwardType.RESOURCE) {
					logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(itemId), RoleResourceLog.OPERATE_IN, itemId, ResOperateType.ACT_AWARD_IN.getInfoType(), count, player.account.getChannel()));
					int t = 0;
					int resType = itemId;
					switch (resType) {
						case ResourceType.IRON:
							t = IronOperateType.ACT_AWARD_IN.getInfoType();
							break;
						case ResourceType.COPPER:
							t = CopperOperateType.ACT_AWARD_IN.getInfoType();
							break;
						case ResourceType.OIL:
							t = OilOperateType.ACT_AWARD_IN.getInfoType();
							break;
						case ResourceType.STONE:
							t = StoneOperateType.ACT_AWARD_IN.getInfoType();
							break;
						default:
							break;
					}
					if (t != 0) {
						logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, count, t), resType);
					}
				}

				builder.addAward(PbHelper.createAward(player, type, itemId, count, itemkey));
			}
		}
		handler.sendMsgToPlayer(GetActivityAwardRs.ext, builder.build());
		// 客户端没做缓存,只能服务器推送

		// 同步后端红点
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);

		SpringUtil.getBean(EventManager.class).join_activity(player, activityId, activityBase.getStaticActivity().getName(), activityId);
		SpringUtil.getBean(EventManager.class).complete_activity(player, activityId, activityBase.getStaticActivity().getName(), activityId, activityBase.getBeginTime(), awardList);
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(activityId).isAward(true).awardId(actAward.getAwardId()).giftName(actAward.getDesc()).roleId(player.roleId).vip(player.getVip()).costGold(actAward.getCond()).channel(player.account.getChannel()).build());
	}

	/**
	 * 主城升级
	 * <p>
	 * 基地升级
	 *
	 * @param handler
	 */
	public void actLevelRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_LEVEL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int awardId = actRecord.getAwardId();
		ActLevelRs.Builder builder = ActLevelRs.newBuilder();

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActLevelRs.ext, builder.build());
	}

	/**
	 * 攻城掠地
	 *
	 * @param handler
	 */
	public void actSceneCityRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SCENE_CITY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		ActSceneCityRs.Builder builder = ActSceneCityRs.newBuilder();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			int state = currentActivity(player, actRecord, e.getSortId());
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addCondState(PbHelper.createCondState(e, 1, state));
			} else {// 未领取奖励
				builder.addCondState(PbHelper.createCondState(e, 0, state));
			}
		}
		handler.sendMsgToPlayer(ActSceneCityRs.ext, builder.build());
	}

	/**
	 * 投资计划
	 * <p>
	 * 成长基金
	 *
	 * @param handler
	 */
	public void actInvestRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_INVEST);
		if (activityBase == null) {
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (condList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActInvestRs.Builder builder = ActInvestRs.newBuilder();

		int state = currentActivity(player, actRecord, 0);// 是否已参与投资计划
		builder.setState(state);

		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActInvestRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 投资计划
	 * <p>
	 * 成长基金
	 *
	 * @param handler
	 */
	public void doInvestRs(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_INVEST);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int state = currentActivity(player, actRecord, 0);
		if (state != 0) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_GOT);
			return;
		}

		if (player.getGold() < 1000) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// 玩家vip需大于3级才能购买
		if (player.getLord().getVip() < 3) {
			handler.sendErrorMsgToPlayer(GameError.VIP_NOT_ENOUGH);
			return;
		}
		actRecord.putState(0, 1);
		playerManager.subAward(player, AwardType.GOLD, 0, 1000, Reason.ACT_INVEST);

		DoInvestRs.Builder builder = DoInvestRs.newBuilder();
		builder.setState(1);
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(DoInvestRs.ext, builder.build());
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_INVEST);
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
		eventManager.join_activity(player, ActivityConst.ACT_INVEST, activityBase.getStaticActivity().getName(), ActivityConst.ACT_INVEST);
	}

	/**
	 * 大咖带队
	 * <p>
	 * 大V带队
	 *
	 * @param handler
	 */
	public void actHighVipRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HIGH_VIP);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		// 大咖带队
		activityManager.activityHighVip(activityKeyId, activityData);

		ActHighVipRs.Builder builder = ActHighVipRs.newBuilder();
		builder.setLevel(staticLimitMgr.getNum(SimpleId.ACT_HIGHT_VIP));

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		for (StaticActAward e : condList) {

			int state = (int) activityData.getStatus(e.getSortId());

			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addCondState(PbHelper.createCondState(e, 1, state));
			} else {// 未领取奖励
				builder.addCondState(PbHelper.createCondState(e, 0, state));
			}
		}
		handler.sendMsgToPlayer(ActHighVipRs.ext, builder.build());
	}

	/**
	 * 兵力排行榜
	 * <p>
	 * 兵力排行
	 *
	 * @param handler
	 */
	public void actSoilderRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SOILDER_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		activityManager.updActPerson(player, ActivityConst.ACT_SOILDER_RANK, 0, 0);
		ActSoilderRankRs.Builder builder = ActSoilderRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}

		handler.sendMsgToPlayer(ActSoilderRankRs.ext, builder.build());
	}

	/**
	 * 城战排行
	 *
	 * @param handler
	 */
	public void actCityRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_CITY_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		activityManager.updActPerson(player, ActivityConst.ACT_CITY_RANK, 0, 0);
		ActCityRs.Builder builder = ActCityRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}

		handler.sendMsgToPlayer(ActCityRs.ext, builder.build());
	}

	/**
	 * 充值排行
	 *
	 * @param handler
	 */
	@Deprecated
	public void actTopupRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_TOPUP_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActTopupRankRs.Builder builder = ActTopupRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank && personRank.getRankValue() != 0) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null && actRank.getRankValue() != 0) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}

		handler.sendMsgToPlayer(ActTopupRankRs.ext, builder.build());
	}

	/**
	 * 锻造排行榜
	 * <p>
	 * 军械生产排行
	 *
	 * @param handler
	 */
	public void actForgeRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_FORGE_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActForgeRankRs.Builder builder = ActForgeRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}

		handler.sendMsgToPlayer(ActForgeRankRs.ext, builder.build());
	}

	/**
	 * 国战排行
	 * <p>
	 * 阵营战排行
	 *
	 * @param handler
	 */
	public void actCountryRank(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityById = staticActivityMgr.getActivityById(ActivityConst.ACT_COUNTRY_RANK);
		if (activityById == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_COUNTRY_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		activityManager.updActPerson(player, activityById, 0, 0);
		ActCountryRankRs.Builder builder = ActCountryRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}
		handler.sendMsgToPlayer(ActCountryRankRs.ext, builder.build());
	}

	/**
	 * 炼油排行榜
	 * <p>
	 * 粮食排行
	 *
	 * @param handler
	 */
	public void actOilRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_OIL_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		activityManager.updActPerson(player, ActivityConst.ACT_OIL_RANK, 0, 0);
		ActOilRankRs.Builder builder = ActOilRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}
		handler.sendMsgToPlayer(ActOilRankRs.ext, builder.build());
	}

	/**
	 * 洗练排行榜
	 * <p>
	 * 精研排行
	 *
	 * @param handler
	 */
	public void actWashRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_WASH_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		activityManager.updActPerson(player, ActivityConst.ACT_WASH_RANK, 0, 0);
		ActWashRankRs.Builder builder = ActWashRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}
		handler.sendMsgToPlayer(ActWashRankRs.ext, builder.build());
	}

	/**
	 * 囤铁排行榜
	 * <p>
	 * 晶体排行榜
	 *
	 * @param handler
	 */
	public void actStoneRank(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_STONE_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		activityManager.updActPerson(player, ActivityConst.ACT_STONE_RANK, 0, 0);
		ActStoneRankRs.Builder builder = ActStoneRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}
		handler.sendMsgToPlayer(ActStoneRankRs.ext, builder.build());
	}

	/**
	 * 宝石转盘
	 * <p>
	 * 晶体转盘
	 *
	 * @param handler
	 */
	public void actStoneDialRq(ClientHandler handler) {

		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_STONE_DIAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		long costStone = actRecord.getStatus(0);// 已消耗宝石
		long count = actRecord.getStatus(1);// 已转次数
		int buyCount = (int) actRecord.getStatus(2);// 金币购买次数

		List<StaticActDial> dialList = staticActivityMgr.getActDialList(activityKeyId, 1);
		if (dialList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 金币购买次数
		StaticActDialStone buyActDialStone = staticActivityMgr.getActDial(buyCount + 1);
		if (buyActDialStone == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 罗盘的次数
		long[] stone = staticActivityMgr.getCostCount(costStone);
		int stoneCount = (int) stone[0];

		ActStoneDialRs.Builder builder = ActStoneDialRs.newBuilder();
		for (StaticActDial e : dialList) {
			if (e == null) {
				continue;
			}
			builder.addActDial(PbHelper.createActDial(e, 0));
		}
		long dialCount = buyCount + stoneCount - count;
		builder.setCostStone(costStone);// 总消耗宝石数
		builder.setDialCount((int) dialCount);// 剩余次数
		builder.setBuyCount(buyActDialStone.getPrice());// 购买一次罗盘的金币价格
		builder.setStoneCount(stone[1]);// 剩余多少宝石获得一次
		handler.sendMsgToPlayer(ActStoneDialRs.ext, builder.build());
	}

	/**
	 * 宝石罗盘
	 * <p>
	 * 晶体转盘
	 *
	 * @param handler
	 */
	public void doStoneDialRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_STONE_DIAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();
		DialEntity dialEntity = staticActivityMgr.getActDialMap(activityKeyId);
		if (dialEntity == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		long costStone = actRecord.getStatus(0);// 已消耗宝石
		long count = actRecord.getStatus(1);// 已转次数
		int buyCount = (int) actRecord.getStatus(2);// 金币购买次数

		// 罗盘的次数
		long[] stone = staticActivityMgr.getCostCount(costStone);

		if (stone[0] + buyCount <= count) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_COUNT);
			return;
		}

		StaticActDial dial = dialEntity.getRandomDail(1, null);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		actRecord.putState(1, count + 1);

		playerManager.addAward(player, dial.getItemType(), dial.getItemId(), dial.getItemCount(), Reason.ACT_DIAL_STONE);

		DoStoneDialRs.Builder builder = DoStoneDialRs.newBuilder();
		builder.setDialId(dial.getDialId());
		builder.setAward(PbHelper.createAward(dial.getItemType(), dial.getItemId(), dial.getItemCount()));
		handler.sendMsgToPlayer(DoStoneDialRs.ext, builder.build());
		SpringUtil.getBean(EventManager.class).spin_the_wheel(player, Lists.newArrayList(ActivityConst.ACT_STONE_DIAL, // 活动类型
			new Award(dial.getItemType(), dial.getItemId(), dial.getItemCount()),
			count));
	}

	/**
	 * 购买宝石罗盘次数
	 *
	 * @param handler
	 */
	public void buyStoneDialRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_STONE_DIAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		long costStone = actRecord.getStatus(0);// 已消耗宝石
		long count = actRecord.getStatus(1);// 已转次数
		int buyCount = (int) actRecord.getStatus(2);// 金币购买次数

		StaticActDialStone buyActDialStone = staticActivityMgr.getActDial(buyCount + 1);

		if (buyActDialStone == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 金币不足
		if (buyActDialStone.getPrice() > player.getGold()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		actRecord.putState(2, buyCount + 1);

		playerManager.subGoldOk(player, buyActDialStone.getPrice(), Reason.BUY_STONE_DIAL);

		// 下一次购买价格
		StaticActDialStone nextBuy = staticActivityMgr.getActDial(buyCount + 2);

		// 当前已消耗宝石,获得的总次数
		StaticActDialStone costActDialStone = staticActivityMgr.getStoneCount(costStone);
		int stoneCount = costActDialStone == null ? 0 : costActDialStone.getCount();
		long dialCount = buyCount + 1 + stoneCount - count;

		BuyStoneDialRs.Builder builder = BuyStoneDialRs.newBuilder();
		builder.setGold(player.getGold());
		builder.setBuyCount(nextBuy.getPrice());
		builder.setDialCount((int) dialCount);
		handler.sendMsgToPlayer(BuyStoneDialRs.ext, builder.build());
	}

	/**
	 * 七日登录
	 *
	 * @param handler
	 */
	public void actSevenLoginRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_LOGIN_SEVEN);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int awardId = actRecord.getAwardId();

		ActSevenLoginRs.Builder builder = ActSevenLoginRs.newBuilder();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		// int state = currentActivity(player, actRecord, 0);
		if (actRecord.getCount() == 0) {
			actRecord.setCount(1);
		}
		builder.setState(Math.min(actRecord.getCount(), 7));
		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActSevenLoginRs.ext, builder.build());
	}

	static final long CD_TIME = 6 * 3600 * 1000L;
	// static final long CD_TIME = 10* 1000L;

	/**
	 * 七星拜将 绝版英雄
	 *
	 * @param
	 */
	public void actHeroKowtow(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HERO_KOWTOW);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		Date date = new Date();
		long key = actRecord.getActivityId();
		if (!actRecord.getStatus().containsKey(key)) {
			actRecord.putState(key, System.currentTimeMillis());
		}
		date.setTime(actRecord.getStatus(key));
		if (DateHelper.dayiy(date, new Date()) > 1) {
			actRecord.putState(key, System.currentTimeMillis());
		}

		int activityKeyId = actRecord.getAwardId();

		long recordTime = actRecord.getStatus(1);// 记录时间(毫秒)
		int buyCount = (int) actRecord.getStatus(2);// 购买次数
		int goldRefresh = (int) actRecord.getStatus(3);// 金币刷新次数
		ActHeroKowtowRs.Builder builder = ActHeroKowtowRs.newBuilder();

		// 几次时间在原有时间上加CD
		// 剩余次数值公式:(sysTime+4*CD-recordTime)/CD
		// 剩余时间公式 (recordTime-sysTime)%CD
		long systime = System.currentTimeMillis();

		// [10,2,200,6,4]
		// 绝版英雄参数配置【钻石刷新的初始消耗数量，钻石刷新根据次数增加的消耗数量，钻石刷新消耗的数量上限，免费次数恢复时间间隔(小时)，免费次数累积上限】
		List<Integer> addtion = staticLimitMgr.getAddtion(199);
		if (null == addtion || addtion.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		// long refresh = (systime + 4 * CD_TIME - recordTime) / CD_TIME;
		long refresh = (systime + addtion.get(4) * CD_TIME - recordTime) / CD_TIME;
		refresh = refresh > addtion.get(4) ? addtion.get(4) : refresh;
		if (refresh < addtion.get(4)) {
			long reless = (recordTime - systime) % CD_TIME;
			reless = reless == 0 ? CD_TIME : reless;
			long refreshTime = reless + systime;
			builder.setRefreshTime(refreshTime);
		}

		// 已有信物个数
		int state = currentActivity(player, actRecord, 0);
		builder.setState(state);
		builder.setRefresh((int) refresh);// 剩余刷新次数
		builder.setBuyCount(buyCount);
		builder.setGoldRefresh(goldRefresh);

		//
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			// 获取下进度
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.setActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.setActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}

		staticActivityMgr.initShop(actRecord, activityKeyId, state);

		// 购买商店
		Iterator<ActShopProp> it = actRecord.getShops().values().iterator();
		while (it.hasNext()) {
			ActShopProp next = it.next();
			if (next == null) {
				continue;
			}
			builder.addActShop(PbHelper.createActShop(next));
		}
		handler.sendMsgToPlayer(ActHeroKowtowRs.ext, builder.build());
	}

	/**
	 * 购买七星拜将道具
	 *
	 * @param handler
	 */
	public void buyHeroKowtowRq(BuyHeroKowtowRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HERO_KOWTOW);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int grid = req.getGrid();
		int activityKeyId = actRecord.getAwardId();

		StaticActShop staticActShop = staticActivityMgr.getActShop(activityKeyId, grid);
		if (staticActShop == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActShopProp actProp = actRecord.getShops().get(grid);
		if (actProp == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 不可购买
		if (actProp.getIsBuy() == 2) {// 不可购买
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// [10,2,200,6,4]
		// 绝版英雄参数配置【钻石刷新的初始消耗数量，钻石刷新根据次数增加的消耗数量，钻石刷新消耗的数量上限，免费次数恢复时间间隔(小时)，免费次数累积上限】
		List<Integer> addtion = staticLimitMgr.getAddtion(199);
		int buyCount = (int) actRecord.getStatus(2);// 购买次数
		int discount = buyCount > 5 ? 5 : buyCount;
		// int price = actProp.getPrice() - actProp.getPrice() * discount / 10;
		int price = actProp.getPrice() - actProp.getPrice() * discount / addtion.get(0);

		// 金币不足
		if (player.getGold() < price) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		// 已有信物个数
		int state = currentActivity(player, actRecord, 0);

		BuyHeroKowtowRs.Builder builder = BuyHeroKowtowRs.newBuilder();

		// 如果购买的是信物
		if (actProp.getPropId() == Integer.valueOf(staticActShop.getParam())) {
			if (state >= 7) {
				handler.sendErrorMsgToPlayer(GameError.CANT_BUY);
				return;
			}
			actRecord.putState(0, state + 1);// 信物数量+1
			builder.setState(state + 1);
		} else {
			int keyId = playerManager.addAward(player, AwardType.PROP, actProp.getPropId(), actProp.getPropNum(), Reason.ACT_KONTOW_HERO);
			builder.setAward(PbHelper.createAward(player, AwardType.PROP, actProp.getPropId(), actProp.getPropNum(), keyId));
		}

		// 购买次数+1
		actProp.setIsBuy(2);
		actRecord.putState(2, buyCount + 1);// 购买次数+1

		playerManager.subAward(player, AwardType.GOLD, 0, price, Reason.ACT_KONTOW_HERO);

		builder.setGold(player.getGold());
		builder.setBuyCount(buyCount + 1);
		handler.sendMsgToPlayer(BuyHeroKowtowRs.ext, builder.build());

	}

	/**
	 * 七星拜将商店刷新
	 *
	 * @param handler
	 */
	public void refreshHeroKowtow(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HERO_KOWTOW);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int activityKeyId = actRecord.getAwardId();

		int state = currentActivity(player, actRecord, 0);

		RefreshHeroKowtowRs.Builder builder = RefreshHeroKowtowRs.newBuilder();

		long recordTime = (actRecord.getStatus(1));// 刷新记录时间
		int goldRefresh = (int) actRecord.getStatus(3);// 金币刷新次数

		// 几次时间在原有时间上加CD
		// 剩余次数值公式:(sysTime+4*CD-recordTime)/CD
		// 剩余时间公式 (recordTime-sysTime)%CD

		// [10,2,200,6,4]
		// 绝版英雄参数配置【钻石刷新的初始消耗数量，钻石刷新根据次数增加的消耗数量，钻石刷新消耗的数量上限，免费次数恢复时间间隔(小时)，免费次数累积上限】
		List<Integer> addtion = staticLimitMgr.getAddtion(199);
		long systime = System.currentTimeMillis();
		// long refresh = (systime + 4 * CD_TIME - recordTime) / CD_TIME;
		long refresh = (systime + addtion.get(4) * CD_TIME - recordTime) / CD_TIME;
		refresh = refresh > addtion.get(4) ? addtion.get(4) : refresh;

		if (refresh <= 0) {// 没有免费次数
			int needGold = goldRefresh * addtion.get(1) + addtion.get(0) > addtion.get(2) ? addtion.get(2) : goldRefresh * addtion.get(1) + addtion.get(0);
			// int needGold = goldRefresh * 2 + 10;
			if (player.getGold() < needGold) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subGoldOk(player, needGold, Reason.REFRESH_KONTOW_REFRESH);
			actRecord.putState(3, goldRefresh + 1);
			builder.setGoldRefresh(goldRefresh + 1);

			builder.setGold(player.getGold());
		} else {
			// 系统时间
			long sysTime = System.currentTimeMillis();
			if (recordTime <= sysTime) {
				recordTime = System.currentTimeMillis() + CD_TIME;
			} else {
				recordTime = recordTime + CD_TIME;
			}
			// 扣除次数
			actRecord.putState(1, recordTime);
			refresh = refresh - 1;
		}

		builder.setRefresh((int) (refresh));

		long reless = (recordTime - systime) % CD_TIME;
		reless = reless == 0 ? CD_TIME : reless;
		long refreshTime = reless + systime;
		builder.setRefreshTime(refreshTime);
		actRecord.putState(2, 0);

		staticActivityMgr.refreshShop(actRecord, activityKeyId, state);

		Iterator<ActShopProp> it = actRecord.getShops().values().iterator();
		while (it.hasNext()) {
			ActShopProp actProp = it.next();
			if (null == actProp) {
				continue;
			}
			builder.addActShop(PbHelper.createActShop(actProp));
		}
		handler.sendMsgToPlayer(RefreshHeroKowtowRs.ext, builder.build());
	}

	/**
	 * 强国策
	 * <p>
	 * 阵营崛起
	 *
	 * @param handler
	 */
	public void actLowCountryRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_LOW_COUNTRY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int awardId = actRecord.getAwardId();
		ActLowCountryRs.Builder builder = ActLowCountryRs.newBuilder();

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActLowCountryRs.ext, builder.build());
	}

	/**
	 * 全服返利
	 *
	 * @param handler
	 */
	public void actSerPayRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SER_PAY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (condList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActSerPayRs.Builder builder = ActSerPayRs.newBuilder();

		builder.setWu((int) activityData.getAddtion(1));
		builder.setShu((int) activityData.getAddtion(2));
		builder.setWei((int) activityData.getAddtion(3));

		int state = currentActivity(player, activityData, 0);
		builder.setState(state);
		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			if (e.getCond() != 0) {
				int keyId = e.getKeyId();
				if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
					builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
				} else {// 未领取奖励
					builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
				}
			}

		}
		handler.sendMsgToPlayer(ActSerPayRs.ext, builder.build());
	}

	/**
	 * 全服返利邮件奖励
	 */
	public void sendSerPayOffAward(ActivityBase activityBase, ActivityData activityData) {
		long countryScore = 0;
		Map<Integer, Integer> countryTops = new HashMap<>();
		for (int i = 1; i <= 3; i++) {
			long temp = activityData.getAddtion(i);
			if (temp == 0) {
				continue;
			}
			if (temp > countryScore) {
				countryScore = temp;
				countryTops.clear();
				countryTops.put(i, i);
			} else if (temp == countryScore) {
				countryTops.put(i, i);
			}
		}
		if (countryTops.isEmpty()) {// 没有国家参与,则不发放奖励
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityData.getAwardId());
		if (condList == null) {
			return;
		}
		int key = 0;
		List<CommonPb.Award> awardList = new ArrayList<>();
		for (StaticActAward e : condList) {
			if (e.getCond() == 0) {
				awardList = e.getAwardPbList();
				key = e.getKeyId();
			}
		}
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (countryTops.containsKey(next.getCountry()) && !actRecord.getReceived().containsKey(key)) {
				if (!awardList.isEmpty()) {
					actRecord.getReceived().put(key, 1);
					playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.SER_PAY_AWARD, activityBase.getStaticActivity().getName());
				}
			}
		}

		iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			List<CommonPb.Award> awards = new ArrayList<>();
			// 已达成条件,未领取奖励,补发奖励邮件
			for (StaticActAward e : condList) {
				if (e.getCond() == 0) {
					continue;
				}
				int cond = currentActivity(next, activityData, e.getSortId());
				if (!actRecord.getReceived().containsKey(e.getKeyId()) && cond != 0 && cond >= e.getCond()) {
					actRecord.getReceived().put(e.getKeyId(), 1);
					awards.addAll(e.getAwardPbList());

				}
			}
			if (!awards.isEmpty()) {
				playerManager.sendAttachMail(next, PbHelper.finilAward(awards), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}

		}

	}

	/**
	 * 首充礼包
	 * <p>
	 * 首次充值送豪礼
	 *
	 * @param handler
	 */
	public void actPayFirstRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActPayFirstRs.Builder builder = ActPayFirstRs.newBuilder();
		builder.setState(player.getLord().getFirstPay());

		int actMold = serverManager.getServer().getActMold();
		StaticActFirstPay actFirstPay = staticActivityMgr.getStaticActFirstPay(actMold);
		if (actFirstPay == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		CommonPb.ActivityCond.Builder activityPb = CommonPb.ActivityCond.newBuilder();
		activityPb.setKeyId(1);
		activityPb.setCond(1);
		if (player.getLord().getFirstPay() == 2) {
			activityPb.setIsAward(1);
		} else {
			activityPb.setIsAward(0);
		}

		List<List<Integer>> awardList = actFirstPay.getAwardList();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			activityPb.addAward(PbHelper.createAward(type, id, count));
		}
		builder.setActivityCond(activityPb.build());
		handler.sendMsgToPlayer(ActPayFirstRs.ext, builder.build());
	}

	/**
	 * 领取首充礼包
	 *
	 * @param handler
	 */
	public void doActPayFirstRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int firstPay = player.getLord().getFirstPay();

		// 活动未完成
		if (firstPay != 1) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}

		int actMold = serverManager.getServer().getActMold();
		StaticActFirstPay actFirstPay = staticActivityMgr.getStaticActFirstPay(actMold);
		if (actFirstPay == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int equipCount = 0;

		List<List<Integer>> awardList = actFirstPay.getAwardList();
		for (List<Integer> e : awardList) {
			if (e == null) {
				continue;
			}
			int type = e.get(0);
			int count = e.get(2);
			if (type == AwardType.EQUIP && count > 0) {
				equipCount += count;
			}
		}
		if (equipManager.getFreeSlot(player) < equipCount) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_EQUIP_SLOT);
			return;
		}
		DoActPayFirstRs.Builder builder = DoActPayFirstRs.newBuilder();
		player.getLord().setFirstPay(2);
		for (List<Integer> e : awardList) {
			if (e == null) {
				continue;
			}
			int type = e.get(0);
			int itemId = e.get(1);
			int count = e.get(2);
			if (type == AwardType.EQUIP && count > 0) {
				for (int c = 0; c < count; c++) {
					int itemkey = playerManager.addAward(player, type, itemId, 1, Reason.FIRST_PAY);
					builder.addAward(PbHelper.createAward(player, type, itemId, 1, itemkey));
				}
			} else {
				int itemkey = playerManager.addAward(player, type, itemId, count, Reason.FIRST_PAY);
				builder.addAward(PbHelper.createAward(player, type, itemId, count, itemkey));
			}
		}

		handler.sendMsgToPlayer(DoActPayFirstRs.ext, builder.build());

		// 开启限时礼包
		ActRecord act = activityManager.getActivityInfo(player, ActivityConst.ACT_FLASH_GIFT);
		if (null != act) {
			long currentTime = System.currentTimeMillis();
			long timeKey = 0;
			if (!act.getStatus().containsKey(timeKey)) {
				act.putState(timeKey, currentTime);
			}
		}
		chatManager.sendVipMsg(ChatId.FIRST_PAY, player.getNick());
		surpriseGiftManager.doSurpriseGift(player, SuripriseId.FirstCharge, 1, true);
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_PAY_FIRST);
		String name = "";
		Date time = new Date();
		if (activityBase != null && activityBase.getStaticActivity() != null) {
			name = activityBase.getStaticActivity().getName();
			time = activityBase.getBeginTime();
		}

		ActivityEventManager.getInst().activityTip(player, act, activityBase);
		SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_PAY_FIRST, name, ActivityConst.ACT_PAY_FIRST);
		SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_PAY_FIRST, name, ActivityConst.ACT_PAY_FIRST, time, awardList);
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(ActivityConst.ACT_PAY_FIRST).isAward(true).awardId(actFirstPay.getFirstPay()).giftName(name).roleId(player.roleId).vip(player.getVip()).costGold(0).channel(player.account.getChannel()).build());
	}

	/**
	 * @param handler
	 */
	public void actBuyGiftRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_BUY_GIFT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 清理数据
		activityManager.refreshDay(actRecord);

		int activityKeyId = actRecord.getAwardId();
		List<StaticActQuota> quotaList = staticActivityMgr.getQuotaList(activityKeyId);
		if (quotaList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActBuyGiftRs.Builder builder = ActBuyGiftRs.newBuilder();
		for (StaticActQuota e : quotaList) {
			if (e == null) {
				continue;
			}
			int quotaId = e.getQuotaId();
			Integer status = actRecord.getReceived().get(quotaId);
			if (status == null) {
				status = 0;
			}
			builder.addQuota(PbHelper.createQuotaPb(e, status));
		}
		handler.sendMsgToPlayer(ActBuyGiftRs.ext, builder.build());
	}

	/**
	 * 折扣购买
	 * <p>
	 * 限时折扣
	 *
	 * @param handler
	 */
	public void doQuotaRq(DoQuotaRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Lord lord = player.getLord();
		if (lord == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int activityId = req.getActivityId();
		if (activityId == 0) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		ActRecord activity = activityManager.getActivityInfo(player, activityId);
		if (activity == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 每天刷新{cleanTime最后一次}
		activityManager.refreshDay(activity);
		int quotaId = req.getQuotaId();

		StaticActQuota staticActQuota = staticActivityMgr.getQuotaById(quotaId);
		if (staticActQuota == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		if (lord.getGold() < staticActQuota.getPrice()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		Integer status = activity.getReceived().get(quotaId);
		if (status == null) {
			status = 0;
		}
		if (status >= staticActQuota.getCount()) {
			handler.sendErrorMsgToPlayer(GameError.COUNT_NOT_ENOUGH);
			return;
		}

		activity.getReceived().put(quotaId, status + 1);
		playerManager.subAward(player, AwardType.GOLD, 0, staticActQuota.getPrice(), Reason.ACT_QUOTA);
		DoQuotaRs.Builder builder = DoQuotaRs.newBuilder();
		List<List<Integer>> awardList = staticActQuota.getAwardList();
		for (List<Integer> e : awardList) {
			if (e.size() != 3) {
				continue;
			}
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			int keyId = playerManager.addAward(player, type, id, count, Reason.ACT_QUOTA);
			builder.addAward(PbHelper.createAward(player, type, id, count, keyId));
		}
		builder.setGold(lord.getGold());
		handler.sendMsgToPlayer(DoQuotaRs.ext, builder.build());
	}

	/**
	 * 罗盘
	 * <p>
	 * 幸运转盘
	 *
	 * @param handler
	 */
	public void actLuckDialRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DIAL_LUCK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int vip = player.getVip();

		int activityKeyId = actRecord.getAwardId();

		StaticActDialLuck common = staticActivityMgr.getDialLuck(activityKeyId, 1, vip);
		StaticActDialLuck honor = staticActivityMgr.getDialLuck(activityKeyId, 2, vip);

		if (common == null || honor == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		DialEntity dial = staticActivityMgr.getActDialMap(activityKeyId);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int itemId = staticLimitMgr.getNum(194);

		int id = 31;
		List<StaticActDial> actDialList = dial.getActDialList(2);
		for (StaticActDial actDial : actDialList) {
			if (actDial.getEquipId() != 0) {
				id = actDial.getEquipId();
				itemId = actDial.getItemId();
			}
		}

		ActLuckDialRs.Builder builder = ActLuckDialRs.newBuilder();

		int commonReceived = 0;

		Iterator<List<StaticActDial>> it = dial.getActDails().values().iterator();
		while (it.hasNext()) {
			List<StaticActDial> dialList = it.next();
			for (StaticActDial e : dialList) {
				if (actRecord.getReceived().containsKey(e.getDialId())) {// 判定该物品是否已获取
					builder.addActDial(PbHelper.createActDial(e, 1));
					if (e.getType() == 1) {
						commonReceived++;
					}
					continue;
				} else if (e.getLimit() > 0 && e.getItemType() == AwardType.ICON) {// 判断玩家身上有没有该头像
					if (player.hasIcon(e.getItemId())) {
						actRecord.putRecord(e.getKeyId(), 1);// 已有该头像
						builder.addActDial(PbHelper.createActDial(e, 1));
						continue;
					}
				} else if (e.getLimit() > 0 && e.getItemType() == AwardType.PROP) {// 判定该道具
					int itemCount = player.getItemNum(e.getItemId());
					int record = actRecord.getRecord(e.getKeyId());
					if (record < itemCount) {
						actRecord.putRecord(e.getKeyId(), itemCount);// 当前已有多少合成物品碎片
					}

					boolean flag = actRecord.getRecord().get(id) != null && actRecord.getRecord().get(id) > 0 ? true : false;// 判断是否已经获取电磁步枪
					if (record >= e.getLimit() || itemCount >= e.getLimit() || flag) {
						builder.addActDial(PbHelper.createActDial(e, 1));
						continue;
					}
				}
				builder.addActDial(PbHelper.createActDial(e, 0));
			}
		}
		boolean flag = actRecord.getRecord().get(id) != null && actRecord.getRecord().get(id) > 0 ? true : false;
		builder.setIsCompound(flag);
		int count = (int) actRecord.getStatus(1);// 已转次数
		int freeCount = common.getFree() - count < 0 ? 0 : common.getFree() - count;

		builder.setFree(freeCount);
		builder.setPrice(common.getPrice());
		builder.setTenPrice(common.getTenPrice());
		if (commonReceived >= dial.getDailCount(1)) {
			builder.setHonorOpen(true);
		} else {
			builder.setHonorOpen(false);
		}

		builder.setHonorPrice(honor.getPrice());
		builder.setHonorTenPrice(honor.getTenPrice());
		Map<Integer, Integer> record = actRecord.getRecord();
		// Integer currentNum = record.get(173);
		Integer currentNum = record.get(itemId);

		if (currentNum == null) {
			currentNum = 0;
		}

		int itemNum = player.getItemNum(itemId);
		builder.setEquipPiece(currentNum + itemNum);
		handler.sendMsgToPlayer(ActLuckDialRs.ext, builder.build());
	}

	/**
	 * 幸运罗盘&至尊罗盘
	 * <p>
	 * 逻辑分开: 幸运转盘、至尊转盘
	 */
	public void doLuckDialRq(DoLuckDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int type = req.getType();
		if (type == 1) {
			doCommonLuck(req, handler);
		} else if (type == 2) {
			doVipLuck(req, handler);
		}
	}

	// 幸运转盘
	public void doCommonLuck(DoLuckDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int type = req.getType();
		int count = req.getCount();

		// 检查次数
		if (count < 1 || count > 10) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 获取活动记录
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DIAL_LUCK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 找到活动Id
		int activityKeyId = actRecord.getAwardId();
		StaticActDialLuck dial = staticActivityMgr.getDialLuck(activityKeyId, type, player.getVip());
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 活动结构
		DialEntity dialEntity = staticActivityMgr.getActDialMap(activityKeyId);
		if (dialEntity == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 单次转盘总次数
		long dialCount = actRecord.getStatus(type);
		// 单抽，如果免费次数没了，判断金币
		int costGold = 0;
		if (count >= 1 && count < 10 && dial.getFree() <= dialCount) {
			if (player.getGold() < dial.getPrice() * count) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, dial.getPrice() * count, Reason.ACT_DIAL_LUCK);
			costGold = dial.getPrice() * count;
		} else if (count == 10) {
			if (player.getGold() < dial.getTenPrice()) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, dial.getTenPrice(), Reason.ACT_DIAL_LUCK);
			costGold = dial.getTenPrice();
		}

		// 普通转盘
		if (count >= 1 && count <= 10) {
			actRecord.putState(type, dialCount + count);
		}

		DoLuckDialRs.Builder builder = DoLuckDialRs.newBuilder();
		for (int i = 0; i < count; i++) {
			StaticActDial actDial = dialEntity.getRandomDail(type, actRecord.getRecord());
			actRecord.getReceived().put(actDial.getDialId(), 1);
			playerManager.addAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), Reason.ACT_DIAL_LUCK);
			builder.addAward(PbHelper.createAward(actDial.getItemType(), actDial.getItemId(), actDial.getItemCount()));
			builder.addPlace(actDial.getPlace());
			if (actDial.getKeyId() != 0) {
				actRecord.addRecord(actDial.getKeyId(), 1);
			}
		}

		// 如果获得的物品数量都达到了, 开启至尊转盘
		if (actRecord.getReceived().size() >= dialEntity.getDailCount(1)) {
			builder.setHonorOpen(true);
		}

		builder.setGold(player.getGold());
		Map<Integer, Integer> record = actRecord.getRecord();
		int itemId = staticLimitMgr.getNum(194);
		List<StaticActDial> actDialList = dialEntity.getActDialList(2);
		for (StaticActDial actDial : actDialList) {
			if (actDial.getEquipId() != 0) {
				itemId = actDial.getItemId();
			}
		}

		Integer currentNum = record.get(itemId);

		if (currentNum == null) {
			currentNum = 0;
		}

		builder.setEquipPiece(currentNum);
		handler.sendMsgToPlayer(DoLuckDialRs.ext, builder.build());
		SpringUtil.getBean(EventManager.class).spin_the_wheel(player, Lists.newArrayList(ActivityConst.ACT_DIAL_LUCK, // 活动类型
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									builder.getAwardList().toString(),
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									count));
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(ActivityConst.ACT_DIAL_LUCK).isAward(false).awardId(0).giftName("").roleId(player.roleId).vip(player.getVip()).costGold(costGold).channel(player.account.getChannel()).build());
	}

	// 至尊转盘
	public void doVipLuck(DoLuckDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int type = req.getType();
		int count = req.getCount();
		if (count != 1 && count != 10) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DIAL_LUCK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();
		StaticActDialLuck dial = staticActivityMgr.getDialLuck(activityKeyId, type, player.getVip());
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		DialEntity dialEntity = staticActivityMgr.getActDialMap(activityKeyId);
		if (dialEntity == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 至尊罗盘,需要普通罗盘全部转满方可开启
		long received = actRecord.getReceived().size();
		if (received < dialEntity.getDailCount(1)) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 单次转盘总次数
		long dialCount = actRecord.getStatus(type);
		// 1.单抽没有免费次数，或者免费次数已用完，则扣除金币
		// 2.10抽只扣金币
		// 3.单抽取一次记录一次次数
		// 4.普通抽取,每抽到一个道具需要记录已获取,用于开启至尊转盘
		int costGold = 0;
		if (count == 1 && dial.getFree() <= dialCount) {
			if (player.getGold() < dial.getPrice()) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, dial.getPrice(), Reason.ACT_DIAL_LUCK);
			costGold = dial.getPrice();
		} else if (count == 10) {
			if (player.getGold() < dial.getTenPrice()) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, dial.getTenPrice(), Reason.ACT_DIAL_LUCK);
			costGold = dial.getTenPrice();
		}

		// 普通转盘
		if (count == 1) {
			actRecord.putState(type, dialCount + 1);
		}

		DoLuckDialRs.Builder builder = DoLuckDialRs.newBuilder();

		if (count == 1) {
			randomProps(dialEntity, type, actRecord, player, builder, 0);
		} else {
			// 前4次十连默认6个
			int randomEquip = 0;
			// 十连次数
			Integer tenCompanies = actRecord.getRecord(-1);
			if (tenCompanies == null) {
				tenCompanies = 0;
			}
			tenCompanies++;
			actRecord.getRecord().put(-1, tenCompanies);
			if (tenCompanies == 4) {
				// 判断下转了几个了
				List<StaticActDial> list = dialEntity.getActDails().get(type);
				int c = 0;
				for (StaticActDial actDial : list) {
					if (actDial.getLimit() > 0) {// 特殊道具限制
						int value = actRecord.getRecord(actDial.getItemId());
						if (value < 4) {
							randomEquip = 4 - value;
						}
					}
				}
			} else if (tenCompanies == 8) {
				// 判断下转了几个了
				List<StaticActDial> list = dialEntity.getActDails().get(type);
				int c = 0;
				for (StaticActDial actDial : list) {
					if (actDial.getLimit() > 0) {// 特殊道具限制
						int value = actRecord.getRecord(actDial.getItemId());
						if (value < 8) {
							randomEquip = 8 - value;
						}
					}
				}
			}

			// 10连 判定下之前抽了几次10连
			for (int i = 0; i < count; i++) {
				randomProps(dialEntity, type, actRecord, player, builder, randomEquip);
				randomEquip--;
				if (randomEquip <= 0) {
					randomEquip = 0;
				}
			}
		}

		builder.setGold(player.getGold());
		builder.setIsCompound(false);
		Map<Integer, Integer> record = actRecord.getRecord();

		// Integer currentNum = record.get(173);
		int itmeId = staticLimitMgr.getNum(194);// 电磁步枪碎片ID
		if (itmeId == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		int id = 31;
		List<StaticActDial> actDialList = dialEntity.getActDialList(2);
		for (StaticActDial actDial : actDialList) {
			if (actDial.getEquipId() != 0) {
				id = actDial.getEquipId();
				itmeId = actDial.getItemId();
			}
		}
		Integer currentNum = record.get(itmeId);
		if (currentNum == null) {
			currentNum = 0;
		}

		/**
		 * 合成装备 31为电磁步枪
		 */
		boolean flag = actRecord.getRecord().get(id) != null && actRecord.getRecord().get(id) > 0 ? true : false;
		builder.setIsCompound(flag);
		if (!flag && currentNum == 8) {
			actRecord.getRecord().put(itmeId, 0);
			currentNum = 0;
			actRecord.getRecord().put(id, 1);
			if (equipManager.getFreeSlot(player) < 1) {
				List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();
				awardList.add(PbHelper.createAward(AwardType.EQUIP, id, 1).build());
				playerManager.sendAttachPbMail(player, awardList, MailId.GRID_IS_FULL, player.getNick(), String.valueOf(Reason.ACT_DIAL_LUCK));
			} else {
				int equipKeyId = playerManager.addAward(player, AwardType.EQUIP, id, 1, Reason.ACT_DIAL_LUCK);
				builder.setComAward(PbHelper.createAward(player, AwardType.EQUIP, id, 1, equipKeyId));
			}
			builder.setIsCompound(true);
		}

		builder.setEquipPiece(currentNum);
		handler.sendMsgToPlayer(DoLuckDialRs.ext, builder.build());

		SpringUtil.getBean(EventManager.class).spin_the_wheel(player, Lists.newArrayList(ActivityConst.ACT_DIAL_LUCK, // 活动类型
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									builder.getAwardList().toString(),
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									count));
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(ActivityConst.ACT_DIAL_LUCK).isAward(true).awardId(0).giftName("").roleId(player.roleId).vip(player.getVip()).costGold(costGold).channel(player.account.getChannel()).build());
	}

	/**
	 * 至尊转盘随机奖励
	 *
	 * @param dialEntity
	 * @param type
	 * @param actRecord
	 * @param player
	 * @param builder
	 */
	private void randomProps(DialEntity dialEntity, int type, ActRecord actRecord, Player player, DoLuckDialRs.Builder builder, int randomEquip) {
		StaticActDial actDial = dialEntity.getLuckRandomDail(type, actRecord.getRecord(), randomEquip);
		// type = 2的也记录道具个数
		if (actDial.getLimit() > 0) {
			int itemId = actDial.getItemId();
			Integer currentNum = actRecord.getRecord().get(itemId);
			if (currentNum == null) {
				actRecord.getRecord().put(itemId, 1);
			} else {
				actRecord.getRecord().put(itemId, 1 + currentNum);
			}
		}
		// TODO jyb 解决至尊转盘获得物品高亮提示
		actRecord.getReceived().put(actDial.getDialId(), 1);

		// TODO caobin 没有限制的物品进入背包
		if (actDial.getLimit() == 0) {
			playerManager.addAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), Reason.ACT_DIAL_LUCK);
		}
		builder.addAward(PbHelper.createAward(actDial.getItemType(), actDial.getItemId(), actDial.getItemCount()));
		builder.addPlace(actDial.getPlace());
		if (actDial.getKeyId() != 0) {
			actRecord.addRecord(actDial.getKeyId(), 1);
		}
	}

	/**
	 * 幸运转盘结束后碎片未合成,补发奖励
	 */
	public void sendLuckOffAward(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();

			int itmeId = staticLimitMgr.getNum(194);
			DialEntity dialEntity = staticActivityMgr.getActDialMap(ActivityConst.ACT_DIAL_LUCK);
			if (dialEntity == null) {
				return;
			}
			int id = 31;
			List<StaticActDial> actDialList = dialEntity.getActDialList(2);
			for (StaticActDial actDial : actDialList) {
				if (actDial.getEquipId() != 0) {
					id = actDial.getEquipId();
					itmeId = actDial.getItemId();
				}
			}
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (null != actRecord && actRecord.getRecord(id) == 0 && actRecord.getRecord(itmeId) > 0) {
				List<Integer> addtion = staticLimitMgr.getAddtion(200);
				int record = actRecord.getRecord(itmeId);

				List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();
				awardList.add(0, PbHelper.createAward(addtion.get(0), addtion.get(1), addtion.get(2) * record).build());
				if (!awardList.isEmpty()) {
					playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
				}
				actRecord.getRecord().put(itmeId, 0);
			}
		}
	}

	/**
	 * 雪夜甄姬
	 *
	 * @param handler
	 */
	public void actzhenjiIconRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ZHENJI_ICON);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		// 活动任务
		List<StaticActTask> actTaskList = staticActivityMgr.getActTasks(activityKeyId);
		if (actTaskList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 活动奖励
		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityKeyId);
		if (actAwardList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActzhenjiIconRs.Builder builder = ActzhenjiIconRs.newBuilder();
		for (StaticActTask e : actTaskList) {
			if (e == null) {
				continue;
			}
			int taskId = e.getTaskId();
			int state = (int) actRecord.getStatus(taskId);
			builder.addActTask(PbHelper.createActTask(e, state));
		}

		for (StaticActAward e : actAwardList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.setActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.setActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActzhenjiIconRs.ext, builder.build());
	}

	/**
	 * 每日优惠
	 *
	 * @param handler
	 */
	public void actDayPayRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DAY_PAY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		int awardId = actRecord.getAwardId();
		ActDayPayRs.Builder builder = ActDayPayRs.newBuilder();

		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.setActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.setActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		List<StaticActPayMoney> payMoneyList = staticActivityMgr.getPayMoneyList(awardId);
		if (payMoneyList == null || payMoneyList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		/*
		 * List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId); // builder.setState(0);
		 *
		 * for (StaticActAward e : condList) { int keyId = e.getKeyId(); if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励 builder.addActivityCond(PbHelper.createActivityCondPb(e, 1)); } else {// 未领取奖励 builder.addActivityCond(PbHelper.createActivityCondPb(e, 0)); } }
		 */

		for (StaticActPayMoney e : payMoneyList) {
			CommonPb.PayItem.Builder payItem = PayItem.newBuilder();

			int keyId = e.getPayMoneyId();
			int buyState = actRecord.getReceived(keyId);
			if (buyState >= e.getLimit()) {
				continue;
			}
			payItem.setPayId(e.getPayMoneyId());
			payItem.setState(buyState);
			payItem.setName(e.getName());
			payItem.setPrice(e.getMoney());
			List<List<Integer>> sellList = e.getSellList();
			if (sellList == null || sellList.size() == 0) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}
			if (sellList.size() > 0) {
				for (List<Integer> sell : sellList) {
					int type = sell.get(0);
					int id = sell.get(1);
					int count = sell.get(2);
					// 钻石奖励挪出来
					if (type == AwardType.GOLD) {
						payItem.setGold(count);
						continue;
					}
					payItem.addAward(PbHelper.createAward(type, id, count));
				}
			}
			payItem.setDesc(e.getDesc());
			payItem.setPercent(e.getPercent());
			payItem.setLimit(e.getLimit());
			payItem.setVal(Integer.valueOf(e.getDesc()));
			payItem.setAsset(e.getAsset());

			builder.addPayItem(payItem);
		}

		handler.sendMsgToPlayer(ActDayPayRs.ext, builder.build());
	}

	/**
	 * 消费有礼
	 * <p>
	 * 消费有礼
	 *
	 * @param handler
	 */
	public void actCostPersonRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_COST_PERSON);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		ActCostPersonRs.Builder builder = ActCostPersonRs.newBuilder();

		int state = currentActivity(player, actRecord, 0);
		builder.setState(state);

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}

		handler.sendMsgToPlayer(ActCostPersonRs.ext, builder.build());
	}

	/**
	 * 消费有礼
	 *
	 * @param handler
	 */
	public void actCostServerRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		// ActRecord actRecord = activityManager.getActivityInfo(player,
		// ActivityConst.ACT_COST_SERVER);
		// if (actRecord == null) {
		// handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
		// return;
		// }
		//
		// ActivityData activityData =
		// activityManager.getActivity(actRecord.getActivityId());
		// if (activityData == null) {
		// handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
		// return;
		// }
		//
		// int activityKeyId = actRecord.getAwardId();
		//
		// ActCostServerRs.Builder builder = ActCostServerRs.newBuilder();
		//
		// int state = currentActivity(player, activityData, 0);
		// builder.setState(state);
		//
		// List<StaticActAward> condList =
		// staticActivityMgr.getActAwardById(activityKeyId);
		// for (StaticActAward e : condList) {
		// int keyId = e.getKeyId();
		// if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
		// builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
		// } else {// 未领取奖励
		// builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
		// }
		// }
		// handler.sendMsgToPlayer(ActCostServerRs.ext, builder.build());
	}

	/**
	 * 充值有礼个人
	 *
	 * @param handler
	 */
	public void actTopupPerson(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_TOPUP_PERSON);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		ActTopupPersonRs.Builder builder = ActTopupPersonRs.newBuilder();

		int state = currentActivity(player, actRecord, 0);
		builder.setState(state);

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}

		handler.sendMsgToPlayer(ActTopupPersonRs.ext, builder.build());
	}

	/**
	 * 充值有礼
	 *
	 * @param handler
	 */
	public void actTopupServer(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_TOPUP_SERVER);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		ActTopupServerRs.Builder builder = ActTopupServerRs.newBuilder();

		int state = currentActivity(player, activityData, 0);
		builder.setState(state);

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActTopupServerRs.ext, builder.build());
	}

	/**
	 * 屯田计划 基金活动
	 *
	 * @param handler
	 */
	public void actGrowFootRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_GROW_FOOT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int awardId = actRecord.getAwardId();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId);
		if (condList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		List<StaticActFoot> footList = staticActivityMgr.getActFoots(awardId);
		if (footList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActGrowFootRs.Builder builder = ActGrowFootRs.newBuilder();

		for (StaticActFoot foot : footList) {
			if (null == foot) {
				continue;
			}
			int sortId = foot.getSortId();

			// 0.未购买 1-N购买后的第几天
			int state = currentActivity(player, actRecord, sortId);

			CommonPb.GrowFoot.Builder footBuilder = PbHelper.createGrowFoot(foot.getFootId(), foot.getType(), state);

			for (StaticActAward e : condList) {
				if (e.getSortId() == sortId) {
					if (actRecord.getReceived().containsKey(e.getKeyId())) {
						footBuilder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
					} else {
						footBuilder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
					}
				}
			}

			builder.addGrowFoot(footBuilder.build());
		}

		handler.sendMsgToPlayer(ActGrowFootRs.ext, builder.build());
	}

	/**
	 * 参与屯田计划
	 *
	 * @param handler
	 */
	public void doGrowFootRq(DoGrowFootRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_GROW_FOOT);
		if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		ActivityData activityData = activityManager.getActivity(activityBase);

		int awardId = actRecord.getAwardId();
		int footId = req.getFootId();

		StaticActFoot staticActFoot = staticActivityMgr.getActFoot(awardId, footId);
		if (staticActFoot == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int sortId = staticActFoot.getSortId();

		// 当前状况
		int state = currentActivity(player, actRecord, sortId);
		if (state != 0) {// 已参与
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_GOT);
			return;
		}

		// 金币是否足够
		if (player.getGold() < staticActFoot.getPrice()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		playerManager.subAward(player, AwardType.GOLD, 0, staticActFoot.getPrice(), Reason.ACT_FOOT);
		actRecord.putState(sortId, TimeHelper.getZeroOfDay());

		// Iterator<Entry<Long, Long>> it =
		// actRecord.getStatus().entrySet().iterator();
		// long v = 0;
		// while (it.hasNext()) {
		// long key = it.next().getKey();
		// v += key;
		// }
		// 记录玩家参与了屯田计划
		activityData.putAddtion(handler.getRoleId(), handler.getRoleId());

		DoGrowFootRs.Builder builder = DoGrowFootRs.newBuilder();
		builder.setGold(player.getGold());

		handler.sendMsgToPlayer(DoGrowFootRs.ext, builder.build());
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(ActivityConst.ACT_GROW_FOOT).isAward(false).awardId(staticActFoot.getFootId()).giftName(staticActFoot.getName()).roleId(player.roleId).channel(player.account.getChannel()).build());
	}

	// exchange hero
	// 1.check has hero
	// 2.check item number
	// 3.add hero
	// 4.remove item
	public void exchangeHeroHandler(ActivityPb.ExchangeHeroRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		SimpleData simpleData = player.getSimpleData();
		if (simpleData.isExchangeEquip()) {
			handler.sendErrorMsgToPlayer(GameError.EXCHANGE_EQUIP_DONE);
			return;
		}

		StaticExchangeHero config = staticActivityMgr.getExchangeHero(200);
		if (config == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// check config
		List<List<Integer>> items = config.getItems();
		if (items == null || items.size() < 1) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		// check item number ok
		for (List<Integer> item : items) {
			if (item == null) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}

			if (item.size() != 3) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}

			int itemId = item.get(1);
			int itemNum = item.get(2);
			Item itemHas = player.getItem(itemId);
			if (itemHas == null || itemHas.getItemNum() < itemNum) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
				return;
			}
		}

		// add award
		List<List<Integer>> awardConfig = config.getAward();
		if (awardConfig == null || awardConfig.size() != 1) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		List<Integer> equipConfig = awardConfig.get(0);
		if (equipConfig == null || equipConfig.size() != 3) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		Award award = new Award(0, equipConfig.get(0), equipConfig.get(1), equipConfig.get(2));
		int freeSlot = equipManager.getFreeSlot(player);
		if (freeSlot <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_EQUIP_SLOT);
			return;
		}

		int keyId = playerManager.addAward(player, award, Reason.ACT_EXCHANGE_HERO);
		simpleData.setExchangeEquip(true);
		ExchangeHeroRs.Builder builder = ExchangeHeroRs.newBuilder();
		award.setKeyId(keyId);
		Map<Integer, Equip> equipMap = player.getEquips();
		Equip equip = equipMap.get(keyId);
		if (equip != null) {
			builder.setEquip(equip.wrapPb());
		}
		for (List<Integer> item : items) {
			if (item == null) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}

			if (item.size() != 3) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}
			int type = item.get(0);
			int itemId = item.get(1);
			int itemNum = item.get(2);
			playerManager.subAward(player, type, itemId, itemNum, Reason.ACT_EXCHANGE_HERO);
			Item prop = player.getItem(itemId);
			if (prop != null) {
				builder.addProp(prop.wrapPb());
			} else {
				Item nullItem = new Item(itemId, 0);
				builder.addProp(nullItem.wrapPb());
			}
		}
		builder.setIsExchangeEquip(simpleData.isExchangeEquip());
		handler.sendMsgToPlayer(ExchangeHeroRs.ext, builder.build());

	}

	// exchange item
	// 1.get propId config
	// 2.check config
	// 3.check item number
	// 4.sub item
	// 5.add item
	public void exchangeItemHandler(ActivityPb.ExchangeItemRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int itemId = req.getPropId();
		// find item config
		StaticExchangeItem config = staticActivityMgr.getExchangeItem(itemId);
		if (config == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int configId = config.getExchangeItemId();
		int configNum = config.getExchangeNum();
		Item item = player.getItem(configId);
		if (item == null || item.getItemNum() < configNum) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		playerManager.addAward(player, AwardType.PROP, itemId, 1, Reason.ACT_EXCHANGE_ITEM);
		playerManager.subAward(player, AwardType.PROP, configId, configNum, Reason.ACT_EXCHANGE_ITEM);

		// wrap Msg
		ExchangeItemRs.Builder builder = ExchangeItemRs.newBuilder();
		Item itemHas = player.getItem(itemId);
		if (itemHas != null) {
			builder.addProp(itemHas.wrapPb());
		} else {
			Item nullItem = new Item(itemId, 0);
			builder.addProp(nullItem.wrapPb());
		}

		Item exchangeItem = player.getItem(config.getExchangeItemId());
		if (exchangeItem != null) {
			builder.addProp(exchangeItem.wrapPb());
		}

		handler.sendMsgToPlayer(ExchangeItemRs.ext, builder.build());
	}

	/**
	 * 体力赠送
	 * <p>
	 * 体力补给
	 *
	 * @param handler
	 */
	public void actPowerRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_POWER);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActPowerRs.Builder builder = ActPowerRs.newBuilder();

		int status = currentActivity(player, actRecord, 0);
		builder.setState(status);

		int awardId = actRecord.getAwardId();

		List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(awardId);
		for (StaticActAward e : actAwardList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActPowerRs.ext, builder.build());
	}

	/**
	 * 领取体力补给
	 **/
	public void getActPowerRq(ClientHandler handler, GetActPowerRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		StaticActAward actAward = staticActivityMgr.getActAward(rq.getKeyId());
		List<List<Integer>> awardList = actAward.getAwardList();
		if (actAward == null || awardList == null || awardList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_POWER);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		if (actRecord.getReceived().containsKey(rq.getKeyId())) {
			handler.sendErrorMsgToPlayer(GameError.TARGET_AWARD_IS_AWARD);
			return;
		}
		long currentTimeMillis = System.currentTimeMillis();
		boolean isFree = false;
		if (actAward.getKeyId() == 217) {
			if (currentTimeMillis < TimeHelper.getHoursOfDay(12)) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			}
			if (currentTimeMillis >= TimeHelper.getHoursOfDay(12) && currentTimeMillis < TimeHelper.getHoursOfDay(15)) {
				isFree = true;
			}
		} else if (actAward.getKeyId() == 218) {
			if (currentTimeMillis < TimeHelper.getHoursOfDay(18)) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			}
			if (currentTimeMillis >= TimeHelper.getHoursOfDay(18) && currentTimeMillis < TimeHelper.getHoursOfDay(21)) {
				isFree = true;
			}
		} else {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		int num = staticLimitMgr.getNum(SimpleId.ACTPOWER_MAKEUP);
		int use = num == 0 ? 50 : num;
		if (!isFree) {
			if (player.getGold() < use) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subGold(player, use, ActivityConst.ACT_POWER);
			Date firstLoginDate = player.account.getFirstLoginDate();
			Date now = new Date();
			SpringUtil.getBean(LogUser.class).getActPowerRqLog(new GetActPowerLog(now, player.roleId, player.getNick(), firstLoginDate == null ? now : firstLoginDate));
		}
		GetActPowerRs.Builder builder = GetActPowerRs.newBuilder();
		for (List<Integer> list : awardList) {
			if (list == null || list.size() != 3) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}
			Award award = new Award(list.get(0), list.get(1), list.get(2));
			playerManager.addAward(player, award.getType(), award.getId(), award.getCount(), Reason.ACT_AWARD);
			builder.addAward(award.wrapPb());
		}
		actRecord.getReceived().put(actAward.getKeyId(), 1);
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(GetActPowerRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 特价礼包
	 *
	 * @param handler
	 */
	public void actPayGiftRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_PAY_GIFT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActPayGiftRs.Builder builder = ActPayGiftRs.newBuilder();

		List<StaticActPayGift> actAwardList = staticActivityMgr.getPayGiftList(actRecord.getAwardId());
		if (null == actAwardList || actAwardList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		for (StaticActPayGift e : actAwardList) {
			if (e == null) {
				continue;
			}
			long state = actRecord.getStatus(e.getPayGiftId());
			builder.addQuota(PbHelper.createPayGift(e, (int) state));
		}
		handler.sendMsgToPlayer(ActPayGiftRs.ext, builder.build());
	}

	/**
	 * 建设排行
	 * <p>
	 * 阵营建设排行
	 *
	 * @param handler
	 */
	public void actBuildRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_BUILD_RANK);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		activityManager.updActPerson(player, ActivityConst.ACT_BUILD_RANK, 0, 0);
		ActBuildRankRs.Builder builder = ActBuildRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}

		handler.sendMsgToPlayer(ActBuildRankRs.ext, builder.build());
	}

	/**
	 * 在线奖励
	 * <p>
	 * 在线有礼
	 *
	 * @param handler
	 */
	public void actNewOnlineTimeRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ONLINE_TIME);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActOnlineTimeRs.Builder builder = ActOnlineTimeRs.newBuilder();
		builder.setState(actRecord.getCount());
		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			Integer st = actRecord.getReceived().get(keyId);
			if (st != null) {
				builder.addActivityCond(PbHelper.createActivityCondPb(e, st));// 可以领取
			} else {
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}

		handler.sendMsgToPlayer(ActOnlineTimeRs.ext, builder.build());
	}

	// 领取在线奖励
	public void actOnlineAward(ActOnlineAwardRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ONLINE_TIME);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		int keyId = rq.getKeyId();
		StaticActAward actAward = staticActivityMgr.getActAward(keyId);
		if (null == actAward) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
//        Integer integer = actRecord.getReceived().get(keyId);
//        if (integer == null || integer == 1) {
//            handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
//            return;
//        }
		if (actRecord.getReceived().containsKey(keyId) && actRecord.getReceived().get(keyId) == 1) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}
		ActOnlineAwardRs.Builder builder = ActOnlineAwardRs.newBuilder();
		builder.addAllAward(actAward.getAwardPbList());
		handler.sendMsgToPlayer(ActOnlineAwardRs.ext, builder.build());
		actAward.getAwardList().forEach(x -> {
			playerManager.addAward(player, x.get(0), x.get(1), x.get(2), Reason.ACT_AWARD);
		});
		actRecord.getReceived().put(keyId, 1);
		actRecord.setCount(0);

	}

	/**
	 * 每日充值
	 *
	 * @param handler
	 */
	public void actPayEveryDayRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_CONTINUOUS_RECHARGE);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		ActPayEveryDayRs.Builder builder = ActPayEveryDayRs.newBuilder();

		int day = DateHelper.dayiy(activityBase.getBeginTime(), new Date());
		builder.setState(day);

		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			int sortId = e.getSortId();
			int state = currentActivity(player, actRecord, sortId);
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCondState(PbHelper.createCondState(e, 1, state));
			} else {// 未领取奖励
				builder.addActivityCondState(PbHelper.createCondState(e, 0, state));
			}
		}

		handler.sendMsgToPlayer(ActPayEveryDayRs.ext, builder.build());
	}

	/**
	 * 月卡&&季卡
	 *
	 * @param handler
	 */
	public void actMonthCardRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MONTH_CARD);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActPayCard> payCardList = Lists.newArrayList(staticActivityMgr.getPayCard().values());

		if (payCardList == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		payCardList = payCardList.stream().sorted(Comparator.comparingInt(StaticActPayCard::getIndex)).collect(Collectors.toList());

		Lord lord = player.getLord();
		ActMonthCardRs.Builder builderList = ActMonthCardRs.newBuilder();
		Date openTime = serverManager.getServer().getOpenTime();
		Date now = new Date();
		//
		int betWeenDays = TimeHelper.equation(openTime.getTime(), now.getTime());
		for (StaticActPayCard e : payCardList) {
			if (e == null) {
				continue;
			}
			if (e.getLimitDate() > betWeenDays) {
				continue;
			}
			CommonPb.MonthCard.Builder builder = MonthCard.newBuilder();
			builder.setQuotaId(e.getPayCardId());
			builder.setCardType(e.getCardType());
			builder.setPrice(e.getMoney());
			long endTimeOfDay = TimeHelper.getEndTimeOfDay();
			if (e.getCardType() == 1) {
				if (endTimeOfDay > lord.getMonthCard()) {
					builder.setState(0);
					builder.setEndTime(0);
				} else {
					builder.setState(1);
					builder.setEndTime(lord.getMonthCard());
				}
			} else if (e.getCardType() == 2) {
				if (endTimeOfDay > lord.getSeasonCard()) {
					builder.setState(0);
					builder.setEndTime(0);
				} else {
					builder.setState(1);
					builder.setEndTime(lord.getSeasonCard());
				}
			} else if (e.getCardType() == 3) {
				long expireTime = player.getWeekCard().getExpireTime(e.getAwardId());
				if (endTimeOfDay > expireTime) {
					builder.setState(0);
					builder.setEndTime(0);
				} else {
					builder.setState(1);
					builder.setEndTime(expireTime);
				}
			} else if (e.getCardType() == 4) {
				long expireTime = playerManager.getAutoKillEndTime(player);
				if (endTimeOfDay > expireTime) {
					builder.setState(0);
					builder.setEndTime(0);
				} else {
					builder.setState(1);
					builder.setEndTime(expireTime);
				}
			}

			int keyId = e.getPayCardId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.setIsAward(1);
			} else {// 未领取奖励
				builder.setIsAward(0);
			}

			List<List<Integer>> sellList = e.getSellList();
			if (sellList.size() > 0) {
				for (List<Integer> sell : sellList) {
					int type = sell.get(0);
					int id = sell.get(1);
					int count = sell.get(2);
					builder.addAward(PbHelper.createAward(type, id, count));
				}
			}

			builder.setAssetBg(StringUtil.isNullOrEmpty(e.getAssetBg()) ? "" : e.getAssetBg());
			builder.setAssetFont(StringUtil.isNullOrEmpty(e.getAssetFont()) ? "" : e.getAssetFont());
			builder.setDesc(StringUtil.isNullOrEmpty(e.getDesc()) ? "" : e.getDesc());
			builder.setDiamond(e.getDiamond());
			builder.setName(StringUtil.isNullOrEmpty(e.getName()) ? "" : e.getName());
			builderList.addMonthCard(builder);
		}
		handler.sendMsgToPlayer(ActMonthCardRs.ext, builderList.build());
	}

	/**
	 * 领取月卡奖励
	 *
	 * @param handler
	 */
	public void getMonthCardAwardRq(GetMonthCardAwardRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MONTH_CARD);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 月卡ID
		int cardId = rq.getCardId();
		StaticActPayCard payCard = staticActivityMgr.getPayCard(cardId);
		if (null == payCard) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		// 判断月卡和季卡的类型
		Lord lord = player.getLord();
		if (payCard.getCardType() == 1) {
			if (TimeHelper.getEndTimeOfDay() > lord.getMonthCard()) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_MONTHCARD_OFF_ERROR);
				return;
			}
		} else if (payCard.getCardType() == 2) {
			if (TimeHelper.getEndTimeOfDay() > lord.getSeasonCard()) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_MONTHCARD_OFF_ERROR);
				return;
			}
		}

		GetMonthCardAwardRs.Builder builder = GetMonthCardAwardRs.newBuilder();
		// 判断最后的领取奖励时间
		if (actRecord.getReceived().containsKey(cardId)) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_MONTHCARD_AWARD_ERROR);
			return;
		} else {
			List<List<Integer>> sellList = payCard.getSellList();
			for (List<Integer> card : sellList) {
				playerManager.addAward(player, card.get(0), card.get(1), card.get(2), Reason.MONTH_CARD_AWARD);
				actRecord.getReceived().put(cardId, 1);
				builder.addAward(PbHelper.createAward(card.get(0), card.get(1), card.get(2)));
			}
			handler.sendMsgToPlayer(GetMonthCardAwardRs.ext, builder.build());

			ActivityEventManager.getInst().activityTip(EventEnum.GET_CARD_AWARD, player, 1, 0);
//            activityManager.updatePassPortTaskCond(player, ActPassPortTaskType.GET_CARD_AWARD, 1);

			dailyTaskManager.record(DailyTaskId.MONTH_CARD, player, 1);
		}
	}

	/**
	 * 累计登陆送VIP
	 * <p>
	 * 登录送VIP
	 *
	 * @param handler
	 */
	public void actLoginVipRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_LOGIN_VIP);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (condList == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		ActLoginVipRs.Builder builder = ActLoginVipRs.newBuilder();

		int state = currentActivity(player, actRecord, 0);
		builder.setState(state);

		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActLoginVipRs.ext, builder.build());
	}

	/**
	 * 等级排行榜
	 * <p>
	 * 等级冲榜
	 *
	 * @param handler
	 */
	public void actLevelRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_LEVEL_RANK);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActLevelRankRs.Builder builder = ActLevelRankRs.newBuilder();

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		boolean flag = false;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		StaticActAward rankAward = null;
		if (personRank != null) {
			flag = true;
			builder.setState(personRank.getRank());
			rankAward = staticActivityMgr.getActRankAward(activityKeyId, personRank.getRank());
		}

		boolean canAward = false;

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target == null || !target.isActive()) {
					continue;
				}
				builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue(), target.getBattleScore()));
			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			if (rankAward != null && rankAward.getKeyId() == keyId) {
				canAward = true;
			} else {
				canAward = false;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1, canAward));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0, canAward));
			}
			awardSet.add(keyId);
		}

		handler.sendMsgToPlayer(ActivityPb.ActLevelRankRs.ext, builder.build());
	}

	/**
	 * 开服七日狂欢
	 * <p>
	 * 七日狂欢
	 *
	 * @param handler
	 */
	public void actSevenRq(ActSevenRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SEVEN);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActSevenRs.Builder builder = ActSevenRs.newBuilder();

		Date createDate = player.account.getCreateDate();
		int state = DateHelper.dayiy(createDate, new Date());
		if (state > 7) {// 超过7天不开该活动
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		builder.setState(state);

		Map<Integer, StaticActSeven> sevens = staticActivityMgr.getSevens();
		if (null == sevens || sevens.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		Iterator<StaticActSeven> it = staticActivityMgr.getSevens().values().iterator();
		while (it.hasNext()) {
			StaticActSeven actSeven = it.next();
			if (actSeven == null) {
				continue;
			}
			int keyId = actSeven.getKeyId();
			int sortId = actSeven.getSortId();

			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCondState(PbHelper.createCondState(actSeven, 1, actSeven.getCond()));
			} else {// 未领取奖励
				int constate = currentActivity(player, actRecord, sortId);
				constate = constate > actSeven.getCond() ? actSeven.getCond() : constate;
				builder.addActivityCondState(PbHelper.createCondState(actSeven, 0, constate));
			}
		}
		handler.sendMsgToPlayer(ActivityPb.ActSevenRs.ext, builder.build());
	}

	/**
	 * 七日狂欢领奖
	 *
	 * @param handler
	 */
	public void doSevenAwardRq(DoSevenAwardRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SEVEN);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		DoSevenAwardRs.Builder builder = DoSevenAwardRs.newBuilder();

		int keyId = req.getKeyId();
		StaticActSeven staticActSeven = staticActivityMgr.getSevens().get(keyId);
		if (staticActSeven == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
			handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}

		int constate = currentActivity(player, actRecord, staticActSeven.getSortId());
		if (constate < staticActSeven.getCond()) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}

		actRecord.getReceived().put(keyId, 1);

		List<List<Integer>> awardList = staticActSeven.getAwardList();
		if (null == awardList || awardList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		for (List<Integer> award : awardList) {
			int type = award.get(0);
			int id = award.get(1);
			int count = award.get(2);
			int kid = playerManager.addAward(player, type, id, count, Reason.ACT_SEVEN);
			builder.addAward(PbHelper.createAward(player, type, id, count, kid));
		}

		handler.sendMsgToPlayer(ActivityPb.DoSevenAwardRs.ext, builder.build());
		SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_SEVEN, activityBase.getStaticActivity().getName(), staticActSeven.getKeyId());
		SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_SEVEN, activityBase.getStaticActivity().getName(), staticActSeven.getKeyId(), new Date(), staticActSeven.getAwardList());

		// 是否有tips
//        boolean tips = activityTips(player, true, actRecord, activityBase);
//        if (!tips) {
//
//        }
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	public void sendSevenOffAward(ActivityBase activityBase, ActivityData activityData) {

		// 奖励列表
		Iterator<StaticActSeven> it = staticActivityMgr.getSevens().values().iterator();

		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			if (next.account == null) {
				continue;
			}
			Date createDate = next.account.getCreateDate();
			int state = DateHelper.dayiy(createDate, new Date());
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			List<CommonPb.Award> awardList = new ArrayList<>();
			while (it.hasNext()) {
				StaticActSeven actSeven = it.next();
				int keyId = actSeven.getKeyId();
				int sortId = actSeven.getSortId();
				Long cond = actRecord.getStatus(sortId);
				if (state >= actSeven.getDay()) {
					if (cond != null && cond > actSeven.getCond() && !actRecord.getReceived().containsKey(keyId)) {
						actRecord.getReceived().put(actSeven.getKeyId(), 1);
						for (List<Integer> award : actSeven.getAwardList()) {
							awardList.add(PbHelper.createAward(award.get(0), award.get(1), award.get(2)).build());
						}

					}
				}
			}
			if (!awardList.isEmpty()) {
				playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}
		}
	}

	/**
	 * 当前tips
	 *
	 * @param player
	 * @param activityBase
	 * @return
	 */
	public boolean activityTips(Player player, boolean canAward, ActRecord actRecord, ActivityBase activityBase) {
		// 首次新开活动有tip
//		if (actRecord.isNew()) {
//			return true;
//		}

		// 每日特惠tips
		if (activityBase.getActivityId() == ActivityConst.ACT_DAY_PAY) {
			List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
			if (null == condList || condList.size() == 0) {
				return false;
			}
			for (StaticActAward e : condList) {
				int keyId = e.getKeyId();
				if (!actRecord.getReceived().containsKey(keyId)) {// 未领取奖励
					return true;
				}
			}
			return false;
		}
		// 许愿池tips
		if (activityBase.getActivityId() == ActivityConst.ACT_HOPE) {
			Map<Integer, StaticActHope> staticActHopeMap = staticActivityMgr.getStaticActHopeMap();
			if (null == staticActHopeMap || staticActHopeMap.size() == 0) {
				return false;
			}
			for (StaticActHope e : staticActHopeMap.values()) {
				if (e != null && !actRecord.getReceived().containsKey(e.getLevel()) && e.getCost() <= player.getGold()) {// 未领取奖励
					return true;
				}
			}
			return false;
		}

		int actTip = activityBase.getStaticActivity().getTip();
		if (actTip == 1) {
			return false;
		}

		// 不可领奖阶段,则没有tips
		if (!canAward) {
			return false;
		}
		// 装备精研tips
		if (activityBase.getActivityId() == ActivityConst.ACT_WASH_EQUIP) {
			List<StaticActEquipUpdate> staticActEquipUpdateList = staticActivityMgr.getStaticActEquipUpdateList();
			if (null == staticActEquipUpdateList || staticActEquipUpdateList.size() == 0) {
				return false;
			}
			//
			int status = (int) actRecord.getStatus(StaticActEquipUpdate.WASH_CONUT);
			int got = (int) actRecord.getStatus(StaticActEquipUpdate.PAY_CONUT);
			for (StaticActEquipUpdate e : staticActEquipUpdateList) {
				boolean flag = e.getType() == 1 ? got >= e.getCond() : status >= e.getCond();
				int keyId = e.getKeyId();
				if (!actRecord.getReceived().containsKey(keyId) && flag) {
					return true;
				}
			}
			return false;
		}

		// 30日签到tips
		if (activityBase.getActivityId() == ActivityConst.ACT_DAILY_CHECKIN) {
			int day;
			if (actRecord.getRecord().containsKey(0)) {
				day = actRecord.getRecord().get(0);
			} else {
				actRecord.getRecord().put(0, 1);
				day = actRecord.getRecord().get(0);
			}
			if (!actRecord.getReceived().containsKey(day)) {
				return true;
			}
			return false;
		}

		// 七日狂欢tips
		if (activityBase.getActivityId() == ActivityConst.ACT_SEVEN) {
			Date createDate = player.account.getCreateDate();
			int dayiy = DateHelper.dayiy(createDate, new Date());
			for (StaticActSeven e : staticActivityMgr.getSevens().values()) {
				int keyId = e.getKeyId();
				int cond = currentActivity(player, actRecord, e.getSortId());
				if (dayiy <= 7 && dayiy >= e.getDay()) {
					if (cond != 0 && cond >= e.getCond() && !actRecord.getReceived().containsKey(keyId)) {
						return true;
					}
				}
			}
			return false;
		}

		Map<Integer, StaticActCommand> actCommands = staticActivityMgr.getActCommands();
		// 基地升级判断tips
		if (activityBase.getActivityId() == ActivityConst.ACT_LEVEL && actCommands != null) {
			for (Map.Entry<Integer, StaticActCommand> entry : actCommands.entrySet()) {
				StaticActCommand actCommand = entry.getValue();
				if ((!(player.getCommandLv() < actCommand.getLevel() || (StaticActCommand.TEC_INSTITUTE == actCommand.getLimit().get(0) && player.getTechLv() < actCommand.getLimit().get(1)) || (StaticActCommand.PLAYER_LEVEL == actCommand.getLimit().get(0) && player.getLevel() < actCommand.getLimit().get(1)))) && (!actRecord.getReceived().containsKey(actCommand.getKeyId()))) {
					return true;
				}
			}
			return false;
		}
		// 首充判断tips
		if (activityBase.getActivityId() == ActivityConst.ACT_PAY_FIRST) {
			if (player.getLord().getFirstPay() == 1) {
				return true;
			}
		}

		List<StaticActPayCard> payCardList = staticActivityMgr.getPayCardList(actRecord.getAwardId());
		// 月卡tips
		if (activityBase.getActivityId() == ActivityConst.ACT_MONTH_CARD) {
			if (payCardList != null) {
				for (StaticActPayCard e : payCardList) {
					if (e == null) {
						continue;
					}
					int keyId = e.getPayCardId();
					switch (e.getCardType()) {
					case 1:
						if (!actRecord.getReceived().containsKey(keyId) && TimeHelper.getEndTimeOfDay() <= player.getLord().getMonthCard()) {
							return true;
						}
						break;
					case 2:
						if (!actRecord.getReceived().containsKey(keyId) && TimeHelper.getEndTimeOfDay() <= player.getLord().getSeasonCard()) {
							return true;
						}
						break;
					default:
						break;
					}
				}
			}
			return false;
		}

		// 紫装转盘判断tips
		if (activityBase.getActivityId() == ActivityConst.ACT_PURPLE_DIAL) {
			// 单次转盘总次数
			long dialCount = actRecord.getStatus(1);
			StaticActDialPurp dial = staticActivityMgr.getDialPurp(actRecord.getAwardId());
			if (dial == null) {
				return false;
			}
			int free = dial.getFreeTimes();
			if (free > dialCount) {
				return true;
			}
			return false;
		}

		// 橙装转盘判断tips
		if (activityBase.getActivityId() == ActivityConst.ACT_ORANGE_DIAL) {
			// 单次转盘总次数
			long dialCount = actRecord.getStatus(2);
			StaticActDialPurp dial = staticActivityMgr.getDialPurp(actRecord.getAwardId());
			if (dial == null) {
				return false;
			}
			int free = dial.getFreeTimes();
			if (free > dialCount) {
				return true;
			}
			return false;
		}

		// 幸运转盘判断tips
		if (activityBase.getActivityId() == ActivityConst.ACT_DIAL_LUCK) {
//			// 单次转盘总次数
			StaticActDialLuck common = staticActivityMgr.getDialLuck(activityBase.getAwardId(), 1, player.getVip());
			int count = (int) actRecord.getStatus(1);// 已转次数
			int freeCount = common.getFree() - count < 0 ? 0 : common.getFree() - count;
			if (freeCount > 0) {
				return true;
			}
			return false;
		}

		// 晶体转盘判断tips
		if (activityBase.getActivityId() == ActivityConst.ACT_CRYSTAL_DIAL || activityBase.getActivityId() == ActivityConst.ACT_GOLD_DIAL) {
			int onePrice = activityBase.getActivityId() == ActivityConst.ACT_CRYSTAL_DIAL ? staticLimitMgr.getNum(230) : staticLimitMgr.getNum(246);
			if (onePrice == 0) {
				return false;
			}
			// 单次转盘总次数
			int count = actRecord.getRecord(0) / onePrice;
			int freeCount = count - actRecord.getRecord(1);// 已转次数
			freeCount = freeCount < 0 ? 0 : freeCount;
			if (freeCount > 0) {
				return true;
			}
			return false;
		}

		// 绝版英雄判断tips
		if (activityBase.getActivityId() == ActivityConst.ACT_HERO_KOWTOW) {
//			long recordTime = (actRecord.getStatus(1));// 刷新记录时间
//			List<Integer> addtion = staticLimitMgr.getAddtion(199);
//			long systime = System.currentTimeMillis();
//			// long refresh = (systime + 4 * CD_TIME - recordTime) / CD_TIME;
//			long refresh = (systime + addtion.get(4) * CD_TIME - recordTime) / CD_TIME;
//			refresh = refresh > addtion.get(4) ? addtion.get(4) : refresh;
//			if (refresh > 0) {
//				return true;
//			}
//			return false;
			// status, key=501 存打开活动的时间
			Date date = new Date();
			long key = actRecord.getActivityId();
			if (!actRecord.getStatus().containsKey(key)) {
				return true;
			}
			date.setTime(actRecord.getStatus(key));
			if (DateHelper.dayiy(date, new Date()) > 1) {
				return true;
			}
			return false;
		}

		/**
		 * 通行证红点tips
		 */
		if (activityBase.getActivityId() == ActivityConst.ACT_PASS_PORT) {
			Map<Integer, Integer> record = actRecord.getRecord();
			int score = record.get(0) == null ? 0 : record.get(0);
			int isBuy = record.get(1) == null ? 0 : record.get(1);
			int lv = staticActivityMgr.getPassPortLv(score);

			List<StaticPassPortAward> passPortList = staticActivityMgr.getPassPortList(actRecord.getAwardId());
			if (passPortList == null || passPortList.size() == 0) {
				return false;
			}

			for (StaticPassPortAward staticPassPortAward : passPortList) {
				if (!actRecord.getReceived().containsKey(staticPassPortAward.getId()) && lv >= staticPassPortAward.getLv()) {
					if (staticPassPortAward.getType() == 2 && isBuy == 0) {
						continue;
					}
					return true;
				}
			}

			int maxPassPortScore = staticActivityMgr.getMaxPassPortScore();
			if (score >= maxPassPortScore) {
				return false;
			}

			Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();
			Set<Entry<Integer, ActPassPortTask>> entries = tasks.entrySet();
			for (Entry<Integer, ActPassPortTask> entry : entries) {
				if (entry != null && entry.getKey() != null && entry.getValue() != null && entry.getValue().getIsAward() != 1) {
					StaticPassPortTask passPortTask = staticActivityMgr.getPassPortTask(entry.getKey());
					int process = entry.getValue().getProcess();
					if (process >= passPortTask.getCond()) {
						return true;
					}
				}
			}
			return false;
		}

		if (activityBase.getActivityId() == ActivityConst.ACT_DOUBLE_EGG || activityBase.getActivityId() == ActivityConst.ACT_NEW_YEAR_EGG || activityBase.getActivityId() == ActivityConst.ACT_DRAGON_BOAT) {
			List<StaticActExchange> awardList = new ArrayList<>(staticActivityMgr.getActDoubleEggs().values());
			for (StaticActExchange e : awardList) {
				int keyId = e.getKeyId();
				// 已兑换次数
				int changeNum = actRecord.getRecordNum(keyId);
				if (changeNum >= e.getMaxNum()) {
					continue;
				}
				// 数量限制
				int propId = getExchangeId(activityBase.getActivityId());
				Item item = player.getItem(propId);
				int total = 0;
				if (item != null) {
					total = item.getItemNum();
				}
				if (total >= e.getNeedNum()) {
					return true;
				}
			}
			return false;
		}
		if (activityBase.getActivityId() == ActivityConst.ACT_DOUBLE_EGG_GIFT || activityBase.getActivityId() == ActivityConst.ACT_NEW_YEAR_GIFT || activityBase.getActivityId() == ActivityConst.ACT_DRAGON_BOAT_GIFT) {
			Integer totalCost = actRecord.getRecord(-1);
			if (totalCost == null || totalCost == 0) {
				return false;
			}
			List<StaticActivityChrismasAward> list = new ArrayList<>(staticActivityMgr.getChrismasAwardMap().values());
			for (StaticActivityChrismasAward award : list) {
				if (totalCost >= award.getCost()) {
					if (!actRecord.getReceived().containsKey(award.getKeyId())) {
						return true;
					}
				}
			}
			return false;
		}

		if (activityBase.getActivityId() == ActivityConst.ACT_TASK_HERO) {
			int num = 0;
			for (ActPassPortTask task : actRecord.getTasks().values()) {
				if (actRecord.getReceived().containsKey(task.getId())) {
					num++;
					continue;
				}
				StaticHeroTask heroTask = staticActivityMgr.getStaticHeroTaskMap().get(task.getId());
				if (task.getProcess() >= heroTask.getCond()) {
					return true;
				}
			}
			if (num >= 6 && !actRecord.getReceived().containsKey(0)) {
				return true;
			}
			return false;
		}

		if (activityBase.getActivityId() == ActivityConst.ACT_WELL_CROWN_THREE_ARMY) {
			ActivityData activityData = activityManager.getActivity(activityBase);
			int selfGold = actRecord.getRecord(0);
			int countryGold = activityData.getRecord(player.getCountry());
			List<StaticActAward> actAwardList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
			for (StaticActAward award : actAwardList) {
				List<Integer> param = com.game.util.StringUtil.stringToList(award.getParam());
				if (param.size() < 2) {
					continue;
				}
				if (selfGold >= param.get(0) && countryGold >= param.get(1)) {
					if (actRecord.getRecevie(award.getKeyId()) != 0) {
						continue;
					}
					return true;
				}
			}
			return false;
		}
		// 春节活动tips
		if (activityBase.getActivityId() == ActivityConst.ACT_SPRING_FESTIVAL) {
			ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
			if (activityData == null) {
				return false;
			}
			List<StaticActSpringFestival> springFestivals = staticActivityMgr.getSpringFestivals(actRecord.getAwardId());
			if (springFestivals != null && !springFestivals.isEmpty()) {
				for (StaticActSpringFestival springFestival : springFestivals) {
					int keyId = springFestival.getKeyId();
					if (springFestival.getType() == SpringType.SpringAward) {
						if (activityData.getRecordNum(0) >= springFestival.getCond() && !actRecord.getReceived().containsKey(keyId)) {
							return true;
						}
					}
					if (springFestival.getType() == SpringType.SpringRecharge) {
						if (springFestival.getCond() == 0 && actRecord.getReceived(keyId) != GameServer.getInstance().currentDay) {
							actRecord.getReceived().remove(springFestival.getKeyId());
						}
						if (actRecord.getStatus(0) >= springFestival.getCond() && !actRecord.getReceived().containsKey(keyId)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		// 塔防活动
		if (activityBase.getActivityId() == ActivityConst.ACT_TD_SEVEN_TASK) {
			Date createDate = player.account.getCreateDate();
			int less = activityBase.getStaticActivity().getLess();
			less = less == 0 ? 7 : less;
			Date endTime = DateHelper.addDate(createDate, less);
			if (new Date().after(endTime)) {
				return false;
			}
			ActivityData activityData = activityManager.getActivity(activityBase);
			ActivityEventManager.getInst().addTipsSyn(EventEnum.GET_ACTIVITY_AWARD_TIP, new TdActor(player, actRecord, activityData, activityBase));
			return activityManager.refreshTDTaskTips(player, actRecord);
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (condList == null) {
			return false;
		}
		StaticActivity staticActivity = activityBase.getStaticActivity();
		int isAddUp = staticActivity.getAddUp();// 全服累计类型活动
		int isRank = staticActivity.getRank();// 全服排行活动

		// 全服返利tips
		if (activityBase.getActivityId() == ActivityConst.ACT_SER_PAY) {
			ActivityData activityData = activityManager.getActivity(activityBase);
			if (activityData == null) {
				return false;
			}

			// 包含可领取,并且未领取奖励,则给红点
			for (StaticActAward actAward : condList) {
				if (actAward.getCond() == 0) {
					continue;
				}
				int cond = currentActivity(player, activityData, actAward.getSortId());
				if (cond <= 0) {
					return false;
				}
				boolean isCondOk = cond >= actAward.getCond() ? true : false;
				boolean isReceivedOk = !actRecord.getReceived().containsKey(actAward.getKeyId());
				if (activityBase.isRankAct()) {
					isCondOk = cond <= actAward.getCond();
				}
				if (isCondOk && isReceivedOk) {
					return true;
				}
			}
			return false;
		}

		// 体力盛宴
		if (activityBase.getActivityId() == ActivityConst.ACT_POWER) {
			for (StaticActAward actAward : condList) {
				int cond = currentActivity(player, actRecord, 0);
				if (cond == actAward.getSortId() && cond >= actAward.getCond()) {
					return !actRecord.getReceived().containsKey(actAward.getKeyId());
				}
			}
			return false;
		}

		// 阵营骨干
		if (activityBase.getActivityId() == ActivityConst.ACT_CAMP_MEMBERS) {
			ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
			Date time = TimeHelper.getHourTime(activityBase.getEndTime(), 19);
			if (System.currentTimeMillis() < time.getTime()) {
				return false;
			}
			CampMembersRank campMembersRank = activityData.getCampMembersRank(player);
			if (campMembersRank == null) {
				return false;
			}
			int playerRank = campMembersRank.getRank();
			List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(actRecord.getAwardId());
			if (displayList == null) {
				return false;
			}
			int tempRank = activityManager.obtainAwardGear(campMembersRank.getRank(), displayList);
			for (StaticActRankDisplay display : displayList) {
				int rank = display.getRank();
				if (playerRank <= rank && tempRank == display.getRank()) {
					if (!actRecord.getReceived().containsKey(display.getKeyId())) {
						return true;
					}
				}
			}
			return false;
		}

		// 成长基金
		if (activityBase.getActivityId() == ActivityConst.ACT_INVEST) {
			int state = currentActivity(player, actRecord, 0);
			if (state == 0 && player.getVip() >= 3) {
				return true;
			}
			if (state != 0) {
				if (condList == null) {
					return false;
				}
				for (StaticActAward e : condList) {
					int keyId = e.getKeyId();
					// 未领取奖励 等级到了
					if (!actRecord.getReceived().containsKey(keyId) && player.getLevel() >= e.getCond()) {
						return true;
					}
				}
			}
			return false;
		}

		// 日常训练
		if (activityBase.getActivityId() == ActivityConst.DAILY_TRAINRS) {
			for (StaticActAward staticActAward : condList) {
				if (!actRecord.getReceived().containsKey(staticActAward.getKeyId()) && actRecord.getRecord(1) >= staticActAward.getCond()) {
					return true;
				}
			}
			return false;
		}

		// 采集资源
		if (activityBase.getActivityId() == ActivityConst.ACT_COLLECTION_RESOURCE) {
			for (StaticActAward staticActAward : condList) {
				if (!actRecord.getReceived().containsKey(staticActAward.getKeyId()) && actRecord.getStatus(0) >= staticActAward.getCond()) {
					return true;
				}
			}
			return false;
		}

		// 惊喜特惠
		if (activityBase.getActivityId() == ActivityConst.ACT_SURIPRISE_GIFT) {
			if (!actRecord.getReceived().containsKey(ActivityConst.ACT_SURIPRISE_GIFT)) {
				return true;
			}
			return false;
		}

		if (activityBase.getActivityId() == ActivityConst.ACT_HERO_DIAL) {
			int awardId = actRecord.getAwardId();
			StaticActDialPurp dial = staticActivityMgr.getDialPurp(awardId);
			if (dial == null) {
				return false;
			}
			if (actRecord.getRecord(GameServer.getInstance().currentDay) < dial.getFreeTimes()) {
				return true;
			}
			return false;
		}
		// 大V带队
		if (activityBase.getActivityId() == ActivityConst.ACT_HIGH_VIP) {
			if (player.getLevel() < staticLimitMgr.getNum(SimpleId.ACT_HIGHT_VIP)) {
				return false;
			}
			ActivityData activityData = activityManager.getActivity(activityBase);
			if (activityData == null) {
				return false;
			}
			for (StaticActAward e : condList) {
				int status = currentActivity(player, activityBase, activityData, e);
				int keyId = e.getKeyId();
				// 有奖励未领取 显示下
				if (!actRecord.getReceived().containsKey(keyId) && status >= e.getCond()) {
					return true;
				}
			}

			return false;
		}

		// 累计类型或者排行类型活动,取全服记录值
		if (isRank == 1 || isAddUp > 0 || isRank == 3) {
			ActivityData activityData = activityManager.getActivity(activityBase);
			if (activityData == null) {
				return false;
			}

			// 包含可领取,并且未领取奖励,则给红点
			StaticActAward preActAward = null;
			for (StaticActAward actAward : condList) {
				int cond = currentActivity(player, activityData, actAward.getSortId());
				if (cond <= 0) {
					return false;
				}
				boolean isCondOk = cond >= actAward.getCond() ? true : false;
				boolean isReceivedOk = !actRecord.getReceived().containsKey(actAward.getKeyId());

				if (activityData.getActivityId() == ActivityConst.ACT_ARMS_PAY) {
					CommonPb.ArmsPayAward.Builder armsPayAward = getArmsPayAward(player, activityData, actRecord);
					int count = armsPayAward.getCount();
					isReceivedOk = count > 0 ? true : false;
				}
				// 导师排行
				if (activityData.getActivityId() == ActivityConst.ACT_MENTOR_SCORE || activityData.getActivityId() == ActivityConst.ACT_TOPUP_RANK || staticActivity.getActivityId() == ActivityConst.ACT_COST_GOLD) {
					Date rewardTime = TimeHelper.getRewardTime(activityBase.getEndTime());
					Date now = new Date();
					if (now.before(rewardTime)) {
						return false;
					}
					if (cond == actAward.getCond()) {
						if (!isReceivedOk) {
							return false;
						}
						return true;
					} else if (preActAward != null && cond <= actAward.getCond() && cond > preActAward.getCond()) {
						if (!isReceivedOk) {
							return false;
						}
						return true;
					}
					preActAward = actAward;
					continue;
				}

				if (activityBase.isRankAct() || activityBase.isRankThree()) {
					isCondOk = cond <= actAward.getCond();
				}
				if (isCondOk && isReceivedOk) {
					return true;
				}
			}
		}
		// 个人记录类型活动,取个人记录值
		else if (isRank == 0 && isAddUp == 0) {
			if (activityBase.getActivityId() == ActivityConst.ACT_LOGIN_SEVEN) {
				return actRecord.getCount() > actRecord.getReceived().size();
			}
			if (activityBase.getActivityId() == ActivityConst.ACT_RAIDERS || activityBase.getActivityId() == ActivityConst.ACT_LUCKLY_EGG) {
				return (actRecord.getCount() + (actRecord.getRecord().size() > 0 ? 0 : 1)) > 0;
			}
			if (activityBase.getActivityId() == ActivityConst.RE_DIAL) {
				return actRecord.getCount() > 0;
			}

			for (StaticActAward actAward : condList) {
				int sortId = actAward.getSortId();
				int cond = currentActivity(player, actRecord, sortId);
				// 七日登录可补签不显示tips
				boolean isCondOk = cond >= actAward.getCond();
				boolean isReceivedOk = !actRecord.getReceived().containsKey(actAward.getKeyId());
				if (activityBase.isRankAct()) {
					isCondOk = cond <= actAward.getCond() && cond > 0;
				}
				if (isCondOk && isReceivedOk) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 领奖处理
	 *
	 * @param player
	 * @param activityBase
	 * @param activity
	 * @param actAward
	 * @return
	 */
	public int currentActivity(Player player, ActivityBase activityBase, ActRecord activity, StaticActAward actAward) {
		switch (activity.getActivityId()) {
		case ActivityConst.ACT_WELL_CROWN_THREE_ARMY: {
			List<Integer> param = com.game.util.StringUtil.stringToList(actAward.getParam());
			if (param.size() < 2) {
				return 0;
			}
			ActRecord record = activityManager.getActivityInfo(player, activityBase);
			ActivityData actData = activityManager.getActivity(activityBase);
			// 个人消耗+阵营消耗
			if (record.getRecord(0) >= param.get(0) && actData.getRecord(player.getCountry()) >= param.get(1)) {
				return 1;
			}
			return 0;
		}
		default:
			return currentActivity(player, activity, actAward.getSortId());
		}
	}

	/**
	 * 活动最新状态值
	 *
	 * @param player
	 * @return
	 */
	public int currentActivity(Player player, ActRecord activity, int sortId) {
		switch (activity.getActivityId()) {
		case ActivityConst.ACT_LEVEL:// 主城升级
			return player.getCommandLv();
		case ActivityConst.ACT_SCENE_CITY: // 攻城掠地
			return (int) activity.getStatus(sortId);
		case ActivityConst.ACT_INVEST: // 投资计划
			if (activity.getStatus(sortId) == 0L) {
				return 0;
			}
			return player.getLevel();
		case ActivityConst.ACT_HIGH_VIP: // 大咖带队
			if (player.getLevel() < staticLimitMgr.getNum(SimpleId.ACT_HIGHT_VIP)) {
				return 0;
			}
			return (int) activity.getStatus(sortId);

		case ActivityConst.ACT_SEVEN_RECHARGE: // 七日充值
			return (int) activity.getStatus(sortId);

		case ActivityConst.ACT_SOILDER_RANK: // 兵力排行
			return (int) activity.getStatus(player.getLord().getLordId());
		case ActivityConst.ACT_CITY_RANK: // 城战排行
			return (int) activity.getStatus(player.getLord().getLordId());
		case ActivityConst.ACT_TOPUP_RANK: // 充值排行
			// return (int) activity.getStatus(player.getLord().getLordId());
			ActivityData ac = activityManager.getActivity(activity.getActivityId());
			ActPlayerRank ra = ac.getLordCostRank(player.getLord().getLordId(), 500);
			if (ra != null) {
				return ra.getRank();
			}
			return 0;
		case ActivityConst.ACT_FORGE_RANK: // 锻造排行榜
			return (int) activity.getStatus(player.getLord().getLordId());
		case ActivityConst.ACT_COUNTRY_RANK: // 国战排行榜
			return (int) activity.getStatus(player.getLord().getLordId());
		case ActivityConst.ACT_OIL_RANK: // 屯粮排行榜
			return (int) activity.getStatus(player.getLord().getLordId());
		case ActivityConst.ACT_COST_GOLD: // 消费钻石排行
			ActivityData activity1 = activityManager.getActivity(activity.getActivityId());
			ActPlayerRank lordRank = activity1.getLordCostRank(player.getLord().getLordId(), 1000);
			if (lordRank != null) {
				return lordRank.getRank();
			}
			return 0;

		case ActivityConst.ACT_MENTOR_SCORE: { // 导师排行
			ActivityData activityData = activityManager.getActivity(activity.getActivityId());
			ActPlayerRank rank = activityData.getLordRank(player.getLord().getLordId());
			if (rank != null) {
				return rank.getRank();
			}
			return 0;
		}

		case ActivityConst.ACT_GROW_FOOT: {// 屯田活动
			long buyTime = activity.getStatus(sortId);
			if (buyTime == 0L) {
				return 0;
			} else {
				return TimeHelper.passDay(buyTime) + 1;
			}
		}
		case ActivityConst.ACT_POWER: {// 体力盛宴
			long hour = System.currentTimeMillis();
			if (hour >= TimeHelper.getTimeOfDay(12) && hour < TimeHelper.getTimeOfDay(15)) {
				return 1;
			} else if (hour >= TimeHelper.getTimeOfDay(18) && hour < TimeHelper.getTimeOfDay(21)) {
				return 2;
			}
			return 0;
		}
		case ActivityConst.ACT_WASH_RANK: { // 洗练排行
			return (int) activity.getStatus(player.getLord().getLordId());
		}
		case ActivityConst.ACT_STONE_RANK: { // 囤宝石排行（屯铁）
			return (int) activity.getStatus(player.getLord().getLordId());
		}
//            case ActivityConst.ACT_LOGIN_SEVEN: { // 七日登录
//                return player.account.getTimeFromCreat();
//            }
		case ActivityConst.ACT_HERO_KOWTOW: { // 七星拜将
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_LOW_COUNTRY: {// 强国策
			// TODO 判断是不是最弱国家
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_SER_PAY: {// 全服返利
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_PAY_FIRST: {// 首充礼包
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_ZHENJI_ICON: { // 雪夜甄姬(完成几个任务个数)
			int state = 0;
			List<StaticActTask> tlist = staticActivityMgr.getActTasks(activity.getActivityId());
			for (StaticActTask e : tlist) {
				long process = activity.getStatus(e.getSortId());
				if (process >= e.getProcess()) {
					state++;
				}
			}
			return state;
		}
		case ActivityConst.ACT_DAY_PAY: { // 每日充值
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_COST_PERSON: { // 消费有奖
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_TOPUP_PERSON: { // 充值有礼(个人)
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_TOPUP_SERVER: { // 充值有礼(全服)
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_BUILD_RANK: { // 建设排行
			return (int) activity.getStatus(player.getLord().getLordId());
		}
		case ActivityConst.ACT_ONLINE_TIME: { // 在线时长
			return player.onLineTime(true);
		}
		case ActivityConst.ACT_CONTINUOUS_RECHARGE: { // 连续充值
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_MONTH_CARD: { // 月卡
			if (player.getLord().getMonthCard() >= TimeHelper.getEndTimeOfDay()) {
				return 1;
			}
			return 0;
		}
		case ActivityConst.ACT_LOGIN_VIP: { // 登陆送VIP
			return player.account.getLoginDays();
		}
		case ActivityConst.ACT_SEVEN: { // 七日狂欢活动
			int cond = sortId / 1000;
			if (cond == 5) {// 副本通过
				return missionManager.pssBossMission(player, sortId % 1000);
			} else if (cond == 6) {// 收集武将
				return heroManager.getQualityNum(player, sortId % 1000);
			} else if (cond == 7) {// 武将洗练[完成, ok1]
				int quality = sortId % 1000;
				return player.getWashHeroMax(quality);
			} else if (cond == 8) {// 装备收集[完成, ok1]
				int quality = (sortId % 1000) / 100;
				int equipType = sortId % 100;
				return player.getEquipMake(quality, equipType);
				// return equipManager.getQualityNum(player,quality, equipType);

			} else if (cond == 9) {// 装备洗练[完成, ok1]
				int quality = (sortId % 1000) / 100;
				return player.getWashEquipMax(quality);
			} else if (cond == 10) {// 打造杀器
				int level = sortId % 1000;
				return killEquipManager.getLevelNum(player, level);
			} else if (cond == 11) {// 玩家等级
				return player.getLevel();
			} else if (cond == 12) {// 司令部等级
				return player.getCommandLv();
			} else if (cond == 13) {// 战斗力[完成, ok1]
				return player.getMaxScore();
			} else if (cond == 16) {// 地图最多拥有城池
				return worldManager.getCityNum(sortId % 1000, player);
			} else {// 取记录值
				return (int) activity.getStatus(sortId);
			}
		}
		case ActivityConst.ACT_WORLD_BATTLE: {
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_ARMS_PAY: {
			if (activity instanceof ActivityData) {
				activity = activityManager.getActivityInfo(player, ActivityConst.ACT_ARMS_PAY);// 获取活动数据
			}
			ActivityData activityData = activityManager.getActivity(ActivityConst.ACT_ARMS_PAY);// 获取活动数据
			int sorce = (int) activityData.getAddtion(player.getLord().getCountry());// 获取阵营分数
			int count = getArmsPayAward(player, activityData, activity).getCount();// 获取次数
			if (sorce > 0 && count > 0) {
				return sorce;
			} else {
				return 0;
			}
		}
		case ActivityConst.ACT_KILL_ALL: {
			// 记录的杀敌数
			Integer kill = activity.getRecord(1);
			if (kill == null) {
				kill = 0;
			}
			return kill;
		}
		case ActivityConst.ACT_DAYLY_RECHARGE: {
			List<StaticActAward> condList = staticActivityMgr.getActAwardById(activity.getAwardId());
			int index = 0;
			for (int i = 0; i < condList.size(); i++) {
				if (condList.get(i).getSortId() == sortId) {
					index = i - 1;
					break;
				}
			}
			List<Entry<Integer, Integer>> reharges = activity.getRecord().entrySet().stream().sorted(Comparator.comparingInt(e -> e.getKey())).collect(Collectors.toList());
			if (reharges.size() <= index) {
				return 0;
			}
			if (index < 0) {
				return 0;
			}
			Integer topUp = reharges.get(index).getValue();
			return topUp;
		}
		case ActivityConst.ACT_ZERO_GIFT: {// 0元礼包
			long buyTime = activity.getStatus(sortId);
			if (buyTime == 0L) {
				return 0;
			} else {
				return TimeHelper.passDay(buyTime) + 1;
			}
		}
		case ActivityConst.ACT_DAYLY_EXPEDITION: {// 每日远征
			// 记录的杀敌数
			Long kill = activity.getStatus(-1L);
			if (kill == null) {
				kill = 0L;
			}
			return kill.intValue();
		}
		case ActivityConst.ACT_DAILY_MISSION: {// 每日战役
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_HERO_WASH: {
			return (int) activity.getStatus(0L);
		}
		case ActivityConst.ACT_LUXURY_GIFT: { // 豪华礼包
			int loginDay = player.getLord().getLoginDays();
			List<Integer> luxury = staticLimitMgr.getAddtion(SimpleId.LUXURY_GIFT);
			int charget = 0;
			if (loginDay < luxury.size()) {
				charget = luxury.get(loginDay - 1);
			} else {
				charget = luxury.get(luxury.size() - 1);
			}
			int val = player.getLord().getTopup();
			if (val >= charget) {
				return 1;
			}
			return 0;
		}
		case ActivityConst.DAILY_TRAINRS: { // 日常训练
			return activity.getRecord(1);
		}
		case ActivityConst.ACT_ORDER: { // 获取订单
			return activity.getRecord(1);
		}
		case ActivityConst.ACT_GRAND_TOTAL: {
			return (int) activity.getStatus(0L);
		}
		case ActivityConst.ACT_WELL_CROWN_THREE_ARMY: {
			return activity.getRecord(0);
		}
		case ActivityConst.LUCK_DIAL: {
			return (int) activity.getStatus(1L);
		}
		case ActivityConst.ACT_COLLECTION_RESOURCE: {
			return (int) activity.getStatus(0);
		}
		case ActivityConst.ACT_RAIDERS: {
			return activity.getRecord().size();
		}
		case ActivityConst.ACT_SEARCH: {
			return (int) activity.getStatus(sortId);
		}
		case ActivityConst.ACT_LUCKLY_EGG: {
			return activity.getRecord().size();
		}
		case ActivityConst.ACT_LOGIN_SEVEN: {
			return activity.getCount();
		}
		case ActivityConst.ACT_MONSTER: {
			return activity.getCount();
		}
		case ActivityConst.BLOOD_ACTIVITY: {
			return activity.getCount();
		}
		default:
			break;
		}
		return 0;
	}

	/**
	 * 屯田活动奖励发放
	 *
	 * @param activityBase
	 * @param activityData
	 */
	public void sendFootMail(ActivityBase activityBase, ActivityData activityData) {
		Map<Long, Long> addtions = activityData.getAddtions();

		int awardId = activityBase.getAwardId();
		List<StaticActFoot> footList = staticActivityMgr.getActFoots(awardId);
		if (null == footList || footList.size() == 0) {
			return;
		}

		Iterator<Long> it = addtions.values().iterator();
		while (it.hasNext()) {
			long lordId = it.next();
			Player target = playerManager.getPlayer(lordId);
			if (target == null) {
				continue;
			}

			ActRecord actRecord = target.activitys.get(activityBase.getActivityId());
			if (actRecord == null) {
				continue;
			}

			Map<Long, Long> status = actRecord.getStatus();
			List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();
			for (StaticActFoot actFoot : footList) {
				if (null == actFoot) {
					continue;
				}
				long sortId = (long) actFoot.getSortId();
				if (!status.containsKey(sortId)) {
					continue;
				}
				List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId, actFoot.getSortId());
				for (StaticActAward e : condList) {
					if (!actRecord.getReceived().containsKey(e.getKeyId())) {
						actRecord.getReceived().put(e.getKeyId(), 1);
						awardList.addAll(e.getAwardPbList());
					}
				}
				long footId = sortId * 1000;
				if (!status.containsKey(footId)) {
					status.put(footId, 1L);
					awardList.add(PbHelper.createAward(AwardType.GOLD, 0, actFoot.getPrice()).build());
				}
			}

			if (!awardList.isEmpty()) {
				playerManager.sendAttachMail(target, PbHelper.finilAward(awardList), MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
			}
		}
	}

	/**
	 * 屯田活动奖励发放
	 *
	 * @param activityBase
	 */
	public void sendFootMailGold(ActivityBase activityBase, Player target) {
		int awardId = activityBase.getAwardId();
		List<StaticActFoot> footList = staticActivityMgr.getActFoots(awardId);
		if (null == footList || footList.size() == 0) {
			return;
		}
		if (target == null) {
			return;
		}

		ActRecord actRecord = target.activitys.get(activityBase.getActivityId());
		if (actRecord == null) {
			return;
		}

		Map<Long, Long> status = actRecord.getStatus();
		List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();
		for (StaticActFoot actFoot : footList) {
			boolean canAward = true;
			if (null == actFoot) {
				continue;
			}
			long sortId = (long) actFoot.getSortId();
			if (!status.containsKey(sortId)) {
				continue;
			}
			List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId, actFoot.getSortId());
			for (StaticActAward e : condList) {
				if (!actRecord.getReceived().containsKey(e.getKeyId())) {
					canAward = false;
				}
			}
			if (canAward) {
				long footId = sortId * 1000;
				if (null != actFoot && status.containsKey(sortId) && !status.containsKey(footId)) {
					status.put(footId, 1L);
					awardList.add(PbHelper.createAward(AwardType.GOLD, 0, actFoot.getPrice()).build());
				}
			}
		}

		if (!awardList.isEmpty()) {
			playerManager.sendAttachMail(target, PbHelper.finilAward(awardList), MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
		}
	}

	public void redDial(ActivityPb.RedDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_ORANGE_DIAL);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 获取当前玩家的碎片个数
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 碎片Id, 数量(record里面满了八个才不能继续获得)
		// 如果record满了八个则单抽和十抽都是从loot2里面获取
		// 如果record不满八个，单抽从loot1,10抽从loot2和mustloot里面随机
		Map<Integer, Integer> record = actRecord.getRecord();
		int awardId = actRecord.getAwardId();
		StaticActRedDial staticActRedDial = staticActivityMgr.getRedDial(awardId);
		if (staticActRedDial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		List<Integer> price = staticActRedDial.getPrice();
		if (price == null || price.size() != 2) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		List<Integer> mustLoot = staticActRedDial.getMustLoot();
		if (mustLoot == null || mustLoot.size() != 3) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int itemId = mustLoot.get(1);
		Integer currentNum = record.get(itemId);
		if (currentNum == null) {
			currentNum = 0;
		}

		int lootNum = req.getLootNum();
		if (lootNum != 1 && lootNum != 10) {
			handler.sendErrorMsgToPlayer(GameError.LOOT_NUM_ERROR);
			return;
		}

		// 如果满8个碎片
		if (currentNum >= staticLimitMgr.getNum(171)) {
			fullRedLoot(staticActRedDial, actRecord, lootNum, player, handler);
		} else if (lootNum == 1) {
			handleRedLoot1(staticActRedDial, actRecord, player, handler);
		} else if (lootNum == 10) {
			handleRedLoot10(staticActRedDial, actRecord, player, handler);
		} else {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

	}

	// 抽一次
	public void handleRedLoot1(StaticActRedDial staticActRedDial, ActRecord actRecord, Player player, ClientHandler handler) {
		List<List<Integer>> lootRate1 = staticActRedDial.getLootRate1();
		if (lootRate1 == null || lootRate1.size() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		List<Integer> price = staticActRedDial.getPrice();
		if (null == price || price.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		int gold = price.get(0);
		if (player.getGold() < gold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		List<Award> awards = activityManager.getRedDialAwards(lootRate1);
		if (awards.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.SERVER_EXCEPTION);
			return;
		}

		playerManager.subAward(player, AwardType.GOLD, 0, gold, Reason.ACT_ORANGE_DIAL);
		Award award = awards.get(0);
		playerManager.addAward(player, award, Reason.ACT_ORANGE_DIAL);
		int itemId = award.getId();
		List<Integer> mustLoot = staticActRedDial.getMustLoot();
		int chipId = mustLoot.get(1);
		if (chipId == itemId) {
			actRecord.updateRecordNum(chipId);
		}

		ActivityPb.RedDialRs.Builder builder = ActivityPb.RedDialRs.newBuilder();
		builder.setGold(player.getGold());
		builder.addAward(award.wrapPb());
		builder.setActChipNum(actRecord.getRecordNum(chipId));
		handler.sendMsgToPlayer(ActivityPb.RedDialRs.ext, builder.build());
	}

	// 抽十次
	public void handleRedLoot10(StaticActRedDial staticActRedDial, ActRecord actRecord, Player player, ClientHandler handler) {
		List<List<Integer>> lootRate2 = staticActRedDial.getLootRate2();
		if (lootRate2 == null || lootRate2.size() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		List<Integer> price = staticActRedDial.getPrice();
		int gold = price.get(1);
		if (player.getGold() < gold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		List<Award> totalAwards = new ArrayList<Award>();
		for (int i = 1; i <= 9; i++) {
			List<Award> awards = activityManager.getRedDialAwards(lootRate2);
			if (!awards.isEmpty()) {
				totalAwards.addAll(awards);
			}
		}
		List<Integer> mustLoot = staticActRedDial.getMustLoot();
		Award mustLootAward = new Award(mustLoot.get(0), mustLoot.get(1), mustLoot.get(2));
		totalAwards.add(mustLootAward);

		playerManager.subAward(player, AwardType.GOLD, 0, gold, Reason.ACT_ORANGE_DIAL);
		playerManager.addAward(player, totalAwards, Reason.ACT_ORANGE_DIAL);
		int chipId = mustLoot.get(1);
		actRecord.updateRecordNum(chipId);
		ActivityPb.RedDialRs.Builder builder = ActivityPb.RedDialRs.newBuilder();
		builder.setGold(player.getGold());
		builder.addAllAward(PbHelper.createAwardList(totalAwards));
		builder.setActChipNum(actRecord.getRecordNum(chipId));
		handler.sendMsgToPlayer(ActivityPb.RedDialRs.ext, builder.build());
	}

	// 单次和十次时，活动抽满8个碎片, 如果record满了八个则单抽和十抽都是从loot2里面获取
	public void fullRedLoot(StaticActRedDial staticActRedDial, ActRecord actRecord, int lootNum, Player player, ClientHandler handler) {
		List<List<Integer>> lootRate2 = staticActRedDial.getLootRate2();
		if (lootRate2 == null || lootRate2.size() <= 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int gold = 0;
		List<Integer> price = staticActRedDial.getPrice();
		if (null == price || price.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		if (lootNum == 1) {
			gold = price.get(0);
		} else if (lootNum == 10) {
			gold = price.get(1);
		}

		if (player.getGold() < gold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		List<Award> totalAwards = new ArrayList<Award>();
		for (int i = 1; i <= lootNum; i++) {
			List<Award> awards = activityManager.getRedDialAwards(lootRate2);
			if (!awards.isEmpty()) {
				totalAwards.addAll(awards);
			}
		}

		playerManager.subAward(player, AwardType.GOLD, 0, gold, Reason.ACT_ORANGE_DIAL);
		playerManager.addAward(player, totalAwards, Reason.ACT_ORANGE_DIAL);
		List<Integer> mustLoot = staticActRedDial.getMustLoot();
		int chipId = mustLoot.get(1);
		ActivityPb.RedDialRs.Builder builder = ActivityPb.RedDialRs.newBuilder();
		builder.setGold(player.getGold());
		builder.addAllAward(PbHelper.createAwardList(totalAwards));
		builder.setActChipNum(actRecord.getRecordNum(chipId));
		handler.sendMsgToPlayer(ActivityPb.RedDialRs.ext, builder.build());
	}

	public void makeEquip(ActivityPb.MakeEquipRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int itemId = req.getChipId();
		StaticProp staticProp = staticPropMgr.getStaticProp(itemId);
		if (staticProp == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int equipId = staticProp.getEquipId();
		StaticEquip staticEquip = equipDataMgr.getStaticEquip(equipId);
		if (staticEquip == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int itemNum = player.getItemNum(itemId);

		int configNum = staticProp.getNeedNum();
		if (itemNum < configNum) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_ITEM);
			return;
		}

		int freeSlot = equipManager.getFreeSlot(player);
		if (freeSlot <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_EQUIP_SLOT);
			return;
		}

		// sub item, and add equip
		playerManager.subAward(player, AwardType.PROP, itemId, configNum, Reason.MAKE_EQUIP);
		int keyId = playerManager.addAward(player, AwardType.EQUIP, equipId, 1, Reason.MAKE_EQUIP);
		Equip equip = player.getEquipItem(keyId);
		ActivityPb.MakeEquipRs.Builder builder = ActivityPb.MakeEquipRs.newBuilder();
		if (equip != null) {
			builder.setEquip(equip.wrapPb());
		}
		Item item = player.getItem(itemId);
		if (item == null) {
			CommonPb.Prop.Builder propBuilder = CommonPb.Prop.newBuilder();
			propBuilder.setPropId(itemId);
			propBuilder.setPropNum(0);
			builder.setProp(propBuilder);
		} else {
			builder.setProp(item.wrapPb());
		}
		handler.sendMsgToPlayer(ActivityPb.MakeEquipRs.ext, builder.build());
	}

	// 获取红装碎片
	public void getRedDial(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_ORANGE_DIAL);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 获取当前玩家的碎片个数
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		Map<Integer, Integer> record = actRecord.getRecord();
		int awardId = actRecord.getAwardId();
		StaticActRedDial staticActRedDial = staticActivityMgr.getRedDial(awardId);
		if (staticActRedDial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		List<Integer> mustLoot = staticActRedDial.getMustLoot();
		if (mustLoot == null || mustLoot.size() != 3) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int itemId = mustLoot.get(1);
		Integer currentNum = record.get(itemId);
		if (currentNum == null) {
			currentNum = 0;
		}

		ActivityPb.GetRedDialRs.Builder builder = ActivityPb.GetRedDialRs.newBuilder();
		builder.setChipNum(currentNum);
		handler.sendMsgToPlayer(ActivityPb.GetRedDialRs.ext, builder.build());

	}

	/**
	 * 紫装转盘
	 *
	 * @param handler
	 */
	public void actPurpDialRq(ActPurpDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Lord lord = player.getLord();

		ActRecord actRecord = activityManager.getActivityInfo(player, req.getId());
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int personScore = actRecord.getRecord(actRecord.getActivityId());
		int activityKeyId = actRecord.getAwardId();

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		boolean flag = true;

		List<StaticActAward> displayList = staticActivityMgr.getActAwardById(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		StaticActDialPurp common = staticActivityMgr.getDialPurp(activityKeyId);
		if (common == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		DialEntity dial = staticActivityMgr.getActDialMap(activityKeyId);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActPurpDialRs.Builder builder = ActPurpDialRs.newBuilder();
		Iterator<List<StaticActDial>> it = dial.getActDails().values().iterator();
		while (it.hasNext()) {
			List<StaticActDial> dialList = it.next();
			for (StaticActDial e : dialList) {
				if (actRecord.getReceived().containsKey(e.getDialId())) {// 判定该物品是否已获取
					builder.addActDial(PbHelper.createActDial(e, 1));
					continue;
				}
				builder.addActDial(PbHelper.createActDial(e, 0));
			}
		}
		int count = (int) actRecord.getStatus(1);// 已转次数
		int freeCount = common.getFreeTimes() - count < 0 ? 0 : common.getFreeTimes() - count;
		builder.setFree(freeCount);
		builder.setPrice(common.getOnePrice());
		builder.setTenPrice(common.getTenPrice());
		builder.setScore(personScore);
		if (personRank != null) {
			builder.setRank(personRank.getRank());
		}
		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActAward e : displayList) {
			if (e == null) {
				continue;
			}
			int rank = e.getCond();
			int keyId = e.getKeyId();

			ActPlayerRank actRank = activityData.getActRank(rank);
			// 个人数据
			int minScore = staticLimitMgr.getNum(226);// 上榜最低积分
			if (minScore == 0) {
				handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
				return;
			}

			if (flag && personRank != null && rank >= personRank.getRank() && personScore >= minScore) {
				if (rank > personRank.getRank()) {
					builder.addActDialRank(PbHelper.createActDialRank(personRank.getRank(), player.getLord().getNick(), personScore, e.getAwardPbList()));
				}
				flag = false;
			}
			if (actRank != null) {
				int score = (int) actRank.getRankValue();
				if (score >= minScore) {
					if (playerManager.getPlayer(actRank.getLordId()) != null) {
						// 积分大于20显示
						builder.addActDialRank(PbHelper.createActDialRank(rank, playerManager.getPlayer(actRank.getLordId()).getLord().getNick(), score, e.getAwardPbList()));
					}

				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}
			awardSet.add(keyId);
		}

		handler.sendMsgToPlayer(ActPurpDialRs.ext, builder.build());

	}

	/**
	 * 转紫装转盘
	 *
	 * @param req,handle
	 */
	public void doPurpDialRq(DoPurpDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		doCommonPurp(req, handler);
	}

	private void doCommonPurp(DoPurpDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int type = req.getType();
		int count = req.getCount();

		// 检查次数
		if (count != 1 && count != 10) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 获取活动记录
		int id = 0;
		if (type == 1) {
			id = ActivityConst.ACT_PURPLE_DIAL;
		}
		if (type == 2) {
			id = ActivityConst.ACT_ORANGE_DIAL;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, id);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 找到活动Id
		int awardId = actRecord.getAwardId();
		StaticActDialPurp dial = staticActivityMgr.getDialPurp(awardId);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 活动结构
		DialEntity dialEntity = staticActivityMgr.getActDialMap(awardId);
		if (dialEntity == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 单次转盘总次数
		long dialCount = actRecord.getStatus(type);

		int free = dial.getFreeTimes();
		int costGold = 0;
		// 单抽，如果免费次数没了，判断金币
		if (count == 1 && free <= dialCount) {
			if (player.getGold() < dial.getOnePrice()) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			if (type == 1) {
				playerManager.subAward(player, AwardType.GOLD, 0, dial.getOnePrice(), Reason.ACT_PURPLE_DIAL);
				costGold = dial.getOnePrice();
			}
			if (type == 2) {
				playerManager.subAward(player, AwardType.GOLD, 0, dial.getOnePrice(), Reason.ACT_ORANGE_DIAL);
				costGold = dial.getOnePrice();
			}
		} else if (count == 10) {
			if (player.getGold() < dial.getTenPrice()) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			if (type == 1) {
				playerManager.subAward(player, AwardType.GOLD, 0, dial.getTenPrice(), Reason.ACT_PURPLE_DIAL);
			}
			if (type == 2) {
				playerManager.subAward(player, AwardType.GOLD, 0, dial.getTenPrice(), Reason.ACT_ORANGE_DIAL);
			}
			costGold = dial.getTenPrice();
		}
		int minScore = staticLimitMgr.getNum(226);// 上榜最低积分
		if (minScore == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		// 普通转盘
		if (count == 1) {
			actRecord.putState(type, dialCount + 1);
		}
		DoPurpDialRs.Builder builder = DoPurpDialRs.newBuilder();
		for (int i = 0; i < count; i++) {
			StaticActDial actDial = dialEntity.getRandomDail(1, actRecord.getRecord());
			// 获取保底物品的配置
			List<StaticActDial> actDialMinGuaranteeList = staticActivityMgr.getActDialMinGuaranteeList(id, awardId);
			if (null != actDial && actDialMinGuaranteeList.size() > 0) {
				Iterator<StaticActDial> iterator = actDialMinGuaranteeList.iterator();
				while (iterator.hasNext()) {
					StaticActDial next = iterator.next();
					if (null == actDial || null == next || actDial.getDialId() != next.getDialId()) {
						actRecord.updateDailGuaranteeNum(next.getDialId());
						logger.error("ActivityID=" + activityBase.getActivityId() + "   保底物品未抽到次数=" + actRecord.getDailGuaranteeNum(next.getDialId()));
					}
					if (actDial.getDialId() == next.getDialId()) {
						actRecord.getDailGuarantee().put(next.getDialId(), 0);
						logger.error("ActivityID=" + activityBase.getActivityId() + "   保底物品抽到重置为0次");
						break;
					}

					Integer dailMinGuaranteeNum = actRecord.getDailMinGuaranteeNum(staticActivityMgr, next.getDialId(), actRecord.getAwardId(), 1);
					if (dailMinGuaranteeNum == null) {
						continue;
					}
					int dailGuaranteeNum = actRecord.getDailGuaranteeNum(next.getDialId());
					if (dailGuaranteeNum >= dailMinGuaranteeNum) {
						logger.error("ActivityID=" + activityBase.getActivityId() + "   保底物品未抽到次数=" + actRecord.getDailGuaranteeNum(next.getDialId()) + " >= " + dailMinGuaranteeNum);
						actDial = next;
						actRecord.getDailGuarantee().put(next.getDialId(), 0);
						logger.error("ActivityID=" + activityBase.getActivityId() + "   保底物品抽到重置为0次");
						break;
					}
				}
			}

			actRecord.getReceived().put(actDial.getDialId(), 1);
			int key = 0;
			if (type == 1) {
				key = playerManager.addAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), Reason.ACT_PURPLE_DIAL);
			}
			if (type == 2) {
				key = playerManager.addAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), Reason.ACT_ORANGE_DIAL);
			}

			activityManager.updActPersonPurp(player, actRecord.getActivityId(), 10, actRecord.getActivityId(), minScore);
			builder.addAward(PbHelper.createAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), key));
			builder.addPlace(actDial.getPlace());
			if (actDial.getKeyId() != 0) {
				actRecord.addRecord(actDial.getKeyId(), 1);
			}
			for (List<Integer> list : dial.getBuyAward()) {
				builder.addBuyAward(PbHelper.createAward(list.get(0), list.get(1), list.get(2)));
				if (type == 1) {
					playerManager.addAward(player, list.get(0), list.get(1), list.get(2), Reason.ACT_PURPLE_DIAL);
				}
				if (type == 2) {
					playerManager.addAward(player, list.get(0), list.get(1), list.get(2), Reason.ACT_ORANGE_DIAL);
				}
			}
		}
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(DoPurpDialRs.ext, builder.build());

		SpringUtil.getBean(EventManager.class).spin_the_wheel(player, Lists.newArrayList(id, // 活动类型
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									builder.getAwardList().toString(),
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									count));

		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(id).isAward(false).awardId(0).giftName("").roleId(player.roleId).vip(player.getVip()).costGold(costGold).channel(player.account.getChannel()).build());
	}

	public void fixSevenLoginSign(ActivityPb.FixSevenLoginSignRq req, FixSevenLoginSignHandler handler) {
		int activityId = ActivityConst.ACT_LOGIN_SEVEN;
		int keyId = req.getKeyId();
		StaticActAward actAward = staticActivityMgr.getActAward(keyId);
		if (actAward == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		if (!activityBase.canAward()) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 活动奖励和配置对不上
		if (actRecord.getAwardId() != actAward.getAwardId()) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 奖励已领取
		if (actRecord.getReceived().containsKey(keyId)) {
			handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}

		// int status = currentActivity(player, actRecord, actAward.getSortId());
		int status = actRecord.getCount();
		if (status < actAward.getCond()) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}
		ActivityPb.FixSevenLoginSignRs.Builder builder = ActivityPb.FixSevenLoginSignRs.newBuilder();
		List<List<Integer>> awardList = actAward.getAwardList();
		if (null == awardList || awardList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int size = awardList.size();
		if (playerManager.isEquipFull(awardList, player)) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
			return;
		}
//        if (actAward.getCost() > player.getGold()) {
//            handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
//            return;
//        }
//        playerManager.subAward(player, AwardType.GOLD, 0, actAward.getCost(), Reason.FIX_SEVEN_LOGIN);
		// 记录领取奖励记录
		actRecord.getReceived().put(keyId, 1);
		for (int i = 0; i < size; i++) {
			List<Integer> e = awardList.get(i);
			int type = e.get(0);
			int itemId = e.get(1);
			int count = e.get(2);
			if (type == AwardType.EQUIP && count > 1) {
				for (int c = 0; c < count; c++) {
					int itemkey = playerManager.addAward(player, type, itemId, 1, Reason.ACT_AWARD);
					builder.addAward(PbHelper.createAward(player, type, itemId, 1, itemkey));
				}
			} else {
				int itemkey = playerManager.addAward(player, type, itemId, count, Reason.ACT_AWARD);

				/**
				 * 活动资源产出日志埋点
				 */
				LogUser logUser = SpringUtil.getBean(LogUser.class);
				if (type == AwardType.RESOURCE) {
					logUser.roleResourceLog(new RoleResourceLog(player.getLord().getLordId(), player.account.getCreateDate(), player.getLevel(), player.getNick(), player.getVip(), player.getCountry(), player.getTitle(), player.getHonor(), player.getResource(itemId), RoleResourceLog.OPERATE_IN, itemId, ResOperateType.ACT_AWARD_IN.getInfoType(), count, player.account.getChannel()));
					int t = 0;
					int resType = itemId;
					switch (resType) {
					case ResourceType.IRON:
						t = IronOperateType.ACT_AWARD_IN.getInfoType();
						break;
					case ResourceType.COPPER:
						t = CopperOperateType.ACT_AWARD_IN.getInfoType();
						break;
					case ResourceType.OIL:
						t = OilOperateType.ACT_AWARD_IN.getInfoType();
						break;
					case ResourceType.STONE:
						t = StoneOperateType.ACT_AWARD_IN.getInfoType();
						break;
					default:
						break;
					}
					if (t != 0) {
						logUser.resourceLog(player, new RoleResourceChangeLog(player.roleId, player.getNick(), player.getLevel(), player.getTitle(), player.getHonor(), player.getCountry(), player.getVip(), player.account.getChannel(), 0, count, t), resType);
					}
				}

				builder.addAward(PbHelper.createAward(player, type, itemId, count, itemkey));
			}
		}
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(ActivityPb.FixSevenLoginSignRs.ext, builder.build());

	}

	/**
	 * 晶体转盘&&金币转盘&&军功转盘 xxx数量换取抽奖次数的活动都是这里 添加配置时记录对应活动id的换取数量(晶体转盘&&金币转盘&&军功转盘记录消耗钻石数量)
	 *
	 * @param handler
	 */
	public void actMasterDialRq(ActMasterDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, req.getId());
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		DialEntity dial = staticActivityMgr.getActDialMap(activityKeyId);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActMasterDialRs.Builder builder = ActMasterDialRs.newBuilder();

		int commonReceived = 0;
		StaticDialCost staticDialCost = staticActivityMgr.getDialCostMap().get(actRecord.getAwardId());
		if (staticDialCost == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int onePrice = staticDialCost.getCost();
		if (onePrice == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		Iterator<List<StaticActDial>> it = dial.getActDails().values().iterator();
		while (it.hasNext()) {
			List<StaticActDial> dialList = it.next();
			if (null == dialList || dialList.size() == 0) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}

			for (StaticActDial e : dialList) {
				if (actRecord.getReceived().containsKey(e.getDialId())) {// 判定该物品是否已获取
					builder.addActDial(PbHelper.createActDial(e, 1));
					if (e.getType() == 1) {
						commonReceived++;
					}
					continue;
				} else if (e.getLimit() > 0 && e.getItemType() == AwardType.ICON) {// 判断玩家身上有没有该头像
					if (player.hasIcon(e.getItemId())) {
						actRecord.putRecord(e.getKeyId(), 1);// 已有该头像
						builder.addActDial(PbHelper.createActDial(e, 1));
						continue;
					}
				} else if (e.getLimit() > 0 && e.getItemType() == AwardType.PROP) {// 判定该道具
					int itemCount = player.getItemNum(e.getItemId());
					int record = actRecord.getRecord(e.getKeyId());
					if (record < itemCount) {
						actRecord.putRecord(e.getKeyId(), itemCount);// 当前已有多少合成物品碎片
					}
					if (record >= e.getLimit() || itemCount >= e.getLimit()) {
						builder.addActDial(PbHelper.createActDial(e, 1));
						continue;
					}
				}
				builder.addActDial(PbHelper.createActDial(e, 0));
			}
		}
		int state = actRecord.getRecord(0);
		int count = actRecord.getRecord(1);// 已转次数
		int freeCount = state / onePrice - count;
		freeCount = freeCount < 0 ? 0 : freeCount;
		builder.setFree(freeCount);
		builder.setSchedule(state % onePrice);
		builder.setMaxSchedule(onePrice);

		handler.sendMsgToPlayer(ActMasterDialRs.ext, builder.build());
	}

	public void doMasterDialRq(DoMasterDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		doCommonMaster(req, handler);
	}

	private void doCommonMaster(DoMasterDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int type = 1;
		int count = req.getCount();

		// 检查次数
		if (count < 1 || count > 10) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		// 获取活动记录
		ActRecord actRecord = activityManager.getActivityInfo(player, req.getId());
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 找到活动Id
		int activityKeyId = actRecord.getAwardId();
		// 活动结构
		DialEntity dial = staticActivityMgr.getActDialMap(activityKeyId);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int onePrice = req.getId() == ActivityConst.ACT_CRYSTAL_DIAL ? staticLimitMgr.getNum(230) : staticLimitMgr.getNum(246);
		if (onePrice == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int dialCount = actRecord.getRecord(type);
		int state = actRecord.getRecord(0);
		int freeCount = state / onePrice - dialCount;
		freeCount = freeCount < 0 ? 0 : freeCount;
		// 单抽，如果免费次数没了，判断金币
		if (count >= 1 && count <= 10 && freeCount < count) {
			handler.sendErrorMsgToPlayer(GameError.COUNT_NOT_ENOUGH);
			return;
		}

		if (count >= 1 && count <= 10) {
			actRecord.putRecord(type, dialCount + count);
		}

		DoMasterDialRs.Builder builder = DoMasterDialRs.newBuilder();
		for (int i = 0; i < count; i++) {
			StaticActDial actDial = dial.getRandomDail(type, actRecord.getRecord());
			if (null == actDial) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}
			actRecord.getReceived().put(actDial.getDialId(), 1);
			playerManager.addAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), Reason.ACT_CRYSTAL_DIAL);
			builder.addAward(PbHelper.createAward(actDial.getItemType(), actDial.getItemId(), actDial.getItemCount()));
			builder.addPlace(actDial.getPlace());
			if (actDial.getKeyId() != 0) {
				actRecord.addRecord(actDial.getKeyId(), 1);
			}
		}
		handler.sendMsgToPlayer(DoMasterDialRs.ext, builder.build());
		SpringUtil.getBean(EventManager.class).spin_the_wheel(player, Lists.newArrayList(activityKeyId, // 活动类型
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									builder.getAwardList().toString(),
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									count));
	}

	/**
	 * 七日充值
	 *
	 * @param handler
	 */
	public void actSevenRechargeRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SEVEN_RECHARGE);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();

		ActSevenRechargeRs.Builder builder = ActSevenRechargeRs.newBuilder();

		int state = (int) actRecord.getStatus(0);
		builder.setState(state);

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}

		handler.sendMsgToPlayer(ActSevenRechargeRs.ext, builder.build());
	}

	/**
	 * 限时礼包
	 *
	 * @param handler
	 */
	public void actFlashGiftRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_FLASH_GIFT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int awardId = actRecord.getAwardId();
		ActFlashGiftRs.Builder builder = ActFlashGiftRs.newBuilder();
		int configNum = staticLimitMgr.getNum(224);

		List<StaticActPayGift> payGiftList = staticActivityMgr.getPayGiftList(awardId);
		if (null == payGiftList || payGiftList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		for (StaticActPayGift e : payGiftList) {

			int keyId = e.getPayGiftId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.setState(1);
			} else {// 未领取奖励
				builder.setState(0);
			}
			builder.setName(e.getName());
			builder.setPrice(e.getMoney());
			builder.setPayId(e.getPayGiftId());
			builder.setEndTime(actRecord.getStatus(0) + (configNum * TimeHelper.MINUTE_MS));
			builder.setDisplay(e.getDisplay());
			List<List<Integer>> sellList = e.getSellList();
			if (sellList.size() > 0) {
				for (List<Integer> sell : sellList) {
					int type = sell.get(0);
					int id = sell.get(1);
					int count = sell.get(2);
					builder.addAward(PbHelper.createAward(type, id, count));
				}
			}
		}

		handler.sendMsgToPlayer(ActFlashGiftRs.ext, builder.build());
	}

	/**
	 * 30日签到
	 *
	 * @param handler
	 */
	public void actCheckInRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DAILY_CHECKIN);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActDailyCheckInRs.Builder builder = ActDailyCheckInRs.newBuilder();
		Map<Integer, StaticDailyCheckin> condList = staticActivityMgr.getDailyCheckinAwards();
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_DAILY_CHECKIN);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int day = 1;// 第几次领奖
		int clean = staticLimitMgr.getNum(237);
		long currentTime = System.currentTimeMillis();
		long timeKey = 0;
		long beginKey = 1;
		if (!actRecord.getStatus().containsKey(timeKey)) {
			actRecord.putState(timeKey, currentTime);
		}
		if (!actRecord.getRecord().containsKey(0)) {
			actRecord.getRecord().put(0, 1);
		}
		Date now = new Date();
		Date pre = new Date();
		pre.setTime(actRecord.getStatus(timeKey));
		int maxKey = condList.size();

		Date toBegin = player.account.getCreateDate();
		Date begin = new Date();
		begin.setTime(toBegin.getTime());
		if (!actRecord.getStatus().containsKey(beginKey)) {
			actRecord.putState(beginKey, begin.getTime());
		} else {
			begin.setTime(actRecord.getStatus(beginKey));
		}
		int whichDay = TimeHelper.whichDay(clean, now, begin);

		while (whichDay > maxKey) {
			long nextBegin = TimeHelper.addDay(actRecord.getStatus(beginKey), maxKey);
			begin.setTime(nextBegin);
			whichDay = TimeHelper.whichDay(clean, now, begin);
			if (whichDay <= maxKey) {
				actRecord.getStatus().clear();
				actRecord.getRecord().clear();
				actRecord.getReceived().clear();
				actRecord.getShops().clear();
			}
			actRecord.putState(beginKey, nextBegin);
		}

		// 判断是否跨天
		if (TimeHelper.isNextDay(clean, now, pre)) {
			actRecord.putRecord(0, actRecord.getRecord(0) + 1);
			actRecord.putState(timeKey, now.getTime());
		}
		if (actRecord.getRecord().containsKey(0)) {
			day = actRecord.getRecord().get(0);
		} else {
			actRecord.getRecord().put(0, 1);
			day = actRecord.getRecord().get(0);
		}
		int param = 0;
		int dayCount = day;
		if (actRecord.getReceived().containsKey(day)) {
			builder.setCanAward(false);
		} else {
			dayCount--;
			builder.setCanAward(true);
		}
		for (StaticDailyCheckin e : condList.values()) {
			if (e == null) {
				continue;
			}
			int keyId = e.getId();
			if (day > keyId) {
				param = 1;
			} else {
				param = 0;
			}
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createDailyCheckinCondPb(e, 1, 0));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createDailyCheckinCondPb(e, 0, param));
				if (param == 1) {
					dayCount--;
				}
			}
		}
		builder.setLoginDay(day);
		builder.setState(dayCount);
		handler.sendMsgToPlayer(ActDailyCheckInRs.ext, builder.build());
	}

	/**
	 * 30日签到领奖
	 *
	 * @param handler
	 */
	public void doCheckInRq(DoDailyCheckInRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_DAILY_CHECKIN);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		DoDailyCheckInRs.Builder builder = DoDailyCheckInRs.newBuilder();

		StaticDailyCheckin staticDailyCheckin = staticActivityMgr.getDailyCheckinAwards().get(req.getKeyId());
		if (staticDailyCheckin == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		if (actRecord.getReceived().containsKey(req.getKeyId())) {// 已领取奖励
			// handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}

		int vip = player.getVip();

		int day = 1;// 第几次领奖
		if (actRecord.getRecord().containsKey(0)) {
			day = actRecord.getRecord().get(0);
		} else {
			actRecord.getRecord().put(0, 1);
			day = actRecord.getRecord().get(0);
		}

		boolean cond = true;
		if (day == req.getKeyId()) {
			cond = false;
		}
		if (cond) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}

		List<Integer> award = staticDailyCheckin.getAward();
		if (null == award || award.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		actRecord.getReceived().put(req.getKeyId(), 1);// 记录奖励
		int type = award.get(0);
		int id = award.get(1);
		int count = award.get(2);
		count = (staticDailyCheckin.getCritical() != 0) && (vip >= staticDailyCheckin.getCritical()) ? (count * 2) : count;
		int kid = playerManager.addAward(player, type, id, count, Reason.ACT_DAILY_CHECKIN);
		builder.addAward(PbHelper.createAward(player, type, id, count, kid));

		handler.sendMsgToPlayer(DoDailyCheckInRs.ext, builder.build());

		// 领取奖励后红点消失
		ActivityEventManager.getInst().activityTip(EventEnum.GET_ACTIVITY_AWARD_TIP, new CommonTipActor(player, actRecord, activityBase));

		SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_DAILY_CHECKIN, activityBase.getStaticActivity().getName(), ActivityConst.ACT_DAILY_CHECKIN);
		SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_DAILY_CHECKIN, activityBase.getStaticActivity().getName(), ActivityConst.ACT_DAILY_CHECKIN, activityBase.getBeginTime(), award);
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(ActivityConst.ACT_DAILY_CHECKIN).isAward(true).awardId(staticDailyCheckin.getAwardId()).giftName(activityBase.getStaticActivity().getName()).roleId(player.roleId).vip(player.getVip()).costGold(0).channel(player.account.getChannel()).build());
	}

	/**
	 * 世界征战
	 *
	 * @param handler
	 */
	public void actWorldBattle(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_WORLD_BATTLE);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActWorldBattleRs.Builder builder = ActWorldBattleRs.newBuilder();
		WorldBattleAward.Builder awardBug = WorldBattleAward.newBuilder();
		awardBug.setType(ActWorldBattleConst.KILL_MASTER);
		awardBug.setCount(currentActivity(player, actRecord, ActWorldBattleConst.KILL_MASTER));
		WorldBattleAward.Builder awardPaper = WorldBattleAward.newBuilder();
		awardPaper.setType(ActWorldBattleConst.COLLECT_PAPER);
		awardPaper.setCount(currentActivity(player, actRecord, ActWorldBattleConst.COLLECT_PAPER));
		WorldBattleAward.Builder awardAtkCity = WorldBattleAward.newBuilder();
		awardAtkCity.setType(ActWorldBattleConst.ATK_CITY);
		awardAtkCity.setCount(currentActivity(player, actRecord, ActWorldBattleConst.ATK_CITY));
		for (StaticActAward e : condList) {
			BattleAward.Builder award = BattleAward.newBuilder();
			int keyId = e.getKeyId();
			award.setId(keyId);
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				award.setIsAward(1);
			} else {// 未领取奖励
				award.setIsAward(0);
			}
			if (e.getSortId() == ActWorldBattleConst.KILL_MASTER) {
				awardBug.addBattleAward(award);
			} else if (e.getSortId() == ActWorldBattleConst.COLLECT_PAPER) {
				awardPaper.addBattleAward(award);
			} else {
				awardAtkCity.addBattleAward(award);
			}
		}
		builder.addAward(awardBug);
		builder.addAward(awardPaper);
		builder.addAward(awardAtkCity);
		handler.sendMsgToPlayer(ActWorldBattleRs.ext, builder.build());
	}

	/**
	 * 军备促销
	 *
	 * @param handler
	 */
	public void actArmsPayRq(ClientHandler handler) {
		// 判断用户是否存在
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		// 获取全服活动记录值
		ActivityData activityData = activityManager.getActivity(ActivityConst.ACT_ARMS_PAY);
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 获取个人活动的记录值
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ARMS_PAY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActPayArmsRs.Builder builder = ActPayArmsRs.newBuilder();
		List<StaticActPayArms> actPayArmsList = staticActivityMgr.getPayArmsList(actRecord.getAwardId());
		if (null == actPayArmsList || actPayArmsList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		for (StaticActPayArms e : actPayArmsList) {
			if (e == null) {
				continue;
			}
			long state = actRecord.getStatus(e.getPayArmsId());
			builder.addQuota(PbHelper.createPayArms(e, (int) state));
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());// 获取宝箱奖励
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		CommonPb.ArmsPayAward.Builder armsPayAward = getArmsPayAward(player, activityData, actRecord);
		builder.setArmsPayAward(armsPayAward);
		handler.sendMsgToPlayer(ActPayArmsRs.ext, builder.build());
	}

	/**
	 * 购买装备促销的物品
	 *
	 * @param req
	 * @param handler
	 */
	public void buyPayArmsRq(BuyPayArmsRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int country = player.getLord().getCountry();// 玩家的阵营

		// 获取全服活动记录值
		ActivityData activityData = activityManager.getActivity(ActivityConst.ACT_ARMS_PAY);
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ARMS_PAY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());// 获取宝箱奖励
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int payArmsId = req.getPayArmsId();
		StaticActPayArms staticActPayArms = staticActivityMgr.getPayArms(payArmsId);
		if (staticActPayArms == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		if (staticActPayArms.getSellList().size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		List<List<Integer>> sellList = staticActPayArms.getSellList();
		if (sellList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int state = (int) actRecord.getStatus(payArmsId);
		if (state >= staticActPayArms.getLimit()) {// 判断物品的购买的次数
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int gold = player.getLord().getGold();
		if (staticActPayArms.getTopup() > gold) {// 判断钻石不足
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		ActivityPb.BuyPayArmsRs.Builder builder = BuyPayArmsRs.newBuilder();

		actRecord.putState(payArmsId, state + 1);// 增加一次购买次数
		long addtion = activityData.getAddtion(country);
		activityData.putAddtion(country, addtion + staticActPayArms.getScore());// 增加阵营积分

		long score = activityData.getAddtion(country);// 阵营分数
		int total = (int) (score / 100);// 总宝箱数量
		int process = 0;
		if (total == 0) {
			process = (int) (score % 100);
		} else {
			process = (int) ((score - total * 100) % 100);
		}

		for (List<Integer> awards : sellList) {
			playerManager.addAward(player, awards.get(0), awards.get(1), awards.get(2), Reason.ACT_ARMS_PAY);
		}
		playerManager.subAward(player, AwardType.GOLD, 0, staticActPayArms.getTopup(), Reason.ACT_ARMS_PAY);// 扣除钻石

		CommonPb.ArmsPayAward.Builder armsPayAward = getArmsPayAward(player, activityData, actRecord);

		builder.setGold(player.getLord().getGold());
		builder.setQuota(PbHelper.createPayArms(staticActPayArms, (int) state));
		builder.setArmsPayAward(armsPayAward);

		handler.sendMsgToPlayer(BuyPayArmsRs.ext, builder.build());
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_ARMS_PAY);
		String name = "";
		if (activityBase != null) {
			name = activityBase.getStaticActivity().getName();
		}

		ActivityEventManager.getInst().activityTip(EventEnum.BUY_PAY_ARMS, new CommonTipActor(player, actRecord, activityBase));

		SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_ARMS_PAY, name, staticActPayArms.getPayArmsId());
		SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_ARMS_PAY, name, staticActPayArms.getPayArmsId(), new Date(), sellList);
	}

	/**
	 * //领取军备促销奖励
	 *
	 * @param req
	 * @param handler
	 */
	public void doPayArmsAward(DoPayArmsAwardRq req, ClientHandler handler) {
		int keyId = req.getKeyId();
		int count = req.getCount();

		StaticActAward actAward = staticActivityMgr.getActAward(keyId);
		if (actAward == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_ARMS_PAY);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 获取全服活动记录值
		ActivityData activityData = activityManager.getActivity(ActivityConst.ACT_ARMS_PAY);
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ARMS_PAY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 活动奖励和配置对不上
		if (actRecord.getAwardId() != actAward.getAwardId()) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		if (count <= 0) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		CommonPb.ArmsPayAward.Builder armsPayAward = getArmsPayAward(player, activityData, actRecord);
		int current = armsPayAward.getCount();
		if (current < count) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}

		ActivityPb.DoPayArmsAwardRs.Builder builder = ActivityPb.DoPayArmsAwardRs.newBuilder();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		List<CommonPb.Award> awardList = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			for (StaticActAward e : condList) {
				Integer received = actRecord.getReceived().get(e.getKeyId());
				if (received != null) {
					actRecord.getReceived().put(e.getKeyId(), received + 1);
				} else {
					actRecord.getReceived().put(e.getKeyId(), 1);
				}
				awardList.addAll(e.getAwardPbList());
				List<List<Integer>> award = e.getAwardList();
				if (award.size() > 0) {
					for (List<Integer> awd : award) {
						playerManager.addAward(player, awd.get(0), awd.get(1), awd.get(2), Reason.ACT_ARMS_PAY);// 添加奖励
					}
				}
			}
		}
		List<Award> awardLists = PbHelper.finilAward(awardList);

		if (awardLists != null && awardLists.size() > 0) {
			builder.addAllAward(PbHelper.createAwardList(awardLists));
		}
		handler.sendMsgToPlayer(DoPayArmsAwardRs.ext, builder.build());

		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	private CommonPb.ArmsPayAward.Builder getArmsPayAward(Player player, ActivityData activityData, ActRecord actRecord) {
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());// 获取宝箱奖励

		int country = player.getLord().getCountry();// 玩家阵营
		long score = activityData.getAddtion(country);// 阵营分数
		int total = (int) (score / 100);// 总宝箱数量
		int process = 0;
		if (total == 0) {
			process = (int) (score % 100);
		} else {
			process = (int) ((score - total * 100) % 100);
		}

		CommonPb.ArmsPayAward.Builder armsPayAward = CommonPb.ArmsPayAward.newBuilder();
		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			armsPayAward.setKeyId(keyId);
			List<List<Integer>> awardList = e.getAwardList();
			for (List<Integer> award : awardList) {
				if (award.size() != 3) {
					continue;
				}
				int type = award.get(0);
				int id = award.get(1);
				int count = award.get(2);
				armsPayAward.addAward(PbHelper.createAward(type, id, count));
			}

			Integer received = actRecord.getReceived().get(e.getKeyId());
			if (received == null) {
				armsPayAward.setCount(total);
			} else {
				armsPayAward.setCount(total - received);
			}
			armsPayAward.setProcess(process);
		}
		return armsPayAward;
	}

	/**
	 * 月卡大礼包
	 *
	 * @param handler
	 */
	public void actMonthGift(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_MONTH_GIFT);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MONTH_GIFT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int awardId = actRecord.getAwardId();
		ActMonthGiftRs.Builder builder = ActMonthGiftRs.newBuilder();
		int configNum = staticLimitMgr.getNum(253);

		List<StaticActPayGift> payGiftList = staticActivityMgr.getPayGiftList(awardId);
		if (null == payGiftList || payGiftList.size() == 0 || payGiftList.size() > 1) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		long timeKey = 0;
		if (!actRecord.getStatus().containsKey(timeKey)) {
			actRecord.putState(timeKey, (System.currentTimeMillis() + configNum * TimeHelper.MINUTE_MS));
			// 推送双卡大礼包
			ActivityEventManager.getInst().activityTip(EventEnum.SYN_ACTIVITY_AND_DISAPPERAR, new CommonTipActor(player, actRecord, activityBase));
		}
		for (StaticActPayGift e : payGiftList) {

			int keyId = e.getPayGiftId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.setState(1);
			} else {// 未领取奖励
				builder.setState(0);
			}
			builder.setName(e.getName());
			builder.setPrice(e.getMoney());
			builder.setPayId(e.getPayGiftId());
			builder.setEndTime(actRecord.getStatus(timeKey));
			builder.setDisplay(e.getDisplay());
			List<List<Integer>> sellList = e.getSellList();
			if (sellList.size() > 0) {
				for (List<Integer> sell : sellList) {
					int type = sell.get(0);
					int id = sell.get(1);
					int count = sell.get(2);
					builder.addAward(PbHelper.createAward(type, id, count));
				}
			}
		}
		handler.sendMsgToPlayer(ActMonthGiftRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 许愿池
	 *
	 * @param handler
	 */
	public void actHope(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HOPE);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		Map<Integer, StaticActHope> staticActHopeMap = staticActivityMgr.getStaticActHopeMap();
		if (null == staticActHopeMap || staticActHopeMap.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActHopeRs.Builder builder = ActHopeRs.newBuilder();
		//
		int level = 0;
		boolean startLevel = true;

		for (StaticActHope e : staticActHopeMap.values()) {
			int keyId = e.getLevel();
			if (!actRecord.getReceived().containsKey(keyId) && startLevel) {// 未领取奖励
				level = keyId;
				startLevel = false;
			}
		}
		StaticActHope staticActHope = staticActHopeMap.get(level);
		if (staticActHope != null) {
			builder.setLevel(staticActHope.getLevel()).setCost(staticActHope.getCost()).setMaxAward(staticActHope.getMaxAward()).setMixAward(staticActHope.getMixAward());
		}

		handler.sendMsgToPlayer(ActHopeRs.ext, builder.build());
	}

	/**
	 * 许愿池领奖
	 *
	 * @param handler
	 */
	public void doHope(DoHopeRq req, ClientHandler handler) {
		int level = req.getLevel();

		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_HOPE);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HOPE);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		Map<Integer, StaticActHope> staticActHopeMap = staticActivityMgr.getStaticActHopeMap();
		if (null == staticActHopeMap || staticActHopeMap.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		StaticActHope staticActHope = staticActHopeMap.get(level);
		StaticActHope next = staticActHopeMap.get(level + 1);
		if (null == staticActHope) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		if (actRecord.getReceived().containsKey(staticActHope.getLevel())) {
			handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}
		if (player.getGold() < staticActHope.getCost()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		DoHopeRs.Builder builder = DoHopeRs.newBuilder();
		playerManager.subGold(player, staticActHope.getCost(), Reason.ACT_HOPE);
		int got = RandomHelper.threadSafeRand(staticActHope.getMixAward(), staticActHope.getMaxAward());
		actRecord.getReceived().put(level, 0);
		playerManager.addGold(player, got, Reason.ACT_HOPE);
		builder.setGold(player.getGold()).setGot(got);
		if (next != null) {
			builder.setCost(next.getCost()).setLevel(next.getLevel()).setMixAward(next.getMixAward()).setMaxAward(next.getMaxAward());
		}
		if (level >= 4) {
			chatManager.sendWorldChat(ChatId.ACT_HOPE, player.getNick(), String.valueOf(got));
		}
		handler.sendMsgToPlayer(DoHopeRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
		SpringUtil.getBean(EventManager.class).act_Hope(player, Lists.newArrayList(staticActHope.getCost(), got));
		SpringUtil.getBean(LogUser.class).act_hope_log(ActHopeLog.builder().channelId(player.account.getChannel()).serverId(player.account.getServerId()).level(staticActHope.getLevel()).lordId(player.roleId).costGold(staticActHope.getCost()).getGold(got).startTime(new Date()).vip(player.getVip()).build());
	}

	/**
	 * 装备精研
	 *
	 * @param handler
	 */
	public void actWashEquip(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_WASH_EQUIP);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActWashEquiptRs.Builder builder = ActWashEquiptRs.newBuilder();
		List<StaticActEquipUpdate> staticActEquipUpdateList = staticActivityMgr.getStaticActEquipUpdateList();
		if (null == staticActEquipUpdateList || staticActEquipUpdateList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		//
		int status = (int) actRecord.getStatus(StaticActEquipUpdate.WASH_CONUT);
		int got = (int) actRecord.getStatus(StaticActEquipUpdate.PAY_CONUT);
		for (StaticActEquipUpdate e : staticActEquipUpdateList) {
			ActWashEquipt actWashEquipt = PbHelper.createActWashEquipt(e);
			builder.addActWashEquipt(actWashEquipt);
			int keyId = e.getKeyId();
			boolean canAward = e.getType() == 1 ? got >= e.getCond() : status >= e.getCond();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCond(e, 1, canAward));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCond(e, 0, canAward));
			}
		}
		builder.setGot(got);
		builder.setState(status);
		handler.sendMsgToPlayer(ActWashEquiptRs.ext, builder.build());
	}

	/**
	 * 通行证活动
	 *
	 * @param handler
	 */
	public void actPassPortRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityData activityData = activityManager.getActivity(ActivityConst.ACT_PASS_PORT);
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_PASS_PORT);
		if (activityBase == null) {
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticPassPortAward> passPortList = staticActivityMgr.getPassPortList(actRecord.getAwardId());
		if (passPortList == null || passPortList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		List<StaticPayPassPort> staticPayPassPortList = staticActivityMgr.getStaticPayPassPortList(actRecord.getAwardId());
		if (staticPayPassPortList == null || staticPayPassPortList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		ActivityPb.ActPassPortRs.Builder builder = ActivityPb.ActPassPortRs.newBuilder();

		Map<Integer, Integer> record = actRecord.getRecord();
		if (record.get(0) == null) {
			record.put(0, 0);
		}
		if (record.get(1) == null) {
			record.put(1, 0);
		}
		Integer beforeScore = record.get(0);
		Integer isBuy = record.get(1);
		int lv = staticActivityMgr.getPassPortLv(beforeScore);

		builder.setLv(lv);
		builder.setScore(beforeScore.intValue());
		builder.setIsBuy(isBuy.intValue());

		for (StaticPassPortAward e : passPortList) {
			int keyId = e.getId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				CommonPb.ActPassPortAward actPassPortAwardPb = PbHelper.createActPassPortAwardPb(e, 1);
				builder.addActPassPortAward(actPassPortAwardPb);
			} else {// 未领取奖励
				CommonPb.ActPassPortAward actPassPortAwardPb = PbHelper.createActPassPortAwardPb(e, 0);
				builder.addActPassPortAward(actPassPortAwardPb);
			}
		}
		List<CommonPb.ActPassPortTask.Builder> passPortTask = activityManager.getPassPortTask(player);
		if (null == passPortTask) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		for (CommonPb.ActPassPortTask.Builder builders : passPortTask) {
			builder.addActPassPortTask(builders);
		}

		for (StaticPayPassPort staticPayPassPort : staticPayPassPortList) {
			builder.addPayItem(PbHelper.createStaticPayPassPort(staticPayPassPort));
		}

		handler.sendMsgToPlayer(ActivityPb.ActPassPortRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
//		com.game.util.LogHelper.MESSAGE_LOGGER.info("拉取通行证协议");
	}

	/**
	 * 装备精研领奖
	 *
	 * @param
	 */
	public void doWashEquip(DoWashEquiptRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_WASH_EQUIP);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_WASH_EQUIP);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		DoWashEquiptRs.Builder builder = DoWashEquiptRs.newBuilder();
		List<StaticActEquipUpdate> staticActEquipUpdateList = staticActivityMgr.getStaticActEquipUpdateList();
		if (null == staticActEquipUpdateList || staticActEquipUpdateList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		if (actRecord.getReceived().containsKey(req.getKeyId())) {// 已领取奖励
			handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}

		//
		int status = (int) actRecord.getStatus(StaticActEquipUpdate.WASH_CONUT);
		int got = (int) actRecord.getStatus(StaticActEquipUpdate.PAY_CONUT);
		StaticActEquipUpdate staticActEquipUpdate = null;
		for (StaticActEquipUpdate e : staticActEquipUpdateList) {
			if (req.getKeyId() == e.getKeyId()) {
				if (e == null) {
					handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
					return;
				}
				ActWashEquipt actWashEquipt = PbHelper.createActWashEquipt(e);
				boolean canAward = e.getType() == 1 ? got >= e.getCond() : status >= e.getCond();
				if (!canAward) {
					handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
					return;
				}
				for (List<Integer> list : e.getAwardList()) {
					playerManager.addAward(player, list.get(0), list.get(1), list.get(2), Reason.ACT_EQUIPT_WASH);
				}
				builder.addAllAward(actWashEquipt.getAwardList());
				staticActEquipUpdate = e;
			}
		}

		actRecord.getReceived().put(req.getKeyId(), 0);
		handler.sendMsgToPlayer(DoWashEquiptRs.ext, builder.build());
		SpringUtil.getBean(EventManager.class).act_equip_wash(player, Lists.newArrayList(staticActEquipUpdate.getSortId()));
		String name = "";
		if (activityBase != null) {
			name = activityBase.getStaticActivity().getName();
		}
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
		SpringUtil.getBean(EventManager.class).join_activity(player, ActivityConst.ACT_WASH_EQUIP, name, staticActEquipUpdate.getKeyId());
		SpringUtil.getBean(EventManager.class).complete_activity(player, ActivityConst.ACT_WASH_EQUIP, name, staticActEquipUpdate.getKeyId(), new Date(), staticActEquipUpdate.getAwardList());
	}

	/**
	 * 装备精研奖励补发
	 *
	 * @param
	 */
	public void sendActWashEquiptOffAward(ActivityBase activityBase, ActivityData activityData) {
		List<StaticActEquipUpdate> staticActEquipUpdateList = staticActivityMgr.getStaticActEquipUpdateList();
		if (null == staticActEquipUpdateList || staticActEquipUpdateList.size() == 0) {
			return;
		}
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			// 已达成条件,未领取奖励,补发奖励邮件
			List<CommonPb.Award> awardList = new ArrayList<>();
			//
			int status = (int) actRecord.getStatus(StaticActEquipUpdate.WASH_CONUT);
			int got = (int) actRecord.getStatus(StaticActEquipUpdate.PAY_CONUT);
			for (StaticActEquipUpdate e : staticActEquipUpdateList) {
				if (e == null) {
					return;
				}
				ActWashEquipt actWashEquipt = PbHelper.createActWashEquipt(e);
				boolean canAward = e.getType() == 1 ? got >= e.getCond() : status >= e.getCond();
				boolean isAward = actRecord.getReceived().containsKey(e.getKeyId());
				if (canAward && !isAward) {
					actRecord.getReceived().put(e.getKeyId(), 1);
					awardList.addAll(actWashEquipt.getAwardList());
				}
			}
			if (awardList.size() > 0) {
				playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}
		}
	}

	/**
	 * 建造礼包
	 *
	 * @param handler
	 */
	public void actBuildingGiftRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_BUILD_QUE);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		int openDays = staticLimitMgr.getAddtion(254).get(2);
		Date openTime = new Date(actRecord.getBeginTime() * TimeHelper.SECOND_MS);
		Date cleanTime = new Date(openTime.getTime() + openDays * TimeHelper.DAY_MS);

		int awardId = actRecord.getAwardId();
		getBuildGiftRs.Builder builder = getBuildGiftRs.newBuilder();

		List<StaticActPayGift> payGiftList = staticActivityMgr.getPayGiftList(awardId);
		if (null == payGiftList || payGiftList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		for (StaticActPayGift e : payGiftList) {
			int keyId = e.getPayGiftId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.setState(1);
			} else {// 未领取奖励
				builder.setState(0);
			}
			builder.setName(e.getName());
			builder.setPrice(e.getMoney());
			builder.setPayId(e.getPayGiftId());
			builder.setEndTime(cleanTime.getTime());
			builder.setDisplay(e.getDisplay());
			List<List<Integer>> sellList = e.getSellList();
			if (sellList.size() > 0) {
				for (List<Integer> sell : sellList) {
					int type = sell.get(0);
					int id = sell.get(1);
					int count = sell.get(2);
					builder.addAward(PbHelper.createAward(type, id, count));
				}
			}
		}

		handler.sendMsgToPlayer(getBuildGiftRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 领取通行证活动奖励
	 *
	 * @param req
	 * @param handler
	 */
	public void doHirDoPassPortAwardRq(ActivityPb.DoPassPortAwardRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_PASS_PORT);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int type = req.getType();
		Map<Integer, Integer> record = actRecord.getRecord();
		if (record.get(0) == null) {
			record.put(0, 0);
		}
		if (record.get(1) == null) {
			record.put(1, 0);
		}

		Integer score = record.get(0);
		Integer isBuy = record.get(1);
		int lv = staticActivityMgr.getPassPortLv(score);

		ActivityPb.DoPassPortAwardRs.Builder doPassPortAward = ActivityPb.DoPassPortAwardRs.newBuilder();
		if (type == 1) {// 活动奖励
			List<StaticPassPortAward> passPortList = staticActivityMgr.getPassPortList(actRecord.getAwardId());
			if (passPortList == null || passPortList.size() == 0) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}

			List<CommonPb.Award> awardList = new ArrayList<>();
			for (StaticPassPortAward staticPassPortAward : passPortList) {
				if (!actRecord.getReceived().containsKey(staticPassPortAward.getId()) && lv >= staticPassPortAward.getLv()) {
					if (staticPassPortAward.getType() == 2 && isBuy == 0) {
						continue;
					}
					List<List<Integer>> award = staticPassPortAward.getAward();
					if (award.size() > 0) {
						for (List<Integer> awd : award) {
							int keyId = playerManager.addAward(player, awd.get(0), awd.get(1), awd.get(2), Reason.PASSPORT_SCORE);
							if (awd.get(0) == AwardType.EQUIP) {
								doPassPortAward.addAward(PbHelper.createAward(player, awd.get(0), awd.get(1), awd.get(2), keyId));
								continue;
							}
							awardList.add(PbHelper.createAward(awd.get(0), awd.get(1), awd.get(2)).build());
						}
					}
					actRecord.getReceived().put(staticPassPortAward.getId(), 1);
				}
			}

			List<Award> awardListsFinal = PbHelper.finilAward(awardList);// 合并奖励
			if (awardListsFinal != null && awardListsFinal.size() > 0) {
				doPassPortAward.addAllAward(PbHelper.createAwardList(awardListsFinal));
			}
		} else if (type == 2) {// 任务奖励
			int taskId = req.getTaskId();
			if (taskId == 0) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			}

			Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();
			ActPassPortTask actPassPortTask = tasks.get(taskId);
			if (actPassPortTask == null) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			}
			StaticPassPortTask passPortTask = staticActivityMgr.getPassPortTask(actPassPortTask.getId());
			if (passPortTask == null) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}
			if (actPassPortTask.getIsAward() == 0 && actPassPortTask.getProcess() >= passPortTask.getCond()) {
				List<Integer> award = passPortTask.getAward();
				playerManager.addAward(player, award.get(0), award.get(1), award.get(2), Reason.PASSPORT_SCORE);
				actPassPortTask.setIsAward(1);
				doPassPortAward.addAward(PbHelper.createAward(award.get(0), award.get(1), award.get(2)));
			}
			doPassPortAward.setType(actPassPortTask.getType());
		} else if (type == 3) {// 一键领取任务奖励
			int taskType = req.getTaskType();
			if (taskType == 0) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			}

			List<CommonPb.Award> awardList = new ArrayList<>();

			Map<Integer, ActPassPortTask> tasks = actRecord.getTasks();
			Set<Entry<Integer, ActPassPortTask>> entrySet = tasks.entrySet();
			for (Entry<Integer, ActPassPortTask> actPassPortTaskEntry : entrySet) {
				ActPassPortTask actPassPortTask = actPassPortTaskEntry.getValue();
				if (actPassPortTask == null) {
					continue;
				}
				StaticPassPortTask passPortTask = staticActivityMgr.getPassPortTask(actPassPortTask.getId());
				if (actPassPortTask.getIsAward() == 0 && actPassPortTask.getProcess() >= passPortTask.getCond() && actPassPortTask.getType() == taskType) {
					List<Integer> award = passPortTask.getAward();
					playerManager.addAward(player, award.get(0), award.get(1), award.get(2), Reason.PASSPORT_SCORE);
					actPassPortTask.setIsAward(1);
					awardList.add(PbHelper.createAward(award.get(0), award.get(1), award.get(2)).build());
				}
			}

			List<Award> awardListsFinal = PbHelper.finilAward(awardList);// 合并奖励
			if (awardListsFinal != null && awardListsFinal.size() > 0) {
				doPassPortAward.addAllAward(PbHelper.createAwardList(awardListsFinal));
			}
			doPassPortAward.setType(taskType);
		} else {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		lv = staticActivityMgr.getPassPortLv(record.get(0));
		doPassPortAward.setScore(record.get(0));
		doPassPortAward.setLv(lv);
		// 判断客户端是否为需要弹出显示进阶奖励
		boolean flag = false;
		if (isBuy == 0 && type == 1) {
			flag = actRecord.isFirstReceive();
		}
		doPassPortAward.setFirstReceive(flag);
		handler.sendMsgToPlayer(ActivityPb.DoPassPortAwardRs.ext, doPassPortAward.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/***
	 * 大杀四方
	 *
	 * @param req
	 * @param handler
	 */
	public void actKillAllRq(ActivityPb.ActKillAllRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_KILL_ALL);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActKillAllRs.Builder builder = ActKillAllRs.newBuilder();
		builder.setKill(actRecord.getRecord(1));
		for (StaticActAward config : condList) {
			CommonPb.ActivityCond.Builder activityPb = CommonPb.ActivityCond.newBuilder();
			activityPb.setKeyId(config.getKeyId());
			activityPb.setCond(config.getCond());
			if (actRecord.getReceived().containsKey(config.getKeyId())) {// 已领取奖励
				activityPb.setIsAward(1);
			} else {// 未领取奖励
				activityPb.setIsAward(0);
			}
			List<List<Integer>> awardList = config.getAwardList();
			for (List<Integer> e : awardList) {
				if (e.size() != 3) {
					continue;
				}
				int type = e.get(0);
				int id = e.get(1);
				int count = e.get(2);
				activityPb.addAward(PbHelper.createAward(type, id, count));
			}
			activityPb.setParam(config.getParam() == null ? "" : config.getParam());
			activityPb.setDesc(StringUtil.isNullOrEmpty(config.getDesc()) ? "" : config.getDesc());
			builder.addActivityCond(activityPb);
		}
		handler.sendMsgToPlayer(ActKillAllRs.ext, builder.build());
	}

	/***
	 * 每日远征
	 *
	 * @param req
	 * @param handler
	 */
	public void actDailyExpeditionRq(ActivityPb.ActDailyExpeditionRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_DAYLY_EXPEDITION);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActDailyExpeditionRs.Builder builder = ActDailyExpeditionRs.newBuilder();
		Long count = actRecord.getStatus(-1L);
		if (count == null) {
			count = 0L;
		}
		builder.setCount(count.intValue());
		for (StaticActAward config : condList) {
			CommonPb.ActivityCond.Builder activityPb = CommonPb.ActivityCond.newBuilder();
			activityPb.setKeyId(config.getKeyId());
			activityPb.setCond(config.getCond());
			if (actRecord.getReceived().containsKey(config.getKeyId())) {// 已领取奖励
				activityPb.setIsAward(1);
			} else {// 未领取奖励
				activityPb.setIsAward(0);
			}
			List<List<Integer>> awardList = config.getAwardList();
			for (List<Integer> e : awardList) {
				if (e.size() != 3) {
					continue;
				}
				int type = e.get(0);
				int id = e.get(1);
				int c = e.get(2);
				activityPb.addAward(PbHelper.createAward(type, id, c));
			}
			activityPb.setParam(config.getParam() == null ? "" : config.getParam());
			builder.addActivityCond(activityPb);
		}
		handler.sendMsgToPlayer(ActDailyExpeditionRs.ext, builder.build());
	}

	/***
	 * 每日充值
	 *
	 * @param req
	 * @param handler
	 */
	public void actDailyRechargeRq(ActivityPb.ActDailyRechargeRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_DAYLY_RECHARGE);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		// 日期 金额
		List<Entry<Integer, Integer>> recordList = actRecord.getRecord().entrySet().stream().sorted(Comparator.comparingInt(e -> e.getKey())).collect(Collectors.toList());
		int index = 0;
		Map<Integer, Integer> mapSortDate = new HashMap<>(16);
		for (int i = 0; i < condList.size(); i++) {
			StaticActAward config = condList.get(i);
			if (config.getCond() != 0) {
				// 当天充值数
				Integer topUp = 0;
				if (recordList.size() > index) {
					topUp = recordList.get(index).getValue();
				}
				if (topUp == null) {
					topUp = 0;
				}
				if (topUp >= config.getCond()) {
					mapSortDate.put(config.getSortId(), recordList.get(index).getKey());
				}
				index++;
			}
		}
		int currentDay = GameServer.getInstance().currentDay;
		ActDailyRechargeRs.Builder builder = ActDailyRechargeRs.newBuilder();
		Integer curRecharge = actRecord.getRecord().get(currentDay);
		if (curRecharge == null) {
			curRecharge = 0;
		}
		builder.setGold(curRecharge);
		int param = 0;
		for (StaticActAward config : condList) {
			CommonPb.ActivityCond.Builder activityPb = CommonPb.ActivityCond.newBuilder();
			activityPb.setKeyId(config.getKeyId());
			activityPb.setCond(config.getCond());
			if (actRecord.getReceived().containsKey(config.getKeyId())) {// 已领取奖励
				activityPb.setIsAward(1);
			} else {// 未领取奖励
				activityPb.setIsAward(0);
			}
			boolean add = true;
			if (config.getCond() == 0) {
				if (actRecord.getReceived().containsKey(config.getKeyId())) {
					activityPb.setCanAward(false);
				} else {
					activityPb.setCanAward(true);
				}
				activityPb.setParam("0");
			} else {
				Integer dayRechargeIsRdeay = mapSortDate.get(config.getSortId());
				// 当日充值完成
				if (dayRechargeIsRdeay != null) {
					// 奖励已领取
					if (actRecord.getReceived().containsKey(config.getKeyId())) {
						activityPb.setCanAward(false);
					} else {
						activityPb.setCanAward(true);
					}
					// 当天充值完成
					if (dayRechargeIsRdeay.intValue() == currentDay) {
						param = 0;
					} else {
						add = false;
					}
				} else {
					activityPb.setCanAward(false);
				}
			}
			activityPb.setParam(param + "");
			if (config.getCond() != 0 && add) {
				param++;
				if (param == 2) {
					param++;
				}
			}
			add = true;
			List<List<Integer>> awardList = config.getAwardList();
			for (List<Integer> e : awardList) {
				if (e.size() != 3) {
					continue;
				}
				int type = e.get(0);
				int id = e.get(1);
				int count = e.get(2);
				activityPb.addAward(PbHelper.createAward(type, id, count));
			}
			activityPb.setDesc(config.getDesc());
			builder.addActivityCond(activityPb);
		}
		handler.sendMsgToPlayer(ActDailyRechargeRs.ext, builder.build());
	}

	/**
	 * 0元礼包
	 *
	 * @param handler
	 */
	public void actZeroGiftRq(ActivityPb.ActZeroGiftRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_ZERO_GIFT);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int awardId = actRecord.getAwardId();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId);
		if (condList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		List<StaticActFreeBuy> footList = staticActivityMgr.getActFreeBuy(awardId);
		if (footList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActZeroGiftRs.Builder builder = ActZeroGiftRs.newBuilder();

		for (StaticActFreeBuy foot : footList) {
			if (null == foot) {
				continue;
			}
			int sortId = foot.getSortId();

			// 0.未购买 1-N购买后的第几天
			int state = currentActivity(player, actRecord, sortId);
			if (state == 0 && activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
				state = -1;
			}
			CommonPb.GrowFoot.Builder footBuilder = PbHelper.createGrowFoot(foot.getFootId(), foot.getType(), state);
			for (StaticActAward e : condList) {
				if (e.getSortId() == sortId) {
					if (actRecord.getReceived().containsKey(e.getKeyId())) {
						footBuilder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
					} else {
						footBuilder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
					}
				}
			}
			builder.addGrowFoot(footBuilder.build());
		}
		handler.sendMsgToPlayer(ActZeroGiftRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 参与0元礼包
	 *
	 * @param handler
	 */
	public void actZeroGiftDoRq(DoActZeroGiftRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_ZERO_GIFT);
		if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		ActivityData activityData = activityManager.getActivity(activityBase);

		int awardId = actRecord.getAwardId();
		int footId = req.getFootId();

		StaticActFreeBuy staticActFoot = staticActivityMgr.getActFreeBuy(awardId, footId);
		if (staticActFoot == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		int sortId = staticActFoot.getSortId();

		// 当前状况
		int state = currentActivity(player, actRecord, sortId);
		if (state != 0) {// 已参与
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_GOT);
			return;
		}

		// 金币是否足够
		if (player.getGold() < staticActFoot.getPrice()) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}

		playerManager.subAward(player, AwardType.GOLD, 0, staticActFoot.getPrice(), Reason.ACT_FOOT);
		actRecord.putState(sortId, TimeHelper.getZeroOfDay());
		// 记录玩家参与了屯田计划
		activityData.putAddtion(handler.getRoleId(), handler.getRoleId());

		DoActZeroGiftRs.Builder builder = DoActZeroGiftRs.newBuilder();
		builder.setGold(player.getGold());

		handler.sendMsgToPlayer(DoActZeroGiftRs.ext, builder.build());

		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 钻石排行榜
	 *
	 * @param handler
	 */

	@Deprecated
	public void actCostGoldRank(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_COST_GOLD);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		int activityKeyId = actRecord.getAwardId();
		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActGoldRankRs.Builder builder = ActGoldRankRs.newBuilder();

		int historyRank = (int) activityData.getStatus(handler.getRoleId());
		builder.setState(historyRank);

		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		if (personRank == null) {
			flag = false;
		}

		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward e = staticActivityMgr.getActAward(rankKeyId);
			if (e == null) {
				continue;
			}

			int rank = display.getRank();
			int keyId = e.getKeyId();

			// 个人记录
			if (flag && personRank.getRank() == rank) {
				flag = false;
			} else if (flag && personRank.getRank() < rank) {
				flag = false;
				builder.addActRank(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
			}

			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(rank);
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					builder.addActRank(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), rank, target.getLord().getNick(), actRank.getRankValue()));
				}

			}

			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}

			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
			awardSet.add(keyId);
		}
		handler.sendMsgToPlayer(ActGoldRankRs.ext, builder.build());
	}

	/**
	 * 特训半价
	 *
	 * @param handler
	 */
	public void actHalfWash(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HALF_WASH);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityPb.ActHalfWashRs.Builder builder = ActivityPb.ActHalfWashRs.newBuilder();
		int got = (int) actRecord.getStatus(0L);
		int count = staticLimitMgr.getNum(260);
		count = count == 0 ? 10 : count;
		builder.setState(got);
		builder.setCount(count);
		handler.sendMsgToPlayer(ActivityPb.ActHalfWashRs.ext, builder.build());
	}

	/**
	 * 每日战役
	 *
	 * @param handler
	 */
	public void actDailyMission(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_DAILY_MISSION);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActDailyMissionRs.Builder builder = ActDailyMissionRs.newBuilder();
		List<StaticActAward> awardList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == awardList || awardList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}

		int got = (int) actRecord.getStatus(0L);
		for (StaticActAward e : awardList) {
			int keyId = e.getKeyId();
			boolean canAward = got >= e.getCond();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1, canAward));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0, canAward));
			}
		}
		builder.setState(got);
		handler.sendMsgToPlayer(ActDailyMissionRs.ext, builder.build());
	}

	/**
	 * 导师排行榜 // 充值排行 // 消费排行
	 *
	 * @param handler
	 */
	public void actMentorScoreRank(ClientHandler handler, ActMasterRankRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int activityId = rq.getActivityId();
		ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int activityKeyId = actRecord.getAwardId();
		// Map<Integer, List<StaticActRankDisplay>> displayList = staticActivityMgr.getActRankDisplays(activityKeyId);
		List<StaticActAward> actAwardById = staticActivityMgr.getActAwardById(activityKeyId);
		if (actAwardById == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		ActMasterRankRs.Builder builder = ActMasterRankRs.newBuilder();
		// 导师排行榜记录值更新
		if (rq.getActivityId() == ActivityConst.ACT_MENTOR_SCORE) {
			activityManager.updActMentorScore(player);
		}
		builder.setActivityId(rq.getActivityId());
		boolean flag = true;
		Lord lord = player.getLord();
		ActPlayerRank personRank = activityId == ActivityConst.ACT_MENTOR_SCORE ? activityData.getLordRank(lord.getLordId()) : activityData.getLordCostRank(lord.getLordId(), (activityId == ActivityConst.ACT_TOPUP_RANK ? 500 : 1000));
		if (personRank == null) {
			flag = false;
		}
		int selfRank = personRank != null ? personRank.getRank() : 0;
		builder.setState(selfRank);
		Date rewardTime = TimeHelper.getRewardTime(activityBase.getEndTime());
		Date now = new Date();
		boolean isOpen = now.after(rewardTime);
		List<CommonPb.ActRank> list = new ArrayList<>();
		List<StaticActRankDisplay> actRankDisplay = staticActivityMgr.getActRankDisplay(activityKeyId);
		boolean isIn = false;
		for (StaticActRankDisplay x : actRankDisplay) {
			// 排行数据
			ActPlayerRank actRank = activityData.getActRank(x.getRank());
			boolean flag1;
			if (actRank != null) {
				Player target = playerManager.getPlayer(actRank.getLordId());
				if (target != null) {
					flag1 = true;
					switch (rq.getActivityId()) {
					case ActivityConst.ACT_TOPUP_RANK:
						if (actRank.getRankValue() < 500) {
							flag1 = false;
						}
						break;
					case ActivityConst.ACT_COST_GOLD:
						if (actRank.getRankValue() < 1000) {
							flag1 = false;
						}
						break;
					default:
						break;
					}
					if (flag1) {
						list.add(PbHelper.createActRank(target.getCountry(), actRank.getLordId(), x.getRank(), target.getLord().getNick(), actRank.getRankValue()));
						if (player.roleId.longValue() == actRank.getLordId()) {
							isIn = true;
						}
					}
				}
			}
		}
		StaticActAward sta = null;
		for (StaticActAward staticActAward : actAwardById) {
			boolean flags = false;
			int rank = staticActAward.getCond();
			if (personRank != null && personRank.getRank() == staticActAward.getCond() || (personRank != null && sta != null && personRank.getRank() <= staticActAward.getCond() && personRank.getRank() > sta.getCond())) {
				flags = true;
			}
			// 个人记录
			if (selfRank != 0 && flag && selfRank == rank) {
				flag = false;
			} else if (selfRank != 0 && flag && selfRank < rank) {
				flag = false;
				if (!isIn) {
					list.add(PbHelper.createActRank(lord.getCountry(), lord.getLordId(), personRank.getRank(), lord.getNick(), personRank.getRankValue()));
				}
			}
			// 奖励数据
			if (isOpen) {
				if (actRecord.getReceived().containsKey(staticActAward.getKeyId())) {// 已领取奖励
					builder.addActivityCond(PbHelper.createActivityCondPb(staticActAward, 1));
				} else {// 未领取奖励
					if (flags) {
						builder.addActivityCond(PbHelper.createActivityCondPb(staticActAward, 0, true));
					} else {
						builder.addActivityCond(PbHelper.createActivityCondPb(staticActAward, 0, false));
					}
				}
			} else {
				builder.addActivityCond(PbHelper.createActivityCondPb(staticActAward, 0, false));
			}
			sta = staticActAward;
		}
		list = list.stream().sorted(Comparator.comparing(CommonPb.ActRank::getRank)).collect(Collectors.toList());
		builder.addAllActRank(list);
		handler.sendMsgToPlayer(ActMasterRankRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 导师排行榜发奖
	 *
	 * @param activityBase
	 * @param activityData
	 */
	private void sendActMentorScore(ActivityBase activityBase, ActivityData activityData) {

		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();

		while (iterator.hasNext()) {
			Player next = iterator.next();
			// 导师排行榜记录值更新
			activityManager.updActMentorScore(next);
		}
		iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (actRecord == null) {
				continue;
			}
			List<CommonPb.Award> awardList = new ArrayList<>();
			ActPlayerRank actRank = activityData.getLordRank(next.roleId);
			if (actRank == null) {
				continue;
			}
			// 获取当前排名的奖励
			StaticActAward rankAward = staticActivityMgr.getActRankAward(activityBase.getAwardId(), actRank.getRank());
			if (rankAward != null && !actRecord.getReceived().containsKey(rankAward.getKeyId())) {
				awardList.addAll(rankAward.getAwardPbList());
			}
			if (awardList.size() > 0) {
				// 已达成条件,未领取奖励,补发奖励邮件
				playerManager.sendAttachMail(next, PbHelper.finilAward(awardList), MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
			}
		}
	}

	public void actOpenBuildingGiftRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActOpenBuildGiftRs.Builder builder = ActOpenBuildGiftRs.newBuilder();
		if (player.getLord().getBuildGift() == 0) {
			builder.setIsOpen(true);
			List<Integer> ations = staticLimitMgr.getAddtion(254);
			if (ations == null || ations.size() == 0 || ations.size() < 3) {
				com.game.util.LogHelper.CONFIG_LOGGER.info("建造礼包开启bug->[{}]", ations);
				return;
			}
			StaticProp prop = staticPropMgr.getStaticProp(ations.get(1));
			playerManager.addAward(player, AwardType.PROP, prop.getPropId(), 1, Reason.BUY_BUILD_TEAM);

			activityManager.openBuildGift(player);
			builder.setAward(PbHelper.createAward(AwardType.PROP, prop.getPropId(), 1));
			player.getLord().setBuildGift(1);
		} else {
			builder.setIsOpen(false);
		}
		handler.sendMsgToPlayer(ActOpenBuildGiftRs.ext, builder.build());
	}

	/**
	 * 0元礼包
	 *
	 * @param activityBase
	 */
	public void sendActZeroMail(ActivityBase activityBase) {
//        long zero = TimeHelper.getZeroTimeMs();
		// 未开始不处理
		Date now = new Date();
		if (now.before(activityBase.getBeginTime())) {
			return;
		}

		List<Player> players = Lists.newArrayList(playerManager.getPlayers().values());
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		// 根据天数分组
		Map<Integer, List<StaticActAward>> map = condList.stream().collect(Collectors.groupingBy(StaticActAward::getSortId));
		players.forEach(e -> {
			// 限制了活动是否开启
			ActRecord actRecord = activityManager.getActivityInfo(e, activityBase);
			if (actRecord != null) {
				List<StaticActFreeBuy> footList = staticActivityMgr.getActFreeBuy(activityBase.getAwardId());
				for (StaticActFreeBuy foot : footList) {
					if (null == foot) {
						continue;
					}
					int sortId = foot.getSortId();
					// 0.未购买 1-N购买后的第几天
					int state = currentActivity(e, activityManager.getActivityInfo(e, activityBase), sortId);
					if (state != 0) {
						// 这个奖励买过了 看下奖励最多几天
						List<StaticActAward> awards = map.get(sortId);
						int days = awards.size();
						// 过了对应天数了
						if (state > days) {
							sendActZeroGold(activityBase, e);
						}
					}
				}
			}
		});
	}

	/**
	 * 0元礼包
	 *
	 * @param activityBase
	 */
	public boolean sendActZeroGold(ActivityBase activityBase, Player target) {
		int awardId = activityBase.getAwardId();
		List<StaticActFreeBuy> footList = staticActivityMgr.getActFreeBuy(awardId);
		if (null == footList || footList.size() == 0) {
			return true;
		}
		if (target == null) {
			return true;
		}
		ActRecord actRecord = target.activitys.get(activityBase.getActivityId());
		if (actRecord == null) {
			return true;
		}

		Map<Long, Long> status = actRecord.getStatus();
		List<CommonPb.Award> awardList = new ArrayList<CommonPb.Award>();
		Date now = new Date();
		boolean result = true;
		boolean sendGold = false;
		int costGold = 0;
		String giftName = "";
		for (StaticActFreeBuy actFoot : footList) {
			boolean canAward = true;
			if (null == actFoot) {
				continue;
			}
			long sortId = (long) actFoot.getSortId();
			if (!status.containsKey(sortId)) {
				continue;
			}
			Date beginTime = new Date(actRecord.getStatus(actFoot.getSortId()));
			int days = TimeHelper.whichDay(0, now, beginTime);
			List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId, actFoot.getSortId());
			int size = condList.size();
			if (days >= size + 1) {
				for (StaticActAward e : condList) {
					if (!actRecord.getReceived().containsKey(e.getKeyId())) {
						canAward = false;
						actRecord.getReceived().put(e.getKeyId(), 1);
						awardList.addAll(e.getAwardPbList());
					}
				}
				if (canAward) {
					long footId = sortId * 1000;
					if (null != actFoot && status.containsKey(sortId) && !status.containsKey(footId)) {
						status.put(footId, 1L);
						awardList.add(PbHelper.createAward(AwardType.GOLD, 0, actFoot.getPrice()).build());
						sendGold = true;
						costGold = actFoot.getPrice();
						giftName = actFoot.getName();
					}
				} else {
					result = false;
				}
			}
		}
		if (!awardList.isEmpty()) {
			if (sendGold) {
				playerManager.sendAttachMail(target, PbHelper.finilAward(awardList), MailId.ACTIVITY_RTN_GOLD, activityBase.getStaticActivity().getName(), costGold + "", giftName);
			} else {
				playerManager.sendAttachMail(target, PbHelper.finilAward(awardList), MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
			}
		}
		return result;
	}

	/**
	 * 双旦活动
	 *
	 * @param handler
	 */
	public void actDoubleEggRq(ActDoubleEggRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		int activityId = rq.getActivityId();
		ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		int propId = getExchangeId(activityId);
		Item item = player.getItem(propId);
		int total = 0;
		if (item != null) {
			total = item.getItemNum();
		}
		ActDoubleEggRs.Builder builder = ActDoubleEggRs.newBuilder();
		builder.setNum((int) actRecord.getStatus(GameServer.getInstance().currentDay));
		builder.setSocks(total);
		builder.setPropId(propId);
		builder.setMaxNum(staticLimitMgr.getNum(SimpleId.ACT_DOUBLE_EGG_DAYLY_MAX_PROP));
		builder.setHelp(activityBase.getStaticActivity().getDesc());
		List<StaticActExchange> awardList = new ArrayList<>(staticActivityMgr.getActDoubleEggs().values());
		if (null == awardList || awardList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		for (StaticActExchange e : awardList) {
			int keyId = e.getKeyId();
			int awardNum = actRecord.getRecordNum(keyId);
			CommonPb.ActivityCond.Builder activityCond = CommonPb.ActivityCond.newBuilder();
			activityCond.setKeyId(keyId).setCond(e.getNeedNum()).setIsAward(awardNum).setChangeNum(e.getMaxNum()).build();
			activityCond.addAward(PbHelper.createAward(e.getAward().get(0), e.getAward().get(1), e.getAward().get(2)));
			builder.addActivityCond(activityCond.build());
		}
		handler.sendMsgToPlayer(ActDoubleEggRs.ext, builder.build());

		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 双旦活动兑换
	 *
	 * @param handler
	 */
	public void actDoubleEggChangeRq(ActDoubleEggChangeRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int activityId = rq.getActivityId();
		ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int key = rq.getKeyId();
		StaticActExchange staticActExchange = staticActivityMgr.getActDoubleEggs().get(key);
		if (staticActExchange == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		// 已兑换次数
		int changeNum = actRecord.getRecord(key);
		if (changeNum >= staticActExchange.getMaxNum()) {
			handler.sendErrorMsgToPlayer(GameError.MAX_BUY_COUNT);
			return;
		}
		// 数量限制
		int propId = getExchangeId(activityId);
		Item item = player.getItem(propId);
		int total = 0;
		if (item != null) {
			total = item.getItemNum();
		}
		if (total < staticActExchange.getNeedNum()) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_NUMBER_ERROR);
			return;
		}
		playerManager.subItem(player, propId, staticActExchange.getNeedNum(), Reason.ACT_DOUBLE_EGG_CHANGE);
		int keyId = playerManager.addAward(player, staticActExchange.getAward().get(0), staticActExchange.getAward().get(1), staticActExchange.getAward().get(2), Reason.ACT_DOUBLE_EGG_CHANGE);
		item = player.getItem(propId);
		actRecord.addRecord(key, 1);
		ActDoubleEggChangeRs.Builder builder = ActDoubleEggChangeRs.newBuilder();
		builder.setProp(PbHelper.createItemPb(item.getItemId(), item.getItemNum()));
		builder.addAward(PbHelper.createAward(player, staticActExchange.getAward().get(0), staticActExchange.getAward().get(1), staticActExchange.getAward().get(2), keyId));
		handler.sendMsgToPlayer(ActDoubleEggChangeRs.ext, builder.build());
		ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
		String name = "";
		if (activityBase != null) {
			name = activityBase.getStaticActivity().getName();
		}
		SpringUtil.getBean(EventManager.class).join_activity(player, activityId, name, staticActExchange.getKeyId());
		SpringUtil.getBean(EventManager.class).complete_activity(player, activityId, name, staticActExchange.getKeyId(), new Date(), staticActExchange.getAward());
		// 没有可以换的推送前端更新下
		boolean needPush = activityTips(player, true, actRecord, activityBase);
		if (!needPush) {
			playerManager.synActivity(player, activityBase.getActivityId());
		}
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 双蛋兑换触发奖励
	 *
	 * @param player
	 * @param killWorms
	 */
	public List<Award> actDoubleEggReward(Player player, boolean killWorms) {
		List<Award> list = new ArrayList<>();
		List<Award> list1 = getAwards(player, killWorms, ActivityConst.ACT_DOUBLE_EGG);
		List<Award> list2 = getAwards(player, killWorms, ActivityConst.ACT_NEW_YEAR_EGG);
		List<Award> list3 = getAwards(player, killWorms, ActivityConst.ACT_MEDAL_EXCHANGE);
		List<Award> list4 = getAwards(player, killWorms, ActivityConst.ACT_DRAGON_BOAT);
		list.addAll(list1);
		list.addAll(list2);
		list.addAll(list3);
		list.addAll(list4);
		return list;
	}

	private List<Award> getAwards(Player player, boolean killWorms, int activityId) {
		List<Award> list = new ArrayList<>();
		ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
		if (actRecord == null) {
			return list;
		}
		ActivityBase base = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (base == null || new Date().before(base.getBeginTime())) {
			return list;
		}
		long num = actRecord.getStatus(GameServer.getInstance().currentDay);
		// 每日获得最大上限
		if (num >= staticLimitMgr.getNum(SimpleId.ACT_DOUBLE_EGG_DAYLY_MAX_PROP)) {
			return list;
		}
		int rate = staticLimitMgr.getNum(SimpleId.ACT_DOUBLE_EGG_KILL_WORM);
		if (!killWorms) {
			rate = staticLimitMgr.getNum(SimpleId.ACT_DOUBLE_EGG_FLY_PERSION);
		}
		int random = RandomUtil.randomBetween(0, 100);
		if (random < rate) {
			// 触发
			int propId = getExchangeId(activityId);
			num++;
			actRecord.putState(GameServer.getInstance().currentDay, num);
			list.add(new Award(AwardType.PROP, propId, 1));
			boolean hasTips = activityTips(player, true, actRecord, base);
			if (hasTips) {
				playerManager.synActivity(player, activityId);
			}
		}
		return list;
	}

	public int getExchangeId(int activityId) {
		switch (activityId) {
		case ActivityConst.ACT_MEDAL_EXCHANGE:
			return staticLimitMgr.getNum(SimpleId.ACT_MEDAL_PROP);
		case ActivityConst.ACT_DRAGON_BOAT:
			return staticLimitMgr.getNum(SimpleId.ACT_DRAON_BOAT_PROP);
		default:
			return staticLimitMgr.getNum(SimpleId.ACT_DOUBLE_EGG_PROP);
		}
	}

	/**
	 * 双旦礼包
	 */
	public void actChrismasRq(ActChrismasRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int activityId = rq.getActivityId();

		ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActChrismasRs.Builder builder = ActChrismasRs.newBuilder();
		builder.setTotalCost(actRecord.getRecord(-1));
		final ActRecord finalRecord = actRecord;
		// 超值特惠
		List<StaticActivityChrismas> activityAwards = staticActivityMgr.getChrismasMap().values().stream().filter(e -> e.getAwardId() == actRecord.getAwardId()).collect(Collectors.toList());
		activityAwards.forEach(staticActChrisms -> {
			List<CommonPb.Award> awards = new ArrayList<>();
			staticActChrisms.getAward().forEach(e -> {
				awards.add(PbHelper.createAward(e.get(0), e.get(1), e.get(2)).build());
			});
			Integer count = finalRecord.getRecord(staticActChrisms.getKeyId());
			builder.addPreference(CommonPb.ActivityCond.newBuilder().setKeyId(staticActChrisms.getKeyId()) // 奖励
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setCond(staticActChrisms.getCost()) // 花费钻石
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.addAllAward(awards) // 奖励内容
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setChangeNum(staticActChrisms.getCanBuy()) // 可购买次数
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setIsAward(count == null ? 0
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																	: count.intValue()) // 已购买次数
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setDesc(staticActChrisms.getDesc() == null ? ""
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																	: staticActChrisms.getDesc())
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.build());
		});
		// 奖励的
		List<StaticActivityChrismasAward> actChrismasAwards = staticActivityMgr.getChrismasAwardMap().values().stream().filter(e -> e.getAwardId() == actRecord.getAwardId()).collect(Collectors.toList());
		actChrismasAwards.forEach(statiAward -> {
			List<CommonPb.Award> awards = new ArrayList<>();
			statiAward.getAward().forEach(e -> {
				awards.add(PbHelper.createAward(e.get(0), e.get(1), e.get(2)).build());
			});
			Integer count = finalRecord.getReceived().get(statiAward.getKeyId());
			builder.addAwards(CommonPb.ActivityCond.newBuilder().setKeyId(statiAward.getKeyId()) // 奖励
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setCond(statiAward.getCost()) // 达成条件
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.addAllAward(awards) // 奖励内容
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setIsAward(count == null ? 0
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																	: count.intValue())// 领取状态
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setDesc(statiAward.getDesc() == null ? ""
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																	: statiAward.getDesc())
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.build());
		});

		handler.sendMsgToPlayer(ActChrismasRs.ext, builder.build());
	}

	/**
	 * 双旦礼包购买
	 *
	 * @param handler
	 */
	public void actChrismasBuyRq(ActChrismasBuyRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int activityId = rq.getActivityId();
		ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int keyId = rq.getKeyId();
		StaticActivityChrismas chrismas = staticActivityMgr.getChrismasMap().get(keyId);
		if (chrismas == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int costGold = chrismas.getCost();
		if (player.getGold() < costGold) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		int buyCount = actRecord.getRecord(keyId);
		if (buyCount >= chrismas.getCanBuy()) {
			handler.sendErrorMsgToPlayer(GameError.COUNT_ERROR);
			return;
		}
		playerManager.subGold(player, chrismas.getCost(), Reason.ACT_DOUBLE_EGG);
		ActChrismasBuyRs.Builder builder = ActChrismasBuyRs.newBuilder();
		chrismas.getAward().forEach(e -> {
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			int tkey = playerManager.addAward(player, type, id, count, Reason.ACT_DOUBLE_EGG);
			builder.addAward(PbHelper.createAward(player, type, id, count, tkey));
		});
		builder.setGold(player.getGold());
		actRecord.addRecord(keyId, 1);
		// 记录钻石消费数据
		actRecord.addRecord(-1, chrismas.getCost());
		// 超值特惠
		final ActRecord finalRecord = actRecord;
		List<StaticActivityChrismas> activityAwards = staticActivityMgr.getChrismasMap().values().stream().filter(e -> e.getAwardId() == actRecord.getAwardId()).collect(Collectors.toList());
		activityAwards.forEach(staticActChrisms -> {
			List<CommonPb.Award> tawards = new ArrayList<>();
			staticActChrisms.getAward().forEach(e -> {
				tawards.add(PbHelper.createAward(e.get(0), e.get(1), e.get(2)).build());
			});
			Integer tcount = finalRecord.getRecord(staticActChrisms.getKeyId());
			builder.addPreference(CommonPb.ActivityCond.newBuilder().setKeyId(staticActChrisms.getKeyId()) // 奖励
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setCond(staticActChrisms.getCost()) // 花费钻石
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.addAllAward(tawards) // 奖励内容
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setChangeNum(staticActChrisms.getCanBuy()) // 可购买次数
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setIsAward(tcount == null ? 0
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																	: tcount.intValue()) // 已购买次数
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setDesc(staticActChrisms.getDesc() == null ? ""
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																	: staticActChrisms.getDesc())
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.build());
		});
		handler.sendMsgToPlayer(ActChrismasBuyRs.ext, builder.build());
		ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
		String name = "";
		if (activityBase != null) {
			name = activityBase.getStaticActivity().getName();
		}
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
		SpringUtil.getBean(EventManager.class).join_activity(player, activityId, name, chrismas.getKeyId() + 90000);
		SpringUtil.getBean(EventManager.class).complete_activity(player, activityId, name, chrismas.getKeyId() + 90000, new Date(), builder.getAwardList());
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(activityId).isAward(false).awardId(chrismas.getKeyId()).giftName(chrismas.getDesc()).roleId(player.roleId).channel(player.account.getChannel()).build());
	}

	/**
	 * 双旦礼包购买
	 *
	 * @param handler
	 */
	public void actChrismasRewardRq(ActChrismasRewardRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int activityId = rq.getActivityId();
		ActRecord actRecord = activityManager.getActivityInfo(player, activityId);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int keyId = rq.getKeyId();
		StaticActivityChrismasAward chrismas = staticActivityMgr.getChrismasAwardMap().get(keyId);
		if (chrismas == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int costConfig = chrismas.getCost();
		int cost = actRecord.getRecord(-1);
		if (cost < costConfig) {
			handler.sendErrorMsgToPlayer(GameError.COST_TYPE_ERROR);
			return;
		}
		ActChrismasRewardRs.Builder builder = ActChrismasRewardRs.newBuilder();
		chrismas.getAward().forEach(e -> {
			int type = e.get(0);
			int id = e.get(1);
			int count = e.get(2);
			int tkeyId = playerManager.addAward(player, type, id, count, Reason.ACT_DOUBLE_EGG);
			builder.addAward(PbHelper.createAward(player, type, id, count, tkeyId));
		});
		actRecord.getReceived().put(keyId, 1);
		// 奖励的
		final ActRecord finalRecord = actRecord;
		List<StaticActivityChrismasAward> actChrismasAwards = staticActivityMgr.getChrismasAwardMap().values().stream().filter(e -> e.getAwardId() == actRecord.getAwardId()).collect(Collectors.toList());
		actChrismasAwards.forEach(statiAward -> {
			List<CommonPb.Award> tawards = new ArrayList<>();
			statiAward.getAward().forEach(e -> {
				tawards.add(PbHelper.createAward(e.get(0), e.get(1), e.get(2)).build());
			});
			Integer tcount = finalRecord.getReceived().get(statiAward.getKeyId());
			builder.addAwards(CommonPb.ActivityCond.newBuilder().setKeyId(statiAward.getKeyId()) // 奖励
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setCond(statiAward.getCost()) // 达成条件
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.addAllAward(tawards) // 奖励内容
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setIsAward(tcount == null ? 0
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																	: tcount.intValue())// 领取状态
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.setDesc(statiAward.getDesc() == null ? ""
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																	: statiAward.getDesc())
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																										.build());
		});
		handler.sendMsgToPlayer(ActChrismasRewardRs.ext, builder.build());
		ActivityBase activityBase = staticActivityMgr.getActivityById(activityId);
		String name = "";
		if (activityBase != null) {
			name = activityBase.getStaticActivity().getName();
		}
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
		SpringUtil.getBean(EventManager.class).join_activity(player, activityId, name, chrismas.getKeyId());
		SpringUtil.getBean(EventManager.class).complete_activity(player, activityId, name, chrismas.getKeyId(), new Date(), builder.getAwardList());
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(activityId).isAward(true).awardId(chrismas.getKeyId()).giftName(chrismas.getDesc()).roleId(player.roleId).vip(player.getVip()).channel(player.account.getChannel()).build());
	}

	private void sendActDoubleEggs(ActivityBase activityBase, ActivityData activityData) {
		int prop = getExchangeId(activityBase.getActivityId());
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			// 双旦活动结束
			activityManager.actDoubleEggEnd(prop, next);
		}
	}

	private void sendActChrismasReward(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (actRecord == null) {
				continue;
			}
			int cost = actRecord.getRecord(-1);
			if (cost == 0) {
				continue;
			}
			// 双旦活动结束
			activityManager.actChrismasRewardEnd(next, actRecord, cost, activityBase);
		}
	}

	/**
	 * 豪华礼包
	 *
	 * @param rq
	 * @param handler
	 */
	public void actLuxuryGiftRq(ActivityPb.ActLuxuryGiftRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_LUXURY_GIFT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_LUXURY_GIFT);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActLuxuryGiftRs.Builder builder = ActLuxuryGiftRs.newBuilder();
		builder.setTotalCharge(player.getLord().getTopup());
		builder.setDay(player.getLord().getLoginDays());
		builder.addAllCharges(staticLimitMgr.getAddtion(SimpleId.LUXURY_GIFT));
		int awardId = actRecord.getAwardId();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(awardId);
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		for (StaticActAward e : condList) {
			if (e == null) {
				continue;
			}
			int keyId = e.getKeyId();
			CommonPb.ActivityCond cond = PbHelper.createActivityCondPb(e, 0);
			int state = currentActivity(player, activityData, 0);
			builder.setCond(cond.toBuilder().setCanAward(state == 1).build());
		}
		if (actRecord.getRecord(0) != 0) {
			builder.setCompleteDay(actRecord.getRecord(0));
		} else {
			builder.setCompleteDay(player.getLord().getLoginDays());
		}
		handler.sendMsgToPlayer(ActLuxuryGiftRs.ext, builder.build());
	}

	/**
	 * 世界活动宝箱结束发放奖励
	 *
	 * @param activityBase
	 * @param activityData
	 */
	private void sendActWorldBoxReward(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			if (next.getPWorldBox() == null) {
				continue;
			}
			// 双旦活动结束
			activityManager.actWorldBoxEnd(next, activityBase);
		}
	}

	/**
	 * 勇冠三军结束处理
	 *
	 * @param activityBase
	 * @param activityData
	 */
	private void endWellCrownThreeArmy(ActivityBase activityBase, ActivityData activityData) {
		Map<Integer, Integer> map = new HashMap<>();
		map.put(1, activityData.getRecord(1));
		map.put(2, activityData.getRecord(2));
		map.put(3, activityData.getRecord(3));
		List<Map.Entry<Integer, Integer>> list = map.entrySet().stream().sorted(Comparator.comparingInt(Entry<Integer, Integer>::getValue).reversed()).collect(Collectors.toList());
		int country = list.get(0).getKey();
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityData.getAwardId());
		Optional<StaticActAward> optional = condList.stream().filter(e -> StringUtil.isNullOrEmpty(e.getParam())).findFirst();
		if (optional.isPresent()) {
			StaticActAward award = optional.get();
			List<Award> awardList = Lists.newArrayList();
			award.getAwardList().forEach(e -> {
				awardList.add(new Award(e.get(0), e.get(1), e.get(2)));
			});
			playerManager.getAllPlayer().values().forEach(e -> {
				if (e.getCountry() == country) {
					playerManager.sendAttachMail(e, awardList, MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
				}
			});
		}
		sendActAward(activityBase, activityData);
	}

	/**
	 * 累积充值奖励
	 *
	 * @param activityBase
	 * @param activityData
	 */
	private void sendActGrandTotalReward(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (actRecord == null) {
				continue;
			}

			long cost = actRecord.getStatus(0L);
			if (cost == 0) {
				continue;
			}
			// 奖励的
			List<CommonPb.Award> tawards = new ArrayList<>();
			List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
			condList.forEach((statiAward) -> {
				if (cost >= statiAward.getCond()) { // 满足奖励领取需求
					if (!actRecord.getReceived().containsKey(statiAward.getKeyId())) {// 没有领取过
						actRecord.getReceived().put(statiAward.getKeyId(), 1);
						statiAward.getAwardList().forEach(e -> {
							tawards.add(PbHelper.createAward(e.get(0), e.get(1), e.get(2)).build());
						});
					}
				}
			});
			// 已达成条件,未领取奖励,补发奖励邮件
			if (tawards.size() > 0) {
				playerManager.sendAttachMail(next, PbHelper.finilAward(tawards), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}
		}
	}

	/**
	 * 阵营骨干活动结束发放奖励
	 *
	 * @param activityBase
	 * @param activityData
	 */
	private void sendActCampMembersRankRq(ActivityBase activityBase, ActivityData activityData) {
		for (CountryData countryData : SpringUtil.getBean(CountryManager.class).getCountrys().values()) {
			LinkedList<CampMembersRank> campMembers = activityData.getCampMembers(countryData.getCountryId());
			int count = 0;
			for (CampMembersRank campMembersRank : campMembers) {
				if (count >= 100) {
					break;
				}
				count++;
				Player player = playerManager.getPlayer(campMembersRank.getLordId());
				if (player == null) {
					continue;
				}
				ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
				List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(actRecord.getAwardId());
				if (displayList == null) {
					continue;
				}
				int rank = campMembersRank.getRank();
				int tempRank = activityManager.obtainAwardGear(rank, displayList);
				List<Award> awards = new ArrayList<>();
				for (StaticActRankDisplay display : displayList) {
					if (rank > display.getRank() || tempRank != display.getRank()) {
						continue;
					}
					StaticActAward staticActAward = staticActivityMgr.getActAward(display.getAwardKeyId());
					if (staticActAward == null) {
						continue;
					}
					// 未领取奖励
					if (!actRecord.getReceived().containsKey(staticActAward.getKeyId())) {
						List<List<Integer>> awardList = staticActAward.getAwardList();
						for (List<Integer> list : awardList) {
							if (list.size() != 3) {
								continue;
							}
							Award award = new Award(list.get(0), list.get(1), list.get(2));
							awards.add(award);
						}
					}
					actRecord.getReceived().put(staticActAward.getKeyId(), 1);
				}
				if (!awards.isEmpty()) {
					playerManager.sendAttachMail(player, awards, MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
				}
				SpringUtil.getBean(LogUser.class).activity_log(CampMembersRankLog.builder().activityId(activityBase.getActivityId()).roleId(player.roleId).vip(player.getVip()).channel(player.account.getChannel()).rank(campMembersRank.getRank()).build());
			}
		}
	}

	public void checkActLuxuryGift(Player player) {
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_LUXURY_GIFT);
		if (actRecord == null) {
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			return;
		}
		int state = currentActivity(player, activityData, 0);
		if (state == 1 && actRecord.getRecord(0) == 0) {
			actRecord.putRecord(0, player.getLord().getLoginDays());
		}
	}

	/**
	 * @Description 购买通行证等级
	 * @Date 2021/1/22 9:25
	 * @Param [handler]
	 * @Return
	 **/
	public void buyActPassPortLvRq(BuyActPassPortLvRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 活动充值记录
		Map<Integer, Integer> record = actRecord.getRecord();
		if (record.get(0) == null) {
			record.put(0, 0);
		}
		if (record.get(1) == null) {
			record.put(1, 0);
		}

		// 是否购买进阶通行证 isBuy 0为购买 1 已经购买
		Integer isBuy = record.get(1);

		// 进阶版已经购买
		if (isBuy.intValue() == 1) {
			// 获取购买到的等级
			int buyLv = rq.getBuyLv();
			// 获取当前通行证等级
			Integer beforeScore = record.get(0);
			int passPortLv = staticActivityMgr.getPassPortLv(beforeScore);
			// 通行证经验逻辑处理
			if (buyLv > passPortLv && buyLv <= staticActivityMgr.getPassPortLvMap().size() - 1) {
				// 获取对应等级的分数值
				Integer beforePassPortScore = staticActivityMgr.getPassPortExp(passPortLv);
				Integer afterPassPortScore = staticActivityMgr.getPassPortExp(buyLv);
				// 购买等级后用户的分数
				int score = record.get(0) + afterPassPortScore - beforePassPortScore;
				// 购买一级通行证所需要的钻石数量
				int oneNum = staticLimitMgr.getNum(288);
				// 购买等级所需要的钻石数量
				int num = (buyLv - passPortLv) * oneNum;
				// 拥有钻石小于需要扣除的钻石
				if (player.getGold() < num) {
					handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
					return;
				}
				playerManager.subAward(player, AwardType.GOLD, 0, num, Reason.BUY_PASS_PORT_LV);

				// 更新通行证分数
				if (buyLv >= staticActivityMgr.getPassPortLvMap().size() - 1) {
					record.put(0, staticActivityMgr.getPassPortExp(staticActivityMgr.getPassPortLvMap().size() - 1));
				} else {
					record.put(0, score);
				}

				// 刷新用户活动状态
				BuyActPassPortLvRs.Builder builder = BuyActPassPortLvRs.newBuilder();
				builder.setGold(player.getGold());
				builder.setLv(staticActivityMgr.getPassPortLv(score));
				builder.setScore(record.get(0));
				builder.setIsBuy(record.get(1));

				List<StaticPassPortAward> passPortList = staticActivityMgr.getPassPortList(actRecord.getAwardId());
				if (passPortList == null || passPortList.size() == 0) {
					handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
					return;
				}

				List<StaticPayPassPort> staticPayPassPortList = staticActivityMgr.getStaticPayPassPortList(actRecord.getAwardId());
				if (staticPayPassPortList == null || staticPayPassPortList.size() == 0) {
					handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
					return;
				}

				List<CommonPb.ActPassPortTask.Builder> passPortTask = activityManager.getPassPortTask(player);
				if (null == passPortTask) {
					handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
					return;
				}

				for (StaticPassPortAward e : passPortList) {
					int keyId = e.getId();
					if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
						CommonPb.ActPassPortAward actPassPortAwardPb = PbHelper.createActPassPortAwardPb(e, 1);
						builder.addActPassPortAward(actPassPortAwardPb);
					} else {// 未领取奖励
						CommonPb.ActPassPortAward actPassPortAwardPb = PbHelper.createActPassPortAwardPb(e, 0);
						builder.addActPassPortAward(actPassPortAwardPb);
					}
				}

				for (CommonPb.ActPassPortTask.Builder builders : passPortTask) {
					builder.addActPassPortTask(builders);
				}

				for (StaticPayPassPort staticPayPassPort : staticPayPassPortList) {
					builder.addPayItem(PbHelper.createStaticPayPassPort(staticPayPassPort));
				}

				handler.sendMsgToPlayer(BuyActPassPortLvRs.ext, builder.build());
				return;

			} else {
				handler.sendErrorMsgToPlayer(GameError.PASS_PORT_LV_ERROR);
				return;
			}
		}
		// 未购买进阶版通行证,无法购买等级
		handler.sendErrorMsgToPlayer(GameError.PASS_PORT_PRO_NOT_BUY);
	}

	public void actNewPurpDialRq(ActHeroDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		Lord lord = player.getLord();

		ActRecord actRecord = activityManager.getActivityInfo(player, req.getId());
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int personScore = actRecord.getRecord(actRecord.getActivityId());
		int activityKeyId = actRecord.getAwardId();

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActPlayerRank personRank = activityData.getLordRank(lord.getLordId());
		boolean flag = true;

		List<StaticActAward> displayList = staticActivityMgr.getActAwardById(activityKeyId);
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		StaticActDialPurp common = staticActivityMgr.getDialPurp(activityKeyId);
		if (common == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		DialEntity dial = staticActivityMgr.getActDialMap(activityKeyId);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActHeroDialRs.Builder builder = ActHeroDialRs.newBuilder();
		Iterator<List<StaticActDial>> it = dial.getActDails().values().iterator();
		while (it.hasNext()) {
			List<StaticActDial> dialList = it.next();
			for (StaticActDial e : dialList) {
				if (actRecord.getReceived().containsKey(e.getDialId())) {// 判定该物品是否已获取
					builder.addActDial(PbHelper.createActDial(e, 1));
					continue;
				}
				builder.addActDial(PbHelper.createActDial(e, 0));
			}
		}

		int curentDay = GameServer.getInstance().currentDay;
		int count = actRecord.getRecord(curentDay);
		int freeCount = common.getFreeTimes() - count < 0 ? 0 : common.getFreeTimes() - count;
		builder.setFree(freeCount);
		builder.setPrice(common.getOnePrice());
		builder.setTenPrice(common.getTenPrice());
		builder.setScore(personScore);
		if (personRank != null) {
			builder.setRank(personRank.getRank());
		}
		HashSet<Integer> awardSet = new HashSet<Integer>();
		for (StaticActAward e : displayList) {
			if (e == null) {
				continue;
			}
			int rank = e.getCond();
			int keyId = e.getKeyId();

			ActPlayerRank actRank = activityData.getActRank(rank);
			// 个人数据
			int minScore = staticLimitMgr.getNum(226);// 上榜最低积分
			if (minScore == 0) {
				handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
				return;
			}
			if (flag && personRank != null && rank >= personRank.getRank() && personScore >= minScore) {
				if (rank > personRank.getRank()) {
					builder.addActDialRank(PbHelper.createActDialRank(personRank.getRank(), player.getLord().getNick(), personScore, e.getAwardPbList()));
				}
				flag = false;
			}
			if (actRank != null) {
				int score = (int) actRank.getRankValue();
				if (score >= minScore) {
					if (playerManager.getPlayer(actRank.getLordId()) != null) {
						// 积分大于20显示
						builder.addActDialRank(PbHelper.createActDialRank(rank, playerManager.getPlayer(actRank.getLordId()).getLord().getNick(), score, e.getAwardPbList()));
					}
				}
			}
			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}
			awardSet.add(keyId);
		}

		List<StaticMyExchange> staticMyExchangeList = staticActivityMgr.getStaticMyExchangeList(activityBase.getAwardId());
		staticMyExchangeList.forEach(ex -> {
			List<List<Integer>> award1 = ex.getAward();
			award1.forEach(x -> {
				if (x.get(0) == AwardType.HERO && player.getHero(x.get(1)) != null) {
					actRecord.putState(ex.getId(), ex.getMaxChangeTimes());
				}
			});
			CommonPb.ActivityCond.Builder activityCond = CommonPb.ActivityCond.newBuilder();
			activityCond.setKeyId(ex.getId()).setCond(ex.getCost().get(2)).setIsAward((int) actRecord.getStatus(ex.getId())).setChangeNum(ex.getMaxChangeTimes()).build();
			ex.getAward().forEach(award -> {
				activityCond.addAward(PbHelper.createAward(award.get(0), award.get(1), award.get(2)));
			});
			builder.addActivityCond(activityCond.build());
		});

		activityData.getRecords().forEach(rewardRecord -> {
			builder.addRecord(rewardRecord.ser(activityData.getActivityId()));
		});
		builder.setHNum(player.getItemNum(ItemId.HERO_GOLD));

		handler.sendMsgToPlayer(ActHeroDialRs.ext, builder.build());

		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	public void doNewCommonPurp(DoHeroDialRq req, ClientHandler handler) {

		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int count = req.getCount();
		// 检查次数
		if (count != 1 && count != 10) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_HERO_DIAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 找到活动Id
		int awardId = actRecord.getAwardId();
		StaticActDialPurp dial = staticActivityMgr.getDialPurp(awardId);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 活动结构
		DialEntity dialEntity = staticActivityMgr.getActDialMap(awardId);
		if (dialEntity == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 单抽，如果免费次数没了，判断金币
		int freeTimes = dial.getFreeTimes();
		// 获取当日是否抽取过
		int curentDay = GameServer.getInstance().currentDay;
		int getCount = actRecord.getRecord(curentDay);
		int costGold = 0;
		boolean isfree = false;
		if (getCount < freeTimes && count == 1) {
			actRecord.putRecord(curentDay, 1);
			isfree = true;
		}
		if (!isfree) {
			if (player.getGold() < (count == 1 ? dial.getOnePrice() : dial.getTenPrice())) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, count == 1 ? dial.getOnePrice() : dial.getTenPrice(), Reason.ACT_HERO_DIAL);
			costGold = count == 1 ? dial.getOnePrice() : dial.getTenPrice();
		}
		int minScore = staticLimitMgr.getNum(226);// 上榜最低积分
		if (minScore == 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		DoHeroDialRs.Builder builder = DoHeroDialRs.newBuilder();
		for (int i = 0; i < count; i++) {
			StaticActDial actDial = dialEntity.getRandomDail(1, actRecord.getRecord());
			// 获取保底物品的配置

			Integer getC = actRecord.getReceived().values().stream().reduce((a, b) -> a + b).orElse(null);
			Integer integer = actRecord.getReceived().keySet().stream().filter(x -> x == ItemId.HERO_GOLD).findAny().orElse(null);
			// 如果前2次都 没抽到英雄币 则第三次直接给一个英雄币
			if (integer == null && getC != null && getC == 2) {
				actDial = dialEntity.getStaticActDial(AwardType.PROP, ItemId.HERO_GOLD);
			}
			int received = actRecord.getReceived(ItemId.HERO_GOLD);
			if (getC != null && integer != null && received < 3) {
				int i1 = 3 - received;// 还需要抽的次数
				int i2 = 30 - getC;// 30次还能抽几次
				if (i1 >= i2) {
					actDial = dialEntity.getStaticActDial(AwardType.PROP, ItemId.HERO_GOLD);
				}
			}
//            if(getC>30){
//                actRecord.getReceived().clear();
//            }
			actRecord.updateReceive(actDial.getItemId(), 1);// 抽到的itemId次数统计

			int key;
			boolean flag = true;
			if (actDial.getItemType() == AwardType.HERO) {
				if (player.getHero(actDial.getItemId()) != null) {
					key = playerManager.addAward(player, AwardType.PROP, ItemId.HERO_GOLD, 5, Reason.ACT_HERO_DIAL);
					builder.addAward(PbHelper.createAward(player, AwardType.HERO, actDial.getItemId(), 5, key));
					flag = false;
					chatManager.sendWorldChat(ChatId.MY_SOUND, player.getNick(), String.valueOf(activityBase.getActivityId()), actDial.getItemType() + "", actDial.getItemId() + "");
					activityData.addRewardRecord(new LuckPoolRewardRecord(player, PbHelper.createAward(actDial.getItemType(), actDial.getItemId(), actDial.getItemCount()).build()));

				}
			}
			if (flag) {
				key = playerManager.addAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), Reason.ACT_HERO_DIAL);
				builder.addAward(PbHelper.createAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), key));

				// 这里要全局
				if (actDial.getBeRecorded() == 1) {
					chatManager.sendWorldChat(ChatId.MY_SOUND, player.getNick(), activityBase.getStaticActivity().getName(), actDial.getItemType() + "", actDial.getItemId() + "");
					activityData.addRewardRecord(new LuckPoolRewardRecord(player, PbHelper.createAward(actDial.getItemType(), actDial.getItemId(), actDial.getItemCount()).build()));
				}
			}
			activityManager.updActPersonPurp(player, actRecord.getActivityId(), 10, actRecord.getActivityId(), minScore);
			builder.addPlace(actDial.getPlace());
			if (actDial.getKeyId() != 0) {
				actRecord.addRecord(actDial.getKeyId(), 1);
			}
			List<List<Integer>> buyAward = dial.getBuyAward();
			if (buyAward != null) {
				for (List<Integer> list : buyAward) {
					builder.addBuyAward(PbHelper.createAward(list.get(0), list.get(1), list.get(2)));
					playerManager.addAward(player, list.get(0), list.get(1), list.get(2), Reason.ACT_HERO_DIAL);
				}
			}
		}
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(DoHeroDialRs.ext, builder.build());

		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);

		// 活动类型
		SpringUtil.getBean(EventManager.class).spin_the_wheel(player, Lists.newArrayList(ActivityConst.ACT_HERO_DIAL, builder.getAwardList().toString(), count));
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(ActivityConst.ACT_HERO_DIAL).isAward(false).awardId(0).giftName("").roleId(player.roleId).vip(player.getVip()).costGold(costGold).channel(player.account.getChannel()).build());
	}

	// 魅影活动兑换列表
	public void actMyChange(DoHeroExchangelRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_HERO_DIAL);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int id = rq.getId();
		StaticMyExchange staticMyExchange = staticActivityMgr.getStaticMyExchange(actRecord.getAwardId(), id);
		if (staticMyExchange == null) {
			return;
		}
		Integer propId = staticMyExchange.getCost().get(1);
		Integer cost = staticMyExchange.getCost().get(2);
		long count = actRecord.getStatus(id);// 兑换次数
		int itemNum = player.getItemNum(propId);
		if (count >= staticMyExchange.getMaxChangeTimes()) {
			handler.sendErrorMsgToPlayer(GameError.MAX_BUY_COUNT);
			return;
		}
		if (itemNum < cost) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		DoHeroExchangelRs.Builder builder = DoHeroExchangelRs.newBuilder();
		List<List<Integer>> award = staticMyExchange.getAward();
		if (playerManager.isEquipFull(award, player)) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
			return;
		}
		if (award != null) {
			award.forEach(aw -> {
				if (aw.get(0) == AwardType.HERO && player.getHero(aw.get(1)) != null) {
					return;
				}
				Integer type = aw.get(0);
				Integer ids = aw.get(1);
				Integer counts = aw.get(2);
				int keyId = playerManager.addAward(player, type, ids, counts, Reason.ACT_HERO_DIAL);
				builder.addAward(PbHelper.createAward(player, type, ids, counts, keyId));
			});
			actRecord.putState(id, count + 1);
			playerManager.subItem(player, propId, cost, Reason.ACT_HERO_DIAL);
		}
		// actRecord.putState(id, count + 1);
		Item item = player.getItem(propId);
		if (item != null) {
			builder.setProp(CommonPb.Prop.newBuilder().setPropId(item.getItemId()).setPropNum(item.getItemNum()));
		}
		handler.sendMsgToPlayer(DoHeroExchangelRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	// 碎片合成装备
	public void actFragment(PropPb.DoFragmentRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int propId = rq.getPropId();
		StaticProp staticProp = staticPropMgr.getStaticProp(propId);
		if (staticProp == null) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_NOT_FOUND);
			return;
		}
		Item item = player.getItem(staticProp.getPropId());
		if (item == null) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_USE);
			return;
		}
		if (item.getItemNum() < staticProp.getNeedNum()) {
			handler.sendErrorMsgToPlayer(GameError.ITEM_CAN_NOT_USE);
			return;
		}
		List<List<Long>> effectValue = staticProp.getEffectValue();
		if (effectValue == null || effectValue.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		// 添加装备
		int freeSlot = equipManager.getFreeSlot(player);
		// 背包满
		if (freeSlot <= 0) {
			handler.sendErrorMsgToPlayer(GameError.EQUIP_FULL);
			return;
		}
		playerManager.subItem(player, propId, staticProp.getNeedNum(), Reason.MY_EQUIP_FRAGMENT);
		List<Integer> collect1 = effectValue.stream().flatMap(x -> x.stream().skip(3)).mapToInt(a -> a.intValue()).boxed().collect(Collectors.toList());
		List<Long> longs = effectValue.get(WeightRandom.initData(collect1));
		PropPb.DoFragmentRs.Builder builder = PropPb.DoFragmentRs.newBuilder();
		if (longs != null) {
			Equip equip = equipManager.addEquip(player, longs.get(1).intValue(), Reason.MY_EQUIP_FRAGMENT);
			builder.addAward(CommonPb.Award.newBuilder().setKeyId(equip.getKeyId()).setType(longs.get(0).intValue()).setId(longs.get(1).intValue()).setCount(longs.get(2).intValue()).addAllSkillId(equip.getSkills()));
		}
		builder.setProp(CommonPb.Prop.newBuilder().setPropId(propId).setPropNum(item.getItemNum()));
		handler.sendMsgToPlayer(PropPb.DoFragmentRs.ext, builder.build());
	}

	/**
	 * @Description 未领取的月卡奖励补发邮件
	 * @Date 2021/4/2 10:34
	 * @Param []
	 * @Return
	 **/
	private void sendActMonthlyCard() {
		int nowDay = GameServer.getInstance().currentDay;
		long endTimeOfDay = TimeHelper.getEndTimeOfDay();
		List<StaticActPayCard> payCardList = Lists.newArrayList(staticActivityMgr.getPayCard().values());
		if (payCardList.size() < 1) {
			com.game.util.LogHelper.CONFIG_LOGGER.info("config is error");
			return;
		}
		for (Player player : playerManager.getPlayers().values()) {
			ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MONTH_CARD);
			if (actRecord == null) {
				continue;
			}
			int cleanTime = actRecord.getCleanTime();
			if (cleanTime == nowDay) {
				continue;
			}
			Lord lord = player.getLord();
			for (StaticActPayCard payCard : payCardList) {
				if (payCard == null) {
					continue;
				}
				if (payCard.getPayCardId() == ActMonthlyCard.MONTHLY_CARD.getKey()) {
					if (endTimeOfDay > lord.getMonthCard() + TimeHelper.DAY_MS) {
						continue;
					}
				}
				if (payCard.getPayCardId() == ActMonthlyCard.SEASON_CARD.getKey()) {
					if (endTimeOfDay > lord.getSeasonCard() + TimeHelper.DAY_MS) {
						continue;
					}
				}
				if (payCard.getPayCardId() == ActMonthlyCard.IRON_CARD.getKey() || payCard.getPayCardId() == ActMonthlyCard.COPPER_CARD.getKey() || payCard.getPayCardId() == ActMonthlyCard.OIL_CARD.getKey() || payCard.getPayCardId() == ActMonthlyCard.STONE_CARD.getKey()) {
					if (endTimeOfDay > player.getWeekCard().getExpireTime(payCard.getAwardId()) + TimeHelper.DAY_MS) {
						continue;
					}
				}
				if (!actRecord.getReceived().containsKey(payCard.getPayCardId())) {
					List<List<Integer>> sellList = payCard.getSellList();
					ArrayList<Award> awards = new ArrayList<>();
					for (List<Integer> list : sellList) {
						Award award = new Award();
						award.setType(list.get(0));
						award.setId(list.get(1));
						award.setCount(list.get(2));
						awards.add(award);
					}
					String payCardId = String.valueOf(payCard.getPayCardId());
					int daysRemaining = 0;
					// 月卡
					if (payCard.getPayCardId() == ActMonthlyCard.MONTHLY_CARD.getKey()) {
						daysRemaining = TimeHelper.equation(endTimeOfDay, lord.getMonthCard());
					}
					// 季卡
					if (payCard.getPayCardId() == ActMonthlyCard.SEASON_CARD.getKey()) {
						daysRemaining = TimeHelper.equation(endTimeOfDay, lord.getSeasonCard());
					}
					// 周卡
					if (payCard.getCardType() == 3) {
						daysRemaining = TimeHelper.equation(endTimeOfDay, player.getWeekCard().getExpireTime(payCard.getAwardId()));
						playerManager.sendAttachMail(player, awards, MailId.WEEK_CARD_SEASON, payCardId, payCardId, String.valueOf(daysRemaining + 1));
					} else if (payCard.getCardType() == 1 || payCard.getCardType() == 2) {
						playerManager.sendAttachMail(player, awards, MailId.PAY_CARD_NOT_RECEIVED, payCardId, payCardId, String.valueOf(daysRemaining + 1));
					} else {
						continue;
					}
					actRecord.getReceived().put(payCard.getPayCardId(), 1);
				}
			}
			actRecord.getReceived().clear();
			actRecord.setCleanTime(nowDay);
		}
	}

	/**
	 * 无畏尖兵 活动111
	 *
	 * @param rq
	 * @param handler
	 */
	public void actHeroTask(ActTaskHeroRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_TASK_HERO);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActTaskHeroRs.Builder builder = ActTaskHeroRs.newBuilder();
		List<Integer> list = staticLimitMgr.getAddtion(SimpleId.TASK_HERO);
		builder.setTaskNum(list.get(1));
		builder.setIsAward(actRecord.getReceived().containsKey(0));
		List<StaticHeroTask> condList = new ArrayList<>(staticActivityMgr.getStaticHeroTaskMap().values());
		if (condList == null || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		if (actRecord.getTasks().size() == 0) {
			condList.forEach(e -> {
				actRecord.getTasks().put(e.getId(), new ActPassPortTask(e));
			});
		}

		activityManager.checkHeroKowtow(player, TaskType.BUILDING_LEVELUP, 1);

		for (StaticHeroTask e : condList) {
			int keyId = e.getId();
			ActPassPortTask actPassPortTask = actRecord.getTasks().get(keyId);
			// 获取下进度
			int cond = actPassPortTask.getProcess();
			// 已领取奖励
			CommonPb.ActivityCond condPb = PbHelper.createActivityCondPb(e, cond, cond >= e.getCond());
			builder.addActivityCond(condPb.toBuilder().setChangeNum(actPassPortTask.getIsAward()).build());
		}
		handler.sendMsgToPlayer(ActTaskHeroRs.ext, builder.build());
	}

	public void doActHeroTaskReward(DoActTaskHeroRewardRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_TASK_HERO);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_TASK_HERO);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		if (actRecord.getReceived().containsKey(rq.getTaskId())) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		DoActTaskHeroRewardRs.Builder builder = DoActTaskHeroRewardRs.newBuilder();
		if (rq.getTaskId() == 0) { // 领取英雄
			List<Integer> list = staticLimitMgr.getAddtion(SimpleId.TASK_HERO);
			if (actRecord.getReceived().size() < list.get(1)) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
				return;
			}
			int key = playerManager.addAward(player, AwardType.HERO, list.get(0), 1, Reason.ACT_HERO_TASK);
			builder.addAward(PbHelper.createAward(player, AwardType.HERO, list.get(0), 1, key));
		} else {
			ActPassPortTask task = actRecord.getTasks().get(rq.getTaskId());
			StaticHeroTask heroTask = staticActivityMgr.getStaticHeroTaskMap().get(rq.getTaskId());
			int cond = actRecord.getTasks().get(rq.getTaskId()).getProcess();
			if (cond < heroTask.getCond()) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
				return;
			}
			heroTask.getAwardlist().forEach(award -> {
				int key = playerManager.addAward(player, award.get(0), award.get(1), award.get(2), Reason.ACT_HERO_TASK);
				builder.addAward(PbHelper.createAward(player, award.get(0), award.get(1), award.get(2), key));
			});
			task.setIsAward(1);
		}
		actRecord.getReceived().put(rq.getTaskId(), 1);
		handler.sendMsgToPlayer(DoActTaskHeroRewardRs.ext, builder.build());

		// 推送客户端
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 惊喜特惠
	 *
	 * @param rq
	 * @param handler
	 */
	public void actSuripriseGift(ActSuripriseGiftRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_SURIPRISE_GIFT);
		if (activityBase == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SURIPRISE_GIFT);
		if (actRecord == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, actRecord, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		actRecord.checkExprie();
		if (!actRecord.hasNoExprie()) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, actRecord, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		Map<Integer, StaticLimitGift> limitGift = staticActivityMgr.getLimitGiftByAward(actRecord.getAwardId());
		if (limitGift == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, actRecord, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActSuripriseGiftRs.Builder rs = ActSuripriseGiftRs.newBuilder();
		for (ActivityRecord record : actRecord.getActivityRecords()) {
			if (actRecord.getReceived().containsKey(record.getKey())) {
				continue;
			}
			StaticLimitGift staticLimitGift = limitGift.get(record.getKey());
			CommonPb.SuripriseGift.Builder builder = CommonPb.SuripriseGift.newBuilder();
			builder.setKeyId(record.getKey());
			builder.setName(staticLimitGift.getName());
			builder.setGold(staticLimitGift.getDisplay());
			builder.setMoney(staticLimitGift.getMoney());
			staticLimitGift.getAwardList().forEach(e -> {
				builder.addAward(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
			});
			builder.setCount(staticLimitGift.getCount());
			builder.setBuyCount(record.getBuyCount());
			builder.setExpireTime(record.getExpireTime());
			builder.setAsset(staticLimitGift.getAsset());
			builder.setIcon(staticLimitGift.getIcon());
			rs.addGifts(builder);
		}
		actRecord.getReceived().put(ActivityConst.ACT_SURIPRISE_GIFT, 1);
		rs.setTips(0);
		handler.sendMsgToPlayer(ActSuripriseGiftRs.ext, rs.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 特价礼包新
	 *
	 * @param handler
	 */
	public void actSpecialGiftRq(ActSpecialGiftRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SPECIAL_GIFT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int awardId = actRecord.getAwardId();
		ActSpecialGiftRs.Builder builder = ActSpecialGiftRs.newBuilder();
		List<StaticActPayMoney> payMoneyList = actPayMap.get(awardId);
		if (payMoneyList == null || payMoneyList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		flushReceived(actRecord);
		Date openTime = serverManager.getServer().getOpenTime();
		Map<Integer, List<StaticActPayMoney>> map = payMoneyList.stream().filter(e -> player.getLevel() >= e.getLevelDisplay()).collect(Collectors.groupingBy(e -> e.getPosition()));
		for (Map.Entry<Integer, List<StaticActPayMoney>> entry : map.entrySet()) {
			// 活动序号 排序
			payMoneyList = entry.getValue().stream().sorted(Comparator.comparing(StaticActPayMoney::getSort)).collect(Collectors.toList());
			CommonPb.ActPayItem.Builder actPayItem = CommonPb.ActPayItem.newBuilder();
			actPayItem.setPosition(entry.getKey());
			int firstJump = 0;
			for (StaticActPayMoney e : payMoneyList) {
				CommonPb.PayItem.Builder payItem = PayItem.newBuilder();

				int keyId = e.getPayMoneyId();
				int buyState = actRecord.getReceived(keyId);
				if (e.getNextJump() != 0) {
					if (firstJump == 0) {
						firstJump = e.getNextJump();
					}
					// 该档已买过
					if (buyState != 0) {
						firstJump = 0;
					}
				}
				if (firstJump != 0 && e.getPayMoneyId() == firstJump) {
					firstJump = e.getNextJump();
					continue;
				}
				if (buyState >= e.getLimit()) {
					continue;
				}

				payItem.setPayId(e.getPayMoneyId());
				payItem.setState(buyState);
				payItem.setName(e.getName());
				payItem.setPrice(e.getMoney());
				List<List<Integer>> sellList = e.getSellList();
				if (sellList == null || sellList.size() == 0) {
					handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
					return;
				}
				if (sellList.size() > 0) {
					for (List<Integer> sell : sellList) {
						int type = sell.get(0);
						int id = sell.get(1);
						int count = sell.get(2);
						// 钻石奖励挪出来
						if (type == AwardType.GOLD) {
							payItem.setGold(count);
							continue;
						}
						payItem.addAward(PbHelper.createAward(type, id, count));
					}
				}
				payItem.setDesc(e.getDesc());
				payItem.setPercent(e.getPercent());
				payItem.setLimit(e.getLimit());
				payItem.setVal(Integer.valueOf(e.getDesc()));
				payItem.setAsset(e.getAsset());
				payItem.setEndTime(e.getTime(openTime));
				payItem.setIllustration(e.getIllustration());

				actPayItem.addPayItem(payItem);
			}
			if (actPayItem.getPayItemList().size() > 0) {
				builder.addActPayItem(actPayItem);
			}
		}
		handler.sendMsgToPlayer(ActSpecialGiftRs.ext, builder.build());
	}

	/**
	 * 移除已过期的充值ID 避免数据太大造成存储异常
	 *
	 * @param actRecord
	 */
	private void flushReceived(ActRecord actRecord) {
		if (actRecord.getReceived().size() >= 20) {
			Iterator<Integer> it = actRecord.getReceived().keySet().iterator();
			while (it.hasNext()) {
				int key = it.next();
				StaticActPayMoney staticActPayMoney = staticActivityMgr.getPayMoney(key);
				if (staticActPayMoney != null) {
					boolean isOpen = staticActPayMoney.isOpen(serverManager.getServer().getOpenTime(), new Date());
					if (!isOpen) {
						it.remove();
					}
				}
			}
		}
	}

	/**
	 * @Description 阵营骨干
	 * @Date 2021/3/15 10:33
	 * @Param [handler]
	 * @Return
	 **/
	public void actCampMembersRankRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_CAMP_MEMBERS);
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_CAMP_MEMBERS);
		ActivityData activityData = activityManager.getActivity(ActivityConst.ACT_CAMP_MEMBERS);
		if (actRecord == null || activityBase == null || activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActCampMembersRankRs.Builder builder = ActCampMembersRankRs.newBuilder();
		long time = TimeHelper.getHourTime(activityBase.getEndTime(), 19).getTime();
		boolean canAwardTime = false;
		if (System.currentTimeMillis() < time) {
			activityData.refreshCampMembersRank(player.getCountry());
		} else {
			canAwardTime = true;
		}
		CampMembersRank campMember = activityData.getCampMembersRank(player);
		if (campMember == null) {
			campMember = new CampMembersRank(player);
		}
		builder.setState(campMember.getRank());

		List<StaticActRankDisplay> displayList = staticActivityMgr.getActRankDisplay(actRecord.getAwardId());
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		LinkedList<CampMembersRank> campMembers = activityData.getCampMembers(player.getCountry());
		int tempRank = activityManager.obtainAwardGear(campMember.getRank(), displayList);
		HashSet<Integer> awardSet = new HashSet<>();
		HashSet<Long> hashSet = new HashSet<>();
		for (StaticActRankDisplay display : displayList) {
			int rankKeyId = display.getAwardKeyId();
			StaticActAward staticActAward = staticActivityMgr.getActAward(rankKeyId);
			if (staticActAward == null) {
				continue;
			}
			int rank = display.getRank();
			int keyId = staticActAward.getKeyId();
			boolean canAward = false;
			if (canAwardTime) {
				if (campMember.getRank() <= rank && rank == tempRank) {
					canAward = true;
				}
			}
			// 个人记录
			if (campMember.getRank() != rank && tempRank == rank && campMember.getRank() != 0) {
				builder.addActRank(PbHelper.createActRank(player, campMember));
			}
			// 排行数据
			if (rank <= campMembers.size() && rank != 0) {
				CampMembersRank campMembersRank = campMembers.get(rank - 1);
				if (campMembersRank != null) {
					hashSet.add(campMembersRank.getLordId());
					Player target = playerManager.getPlayer(campMembersRank.getLordId());
					builder.addActRank(PbHelper.createActRank(target, campMembersRank));
				}
			}
			// 同一个奖励选项只发送一次
			if (awardSet.contains(keyId)) {
				continue;
			}
			// 奖励数据
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(staticActAward, 1, canAward));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(staticActAward, 0, canAward));
			}
			awardSet.add(keyId);
		}
		// 不够展示人数 需要展示最后一名
		if (hashSet.size() < displayList.size() && !campMembers.isEmpty()) {
			CampMembersRank last = campMembers.getLast();
			if (last.getRank() != campMember.getRank() && !hashSet.contains(last.getLordId())) {
				Player target = playerManager.getPlayer(last.getLordId());
				builder.addActRank(PbHelper.createActRank(target, last));
			}
		}
		handler.sendMsgToPlayer(ActCampMembersRankRs.ext, builder.build());
	}

	/**
	 * @Description 日常训练
	 * @Date 2021/3/16 15:44
	 * @Param [handler]
	 * @Return
	 **/
	public void actDailyTrainRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.DAILY_TRAINRS);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActDailyTrainRs.Builder builder = ActDailyTrainRs.newBuilder();
		builder.setTrainSoldier(actRecord.getRecord(1));
		for (StaticActAward config : condList) {
			CommonPb.ActivityCond.Builder activityPb = CommonPb.ActivityCond.newBuilder();
			activityPb.setKeyId(config.getKeyId());
			activityPb.setCond(config.getCond());
			if (actRecord.getReceived().containsKey(config.getKeyId())) {// 已领取奖励
				activityPb.setIsAward(1);
			} else {// 未领取奖励
				activityPb.setIsAward(0);
			}
			List<List<Integer>> awardList = config.getAwardList();
			for (List<Integer> e : awardList) {
				if (e.size() != 3) {
					continue;
				}
				int type = e.get(0);
				int id = e.get(1);
				int count = e.get(2);
				activityPb.addAward(PbHelper.createAward(type, id, count));
			}
			if (actRecord.getRecord(1) >= config.getCond()) {
				activityPb.setCanAward(true);
			} else {
				activityPb.setCanAward(false);
			}
			activityPb.setParam(config.getParam() == null ? "" : config.getParam());
			activityPb.setDesc(StringUtil.isNullOrEmpty(config.getDesc()) ? "" : config.getDesc());
			builder.addActivityCond(activityPb);
		}
		handler.sendMsgToPlayer(ActDailyTrainRs.ext, builder.build());
	}

	public void actOrderRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_ORDER);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActOrderRs.Builder builder = ActOrderRs.newBuilder();
		builder.setCond(actRecord.getRecord(1));
		for (StaticActAward config : condList) {
			CommonPb.ActivityCond.Builder activityPb = CommonPb.ActivityCond.newBuilder();
			activityPb.setKeyId(config.getKeyId());
			activityPb.setCond(config.getCond());
			if (actRecord.getReceived().containsKey(config.getKeyId())) {// 已领取奖励
				activityPb.setIsAward(1);
			} else {// 未领取奖励
				activityPb.setIsAward(0);
			}
			List<List<Integer>> awardList = config.getAwardList();
			for (List<Integer> e : awardList) {
				if (e.size() != 3) {
					continue;
				}
				int type = e.get(0);
				int id = e.get(1);
				int count = e.get(2);
				activityPb.addAward(PbHelper.createAward(type, id, count));
			}
			activityPb.setParam(config.getParam() == null ? "" : config.getParam());
			activityPb.setDesc(StringUtil.isNullOrEmpty(config.getDesc()) ? "" : config.getDesc());
			builder.addActivityCond(activityPb);
		}
		handler.sendMsgToPlayer(ActOrderRs.ext, builder.build());
	}

	// 魅影活动结束后将玩家身上剩余的英雄币换成突破卡
	private void sendActMyReward() {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			activityManager.actMyEnd(next);
		}
	}

	/**
	 * 累计充值
	 *
	 * @param rq
	 * @param handler
	 */
	public void actGrandRecharegRq(ActGrandRecharegRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_GRAND_TOTAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int activityKeyId = actRecord.getAwardId();
		ActGrandRecharegRs.Builder builder = ActGrandRecharegRs.newBuilder();
		int gold = currentActivity(player, actRecord, 0);
		builder.setGold(gold);
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励ActTopupServerRq
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActGrandRecharegRs.ext, builder.build());
	}

	/**
	 * 勇冠三军
	 *
	 * @param rq
	 * @param handler
	 */
	public void actWellCrownThreeArmyRq(ActWellCrownThreeArmyRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_WELL_CROWN_THREE_ARMY);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_WELL_CROWN_THREE_ARMY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(activityBase);

		int activityKeyId = actRecord.getAwardId();
		ActWellCrownThreeArmyRs.Builder builder = ActWellCrownThreeArmyRs.newBuilder();
		int gold = currentActivity(player, actRecord, 0);
		builder.setGold(gold);
		builder.addCamp(CommonPb.TwoInt.newBuilder().setV1(1).setV2(activityData.getRecord(1)).build());
		builder.addCamp(CommonPb.TwoInt.newBuilder().setV1(2).setV2(activityData.getRecord(2)).build());
		builder.addCamp(CommonPb.TwoInt.newBuilder().setV1(3).setV2(activityData.getRecord(3)).build());
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityKeyId);
		for (StaticActAward e : condList) {
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励ActTopupServerRq
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		}
		handler.sendMsgToPlayer(ActWellCrownThreeArmyRs.ext, builder.build());
	}

	// 阵营骨干结束当天19刷新定榜 刷新任务红点
	public void refreshCampMembersRank(Date now, ActivityBase activityBase, int step) {
		if (activityBase.getActivityId() == ActivityConst.ACT_CAMP_MEMBERS && activityBase.isRankThree() && step == ActivityConst.ACTIVITY_BEGIN) {
			Date refreshTime = TimeHelper.getHourTime(activityBase.getEndTime(), 19);
			if (now.after(refreshTime)) {
				ActivityData activityData = activityManager.getActivity(activityBase);
				if (activityData == null) {
					return;
				}
				long paramsTime = 0;
				try {
					paramsTime = Long.parseLong(activityData.getParams());
				} catch (Exception e) {
					paramsTime = 0;
					logger.debug("activityData.getParams() error activityData.getParams()={}", activityData.getParams());
				}
				Date date = new Date(paramsTime);
				if (date.after(refreshTime) && date.before(now)) {
					return;
				}
				for (CountryData countryData : SpringUtil.getBean(CountryManager.class).getCountrys().values()) {
					activityData.refreshCampMembersRank(countryData.getCountryId());
					int i = 0;
					for (CampMembersRank campMember : activityData.getCampMembers(countryData.getCountryId())) {
						if (i == 100) {
							break;
						}
						i++;
						Player player = playerManager.getPlayer(campMember.getLordId());
						if (player == null) {
							continue;
						}
						playerManager.synActivity(player, activityBase.getActivityId());
					}
				}
				activityData.setParams(String.valueOf(now.getTime()));
			}
		}
	}

	/**
	 * @Description 采集资源
	 * @Date 2021/3/26 14:07
	 * @Param [handler]
	 * @Return
	 **/
	public void actCollectionResourceRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}

		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_COLLECTION_RESOURCE);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() < 1) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		actCollectionResourceRs.Builder builder = actCollectionResourceRs.newBuilder();
		builder.setResourceNum((int) actRecord.getStatus(0));
		for (StaticActAward config : condList) {
			CommonPb.ActivityCond.Builder activityPb = CommonPb.ActivityCond.newBuilder();
			activityPb.setKeyId(config.getKeyId());
			activityPb.setCond(config.getCond());
			if (actRecord.getReceived().containsKey(config.getKeyId())) {// 已领取奖励
				activityPb.setIsAward(1);
			} else {// 未领取奖励
				activityPb.setIsAward(0);
			}
			List<List<Integer>> awardList = config.getAwardList();
			for (List<Integer> e : awardList) {
				if (e.size() != 3) {
					continue;
				}
				int type = e.get(0);
				int id = e.get(1);
				int count = e.get(2);
				activityPb.addAward(PbHelper.createAward(type, id, count));
			}
			activityPb.setParam(config.getParam() == null ? "" : config.getParam());
			activityPb.setDesc(StringUtil.isNullOrEmpty(config.getDesc()) ? "" : config.getDesc());
			builder.addActivityCond(activityPb);
		}
		handler.sendMsgToPlayer(actCollectionResourceRs.ext, builder.build());
	}

	// 采集资源活动结束的时候 发放奖励
	private void sendActCollectionResource(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (actRecord == null) {
				continue;
			}
			// 奖励的
			List<CommonPb.Award> tawards = new ArrayList<>();
			List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
			condList.forEach((statiAward) -> {
				if (actRecord.getStatus(0) >= statiAward.getCond() && !actRecord.getReceived().containsKey(statiAward.getKeyId())) {
					tawards.addAll(statiAward.getAwardPbList());
				}
			});
			// 已达成条件,未领取奖励,补发奖励邮件
			if (tawards.size() > 0) {
				playerManager.sendAttachMail(next, PbHelper.finilAward(tawards), MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}
		}
	}

	// 物质搜寻相关
	public void actMaterInfo(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SEARCH);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActMaterInfoRs.Builder builder = ActMaterInfoRs.newBuilder();
		builder.setFinish((int) actRecord.getStatus(actRecord.getCount()));
		if (actRecord.getCount() == 0) {
			actRecord.addCount();
		}
		builder.setDay(actRecord.getCount());
		condList.forEach(x -> {
			CommonPb.ActivityCond.Builder builder1 = CommonPb.ActivityCond.newBuilder();
			builder1.setKeyId(x.getKeyId());
			builder1.setCond(x.getCond());
			builder1.setIsAward(1);
			if ((!actRecord.getReceived().containsKey(x.getKeyId()) && actRecord.getStatus(x.getSortId()) >= x.getCond()) || actRecord.getStatus(x.getSortId()) < x.getCond()) {
				builder1.setIsAward(0);
			}
			builder1.addAllAward(x.getAwardPbList());
			builder.addActivityCond(builder1);
		});
		handler.sendMsgToPlayer(ActMaterInfoRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	public void actMaterAward(ActMaterAwardRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SEARCH);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int keyId = rq.getKeyId();
		StaticActAward actAward = staticActivityMgr.getActAward(keyId);
		if (actAward == null) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		if (actRecord.getStatus(actAward.getSortId()) < actAward.getCond() || actRecord.getReceived().containsKey(actAward.getKeyId())) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}
		ActMaterAwardRs.Builder builder = ActMaterAwardRs.newBuilder();
		actAward.getAwardList().forEach(x -> {
			int i = playerManager.addAward(player, x.get(0), x.get(1), x.get(2), Reason.ACT_SEARCH);
			builder.addAward(PbHelper.createAward(player, x.get(0), x.get(1), x.get(2), i));
		});
		// builder.addAllAward(actAward.getAwardPbList());
		handler.sendMsgToPlayer(ActMaterAwardRs.ext, builder.build());
		actRecord.updateReceive(actAward.getKeyId(), 1);
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	// 夺宝奇兵相关
	public void raidersInfo(RaidersRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int acId = rq.getAcId();
		ActRecord actRecord = activityManager.getActivityInfo(player, acId);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		RaidersRs.Builder builder = RaidersRs.newBuilder();
		builder.setFinish(actRecord.getRecord().size());

		int free = actRecord.getRecord().size() > 0 ? 0 : 1;

		int count = actRecord.getCount() + free;
		builder.setCount(count);// 剩余次数

		int i = actRecord.getCount() + free + actRecord.getRecord().size();

		StaticDialAwards staticDialAwards = staticActivityMgr.getStaticDialAwards(activityBase.getAwardId(), i + 1);
		builder.setRechar(0);
		if (staticDialAwards != null) {
			int i2 = staticDialAwards.getCond() - (int) actRecord.getStatus(0L);
			builder.setRechar(i2 < 0 ? 0 : i2);
		}
		List<StaticDialAwards> staticDialAwardsList = staticActivityMgr.getStaticDialAwardsList(activityBase.getAwardId());
		List<StaticActAward> actAwardById = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		builder.setTarget(0);
		if (actAwardById != null && !actAwardById.isEmpty()) {
			StaticActAward staticActAward = actAwardById.get(0);
			builder.setTarget(staticActAward.getCond());

			CommonPb.ActivityCond.Builder builder1 = CommonPb.ActivityCond.newBuilder();
			builder1.setKeyId(staticActAward.getKeyId());
			builder1.addAllAward(staticActAward.getAwardPbList());
			builder1.setIsAward(actRecord.getReceived().containsKey(staticActAward.getKeyId()) ? 1 : 0);
			builder1.setCanAward(actRecord.getRecord().size() >= staticDialAwardsList.size());
			builder.setActAward(builder1);
		}
		for (int j = 0; j < staticDialAwardsList.size(); j++) {
			builder.addIndex(actRecord.getRecord().containsKey(j + 1) ? 1 : 0);
			StaticDialAwards staticDialAwards1 = staticDialAwardsList.get(j);
			boolean flag = false;
			if (actRecord.getRecord().containsValue(staticDialAwards1.getKeyId())) {
				flag = true;
			}
			List<List<Integer>> award = staticDialAwards1.getAward();
			for (List<Integer> integers : award) {
				CommonPb.ActDial.Builder builder1 = CommonPb.ActDial.newBuilder();
				builder1.setGot(flag ? 1 : 0);
				builder1.setItemType(integers.get(0));
				builder1.setItemId(integers.get(1));
				builder1.setItemCount(integers.get(2));
				builder1.setPlace(integers.get(3));
				builder.addActdial(builder1);
			}
		}
		handler.sendMsgToPlayer(RaidersRs.ext, builder.build());
	}

	public void doRaiders(DoRaidersRq rq, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int acId = rq.getAcId();
		ActRecord actRecord = activityManager.getActivityInfo(player, acId);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		List<StaticActAward> actAwardById = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (actAwardById == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int index = rq.getIndex();
		if (actRecord.getRecord().containsKey(index)) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}
		// 判断是否有免费次数或者可以抽取
		if (actRecord.getRecord().size() > 0 && actRecord.getCount() == 0) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}
		if (actRecord.getRecord().size() > 0) {
			actRecord.setCount(actRecord.getCount() - 1);
		}
		StaticDialAwards randomDail = staticActivityMgr.getRandomDail(activityBase.getAwardId(), actRecord.getRecord());
		DoRaidersRs.Builder builder = DoRaidersRs.newBuilder();
		if (randomDail != null) {
			List<List<Integer>> award = randomDail.getAward();
			if (award != null) {
				award.forEach(x -> {
					builder.addAward(PbHelper.createAward(x.get(0), x.get(1), x.get(2)));
					playerManager.addAward(player, x.get(0), x.get(1), x.get(2), acId == ActivityConst.ACT_RAIDERS ? Reason.ACT_RAIDERS : Reason.ACT_EGG);
				});
			}
			actRecord.putRecord(index, randomDail.getKeyId());
		}
		if (actRecord.getRecord().size() >= actAwardById.get(0).getCond()) {
			builder.addAllActdial(actAwardById.get(0).getAwardPbList());
		}
		handler.sendMsgToPlayer(DoRaidersRs.ext, builder.build());
		if (actRecord.getCount() == 0) {
			playerManager.synActivity(player, activityBase.getActivityId());
		}
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	// 充值转盘相关
	public void recharDialInfo(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.RE_DIAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		DialEntity dial = staticActivityMgr.getActDialMap(activityBase.getAwardId());
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int i = actRecord.getCount() + actRecord.getRecord().size();// 这个是总次数
		List<StaticActAward> actAwardById = staticActivityMgr.getActAwardById(actRecord.getAwardId(), i + 1);
		RecharDialRs.Builder builder = RecharDialRs.newBuilder();
		builder.setFinish((int) actRecord.getStatus(0L));
		if (actAwardById != null && !actAwardById.isEmpty()) {
			builder.setTarget(actAwardById.get(0).getCond());
		} else {
			actAwardById = staticActivityMgr.getActAwardById(actRecord.getAwardId(), i);
			if (actAwardById != null && !actAwardById.isEmpty()) {
				builder.setTarget(actAwardById.get(0).getCond());
			}
		}
		builder.setCount(actRecord.getCount());
		List<StaticActDial> actDialList = dial.getActDialList(1);
		actDialList.forEach(x -> {
			CommonPb.ActDial.Builder builder1 = CommonPb.ActDial.newBuilder();
			builder1.setDialId(x.getDialId());
			builder1.setType(1);
			builder1.setGot(actRecord.getRecord().getOrDefault(x.getKeyId(), 0));
			builder1.setItemType(x.getItemType());
			builder1.setItemId(x.getItemId());
			builder1.setItemCount(x.getItemCount());
			builder1.setPlace(x.getPlace());
			builder.addActdial(builder1);
		});
		handler.sendMsgToPlayer(RecharDialRs.ext, builder.build());

	}

	public void doRecharDialInfo(DoRecharDialRq rq, ClientHandler handler) {

		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.RE_DIAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		DialEntity dial = staticActivityMgr.getActDialMap(activityBase.getAwardId());
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		List<StaticActAward> actAwardById = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (actAwardById == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		int count = rq.getCount();
		if (count <= 0) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		if (actRecord.getCount() == 0) {
			handler.sendErrorMsgToPlayer(GameError.NOT_FINISH_TASK);
			return;
		}
		int totalCount = actRecord.getCount();
		if (count >= totalCount) {
			actRecord.setCount(0);
			count = totalCount;
		} else {
			if (count > 0) {
				actRecord.setCount(actRecord.getCount() - count);
			}
		}
		DoRecharDialRs.Builder builder = DoRecharDialRs.newBuilder();
		for (int i = 0; i < count; i++) {
			StaticActDial actDial = dial.getRandomDail(1, actRecord.getRecord());
			int i1 = playerManager.addAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), Reason.ACT_RECHAR_DIAL);
			builder.addAward(PbHelper.createAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), i1));
			builder.addPlace(actDial.getPlace());
			builder.setCount(actRecord.getCount());
			if (actDial.getKeyId() != 0) {
				actRecord.addRecord(actDial.getKeyId(), 1);
			}
		}
		handler.sendMsgToPlayer(DoRecharDialRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(EventEnum.DO_DIAL, new CommonTipActor(player, actRecord, activityBase));
	}

	// 好运准盘相关
	public void queryLucklyInfo(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.LUCK_DIAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActAward> displayList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (displayList == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		StaticActDialPurp common = staticActivityMgr.getDialPurp(actRecord.getAwardId());
		if (common == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		DialEntity dial = staticActivityMgr.getActDialMap(actRecord.getAwardId());
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		ActLucklyDialRs.Builder builder = ActLucklyDialRs.newBuilder();
		int count = actRecord.getCount();// 已转次数
		int freeCount = common.getFreeTimes() - count < 0 ? 0 : common.getFreeTimes() - count;
		builder.setFree(freeCount);
		builder.setPrice(common.getOnePrice());
		builder.setTenPrice(common.getTenPrice());
		builder.setCount((int) actRecord.getStatus(1L));

		List<StaticActDial> actDialList = dial.getActDialList(1);
		actDialList.forEach(x -> {
			CommonPb.ActDial.Builder builder1 = CommonPb.ActDial.newBuilder();
			builder1.setDialId(x.getDialId()).setType(x.getType()).setItemType(x.getItemType()).setItemId(x.getItemId()).setItemCount(x.getItemCount()).setPlace(x.getPlace());
			builder.addActDial(builder1);
		});
		for (StaticActAward ex : displayList) {
			CommonPb.ActivityCond.Builder activityCond = CommonPb.ActivityCond.newBuilder();
			activityCond.setKeyId(ex.getKeyId()).setCond(ex.getCond()).setIsAward(actRecord.getReceived(ex.getKeyId())).addAllAward(ex.getAwardPbList()).build();
			builder.addActivityCond(activityCond.build());
		}
		activityData.getRecords().forEach(rewardRecord -> {
			builder.addRecord(rewardRecord.ser(activityData.getActivityId()));
		});
		handler.sendMsgToPlayer(ActLucklyDialRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	public void doLucklyAward(DoLucklyDialRq req, ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		int count = req.getCount();
		// 检查次数
		if (count != 1 && count != 10) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.LUCK_DIAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 找到活动Id
		int awardId = actRecord.getAwardId();
		StaticActDialPurp dial = staticActivityMgr.getDialPurp(awardId);
		if (dial == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}

		// 活动结构
		DialEntity dialEntity = staticActivityMgr.getActDialMap(awardId);
		if (dialEntity == null) {
			handler.sendErrorMsgToPlayer(GameError.NO_CONFIG);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		// 单抽，如果免费次数没了，判断金币
		int freeTimes = dial.getFreeTimes();
		int getCount = actRecord.getCount();

		int costGold = 0;
		boolean isfree = false;
		switch (count) {
		case 1:
			if (getCount < freeTimes) {
				actRecord.addCount();
				isfree = true;
			} else {
				int itemNum = player.getItemNum(ItemId.LUCK_DIAL);
				if (itemNum >= count) {
					playerManager.subAward(player, AwardType.PROP, ItemId.LUCK_DIAL, count, Reason.LUCK_DIAL);
					isfree = true;
				}
			}
			break;
		case 10:
			int itemNum = player.getItemNum(ItemId.LUCK_DIAL);
			if (itemNum >= count) {
				playerManager.subAward(player, AwardType.PROP, ItemId.LUCK_DIAL, count, Reason.LUCK_DIAL);
				isfree = true;
			}
		default:
			break;
		}

		if (!isfree) {
			if (player.getGold() < (count == 1 ? dial.getOnePrice() : dial.getTenPrice())) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			playerManager.subAward(player, AwardType.GOLD, 0, count == 1 ? dial.getOnePrice() : dial.getTenPrice(), Reason.LUCK_DIAL);
			costGold = count == 1 ? dial.getOnePrice() : dial.getTenPrice();
		}
		DoLucklyDialRs.Builder builder = DoLucklyDialRs.newBuilder();
		for (int i = 0; i < count; i++) {
			StaticActDial actDial = dialEntity.getRandomDail(1, actRecord.getRecord());
			// 获取保底物品的配置
			List<StaticActDial> actDialMinGuaranteeList = staticActivityMgr.getActDialMinGuaranteeList(ActivityConst.LUCK_DIAL, awardId);
			if (null != actDial && actDialMinGuaranteeList.size() > 0) {
				Iterator<StaticActDial> iterator = actDialMinGuaranteeList.iterator();
				while (iterator.hasNext()) {
					StaticActDial next = iterator.next();
					if (null == actDial || null == next || actDial.getDialId() != next.getDialId()) {
						actRecord.updateDailGuaranteeNum(next.getDialId());
						logger.error("ActivityID=" + activityBase.getActivityId() + "   保底物品未抽到次数=" + actRecord.getDailGuaranteeNum(next.getDialId()));
					}
					if (actDial.getDialId() == next.getDialId()) {
						actRecord.getDailGuarantee().put(next.getDialId(), 0);
						logger.error("ActivityID=" + activityBase.getActivityId() + "   保底物品抽到重置为0次");
						break;
					}

					Integer dailMinGuaranteeNum = actRecord.getDailMinGuaranteeNum(staticActivityMgr, next.getDialId(), actRecord.getAwardId(), 1);
					if (dailMinGuaranteeNum == null) {
						continue;
					}
					int dailGuaranteeNum = actRecord.getDailGuaranteeNum(next.getDialId());
					if (dailGuaranteeNum >= dailMinGuaranteeNum) {
						logger.error("ActivityID=" + activityBase.getActivityId() + "   保底物品未抽到次数=" + actRecord.getDailGuaranteeNum(next.getDialId()) + " >= " + dailMinGuaranteeNum);
						actDial = next;
						actRecord.getDailGuarantee().put(next.getDialId(), 0);
						logger.error("ActivityID=" + activityBase.getActivityId() + "   保底物品抽到重置为0次");
						break;
					}
				}
			}
			int key;
			key = playerManager.addAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), Reason.LUCK_DIAL);
			builder.addAward(PbHelper.createAward(player, actDial.getItemType(), actDial.getItemId(), actDial.getItemCount(), key));
			// 这里要全局
			if (actDial.getBeRecorded() == 1) {
				chatManager.sendWorldChat(ChatId.MY_SOUND, player.getNick(), activityBase.getStaticActivity().getName(), actDial.getItemType() + "", actDial.getItemId() + "");
				activityData.addRewardRecord(new LuckPoolRewardRecord(player, PbHelper.createAward(actDial.getItemType(), actDial.getItemId(), actDial.getItemCount()).build()));
			}

			builder.addPlace(actDial.getPlace());
			if (actDial.getKeyId() != 0) {
				actRecord.addRecord(actDial.getKeyId(), 1);
			}
			List<List<Integer>> buyAward = dial.getBuyAward();
			if (buyAward != null) {
				for (List<Integer> list : buyAward) {
					builder.addBuyAward(PbHelper.createAward(list.get(0), list.get(1), list.get(2)));
					playerManager.addAward(player, list.get(0), list.get(1), list.get(2), Reason.LUCK_DIAL);
				}
			}
		}
		// 抽取次数累计
		activityManager.updActPerson(player, activityBase, count, 1);
		builder.setGold(player.getGold());
		builder.setProp(CommonPb.Prop.newBuilder().setPropId(ItemId.LUCK_DIAL).setPropNum(player.getItemNum(ItemId.LUCK_DIAL)).build());
		handler.sendMsgToPlayer(DoLucklyDialRs.ext, builder.build());
		// 活动类型
		SpringUtil.getBean(EventManager.class).spin_the_wheel(player, Lists.newArrayList(ActivityConst.LUCK_DIAL, builder.getAwardList().toString(), count));
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(ActivityConst.LUCK_DIAL).isAward(false).awardId(0).giftName("").roleId(player.roleId).vip(player.getVip()).costGold(costGold).channel(player.account.getChannel()).build());
		SpringUtil.getBean(LogUser.class).activity_log(ActivityLog.builder().activityId(ActivityConst.LUCK_DIAL).isAward(false).awardId(0).giftName("").roleId(player.roleId).vip(player.getVip()).costGold(costGold).channel(player.account.getChannel()).build());
	}

	public void actMonster(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MONSTER);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (null == condList || condList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActMonsterRs.Builder builder = ActMonsterRs.newBuilder();
		builder.setFinish(actRecord.getCount());
		condList.forEach(x -> {
			CommonPb.ActivityCond.Builder builder1 = CommonPb.ActivityCond.newBuilder();
			builder1.setKeyId(x.getKeyId());
			builder1.setCond(x.getCond());
			builder1.setIsAward(1);
			if ((!actRecord.getReceived().containsKey(x.getKeyId()) && actRecord.getCount() >= x.getCond()) || actRecord.getCount() < x.getCond()) {
				builder1.setIsAward(0);
			}
			builder1.addAllAward(x.getAwardPbList());
			builder1.setDesc(x.getDesc());
			builder.addActivityCond(builder1);
		});
		handler.sendMsgToPlayer(ActMonsterRs.ext, builder.build());
	}

	// 进阶通行证奖励展示
	public void actPassPortPorAwardRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_PASS_PORT);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActPassPortPorAwardRs.Builder builder = ActPassPortPorAwardRs.newBuilder();
		Map<Integer, Integer> record = actRecord.getRecord();
		Integer isBuy = record.get(1);
		if (isBuy == null || isBuy == 1) {
			handler.sendMsgToPlayer(ActivityPb.ActPassPortPorAwardRs.ext, builder.build());
			return;
		}
		List<StaticPassPortAward> passPortList = staticActivityMgr.getPassPortList(actRecord.getAwardId());
		if (passPortList == null || passPortList.size() == 0) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		Map<Integer, Map<Integer, Award>> map = new HashMap<>();
		passPortList.forEach(e -> {
			if (e.getType() == 2 && e.getAward() != null) {
				e.getAward().forEach(v -> {
					if (v.size() == 3) {
						Award award = new Award(v);
						Map<Integer, Award> integerAwardMap = map.computeIfAbsent(award.getType(), k -> new HashMap<>());
						Award award1 = integerAwardMap.computeIfAbsent(award.getId(), k -> award);
						if (award != award1) {
							award1.setCount(award1.getCount() + award.getCount());
						}
					}
				});
			}
		});
		map.values().forEach(e -> {
			e.values().forEach(v -> {
				builder.addAward(v.wrapPb());
			});
		});
		handler.sendMsgToPlayer(ActivityPb.ActPassPortPorAwardRs.ext, builder.build());
	}

	/**
	 * 材料置换活动
	 **/
	public void getMaterialSubstitution(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MATERIAL_SUBSTITUTION);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		Map<Integer, Integer> received = actRecord.getReceived();
		GetMaterialSubstitutionRs.Builder builder = GetMaterialSubstitutionRs.newBuilder();
		// 已经兑换的次数
		int vipConvert = received.getOrDefault(1, 0);
		int convertCount = received.getOrDefault(2, 0);
		StaticMaterialSubstituteVip vip = staticActivityMgr.getMaterialSubstituteMap().get(player.getVip());
		int vipFree = vip == null ? 0 : vip.getFreeTimes();
		if (vipConvert >= vipFree) {
			builder.setFree(0);
			builder.setCost(staticActivityMgr.getMaterialSubstituteCost(convertCount + 1));
		} else {
			builder.setFree(vipFree - vipConvert);
		}
		builder.setFreeTimes(vipFree);
		handler.sendMsgToPlayer(GetMaterialSubstitutionRs.ext, builder.build());
	}

	/**
	 * 材料置换
	 **/
	public void materialSubstitution(ClientHandler handler, MaterialSubstitutionRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_MATERIAL_SUBSTITUTION);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int propId = rq.getPropId();
		int targetId = rq.getTargetId();
		HashMap<Integer, Item> itemMap = player.getItemMap();
		Item item = itemMap.get(propId);
		StaticProp targetProp = staticPropMgr.getStaticProp(targetId);
		StaticProp staticProp = staticPropMgr.getStaticProp(propId);
		if (staticProp == null || item == null || targetProp == null || propId == targetId || targetProp.getColor() != staticProp.getColor() || item.getItemNum() < 1) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		Map<Integer, Integer> received = actRecord.getReceived();
		// 已经兑换的次数
		int vipConvert = received.getOrDefault(1, 0);
		int convertCount = received.getOrDefault(2, 0);
		StaticMaterialSubstituteVip vip = staticActivityMgr.getMaterialSubstituteMap().get(player.getVip());
		int vipFree = vip == null ? 0 : vip.getFreeTimes();
		int gold = 0;
		if (vipConvert < vipFree) {
			received.put(1, ++vipConvert);
		} else {
			int cost = staticActivityMgr.getMaterialSubstituteCost(convertCount + 1);
			if (player.getGold() < cost) {
				handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
				return;
			}
			gold = cost;
			playerManager.subAward(player, AwardType.GOLD, 0, cost, Reason.ACT_MATERIAL_SUBSTITUTION);
			received.put(2, ++convertCount);
		}
		playerManager.subAward(player, AwardType.PROP, propId, 1, Reason.ACT_MATERIAL_SUBSTITUTION);
		playerManager.addAward(player, AwardType.PROP, targetId, 1, Reason.ACT_MATERIAL_SUBSTITUTION);
		MaterialSubstitutionRs.Builder builder = MaterialSubstitutionRs.newBuilder();
		if (vipConvert >= vipFree) {
			builder.setFree(0);
			builder.setCost(staticActivityMgr.getMaterialSubstituteCost(convertCount + 1));
		} else {
			builder.setFree(vipFree - vipConvert);
		}
		builder.setGold(player.getGold());
		Lists.newArrayList(propId, targetId).forEach(e -> {
			Item v = player.getItem(e);
			if (v == null) {
				return;
			}
			builder.addProp(v.wrapPb());
		});
		LogUser.getMaterialSubstitutionLog(new ActMaterialSubstitutionLog().builder().lordId(player.roleId).date(new Date()).lv(player.getLord().getLevel()).vip(player.getVip()).propId(propId).targetId(targetId).gold(gold).freeCount(vipConvert).convertCount(convertCount).build());
		handler.sendMsgToPlayer(MaterialSubstitutionRs.ext, builder.build());
	}

	public void broodAct(ClientHandler handler) {

		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.BLOOD_ACTIVITY);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(actRecord.getAwardId());
		if (condList == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		ActivityPb.BroodActRs.Builder builder = ActivityPb.BroodActRs.newBuilder();
		builder.setCount(actRecord.getCount());
		condList.forEach(e -> {
			int keyId = e.getKeyId();
			if (actRecord.getReceived().containsKey(keyId)) {// 已领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 1));
			} else {// 未领取奖励
				builder.addActivityCond(PbHelper.createActivityCondPb(e, 0));
			}
		});
		handler.sendMsgToPlayer(ActivityPb.BroodActRs.ext, builder.build());

	}

	public void sendBroodAct() {
		WorldData worldData = worldManager.getWolrdInfo();
		WorldActPlan worldActPlan = worldData.getWorldActPlans().get(WorldActivityConsts.ACTIVITY_12);
		LocalDate now1 = LocalDate.now();
		if (worldActPlan == null || worldActPlan.getPreheatTime() == 0 || now1.getDayOfWeek().getValue() != 5 || TimeHelper.isThisWeek(worldActPlan.getTargetSuccessTime())) {
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.BLOOD_ACTIVITY);
		if (activityBase == null) {
			return;
		}
		List<StaticActAward> condList = staticActivityMgr.getActAwardById(activityBase.getAwardId());
		if (condList == null) {
			return;
		}
		Map<Long, Player> players = playerManager.getPlayers();
		for (Player player : players.values()) {
			boolean flag = false;
			ActRecord activityInfo = activityManager.getActivityInfo(player, ActivityConst.BLOOD_ACTIVITY);
			if (activityInfo == null) {
				continue;
			}
			List<Award> list = new ArrayList<>();
			HashBasedTable<Integer, Integer, Award> table = HashBasedTable.create();
			for (StaticActAward staticActAward : condList) {
				if (staticActAward.getCond() <= activityInfo.getCount() && !activityInfo.getReceived().containsKey(staticActAward.getKeyId())) {
					List<List<Integer>> awardList = staticActAward.getAwardList();
					for (List<Integer> a : awardList) {
						// playerManager.addAward(player, a.get(0), a.get(1), a.get(2), Reason.ACT_AWARD);
						// list.add(new Award(a.get(0), a.get(1), a.get(2)));
						Award award = table.get(a.get(0), a.get(1));
						if (award == null) {
							table.put(a.get(0), a.get(1), new Award(a.get(0), a.get(1), a.get(2)));
						} else {
							award.setCount(award.getCount() + a.get(2));
						}
						flag = true;
					}
				}
			}
			list.addAll(table.values());
			if (flag) {
				playerManager.sendAttachMail(player, list, MailId.ACTIVITY_MAIL_AWARD, activityBase.getStaticActivity().getName());
			}
			// 推送活动消失
			if (player.isLogin) {
				ActivityEventManager.getInst().activityTip(EventEnum.BUY_BROOD_DISPEAR, new CommonTipActor(player, activityInfo, activityBase));
			}
			activityInfo.cleanActivity();
		}
	}

	/**
	 * 春节活动-春节献礼
	 **/
	public void actSpringAwardRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SPRING_FESTIVAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActSpringFestival> springAward = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringAward);
		if (springAward.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActSpringAwardRs.Builder builder = ActSpringAwardRs.newBuilder();
		builder.setEndTime(activityBase.getEndTime().getTime());
		int luckDrawSpeed = activityData.getRecordNum(0);
		builder.setLuckDrawSpeed(luckDrawSpeed);
		for (StaticActSpringFestival spring : springAward) {
			ActSpringFestival.Builder b = spring.wrapBp();
			b.setIsAward(actRecord.getReceived(spring.getKeyId()) == 0 ? 0 : 1);
			builder.addGoods(b);
		}
		List<ActSpringFestival.Builder> goodsBuilderList = builder.getGoodsBuilderList();
		ActSpringFestival.Builder display = goodsBuilderList.stream().filter(e -> e.getIsAward() == 0).sorted(Comparator.comparing(e -> e.getSortId())).findFirst().orElse(goodsBuilderList.get(goodsBuilderList.size() - 1));
		builder.setDisplay(display);
		ActSpringFestival.Builder stageBuilder = goodsBuilderList.stream().filter(e -> luckDrawSpeed >= e.getCond()).sorted(Comparator.comparing(ActSpringFestival.Builder::getStage).reversed()).findFirst().orElse(goodsBuilderList.get(0));
		builder.setStage(stageBuilder.getStage());
		// 推动春节充值活动的红点
		builder.setRechargeTips(false);
		List<StaticActSpringFestival> springList = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringRecharge);
		for (StaticActSpringFestival e : springList) {
			if (e.getCond() == 0 && actRecord.getReceived(e.getKeyId()) != GameServer.getInstance().currentDay) {
				actRecord.getReceived().remove(e.getKeyId());
			}
			if (actRecord.getStatus(0) >= e.getCond() && !actRecord.getReceived().containsKey(e.getKeyId())) {
				builder.setRechargeTips(true);
				break;
			}
		}
		handler.sendMsgToPlayer(ActSpringAwardRs.ext, builder.build());
	}

	/**
	 * 春节活动-春节转盘
	 **/
	public void actSpringTurntableRq(ClientHandler handler, ActSpringTurntableRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SPRING_FESTIVAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		// 转盘普通物品列表
		List<StaticActSpringFestival> springTurntable = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringTurntable);
		// 转盘特殊物品列表
		List<StaticActSpringFestival> specialList = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringTurntableSpecial);
		if (springTurntable.isEmpty() || specialList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActSpringTurntableRs.Builder builder = ActSpringTurntableRs.newBuilder();
		builder.setEndTime(activityBase.getEndTime().getTime());
		for (StaticActSpringFestival spring : specialList) {
			ActSpringFestival.Builder b = spring.wrapBp();
			b.setIsAward(actRecord.getReceived(spring.getKeyId()) == 0 ? 0 : 1);
			b.setIsExis(actRecord.getRecord().containsKey(spring.getKeyId()));
			builder.addSpecial(b);
		}
		List<ActSpringFestival.Builder> specialBuilderList = builder.getSpecialBuilderList();
		ActSpringFestival.Builder specialBuilder = specialBuilderList.stream().filter(e -> e.getIsAward() == 0).sorted(Comparator.comparing(e -> e.getSortId())).findFirst().orElse(specialBuilderList.get(specialBuilderList.size() - 1));
		if (rq.getIsNext()) {
			if (specialBuilder.getIsAward() > 0 || !specialBuilder.getIsExis()) {
				handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
				return;
			} else {
				specialBuilder.setIsAward(1);
				actRecord.getReceived().put(specialBuilder.getKeyId(), 1);
				specialBuilder = specialBuilderList.stream().filter(e -> e.getIsAward() == 0).sorted(Comparator.comparing(e -> e.getSortId())).findFirst().orElse(specialBuilderList.get(specialBuilderList.size() - 1));
			}
		}
		builder.setRank(specialBuilder.getSortId());
		builder.setDisplay(specialBuilder);
		springTurntable.forEach(e -> {
			builder.addGoods(e.wrapBp());
		});
		builder.setProp(Prop.newBuilder().setPropId(ItemId.LANTERN).setPropNum(player.getItemNum(ItemId.LANTERN)).build());
		handler.sendMsgToPlayer(ActSpringTurntableRs.ext, builder.build());
	}

	/**
	 * 春节活动-春节充值
	 **/
	public void actSpringRechargeRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SPRING_FESTIVAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActSpringFestival> springRecharge = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringRecharge);
		if (springRecharge.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ActSpringRechargeRs.Builder builder = ActSpringRechargeRs.newBuilder();
		builder.setEndTime(activityBase.getEndTime().getTime());
		builder.setRechargeAmount((int) actRecord.getStatus(0));
		for (StaticActSpringFestival spring : springRecharge) {
			ActSpringFestival.Builder b = spring.wrapBp();
			int received = actRecord.getReceived(spring.getKeyId());
			if (spring.getCond() == 0 && received != GameServer.getInstance().currentDay) {
				actRecord.getReceived().remove(spring.getKeyId());
				received = 0;
			}
			b.setIsAward(received == 0 ? 0 : 1);
			builder.addGoods(b);
		}
		handler.sendMsgToPlayer(ActSpringRechargeRs.ext, builder.build());
	}

	/**
	 * 春节活动-转春节转盘
	 **/
	public void doSpringTurntableRq(ClientHandler handler, DoSpringTurntableRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SPRING_FESTIVAL);
		if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase.getActivityId());
		if (actRecord == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int count = rq.getCount();
		if (count < 1 || count > 10) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		if (player.getItemNum((ItemId.LANTERN)) < count) {
			handler.sendErrorMsgToPlayer(GameError.COUNT_ERROR);
			return;
		}
		playerManager.subAward(player, AwardType.PROP, ItemId.LANTERN, count, Reason.ACT_SPRING_FESTIVAL);
		// 转盘普通物品列表
		List<StaticActSpringFestival> springTurntable = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringTurntable);
		// 转盘特殊物品列表
		List<StaticActSpringFestival> specialList = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringTurntableSpecial);
		if (springTurntable.isEmpty() || specialList.isEmpty()) {
			handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
			return;
		}
		ArrayList<ActSpringFestival.Builder> specialBuilderList = new ArrayList<>();
		for (StaticActSpringFestival spring : specialList) {
			ActSpringFestival.Builder b = spring.wrapBp();
			b.setIsAward(actRecord.getReceived(spring.getKeyId()) == 0 ? 0 : 1);
			b.setIsExis(actRecord.getRecord().containsKey(spring.getKeyId()));
			specialBuilderList.add(b);
		}
		ActSpringFestival.Builder specialBuilder = specialBuilderList.stream().filter(e -> e.getIsAward() == 0).sorted(Comparator.comparing(e -> e.getSortId())).findFirst().orElse(specialBuilderList.get(specialBuilderList.size() - 1));
		StaticActSpringFestival special = specialList.stream().filter(e -> e.getKeyId() == specialBuilder.getKeyId()).findFirst().orElse(null);
		// 奖品池
		ArrayList<StaticActSpringFestival> awardList = Lists.newArrayList(springTurntable);
		// 特殊奖励抽中了将不放入奖品池
		if (!specialBuilder.getIsExis()) {
			awardList.add(special);
		}
		DoSpringTurntableRs.Builder builder = DoSpringTurntableRs.newBuilder();
		for (int i = 0; i < count; i++) {
			int total = 0;
			int randomNumber = RandomUtil.getRandomNumber(awardList.stream().mapToInt(StaticActSpringFestival::getProbability).sum());
			for (StaticActSpringFestival springFestival : awardList) {
				total += springFestival.getProbability();
				if (total > randomNumber) {
					ArrayList<Award> awards = new ArrayList<>();
					if (springFestival.getType() == SpringType.SpringTurntableSpecial) {
						springFestival.getAwardList().forEach(x -> {
							if (x.size() != 3) {
								return;
							}
							awards.add(new Award(x.get(0), x.get(1), x.get(2)));
							chatManager.sendWorldChat(ChatId.MY_SOUND, player.getNick(), activityBase.getStaticActivity().getName(), String.valueOf(x.get(0)), String.valueOf(x.get(1)));
						});
						awardList.remove(awardList.size() - 1);
						actRecord.getRecord().put(springFestival.getKeyId(), 1);
						specialBuilder.setIsExis(true);
					} else {
						ArrayList<Award> turntable = new ArrayList<>();
						springFestival.getAwardList().forEach(x -> {
							if (x.size() != 4) {
								return;
							}
							// 此处Award的keyId是概率
							turntable.add(new Award(x.get(3), x.get(0), x.get(1), x.get(2)));
						});
						int random = RandomUtil.getRandomNumber(turntable.stream().mapToInt(Award::getKeyId).sum());
						int totalAward = 0;
						for (Award award : turntable) {
							totalAward += award.getKeyId();
							if (totalAward > random) {
								award.setKeyId(0);
								awards.add(award);
								break;
							}
						}
					}
					awards.forEach(x -> {
						builder.addAward(x.wrapPb());
						playerManager.addAward(player, x, Reason.ACT_SPRING_FESTIVAL);
					});
					builder.addPlace(springFestival.getPlace());
					builder.setLuckDrawId(springFestival.getKeyId());
					break;
				}
			}
		}
		int before = activityData.getRecordNum(0);
		activityData.addRecord(0, count);
		int after = activityData.getRecordNum(0);
		builder.setDisplay(specialBuilder);
		builder.setProp(Prop.newBuilder().setPropId(ItemId.LANTERN).setPropNum(player.getItemNum(ItemId.LANTERN)).build());
		// 推动春节献礼活动的红点
		builder.setAwardTips(false);
		List<StaticActSpringFestival> springList = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringAward);
		if (springList != null && !springList.isEmpty()) {
			for (StaticActSpringFestival e : springList) {
				int keyId = e.getKeyId();
				if (!actRecord.getReceived().containsKey(keyId) && after >= e.getCond()) {
					builder.setAwardTips(true);
				}
				if (before < e.getCond() && e.getCond() <= after) {
					activityManager.pushSpringTips();
					break;
				}
			}
		}
		handler.sendMsgToPlayer(DoSpringTurntableRs.ext, builder.build());
	}

	/**
	 * 春节活动-春节活动领奖
	 **/
	public void receiveSpringFestivalRq(ClientHandler handler, ReceiveSpringFestivalRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_SPRING_FESTIVAL);
		if (activityBase == null || activityBase.getStep() != ActivityConst.ACTIVITY_BEGIN) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase.getActivityId());
		if (actRecord == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		List<StaticActSpringFestival> springList = null;
		int limit = 0;
		if (rq.getReceiveType() == 1) {
			springList = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringAward);
			// 全服转盘进度
			limit = activityData.getRecordNum(0);
		} else if (rq.getReceiveType() == 2) {
			springList = staticActivityMgr.getSpringFestivalsByType(actRecord.getAwardId(), SpringType.SpringRecharge);
			// 玩家充值金额
			limit = (int) actRecord.getStatus(0);
		}
		if (springList == null) {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		StaticActSpringFestival spring = springList.stream().filter(e -> e.getKeyId() == rq.getKeyId()).findFirst().orElse(null);
		if (spring == null || actRecord.getReceived().containsKey(rq.getKeyId())) {
			handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}
		if (limit < spring.getCond()) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
			return;
		}
		actRecord.getReceived().put(spring.getKeyId(), GameServer.getInstance().currentDay);
		ReceiveSpringFestivalRs.Builder builder = ReceiveSpringFestivalRs.newBuilder();
		spring.getAwardList().forEach(e -> {
			if (e.size() == 3) {
				Award award = new Award(e.get(0), e.get(1), e.get(2));
				builder.addAward(award.wrapPb());
				playerManager.addAward(player, award, Reason.ACT_SPRING_FESTIVAL);
			}
		});
		for (StaticActSpringFestival e : springList) {
			ActSpringFestival.Builder b = e.wrapBp();
			int received = actRecord.getReceived(e.getKeyId());
			if (e.getCond() == 0 && received != GameServer.getInstance().currentDay) {
				actRecord.getReceived().remove(e.getKeyId());
				received = 0;
			}
			b.setIsAward(received == 0 ? 0 : 1);
			builder.addGoods(b);
		}
		builder.setLuckDrawSpeed(activityData.getRecordNum(0));
		builder.setRechargeAmount((int) actRecord.getStatus(0));
		if (spring.getType() == SpringType.SpringAward) {
			ArrayList<ActSpringFestival.Builder> specialBuilderList = new ArrayList<>();
			for (StaticActSpringFestival e : springList) {
				ActSpringFestival.Builder b = e.wrapBp();
				b.setIsAward(actRecord.getReceived(e.getKeyId()) == 0 ? 0 : 1);
				specialBuilderList.add(b);
			}
			ActSpringFestival.Builder specialBuilder = specialBuilderList.stream().filter(e -> e.getIsAward() == 0).sorted(Comparator.comparing(e -> e.getSortId())).findFirst().orElse(specialBuilderList.get(specialBuilderList.size() - 1));
			builder.setDisplay(specialBuilder);
		}
		handler.sendMsgToPlayer(ReceiveSpringFestivalRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	/**
	 * 春节活动-购买灯笼
	 **/
	public void buyLanternRq(ClientHandler handler, BuyLanternRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, ActivityConst.ACT_SPRING_FESTIVAL);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(actRecord.getActivityId());
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int step = activityBase.getStep();
		if (step != ActivityConst.ACTIVITY_BEGIN) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		int count = rq.getCount();
		int lanternPrice = staticLimitMgr.getNum(SimpleId.LANTERN_PRICE);
		lanternPrice = lanternPrice == 0 ? 288 : lanternPrice;
		lanternPrice = count * lanternPrice;
		if (player.getGold() < lanternPrice) {
			handler.sendErrorMsgToPlayer(GameError.NOT_ENOUGH_GOLD);
			return;
		}
		playerManager.subAward(player, AwardType.GOLD, 0, lanternPrice, Reason.ACT_SPRING_FESTIVAL);
		playerManager.addAward(player, AwardType.PROP, ItemId.LANTERN, count, Reason.ACT_SPRING_FESTIVAL);
		BuyLanternRs.Builder builder = BuyLanternRs.newBuilder();
		builder.setProp(Prop.newBuilder().setPropId(ItemId.LANTERN).setPropNum(player.getItemNum(ItemId.LANTERN)).build());
		builder.setGold(player.getGold());
		handler.sendMsgToPlayer(BuyLanternRs.ext, builder.build());
	}

	/**
	 * 春节特惠(礼包)
	 **/
	public void actSpringGiftRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = activityManager.getActivityBase(ActivityConst.ACT_SPRING_FESTIVAL_GIFT);
		if (activityBase == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, null, activityBase));
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase.getActivityId());
		if (actRecord == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, actRecord, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		Map<Integer, StaticLimitGift> springGiftMap = staticActivityMgr.getSpringGiftMap(actRecord.getAwardId());
		if (springGiftMap == null) {
			ActivityEventManager.getInst().updateActivityHandler(EventEnum.TIME_DISAPPEAR, new CommonTipActor(player, actRecord, activityBase));
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActSpringGiftRs.Builder builder = ActSpringGiftRs.newBuilder();
		for (StaticLimitGift gift : springGiftMap.values()) {
			if (actRecord.getRecordNum(gift.getKeyId()) >= gift.getCount()) {
				continue;
			}
			CommonPb.SuripriseGift.Builder b = CommonPb.SuripriseGift.newBuilder();
			b.setKeyId(gift.getKeyId());
			b.setName(gift.getName());
			b.setGold(gift.getDisplay());
			b.setMoney(gift.getMoney());
			List<List<Integer>> awardList = gift.getAwardList();
			if (awardList != null) {
				awardList.forEach(e -> {
					b.addAward(PbHelper.createAward(e.get(0), e.get(1), e.get(2)));
				});
			}
			b.setCount(gift.getCount());
			b.setBuyCount(actRecord.getRecordNum(gift.getKeyId()));
			b.setAsset(gift.getAsset());
			b.setIcon(gift.getIcon());
			builder.addGifts(b);
		}
		handler.sendMsgToPlayer(ActSpringGiftRs.ext, builder.build());
		ActivityEventManager.getInst().activityTip(player, actRecord, activityBase);
	}

	// 春节活动在活动结束的时候 发放奖励
	private void sendSpringFestival(ActivityBase activityBase, ActivityData activityData) {
		Iterator<Player> iterator = playerManager.getAllPlayer().values().iterator();
		while (iterator.hasNext()) {
			Player next = iterator.next();
			next.getLord().setClothes(0);
			ActRecord actRecord = activityManager.getActivityInfo(next, activityBase);
			if (actRecord == null) {
				continue;
			}
			ArrayList<Award> awards = Lists.newArrayList();
			List<StaticActSpringFestival> springFestivals = staticActivityMgr.getSpringFestivals(actRecord.getAwardId());
			if (springFestivals == null) {
				continue;
			}
			for (StaticActSpringFestival springFestival : springFestivals) {
				if (springFestival.getType() == SpringType.SpringAward) {
					if (activityData.getRecordNum(0) >= springFestival.getCond() && actRecord.getReceived(springFestival.getKeyId()) == 0) {
						springFestival.getAwardList().forEach(e -> {
							if (e.size() == 3) {
								Award award = new Award(e.get(0), e.get(1), e.get(2));
								if (award.getType() == AwardType.PROP && award.getId() == ItemId.LANTERN) {
									playerManager.addAward(next, award, Reason.ACT_SPRING_FESTIVAL);
								} else if (award.getType() != AwardType.ROLE_CLOTHES) {
									awards.add(award);
								}
								actRecord.getReceived().put(springFestival.getKeyId(), GameServer.getInstance().currentDay);
							}
						});
					}
				} else if (springFestival.getType() == SpringType.SpringRecharge) {
					if (springFestival.getCond() != 0 && actRecord.getStatus(0) >= springFestival.getCond() && actRecord.getReceived(springFestival.getKeyId()) == 0) {
						springFestival.getAwardList().forEach(e -> {
							if (e.size() == 3) {
								Award award = new Award(e.get(0), e.get(1), e.get(2));
								if (award.getType() == AwardType.PROP && award.getId() == ItemId.LANTERN) {
									playerManager.addAward(next, award, Reason.ACT_SPRING_FESTIVAL);
								} else if (award.getType() != AwardType.ROLE_CLOTHES) {
									awards.add(award);
								}
								actRecord.getReceived().put(springFestival.getKeyId(), GameServer.getInstance().currentDay);
							}
						});
					}
				}
			}
			List<Integer> addtion = staticLimitMgr.getAddtion(SimpleId.LANTERN_AWARD);
			int lanternNum = next.getItemNum(ItemId.LANTERN);
			if (lanternNum > 0 && addtion != null && addtion.size() == 3) {
				playerManager.subAward(next, AwardType.PROP, ItemId.LANTERN, lanternNum, Reason.ACT_SPRING_FESTIVAL);
				awards.add(new Award(addtion.get(0), addtion.get(1), addtion.get(2) * lanternNum));
			}
			// 已达成条件,未领取奖励,补发奖励邮件
			if (awards.size() > 0) {
				playerManager.sendAttachMail(next, awards, MailId.PLAYER_ACT_MAIL, activityBase.getStaticActivity().getName());
			}
		}
	}

	/**
	 * 获取塔防活动
	 **/
	public void getTDTaskRq(ClientHandler handler) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_TD_SEVEN_TASK);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		Date createDate = player.account.getCreateDate();
		int state = DateHelper.dayiy(createDate, new Date());
		if (state > 7) {// 超过7天不开该活动
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}

		GetTDTaskRs.Builder builder = GetTDTaskRs.newBuilder();
		int integral = actRecord.getRecordNum(0);
		builder.setIntegral(integral);
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		tdTaskManager.getStaticTDSevenBoxAwardMap().values().forEach(e -> {
			builder.addBoxAward(e.warp(actRecord));
		});

		Map<Integer, Map<Integer, StaticTDSevenTask>> tdSevenTaskByType = tdTaskManager.getTdSevenTaskByType();

		tdSevenTaskByType.forEach((k, v) -> {
			if (k == ActTDSevenType.tdTaskType_1) {
				v.values().forEach(e -> {
					ActivityCondState.Builder warp = e.warp(player, actRecord);
					builder.addTask(warp);
				});
				return;
			}

			StaticTDSevenTask staticTDSevenTask = v.values().stream().filter(e -> !actRecord.getReceived().containsKey(e.getTaskId())).sorted(Comparator.comparing(e -> e.getTaskId())).findFirst().orElse(null);
			if (staticTDSevenTask != null) {
				builder.addTask(staticTDSevenTask.warp(player, actRecord));
			} else {
				StaticTDSevenTask value = v.values().stream().sorted(Comparator.comparing(StaticTDSevenTask::getTaskId).reversed()).findFirst().get();
				builder.addTask(value.warp(player, actRecord));
			}

		});

		handler.sendMsgToPlayer(GetTDTaskRs.ext, builder.build());
	}

	/**
	 * 领取塔防活动
	 **/
	public void tdTaskAwardRq(ClientHandler handler, TDTaskAwardRq rq) {
		Player player = playerManager.getPlayer(handler.getRoleId());
		if (player == null) {
			handler.sendErrorMsgToPlayer(GameError.PLAYER_NOT_EXIST);
			return;
		}
		ActivityBase activityBase = staticActivityMgr.getActivityById(ActivityConst.ACT_TD_SEVEN_TASK);
		if (activityBase == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActRecord actRecord = activityManager.getActivityInfo(player, activityBase);
		if (actRecord == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		ActivityData activityData = activityManager.getActivity(actRecord.getActivityId());
		if (activityData == null) {
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		Date createDate = player.account.getCreateDate();
		int state = DateHelper.dayiy(createDate, new Date());
		if (state > 7) {// 超过7天不开该活动
			handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_OPEN);
			return;
		}
		TDTaskManager tdTaskManager = SpringUtil.getBean(TDTaskManager.class);
		int type = rq.getType(); // 1:任务 2:宝箱奖励
		int keyId = rq.getKeyId();
		Map<Integer, StaticTDSevenBoxAward> staticTDSevenBoxAwardMap = tdTaskManager.getStaticTDSevenBoxAwardMap();
		TDTaskAwardRs.Builder builder = TDTaskAwardRs.newBuilder();
		Map<Integer, Integer> received = actRecord.getReceived();
		int currentDay = GameServer.getInstance().currentDay;
		if (received.containsKey(keyId)) {
			handler.sendErrorMsgToPlayer(GameError.AWARD_HAD_GOT);
			return;
		}

		if (type == 2) {
			StaticTDSevenBoxAward staticTDSevenBoxAward = staticTDSevenBoxAwardMap.get(keyId);
			if (staticTDSevenBoxAward == null) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}
			if (actRecord.getRecordNum(0) < staticTDSevenBoxAward.getCond()) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
				return;
			}
			received.put(keyId, currentDay);
			staticTDSevenBoxAward.getAwardList().forEach(e -> {
				if (e.size() == 3) {
					Award award = new Award(e.get(0), e.get(1), e.get(2));
					playerManager.addAward(player, award, Reason.ACT_TD_SEVEN_TASK);
					builder.addAward(award.wrapPb());
				}
			});
		} else if (type == 1) {
			Map<Integer, StaticTDSevenTask> staticTDSevenTaskMap = tdTaskManager.getStaticTDSevenTaskMap();
			StaticTDSevenTask staticTDSevenTask = staticTDSevenTaskMap.get(keyId);
			if (staticTDSevenTaskMap == null) {
				handler.sendErrorMsgToPlayer(GameError.CONFIG_ERROR);
				return;
			}
			if (staticTDSevenTask.getType() == 3 && activityData.getCampMembers(staticTDSevenTask.getParam().get(1)).size() >= staticTDSevenTask.getParam().get(0)) {
				actRecord.putState(staticTDSevenTask.getTaskId(), currentDay);
			}
			if (actRecord.getStatus(keyId) <= 0) {
				handler.sendErrorMsgToPlayer(GameError.ACTIVITY_NOT_FINISH);
				return;
			}
			received.put(keyId, currentDay);
			List<Integer> awardList = staticTDSevenTask.getAwardList();
			actRecord.addRecord(0, awardList.get(2));
			builder.addAward(CommonPb.Award.newBuilder().setType(awardList.get(0)).setId(awardList.get(1)).setCount(awardList.get(2)).build());
		} else {
			handler.sendErrorMsgToPlayer(GameError.PARAM_ERROR);
			return;
		}
		builder.setIntegral(actRecord.getRecordNum(0));
		staticTDSevenBoxAwardMap.values().forEach(e -> {
			builder.addBoxAward(e.warp(actRecord));
		});

		Map<Integer, Map<Integer, StaticTDSevenTask>> tdSevenTaskByType = tdTaskManager.getTdSevenTaskByType();
		tdSevenTaskByType.forEach((k, v) -> {
			if (k == ActTDSevenType.tdTaskType_1) {
				v.values().forEach(e -> {
					ActivityCondState.Builder warp = e.warp(player, actRecord);
					builder.addTask(warp);
				});
				return;
			}
			StaticTDSevenTask staticTDSevenTask = v.values().stream().filter(e -> !actRecord.getReceived().containsKey(e.getTaskId())).sorted(Comparator.comparing(e -> e.getTaskId())).findFirst().orElse(null);
			if (staticTDSevenTask != null) {
				ActivityCondState.Builder warp = staticTDSevenTask.warp(player, actRecord);
				builder.addTask(warp);
			} else {
				StaticTDSevenTask value = v.values().stream().sorted(Comparator.comparing(StaticTDSevenTask::getTaskId).reversed()).findFirst().get();
				builder.addTask(value.warp(player, actRecord));
			}
		});
		handler.sendMsgToPlayer(TDTaskAwardRs.ext, builder.build());

		ActivityEventManager.getInst().activityTip(EventEnum.GET_ACTIVITY_AWARD_TIP, new TdActor(player, actRecord, activityData, activityBase));
	}
}