package com.game.manager;

import com.game.domain.NetState;
import com.game.network.ChannelUtil;
import com.game.network.INet;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RobotNetManager {

	private Map<String, NetState> netStateMap = new ConcurrentHashMap<>();

	public void listen(INet net, ChannelHandlerContext ctx) {
		String netId = ChannelUtil.getNetId(ctx);
		if (netStateMap.containsKey(netId)) {
			return;
		}
		NetState netState = new NetState();
		netState.setNetId(netId);
		netState.setNet(net);
		netState.setHeatBeatTime(System.currentTimeMillis() + 60000);
		netStateMap.put(netId, netState);
	}

	public void remove(String netId) {
		if (netId == null) {
			return;
		}
		netStateMap.remove(netId);
	}

	public Map<String, NetState> getNetStateMap() {
		return netStateMap;
	}


}
