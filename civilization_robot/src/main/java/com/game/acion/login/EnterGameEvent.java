package com.game.acion.login;

import com.game.define.LoadData;
import com.game.domain.Robot;
import com.game.load.ILoadData;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.ActivityPb.ActDayPayRq;
import com.game.pb.ActivityPb.ActDayPayRs;
import com.game.pb.ActivityPb.ActMonthCardRq;
import com.game.pb.ActivityPb.ActMonthCardRs;
import com.game.pb.ActivityPb.ActOnlineTimeRq;
import com.game.pb.ActivityPb.ActOnlineTimeRs;
import com.game.pb.ActivityPb.ActPayFirstRq;
import com.game.pb.ActivityPb.ActPayFirstRs;
import com.game.pb.ActivityPb.ActSevenLoginRq;
import com.game.pb.ActivityPb.ActSevenLoginRs;
import com.game.pb.ActivityPb.ActSpecialGiftRq;
import com.game.pb.ActivityPb.ActSpecialGiftRs;
import com.game.pb.ActivityPb.GetActivityListRq;
import com.game.pb.ActivityPb.GetActivityListRs;
import com.game.pb.ActivityPb.GetWonderfulListRq;
import com.game.pb.ActivityPb.GetWonderfulListRs;
import com.game.pb.BasePb.Base;
import com.game.pb.BeautyPb.NewGetBeautyListRq;
import com.game.pb.BeautyPb.NewGetBeautyListRs;
import com.game.pb.BroodWarPb.AppointInfoRq;
import com.game.pb.BroodWarPb.AppointInfoRs;
import com.game.pb.BroodWarPb.BroodWarInitRq;
import com.game.pb.BroodWarPb.BroodWarInitRs;
import com.game.pb.BuildingPb.GetBuildingRq;
import com.game.pb.BuildingPb.GetBuildingRs;
import com.game.pb.CastlePb.GetSoldierLineRq;
import com.game.pb.CastlePb.GetSoldierLineRs;
import com.game.pb.ChatPb.GetChatRq;
import com.game.pb.ChatPb.GetChatRs;
import com.game.pb.ChatPb.GetPersonChatRoomRq;
import com.game.pb.ChatPb.GetPersonChatRoomRs;
import com.game.pb.CountryPb.GetCountryGloryRq;
import com.game.pb.CountryPb.GetCountryGloryRs;
import com.game.pb.CountryPb.GetCountryNameRq;
import com.game.pb.CountryPb.GetCountryNameRs;
import com.game.pb.CountryPb.GetCountryRq;
import com.game.pb.CountryPb.GetCountryRs;
import com.game.pb.CountryPb.GetCountryTaskRq;
import com.game.pb.CountryPb.GetCountryTaskRs;
import com.game.pb.CountryPb.GetCountryWarRq;
import com.game.pb.CountryPb.GetCountryWarRs;
import com.game.pb.CountryPb.GetGovernRq;
import com.game.pb.CountryPb.GetGovernRs;
import com.game.pb.DepotPb.GetResourcePacketRq;
import com.game.pb.DepotPb.GetResourcePacketRs;
import com.game.pb.EquipPb.GetEquipBagRq;
import com.game.pb.EquipPb.GetEquipBagRs;
import com.game.pb.FlameWarPb.FlameBagRq;
import com.game.pb.FlameWarPb.FlameBagRs;
import com.game.pb.FlameWarPb.FlameWarInitRq;
import com.game.pb.FlameWarPb.FlameWarInitRs;
import com.game.pb.HeroPb.GetHeroRq;
import com.game.pb.HeroPb.GetHeroRs;
import com.game.pb.JourneyPb.GetAllJourneyRq;
import com.game.pb.JourneyPb.GetAllJourneyRs;
import com.game.pb.KillEquipPb.GetKillEquipRq;
import com.game.pb.KillEquipPb.GetKillEquipRs;
import com.game.pb.MailPb.BlackListRq;
import com.game.pb.MailPb.BlackListRs;
import com.game.pb.MailPb.GetCountryMailRq;
import com.game.pb.MailPb.GetCountryMailRs;
import com.game.pb.MailPb.GetMailCountRq;
import com.game.pb.MailPb.GetMailCountRs;
import com.game.pb.MailPb.GetMailRq;
import com.game.pb.MailPb.GetMailRs;
import com.game.pb.MissionPb.GetAllMissionRq;
import com.game.pb.MissionPb.GetAllMissionRs;
import com.game.pb.OmamentPb.GetOmamentBagRq;
import com.game.pb.OmamentPb.GetOmamentBagRs;
import com.game.pb.OmamentPb.GetOmamentDeressRq;
import com.game.pb.OmamentPb.GetOmamentDeressRs;
import com.game.pb.PropPb.GetPropBagRq;
import com.game.pb.PropPb.GetPropBagRs;
import com.game.pb.PvpBattlePb.GetGreetRq;
import com.game.pb.PvpBattlePb.GetGreetRs;
import com.game.pb.PvpBattlePb.GetPvpInfoRq;
import com.game.pb.PvpBattlePb.GetPvpInfoRs;
import com.game.pb.RebelPb.GetExchangeInfoRq;
import com.game.pb.RebelPb.GetExchangeInfoRs;
import com.game.pb.RolePb.EnterGameRq;
import com.game.pb.RolePb.EnterGameRs;
import com.game.pb.RolePb.GetTimeRq;
import com.game.pb.RolePb.GetTimeRs;
import com.game.pb.RolePb.getFrameRq;
import com.game.pb.RolePb.getFrameRs;
import com.game.pb.ShopPb.GetShopRq;
import com.game.pb.ShopPb.GetShopRs;
import com.game.pb.ShopPb.GetVipGiftRq;
import com.game.pb.ShopPb.GetVipGiftRs;
import com.game.pb.ShopPb.GetVipShopRq;
import com.game.pb.ShopPb.GetVipShopRs;
import com.game.pb.SkinPb.GetCommandSkinRq;
import com.game.pb.SkinPb.GetCommandSkinRs;
import com.game.pb.TDPb.EndlessTowerDefenseInitRq;
import com.game.pb.TDPb.EndlessTowerDefenseInitRs;
import com.game.pb.TDPb.TDMapInitRq;
import com.game.pb.TDPb.TDMapInitRs;
import com.game.pb.TDPb.TDTowerInitRq;
import com.game.pb.TDPb.TDTowerInitRs;
import com.game.pb.TaskPb.GetTaskRq;
import com.game.pb.TaskPb.GetTaskRs;
import com.game.pb.TechPb.GetTechRq;
import com.game.pb.TechPb.GetTechRs;
import com.game.pb.WallPb.GetWallInfoRq;
import com.game.pb.WallPb.GetWallInfoRs;
import com.game.pb.WarBookPb.GetWarBookBagRq;
import com.game.pb.WarBookPb.GetWarBookBagRs;
import com.game.pb.WorkShopPb.GetWsQueRq;
import com.game.pb.WorkShopPb.GetWsQueRs;
import com.game.pb.WorldBoxPb.GetWorldBoxRq;
import com.game.pb.WorldBoxPb.GetWorldBoxRs;
import com.game.pb.WorldPb.AutoKillMonsterRq;
import com.game.pb.WorldPb.AutoKillMonsterRs;
import com.game.pb.WorldPb.GetBigMonsterActivityRq;
import com.game.pb.WorldPb.GetBigMonsterActivityRs;
import com.game.pb.WorldPb.GetCityRq;
import com.game.pb.WorldPb.GetCityRs;
import com.game.pb.WorldPb.GetPvpCityRq;
import com.game.pb.WorldPb.GetPvpCityRs;
import com.game.pb.WorldPb.GetRebelWarRq;
import com.game.pb.WorldPb.GetRebelWarRs;
import com.game.pb.WorldPb.GetWorldBossInfoRq;
import com.game.pb.WorldPb.GetWorldBossInfoRs;
import com.game.pb.WorldPb.GetWorldTargetTaskRq;
import com.game.pb.WorldPb.GetWorldTargetTaskRs;
import com.game.pb.WorldPb.WorldActivityPlanRq;
import com.game.pb.WorldPb.WorldActivityPlanRs;
import com.game.util.BasePbHelper;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 *
 * @Description 登录成功后，进行相关角色的数据拉取事件
 * @Date 2022/10/20 14:35
 **/

@LoadData(name = "进入游戏", initSeq = 2000)
@Component
public class EnterGameEvent implements ILoadData {

	private List<EnterGameAction> loginActions = new ArrayList<>();

	@Override
	public void load() {
		// 拉取聊天消息
		createAction(GetChatRq.EXT_FIELD_NUMBER, GetChatRs.EXT_FIELD_NUMBER, GetChatRq.ext, GetChatRq.newBuilder().build());
		createAction(ActPayFirstRq.EXT_FIELD_NUMBER, ActPayFirstRs.EXT_FIELD_NUMBER, ActPayFirstRq.ext, ActPayFirstRq.newBuilder().build());
		createAction(getFrameRq.EXT_FIELD_NUMBER, getFrameRs.EXT_FIELD_NUMBER, getFrameRq.ext, getFrameRq.newBuilder().build());
		//获取建筑信息请求
		createAction(GetBuildingRq.EXT_FIELD_NUMBER, GetBuildingRs.EXT_FIELD_NUMBER, GetBuildingRq.ext, GetBuildingRq.newBuilder().build());
		//获取任务信息
		createAction(GetTaskRq.EXT_FIELD_NUMBER, GetTaskRs.EXT_FIELD_NUMBER, GetTaskRq.ext, GetTaskRq.newBuilder().build());

		//获取阵营任务信息
		createAction(GetCountryTaskRq.EXT_FIELD_NUMBER, GetCountryTaskRs.EXT_FIELD_NUMBER, GetCountryTaskRq.ext, GetCountryTaskRq.newBuilder().build());
		//活动列表数据请求
		createAction(GetActivityListRq.EXT_FIELD_NUMBER, GetActivityListRs.EXT_FIELD_NUMBER, GetActivityListRq.ext, GetActivityListRq.newBuilder().build());
		//福利列表数据请求
		createAction(GetWonderfulListRq.EXT_FIELD_NUMBER, GetWonderfulListRs.EXT_FIELD_NUMBER, GetWonderfulListRq.ext, GetWonderfulListRq.newBuilder().build());
//		createAction(GetMeetingTaskRq.EXT_FIELD_NUMBER, GetMeetingTaskRs.EXT_FIELD_NUMBER, GetMeetingTaskRq.ext, GetMeetingTaskRq.newBuilder().build());
		createAction(ActSevenLoginRq.EXT_FIELD_NUMBER, ActSevenLoginRs.EXT_FIELD_NUMBER, ActSevenLoginRq.ext, ActSevenLoginRq.newBuilder().build());
		createAction(GetWorldBoxRq.EXT_FIELD_NUMBER, GetWorldBoxRs.EXT_FIELD_NUMBER, GetWorldBoxRq.ext, GetWorldBoxRq.newBuilder().build());
		//ActivityCache
		//请求世界boss信息
		createAction(GetWorldBossInfoRq.EXT_FIELD_NUMBER, GetWorldBossInfoRs.EXT_FIELD_NUMBER, GetWorldBossInfoRq.ext, GetWorldBossInfoRq.newBuilder().build());
		//世界活动请求
		createAction(WorldActivityPlanRq.EXT_FIELD_NUMBER, WorldActivityPlanRs.EXT_FIELD_NUMBER, WorldActivityPlanRq.ext, WorldActivityPlanRq.newBuilder().build());
//		Network.WorldActivityPlanRequest();
		//ChatCache
		//聊天黑名单数据请求
		createAction(BlackListRq.EXT_FIELD_NUMBER, BlackListRs.EXT_FIELD_NUMBER, BlackListRq.ext, BlackListRq.newBuilder().build());
		//私聊数据请求
		createAction(GetPersonChatRoomRq.EXT_FIELD_NUMBER, GetPersonChatRoomRs.EXT_FIELD_NUMBER, GetPersonChatRoomRq.ext, GetPersonChatRoomRq.newBuilder().build());

		//CountryCache
		//阵营基本信息请求
		createAction(GetCountryRq.EXT_FIELD_NUMBER, GetCountryRs.EXT_FIELD_NUMBER, GetCountryRq.ext, GetCountryRq.newBuilder().build());
		//获取阵营的名字
		createAction(GetCountryNameRq.EXT_FIELD_NUMBER, GetCountryNameRs.EXT_FIELD_NUMBER, GetCountryNameRq.ext, GetCountryNameRq.newBuilder().build());
		//请求国家荣誉
		createAction(GetCountryGloryRq.EXT_FIELD_NUMBER, GetCountryGloryRs.EXT_FIELD_NUMBER, GetCountryGloryRq.ext, GetCountryGloryRq.newBuilder().build());
		//请求国家官员
		createAction(GetGovernRq.EXT_FIELD_NUMBER, GetGovernRs.EXT_FIELD_NUMBER, GetGovernRq.ext, GetGovernRq.newBuilder().build());
		//请求国家战争
		createAction(GetCountryWarRq.EXT_FIELD_NUMBER, GetCountryWarRs.EXT_FIELD_NUMBER, GetCountryWarRq.ext, GetCountryWarRq.newBuilder().build());

		//DepotCache
		//拿到资源打包信息请求
		createAction(GetResourcePacketRq.EXT_FIELD_NUMBER, GetResourcePacketRs.EXT_FIELD_NUMBER, GetResourcePacketRq.ext, GetResourcePacketRq.newBuilder().build());
		//EquipCache
		//获取背包装备数据
		createAction(GetEquipBagRq.EXT_FIELD_NUMBER, GetEquipBagRs.EXT_FIELD_NUMBER, GetEquipBagRq.ext, GetEquipBagRq.newBuilder().build());
		//HeroCache
		//请求获取已有英雄
		createAction(GetHeroRq.EXT_FIELD_NUMBER, GetHeroRs.EXT_FIELD_NUMBER, GetHeroRq.ext, GetHeroRq.newBuilder().build());
		//拿兵排数加成请求
		createAction(GetSoldierLineRq.EXT_FIELD_NUMBER, GetSoldierLineRs.EXT_FIELD_NUMBER, GetSoldierLineRq.ext, GetSoldierLineRq.newBuilder().build());
		//JourneyCache
		//请求征途关卡信息
		createAction(GetAllJourneyRq.EXT_FIELD_NUMBER, GetAllJourneyRs.EXT_FIELD_NUMBER, GetAllJourneyRq.ext, GetAllJourneyRq.newBuilder().build());

		//KillEquipCache
		//获取神器列表
		createAction(GetKillEquipRq.EXT_FIELD_NUMBER, GetKillEquipRs.EXT_FIELD_NUMBER, GetKillEquipRq.ext, GetKillEquipRq.newBuilder().build());
		//MailCache
		//获取邮件页数
		createAction(GetMailCountRq.EXT_FIELD_NUMBER, GetMailCountRs.EXT_FIELD_NUMBER, GetMailCountRq.ext, GetMailCountRq.newBuilder().build());
		//获取发送阵营邮件所需要钻石
		createAction(GetCountryMailRq.EXT_FIELD_NUMBER, GetCountryMailRs.EXT_FIELD_NUMBER, GetCountryMailRq.ext, GetCountryMailRq.newBuilder().build());
		//OmamentCache
		//获取配饰数据
		createAction(GetOmamentBagRq.EXT_FIELD_NUMBER, GetOmamentBagRs.EXT_FIELD_NUMBER, GetOmamentBagRq.ext, GetOmamentBagRq.newBuilder().build());
		//获取玩家穿戴的配饰
		createAction(GetOmamentDeressRq.EXT_FIELD_NUMBER, GetOmamentDeressRs.EXT_FIELD_NUMBER, GetOmamentDeressRq.ext, GetOmamentDeressRq.newBuilder().build());
		createGetMailAction(GetMailRq.EXT_FIELD_NUMBER, GetMailRs.EXT_FIELD_NUMBER, GetMailRq.ext, GetMailRq.newBuilder().build());

		//PvpBattleCache
		//获取血战战斗信息
		createAction(GetPvpInfoRq.EXT_FIELD_NUMBER, GetPvpInfoRs.EXT_FIELD_NUMBER, GetPvpInfoRq.ext, GetPvpInfoRq.newBuilder().build());
		//获取恭贺或者买酒套话信息
		createAction(GetGreetRq.EXT_FIELD_NUMBER, GetGreetRs.EXT_FIELD_NUMBER, GetGreetRq.ext, GetGreetRq.newBuilder().build());
		//RoleCache
		//世界目标任务请求
		createAction(GetWorldTargetTaskRq.EXT_FIELD_NUMBER, GetWorldTargetTaskRs.EXT_FIELD_NUMBER, GetWorldTargetTaskRq.ext, GetWorldTargetTaskRq.newBuilder().build());
		//获取主城皮肤列表
		createAction(GetCommandSkinRq.EXT_FIELD_NUMBER, GetCommandSkinRs.EXT_FIELD_NUMBER, GetCommandSkinRq.ext, GetCommandSkinRq.newBuilder().build());

		//MissionCache
		//获取副本信息
		createAction(GetAllMissionRq.EXT_FIELD_NUMBER, GetAllMissionRs.EXT_FIELD_NUMBER, GetAllMissionRq.ext, GetAllMissionRq.newBuilder().build());
		//PropCache
		//获取背包数据
		createAction(GetPropBagRq.EXT_FIELD_NUMBER, GetPropBagRs.EXT_FIELD_NUMBER, GetPropBagRq.ext, GetPropBagRq.newBuilder().build());
		//ShopCache
		//获取商店军事和其它数据
		createAction(GetShopRq.EXT_FIELD_NUMBER, GetShopRs.EXT_FIELD_NUMBER, GetShopRq.ext, GetShopRq.newBuilder().build());
		//获取vip礼包数据
		createAction(GetVipGiftRq.EXT_FIELD_NUMBER, GetVipGiftRs.EXT_FIELD_NUMBER, GetVipGiftRq.ext, GetVipGiftRq.newBuilder().build());
		//获取vip特价商品数据
		createAction(GetVipShopRq.EXT_FIELD_NUMBER, GetVipShopRs.EXT_FIELD_NUMBER, GetVipShopRq.ext, GetVipShopRq.newBuilder().build());

		//ActivityCache
		//每日特惠数据请求
		createAction(ActDayPayRq.EXT_FIELD_NUMBER, ActDayPayRs.EXT_FIELD_NUMBER, ActDayPayRq.ext, ActDayPayRq.newBuilder().build());
		//特价礼包新数据请求
		createAction(ActSpecialGiftRq.EXT_FIELD_NUMBER, ActSpecialGiftRs.EXT_FIELD_NUMBER, ActSpecialGiftRq.ext, ActSpecialGiftRq.newBuilder().build());
		//月卡数据请求
		createAction(ActMonthCardRq.EXT_FIELD_NUMBER, ActMonthCardRs.EXT_FIELD_NUMBER, ActMonthCardRq.ext, ActMonthCardRq.newBuilder().build());
		//TechCache
		//获取科技信息
		createAction(GetTechRq.EXT_FIELD_NUMBER, GetTechRs.EXT_FIELD_NUMBER, GetTechRq.ext, GetTechRq.newBuilder().build());
		//TowerDefenceCache
		//请求关卡状态信息
		createAction(TDMapInitRq.EXT_FIELD_NUMBER, TDMapInitRs.EXT_FIELD_NUMBER, TDMapInitRq.ext, TDMapInitRq.newBuilder().build());
		//请求防御塔信息
		createAction(TDTowerInitRq.EXT_FIELD_NUMBER, TDTowerInitRs.EXT_FIELD_NUMBER, TDTowerInitRq.ext, TDTowerInitRq.newBuilder().build());
		//请求无尽模式数据
		createAction(EndlessTowerDefenseInitRq.EXT_FIELD_NUMBER, EndlessTowerDefenseInitRs.EXT_FIELD_NUMBER, EndlessTowerDefenseInitRq.ext, EndlessTowerDefenseInitRq.newBuilder().build());
		//WallCache
		//请求城墙信息
		createAction(GetWallInfoRq.EXT_FIELD_NUMBER, GetWallInfoRs.EXT_FIELD_NUMBER, GetWallInfoRq.ext, GetWallInfoRq.newBuilder().build());
		//WarBookCache
		//请求兵书信息
		createAction(GetWarBookBagRq.EXT_FIELD_NUMBER, GetWarBookBagRs.EXT_FIELD_NUMBER, GetWarBookBagRq.ext, GetWarBookBagRq.newBuilder().build());
		//WorldCache
		//拿到所有伏击叛军的战争请求
		createAction(GetRebelWarRq.EXT_FIELD_NUMBER, GetRebelWarRs.EXT_FIELD_NUMBER, GetRebelWarRq.ext, GetRebelWarRq.newBuilder().build());
		//WorldCache
		//伏击叛军兑换信息请求
		createAction(GetExchangeInfoRq.EXT_FIELD_NUMBER, GetExchangeInfoRs.EXT_FIELD_NUMBER, GetExchangeInfoRq.ext, GetExchangeInfoRq.newBuilder().build());
		//自动清剿数据请求
		createAction(AutoKillMonsterRq.EXT_FIELD_NUMBER, AutoKillMonsterRs.EXT_FIELD_NUMBER, AutoKillMonsterRq.ext, AutoKillMonsterRq.newBuilder().build());

		//WorkShopCache
		//获取生产队列和预设队列请求
		createAction(GetWsQueRq.EXT_FIELD_NUMBER, GetWsQueRs.EXT_FIELD_NUMBER, GetWsQueRq.ext, GetWsQueRq.newBuilder().build());
		//Beauty2DCache
		//新美女列表
		createAction(NewGetBeautyListRq.EXT_FIELD_NUMBER, NewGetBeautyListRs.EXT_FIELD_NUMBER, NewGetBeautyListRq.ext, NewGetBeautyListRq.newBuilder().build());
		//NewPvpCache
		//获取新版母巢之战信息
		createAction(BroodWarInitRq.EXT_FIELD_NUMBER, BroodWarInitRs.EXT_FIELD_NUMBER, BroodWarInitRq.ext, BroodWarInitRq.newBuilder().build());
		//获取任命列表
		createAction(AppointInfoRq.EXT_FIELD_NUMBER, AppointInfoRs.EXT_FIELD_NUMBER, AppointInfoRq.ext, AppointInfoRq.newBuilder().build());

		//战火燎原活动数据
		createAction(FlameWarInitRq.EXT_FIELD_NUMBER, FlameWarInitRs.EXT_FIELD_NUMBER, FlameWarInitRq.ext, FlameWarInitRq.newBuilder().build());
		//战火燎原临时物资
		createAction(FlameBagRq.EXT_FIELD_NUMBER, FlameBagRs.EXT_FIELD_NUMBER, FlameBagRq.ext, FlameBagRq.newBuilder().build());
		createAction(GetTimeRq.EXT_FIELD_NUMBER, GetTimeRs.EXT_FIELD_NUMBER, GetTimeRq.ext, GetTimeRq.newBuilder().build());
		createAction(ActOnlineTimeRq.EXT_FIELD_NUMBER, ActOnlineTimeRs.EXT_FIELD_NUMBER, ActOnlineTimeRq.ext, ActOnlineTimeRq.newBuilder().build());
		createAction(GetCityRq.EXT_FIELD_NUMBER, GetCityRs.EXT_FIELD_NUMBER, GetCityRq.ext, GetCityRq.newBuilder().build());
//		createAction(GetWorldCountryWarRq.EXT_FIELD_NUMBER, GetWorldCountryWarRs.EXT_FIELD_NUMBER, GetWorldCountryWarRq.ext, GetWorldCountryWarRq.newBuilder().build());
		createAction(GetPvpCityRq.EXT_FIELD_NUMBER, GetPvpCityRs.EXT_FIELD_NUMBER, GetPvpCityRq.ext, GetPvpCityRq.newBuilder().build());
		createAction(GetBigMonsterActivityRq.EXT_FIELD_NUMBER, GetBigMonsterActivityRs.EXT_FIELD_NUMBER, GetBigMonsterActivityRq.ext, GetBigMonsterActivityRq.newBuilder().build());

		// 通知服务器登录全部完成
		createAction(EnterGameRq.EXT_FIELD_NUMBER, EnterGameRs.EXT_FIELD_NUMBER, EnterGameRq.ext, EnterGameRq.newBuilder().build());
	}

	@Override
	public void init() {
	}


	/**
	 * 进入游戏
	 *
	 * @param robot
	 */
	public void doEnterGame(Robot robot) {
		loginActions.forEach(e -> {
			e.registerEvent(robot);
		});
	}

	private <T> void createAction(int cmd, int response, GeneratedExtension<Base, T> ext, T msg) {
		Base base = BasePbHelper.createRqBase(cmd, ext, msg).build();
		Packet packet = PacketCreator.create(base);
		EnterGameAction action = new EnterGameAction(response, packet);
		loginActions.add(action);
	}


	private <T> void createGetMailAction(int cmd, int response, GeneratedExtension<Base, T> ext, T msg) {
		Base base = BasePbHelper.createRqBase(cmd, ext, msg).build();
		Packet packet = PacketCreator.create(base);
		GetMailLoginAction action = new GetMailLoginAction(response, packet);
		loginActions.add(action);
	}


}
