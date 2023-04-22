package com.game.manager;

import com.game.domain.UserClient;
import com.game.packet.Packet;
import com.game.packet.PacketCreator;
import com.game.pb.BasePb.Base;
import com.game.pb.InnerPb.ChannelOfflineRq;
import com.game.pb.RolePb;
import com.game.server.GateServer;
import com.game.util.BasePbHelper;
import com.game.util.LogHelper;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

/**
 * @Author 陈奎
 * @Description 用户客户端管理
 * @Date 2022/9/9 11:30
 **/

public class UserClientManager {

	public static int MAX_CONNECT = 20000;

	public static UserClientManager inst = new UserClientManager();

	public static UserClientManager getInst() {
		return inst;
	}

	public static AtomicInteger maxConnect = new AtomicInteger(0);

	@Getter
	private Map<Long, UserClient> channels = new ConcurrentHashMap<>();

	public void put(long id, UserClient client) {
		channels.put(id, client);
	}

	public void remove(long channelId) {
		channels.remove(channelId);
	}

	public UserClient getChannel(long id) {
		if (channels.containsKey(id)) {
			return channels.get(id);
		}
		return null;
	}

	/**
	 * 通过账号编号
	 *
	 * @param accountKey
	 * @return
	 */
	public UserClient getByAccountKey(int accountKey) {
		return channels.values().stream().filter(e -> e.getAccountKey() == accountKey).findFirst().orElse(null);
	}

	public void offLine(long channelId, long roleId) {
		ChannelOfflineRq.Builder builder = ChannelOfflineRq.newBuilder();
		builder.setChannelId(channelId);
		UserClient userClient = getChannel(channelId);
		if (userClient != null) {
			userClient.setOffTime(System.currentTimeMillis());
			roleId = userClient.getRoleId();
			builder.setUserId(userClient.getRoleId());
		}
		Base.Builder base = BasePbHelper.createRqBase(ChannelOfflineRq.EXT_FIELD_NUMBER, channelId, ChannelOfflineRq.ext, builder.build());
		Packet packet = PacketCreator.create(ChannelOfflineRq.EXT_FIELD_NUMBER, base.build().toByteArray(), roleId, channelId);
		GateServer.getInst().getNet().send(packet);
		LogHelper.GAME_LOGGER.info("offLine channelId:{} playerId:{}", channelId, roleId);
	}

    /**
     * 同一个号多人登录，则挤下线
     *
     * @param channelId
     * @param accountKey
     */
    public void kickClient(int accountKey, long channelId) {
        Iterator<Entry<Long, UserClient>> it = channels.entrySet().iterator();

        RolePb.SynOfflineRq.Builder builder = RolePb.SynOfflineRq.newBuilder();
        builder.setType(1);
        Base.Builder base = BasePbHelper.createRqBase(RolePb.SynOfflineRq.EXT_FIELD_NUMBER, null, RolePb.SynOfflineRq.ext, builder.build());
        Packet packet = PacketCreator.create(base.build());
        while (it.hasNext()) {
            Entry<Long, UserClient> entry = it.next();
            UserClient userClient = entry.getValue();
            if (userClient.getAccountKey() == accountKey && userClient.getChannelId() != channelId) {
                userClient.sendPacket(packet);
                userClient.close();
            }
        }
    }


}
