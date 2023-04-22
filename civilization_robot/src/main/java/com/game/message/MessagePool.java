package com.game.message;

import com.game.define.LoadData;
import com.game.load.ILoadData;
import com.game.manager.MessageEventManager;
import com.game.message.cs.AttackRebelHandler;
import com.game.acion.login.CreateRoleHandler;
import com.game.message.cs.DecompoundEquipHandler;
import com.game.message.cs.DoneEquipHandler;
import com.game.message.cs.EmbattleHeroHandler;
import com.game.message.cs.GetEmbattleInfoHandler;
import com.game.message.cs.GetEquipBagHandler;
import com.game.message.cs.GetMarchHandler;
import com.game.message.cs.GetHeroHandler;
import com.game.message.cs.GetMailHandler;
import com.game.message.cs.GetMapHandler;
import com.game.message.cs.GetMapNpcHandler;
import com.game.message.cs.GetStarAwardHandler;
import com.game.message.cs.HeroMissionHandler;
import com.game.message.cs.MissionDoneHandler;
import com.game.message.cs.SynChatHandler;
import com.game.message.cs.SynCountryWarHandler;
import com.game.message.cs.SynMarchHandler;
import com.game.message.cs.TaskAwardHandler;
import com.game.acion.login.BeginGameHander;
import com.game.acion.login.RoleLoginHandler;
import com.game.message.cs.SynEntityAddHandler;
import com.game.message.cs.SynEntityHandler;
import com.game.message.cs.SynEntityUpdateHandler;
import com.game.message.cs.WearEquipHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.BuildingPb.GetBuildingRs;
import com.game.pb.ChatPb.SynChatRq;
import com.game.pb.EquipPb.DecompoundEquipRs;
import com.game.pb.EquipPb.DoneEquipRs;
import com.game.pb.EquipPb.GetEquipBagRs;
import com.game.pb.EquipPb.WearEquipRs;
import com.game.pb.HeroPb.EmbattleHeroRs;
import com.game.pb.HeroPb.GetEmbattleInfoRs;
import com.game.pb.HeroPb.GetHeroRs;
import com.game.pb.MailPb.GetMailRs;
import com.game.pb.MapInfoPb.GetMapNpcRs;
import com.game.pb.MissionPb.GetStarAwardRs;
import com.game.pb.MissionPb.HeroMissionRs;
import com.game.pb.MissionPb.MissionDoneRs;
import com.game.pb.RolePb.CreateRoleRs;
import com.game.pb.RolePb.RoleLoginRs;
import com.game.pb.RolePb.UserLoginRs;
import com.game.pb.TaskPb.TaskAwardRs;
import com.game.pb.WorldPb.AttackRebelRs;
import com.game.pb.WorldPb.GetMapRs;
import com.game.pb.WorldPb.GetMarchRs;
import com.game.pb.WorldPb.SynCountryWarRq;
import com.game.pb.WorldPb.SynEntityAddRq;
import com.game.pb.WorldPb.SynEntityRq;
import com.game.pb.WorldPb.SynEntityUpdateRq;
import com.game.pb.WorldPb.SynMarchRq;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "玩家接收消息注册", initSeq = 5)
public class MessagePool implements ILoadData {

	public Map<Integer, IMessageHandler> pools = new HashMap<>();

	@Autowired
	private MessageEventManager messageEventManager;

	@Override
	public void load() {
	}

	@Override
	public void init() {
		this.registerLogin();
		this.registerWorld();
		this.registerHero();
		this.registerMarch();
		this.registerBag();
		this.regitserTask();
		this.registerMail();
		this.registerBuilding();
	}

	private void registerLogin() {
		register(UserLoginRs.EXT_FIELD_NUMBER, 0, BeginGameHander.class);
		register(CreateRoleRs.EXT_FIELD_NUMBER, 0, CreateRoleHandler.class);
		register(RoleLoginRs.EXT_FIELD_NUMBER, 0, RoleLoginHandler.class);
	}

	private void registerWorld() {
		register(SynEntityRq.EXT_FIELD_NUMBER, 0, SynEntityHandler.class);
		register(GetMapRs.EXT_FIELD_NUMBER, 0, GetMapHandler.class);
		register(SynEntityAddRq.EXT_FIELD_NUMBER, 0, SynEntityAddHandler.class);
		register(SynEntityUpdateRq.EXT_FIELD_NUMBER, 0, SynEntityUpdateHandler.class);
		register(GetMapNpcRs.EXT_FIELD_NUMBER, 0, GetMapNpcHandler.class);
		register(SynCountryWarRq.EXT_FIELD_NUMBER, 0, SynCountryWarHandler.class);
		register(SynChatRq.EXT_FIELD_NUMBER, 0, SynChatHandler.class);
	}

	private void registerHero() {
		register(GetHeroRs.EXT_FIELD_NUMBER, 0, GetHeroHandler.class);
		register(GetEmbattleInfoRs.EXT_FIELD_NUMBER, 0, GetEmbattleInfoHandler.class);
		register(EmbattleHeroRs.EXT_FIELD_NUMBER, 0, EmbattleHeroHandler.class);
		register(HeroMissionRs.EXT_FIELD_NUMBER, 0, HeroMissionHandler.class);
		register(GetStarAwardRs.EXT_FIELD_NUMBER, 0, GetStarAwardHandler.class);
		register(MissionDoneRs.EXT_FIELD_NUMBER, 0, MissionDoneHandler.class);
		register(AttackRebelRs.EXT_FIELD_NUMBER, 0, AttackRebelHandler.class);
		register(AttackRebelRs.EXT_FIELD_NUMBER, 0, AttackRebelHandler.class);
	}

	private void registerMarch() {
		register(GetMarchRs.EXT_FIELD_NUMBER, 0, GetMarchHandler.class);
		register(SynMarchRq.EXT_FIELD_NUMBER, 0, SynMarchHandler.class);
	}

	private void registerBag() {
		register(GetEquipBagRs.EXT_FIELD_NUMBER, 0, GetEquipBagHandler.class);
		register(WearEquipRs.EXT_FIELD_NUMBER, 0, WearEquipHandler.class);
		register(DoneEquipRs.EXT_FIELD_NUMBER, 0, DoneEquipHandler.class);
		register(DecompoundEquipRs.EXT_FIELD_NUMBER, 0, DecompoundEquipHandler.class);
	}

	private void registerBuilding() {
//		register(GetBuildingRs.EXT_FIELD_NUMBER, 0, GetBuildingHandler.class);
	}

	private void regitserTask() {
		register(TaskAwardRs.EXT_FIELD_NUMBER, 0, TaskAwardHandler.class);
	}

	private void registerMail() {
		register(GetMailRs.EXT_FIELD_NUMBER, 0, GetMailHandler.class);
	}

	public void register(int req, int res, Class<? extends MessageHandler> classes) {
		try {
			IMessageHandler handler = classes.newInstance();
			handler.setResponseCmd(res);
			pools.put(req, handler);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}


	public void handler(ChannelHandlerContext ctx, int accountKey, Base msg) {
		IMessageHandler handler = pools.get(msg.getCommand());
		if (handler != null) {
			handler.action(ctx, accountKey, msg);
		}
		// 事件处理
		messageEventManager.callEvent(ctx, accountKey, msg);
	}
}
