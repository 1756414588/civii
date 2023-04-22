package com.game.message.pool;

import com.game.flame.handler.*;
import com.game.message.handler.ClientHandler;
import com.game.message.handler.ServerHandler;
import com.game.message.handler.cs.*;
import com.game.message.handler.register.RegisterHandler;
import com.game.message.handler.ss.*;
import com.game.pb.*;
import com.game.pb.BuildingPb.*;
import com.game.pb.ActManoeuvrePb.*;
import com.game.pb.DepotPb.*;
import com.game.pb.EquipPb.*;
import com.game.pb.GmToolPb.*;
import com.game.pb.HeroPb.*;
import com.game.pb.InnerPb.ChannelOfflineRq;
import com.game.pb.InnerPb.PayBackRq;
import com.game.pb.InnerPb.RegisterRq;
import com.game.pb.InnerPb.RegisterRs;
import com.game.pb.InnerPb.UseGiftCodeRs;
import com.game.pb.InnerPb.VerifyRs;
import com.game.pb.MapInfoPb.GetMapNpcRq;
import com.game.pb.MapInfoPb.GetMapNpcRs;
import com.game.pb.MapInfoPb.RobotRepairRq;
import com.game.pb.MapInfoPb.RobotRepairRs;
import com.game.pb.MissionPb.*;
import com.game.pb.PropPb.GetPropBagRq;
import com.game.pb.PropPb.GetPropBagRs;
import com.game.pb.PropPb.UsePropRq;
import com.game.pb.PropPb.UsePropRs;
import com.game.pb.PvpBattlePb.*;
import com.game.pb.RiotPb.RiotWarHelpRq;
import com.game.pb.RiotPb.RiotWarHelpRs;
import com.game.pb.RolePb.*;
import com.game.pb.ShopPb.*;
import com.game.pb.SoldierPb.*;
import com.game.pb.StaffPb.*;
import com.game.util.LogHelper;

import java.util.HashMap;

public class MessagePool {

	private HashMap<Integer, Class<? extends ClientHandler>> clientHandlers = new HashMap<Integer, Class<? extends ClientHandler>>();
	private HashMap<Integer, Class<? extends ServerHandler>> serverHandlers = new HashMap<Integer, Class<? extends ServerHandler>>();
	private HashMap<Integer, Integer> rsMsgCmd = new HashMap<Integer, Integer>();

	// 注意新协议注册之前,一定要加到GameServer里面
	// ex: StaffPb.registerAllExtensions(registry);
	public MessagePool() {
		try {
			// 角色
			registerRole();

			// 背包
			regsiterBag();

			// 英雄
			registerHero();

			// 装备
			resgisterEquip();

			// 士兵
			registerSoldier();

			// 关卡
			registerMission();

			// 建筑
			registerBuilding();

			// 商店
			registerShop();

			// 聚宝盆
			registerDepot();

			// 科技
			registerTech();

			// 任务
			registerTask();

			// Rank
			registerRank();

			// 作坊系统
			resgisterWorkShop();

			// 杀器
			resgisterKillEquip();

			// 世界
			registerWorld();

			// 城墙
			registerWall();

			// 邮件
			registerMail();

			// 国家
			registerCountry();

			// 聊天
			registerChat();

			// 活动
			registerActivity();

			// 血战要塞
			registerPvpBattle();

			// 参谋部
			registerStaff();

			// 城堡
			registerCastle();

			//叛军活动
			registerRebel();

			//支付
			registerPay();

			//美女
			registBeauty();

			//配饰
			registOmament();

			//好友
			registFriend();

			//首杀
			registFirstBlood();

			//征途
			registsJourney();

			//塔防
			registerTD();

			//虫族入侵
			registerRiot();

			//兵书
			registerWarBook();

			//世界宝箱
			registerWorldBox();

			//皮肤
			registerSkin();

			//日常任务
			registerDailyTask();

			//自动杀虫
			registerAuto();

			//圣域争霸
			registerBroodWar();

			// 虫族主宰
			registerZergWar();

			// 沙盘演武
			registerManoeuvree();

			// 渔场
			registerFishing();

			// 战火
			registerFlame();

			// 地图信息
			registerMapInfo();

			// ss

			// 服务器注册
			registerApp();

			// 客户端连接相关
			registerChannel();

			registerAchievement();

			// ss
			registerS(VerifyRs.EXT_FIELD_NUMBER, UserLoginRs.EXT_FIELD_NUMBER, VerifyRsHandler.class);
//			registerS(RegisterRq.EXT_FIELD_NUMBER, 0, RegisterRsHandler.class);
			registerS(PayBackRq.EXT_FIELD_NUMBER, 0, PayBackRqHandler.class);
			registerS(SendToMailRq.EXT_FIELD_NUMBER, 0, GameMailRqHandler.class);
			registerS(PersonMailRq.EXT_FIELD_NUMBER, 0, PersonMailRqHandler.class);
			registerS(ReplyPersonMailRq.EXT_FIELD_NUMBER, 0, ReplyPersonMailRqHandler.class);
			registerS(GameMailRq.EXT_FIELD_NUMBER, 0, GameMailRqHandler.class);
			registerS(ForbiddenRq.EXT_FIELD_NUMBER, 0, ForbiddenRqHandler.class);
			registerS(NoticeRq.EXT_FIELD_NUMBER, 0, NoticeRqHandler.class);
			registerS(ModVipRq.EXT_FIELD_NUMBER, 0, ModVipRqHandler.class);
			registerS(AddItemRq.EXT_FIELD_NUMBER, 0, AddItemRqHandler.class);
			registerS(FakeRechargeRq.EXT_FIELD_NUMBER, 0, FakeRechargeRqHandler.class);
			registerS(GmToolPb.TaskJumpRq.EXT_FIELD_NUMBER, 0, TaskJumpRqHandler.class);
			registerS(GetOnlinesRq.EXT_FIELD_NUMBER, 0, GetOnlinesHandler.class);
			registerS(GetPersonRq.EXT_FIELD_NUMBER, 0, GetPersonRqHandler.class);

			registerS(UseGiftCodeRs.EXT_FIELD_NUMBER, GiftCodeRs.EXT_FIELD_NUMBER, UseGiftCodeRsHandler.class);
		} catch (Exception e) {
			LogHelper.ERROR_LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * 圣域争霸
	 */
	private void registerBroodWar() {
		registerC(BroodWarPb.BroodWarInitRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarInitRs.EXT_FIELD_NUMBER, BroodWarInitHandler.class);
		registerC(BroodWarPb.BuyBuffRq.EXT_FIELD_NUMBER, BroodWarPb.BuyBuffRs.EXT_FIELD_NUMBER, BuyBuffHandler.class);
		registerC(BroodWarPb.AttackBroodRq.EXT_FIELD_NUMBER, BroodWarPb.AttackBroodRs.EXT_FIELD_NUMBER, AttackBroodHandler.class);
		registerC(BroodWarPb.FightNowRq.EXT_FIELD_NUMBER, BroodWarPb.FightNowRs.EXT_FIELD_NUMBER, FightNowHandler.class);
		registerC(BroodWarPb.BroodWarRecordRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarRecordRs.EXT_FIELD_NUMBER, BroodWarReportHandler.class);
		registerC(BroodWarPb.BroodWarPlayBackRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarPlayBackRs.EXT_FIELD_NUMBER, BroodWarPlayBackHandler.class);
		registerC(BroodWarPb.AppointInfoRq.EXT_FIELD_NUMBER, BroodWarPb.AppointInfoRs.EXT_FIELD_NUMBER, AppointInfoHandler.class);
		registerC(BroodWarPb.BroodWarScoreRankRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarScoreRankRs.EXT_FIELD_NUMBER, BroodWarScoreRankHandler.class);
		registerC(BroodWarPb.BroodWarAppointRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarAppointRs.EXT_FIELD_NUMBER, BroodWarAppointHandler.class);
		registerC(BroodWarPb.BroodWarHOFRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarHOFRs.EXT_FIELD_NUMBER, BroodWarHOFHandler.class);
		registerC(BroodWarPb.BroodWarRelieveRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarRelieveRs.EXT_FIELD_NUMBER, BroodWarReliveHandler.class);
		registerC(BroodWarPb.BroodWarIntegralRankRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarIntegralRankRs.EXT_FIELD_NUMBER, BroodWarIntegralHandler.class);
		registerC(BroodWarPb.BroodWarSoldierRq.EXT_FIELD_NUMBER, BroodWarPb.BroodWarSoldierRs.EXT_FIELD_NUMBER, BroodWarSoliderHandler.class);
		registerC(BroodWarPb.BuyBroodShopRq.EXT_FIELD_NUMBER, BroodWarPb.BuyBroodShopRs.EXT_FIELD_NUMBER, BuyBroodShopHandler.class);
	}

	/**
	 * 虫族主宰
	 */
	private void registerZergWar() {
		registerC(ZergPb.GetZergRq.EXT_FIELD_NUMBER, ZergPb.GetZergRs.EXT_FIELD_NUMBER, GetZergHandler.class);
		registerC(ZergPb.AttackZergRq.EXT_FIELD_NUMBER, ZergPb.AttackZergRs.EXT_FIELD_NUMBER, AttackZergHandler.class);
		registerC(ZergPb.AttendZergCityRq.EXT_FIELD_NUMBER, ZergPb.AttendZergCityRs.EXT_FIELD_NUMBER, AttendZergCityHandler.class);
		registerC(ZergPb.ZergWarHelpRq.EXT_FIELD_NUMBER, ZergPb.ZergWarHelpRs.EXT_FIELD_NUMBER, ZergWarHelpHandler.class);
		registerC(ZergPb.GetZergShopRq.EXT_FIELD_NUMBER, ZergPb.GetZergShopRs.EXT_FIELD_NUMBER, GetZergShopHandler.class);
		registerC(ZergPb.ZergBuyShopRq.EXT_FIELD_NUMBER, ZergPb.ZergBuyShopRs.EXT_FIELD_NUMBER, ZergBuyShopHandler.class);
	}

	/**
	 * 沙盘演武
	 */
	private void registerManoeuvree() {
		registerC(GetActManoeuvreRq.EXT_FIELD_NUMBER, GetActManoeuvreRs.EXT_FIELD_NUMBER, GetActManoeuvreHandler.class);
		registerC(ActManoeuvreSignUpRq.EXT_FIELD_NUMBER, ActManoeuvreSignUpRs.EXT_FIELD_NUMBER, ActManoeuvreSignUpHandler.class);
		registerC(ActManoeuvreApplyLineRq.EXT_FIELD_NUMBER, ActManoeuvreApplyLineRs.EXT_FIELD_NUMBER, ActManoeuvreApplyLineHandler.class);
		registerC(ActManoeuvreArmyRq.EXT_FIELD_NUMBER, ActManoeuvreArmyRs.EXT_FIELD_NUMBER, ActManoeuvreArmyHandler.class);
		registerC(ActManoeuvreChangeLineRq.EXT_FIELD_NUMBER, ActManoeuvreChangeLineRs.EXT_FIELD_NUMBER, ActManoeuvreChangeLineHandler.class);
		registerC(ActManoeuvreCourseRq.EXT_FIELD_NUMBER, ActManoeuvreCourseRs.EXT_FIELD_NUMBER, ActManoeuvreCourseHandler.class);
		registerC(ActManoeuvreRecordRq.EXT_FIELD_NUMBER, ActManoeuvreRecordRs.EXT_FIELD_NUMBER, ActManoeuvreRecordHandler.class);
		registerC(ActManoeuvreDetailRq.EXT_FIELD_NUMBER, ActManoeuvreDetailRs.EXT_FIELD_NUMBER, ActManoeuvreDetailHandler.class);
		registerC(GetActManoeuvreShopRq.EXT_FIELD_NUMBER, GetActManoeuvreShopRs.EXT_FIELD_NUMBER, GetActManoeuvreShopHandler.class);
		registerC(ActManoeuvreBuyShopRq.EXT_FIELD_NUMBER, ActManoeuvreBuyShopRs.EXT_FIELD_NUMBER, ActManoeuvreBuyShopHandler.class);
	}


	//战火燎原
	private void registerFlame() {
		registerC(FlameWarPb.FlameWarInitRq.EXT_FIELD_NUMBER, FlameWarPb.FlameWarInitRs.EXT_FIELD_NUMBER, FlameWarInitHandler.class);// 初始界面
		registerC(FlameWarPb.FlameBuyBuffRq.EXT_FIELD_NUMBER, FlameWarPb.FlameBuyBuffRs.EXT_FIELD_NUMBER, FlameWarBuyBuffHandler.class);// 购买buff
		registerC(FlameWarPb.ShopExchangeRq.EXT_FIELD_NUMBER, FlameWarPb.ShopExchangeRs.EXT_FIELD_NUMBER, FlameExchangeShopHanler.class);// 兑换物品
		registerC(FlameWarPb.OpenFlameMapRq.EXT_FIELD_NUMBER, FlameWarPb.OpenFlameMapRs.EXT_FIELD_NUMBER, FlameEnterMapHandler.class);// 进入活动
		registerC(FlameWarPb.AttackFlameRq.EXT_FIELD_NUMBER, FlameWarPb.AttackFlameRs.EXT_FIELD_NUMBER, FlameAttackPosHandler.class);// 攻击
		registerC(FlameWarPb.ReceiveBuildAwardRq.EXT_FIELD_NUMBER, FlameWarPb.ReceiveBuildAwardRs.EXT_FIELD_NUMBER, FlameReceiveHandler.class);// 领取奖励
		registerC(FlameWarPb.FlameBagRq.EXT_FIELD_NUMBER, FlameWarPb.FlameBagRs.EXT_FIELD_NUMBER, FlameBagHandler.class);// 打开背包
		registerC(FlameWarPb.FlameMapRq.EXT_FIELD_NUMBER, FlameWarPb.FlameMapRs.EXT_FIELD_NUMBER, FlameLoadMapHandler.class);// 拉地图实体
		registerC(FlameWarPb.FlameLogOutRq.EXT_FIELD_NUMBER, FlameWarPb.FlameLogOutRs.EXT_FIELD_NUMBER, FlameLogOutHandler.class);// 退出活动
		registerC(FlameWarPb.FlameBuildInfoRq.EXT_FIELD_NUMBER, FlameWarPb.FlameBuildInfoRs.EXT_FIELD_NUMBER, FlameBuildInfoHandler.class);// 查看建组详情
		registerC(FlameWarPb.FlameLoadAllBuildRq.EXT_FIELD_NUMBER, FlameWarPb.FlameLoadAllBuildRs.EXT_FIELD_NUMBER, FlameLoadAllBuildHandler.class);// 拉取所有建筑信息
		registerC(FlameWarPb.FlameResInfoRq.EXT_FIELD_NUMBER, FlameWarPb.FlameResInfoRs.EXT_FIELD_NUMBER, FlameResInfoHandler.class);// 查看采集点详情
		registerC(FlameWarPb.FlameFightHelpRq.EXT_FIELD_NUMBER, FlameWarPb.FlameFightHelpRs.EXT_FIELD_NUMBER, FlameFightHelpHandler.class);// 战争支援
		registerC(FlameWarPb.FlameRealWarInfoRq.EXT_FIELD_NUMBER, FlameWarPb.FlameRealWarInfoRs.EXT_FIELD_NUMBER, FlameRealWarInfoHandler.class);// 实时战况
		registerC(FlameWarPb.FlameRankRq.EXT_FIELD_NUMBER, FlameWarPb.FlameRankRs.EXT_FIELD_NUMBER, FlameRankHandler.class);//排行榜
		registerC(FlameWarPb.FlameResRankRq.EXT_FIELD_NUMBER, FlameWarPb.FlameResRankRs.EXT_FIELD_NUMBER, FlameResRankHandler.class);//采集点详情
	}

	private void registerSkin() {
		registerC(SkinPb.GetCommandSkinRq.EXT_FIELD_NUMBER, SkinPb.GetCommandSkinRs.EXT_FIELD_NUMBER, GetCommandSkinHandler.class);
		registerC(SkinPb.ChangeCommandSkinRq.EXT_FIELD_NUMBER, SkinPb.ChangeCommandSkinRs.EXT_FIELD_NUMBER, ChangeCommandSkinHandler.class);
		registerC(SkinPb.UpCommandSkinLevRq.EXT_FIELD_NUMBER, SkinPb.UpCommandSkinLevRs.EXT_FIELD_NUMBER, UpCommandSkinLevHandler.class);
	}

	private void registerDailyTask() {
		registerC(DailyTaskPb.DailyTaskRq.EXT_FIELD_NUMBER, DailyTaskPb.DailyTaskRs.EXT_FIELD_NUMBER, DailyTaskHandler.class);
		registerC(DailyTaskPb.DailyTaskCompleteRq.EXT_FIELD_NUMBER, DailyTaskPb.DailyTaskCompleteRs.EXT_FIELD_NUMBER, DailyTaskCompleteHandler.class);
		registerC(DailyTaskPb.DailyActiveAwardRq.EXT_FIELD_NUMBER, DailyTaskPb.DailyActiveAwardRs.EXT_FIELD_NUMBER, DailyActiveAwardHandler.class);
	}


	private void registsJourney() {
		registerC(JourneyPb.GetAllJourneyRq.EXT_FIELD_NUMBER, JourneyPb.GetAllJourneyRs.EXT_FIELD_NUMBER, GetAllJourneyHandler.class);
		registerC(JourneyPb.JourneyDoneRq.EXT_FIELD_NUMBER, JourneyPb.JourneyDoneRs.EXT_FIELD_NUMBER, JourneyDoneHandler.class);
		registerC(JourneyPb.SweepJourneyRq.EXT_FIELD_NUMBER, JourneyPb.SweepJourneyRs.EXT_FIELD_NUMBER, SweepJourneyHandler.class);
		registerC(JourneyPb.BuyJourneyTimesRq.EXT_FIELD_NUMBER, JourneyPb.BuyJourneyTimesRs.EXT_FIELD_NUMBER, BuyJourneyTimesHandler.class);
	}

	private void registerWarBook() {
		registerC(WarBookPb.GetWarBookBagRq.EXT_FIELD_NUMBER, WarBookPb.GetWarBookBagRs.EXT_FIELD_NUMBER, GetWarBookBagHandler.class);
		registerC(WarBookPb.LockWarBookRq.EXT_FIELD_NUMBER, WarBookPb.LockWarBookRs.EXT_FIELD_NUMBER, LockWarBookHandler.class);
		registerC(WarBookPb.WearWarBookRq.EXT_FIELD_NUMBER, WarBookPb.WearWarBookRs.EXT_FIELD_NUMBER, WearWarBookHandler.class);
		registerC(WarBookPb.DecompoundWarBookRq.EXT_FIELD_NUMBER, WarBookPb.DecompoundWarBookRs.EXT_FIELD_NUMBER, DecompoundWarBookHandler.class);
		registerC(WarBookPb.TakeOffWarBookRq.EXT_FIELD_NUMBER, WarBookPb.TakeOffWarBookRs.EXT_FIELD_NUMBER, TakeOffWarBookHandler.class);
		registerC(WarBookPb.StrongWarBookRq.EXT_FIELD_NUMBER, WarBookPb.StrongWarBookRs.EXT_FIELD_NUMBER, StrongWarBookHandler.class);
		registerC(WarBookPb.GetWarBookShopRq.EXT_FIELD_NUMBER, WarBookPb.GetWarBookShopRs.EXT_FIELD_NUMBER, GetWarBookShopHandler.class);
		registerC(WarBookPb.GetWarBookExchangeRq.EXT_FIELD_NUMBER, WarBookPb.GetWarBookExchangeRs.EXT_FIELD_NUMBER, GetWarBookExchangeHandler.class);
		registerC(WarBookPb.DoWarBookShopRq.EXT_FIELD_NUMBER, WarBookPb.DoWarBookShopRs.EXT_FIELD_NUMBER, DoWarBookShopHandler.class);
		registerC(WarBookPb.RefreshWarBookShopRq.EXT_FIELD_NUMBER, WarBookPb.RefreshWarBookShopRs.EXT_FIELD_NUMBER, RefreshWarBookShopHandler.class);
		registerC(WarBookPb.DoWarBookExchangeRq.EXT_FIELD_NUMBER, WarBookPb.DoWarBookExchangeRs.EXT_FIELD_NUMBER, DoWarBookExchangeHandler.class);
	}

	private void registOmament() {
		registerC(OmamentPb.GetOmamentBagRq.EXT_FIELD_NUMBER, OmamentPb.GetOmamentBagRs.EXT_FIELD_NUMBER, GetOmamentBagHandler.class);
		registerC(OmamentPb.GetOmamentDeressRq.EXT_FIELD_NUMBER, OmamentPb.GetOmamentDeressRs.EXT_FIELD_NUMBER, GetOmamentDeressHandler.class);
		registerC(OmamentPb.WearOmamentRq.EXT_FIELD_NUMBER, OmamentPb.WearOmamentRs.EXT_FIELD_NUMBER, WearOmamentHandler.class);
		registerC(OmamentPb.BaublesCompoundOmamentRq.EXT_FIELD_NUMBER, OmamentPb.BaublesCompoundOmamentRs.EXT_FIELD_NUMBER, BaublesCompoundOmamentHandler.class);
		registerC(OmamentPb.CompoundOmamentRq.EXT_FIELD_NUMBER, OmamentPb.CompoundOmamentRs.EXT_FIELD_NUMBER, CompoundOmamentHandler.class);
	}

	private void registerPay() {
		registerC(PayPb.GetOrderNumRq.EXT_FIELD_NUMBER, PayPb.GetOrderNumRs.EXT_FIELD_NUMBER, GetOrderNumHandler.class);
		registerC(PayPb.GetRechargeRq.EXT_FIELD_NUMBER, PayPb.GetRechargeRs.EXT_FIELD_NUMBER, GetRechargeHandler.class);
	}

	//美女
	private void registBeauty() {
		//新美女
		registerC(BeautyPb.GetNewBeautySkillRq.EXT_FIELD_NUMBER, BeautyPb.GetNewBeautySkillRs.EXT_FIELD_NUMBER, GetNewBeautySkillHandler.class);
		registerC(BeautyPb.UpNewBeautySkillRq.EXT_FIELD_NUMBER, BeautyPb.UpNewBeautySkillRs.EXT_FIELD_NUMBER, UpBeautySkillHandler.class);
		registerC(BeautyPb.UnlockingBeautyRq.EXT_FIELD_NUMBER, BeautyPb.UnlockingBeautyRs.EXT_FIELD_NUMBER, UnlockingBeautyHandler.class);
		registerC(BeautyPb.NewGetBeautyListRq.EXT_FIELD_NUMBER, BeautyPb.NewGetBeautyListRs.EXT_FIELD_NUMBER, GetBeautyListHanlder.class);
		registerC(BeautyPb.NewPlaySGameRq.EXT_FIELD_NUMBER, BeautyPb.NewPlaySGameRs.EXT_FIELD_NUMBER, PlaySGameHandler.class);
		registerC(BeautyPb.NewPlaySeekingRq.EXT_FIELD_NUMBER, BeautyPb.NewPlaySeekingRs.EXT_FIELD_NUMBER, PlaySeekingHandler.class);
		registerC(BeautyPb.NewPlayBeautyGiftRq.EXT_FIELD_NUMBER, BeautyPb.NewPlayBeautyGiftRs.EXT_FIELD_NUMBER, PlayBeautyGiftHandler.class);
		registerC(BeautyPb.ClickBeautyRq.EXT_FIELD_NUMBER, BeautyPb.ClickBeautyRs.EXT_FIELD_NUMBER, ClickBeautyHandler.class);
	}

	private void registerTD() {
		registerC(TDPb.TDMapInitRq.EXT_FIELD_NUMBER, TDPb.TDMapInitRs.EXT_FIELD_NUMBER, TDMapInitHandler.class);
		registerC(TDPb.TowerWarRq.EXT_FIELD_NUMBER, TDPb.TowerWarRs.EXT_FIELD_NUMBER, TowerWarDetailHandler.class);
		registerC(TDPb.TowerWarReportRq.EXT_FIELD_NUMBER, TDPb.TowerWarReportRs.EXT_FIELD_NUMBER, TowerWarReportHandler.class);
		registerC(TDPb.TowerRewardRq.EXT_FIELD_NUMBER, TDPb.TowerRewardRs.EXT_FIELD_NUMBER, TowerRewardHandler.class);
		registerC(TDPb.TDTowerInitRq.EXT_FIELD_NUMBER, TDPb.TDTowerInitRs.EXT_FIELD_NUMBER, TDTowerInitHandler.class);
		registerC(TDPb.TDBounsRq.EXT_FIELD_NUMBER, TDPb.TDBounsRs.EXT_FIELD_NUMBER, TDBoundsInitHandler.class);
		registerC(TDPb.EndlessTowerDefenseInitRq.EXT_FIELD_NUMBER, TDPb.EndlessTowerDefenseInitRs.EXT_FIELD_NUMBER, EndlessTowerDefenseInitHandler.class);
		registerC(TDPb.EndlessTDRankRq.EXT_FIELD_NUMBER, TDPb.EndlessTDRankRs.EXT_FIELD_NUMBER, EndlessTDRankHandler.class);
		registerC(TDPb.ConvertShopRq.EXT_FIELD_NUMBER, TDPb.ConvertShopRs.EXT_FIELD_NUMBER, ConvertShopHandler.class);
		registerC(TDPb.BuyConvertShopRq.EXT_FIELD_NUMBER, TDPb.BuyConvertShopRs.EXT_FIELD_NUMBER, BuyConvertShopHandler.class);
		registerC(TDPb.BattleShopRq.EXT_FIELD_NUMBER, TDPb.BattleShopRs.EXT_FIELD_NUMBER, BattleShopHandler.class);
		registerC(TDPb.BuyBattleShopRq.EXT_FIELD_NUMBER, TDPb.BuyBattleShopRs.EXT_FIELD_NUMBER, BuyBattleShopHandler.class);
		registerC(TDPb.QuartermasterWarehouseRq.EXT_FIELD_NUMBER, TDPb.QuartermasterWarehouseRs.EXT_FIELD_NUMBER, QuartermasterWarehouseHandler.class);
		registerC(TDPb.RefreshBattleShopRq.EXT_FIELD_NUMBER, TDPb.RefreshBattleShopRs.EXT_FIELD_NUMBER, RefreshBattleShopHandler.class);
		registerC(TDPb.ReceiveRankAwardRq.EXT_FIELD_NUMBER, TDPb.ReceiveRankAwardRs.EXT_FIELD_NUMBER, ReceiveRankAwardHandler.class);
		registerC(TDPb.FightAutoRq.EXT_FIELD_NUMBER, TDPb.FightAutoRs.EXT_FIELD_NUMBER, FightAutoHandler.class);
		registerC(TDPb.EndlessTDOverRq.EXT_FIELD_NUMBER, TDPb.EndlessTDOverRs.EXT_FIELD_NUMBER, EndlessTDOverHandler.class);
		registerC(TDPb.PlayEndlessTDRq.EXT_FIELD_NUMBER, TDPb.PlayEndlessTDRs.EXT_FIELD_NUMBER, PlayEndlessTDHandler.class);
		registerC(TDPb.EndlessTDReportRq.EXT_FIELD_NUMBER, TDPb.EndlessTDReportRs.EXT_FIELD_NUMBER, EndlessTDReportHandler.class);
		registerC(TDPb.SelectEndlessTDProRq.EXT_FIELD_NUMBER, TDPb.SelectEndlessTDProRs.EXT_FIELD_NUMBER, SelectEndlessTDProHandler.class);
		registerC(TDPb.UseEndlessTDProRq.EXT_FIELD_NUMBER, TDPb.UseEndlessTDProRs.EXT_FIELD_NUMBER, UseEndlessTDProHandler.class);
		registerC(TDPb.EndlessTDTowerInitRq.EXT_FIELD_NUMBER, TDPb.EndlessTDTowerInitRs.EXT_FIELD_NUMBER, EndlessTDTowerInitHandler.class);

		registerC(TDPb.BulletWarInfoRq.EXT_FIELD_NUMBER, TDPb.BulletWarInfoRs.EXT_FIELD_NUMBER, BulletWarInfoHandler.class);
		registerC(TDPb.BulletWarLevelAwardRq.EXT_FIELD_NUMBER, TDPb.BulletWarLevelAwardRs.EXT_FIELD_NUMBER, BulletWarAwardHandler.class);
	}

	private void registerWorldBox() {
		registerC(WorldBoxPb.GetWorldBoxRq.EXT_FIELD_NUMBER, WorldBoxPb.GetWorldBoxRs.EXT_FIELD_NUMBER, GetWorldBoxHandler.class);
		registerC(WorldBoxPb.ReceiveWorldBoxRq.EXT_FIELD_NUMBER, WorldBoxPb.ReceiveWorldBoxRs.EXT_FIELD_NUMBER, ReceiveWorldBoxHandler.class);
		registerC(WorldBoxPb.OpenWorldBoxRq.EXT_FIELD_NUMBER, WorldBoxPb.OpenWorldBoxRs.EXT_FIELD_NUMBER, OpenWorldBoxHandler.class);
		registerC(WorldBoxPb.TopWolrdBoxRq.EXT_FIELD_NUMBER, WorldBoxPb.TopWolrdBoxRs.EXT_FIELD_NUMBER, TopWolrdBoxHandler.class);
		registerC(WorldBoxPb.DropWolrdBoxRq.EXT_FIELD_NUMBER, WorldBoxPb.DropWolrdBoxRs.EXT_FIELD_NUMBER, DropWolrdBoxHandler.class);
	}

	/**
	 * 自动杀虫
	 */
	private void registerAuto() {
		registerC(WorldPb.AutoKillMonsterRq.EXT_FIELD_NUMBER, WorldPb.AutoKillMonsterRs.EXT_FIELD_NUMBER, AutoKillMonsterHandler.class);
		registerC(WorldPb.AutoStartKillMonsterRq.EXT_FIELD_NUMBER, WorldPb.AutoStartKillMonsterRs.EXT_FIELD_NUMBER, AutoStartKillMonsterHandler.class);
		registerC(WorldPb.AutoKillMonsterRewardRq.EXT_FIELD_NUMBER, WorldPb.AutoKillMonsterRewardRs.EXT_FIELD_NUMBER, AutoKillMonsterRewardHandler.class);
		registerC(WorldPb.AddKillMonsterSoldierRq.EXT_FIELD_NUMBER, WorldPb.AddKillMonsterSoldierRs.EXT_FIELD_NUMBER, AddKillMonsterSoldierHandler.class);
	}

	public void registerC(int id, int rsCmd, Class<? extends ClientHandler> handlerClass) {
		if (handlerClass != null) {
			clientHandlers.put(id, handlerClass);
			if (rsMsgCmd.containsKey(id)) {
				LogHelper.CONFIG_LOGGER.info("register command wrong, id = " + id + ", rsCmd = " + rsCmd);
			}
			rsMsgCmd.put(id, rsCmd);
		}
	}

	private void registerS(int id, int rsCmd, Class<? extends ServerHandler> handlerClass) {
		if (handlerClass != null) {
			serverHandlers.put(id, handlerClass);
			rsMsgCmd.put(id, rsCmd);
		}
	}

	public ClientHandler getClientHandler(int id) throws InstantiationException, IllegalAccessException {
		if (!clientHandlers.containsKey(id)) {
			return null;
		} else {
			ClientHandler handler = clientHandlers.get(id).newInstance();
			int responseId = rsMsgCmd.get(id);
			handler.setRsMsgCmd(responseId);
			return handler;
		}
	}

	public ServerHandler getServerHandler(int id) throws InstantiationException, IllegalAccessException {
		if (!serverHandlers.containsKey(id)) {
			return null;
		} else {
			ServerHandler handler = serverHandlers.get(id).newInstance();
			handler.setRsMsgCmd(rsMsgCmd.get(id));
			return handler;
		}
	}

	// 角色
	public void registerRole() {
		registerC(UserLoginRq.EXT_FIELD_NUMBER, 0, BeginGameHandler.class);
		registerC(CreateRoleRq.EXT_FIELD_NUMBER, CreateRoleRs.EXT_FIELD_NUMBER, CreateRoleHanlder.class);
		registerC(RoleLoginRq.EXT_FIELD_NUMBER, RoleLoginRs.EXT_FIELD_NUMBER, RoleLoginHandler.class);
		registerC(RolePb.GetTimeRq.EXT_FIELD_NUMBER, RolePb.GetTimeRs.EXT_FIELD_NUMBER, GetTimeHandler.class);
		registerC(RolePb.RoleReloginRq.EXT_FIELD_NUMBER, RolePb.RoleReloginRs.EXT_FIELD_NUMBER, RoleReloginHandler.class);
		registerC(RolePb.RefreshDataRq.EXT_FIELD_NUMBER, RolePb.RefreshDataRs.EXT_FIELD_NUMBER, RefreshDataRqHandler.class);
		registerC(RolePb.BuyEnergyRq.EXT_FIELD_NUMBER, RolePb.BuyEnergyRs.EXT_FIELD_NUMBER, BuyEnergyHandler.class);
		registerC(RolePb.SetPortraitRq.EXT_FIELD_NUMBER, RolePb.SetPortraitRs.EXT_FIELD_NUMBER, SetPortraityHandler.class);
		registerC(RolePb.NewStateRq.EXT_FIELD_NUMBER, RolePb.NewStateRs.EXT_FIELD_NUMBER, NewStateHandler.class);
		registerC(RolePb.OpenAutoRq.EXT_FIELD_NUMBER, RolePb.OpenAutoRs.EXT_FIELD_NUMBER, OpenAutoHandler.class);
		registerC(RolePb.GetLevelAwardRq.EXT_FIELD_NUMBER, RolePb.GetLevelAwardRs.EXT_FIELD_NUMBER, GetLevelAwardHandler.class);
		registerC(RolePb.GiftCodeRq.EXT_FIELD_NUMBER, RolePb.GiftCodeRs.EXT_FIELD_NUMBER, GiftCodeHandler.class);
		registerC(RolePb.AutoAddSoldierRq.EXT_FIELD_NUMBER, RolePb.AutoAddSoldierRs.EXT_FIELD_NUMBER, AutoAddSoldierHandler.class);
		registerC(RolePb.NewChangeNameRq.EXT_FIELD_NUMBER, RolePb.NewChangeNameRs.EXT_FIELD_NUMBER, NewChangeNameHandler.class);
		registerC(RolePb.UpdateGuideRq.EXT_FIELD_NUMBER, RolePb.UpdateGuideRs.EXT_FIELD_NUMBER, UpdateGuideHandler.class);
		registerC(RolePb.UseCdkRq.EXT_FIELD_NUMBER, RolePb.UseCdkRs.EXT_FIELD_NUMBER, UseCdkHandler.class);
		registerC(RolePb.GetPeopleRq.EXT_FIELD_NUMBER, RolePb.GetPeopleRs.EXT_FIELD_NUMBER, GetPepoleHandler.class);
		registerC(RolePb.getFrameRq.EXT_FIELD_NUMBER, RolePb.getFrameRs.EXT_FIELD_NUMBER, GetFrameHandler.class);
		registerC(RolePb.setFrameRq.EXT_FIELD_NUMBER, RolePb.setFrameRs.EXT_FIELD_NUMBER, SetFrameHandler.class);
		registerC(RolePb.enterFrameRq.EXT_FIELD_NUMBER, RolePb.enterFrameRs.EXT_FIELD_NUMBER, EnterFrameHandler.class);
		registerC(RolePb.EnterGameRq.EXT_FIELD_NUMBER, RolePb.EnterGameRs.EXT_FIELD_NUMBER, EnterGameHandler.class);
		registerC(RolePb.GuilderRq.EXT_FIELD_NUMBER, RolePb.GuilderRs.EXT_FIELD_NUMBER, GuilderHandler.class);//网易顿
	}

	// 背包
	public void regsiterBag() {
		registerC(GetPropBagRq.EXT_FIELD_NUMBER, GetPropBagRs.EXT_FIELD_NUMBER, GetItemBagHandler.class);
		registerC(UsePropRq.EXT_FIELD_NUMBER, UsePropRs.EXT_FIELD_NUMBER, UseItemHandler.class);
		registerC(PropPb.SellPropRq.EXT_FIELD_NUMBER, PropPb.SellPropRs.EXT_FIELD_NUMBER, SellPropHandler.class);
		registerC(PropPb.ChangeLordNameRq.EXT_FIELD_NUMBER, PropPb.ChangeLordNameRs.EXT_FIELD_NUMBER, ChangeLordNameHandler.class);
	}

	// 英雄
	public void registerHero() {
		registerC(GetHeroRq.EXT_FIELD_NUMBER, GetHeroRs.EXT_FIELD_NUMBER, GetHeroHandler.class);
		registerC(GetEmbattleInfoRq.EXT_FIELD_NUMBER, GetEmbattleInfoRs.EXT_FIELD_NUMBER, GetEmbattleInfoHandler.class);
		registerC(EmbattleHeroRq.EXT_FIELD_NUMBER, EmbattleHeroRs.EXT_FIELD_NUMBER, EmbattleHeroHandler.class);
		registerC(WashHeroRq.EXT_FIELD_NUMBER, WashHeroRs.EXT_FIELD_NUMBER, WashHeroHandler.class);
		registerC(HeroPb.LootHeroRq.EXT_FIELD_NUMBER, HeroPb.LootHeroRs.EXT_FIELD_NUMBER, LootHeroHandler.class);
		registerC(HeroPb.AdvanceHeroRq.EXT_FIELD_NUMBER, HeroPb.AdvanceHeroRs.EXT_FIELD_NUMBER, AdvanceHeroHandler.class);
		registerC(HeroPb.LootOpenRq.EXT_FIELD_NUMBER, HeroPb.LootOpenRs.EXT_FIELD_NUMBER, LootOpenandler.class);
		registerC(HeroPb.DivineAdvanceRq.EXT_FIELD_NUMBER, HeroPb.DivineAdvanceRs.EXT_FIELD_NUMBER, DivineAdvanceHandler.class);
		registerC(HeroPb.LookHeroRq.EXT_FIELD_NUMBER, HeroPb.LookHeroRs.EXT_FIELD_NUMBER, LookHeroHandler.class);
		registerC(HeroPb.TelnetHeroRq.EXT_FIELD_NUMBER, HeroPb.TelnetHeroRs.EXT_FIELD_NUMBER, TalnetHeroHandler.class);// 天赋

	}

	// 装备
	public void resgisterEquip() {
		registerC(GetEquipBagRq.EXT_FIELD_NUMBER, GetEquipBagRs.EXT_FIELD_NUMBER, GetEquipBagHandler.class);
		registerC(DecompoundEquipRq.EXT_FIELD_NUMBER, DecompoundEquipRs.EXT_FIELD_NUMBER, DecompoundEquipHandler.class);
		registerC(BuyEquipSlotRq.EXT_FIELD_NUMBER, BuyEquipSlotRs.EXT_FIELD_NUMBER, BuyEquipSlotHandler.class);
		registerC(WearEquipRq.EXT_FIELD_NUMBER, WearEquipRs.EXT_FIELD_NUMBER, WearEquipHandler.class);
		registerC(TakeOffEquipRq.EXT_FIELD_NUMBER, TakeOffEquipRs.EXT_FIELD_NUMBER, TakeOffEquipHandler.class);
		registerC(WashHeroEquipRq.EXT_FIELD_NUMBER, WashHeroEquipRs.EXT_FIELD_NUMBER, WashHeroEquipHandler.class);
		registerC(WashEquipItemRq.EXT_FIELD_NUMBER, WashEquipItemRs.EXT_FIELD_NUMBER, WashEquipItemHandler.class);
		registerC(HireBlackSmithRq.EXT_FIELD_NUMBER, HireBlackSmithRs.EXT_FIELD_NUMBER, HireBlackSmithHandler.class);
		registerC(EquipPb.CompoundEquipRq.EXT_FIELD_NUMBER, EquipPb.CompoundEquipRs.EXT_FIELD_NUMBER, CompoundEquipItemHandler.class);
		registerC(EquipPb.BlackSmithFreeCdRq.EXT_FIELD_NUMBER, EquipPb.BlackSmithFreeCdRs.EXT_FIELD_NUMBER, BlackSmithFreeCdHandler.class);
		registerC(EquipPb.DoneEquipRq.EXT_FIELD_NUMBER, EquipPb.DoneEquipRs.EXT_FIELD_NUMBER, DoneEquipHandler.class);
	}

	// 士兵
	public void registerSoldier() {
		registerC(GetSoldierRq.EXT_FIELD_NUMBER, GetSoldierRs.EXT_FIELD_NUMBER, GetSoldierHandler.class);
		registerC(LargerBarracksRq.EXT_FIELD_NUMBER, LargerBarracksRs.EXT_FIELD_NUMBER, LargerBarracksHandler.class);
		registerC(LevelupRecruitTimeRq.EXT_FIELD_NUMBER, LevelupRecruitTimeRs.EXT_FIELD_NUMBER, LevelupRecruitTimeHandler.class);
		registerC(RecruitSoldierRq.EXT_FIELD_NUMBER, RecruitSoldierRs.EXT_FIELD_NUMBER, RecruitSoldierHandler.class);
		registerC(RecruitDoneRq.EXT_FIELD_NUMBER, RecruitDoneRs.EXT_FIELD_NUMBER, RecruitDoneHandler.class);
		registerC(CancelRecruitRq.EXT_FIELD_NUMBER, CancelRecruitRs.EXT_FIELD_NUMBER, CancelRecruitHandler.class);
		registerC(RecruitWorkQueCdRq.EXT_FIELD_NUMBER, RecruitWorkQueCdRs.EXT_FIELD_NUMBER, SoldierBuyWorkQueCdHandler.class);
		registerC(PrimarySoldierSpeedRq.EXT_FIELD_NUMBER, PrimarySoldierSpeedRs.EXT_FIELD_NUMBER, PrimarySoldierSpeedHandler.class);

	}

	// 关卡
	public void registerMission() {
		registerC(GetAllMissionRq.EXT_FIELD_NUMBER, GetAllMissionRs.EXT_FIELD_NUMBER, GetAllMissionHandler.class);
		registerC(MissionDoneRq.EXT_FIELD_NUMBER, MissionDoneRs.EXT_FIELD_NUMBER, MissionDoneHandler.class);
		registerC(HeroMissionRq.EXT_FIELD_NUMBER, HeroMissionRs.EXT_FIELD_NUMBER, MissionHeroHandler.class);
		registerC(ResourceMissionRq.EXT_FIELD_NUMBER, ResourceMissionRs.EXT_FIELD_NUMBER, ResourceMissionHandler.class);
		registerC(EquipPaperMissionRq.EXT_FIELD_NUMBER, EquipPaperMissionRs.EXT_FIELD_NUMBER, EquipPaperMissionHandler.class);
		registerC(MissionPb.SweepMissionRq.EXT_FIELD_NUMBER, MissionPb.SweepMissionRs.EXT_FIELD_NUMBER, SweepMissionHandler.class);
		registerC(MissionPb.GetStarAwardRq.EXT_FIELD_NUMBER, MissionPb.GetStarAwardRs.EXT_FIELD_NUMBER, GetStarAwardHandler.class);
		registerC(MissionPb.GetAllStarInfoRq.EXT_FIELD_NUMBER, MissionPb.GetAllStarInfoRs.EXT_FIELD_NUMBER, GetAllStarInfoHandler.class);


	}

	// 建筑
	public void registerBuilding() {
		registerC(GetBuildingRq.EXT_FIELD_NUMBER, GetBuildingRs.EXT_FIELD_NUMBER, GetBuildingHandler.class);
		registerC(DoResourceRq.EXT_FIELD_NUMBER, DoResourceRs.EXT_FIELD_NUMBER, DoRescoureHandler.class);
		registerC(DoAllResourceRq.EXT_FIELD_NUMBER, DoAllResourceRs.EXT_FIELD_NUMBER, DoAllRescoureHandler.class);
		registerC(HireOfficerRq.EXT_FIELD_NUMBER, HireOfficerRs.EXT_FIELD_NUMBER, DoHireEmployeeHandler.class);
		registerC(UpBuildingRq.EXT_FIELD_NUMBER, UpBuildingRs.EXT_FIELD_NUMBER, UpBuildingHandler.class);
		registerC(BuyBuildQueCdRq.EXT_FIELD_NUMBER, BuyBuildQueCdRs.EXT_FIELD_NUMBER, BuyBuildQueHandler.class);
		registerC(BuyBuildTeamRq.EXT_FIELD_NUMBER, BuyBuildTeamRs.EXT_FIELD_NUMBER, BuyBuildTeamHandler.class);
		registerC(OpenBuildingRq.EXT_FIELD_NUMBER, OpenBuildingRs.EXT_FIELD_NUMBER, OpenBuildingHandler.class);
		registerC(PrimaryBuildSpeedRq.EXT_FIELD_NUMBER, PrimaryBuildSpeedRs.EXT_FIELD_NUMBER, PrimaryBuildSpeedHandler.class);
		registerC(GetWareRq.EXT_FIELD_NUMBER, GetWareRs.EXT_FIELD_NUMBER, GetWareHandler.class);
		registerC(GetWareAwardRq.EXT_FIELD_NUMBER, GetWareAwardRs.EXT_FIELD_NUMBER, GetWareAwardHandler.class);
		registerC(OpenMilitiaRq.EXT_FIELD_NUMBER, OpenMilitiaRs.EXT_FIELD_NUMBER, OpenMilitiaHandler.class);
		registerC(BuildingPb.RebuildMilitiaRq.EXT_FIELD_NUMBER, BuildingPb.RebuildMilitiaRs.EXT_FIELD_NUMBER, DestroyMilitiaHandler.class);
		registerC(BuildingPb.ResBuildingDesRq.EXT_FIELD_NUMBER, BuildingPb.ResBuildingDesRs.EXT_FIELD_NUMBER, ResBuildingDesHandler.class);
		registerC(BuildingPb.BuildResRq.EXT_FIELD_NUMBER, BuildingPb.BuildResRs.EXT_FIELD_NUMBER, BuildResHandler.class);
		registerC(BuildingPb.BuyUpBuildQueRq.EXT_FIELD_NUMBER, BuildingPb.BuyUpBuildQueRs.EXT_FIELD_NUMBER, BuyUpBuildQueHandler.class);
		registerC(BuildingPb.BuyRebuildQueueRq.EXT_FIELD_NUMBER, BuildingPb.BuyRebuildQueueRs.EXT_FIELD_NUMBER, BuyRebuildQueueHandler.class);
		registerC(BuildingPb.BuyRebMilitiaQueueRq.EXT_FIELD_NUMBER, BuildingPb.BuyRebMilitiaQueueRs.EXT_FIELD_NUMBER, BuyRebMilitiaQueueHandler.class);
		registerC(BuildingPb.RecoverBuildRq.EXT_FIELD_NUMBER, BuildingPb.RecoverBuildRs.EXT_FIELD_NUMBER, RecoverBuildHandler.class);
		registerC(BuildingPb.ClickWormRq.EXT_FIELD_NUMBER, BuildingPb.ClickWormRs.EXT_FIELD_NUMBER, ClickWormHandler.class);
	}

	// 商店
	public void registerShop() {
		registerC(GetShopRq.EXT_FIELD_NUMBER, GetShopRs.EXT_FIELD_NUMBER, GetShopHandler.class);
		registerC(BuyShopRq.EXT_FIELD_NUMBER, BuyShopRs.EXT_FIELD_NUMBER, BuyShopHandler.class);
		registerC(GetVipGiftRq.EXT_FIELD_NUMBER, GetVipGiftRs.EXT_FIELD_NUMBER, GetVipGiftHandler.class);
		registerC(BuyVipGiftRq.EXT_FIELD_NUMBER, BuyVipGiftRs.EXT_FIELD_NUMBER, BuyVipGiftHandler.class);
		registerC(GetVipShopRq.EXT_FIELD_NUMBER, GetVipShopRs.EXT_FIELD_NUMBER, GetVipShopHandler.class);
		registerC(BuyVipShopRq.EXT_FIELD_NUMBER, BuyVipShopRs.EXT_FIELD_NUMBER, BuyVipShopHandler.class);
		registerC(BuyAndUseShopRq.EXT_FIELD_NUMBER, BuyAndUseShopRs.EXT_FIELD_NUMBER, BuyAndUseShopHandler.class);
		registerC(MissionPb.GetSweepHeroRq.EXT_FIELD_NUMBER, MissionPb.GetSweepHeroRs.EXT_FIELD_NUMBER, GetSweepHeroHandler.class);
		registerC(MissionPb.UpdateSweepHeroRq.EXT_FIELD_NUMBER, MissionPb.UpdateSweepHeroRs.EXT_FIELD_NUMBER, UpdateSweepHeroHandler.class);
	}

	// 聚宝盆
	public void registerDepot() {
		registerC(OpenDepotRq.EXT_FIELD_NUMBER, OpenDepotRs.EXT_FIELD_NUMBER, OpenDepotHandler.class);
		registerC(BuyDepotRq.EXT_FIELD_NUMBER, BuyDepotRs.EXT_FIELD_NUMBER, BuyDepotHandler.class);
		registerC(ExchangeResRq.EXT_FIELD_NUMBER, ExchangeResRs.EXT_FIELD_NUMBER, ExchangeResHandler.class);
		registerC(DepotPb.GetResourcePacketRq.EXT_FIELD_NUMBER, DepotPb.GetResourcePacketRs.EXT_FIELD_NUMBER, GetResourcePacketHandler.class);
		registerC(DepotPb.ResourcePacketRq.EXT_FIELD_NUMBER, DepotPb.ResourcePacketRs.EXT_FIELD_NUMBER, ResourcePacketHandler.class);
	}

	// 科技
	public void registerTech() {
		registerC(TechPb.UpTechRq.EXT_FIELD_NUMBER, TechPb.UpTechRs.EXT_FIELD_NUMBER, UpTechHandler.class);
		registerC(TechPb.TechLevelupRq.EXT_FIELD_NUMBER, TechPb.TechLevelupRs.EXT_FIELD_NUMBER, TechLevelupHandler.class);
		registerC(TechPb.HireResearcherRq.EXT_FIELD_NUMBER, TechPb.HireResearcherRs.EXT_FIELD_NUMBER, HireResearcherHandler.class);
		registerC(TechPb.TechKillCdRq.EXT_FIELD_NUMBER, TechPb.TechKillCdRs.EXT_FIELD_NUMBER, TechKillCdHandler.class);
		registerC(TechPb.GetTechRq.EXT_FIELD_NUMBER, TechPb.GetTechRs.EXT_FIELD_NUMBER, GetTechHandler.class);

	}

	// 任务
	public void registerTask() {
		registerC(TaskPb.GetTaskRq.EXT_FIELD_NUMBER, TaskPb.GetTaskRs.EXT_FIELD_NUMBER, GetTaskHandler.class);
		registerC(TaskPb.TaskAwardRq.EXT_FIELD_NUMBER, TaskPb.TaskAwardRs.EXT_FIELD_NUMBER, TaskAwardHandler.class);

	}

	// 排行
	public void registerRank() {
		registerC(RankPb.GetRankRq.EXT_FIELD_NUMBER, RankPb.GetRankRs.EXT_FIELD_NUMBER, GetRankHandler.class);
		registerC(RankPb.GetAreaRankRq.EXT_FIELD_NUMBER, RankPb.GetAreaRankRs.EXT_FIELD_NUMBER, GetAreaRankHandler.class);
		registerC(RankPb.GetRebelScoreRankRq.EXT_FIELD_NUMBER, RankPb.GetRebelScoreRankRs.EXT_FIELD_NUMBER, GetRebelScoreRankHandler.class);
		registerC(RankPb.GetCountryRankRq.EXT_FIELD_NUMBER, RankPb.GetCountryRankRs.EXT_FIELD_NUMBER, GetCountryRankHandler.class);
	}

	// 作坊
	public void resgisterWorkShop() {
		registerC(WorkShopPb.MakePropRq.EXT_FIELD_NUMBER, WorkShopPb.MakePropRs.EXT_FIELD_NUMBER, MakePropBeginHandler.class);
		registerC(WorkShopPb.MakeDoneRq.EXT_FIELD_NUMBER, WorkShopPb.MakeDoneRs.EXT_FIELD_NUMBER, MakePropDoneHandler.class);
		registerC(WorkShopPb.BuyQueRq.EXT_FIELD_NUMBER, WorkShopPb.BuyQueRs.EXT_FIELD_NUMBER, BuyWorkPropQuehandler.class);
		registerC(WorkShopPb.PreMakeRq.EXT_FIELD_NUMBER, WorkShopPb.PreMakeRs.EXT_FIELD_NUMBER, PrePropMakeHandler.class);
		registerC(WorkShopPb.GetWsQueRq.EXT_FIELD_NUMBER, WorkShopPb.GetWsQueRs.EXT_FIELD_NUMBER, GetWsQueHandler.class);
		registerC(WorkShopPb.MakeAllDoneRq.EXT_FIELD_NUMBER, WorkShopPb.MakeAllDoneRs.EXT_FIELD_NUMBER, MakeAllDoneHandler.class);

	}

	// 杀器
	public void resgisterKillEquip() {
		registerC(KillEquipPb.CompundRq.EXT_FIELD_NUMBER, KillEquipPb.CompundRs.EXT_FIELD_NUMBER, KillEuqipCompundHandler.class);
		registerC(KillEquipPb.BuyKillRq.EXT_FIELD_NUMBER, KillEquipPb.BuyKillRs.EXT_FIELD_NUMBER, BuyKillEquipHandler.class);
		registerC(KillEquipPb.UpKillRq.EXT_FIELD_NUMBER, KillEquipPb.UpKillRs.EXT_FIELD_NUMBER, UpKillEquipHandler.class);
		registerC(KillEquipPb.GetKillEquipRq.EXT_FIELD_NUMBER, KillEquipPb.GetKillEquipRs.EXT_FIELD_NUMBER, GetKillEquipHandler.class);
		registerC(KillEquipPb.OpenKillEquipRq.EXT_FIELD_NUMBER, KillEquipPb.OpenKillEquipRs.EXT_FIELD_NUMBER, OpenKillEquipHandler.class);
	}

	// 世界
	public void registerWorld() {
		registerC(WorldPb.GetMapRq.EXT_FIELD_NUMBER, WorldPb.GetMapRs.EXT_FIELD_NUMBER, GetMapHandler.class);
		registerC(WorldPb.AttackRebelRq.EXT_FIELD_NUMBER, WorldPb.AttackRebelRs.EXT_FIELD_NUMBER, AttackRebelHandler.class);
		registerC(WorldPb.AttendCountryWarRq.EXT_FIELD_NUMBER, WorldPb.AttendCountryWarRs.EXT_FIELD_NUMBER, AttendCountryWarHandler.class);
		registerC(WorldPb.CollectResRq.EXT_FIELD_NUMBER, WorldPb.CollectResRs.EXT_FIELD_NUMBER, CollectResHandler.class);
		registerC(WorldPb.AttackCityRq.EXT_FIELD_NUMBER, WorldPb.AttackCityRs.EXT_FIELD_NUMBER, AttackCityHandler.class);
		registerC(WorldPb.GetPvpCityRq.EXT_FIELD_NUMBER, WorldPb.GetPvpCityRs.EXT_FIELD_NUMBER, GetAttackCityHandler.class);
		registerC(WorldPb.AttendPvpCityRq.EXT_FIELD_NUMBER, WorldPb.AttendPvpCityRs.EXT_FIELD_NUMBER, AttendPvpCityHandler.class);
		registerC(WorldPb.CountryWarRq.EXT_FIELD_NUMBER, WorldPb.CountryWarRs.EXT_FIELD_NUMBER, CountryWarHandler.class);
		registerC(WorldPb.GetElectionRq.EXT_FIELD_NUMBER, WorldPb.GetElectionRs.EXT_FIELD_NUMBER, GetElectionHandler.class);
		registerC(WorldPb.ElectionCityRq.EXT_FIELD_NUMBER, WorldPb.ElectionCityRs.EXT_FIELD_NUMBER, ElectionCityHandler.class);
		registerC(WorldPb.CancelCityOwnerRq.EXT_FIELD_NUMBER, WorldPb.CancelCityOwnerRs.EXT_FIELD_NUMBER, CancelCityOwnerHandler.class);
		registerC(WorldPb.GetPlayerPosRq.EXT_FIELD_NUMBER, WorldPb.GetPlayerPosRs.EXT_FIELD_NUMBER, GetPlayerPosHandler.class);
		registerC(WorldPb.GetCityOwnRq.EXT_FIELD_NUMBER, WorldPb.GetCityOwnRs.EXT_FIELD_NUMBER, GetCityOwnHandler.class);
		registerC(WorldPb.RebuildCityRq.EXT_FIELD_NUMBER, WorldPb.RebuildCityRs.EXT_FIELD_NUMBER, RebuildCityHandler.class);
		registerC(WorldPb.FixCityRq.EXT_FIELD_NUMBER, WorldPb.FixCityRs.EXT_FIELD_NUMBER, FixCityHandler.class);
		registerC(WorldPb.KillWorldBossRq.EXT_FIELD_NUMBER, WorldPb.KillWorldBossRs.EXT_FIELD_NUMBER, KillWorldBossHandler.class);
		registerC(WorldPb.GetWorldTargerAwardRq.EXT_FIELD_NUMBER, WorldPb.GetWorldTargerAwardRs.EXT_FIELD_NUMBER, GetWorldTargerAwardHandler.class);
		registerC(WorldPb.GetSeasonRq.EXT_FIELD_NUMBER, WorldPb.GetSeasonRs.EXT_FIELD_NUMBER, GetSeasonHandler.class);
		registerC(WorldPb.GetCityAwardRq.EXT_FIELD_NUMBER, WorldPb.GetCityAwardRs.EXT_FIELD_NUMBER, GetCityAwardHandler.class);
		registerC(WorldPb.ScoutRq.EXT_FIELD_NUMBER, WorldPb.ScoutRs.EXT_FIELD_NUMBER, ScoutHandler.class);
		registerC(WorldPb.AddSoldierRq.EXT_FIELD_NUMBER, WorldPb.AddSoldierRs.EXT_FIELD_NUMBER, AddSoldierHandler.class);
		//迁城
		registerC(WorldPb.MapMoveRq.EXT_FIELD_NUMBER, WorldPb.MapMoveRs.EXT_FIELD_NUMBER, MapMoveHandler.class);
		registerC(WorldPb.GetMarchRq.EXT_FIELD_NUMBER, WorldPb.GetMarchRs.EXT_FIELD_NUMBER, GetMarchHandler.class);
		registerC(WorldPb.MarchCancelRq.EXT_FIELD_NUMBER, WorldPb.MarchCancelRs.EXT_FIELD_NUMBER, MarchCancelHandler.class);
		registerC(WorldPb.SpeedMarchRq.EXT_FIELD_NUMBER, WorldPb.SpeedMarchRs.EXT_FIELD_NUMBER, SpeedMarchHandler.class);
		registerC(WorldPb.CityFightHelpRq.EXT_FIELD_NUMBER, WorldPb.CityFightHelpRs.EXT_FIELD_NUMBER, CityFightHelpHandler.class);
		registerC(WorldPb.GetDefenceInfoRq.EXT_FIELD_NUMBER, WorldPb.GetDefenceInfoRs.EXT_FIELD_NUMBER, GetDefenceInfoHandler.class);
		registerC(WorldPb.CallTransferRq.EXT_FIELD_NUMBER, WorldPb.CallTransferRs.EXT_FIELD_NUMBER, CallTransferHandler.class);
		registerC(WorldPb.ReplyTransferRq.EXT_FIELD_NUMBER, WorldPb.ReplyTransferRs.EXT_FIELD_NUMBER, ReplyTransferHandler.class);
		registerC(WorldPb.GetCityRq.EXT_FIELD_NUMBER, WorldPb.GetCityRs.EXT_FIELD_NUMBER, GetCityHandler.class);
		registerC(WorldPb.GetWorldBossRq.EXT_FIELD_NUMBER, WorldPb.GetWorldBossRs.EXT_FIELD_NUMBER, GetWorldBossHandler.class);
		registerC(WorldPb.DevCityRq.EXT_FIELD_NUMBER, WorldPb.DevCityRs.EXT_FIELD_NUMBER, DevCityHandler.class);
		registerC(WorldPb.GetResInfoRq.EXT_FIELD_NUMBER, WorldPb.GetResInfoRs.EXT_FIELD_NUMBER, GetResInfoHandler.class);
		registerC(WorldPb.GetWorldCountryWarRq.EXT_FIELD_NUMBER, WorldPb.GetWorldCountryWarRs.EXT_FIELD_NUMBER, GetCountryWarInfoHandler.class);
		registerC(WorldPb.GetWorldTargetTaskRq.EXT_FIELD_NUMBER, WorldPb.GetWorldTargetTaskRs.EXT_FIELD_NUMBER, GetWorldTargetTaskHandler.class);
		registerC(WorldPb.AwardWorldTargetRq.EXT_FIELD_NUMBER, WorldPb.AwardWorldTargetRs.EXT_FIELD_NUMBER, AwardWorldTargetHandler.class);
		registerC(WorldPb.GetWorldBossInfoRq.EXT_FIELD_NUMBER, WorldPb.GetWorldBossInfoRs.EXT_FIELD_NUMBER, GetWorldBossInfoHandler.class);
		registerC(WorldPb.AttackWorldBossRq.EXT_FIELD_NUMBER, WorldPb.AttackWorldBossRs.EXT_FIELD_NUMBER, AttackWorldBossHandler.class);
		registerC(WorldPb.WorldActivityPlanRq.EXT_FIELD_NUMBER, WorldPb.WorldActivityPlanRs.EXT_FIELD_NUMBER, WorldActivityPlanHandler.class);
		registerC(WorldPb.RebelWarRq.EXT_FIELD_NUMBER, WorldPb.RebelWarRs.EXT_FIELD_NUMBER, RebelWarHandler.class);
		registerC(WorldPb.AttendRebelWarRq.EXT_FIELD_NUMBER, WorldPb.AttendRebelWarRs.EXT_FIELD_NUMBER, AttendRebelWarHandler.class);
		registerC(WorldPb.GetRebelWarRq.EXT_FIELD_NUMBER, WorldPb.GetRebelWarRs.EXT_FIELD_NUMBER, GetRebelWarHandler.class);
		registerC(WorldPb.RebelFightHelpRq.EXT_FIELD_NUMBER, WorldPb.RebelFightHelpRs.EXT_FIELD_NUMBER, RebelFightHelpHandler.class);
		registerC(WorldPb.RebelFightShareRq.EXT_FIELD_NUMBER, WorldPb.RebelFightShareRq.EXT_FIELD_NUMBER, RebelFightShareHandler.class);
		registerC(WorldPb.FindNearMonsterRq.EXT_FIELD_NUMBER, WorldPb.FindNearMonsterRs.EXT_FIELD_NUMBER, FindNearMonsterHandler.class);
		registerC(WorldPb.DeliveryInitRq.EXT_FIELD_NUMBER, WorldPb.DeliveryInitRs.EXT_FIELD_NUMBER, DeliveryInitHandler.class);
		registerC(WorldPb.DeliveryRq.EXT_FIELD_NUMBER, WorldPb.MapMoveRs.EXT_FIELD_NUMBER, DeliveryHandler.class);

		registerC(WorldPb.GetStealCityAwardRq.EXT_FIELD_NUMBER, WorldPb.GetStealCityAwardRs.EXT_FIELD_NUMBER, GetStealCityAwardHandler.class);
		registerC(WorldPb.GetNowStealCityRq.EXT_FIELD_NUMBER, WorldPb.GetNowStealCityRs.EXT_FIELD_NUMBER, GetNowStealCityHandler.class);
		registerC(WorldPb.GetRebelPosRq.EXT_FIELD_NUMBER, WorldPb.GetRebelPosRs.EXT_FIELD_NUMBER, GetRebelPosHandler.class);
		//巨型虫族
		registerC(WorldPb.GetBigMonsterInfoRq.EXT_FIELD_NUMBER, WorldPb.GetBigMonsterInfoRs.EXT_FIELD_NUMBER, GetBigMonsterInfoHandler.class);
		registerC(WorldPb.GetBigMonsterActivityRq.EXT_FIELD_NUMBER, WorldPb.GetBigMonsterActivityRs.EXT_FIELD_NUMBER, GetBigMonsterActivityHandler.class);
		registerC(WorldPb.GetBigMonsterWarRq.EXT_FIELD_NUMBER, WorldPb.GetBigMonsterWarRs.EXT_FIELD_NUMBER, GetBigMonsterWarHandler.class);
		registerC(WorldPb.BigMonsterFightHelpRq.EXT_FIELD_NUMBER, WorldPb.BigMonsterFightHelpRs.EXT_FIELD_NUMBER, BigMonsterFightHelpHandler.class);
		registerC(WorldPb.GetBigMonsterByLevelRq.EXT_FIELD_NUMBER, WorldPb.GetBigMonsterByLevelRs.EXT_FIELD_NUMBER, GetBigMonsterByLevelHandler.class);

		registerC(WorldPb.GetForTressRq.EXT_FIELD_NUMBER, WorldPb.GetForTressRs.EXT_FIELD_NUMBER, FortressHandler.class);// 查看要塞信息
		registerC(WorldPb.GetForTressBuildRq.EXT_FIELD_NUMBER, WorldPb.GetForTressBuildRs.EXT_FIELD_NUMBER, DoFortressBuildHandler.class);// 建设要塞
		registerC(WorldPb.DoResourceInfoRq.EXT_FIELD_NUMBER, WorldPb.DoResourceInfoRs.EXT_FIELD_NUMBER, DoSuperResInfoHandler.class);// 矿点基本信息
		registerC(WorldPb.AttkerSuperResRq.EXT_FIELD_NUMBER, WorldPb.AttkerSuperResRs.EXT_FIELD_NUMBER, AttackSuperResHandler.class);// 攻击||驻防 大型矿点
		registerC(WorldPb.GetForTressUpNameRq.EXT_FIELD_NUMBER, WorldPb.GetForTressUpNameRs.EXT_FIELD_NUMBER, UpdateFortressNameHandler.class);// 要塞改名
		registerC(WorldPb.GetAllMainCityCountryRq.EXT_FIELD_NUMBER, WorldPb.GetAllMainCityCountryRs.EXT_FIELD_NUMBER, GetAllMainCityCountryHandler.class);// 获取所有地图中心城市的归属
		registerC(WorldPb.SearchEntityRq.EXT_FIELD_NUMBER, WorldPb.SearchEntityRs.EXT_FIELD_NUMBER, SearchResourceHandler.class);// 获取所有地图中心城市的归属
		registerC(WorldPb.CityRemarkRq.EXT_FIELD_NUMBER, WorldPb.CityRemarkRs.EXT_FIELD_NUMBER, CityRemarkHandler.class);// 标记
	}

	// 城墙
	public void registerWall() {
		registerC(WallPb.GetWallInfoRq.EXT_FIELD_NUMBER, WallPb.GetWallInfoRs.EXT_FIELD_NUMBER, GetWallInfoHandler.class);
		registerC(WallPb.ChangeHeroPosRq.EXT_FIELD_NUMBER, WallPb.ChangeHeroPosRs.EXT_FIELD_NUMBER, ChangeHeroPosHandler.class);
		registerC(WallPb.HireDefenderRq.EXT_FIELD_NUMBER, WallPb.HireDefenderRs.EXT_FIELD_NUMBER, HireDefendersHandler.class);
		registerC(WallPb.KillDefenderCdRq.EXT_FIELD_NUMBER, WallPb.KillDefenderCdRs.EXT_FIELD_NUMBER, KillDefenderCdHandler.class);
		registerC(WallPb.LevelUpDefenderRq.EXT_FIELD_NUMBER, WallPb.LevelUpDefenderRs.EXT_FIELD_NUMBER, LevelUpDefenderHandler.class);
		registerC(WallPb.FriendAssistRq.EXT_FIELD_NUMBER, WallPb.FriendAssistRs.EXT_FIELD_NUMBER, FriendAssistHandler.class);
		registerC(WallPb.FriendMarchCancelRq.EXT_FIELD_NUMBER, WallPb.FriendMarchCancelRs.EXT_FIELD_NUMBER, FriendMarchCancelHandler.class);
		registerC(WallPb.KickMarchRq.EXT_FIELD_NUMBER, WallPb.KickMarchRs.EXT_FIELD_NUMBER, KickMarchHandler.class);
	}

	// 邮件
	public void registerMail() {
		registerC(MailPb.GetMailRq.EXT_FIELD_NUMBER, MailPb.GetMailRs.EXT_FIELD_NUMBER, GetMailHandler.class);
		registerC(MailPb.MailReadRq.EXT_FIELD_NUMBER, MailPb.MailReadRs.EXT_FIELD_NUMBER, MailReadHandler.class);
		registerC(MailPb.MailLockRq.EXT_FIELD_NUMBER, MailPb.MailLockRs.EXT_FIELD_NUMBER, MailLockHandler.class);
		registerC(MailPb.MailRemoveRq.EXT_FIELD_NUMBER, MailPb.MailRemoveRs.EXT_FIELD_NUMBER, MailRemoveHandler.class);
		registerC(MailPb.GetMailReportRq.EXT_FIELD_NUMBER, MailPb.GetMailReportRs.EXT_FIELD_NUMBER, GetMailReportHandler.class);
		registerC(MailPb.SendMailRq.EXT_FIELD_NUMBER, MailPb.SendMailRs.EXT_FIELD_NUMBER, SendMailHandler.class);
		registerC(MailPb.MailAwardRq.EXT_FIELD_NUMBER, MailPb.MailAwardRs.EXT_FIELD_NUMBER, MailAwardHandler.class);
		registerC(MailPb.ReplyMailRq.EXT_FIELD_NUMBER, MailPb.ReplyMailRs.EXT_FIELD_NUMBER, ReplyMailHandler.class);
		registerC(MailPb.BlackListRq.EXT_FIELD_NUMBER, MailPb.BlackListRs.EXT_FIELD_NUMBER, BlackListHandler.class);
		registerC(MailPb.BlackRq.EXT_FIELD_NUMBER, MailPb.BlackRs.EXT_FIELD_NUMBER, BlackHandler.class);
		registerC(MailPb.ReadAllRq.EXT_FIELD_NUMBER, MailPb.ReadAllRs.EXT_FIELD_NUMBER, ReadAllHandler.class);
		registerC(MailPb.GetMailCountRq.EXT_FIELD_NUMBER, MailPb.GetMailCountRs.EXT_FIELD_NUMBER, GetMailCountHandler.class);
		registerC(MailPb.GetCountryMailRq.EXT_FIELD_NUMBER, MailPb.GetCountryMailRs.EXT_FIELD_NUMBER, GetCountryMailHandler.class);
		registerC(MailPb.SendCountryMailRq.EXT_FIELD_NUMBER, MailPb.SendCountryMailRs.EXT_FIELD_NUMBER, SendCountryMailHandler.class);
		registerC(MailPb.GetPersonMailRq.EXT_FIELD_NUMBER, MailPb.GetPersonMailRs.EXT_FIELD_NUMBER, GetPersonMailHandler.class);
	}

	// 国家
	public void registerCountry() {
		registerC(CountryPb.GetCountryRq.EXT_FIELD_NUMBER, CountryPb.GetCountryRs.EXT_FIELD_NUMBER, GetCountryHandler.class);
		registerC(CountryPb.CountryBuildRq.EXT_FIELD_NUMBER, CountryPb.CountryBuildRs.EXT_FIELD_NUMBER, CountryBuildHandler.class);
		registerC(CountryPb.TitleUpRq.EXT_FIELD_NUMBER, CountryPb.TitleUpRs.EXT_FIELD_NUMBER, TitleUpHandler.class);
		registerC(CountryPb.GetCountryTaskRq.EXT_FIELD_NUMBER, CountryPb.GetCountryTaskRs.EXT_FIELD_NUMBER, GetCountryTaskHandler.class);
		registerC(CountryPb.CountryTaskAwardRq.EXT_FIELD_NUMBER, CountryPb.CountryTaskAwardRs.EXT_FIELD_NUMBER, CountryTaskAwardHandler.class);
		registerC(CountryPb.GetCountryHeroRq.EXT_FIELD_NUMBER, CountryPb.GetCountryHeroRs.EXT_FIELD_NUMBER, GetCountryHeroHandler.class);
		registerC(CountryPb.GetCountryGloryRq.EXT_FIELD_NUMBER, CountryPb.GetCountryGloryRs.EXT_FIELD_NUMBER, GetCountryGloryHandler.class);
		registerC(CountryPb.CountryGloryAwardRq.EXT_FIELD_NUMBER, CountryPb.CountryGloryAwardRs.EXT_FIELD_NUMBER, CountryGloryAwardHandler.class);
		registerC(CountryPb.GetGloryRankRq.EXT_FIELD_NUMBER, CountryPb.GetGloryRankRs.EXT_FIELD_NUMBER, GetGloryRankHandler.class);
		registerC(CountryPb.GetGovernRq.EXT_FIELD_NUMBER, CountryPb.GetGovernRs.EXT_FIELD_NUMBER, GetGovernHandler.class);
		registerC(CountryPb.VoteGovernRq.EXT_FIELD_NUMBER, CountryPb.VoteGovernRs.EXT_FIELD_NUMBER, VoteGovernHandler.class);
		registerC(CountryPb.AppointGeneralRq.EXT_FIELD_NUMBER, CountryPb.AppointGeneralRs.EXT_FIELD_NUMBER, AppointGeneralHandler.class);
		registerC(CountryPb.RevokeGeneralRq.EXT_FIELD_NUMBER, CountryPb.RevokeGeneralRs.EXT_FIELD_NUMBER, RevokeGeneralHandler.class);
		registerC(CountryPb.GetCountryDailyRq.EXT_FIELD_NUMBER, CountryPb.GetCountryDailyRs.EXT_FIELD_NUMBER, GetCountryDailyHandler.class);
		registerC(CountryPb.GetCountryCityRq.EXT_FIELD_NUMBER, CountryPb.GetCountryCityRs.EXT_FIELD_NUMBER, GetCountryCityHandler.class);
		registerC(CountryPb.GetCountryWarRq.EXT_FIELD_NUMBER, CountryPb.GetCountryWarRs.EXT_FIELD_NUMBER, GetCountryWarHandler.class);
		registerC(CountryPb.DoCountryPublishRq.EXT_FIELD_NUMBER, CountryPb.DoCountryPublishRs.EXT_FIELD_NUMBER, DoCountryPublishHandler.class);
		registerC(CountryPb.GetAppointRq.EXT_FIELD_NUMBER, CountryPb.GetAppointRs.EXT_FIELD_NUMBER, GetAppointHandler.class);
		registerC(CountryPb.GetCityWarRq.EXT_FIELD_NUMBER, CountryPb.GetCityWarRs.EXT_FIELD_NUMBER, GetCityWarHandler.class);
		registerC(CountryPb.FindCountryHeroRq.EXT_FIELD_NUMBER, CountryPb.FindCountryHeroRs.EXT_FIELD_NUMBER, FindCountryHeroHandler.class);
		registerC(CountryPb.OpenCountryHeroRq.EXT_FIELD_NUMBER, CountryPb.OpenCountryHeroRs.EXT_FIELD_NUMBER, OpenCountryHeroHandler.class);
		registerC(CountryPb.TrainCountryHeroRq.EXT_FIELD_NUMBER, CountryPb.TrainCountryHeroRs.EXT_FIELD_NUMBER, TrainCountryHeroHandler.class);
		registerC(CountryPb.ModifyCountryNameRq.EXT_FIELD_NUMBER, CountryPb.ModifyCountryNameRs.EXT_FIELD_NUMBER, ModifyCountryNameHandler.class);
		registerC(CountryPb.GetCountryNameRq.EXT_FIELD_NUMBER, CountryPb.GetCountryNameRs.EXT_FIELD_NUMBER, GetCountryNameHandler.class);
		registerC(CountryPb.TitleAwardRq.EXT_FIELD_NUMBER, CountryPb.TitleAwardRs.EXT_FIELD_NUMBER, LoadTitleAwardHandler.class);
	}

	// 聊天
	public void registerChat() {
		registerC(ChatPb.GetChatRq.EXT_FIELD_NUMBER, ChatPb.GetChatRs.EXT_FIELD_NUMBER, GetChatHandler.class);
		registerC(ChatPb.DoChatRq.EXT_FIELD_NUMBER, ChatPb.DoChatRs.EXT_FIELD_NUMBER, DoChatHandler.class);
		registerC(ChatPb.ShareMailRq.EXT_FIELD_NUMBER, ChatPb.ShareMailRs.EXT_FIELD_NUMBER, ShareMailHandler.class);
		registerC(ChatPb.ShareChatRq.EXT_FIELD_NUMBER, ChatPb.ShareChatRs.EXT_FIELD_NUMBER, ShareChatHandler.class);
		registerC(ChatPb.SeeManRq.EXT_FIELD_NUMBER, ChatPb.SeeManRs.EXT_FIELD_NUMBER, SeeManHandler.class);
		registerC(ChatPb.SuggestRq.EXT_FIELD_NUMBER, ChatPb.SuggestRs.EXT_FIELD_NUMBER, SuggestHandler.class);
		registerC(ChatPb.RecordRq.EXT_FIELD_NUMBER, ChatPb.RecordRs.EXT_FIELD_NUMBER, RecordHandler.class);
		registerC(ChatPb.UpdateSignatureRq.EXT_FIELD_NUMBER, ChatPb.UpdateSignatureRs.EXT_FIELD_NUMBER, UpdateSignatureHandler.class);
		registerC(ChatPb.DoPersonChatRq.EXT_FIELD_NUMBER, ChatPb.DoPersonChatRs.EXT_FIELD_NUMBER, DoPersonChatHandler.class);
		registerC(ChatPb.GetPersonChatRoomRq.EXT_FIELD_NUMBER, ChatPb.GetPersonChatRoomRs.EXT_FIELD_NUMBER, GetPersonChatRoomHanlder.class);
		registerC(ChatPb.GetPersonChatRq.EXT_FIELD_NUMBER, ChatPb.GetPersonChatRs.EXT_FIELD_NUMBER, GetPersonChatHanlder.class);
		registerC(ChatPb.ShareHeroRq.EXT_FIELD_NUMBER, ChatPb.ShareHeroRs.EXT_FIELD_NUMBER, ShareHeroHandler.class);

		registerC(ChatPb.PersonChatReadRq.EXT_FIELD_NUMBER, ChatPb.PersonChatReadRs.EXT_FIELD_NUMBER, PersonChatReadHanlder.class);
		registerC(ChatPb.PersonChatRemoveRq.EXT_FIELD_NUMBER, ChatPb.PersonChatRemoveRs.EXT_FIELD_NUMBER, PersonChatRemoveHandler.class);
		registerC(ChatPb.DuelRq.EXT_FIELD_NUMBER, ChatPb.DuelRs.EXT_FIELD_NUMBER, DuelHandler.class);
	}

	public void registerActivity() {
		registerC(ActivityPb.GetActivityListRq.EXT_FIELD_NUMBER, ActivityPb.GetActivityListRs.EXT_FIELD_NUMBER, GetActivityListHandler.class);
		registerC(ActivityPb.GetActivityAwardRq.EXT_FIELD_NUMBER, ActivityPb.GetActivityAwardRs.EXT_FIELD_NUMBER, GetActivityAwardHandler.class);
		registerC(ActivityPb.ActLevelRq.EXT_FIELD_NUMBER, ActivityPb.ActLevelRs.EXT_FIELD_NUMBER, ActLevelHandler.class);
		registerC(ActivityPb.ActSceneCityRq.EXT_FIELD_NUMBER, ActivityPb.ActSceneCityRs.EXT_FIELD_NUMBER, ActSceneCityHandler.class);
		registerC(ActivityPb.ActInvestRq.EXT_FIELD_NUMBER, ActivityPb.ActInvestRs.EXT_FIELD_NUMBER, ActInvestHandler.class);
		registerC(ActivityPb.DoInvestRq.EXT_FIELD_NUMBER, ActivityPb.DoInvestRs.EXT_FIELD_NUMBER, DoInvestHandler.class);
		registerC(ActivityPb.ActHighVipRq.EXT_FIELD_NUMBER, ActivityPb.ActHighVipRs.EXT_FIELD_NUMBER, ActHighVipHandler.class);
		registerC(ActivityPb.ActSoilderRankRq.EXT_FIELD_NUMBER, ActivityPb.ActSoilderRankRs.EXT_FIELD_NUMBER, ActSoilderRankHandler.class);
		registerC(ActivityPb.ActCityRq.EXT_FIELD_NUMBER, ActivityPb.ActCityRs.EXT_FIELD_NUMBER, ActCityHandler.class);
		registerC(ActivityPb.ActTopupRankRq.EXT_FIELD_NUMBER, ActivityPb.ActTopupRankRs.EXT_FIELD_NUMBER, ActTopupRankHandler.class);
		registerC(ActivityPb.ActForgeRankRq.EXT_FIELD_NUMBER, ActivityPb.ActForgeRankRs.EXT_FIELD_NUMBER, ActForgeRankHandler.class);
		registerC(ActivityPb.ActCountryRankRq.EXT_FIELD_NUMBER, ActivityPb.ActCountryRankRs.EXT_FIELD_NUMBER, ActCountryRankHandler.class);
		registerC(ActivityPb.ActOilRankRq.EXT_FIELD_NUMBER, ActivityPb.ActOilRankRs.EXT_FIELD_NUMBER, ActOilRankHandler.class);
		registerC(ActivityPb.ActWashRankRq.EXT_FIELD_NUMBER, ActivityPb.ActWashRankRs.EXT_FIELD_NUMBER, ActWashRankHandler.class);
		registerC(ActivityPb.ActStoneRankRq.EXT_FIELD_NUMBER, ActivityPb.ActStoneRankRs.EXT_FIELD_NUMBER, ActStoneRankHandler.class);
		registerC(ActivityPb.ActSevenLoginRq.EXT_FIELD_NUMBER, ActivityPb.ActSevenLoginRs.EXT_FIELD_NUMBER, ActSevenLoginHandler.class);
		registerC(ActivityPb.ActHeroKowtowRq.EXT_FIELD_NUMBER, ActivityPb.ActHeroKowtowRs.EXT_FIELD_NUMBER, ActHeroKowtowHandler.class);
		registerC(ActivityPb.BuyHeroKowtowRq.EXT_FIELD_NUMBER, ActivityPb.BuyHeroKowtowRs.EXT_FIELD_NUMBER, BuyHeroKowtowHandler.class);
		registerC(ActivityPb.RefreshHeroKowtowRq.EXT_FIELD_NUMBER, ActivityPb.RefreshHeroKowtowRs.EXT_FIELD_NUMBER, RefreshHeroKowtowHandler.class);
		registerC(ActivityPb.ActLowCountryRq.EXT_FIELD_NUMBER, ActivityPb.ActLowCountryRs.EXT_FIELD_NUMBER, ActLowCountryHandler.class);
		registerC(ActivityPb.ActSerPayRq.EXT_FIELD_NUMBER, ActivityPb.ActSerPayRs.EXT_FIELD_NUMBER, ActSerPayHandler.class);
		registerC(ActivityPb.ActPayFirstRq.EXT_FIELD_NUMBER, ActivityPb.ActPayFirstRs.EXT_FIELD_NUMBER, ActPayFirstHandler.class);
		registerC(ActivityPb.ActBuyGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActBuyGiftRs.EXT_FIELD_NUMBER, ActBuyGiftHandler.class);
		registerC(ActivityPb.DoQuotaRq.EXT_FIELD_NUMBER, ActivityPb.DoQuotaRs.EXT_FIELD_NUMBER, DoQuotaHandler.class);
		registerC(ActivityPb.ActLuckDialRq.EXT_FIELD_NUMBER, ActivityPb.ActLuckDialRs.EXT_FIELD_NUMBER, ActLuckDialHandler.class);
		//紫装转盘
		registerC(ActivityPb.ActPurpDialRq.EXT_FIELD_NUMBER, ActivityPb.ActPurpDialRs.EXT_FIELD_NUMBER, ActPurpDialHandler.class);
		registerC(ActivityPb.ActMasterDialRq.EXT_FIELD_NUMBER, ActivityPb.ActMasterDialRs.EXT_FIELD_NUMBER, ActMasterDialHandler.class);
		registerC(ActivityPb.DoLuckDialRq.EXT_FIELD_NUMBER, ActivityPb.DoLuckDialRs.EXT_FIELD_NUMBER, DoLuckDialHandler.class);
		registerC(ActivityPb.DoPurpDialRq.EXT_FIELD_NUMBER, ActivityPb.DoPurpDialRs.EXT_FIELD_NUMBER, DoPurpDialHandler.class);
		registerC(ActivityPb.DoMasterDialRq.EXT_FIELD_NUMBER, ActivityPb.DoMasterDialRs.EXT_FIELD_NUMBER, DoMasterDialHandler.class);
		registerC(ActivityPb.ActzhenjiIconRq.EXT_FIELD_NUMBER, ActivityPb.ActzhenjiIconRs.EXT_FIELD_NUMBER, ActzhenjiIconHandler.class);
		registerC(ActivityPb.ActDayPayRq.EXT_FIELD_NUMBER, ActivityPb.ActDayPayRs.EXT_FIELD_NUMBER, ActDayPayHandler.class);
		registerC(ActivityPb.ActFlashGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActFlashGiftRs.EXT_FIELD_NUMBER, ActFlashGiftHandler.class);
		registerC(ActivityPb.ActCostPersonRq.EXT_FIELD_NUMBER, ActivityPb.ActCostPersonRs.EXT_FIELD_NUMBER, ActCostPersonHandler.class);
		registerC(ActivityPb.ActCostServerRq.EXT_FIELD_NUMBER, ActivityPb.ActCostServerRs.EXT_FIELD_NUMBER, ActCostServerHandler.class);
		registerC(ActivityPb.ActTopupPersonRq.EXT_FIELD_NUMBER, ActivityPb.ActTopupPersonRs.EXT_FIELD_NUMBER, ActTopupPersonHandler.class);
		registerC(ActivityPb.ActTopupServerRq.EXT_FIELD_NUMBER, ActivityPb.ActTopupServerRs.EXT_FIELD_NUMBER, ActTopupServerHandler.class);
		registerC(ActivityPb.ActGrowFootRq.EXT_FIELD_NUMBER, ActivityPb.ActGrowFootRs.EXT_FIELD_NUMBER, ActGrowFootHandler.class);
		registerC(ActivityPb.DoGrowFootRq.EXT_FIELD_NUMBER, ActivityPb.DoGrowFootRs.EXT_FIELD_NUMBER, DoGrowFootHandler.class);
		registerC(ActivityPb.ActStoneDialRq.EXT_FIELD_NUMBER, ActivityPb.ActStoneDialRs.EXT_FIELD_NUMBER, ActStoneDialHandler.class);
		registerC(ActivityPb.DoStoneDialRq.EXT_FIELD_NUMBER, ActivityPb.DoStoneDialRs.EXT_FIELD_NUMBER, DoStoneDialHandler.class);
		registerC(ActivityPb.BuyStoneDialRq.EXT_FIELD_NUMBER, ActivityPb.BuyStoneDialRs.EXT_FIELD_NUMBER, BuyStoneDialHandler.class);
		registerC(ActivityPb.ExchangeHeroRq.EXT_FIELD_NUMBER, ActivityPb.ExchangeHeroRs.EXT_FIELD_NUMBER, ExchangeHeroHandler.class);
		registerC(ActivityPb.ExchangeItemRq.EXT_FIELD_NUMBER, ActivityPb.ExchangeItemRs.EXT_FIELD_NUMBER, ExchangeItemHandler.class);
		registerC(ActivityPb.ActPowerRq.EXT_FIELD_NUMBER, ActivityPb.ActPowerRs.EXT_FIELD_NUMBER, ActPowerHandler.class);
		registerC(ActivityPb.ActPayGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActPayGiftRs.EXT_FIELD_NUMBER, ActPayGiftHandler.class);
		registerC(ActivityPb.ActBuildRankRq.EXT_FIELD_NUMBER, ActivityPb.ActBuildRankRs.EXT_FIELD_NUMBER, ActBuildRankHandler.class);
		registerC(ActivityPb.ActOnlineTimeRq.EXT_FIELD_NUMBER, ActivityPb.ActOnlineTimeRs.EXT_FIELD_NUMBER, ActOnlineTimeHandler.class);
		registerC(ActivityPb.ActOnlineAwardRq.EXT_FIELD_NUMBER, ActivityPb.ActOnlineAwardRs.EXT_FIELD_NUMBER, ActOnlineAwardHandler.class);

		registerC(ActivityPb.ActPayEveryDayRq.EXT_FIELD_NUMBER, ActivityPb.ActPayEveryDayRs.EXT_FIELD_NUMBER, ActPayEveryDayHandler.class);
		registerC(ActivityPb.ActMonthCardRq.EXT_FIELD_NUMBER, ActivityPb.ActMonthCardRs.EXT_FIELD_NUMBER, ActMonthCardHandler.class);
		registerC(ActivityPb.DoActPayFirstRq.EXT_FIELD_NUMBER, ActivityPb.DoActPayFirstRs.EXT_FIELD_NUMBER, DoActPayFirstHandler.class);
		registerC(ActivityPb.ActLoginVipRq.EXT_FIELD_NUMBER, ActivityPb.ActLoginVipRs.EXT_FIELD_NUMBER, ActLoginVipHandler.class);
		registerC(ActivityPb.ActLevelRankRq.EXT_FIELD_NUMBER, ActivityPb.ActLevelRankRs.EXT_FIELD_NUMBER, ActLevelRankHandler.class);
		registerC(ActivityPb.ActSevenRq.EXT_FIELD_NUMBER, ActivityPb.ActSevenRs.EXT_FIELD_NUMBER, ActSevenHandler.class);
		registerC(ActivityPb.DoSevenAwardRq.EXT_FIELD_NUMBER, ActivityPb.DoSevenAwardRs.EXT_FIELD_NUMBER, DoSevenAwardHandler.class);
		registerC(ActivityPb.GetWonderfulListRq.EXT_FIELD_NUMBER, ActivityPb.GetWonderfulListRs.EXT_FIELD_NUMBER, GetWonderfulListHandler.class);
		registerC(ActivityPb.RedDialRq.EXT_FIELD_NUMBER, ActivityPb.RedDialRs.EXT_FIELD_NUMBER, RedDialHandler.class);
		registerC(ActivityPb.MakeEquipRq.EXT_FIELD_NUMBER, ActivityPb.MakeEquipRs.EXT_FIELD_NUMBER, MakeEquipHandler.class);
		registerC(ActivityPb.GetRedDialRq.EXT_FIELD_NUMBER, ActivityPb.GetRedDialRs.EXT_FIELD_NUMBER, GetRedDialHandler.class);
		registerC(ActivityPb.ActSevenRechargeRq.EXT_FIELD_NUMBER, ActivityPb.ActSevenRechargeRs.EXT_FIELD_NUMBER, ActSevenRechargeHandler.class);
		registerC(ActivityPb.ActDailyCheckInRq.EXT_FIELD_NUMBER, ActivityPb.ActDailyCheckInRs.EXT_FIELD_NUMBER, ActCheckInHandler.class);
		registerC(ActivityPb.DoDailyCheckInRq.EXT_FIELD_NUMBER, ActivityPb.DoDailyCheckInRs.EXT_FIELD_NUMBER, DoCheckInHandler.class);
		registerC(ActivityPb.ActWorldBattleRq.EXT_FIELD_NUMBER, ActivityPb.ActWorldBattleRs.EXT_FIELD_NUMBER, ActWorldBattleHandler.class);

		//领取月卡奖励
		registerC(ActivityPb.GetMonthCardAwardRq.EXT_FIELD_NUMBER, ActivityPb.GetMonthCardAwardRs.EXT_FIELD_NUMBER, GetMonthCardAwardHandler.class);
		registerC(ActivityPb.FixSevenLoginSignRq.EXT_FIELD_NUMBER, ActivityPb.FixSevenLoginSignRs.EXT_FIELD_NUMBER, FixSevenLoginSignHandler.class);
		//军备促销
		registerC(ActivityPb.ActPayArmsRq.EXT_FIELD_NUMBER, ActivityPb.ActPayArmsRs.EXT_FIELD_NUMBER, ActArmsPayHandler.class);
		registerC(ActivityPb.BuyPayArmsRq.EXT_FIELD_NUMBER, ActivityPb.BuyPayArmsRs.EXT_FIELD_NUMBER, BuyPayArmsHandler.class);
		registerC(ActivityPb.DoPayArmsAwardRq.EXT_FIELD_NUMBER, ActivityPb.DoPayArmsAwardRs.EXT_FIELD_NUMBER, DoPayArmsAwardHandler.class);
		//月卡礼包
		registerC(ActivityPb.ActMonthGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActMonthGiftRs.EXT_FIELD_NUMBER, ActMonthGiftHandler.class);
		//许愿池
		registerC(ActivityPb.ActHopeRq.EXT_FIELD_NUMBER, ActivityPb.ActHopeRs.EXT_FIELD_NUMBER, ActHopeHandler.class);
		registerC(ActivityPb.DoHopeRq.EXT_FIELD_NUMBER, ActivityPb.DoHopeRs.EXT_FIELD_NUMBER, DoHopeHandler.class);
		//装备精研
		registerC(ActivityPb.ActWashEquiptRq.EXT_FIELD_NUMBER, ActivityPb.ActWashEquiptRs.EXT_FIELD_NUMBER, ActWashEquipHandle.class);
		registerC(ActivityPb.DoWashEquiptRq.EXT_FIELD_NUMBER, ActivityPb.DoWashEquiptRs.EXT_FIELD_NUMBER, DoWashEquiptHandler.class);
		//英雄令
		registerC(ActivityPb.ActPassPortRq.EXT_FIELD_NUMBER, ActivityPb.ActPassPortRs.EXT_FIELD_NUMBER, ActPassPortHandler.class);
		registerC(ActivityPb.DoPassPortAwardRq.EXT_FIELD_NUMBER, ActivityPb.DoPassPortAwardRs.EXT_FIELD_NUMBER, DoPassPortAwardHandler.class);
		//建造礼包
		registerC(ActivityPb.getBuildGiftRq.EXT_FIELD_NUMBER, ActivityPb.getBuildGiftRs.EXT_FIELD_NUMBER, GetBuildGiftHandler.class);
		//大杀四方
		registerC(ActivityPb.ActKillAllRq.EXT_FIELD_NUMBER, ActivityPb.ActKillAllRs.EXT_FIELD_NUMBER, ActKillAllHandler.class);
		//每日远征
		registerC(ActivityPb.ActDailyExpeditionRq.EXT_FIELD_NUMBER, ActivityPb.ActDailyExpeditionRs.EXT_FIELD_NUMBER, ActDailyExpeditionHandler.class);
		//每日充值
		registerC(ActivityPb.ActDailyRechargeRq.EXT_FIELD_NUMBER, ActivityPb.ActDailyRechargeRs.EXT_FIELD_NUMBER, ActDailyRechargeHandler.class);

		//消费排行
		registerC(ActivityPb.ActGoldRankRq.EXT_FIELD_NUMBER, ActivityPb.ActGoldRankRs.EXT_FIELD_NUMBER, ActCostGoldRankHandler.class);
		//0元礼包
		registerC(ActivityPb.ActZeroGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActZeroGiftRs.EXT_FIELD_NUMBER, ActZeroGiftHandler.class);
		registerC(ActivityPb.DoActZeroGiftRq.EXT_FIELD_NUMBER, ActivityPb.DoActZeroGiftRs.EXT_FIELD_NUMBER, DoActZeroGiftHandler.class);
		//特训半价
		registerC(ActivityPb.ActHalfWashRq.EXT_FIELD_NUMBER, ActivityPb.ActHalfWashRs.EXT_FIELD_NUMBER, ActHalfWashHandler.class);
		//每日战役
		registerC(ActivityPb.ActDailyMissionRq.EXT_FIELD_NUMBER, ActivityPb.ActDailyMissionRs.EXT_FIELD_NUMBER, ActDailyMissionHandler.class);
		//开启建造队列礼包
		registerC(ActivityPb.ActOpenBuildGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActOpenBuildGiftRs.EXT_FIELD_NUMBER, ActOpenBuildGiftHandler.class);
		//导师排行
		registerC(ActivityPb.ActMasterRankRq.EXT_FIELD_NUMBER, ActivityPb.ActMasterRankRs.EXT_FIELD_NUMBER, ActMentorScoreRankHandler.class);
		//双蛋兑换
		registerC(ActivityPb.ActDoubleEggRq.EXT_FIELD_NUMBER, ActivityPb.ActDoubleEggRs.EXT_FIELD_NUMBER, ActDoubleEggHandler.class);
		registerC(ActivityPb.ActDoubleEggChangeRq.EXT_FIELD_NUMBER, ActivityPb.ActDoubleEggChangeRs.EXT_FIELD_NUMBER, ActDoubleEggChangeHandler.class);
		//双旦礼包
		registerC(ActivityPb.ActChrismasRq.EXT_FIELD_NUMBER, ActivityPb.ActChrismasRs.EXT_FIELD_NUMBER, ActChrismasHandler.class);
		registerC(ActivityPb.ActChrismasBuyRq.EXT_FIELD_NUMBER, ActivityPb.ActChrismasBuyRs.EXT_FIELD_NUMBER, ActChrismasBuyHandler.class);
		registerC(ActivityPb.ActChrismasRewardRq.EXT_FIELD_NUMBER, ActivityPb.ActChrismasRewardRs.EXT_FIELD_NUMBER, ActChrismasRewardHandler.class);
		registerC(ActivityPb.ActLuxuryGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActLuxuryGiftRs.EXT_FIELD_NUMBER, ActLuxuryGiftHandler.class);
		//购买通行证等级
		registerC(ActivityPb.BuyActPassPortLvRq.EXT_FIELD_NUMBER, ActivityPb.BuyActPassPortLvRs.EXT_FIELD_NUMBER, BuyActPassPortLvHandler.class);

		//查看魅影转盘信息
		registerC(ActivityPb.ActHeroDialRq.EXT_FIELD_NUMBER, ActivityPb.ActHeroDialRs.EXT_FIELD_NUMBER, ActHeroDialHandler.class);
		//魅影转抽抽取
		registerC(ActivityPb.DoHeroDialRq.EXT_FIELD_NUMBER, ActivityPb.DoHeroDialRs.EXT_FIELD_NUMBER, DoHeroDialHandler.class);
		//魅影转盘兑换
		registerC(ActivityPb.DoHeroExchangelRq.EXT_FIELD_NUMBER, ActivityPb.DoHeroExchangelRs.EXT_FIELD_NUMBER, actMyChangeHandler.class);
		registerC(PropPb.DoFragmentRq.EXT_FIELD_NUMBER, PropPb.DoFragmentRs.EXT_FIELD_NUMBER, DoFragmentHandler.class);
		//无畏尖兵
		registerC(ActivityPb.ActTaskHeroRq.EXT_FIELD_NUMBER, ActivityPb.ActTaskHeroRs.EXT_FIELD_NUMBER, ActTaskHeroHandler.class);
		registerC(ActivityPb.DoActTaskHeroRewardRq.EXT_FIELD_NUMBER, ActivityPb.DoActTaskHeroRewardRs.EXT_FIELD_NUMBER, DoActTaskHeroRewardHandler.class);
		registerC(ActivityPb.ActSuripriseGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActSuripriseGiftRs.EXT_FIELD_NUMBER, ActSuripriseGiftHandler.class);

		//充值转盘
		registerC(ActivityPb.RecharDialRq.EXT_FIELD_NUMBER, ActivityPb.RecharDialRs.EXT_FIELD_NUMBER, ActRecharDialInfoHandler.class);
		registerC(ActivityPb.DoRecharDialRq.EXT_FIELD_NUMBER, ActivityPb.DoRecharDialRs.EXT_FIELD_NUMBER, DoRecharDialHandler.class);

		//夺宝奇兵
		registerC(ActivityPb.RaidersRq.EXT_FIELD_NUMBER, ActivityPb.RaidersRs.EXT_FIELD_NUMBER, ActRaidersInfoHandler.class);
		registerC(ActivityPb.DoRaidersRq.EXT_FIELD_NUMBER, ActivityPb.DoRaidersRs.EXT_FIELD_NUMBER, DoRaidersHandler.class);

		//特价尊享
		registerC(ActivityPb.ActSpecialGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActSpecialGiftRs.EXT_FIELD_NUMBER, ActSpecialGiftHandler.class);
		//阵营骨干
		registerC(ActivityPb.ActCampMembersRankRq.EXT_FIELD_NUMBER, ActivityPb.ActCampMembersRankRs.EXT_FIELD_NUMBER, ActCampMembersRankHandler.class);
		//日常训练
		registerC(ActivityPb.ActDailyTrainRq.EXT_FIELD_NUMBER, ActivityPb.ActDailyTrainRs.EXT_FIELD_NUMBER, ActDailyTrainHandler.class);
		//获取订单活动
		registerC(ActivityPb.ActOrderRq.EXT_FIELD_NUMBER, ActivityPb.ActOrderRs.EXT_FIELD_NUMBER, ActOrderInfoHandler.class);
		//勇冠三军
		registerC(ActivityPb.ActWellCrownThreeArmyRq.EXT_FIELD_NUMBER, ActivityPb.ActWellCrownThreeArmyRs.EXT_FIELD_NUMBER, ActWellCrownThreeArmyHandler.class);
		//累计充值
		registerC(ActivityPb.ActGrandRecharegRq.EXT_FIELD_NUMBER, ActivityPb.ActGrandRecharegRs.EXT_FIELD_NUMBER, ActGrandRecharegHandler.class);

		//好运转盘相关
		registerC(ActivityPb.ActLucklyDialRq.EXT_FIELD_NUMBER, ActivityPb.ActLucklyDialRs.EXT_FIELD_NUMBER, ActLucklyInfoHandler.class);
		registerC(ActivityPb.DoLucklyDialRq.EXT_FIELD_NUMBER, ActivityPb.DoLucklyDialRs.EXT_FIELD_NUMBER, ActLucklyAwardHandler.class);
		//资源采集
		registerC(ActivityPb.actCollectionResourceRq.EXT_FIELD_NUMBER, ActivityPb.actCollectionResourceRs.EXT_FIELD_NUMBER, ActCollectionResourceHandler.class);

		//物质搜寻
		registerC(ActivityPb.ActMaterInfoRq.EXT_FIELD_NUMBER, ActivityPb.ActMaterInfoRs.EXT_FIELD_NUMBER, ActMaterInfoHandler.class);
		registerC(ActivityPb.ActMaterAwardRq.EXT_FIELD_NUMBER, ActivityPb.ActMaterAwardRs.EXT_FIELD_NUMBER, ActMaterAwardHandler.class);

		registerC(ActivityPb.ActMonsterRq.EXT_FIELD_NUMBER, ActivityPb.ActMonsterRs.EXT_FIELD_NUMBER, ActMonsterHandler.class);
		registerC(ActivityPb.ActPassPortPorAwardRq.EXT_FIELD_NUMBER, ActivityPb.ActPassPortPorAwardRs.EXT_FIELD_NUMBER, ActPassPortPorAwardRqHandler.class);
		//领取体力补给
		registerC(ActivityPb.GetActPowerRq.EXT_FIELD_NUMBER, ActivityPb.GetActPowerRs.EXT_FIELD_NUMBER, GetActPowerHandler.class);
		// 材料置换
		registerC(ActivityPb.GetMaterialSubstitutionRq.EXT_FIELD_NUMBER, ActivityPb.GetMaterialSubstitutionRs.EXT_FIELD_NUMBER, GetMaterialSubstitutionHandler.class);
		registerC(ActivityPb.MaterialSubstitutionRq.EXT_FIELD_NUMBER, ActivityPb.MaterialSubstitutionRs.EXT_FIELD_NUMBER, MaterialSubstitutionHandler.class);
		//母巢助力
		registerC(ActivityPb.BroodActRq.EXT_FIELD_NUMBER, ActivityPb.BroodActRs.EXT_FIELD_NUMBER, BroodActHandler.class);
		// 春节活动
		registerC(ActivityPb.ActSpringAwardRq.EXT_FIELD_NUMBER, ActivityPb.ActSpringAwardRs.EXT_FIELD_NUMBER, ActSpringAwardHandler.class);
		registerC(ActivityPb.ActSpringTurntableRq.EXT_FIELD_NUMBER, ActivityPb.ActSpringTurntableRs.EXT_FIELD_NUMBER, ActSpringTurntableHandler.class);
		registerC(ActivityPb.ActSpringRechargeRq.EXT_FIELD_NUMBER, ActivityPb.ActSpringRechargeRs.EXT_FIELD_NUMBER, ActSpringRechargeHandler.class);
		registerC(ActivityPb.DoSpringTurntableRq.EXT_FIELD_NUMBER, ActivityPb.DoSpringTurntableRs.EXT_FIELD_NUMBER, DoSpringTurntableHandler.class);
		registerC(ActivityPb.ReceiveSpringFestivalRq.EXT_FIELD_NUMBER, ActivityPb.ReceiveSpringFestivalRs.EXT_FIELD_NUMBER, ReceiveSpringFestivalHandler.class);
		registerC(ActivityPb.BuyLanternRq.EXT_FIELD_NUMBER, ActivityPb.BuyLanternRs.EXT_FIELD_NUMBER, BuyLanternHandler.class);
		registerC(ActivityPb.ActSpringGiftRq.EXT_FIELD_NUMBER, ActivityPb.ActSpringGiftRs.EXT_FIELD_NUMBER, ActSpringGiftHandler.class);
		registerC(ActivityPb.GetTDTaskRq.EXT_FIELD_NUMBER, ActivityPb.GetTDTaskRs.EXT_FIELD_NUMBER, GetTDTaskHandler.class);
		registerC(ActivityPb.TDTaskAwardRq.EXT_FIELD_NUMBER, ActivityPb.TDTaskAwardRs.EXT_FIELD_NUMBER, TDTaskAwardHandler.class);
	}

	public void registerPvpBattle() {
		registerC(MassActionRq.EXT_FIELD_NUMBER, MassActionRs.EXT_FIELD_NUMBER, MassHeroHandler.class);
		registerC(GoActionRq.EXT_FIELD_NUMBER, GoActionRs.EXT_FIELD_NUMBER, GoActionHandler.class);
		registerC(FightActionRq.EXT_FIELD_NUMBER, FightActionRs.EXT_FIELD_NUMBER, FightActionHandler.class);
		registerC(SaveActionRq.EXT_FIELD_NUMBER, SaveActionRs.EXT_FIELD_NUMBER, SaveActionHandler.class);
		registerC(SoloActionRq.EXT_FIELD_NUMBER, SoloActionRs.EXT_FIELD_NUMBER, SoloActionHandler.class);
		registerC(GetPvpInfoRq.EXT_FIELD_NUMBER, GetPvpInfoRs.EXT_FIELD_NUMBER, GetPvpInfoHandler.class);
		registerC(AttendPvpRq.EXT_FIELD_NUMBER, AttendPvpRs.EXT_FIELD_NUMBER, AttendPvpHandler.class);
		registerC(GetPvpRankInfoRq.EXT_FIELD_NUMBER, GetPvpRankInfoRs.EXT_FIELD_NUMBER, GetPvpRankHandler.class);
		registerC(BuyWineRq.EXT_FIELD_NUMBER, BuyWineRs.EXT_FIELD_NUMBER, BuyWineHandler.class);
		registerC(DigPropRq.EXT_FIELD_NUMBER, DigPropRs.EXT_FIELD_NUMBER, DigPropHandler.class);
		registerC(GetGreetRq.EXT_FIELD_NUMBER, GetGreetRs.EXT_FIELD_NUMBER, GetGreetHandler.class);
		registerC(ExchangeRq.EXT_FIELD_NUMBER, ExchangeRs.EXT_FIELD_NUMBER, ExchangePvpItemHandler.class);
		registerC(GreetActionRq.EXT_FIELD_NUMBER, GreetActionRs.EXT_FIELD_NUMBER, GreetActionHandler.class);
	}

	public void registerStaff() {
		registerC(OpenStaffTaskRq.EXT_FIELD_NUMBER, OpenStaffTaskRs.EXT_FIELD_NUMBER, OpenStaffTaskHandler.class);
		registerC(GetStaffTaskRq.EXT_FIELD_NUMBER, GetStaffTaskRs.EXT_FIELD_NUMBER, GetStaffTaskHandler.class);
		registerC(ActivateTaskRq.EXT_FIELD_NUMBER, ActivateTaskRs.EXT_FIELD_NUMBER, ActivateTaskHandler.class);
	}

	public void registerRiot() {
		registerC(RiotWarHelpRq.EXT_FIELD_NUMBER, RiotWarHelpRs.EXT_FIELD_NUMBER, RiotWarHelpHandler.class);
		registerC(RiotPb.RiotItemShopRq.EXT_FIELD_NUMBER, RiotPb.RiotItemShopRs.EXT_FIELD_NUMBER, RiotItemShopHandler.class);
		registerC(RiotPb.RiotItemShopBuyRq.EXT_FIELD_NUMBER, RiotPb.RiotItemShopBuyRs.EXT_FIELD_NUMBER, RiotItemShopBuyHandler.class);
		registerC(RiotPb.RiotScoreShopBuyRq.EXT_FIELD_NUMBER, RiotPb.RiotScoreShopBuyRs.EXT_FIELD_NUMBER, RiotScoreShopBuyHandler.class);
		registerC(RiotPb.AttendRiotCityRq.EXT_FIELD_NUMBER, RiotPb.AttendRiotCityRs.EXT_FIELD_NUMBER, AttendRiotCityHandler.class);
	}

	public void registerCastle() {
		registerC(CastlePb.GetMiningHeroListRq.EXT_FIELD_NUMBER, CastlePb.GetMiningHeroListRs.EXT_FIELD_NUMBER, GetMiningHeroListHandler.class);
		registerC(CastlePb.MiningUpRq.EXT_FIELD_NUMBER, CastlePb.MiningUpRs.EXT_FIELD_NUMBER, UpMiningHeroHandler.class);
		registerC(CastlePb.MiningDownRq.EXT_FIELD_NUMBER, CastlePb.MiningDownRs.EXT_FIELD_NUMBER, DownMiningHeroHandler.class);
		registerC(CastlePb.GetMeetingTaskRq.EXT_FIELD_NUMBER, CastlePb.GetMeetingTaskRs.EXT_FIELD_NUMBER, GetMeetingTaskHandler.class);
		registerC(CastlePb.OpenMeetingTaskRq.EXT_FIELD_NUMBER, CastlePb.OpenMeetingTaskRs.EXT_FIELD_NUMBER, OpenMeetingTaskHandler.class);
		registerC(CastlePb.OpenPointSoldiersRq.EXT_FIELD_NUMBER, CastlePb.OpenPointSoldiersRs.EXT_FIELD_NUMBER, OpenPointSoldiersHandler.class);
		registerC(CastlePb.GetSoldierLineRq.EXT_FIELD_NUMBER, CastlePb.GetSoldierLineRs.EXT_FIELD_NUMBER, GetSoldierLineHandler.class);
		registerC(CastlePb.OpenNextStepRq.EXT_FIELD_NUMBER, CastlePb.OpenNextStepRs.EXT_FIELD_NUMBER, OpenNextStepHandler.class);
		registerC(CastlePb.UpdateDefenseSoldierRq.EXT_FIELD_NUMBER, CastlePb.UpdateDefenseSoldierRs.EXT_FIELD_NUMBER, UpdateDefenseSoldierHandler.class);
		registerC(CastlePb.BuyDefenseSoldiersRq.EXT_FIELD_NUMBER, CastlePb.BuyDefenseSoldiersRs.EXT_FIELD_NUMBER, BuyDefenseSoldiersHandler.class);
	}

	public void registerRebel() {
		registerC(RebelPb.UseRebelPropRq.EXT_FIELD_NUMBER, RebelPb.UseRebelPropRs.EXT_FIELD_NUMBER, UseRebelPropHandler.class);
		registerC(RebelPb.ExchangeRebelAwardRq.EXT_FIELD_NUMBER, RebelPb.ExchangeRebelAwardRs.EXT_FIELD_NUMBER, ExchangeRebelAwardHandler.class);
		registerC(RebelPb.GetExchangeInfoRq.EXT_FIELD_NUMBER, RebelPb.GetExchangeInfoRs.EXT_FIELD_NUMBER, GetExchangeInfoHandler.class);
		registerC(RebelPb.RebelGuideAwardRq.EXT_FIELD_NUMBER, RebelPb.RebelGuideAwardRs.EXT_FIELD_NUMBER, RebelGuideAwardHandler.class);
	}

	public void registFriend() {
		registerC(FriendPb.AddFriendRq.EXT_FIELD_NUMBER, FriendPb.AddFriendRs.EXT_FIELD_NUMBER, AddFriendHandler.class);
		registerC(FriendPb.ApplyFriendRq.EXT_FIELD_NUMBER, FriendPb.ApplyFriendRs.EXT_FIELD_NUMBER, ApplyFriendHandler.class);
		registerC(FriendPb.FastApplyFriendRq.EXT_FIELD_NUMBER, FriendPb.FastApplyFriendRs.EXT_FIELD_NUMBER, FastApplyFriendHandler.class);
		registerC(FriendPb.FriendListRq.EXT_FIELD_NUMBER, FriendPb.FriendListRs.EXT_FIELD_NUMBER, GetFriendListHandler.class);
		registerC(FriendPb.GetMasterAwardRq.EXT_FIELD_NUMBER, FriendPb.GetMasterAwardRs.EXT_FIELD_NUMBER, GetMasterAwardHandler.class);
		registerC(FriendPb.GetMasterShopAwardRq.EXT_FIELD_NUMBER, FriendPb.GetMasterShopAwardRs.EXT_FIELD_NUMBER, GetMasterShopAwardHandler.class);
		registerC(FriendPb.MasterAwardRq.EXT_FIELD_NUMBER, FriendPb.MasterAwardRs.EXT_FIELD_NUMBER, MasterAwardHandler.class);
		registerC(FriendPb.MasterShopAwardRq.EXT_FIELD_NUMBER, FriendPb.MasterShopAwardRs.EXT_FIELD_NUMBER, MasterShopAwardHandler.class);
		registerC(FriendPb.RemoveFriendRq.EXT_FIELD_NUMBER, FriendPb.RemoveFriendRs.EXT_FIELD_NUMBER, RemoveFriendHandler.class);
		registerC(FriendPb.SearchFriendRq.EXT_FIELD_NUMBER, FriendPb.SearchFriendRs.EXT_FIELD_NUMBER, SearchFriendHandler.class);
		registerC(FriendPb.GetInviteCompanionListRq.EXT_FIELD_NUMBER, FriendPb.GetInviteCompanionListRs.EXT_FIELD_NUMBER, GetInviteCompanionListRqHandler.class);
		registerC(FriendPb.DoInviteCompanionRq.EXT_FIELD_NUMBER, FriendPb.DoInviteCompanionRs.EXT_FIELD_NUMBER, DoInviteCompanionRqHandler.class);
		registerC(FriendPb.RemoveMasterRq.EXT_FIELD_NUMBER, FriendPb.RemoveMasterRs.EXT_FIELD_NUMBER, RemoveMasterRqHandler.class);
		registerC(FriendPb.ProcessAllRq.EXT_FIELD_NUMBER, FriendPb.ProcessAllRs.EXT_FIELD_NUMBER, ProcessAllHandler.class);
		registerC(FriendPb.SearchRq.EXT_FIELD_NUMBER, FriendPb.SearchRs.EXT_FIELD_NUMBER, SearchFriHandler.class);
	}

	public void registFirstBlood() {
		registerC(FirstBloodPb.CityFirstBloodRq.EXT_FIELD_NUMBER, FirstBloodPb.CityFirstBloodRs.EXT_FIELD_NUMBER, GetFirstBloodInfoHandler.class);
		registerC(FirstBloodPb.AllFirstBloodRq.EXT_FIELD_NUMBER, FirstBloodPb.AllFirstBloodRs.EXT_FIELD_NUMBER, GetAllFirstBloodInfoHandler.class);
	}

	public void registerFishing() {
		registerC(FishingPb.GetReachBaitAtlasRq.EXT_FIELD_NUMBER, FishingPb.GetReachBaitAtlasRs.EXT_FIELD_NUMBER, GetReachBaitAtlasHandler.class);
		registerC(FishingPb.GetReachFishAtlasRq.EXT_FIELD_NUMBER, FishingPb.GetReachFishAtlasRs.EXT_FIELD_NUMBER, GetReachFishAtlasHandler.class);
		registerC(FishingPb.GetHeroGroupConfigRq.EXT_FIELD_NUMBER, FishingPb.GetHeroGroupConfigRs.EXT_FIELD_NUMBER, GetHeroGroupConfigHandler.class);
		registerC(FishingPb.GetFishingLevelConfigRq.EXT_FIELD_NUMBER, FishingPb.GetFishingLevelConfigRs.EXT_FIELD_NUMBER, GetFishingLevelConfigHandler.class);
		registerC(FishingPb.GetPointsShopConfigRq.EXT_FIELD_NUMBER, FishingPb.GetPointsShopConfigRs.EXT_FIELD_NUMBER, GetPointsShopConfigHandler.class);
		registerC(FishingPb.GetPlayerFishingDataRq.EXT_FIELD_NUMBER, FishingPb.GetPlayerFishingDataRs.EXT_FIELD_NUMBER, GetPlayerFishingDataHandler.class);
		registerC(FishingPb.GetFishingTeamQueueRq.EXT_FIELD_NUMBER, FishingPb.GetFishingTeamQueueRs.EXT_FIELD_NUMBER, GetFishingTeamQueueHandler.class);
		registerC(FishingPb.PickHeroRq.EXT_FIELD_NUMBER, FishingPb.PickHeroRs.EXT_FIELD_NUMBER, PickHeroHandler.class);
		registerC(FishingPb.DispatchTeamRq.EXT_FIELD_NUMBER, FishingPb.DispatchTeamRs.EXT_FIELD_NUMBER, DispatchTeamHandler.class);
		registerC(FishingPb.GetBaitsRq.EXT_FIELD_NUMBER, FishingPb.GetBaitsRs.EXT_FIELD_NUMBER, GetBaitsHandler.class);
		registerC(FishingPb.ThrowPoleRq.EXT_FIELD_NUMBER, FishingPb.ThrowPoleRs.EXT_FIELD_NUMBER, ThrowPoleHandler.class);
		registerC(FishingPb.TakeBackPoleRq.EXT_FIELD_NUMBER, FishingPb.TakeBackPoleRs.EXT_FIELD_NUMBER, TakeBackPoleHandler.class);
		registerC(FishingPb.GetFishRecordRq.EXT_FIELD_NUMBER, FishingPb.GetFishRecordRs.EXT_FIELD_NUMBER, GetFishRecordHandler.class);
		registerC(FishingPb.ShareFishRecordRq.EXT_FIELD_NUMBER, FishingPb.ShareFishRecordRs.EXT_FIELD_NUMBER, ShareFishRecordHandler.class);
		registerC(FishingPb.LookFishRecordRq.EXT_FIELD_NUMBER, FishingPb.LookFishRecordRs.EXT_FIELD_NUMBER, LookFishRecordHandler.class);
		registerC(FishingPb.GetFishAtlasAwardRq.EXT_FIELD_NUMBER, FishingPb.GetFishAtlasAwardRs.EXT_FIELD_NUMBER, GetFishAtlasAwardHandler.class);
		registerC(FishingPb.PointsExchangeRq.EXT_FIELD_NUMBER, FishingPb.PointsExchangeRs.EXT_FIELD_NUMBER, PointsExchangeHandler.class);

	}


	public void registerMapInfo() {
		registerC(GetMapNpcRq.EXT_FIELD_NUMBER, GetMapNpcRs.EXT_FIELD_NUMBER, GetMapNpcHandler.class);
		registerC(RobotRepairRq.EXT_FIELD_NUMBER, RobotRepairRs.EXT_FIELD_NUMBER, RobotRepairHandler.class);
	}

	public void registerApp() {
		registerC(RegisterRq.EXT_FIELD_NUMBER, RegisterRs.EXT_FIELD_NUMBER, RegisterHandler.class);
	}


	public void registerChannel() {
		registerC(ChannelOfflineRq.EXT_FIELD_NUMBER, 0, ChannelOfflineHandler.class);
	}

	public void registerAchievement() {
		registerC(ActivityPb.AchievementRq.EXT_FIELD_NUMBER, ActivityPb.AchievementRs.EXT_FIELD_NUMBER, AchievementInfoHandler.class);
		registerC(ActivityPb.AchievementInfoRq.EXT_FIELD_NUMBER, ActivityPb.AchievementInfoRs.EXT_FIELD_NUMBER, AchievementDetailHandler.class);
		registerC(ActivityPb.AchievementBoxAwardRq.EXT_FIELD_NUMBER, ActivityPb.AchievementBoxAwardRs.EXT_FIELD_NUMBER, AchievementBoxAwardHandler.class);
		registerC(ActivityPb.AchievementInfoAwardRq.EXT_FIELD_NUMBER, ActivityPb.AchievementInfoAwardRs.EXT_FIELD_NUMBER, AchievementDetailAwardHandler.class);
		registerC(RankPb.GetAchiRankRq.EXT_FIELD_NUMBER, RankPb.GetAchiRankRs.EXT_FIELD_NUMBER, GetAchievementRankHandler.class);

	}
}
