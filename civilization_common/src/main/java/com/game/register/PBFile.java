package com.game.register;

import com.game.pb.AccountPb;
import com.game.pb.ActManoeuvrePb;
import com.game.pb.ActivityPb;
import com.game.pb.BeautyPb;
import com.game.pb.BroodWarPb;
import com.game.pb.BuildingPb;
import com.game.pb.CastlePb;
import com.game.pb.ChatPb;
import com.game.pb.CommonPb;
import com.game.pb.CountryPb;
import com.game.pb.DailyTaskPb;
import com.game.pb.DepotPb;
import com.game.pb.EquipPb;
import com.game.pb.FirstBloodPb;
import com.game.pb.FishingPb;
import com.game.pb.FlameWarPb;
import com.game.pb.FriendPb;
import com.game.pb.GmToolPb;
import com.game.pb.HeroPb;
import com.game.pb.InnerPb;
import com.game.pb.JourneyPb;
import com.game.pb.KillEquipPb;
import com.game.pb.MailPb;
import com.game.pb.MapInfoPb;
import com.game.pb.MissionPb;
import com.game.pb.NotifyPb;
import com.game.pb.OmamentPb;
import com.game.pb.PayPb;
import com.game.pb.PropPb;
import com.game.pb.PvpBattlePb;
import com.game.pb.RankPb;
import com.game.pb.RebelPb;
import com.game.pb.RiotPb;
import com.game.pb.RolePb;
import com.game.pb.SettingPb;
import com.game.pb.ShopPb;
import com.game.pb.SkinPb;
import com.game.pb.SoldierPb;
import com.game.pb.StaffPb;
import com.game.pb.TDPb;
import com.game.pb.TaskPb;
import com.game.pb.TechPb;
import com.game.pb.WallPb;
import com.game.pb.WarBookPb;
import com.game.pb.WorkShopPb;
import com.game.pb.WorldBoxPb;
import com.game.pb.WorldPb;
import com.game.pb.ZergPb;
import com.game.util.LogHelper;
import com.google.protobuf.ExtensionRegistry;

/**
 * @Author 陈奎
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
		LogHelper.GAME_LOGGER.info("加载pb文件");
	}

}
