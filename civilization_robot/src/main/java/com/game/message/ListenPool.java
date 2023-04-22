package com.game.message;

import com.game.define.LoadData;
import com.game.load.ILoadData;
import com.game.message.cs.MissionDoneHandler;
import com.game.message.listen.ListenAttackCityHandler;
import com.game.message.listen.ListenAttackRebelHandler;
import com.game.message.listen.ListenAttendCountryHandler;
import com.game.message.listen.ListenAttendCountryWarHandler;
import com.game.message.listen.ListenCountryWarHandler;
import com.game.message.listen.ListenEventHandler;
import com.game.message.listen.ListenGetMapHandler;
import com.game.message.listen.ListenMapMoveHandler;
import com.game.message.listen.ListenMissionDoneHandler;
import com.game.message.listen.ListenNewStateHandler;
import com.game.message.listen.ListenShareMailHandler;
import com.game.message.listen.ListenWearEquipHandler;
import com.game.message.listen.UpdateGuideHandler;
import com.game.pb.BasePb.Base;
import com.game.pb.ChatPb.ShareMailRq;
import com.game.pb.EquipPb.WearEquipRq;
import com.game.pb.InnerPb.ListenEventRs;
import com.game.pb.MissionPb.MissionDoneRq;
import com.game.pb.RolePb.CreateRoleRq;
import com.game.pb.RolePb.GetTimeRq;
import com.game.pb.RolePb.NewStateRq;
import com.game.pb.RolePb.RoleLoginRq;
import com.game.pb.RolePb.UpdateGuideRq;
import com.game.pb.RolePb.UserLoginRq;
import com.game.pb.WorldPb.AttackCityRq;
import com.game.pb.WorldPb.AttackRebelRq;
import com.game.pb.WorldPb.AttendCountryWarRq;
import com.game.pb.WorldPb.CountryWarRq;
import com.game.pb.WorldPb.GetMapRq;
import com.game.pb.WorldPb.MapMoveRq;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
@LoadData(name = "监听消息注册", initSeq = 5)
public class ListenPool implements ILoadData {

	public Map<Integer, IMessageHandler> pools = new HashMap<>();
	// 不需要处理的消息过滤
	public Set<Integer> filters = new HashSet<>();
	// 服务器的信号
	public Set<Integer> appSignals = new HashSet<>();

	@Override
	public void load() {
	}

	@Override
	public void init() {
		this.registerFilter();
		this.registerAppMsg();
		this.registerLogin();
		this.registerListen();
		this.listenEquip();
		this.listenWar();
		this.listenMail();
		this.listenMission();
	}

	public void registerFilter() {
		filters.add(UserLoginRq.EXT_FIELD_NUMBER);
		filters.add(CreateRoleRq.EXT_FIELD_NUMBER);
		filters.add(RoleLoginRq.EXT_FIELD_NUMBER);
		filters.add(GetTimeRq.EXT_FIELD_NUMBER);
	}

	public void registerAppMsg() {
		appSignals.add(ListenEventRs.EXT_FIELD_NUMBER);
	}

	private void registerLogin() {
		register(ListenEventRs.EXT_FIELD_NUMBER, 0, ListenEventHandler.class);
	}

	private void registerListen() {
		register(GetMapRq.EXT_FIELD_NUMBER, 0, ListenGetMapHandler.class);
		register(AttackRebelRq.EXT_FIELD_NUMBER, 0, ListenAttackRebelHandler.class);
		register(AttackCityRq.EXT_FIELD_NUMBER, 0, ListenAttackCityHandler.class);
		register(AttendCountryWarRq.EXT_FIELD_NUMBER, 0, ListenAttendCountryHandler.class);
		register(MapMoveRq.EXT_FIELD_NUMBER, 0, ListenMapMoveHandler.class);
		register(NewStateRq.EXT_FIELD_NUMBER, 0, ListenNewStateHandler.class);
		register(UpdateGuideRq.EXT_FIELD_NUMBER, 0, UpdateGuideHandler.class);
	}

	private void listenEquip() {
		register(WearEquipRq.EXT_FIELD_NUMBER, 0, ListenWearEquipHandler.class);
	}

	private void listenWar() {
		register(CountryWarRq.EXT_FIELD_NUMBER, 0, ListenCountryWarHandler.class);
		register(AttendCountryWarRq.EXT_FIELD_NUMBER, 0, ListenAttendCountryWarHandler.class);
	}

	private void listenMail() {
		register(ShareMailRq.EXT_FIELD_NUMBER, 0, ListenShareMailHandler.class);
	}

	private void listenMission() {
		register(MissionDoneRq.EXT_FIELD_NUMBER, 0, ListenMissionDoneHandler.class);
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

	public boolean handler(ChannelHandlerContext ctx, int cmd, Base msg) {
		IMessageHandler handler = pools.get(cmd);
		if (handler != null) {
			handler.action(ctx, 0, msg);
			return true;
		}
		return false;
	}

	/**
	 * 过滤掉不需要监听的消息
	 *
	 * @param cmd
	 * @return
	 */
	public boolean filter(int cmd) {
		return filters.contains(cmd);
	}

	/**
	 * 应用信号
	 *
	 * @param cmd
	 * @return
	 */
	public boolean isAppSignal(int cmd) {
		return appSignals.contains(cmd);
	}

}
