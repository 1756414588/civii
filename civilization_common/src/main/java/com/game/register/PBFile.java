package com.game.register;

import com.game.pb.*;
import com.game.util.LogHelper;
import com.google.protobuf.ExtensionRegistry;

/**
 *
 * @Description protobuf注册文件
 * @Date 2022/9/9 11:30
 **/

public class PBFile {

	public static PBFile pbFile = new PBFile();

	public static PBFile getInst() {
		return pbFile;
	}


	static public ExtensionRegistry registry = ExtensionRegistry.newInstance();

	public void register() {
		AccountPb.registerAllExtensions(registry);
		CommonPb.registerAllExtensions(registry);
		ActivityPb.registerAllExtensions(registry);
		InnerPb.registerAllExtensions(registry);
		BuildingPb.registerAllExtensions(registry);
		ChatPb.registerAllExtensions(registry);
		EquipPb.registerAllExtensions(registry);
		HeroPb.registerAllExtensions(registry);
		PropPb.registerAllExtensions(registry);
		MailPb.registerAllExtensions(registry);
		KillEquipPb.registerAllExtensions(registry);
		MissionPb.registerAllExtensions(registry);
		NotifyPb.registerAllExtensions(registry);
		RankPb.registerAllExtensions(registry);
		RolePb.registerAllExtensions(registry);
		SettingPb.registerAllExtensions(registry);
		ShopPb.registerAllExtensions(registry);
		SoldierPb.registerAllExtensions(registry);
		TaskPb.registerAllExtensions(registry);
		TechPb.registerAllExtensions(registry);
		WorkShopPb.registerAllExtensions(registry);
		WorldPb.registerAllExtensions(registry);
		DepotPb.registerAllExtensions(registry);
		WallPb.registerAllExtensions(registry);
		GmToolPb.registerAllExtensions(registry);
		CountryPb.registerAllExtensions(registry);
		PvpBattlePb.registerAllExtensions(registry);
		StaffPb.registerAllExtensions(registry);
		RiotPb.registerAllExtensions(registry);
		CastlePb.registerAllExtensions(registry);
		PayPb.registerAllExtensions(registry);
		RebelPb.registerAllExtensions(registry);
		BeautyPb.registerAllExtensions(registry);
		FriendPb.registerAllExtensions(registry);
		OmamentPb.registerAllExtensions(registry);
		JourneyPb.registerAllExtensions(registry);
		TDPb.registerAllExtensions(registry);
		FirstBloodPb.registerAllExtensions(registry);
		WarBookPb.registerAllExtensions(registry);
		WorldBoxPb.registerAllExtensions(registry);
		SkinPb.registerAllExtensions(registry);
		DailyTaskPb.registerAllExtensions(registry);
		BroodWarPb.registerAllExtensions(registry);
		ZergPb.registerAllExtensions(registry);
		ActManoeuvrePb.registerAllExtensions(registry);
		FishingPb.registerAllExtensions(registry);
		FlameWarPb.registerAllExtensions(registry);
		MapInfoPb.registerAllExtensions(registry);
		SeasonActivityPb.registerAllExtensions(registry);
		LogHelper.GAME_LOGGER.info("加载pb文件");
	}

}
