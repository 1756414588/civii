package com.game.action;

import com.game.action.cs.UserLoginHandler;
import com.game.action.rs.AppRegisterHandler;
import com.game.action.rs.ListenEventHandler;
import com.game.action.gs.UserLoginResponse;
import com.game.action.gs.RoleLoginResponse;
import com.game.packet.Packet;
import com.game.pb.InnerPb.ListenEventRq;
import com.game.pb.InnerPb.ListenEventRs;
import com.game.pb.InnerPb.RegisterRs;
import com.game.pb.RolePb.RoleLoginRs;
import com.game.pb.RolePb.UserLoginRq;
import com.game.pb.RolePb.UserLoginRs;
import io.netty.channel.ChannelHandlerContext;
import java.util.HashMap;
import java.util.Map;

public class MessagePool {

	public static MessagePool inst = new MessagePool();

	public static MessagePool getInst() {
		return inst;
	}

	protected Map<Integer, IPacketHandler> pools = new HashMap<>();


	public void init() {
		this.registerLogin();
		this.registerGate();
		this.registerRobot();
	}

	public boolean isContain(int cmd) {
		return pools.containsKey(cmd);
	}

	public void registerLogin() {
		register(UserLoginRq.EXT_FIELD_NUMBER, 0, UserLoginHandler.class);
		register(UserLoginRs.EXT_FIELD_NUMBER, 0, UserLoginResponse.class);
//		register(RoleLoginRq.EXT_FIELD_NUMBER, 0, RoleLoginRqHandler.class);
		register(RoleLoginRs.EXT_FIELD_NUMBER, 0, RoleLoginResponse.class);
	}

	public void registerRobot() {
		register(ListenEventRq.EXT_FIELD_NUMBER, ListenEventRs.EXT_FIELD_NUMBER, ListenEventHandler.class);
	}

	public void registerGate() {
		register(RegisterRs.EXT_FIELD_NUMBER, 0, AppRegisterHandler.class);
	}


	public void register(int req, int res, Class<? extends PacketHandler> classes) {
		try {
			IPacketHandler handler = classes.newInstance();
			handler.setResponseCmd(res);
			pools.put(req, handler);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}


	/**
	 *
	 * @param ctx
	 * @param packet
	 * @return
	 */
	public boolean handler(ChannelHandlerContext ctx, Packet packet) {
		IPacketHandler handler = pools.get(packet.getCmd());
		if (handler != null) {
			handler.action(ctx, packet);
			return true;
		}
		return false;
	}
}
